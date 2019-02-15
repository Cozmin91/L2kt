package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q124_MeetingTheElroki : Quest(124, "Meeting the Elroki") {
    init {

        addStartNpc(MARQUEZ)
        addTalkId(MARQUEZ, MUSHIKA, ASAMAH, KARAKAWEI, MANTARASA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("32113-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32113-04.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32114-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32115-04.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32117-02.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 4)
                st["progress"] = "1"
        } else if (event.equals("32117-03.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32118-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(8778, 1) // Egg
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "32113-01a.htm" else "32113-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MARQUEZ -> if (cond == 1)
                        htmltext = "32113-03.htm"
                    else if (cond > 1)
                        htmltext = "32113-04a.htm"

                    MUSHIKA -> if (cond == 2)
                        htmltext = "32114-01.htm"
                    else if (cond > 2)
                        htmltext = "32114-03.htm"

                    ASAMAH -> if (cond == 3)
                        htmltext = "32115-01.htm"
                    else if (cond == 6) {
                        htmltext = "32115-05.htm"
                        st.takeItems(8778, -1)
                        st.rewardItems(57, 71318)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    KARAKAWEI -> if (cond == 4) {
                        htmltext = "32117-01.htm"
                        if (st.getInt("progress") == 1)
                            htmltext = "32117-02.htm"
                    } else if (cond > 4)
                        htmltext = "32117-04.htm"

                    MANTARASA -> if (cond == 5)
                        htmltext = "32118-01.htm"
                    else if (cond > 5)
                        htmltext = "32118-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> if (npc.npcId == ASAMAH)
                htmltext = "32115-06.htm"
            else
                htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        const val qn = "Q124_MeetingTheElroki"

        // NPCs
        private const val MARQUEZ = 32113
        private const val MUSHIKA = 32114
        private const val ASAMAH = 32115
        private const val KARAKAWEI = 32117
        private const val MANTARASA = 32118
    }
}