package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q010_IntoTheWorld : Quest(10, "Into the World") {
    init {

        setItemsIds(VERY_EXPENSIVE_NECKLACE)

        addStartNpc(BALANKI)
        addTalkId(BALANKI, REED, GERALD)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30533-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30520-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(VERY_EXPENSIVE_NECKLACE, 1)
        } else if (event.equals("30650-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(VERY_EXPENSIVE_NECKLACE, 1)
        } else if (event.equals("30520-04.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30533-05.htm", ignoreCase = true)) {
            st.giveItems(SOE_GIRAN, 1)
            st.rewardItems(MARK_OF_TRAVELER, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 3 && player.race == ClassRace.DWARF)
                htmltext = "30533-01.htm"
            else
                htmltext = "30533-01a.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BALANKI -> if (cond < 4)
                        htmltext = "30533-03.htm"
                    else if (cond == 4)
                        htmltext = "30533-04.htm"

                    REED -> if (cond == 1)
                        htmltext = "30520-01.htm"
                    else if (cond == 2)
                        htmltext = "30520-02a.htm"
                    else if (cond == 3)
                        htmltext = "30520-03.htm"
                    else if (cond == 4)
                        htmltext = "30520-04a.htm"

                    GERALD -> if (cond == 2)
                        htmltext = "30650-01.htm"
                    else if (cond > 2)
                        htmltext = "30650-04.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q010_IntoTheWorld"

        // Items
        private const val VERY_EXPENSIVE_NECKLACE = 7574

        // Rewards
        private const val SOE_GIRAN = 7559
        private const val MARK_OF_TRAVELER = 7570

        // NPCs
        private const val REED = 30520
        private const val BALANKI = 30533
        private const val GERALD = 30650
    }
}