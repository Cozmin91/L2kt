package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q042_HelpTheUncle : Quest(42, "Help the Uncle!") {
    init {

        setItemsIds(MAP_PIECE, MAP)

        addStartNpc(WATERS)
        addTalkId(WATERS, SOPHYA)

        addKillId(MONSTER_EYE_DESTROYER, MONSTER_EYE_GAZER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30828-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30828-03.htm", ignoreCase = true) && st.hasQuestItems(TRIDENT)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TRIDENT, 1)
        } else if (event.equals("30828-05.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAP_PIECE, 30)
            st.giveItems(MAP, 1)
        } else if (event.equals("30735-06.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAP, 1)
        } else if (event.equals("30828-07.htm", ignoreCase = true)) {
            st.giveItems(PET_TICKET, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 25) "30828-00a.htm" else "30828-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    WATERS -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(TRIDENT)) "30828-01a.htm" else "30828-02.htm"
                    else if (cond == 2)
                        htmltext = "30828-03a.htm"
                    else if (cond == 3)
                        htmltext = "30828-04.htm"
                    else if (cond == 4)
                        htmltext = "30828-05a.htm"
                    else if (cond == 5)
                        htmltext = "30828-06.htm"

                    SOPHYA -> if (cond == 4)
                        htmltext = "30735-05.htm"
                    else if (cond == 5)
                        htmltext = "30735-06a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItemsAlways(MAP_PIECE, 1, 30))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q042_HelpTheUncle"

        // NPCs
        private const val WATERS = 30828
        private const val SOPHYA = 30735

        // Items
        private const val TRIDENT = 291
        private const val MAP_PIECE = 7548
        private const val MAP = 7549
        private const val PET_TICKET = 7583

        // Monsters
        private const val MONSTER_EYE_DESTROYER = 20068
        private const val MONSTER_EYE_GAZER = 20266
    }
}