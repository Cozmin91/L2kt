package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q043_HelpTheSister : Quest(43, "Help the Sister!") {
    init {

        setItemsIds(MAP_PIECE, MAP)

        addStartNpc(COOPER)
        addTalkId(COOPER, GALLADUCCI)

        addKillId(SPECTER, SORROW_MAIDEN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30829-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30829-03.htm", ignoreCase = true) && st.hasQuestItems(CRAFTED_DAGGER)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRAFTED_DAGGER, 1)
        } else if (event.equals("30829-05.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAP_PIECE, 30)
            st.giveItems(MAP, 1)
        } else if (event.equals("30097-06.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAP, 1)
        } else if (event.equals("30829-07.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 26) "30829-00a.htm" else "30829-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    COOPER -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(CRAFTED_DAGGER)) "30829-01a.htm" else "30829-02.htm"
                    else if (cond == 2)
                        htmltext = "30829-03a.htm"
                    else if (cond == 3)
                        htmltext = "30829-04.htm"
                    else if (cond == 4)
                        htmltext = "30829-05a.htm"
                    else if (cond == 5)
                        htmltext = "30829-06.htm"

                    GALLADUCCI -> if (cond == 4)
                        htmltext = "30097-05.htm"
                    else if (cond == 5)
                        htmltext = "30097-06a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItemsAlways(MAP_PIECE, 1, 30))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q043_HelpTheSister"

        // NPCs
        private const val COOPER = 30829
        private const val GALLADUCCI = 30097

        // Items
        private const val CRAFTED_DAGGER = 220
        private const val MAP_PIECE = 7550
        private const val MAP = 7551
        private const val PET_TICKET = 7584

        // Monsters
        private const val SPECTER = 20171
        private const val SORROW_MAIDEN = 20197
    }
}