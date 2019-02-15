package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q002_WhatWomenWant : Quest(2, "What Women Want") {
    init {

        setItemsIds(ARUJIEN_LETTER_1, ARUJIEN_LETTER_2, ARUJIEN_LETTER_3, POETRY_BOOK, GREENIS_LETTER)

        addStartNpc(ARUJIEN)
        addTalkId(ARUJIEN, MIRABEL, HERBIEL, GREENIS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30223-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ARUJIEN_LETTER_1, 1)
        } else if (event.equals("30223-08.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARUJIEN_LETTER_3, 1)
            st.giveItems(POETRY_BOOK, 1)
        } else if (event.equals("30223-09.htm", ignoreCase = true)) {
            st.takeItems(ARUJIEN_LETTER_3, 1)
            st.rewardItems(57, 450)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF && player.race != ClassRace.HUMAN)
                htmltext = "30223-00.htm"
            else if (player.level < 2)
                htmltext = "30223-01.htm"
            else
                htmltext = "30223-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ARUJIEN -> if (st.hasQuestItems(ARUJIEN_LETTER_1))
                        htmltext = "30223-05.htm"
                    else if (st.hasQuestItems(ARUJIEN_LETTER_3))
                        htmltext = "30223-07.htm"
                    else if (st.hasQuestItems(ARUJIEN_LETTER_2))
                        htmltext = "30223-06.htm"
                    else if (st.hasQuestItems(POETRY_BOOK))
                        htmltext = "30223-11.htm"
                    else if (st.hasQuestItems(GREENIS_LETTER)) {
                        htmltext = "30223-10.htm"
                        st.takeItems(GREENIS_LETTER, 1)
                        st.giveItems(MYSTICS_EARRING, 1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    MIRABEL -> if (cond == 1) {
                        htmltext = "30146-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ARUJIEN_LETTER_1, 1)
                        st.giveItems(ARUJIEN_LETTER_2, 1)
                    } else if (cond > 1)
                        htmltext = "30146-02.htm"

                    HERBIEL -> if (cond == 2) {
                        htmltext = "30150-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ARUJIEN_LETTER_2, 1)
                        st.giveItems(ARUJIEN_LETTER_3, 1)
                    } else if (cond > 2)
                        htmltext = "30150-02.htm"

                    GREENIS -> if (cond < 4)
                        htmltext = "30157-01.htm"
                    else if (cond == 4) {
                        htmltext = "30157-02.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(POETRY_BOOK, 1)
                        st.giveItems(GREENIS_LETTER, 1)
                    } else if (cond == 5)
                        htmltext = "30157-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q002_WhatWomenWant"

        // NPCs
        private const val ARUJIEN = 30223
        private const val MIRABEL = 30146
        private const val HERBIEL = 30150
        private const val GREENIS = 30157

        // Items
        private const val ARUJIEN_LETTER_1 = 1092
        private const val ARUJIEN_LETTER_2 = 1093
        private const val ARUJIEN_LETTER_3 = 1094
        private const val POETRY_BOOK = 689
        private const val GREENIS_LETTER = 693

        // Rewards
        private const val MYSTICS_EARRING = 113
    }
}