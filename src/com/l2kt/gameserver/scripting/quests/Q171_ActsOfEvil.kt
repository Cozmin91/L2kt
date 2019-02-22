package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q171_ActsOfEvil : Quest(171, "Acts of Evil") {
    init {
        CHANCES[20496] = 530000
        CHANCES[20497] = 550000
        CHANCES[20498] = 510000
        CHANCES[20499] = 500000
    }

    init {

        setItemsIds(
            BLADE_MOLD,
            TYRA_BILL,
            RANGER_REPORT_1,
            RANGER_REPORT_2,
            RANGER_REPORT_3,
            RANGER_REPORT_4,
            WEAPON_TRADE_CONTRACT,
            ATTACK_DIRECTIVES,
            CERTIFICATE,
            CARGO_BOX,
            OL_MAHUM_HEAD
        )

        addStartNpc(ALVAH)
        addTalkId(ALVAH, ARODIN, TYRA, ROLENTO, NETI, BURAI)

        addKillId(20496, 20497, 20498, 20499, 20062, 20064, 20066, 20438)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30381-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30207-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30381-04.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30381-07.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(WEAPON_TRADE_CONTRACT, 1)
        } else if (event.equals("30437-03.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CARGO_BOX, 1)
            st.giveItems(CERTIFICATE, 1)
        } else if (event.equals("30617-04.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ATTACK_DIRECTIVES, 1)
            st.takeItems(CARGO_BOX, 1)
            st.takeItems(CERTIFICATE, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 27) "30381-01a.htm" else "30381-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ALVAH -> if (cond < 4)
                        htmltext = "30381-02a.htm"
                    else if (cond == 4)
                        htmltext = "30381-03.htm"
                    else if (cond == 5) {
                        if (st.hasQuestItems(RANGER_REPORT_1, RANGER_REPORT_2, RANGER_REPORT_3, RANGER_REPORT_4)) {
                            htmltext = "30381-05.htm"
                            st["cond"] = "6"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(RANGER_REPORT_1, 1)
                            st.takeItems(RANGER_REPORT_2, 1)
                            st.takeItems(RANGER_REPORT_3, 1)
                            st.takeItems(RANGER_REPORT_4, 1)
                        } else
                            htmltext = "30381-04a.htm"
                    } else if (cond == 6) {
                        if (st.hasQuestItems(WEAPON_TRADE_CONTRACT, ATTACK_DIRECTIVES))
                            htmltext = "30381-06.htm"
                        else
                            htmltext = "30381-05a.htm"
                    } else if (cond > 6 && cond < 11)
                        htmltext = "30381-07a.htm"
                    else if (cond == 11) {
                        htmltext = "30381-08.htm"
                        st.rewardItems(57, 90000)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ARODIN -> if (cond == 1)
                        htmltext = "30207-01.htm"
                    else if (cond == 2)
                        htmltext = "30207-01a.htm"
                    else if (cond == 3) {
                        if (st.hasQuestItems(TYRA_BILL)) {
                            htmltext = "30207-03.htm"
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(TYRA_BILL, 1)
                        } else
                            htmltext = "30207-01a.htm"
                    } else if (cond > 3)
                        htmltext = "30207-03a.htm"

                    TYRA -> if (cond == 2) {
                        if (st.getQuestItemsCount(BLADE_MOLD) >= 20) {
                            htmltext = "30420-01.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(BLADE_MOLD, -1)
                            st.giveItems(TYRA_BILL, 1)
                        } else
                            htmltext = "30420-01b.htm"
                    } else if (cond == 3)
                        htmltext = "30420-01a.htm"
                    else if (cond > 3)
                        htmltext = "30420-02.htm"

                    NETI -> if (cond == 7) {
                        htmltext = "30425-01.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 7)
                        htmltext = "30425-02.htm"

                    ROLENTO -> if (cond == 8)
                        htmltext = "30437-01.htm"
                    else if (cond > 8)
                        htmltext = "30437-03a.htm"

                    BURAI -> if (cond == 9 && st.hasQuestItems(CERTIFICATE, CARGO_BOX, ATTACK_DIRECTIVES))
                        htmltext = "30617-01.htm"
                    else if (cond == 10) {
                        if (st.getQuestItemsCount(OL_MAHUM_HEAD) >= 30) {
                            htmltext = "30617-05.htm"
                            st["cond"] = "11"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(OL_MAHUM_HEAD, -1)
                            st.rewardItems(57, 8000)
                        } else
                            htmltext = "30617-04a.htm"
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        when (npcId) {
            20496, 20497, 20498, 20499 -> if (st.getInt("cond") == 2 && !st.dropItems(
                    BLADE_MOLD,
                    1,
                    20,
                    CHANCES[npcId] ?: return null
                )
            ) {
                val count = st.getQuestItemsCount(BLADE_MOLD)
                if (count == 5 || count >= 10 && Rnd[100] < 25)
                    addSpawn(27190, player!!, false, 0, true)
            }

            20062, 20064 -> if (st.getInt("cond") == 5) {
                if (!st.hasQuestItems(RANGER_REPORT_1)) {
                    st.giveItems(RANGER_REPORT_1, 1)
                    st.playSound(QuestState.SOUND_ITEMGET)
                } else if (Rnd[100] < 20) {
                    if (!st.hasQuestItems(RANGER_REPORT_2)) {
                        st.giveItems(RANGER_REPORT_2, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (!st.hasQuestItems(RANGER_REPORT_3)) {
                        st.giveItems(RANGER_REPORT_3, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (!st.hasQuestItems(RANGER_REPORT_4)) {
                        st.giveItems(RANGER_REPORT_4, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }
                }
            }

            20438 -> if (st.getInt("cond") == 6 && Rnd[100] < 10 && !st.hasQuestItems(
                    WEAPON_TRADE_CONTRACT,
                    ATTACK_DIRECTIVES
                )
            ) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(WEAPON_TRADE_CONTRACT, 1)
                st.giveItems(ATTACK_DIRECTIVES, 1)
            }

            20066 -> if (st.getInt("cond") == 10)
                st.dropItems(OL_MAHUM_HEAD, 1, 30, 500000)
        }

        return null
    }

    companion object {
        private val qn = "Q171_ActsOfEvil"

        // Items
        private val BLADE_MOLD = 4239
        private val TYRA_BILL = 4240
        private val RANGER_REPORT_1 = 4241
        private val RANGER_REPORT_2 = 4242
        private val RANGER_REPORT_3 = 4243
        private val RANGER_REPORT_4 = 4244
        private val WEAPON_TRADE_CONTRACT = 4245
        private val ATTACK_DIRECTIVES = 4246
        private val CERTIFICATE = 4247
        private val CARGO_BOX = 4248
        private val OL_MAHUM_HEAD = 4249

        // NPCs
        private val ALVAH = 30381
        private val ARODIN = 30207
        private val TYRA = 30420
        private val ROLENTO = 30437
        private val NETI = 30425
        private val BURAI = 30617

        // Turek Orcs drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}