package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q115_TheOtherSideOfTruth : Quest(115, "The Other Side of Truth") {
    init {
        NPC_VALUES[32021] = intArrayOf(1, 2, 1, 6, 10, 12, 14)
        NPC_VALUES[32077] = intArrayOf(2, 4, 1, 5, 9, 12, 13)
        NPC_VALUES[32078] = intArrayOf(4, 8, 3, 3, 9, 10, 11)
        NPC_VALUES[32079] = intArrayOf(8, 0, 7, 3, 5, 6, 7)
    }

    init {

        setItemsIds(MISA_LETTER, RAFFORTY_LETTER, PIECE_OF_TABLET, REPORT_PIECE)

        addStartNpc(RAFFORTY)
        addTalkId(RAFFORTY, MISA, KIERRE, SCULPTURE_1, SCULPTURE_2, SCULPTURE_3, SCULPTURE_4)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32020-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32020-05.htm", ignoreCase = true) || event.equals(
                "32020-08.htm",
                ignoreCase = true
            ) || event.equals("32020-13.htm", ignoreCase = true)
        ) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("32020-07.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MISA_LETTER, 1)
        } else if (event.equals("32020-11.htm", ignoreCase = true) || event.equals("32020-12.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 3) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("32020-17.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32020-23.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(REPORT_PIECE, 1)
        } else if (event.equals("32020-27.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(PIECE_OF_TABLET)) {
                st["cond"] = "11"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else {
                htmltext = "32020-25.htm"
                st.takeItems(PIECE_OF_TABLET, 1)
                st.rewardItems(57, 60040)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        } else if (event.equals("32020-28.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(PIECE_OF_TABLET)) {
                st["cond"] = "11"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else {
                htmltext = "32020-26.htm"
                st.takeItems(PIECE_OF_TABLET, 1)
                st.rewardItems(57, 60040)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        } else if (event.equals("32018-05.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RAFFORTY_LETTER, 1)
        } else if (event.equals("sculpture-03.htm", ignoreCase = true)) {
            val infos = NPC_VALUES[npc!!.npcId] ?: return null
            val ex = st.getInt("ex")
            val numberToModulo = if (infos[1] == 0) ex else ex % infos[1]

            if (numberToModulo <= infos[2]) {
                if (ex == infos[3] || ex == infos[4] || ex == infos[5]) {
                    st["ex"] = (ex + infos[0]).toString()
                    st.giveItems(PIECE_OF_TABLET, 1)
                    st.playSound(QuestState.SOUND_ITEMGET)
                }
            }
        } else if (event.equals("sculpture-04.htm", ignoreCase = true)) {
            val infos = NPC_VALUES[npc!!.npcId] ?: return null
            val ex = st.getInt("ex")
            val numberToModulo = if (infos[1] == 0) ex else ex % infos[1]

            if (numberToModulo <= infos[2])
                if (ex == infos[3] || ex == infos[4] || ex == infos[5])
                    st["ex"] = (ex + infos[0]).toString()
        } else if (event.equals("sculpture-06.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)

            // Spawn a suspicious man broadcasting a message, which dissapear few seconds later broadcasting a second message.
            val stranger = addSpawn(SUSPICIOUS_MAN, player.x + 50, player.y + 50, player.z, 0, false, 3100, false)
            stranger!!.broadcastNpcSay("This looks like the right place...")

            startQuestTimer("despawn_1", 3000, stranger, player, false)
        } else if (event.equals("32022-02.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(REPORT_PIECE, 1)

            // Spawn a suspicious man broadcasting a message, which dissapear few seconds later broadcasting a second message.
            val stranger = addSpawn(SUSPICIOUS_MAN, player.x + 50, player.y + 50, player.z, 0, false, 5100, false)
            stranger!!.broadcastNpcSay("We meet again.")

            startQuestTimer("despawn_2", 5000, stranger, player, false)
        } else if (event.equals("despawn_1", ignoreCase = true)) {
            npc!!.broadcastNpcSay("I see someone. Is this fate?")
            return null
        } else if (event.equals("despawn_2", ignoreCase = true)) {
            npc!!.broadcastNpcSay("Don't bother trying to find out more about me. Follow your own destiny.")
            return null
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 53) "32020-02.htm" else "32020-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")

                when (npc.npcId) {
                    RAFFORTY -> if (cond == 1)
                        htmltext = "32020-04.htm"
                    else if (cond == 2)
                        htmltext = "32020-06.htm"
                    else if (cond == 3)
                        htmltext = "32020-09.htm"
                    else if (cond == 4)
                        htmltext = "32020-16.htm"
                    else if (cond == 5) {
                        htmltext = "32020-18.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(RAFFORTY_LETTER, 1)
                    } else if (cond == 6) {
                        if (!st.hasQuestItems(RAFFORTY_LETTER)) {
                            htmltext = "32020-20.htm"
                            st.giveItems(RAFFORTY_LETTER, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "32020-19.htm"
                    } else if (cond == 7)
                        htmltext = "32020-19.htm"
                    else if (cond == 8)
                        htmltext = "32020-21.htm"
                    else if (cond == 9)
                        htmltext = "32020-22.htm"
                    else if (cond == 10)
                        htmltext = "32020-24.htm"
                    else if (cond == 11)
                        htmltext = "32020-29.htm"
                    else if (cond == 12) {
                        htmltext = "32020-30.htm"
                        st.takeItems(PIECE_OF_TABLET, 1)
                        st.rewardItems(57, 60040)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    MISA -> if (cond == 1) {
                        htmltext = "32018-02.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(MISA_LETTER, 1)
                    } else if (cond == 2)
                        htmltext = "32018-03.htm"
                    else if (cond == 6)
                        htmltext = "32018-04.htm"
                    else if (cond > 6)
                        htmltext = "32018-06.htm"
                    else
                        htmltext = "32018-01.htm"

                    KIERRE -> if (cond == 8)
                        htmltext = "32022-01.htm"
                    else if (cond == 9) {
                        if (!st.hasQuestItems(REPORT_PIECE)) {
                            htmltext = "32022-04.htm"
                            st.giveItems(REPORT_PIECE, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "32022-03.htm"
                    } else if (cond == 11)
                        htmltext = "32022-05.htm"

                    SCULPTURE_1, SCULPTURE_2, SCULPTURE_3, SCULPTURE_4 -> if (cond == 7) {
                        val infos = NPC_VALUES[npc.npcId] ?: return null
                        val ex = st.getInt("ex")
                        val numberToModulo = if (infos[1] == 0) ex else ex % infos[1]

                        if (numberToModulo <= infos[2]) {
                            if (ex == infos[3] || ex == infos[4] || ex == infos[5])
                                htmltext = "sculpture-02.htm"
                            else if (ex == infos[6])
                                htmltext = "sculpture-05.htm"
                            else {
                                st["ex"] = (ex + infos[0]).toString()
                                htmltext = "sculpture-01.htm"
                            }
                        } else
                            htmltext = "sculpture-01a.htm"
                    } else if (cond > 7 && cond < 11)
                        htmltext = "sculpture-07.htm"
                    else if (cond == 11) {
                        if (!st.hasQuestItems(PIECE_OF_TABLET)) {
                            htmltext = "sculpture-08.htm"
                            st["cond"] = "12"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.giveItems(PIECE_OF_TABLET, 1)
                        } else
                            htmltext = "sculpture-09.htm"
                    } else if (cond == 12)
                        htmltext = "sculpture-09.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q115_TheOtherSideOfTruth"

        // Items
        private const val MISA_LETTER = 8079
        private const val RAFFORTY_LETTER = 8080
        private const val PIECE_OF_TABLET = 8081
        private const val REPORT_PIECE = 8082

        // NPCs
        private const val RAFFORTY = 32020
        private const val MISA = 32018
        private const val KIERRE = 32022
        private const val SCULPTURE_1 = 32021
        private const val SCULPTURE_2 = 32077
        private const val SCULPTURE_3 = 32078
        private const val SCULPTURE_4 = 32079
        private const val SUSPICIOUS_MAN = 32019

        // Used to test progression through sculptures. The array consists of value to add, used modulo, tested modulo value, tested values 1/2/3/4.
        private val NPC_VALUES = HashMap<Int, IntArray>()
    }
}