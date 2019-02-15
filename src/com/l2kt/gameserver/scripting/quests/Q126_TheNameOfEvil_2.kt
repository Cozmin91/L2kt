package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q126_TheNameOfEvil_2 : Quest(126, "The Name of Evil - 2") {
    init {

        addStartNpc(ASAMANAH)
        addTalkId(ASAMANAH, MUSHIKA, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU, WARRIOR_GRAVE, SHILEN_STONE_STATUE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32115-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32115-10.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32119-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32119-09.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32119-11.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32120-07.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32120-09.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32120-11.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32121-07.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32121-10.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32121-15.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32122-03.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32122-15.htm", ignoreCase = true)) {
            st["cond"] = "13"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32122-18.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32122-87.htm", ignoreCase = true))
            st.giveItems(BONEPOWDER, 1)
        else if (event.equals("32122-90.htm", ignoreCase = true)) {
            st["cond"] = "18"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32109-02.htm", ignoreCase = true)) {
            st["cond"] = "19"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32109-19.htm", ignoreCase = true)) {
            st["cond"] = "20"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BONEPOWDER, 1)
        } else if (event.equals("32115-21.htm", ignoreCase = true)) {
            st["cond"] = "21"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32115-28.htm", ignoreCase = true)) {
            st["cond"] = "22"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32114-08.htm", ignoreCase = true)) {
            st["cond"] = "23"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32114-09.htm", ignoreCase = true)) {
            st.giveItems(EWA, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("DOOne", ignoreCase = true)) {
            htmltext = "32122-26.htm"
            if (st.getInt("DO") < 1)
                st["DO"] = "1"
        } else if (event.equals("MIOne", ignoreCase = true)) {
            htmltext = "32122-30.htm"
            if (st.getInt("MI") < 1)
                st["MI"] = "1"
        } else if (event.equals("FAOne", ignoreCase = true)) {
            htmltext = "32122-34.htm"
            if (st.getInt("FA") < 1)
                st["FA"] = "1"
        } else if (event.equals("SOLOne", ignoreCase = true)) {
            htmltext = "32122-38.htm"
            if (st.getInt("SOL") < 1)
                st["SOL"] = "1"
        } else if (event.equals("FA_2One", ignoreCase = true)) {
            if (st.getInt("FA_2") < 1)
                st["FA_2"] = "1"
            htmltext = getSongOne(st)
        } else if (event.equals("FATwo", ignoreCase = true)) {
            htmltext = "32122-47.htm"
            if (st.getInt("FA") < 1)
                st["FA"] = "1"
        } else if (event.equals("SOLTwo", ignoreCase = true)) {
            htmltext = "32122-51.htm"
            if (st.getInt("SOL") < 1)
                st["SOL"] = "1"
        } else if (event.equals("TITwo", ignoreCase = true)) {
            htmltext = "32122-55.htm"
            if (st.getInt("TI") < 1)
                st["TI"] = "1"
        } else if (event.equals("SOL_2Two", ignoreCase = true)) {
            htmltext = "32122-59.htm"
            if (st.getInt("SOL_2") < 1)
                st["SOL_2"] = "1"
        } else if (event.equals("FA_2Two", ignoreCase = true)) {
            if (st.getInt("FA_2") < 1)
                st["FA_2"] = "1"
            htmltext = getSongTwo(st)
        } else if (event.equals("SOLTri", ignoreCase = true)) {
            htmltext = "32122-68.htm"
            if (st.getInt("SOL") < 1)
                st["SOL"] = "1"
        } else if (event.equals("FATri", ignoreCase = true)) {
            htmltext = "32122-72.htm"
            if (st.getInt("FA") < 1)
                st["FA"] = "1"
        } else if (event.equals("MITri", ignoreCase = true)) {
            htmltext = "32122-76.htm"
            if (st.getInt("MI") < 1)
                st["MI"] = "1"
        } else if (event.equals("FA_2Tri", ignoreCase = true)) {
            htmltext = "32122-80.htm"
            if (st.getInt("FA_2") < 1)
                st["FA_2"] = "1"
        } else if (event.equals("MI_2Tri", ignoreCase = true)) {
            if (st.getInt("MI_2") < 1)
                st["MI_2"] = "1"
            htmltext = getSongTri(st)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 77)
                htmltext = "32115-02.htm"
            else {
                val st2 = player.getQuestState(Q125_TheNameOfEvil_1.qn)
                if (st2 != null && st2.isCompleted)
                    htmltext = "32115-01.htm"
                else
                    htmltext = "32115-04.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ASAMANAH -> if (cond == 1) {
                        htmltext = "32115-11.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 1 && cond < 20)
                        htmltext = "32115-12.htm"
                    else if (cond == 20)
                        htmltext = "32115-13.htm"
                    else if (cond == 21)
                        htmltext = "32115-22.htm"
                    else if (cond == 22)
                        htmltext = "32115-29.htm"

                    ULU_KAIMU -> if (cond == 1)
                        htmltext = "32119-01a.htm"
                    else if (cond == 2)
                        htmltext = "32119-02.htm"
                    else if (cond == 3)
                        htmltext = "32119-08.htm"
                    else if (cond == 4)
                        htmltext = "32119-09.htm"
                    else if (cond > 4)
                        htmltext = "32119-12.htm"

                    BALU_KAIMU -> if (cond < 5)
                        htmltext = "32120-02.htm"
                    else if (cond == 5)
                        htmltext = "32120-01.htm"
                    else if (cond == 6)
                        htmltext = "32120-03.htm"
                    else if (cond == 7)
                        htmltext = "32120-08.htm"
                    else if (cond > 7)
                        htmltext = "32120-12.htm"

                    CHUTA_KAIMU -> if (cond < 8)
                        htmltext = "32121-02.htm"
                    else if (cond == 8)
                        htmltext = "32121-01.htm"
                    else if (cond == 9)
                        htmltext = "32121-03.htm"
                    else if (cond == 10)
                        htmltext = "32121-10.htm"
                    else if (cond > 10)
                        htmltext = "32121-16.htm"

                    WARRIOR_GRAVE -> if (cond < 11)
                        htmltext = "32122-02.htm"
                    else if (cond == 11)
                        htmltext = "32122-01.htm"
                    else if (cond == 12)
                        htmltext = "32122-15.htm"
                    else if (cond == 13)
                        htmltext = "32122-18.htm"
                    else if (cond == 14)
                        htmltext = "32122-24.htm"
                    else if (cond == 15)
                        htmltext = "32122-45.htm"
                    else if (cond == 16)
                        htmltext = "32122-66.htm"
                    else if (cond == 17)
                        htmltext = "32122-84.htm"
                    else if (cond == 18)
                        htmltext = "32122-91.htm"

                    SHILEN_STONE_STATUE -> when {
                        cond < 18 -> htmltext = "32109-03.htm"
                        cond == 18 -> htmltext = "32109-02.htm"
                        cond == 19 -> htmltext = "32109-05.htm"
                        cond > 19 -> htmltext = "32109-04.htm"
                    }

                    MUSHIKA -> when {
                        cond < 22 -> htmltext = "32114-02.htm"
                        cond == 22 -> htmltext = "32114-01.htm"
                        cond == 23 -> htmltext = "32114-04.htm"
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        const val qn = "Q126_TheNameOfEvil_2"

        private const val MUSHIKA = 32114
        private const val ASAMANAH = 32115
        private const val ULU_KAIMU = 32119
        private const val BALU_KAIMU = 32120
        private const val CHUTA_KAIMU = 32121
        private const val WARRIOR_GRAVE = 32122
        private const val SHILEN_STONE_STATUE = 32109

        private const val BONEPOWDER = 8783
        private const val EWA = 729

        private fun getSongOne(st: QuestState): String {
            var htmltext = "32122-24.htm"
            if (st.getInt("cond") == 14 && st.getInt("DO") > 0 && st.getInt("MI") > 0 && st.getInt("FA") > 0 && st.getInt(
                    "SOL"
                ) > 0 && st.getInt("FA_2") > 0
            ) {
                htmltext = "32122-42.htm"
                st["cond"] = "15"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.unset("DO")
                st.unset("MI")
                st.unset("FA")
                st.unset("SOL")
                st.unset("FA_2")
            }
            return htmltext
        }

        private fun getSongTwo(st: QuestState): String {
            var htmltext = "32122-45.htm"
            if (st.getInt("cond") == 15 && st.getInt("FA") > 0 && st.getInt("SOL") > 0 && st.getInt("TI") > 0 && st.getInt(
                    "SOL_2"
                ) > 0 && st.getInt("FA_2") > 0
            ) {
                htmltext = "32122-63.htm"
                st["cond"] = "16"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.unset("FA")
                st.unset("SOL")
                st.unset("TI")
                st.unset("SOL_2")
                st.unset("FA3_2")
            }
            return htmltext
        }

        private fun getSongTri(st: QuestState): String {
            var htmltext = "32122-66.htm"
            if (st.getInt("cond") == 16 && st.getInt("SOL") > 0 && st.getInt("FA") > 0 && st.getInt("MI") > 0 && st.getInt(
                    "FA_2"
                ) > 0 && st.getInt("MI_2") > 0
            ) {
                htmltext = "32122-84.htm"
                st["cond"] = "17"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.unset("SOL")
                st.unset("FA")
                st.unset("MI")
                st.unset("FA_2")
                st.unset("MI_2")
            }
            return htmltext
        }
    }
}