package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q031_SecretBuriedInTheSwamp : Quest(31, "Secret Buried in the Swamp") {
    init {

        setItemsIds(KRORIN_JOURNAL)

        addStartNpc(ABERCROMBIE)
        addTalkId(
            ABERCROMBIE,
            CORPSE_OF_DWARF,
            FORGOTTEN_MONUMENT_1,
            FORGOTTEN_MONUMENT_2,
            FORGOTTEN_MONUMENT_3,
            FORGOTTEN_MONUMENT_4
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31555-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31665-01.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(KRORIN_JOURNAL, 1)
        } else if (event.equals("31555-04.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31661-01.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31662-01.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31663-01.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31664-01.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31555-07.htm", ignoreCase = true)) {
            st.takeItems(KRORIN_JOURNAL, 1)
            st.rewardItems(57, 40000)
            st.rewardExpAndSp(130000, 0)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 66) "31555-00a.htm" else "31555-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ABERCROMBIE -> if (cond == 1)
                        htmltext = "31555-02.htm"
                    else if (cond == 2)
                        htmltext = "31555-03.htm"
                    else if (cond in 3..6)
                        htmltext = "31555-05.htm"
                    else if (cond == 7)
                        htmltext = "31555-06.htm"

                    CORPSE_OF_DWARF -> if (cond == 1)
                        htmltext = "31665-00.htm"
                    else if (cond > 1)
                        htmltext = "31665-02.htm"

                    FORGOTTEN_MONUMENT_1 -> if (cond == 3)
                        htmltext = "31661-00.htm"
                    else if (cond > 3)
                        htmltext = "31661-02.htm"

                    FORGOTTEN_MONUMENT_2 -> if (cond == 4)
                        htmltext = "31662-00.htm"
                    else if (cond > 4)
                        htmltext = "31662-02.htm"

                    FORGOTTEN_MONUMENT_3 -> if (cond == 5)
                        htmltext = "31663-00.htm"
                    else if (cond > 5)
                        htmltext = "31663-02.htm"

                    FORGOTTEN_MONUMENT_4 -> if (cond == 6)
                        htmltext = "31664-00.htm"
                    else if (cond > 6)
                        htmltext = "31664-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q031_SecretBuriedInTheSwamp"

        // Item
        private const val KRORIN_JOURNAL = 7252

        // NPCs
        private const val ABERCROMBIE = 31555
        private const val FORGOTTEN_MONUMENT_1 = 31661
        private const val FORGOTTEN_MONUMENT_2 = 31662
        private const val FORGOTTEN_MONUMENT_3 = 31663
        private const val FORGOTTEN_MONUMENT_4 = 31664
        private const val CORPSE_OF_DWARF = 31665
    }
}