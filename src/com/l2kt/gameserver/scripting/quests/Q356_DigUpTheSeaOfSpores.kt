package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q356_DigUpTheSeaOfSpores : Quest(356, "Dig Up the Sea of Spores!") {
    init {

        setItemsIds(HERB_SPORE, CARN_SPORE)

        addStartNpc(30717) // Gauen
        addTalkId(30717)

        addKillId(ROTTING_TREE, SPORE_ZOMBIE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30717-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30717-17.htm", ignoreCase = true)) {
            st.takeItems(HERB_SPORE, -1)
            st.takeItems(CARN_SPORE, -1)
            st.rewardItems(57, 20950)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30717-14.htm", ignoreCase = true)) {
            st.takeItems(HERB_SPORE, -1)
            st.takeItems(CARN_SPORE, -1)
            st.rewardExpAndSp(35000, 2600)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30717-12.htm", ignoreCase = true)) {
            st.takeItems(HERB_SPORE, -1)
            st.rewardExpAndSp(24500, 0)
        } else if (event.equals("30717-13.htm", ignoreCase = true)) {
            st.takeItems(CARN_SPORE, -1)
            st.rewardExpAndSp(0, 1820)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 43) "30717-01.htm" else "30717-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30717-07.htm"
                else if (cond == 2) {
                    if (st.getQuestItemsCount(HERB_SPORE) >= 50)
                        htmltext = "30717-08.htm"
                    else if (st.getQuestItemsCount(CARN_SPORE) >= 50)
                        htmltext = "30717-09.htm"
                    else
                        htmltext = "30717-07.htm"
                } else if (cond == 3)
                    htmltext = "30717-10.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        if (cond < 3) {
            when (npc.npcId) {
                ROTTING_TREE -> if (st.dropItems(HERB_SPORE, 1, 50, 630000))
                    st["cond"] = if (cond == 2) "3" else "2"

                SPORE_ZOMBIE -> if (st.dropItems(CARN_SPORE, 1, 50, 760000))
                    st["cond"] = if (cond == 2) "3" else "2"
            }
        }

        return null
    }

    companion object {
        private val qn = "Q356_DigUpTheSeaOfSpores"

        // Items
        private val HERB_SPORE = 5866
        private val CARN_SPORE = 5865

        // Monsters
        private val ROTTING_TREE = 20558
        private val SPORE_ZOMBIE = 20562
    }
}