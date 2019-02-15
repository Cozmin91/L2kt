package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q363_SorrowfulSoundOfFlute : Quest(363, "Sorrowful Sound of Flute") {
    init {

        setItemsIds(NANARIN_FLUTE, BLACK_BEER, CLOTHES)

        addStartNpc(NANARIN)
        addTalkId(NANARIN, OPIX, ALDO, RANSPO, HOLVAS, BARBADO, POITAN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30956-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30956-05.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CLOTHES, 1)
        } else if (event.equals("30956-06.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(NANARIN_FLUTE, 1)
        } else if (event.equals("30956-07.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BLACK_BEER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30956-03.htm" else "30956-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    NANARIN -> if (cond == 1)
                        htmltext = "30956-02.htm"
                    else if (cond == 2)
                        htmltext = "30956-04.htm"
                    else if (cond == 3)
                        htmltext = "30956-08.htm"
                    else if (cond == 4) {
                        if (st.getInt("success") == 1) {
                            htmltext = "30956-09.htm"
                            st.giveItems(THEME_OF_SOLITUDE, 1)
                            st.playSound(QuestState.SOUND_FINISH)
                        } else {
                            htmltext = "30956-10.htm"
                            st.playSound(QuestState.SOUND_GIVEUP)
                        }
                        st.exitQuest(true)
                    }

                    OPIX, POITAN, ALDO, RANSPO, HOLVAS -> {
                        htmltext = npc.npcId.toString() + "-01.htm"
                        if (cond == 1) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    }

                    BARBADO -> if (cond == 3) {
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)

                        if (st.hasQuestItems(NANARIN_FLUTE)) {
                            htmltext = "30959-02.htm"
                            st["success"] = "1"
                        } else
                            htmltext = "30959-01.htm"

                        st.takeItems(BLACK_BEER, -1)
                        st.takeItems(CLOTHES, -1)
                        st.takeItems(NANARIN_FLUTE, -1)
                    } else if (cond == 4)
                        htmltext = "30959-03.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q363_SorrowfulSoundOfFlute"

        // NPCs
        private val NANARIN = 30956
        private val OPIX = 30595
        private val ALDO = 30057
        private val RANSPO = 30594
        private val HOLVAS = 30058
        private val BARBADO = 30959
        private val POITAN = 30458

        // Item
        private val NANARIN_FLUTE = 4319
        private val BLACK_BEER = 4320
        private val CLOTHES = 4318

        // Reward
        private val THEME_OF_SOLITUDE = 4420
    }
}