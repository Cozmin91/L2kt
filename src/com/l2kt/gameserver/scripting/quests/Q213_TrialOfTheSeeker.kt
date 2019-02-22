package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q213_TrialOfTheSeeker : Quest(213, "Trial of the Seeker") {
    init {

        setItemsIds(
            DUFNER_LETTER,
            TERRY_ORDER_1,
            TERRY_ORDER_2,
            TERRY_LETTER,
            VIKTOR_LETTER,
            HAWKEYE_LETTER,
            MYSTERIOUS_RUNESTONE,
            OL_MAHUM_RUNESTONE,
            TUREK_RUNESTONE,
            ANT_RUNESTONE,
            TURAK_BUGBEAR_RUNESTONE,
            TERRY_BOX,
            VIKTOR_REQUEST,
            MEDUSA_SCALES,
            SHILEN_RUNESTONE,
            ANALYSIS_REQUEST,
            MARINA_LETTER,
            EXPERIMENT_TOOLS,
            ANALYSIS_RESULT,
            TERRY_ORDER_3,
            LIST_OF_HOST,
            ABYSS_RUNESTONE_1,
            ABYSS_RUNESTONE_2,
            ABYSS_RUNESTONE_3,
            ABYSS_RUNESTONE_4,
            TERRY_REPORT
        )

        addStartNpc(DUFNER)
        addTalkId(TERRY, DUFNER, BRUNON, VIKTOR, MARINA)

        addKillId(
            NEER_GHOUL_BERSERKER,
            ANT_CAPTAIN,
            OL_MAHUM_CAPTAIN,
            TURAK_BUGBEAR_WARRIOR,
            TUREK_ORC_WARLORD,
            ANT_WARRIOR_CAPTAIN,
            MARSH_STAKATO_DRONE,
            BREKA_ORC_OVERLORD,
            LETO_LIZARDMAN_WARRIOR,
            MEDUSA
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // DUFNER
        if (event.equals("30106-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(DUFNER_LETTER, 1)

            if (!player.memos.getBool("secondClassChange35", false)) {
                htmltext = "30106-05a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                player.memos.set("secondClassChange35", true)
            }
        } else if (event.equals("30064-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(DUFNER_LETTER, 1)
            st.giveItems(TERRY_ORDER_1, 1)
        } else if (event.equals("30064-06.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MYSTERIOUS_RUNESTONE, 1)
            st.takeItems(TERRY_ORDER_1, 1)
            st.giveItems(TERRY_ORDER_2, 1)
        } else if (event.equals("30064-10.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ANT_RUNESTONE, 1)
            st.takeItems(OL_MAHUM_RUNESTONE, 1)
            st.takeItems(TURAK_BUGBEAR_RUNESTONE, 1)
            st.takeItems(TUREK_RUNESTONE, 1)
            st.takeItems(TERRY_ORDER_2, 1)
            st.giveItems(TERRY_BOX, 1)
            st.giveItems(TERRY_LETTER, 1)
        } else if (event.equals("30064-18.htm", ignoreCase = true)) {
            if (player.level < 36) {
                htmltext = "30064-17.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(ANALYSIS_RESULT, 1)
                st.giveItems(TERRY_ORDER_3, 1)
            } else {
                st["cond"] = "16"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(ANALYSIS_RESULT, 1)
                st.giveItems(LIST_OF_HOST, 1)
            }
        } else if (event.equals("30684-05.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TERRY_LETTER, 1)
            st.giveItems(VIKTOR_LETTER, 1)
        } else if (event.equals("30684-11.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TERRY_LETTER, 1)
            st.takeItems(TERRY_BOX, 1)
            st.takeItems(HAWKEYE_LETTER, 1)
            st.takeItems(VIKTOR_LETTER, 1)
            st.giveItems(VIKTOR_REQUEST, 1)
        } else if (event.equals("30684-15.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(VIKTOR_REQUEST, 1)
            st.takeItems(MEDUSA_SCALES, 10)
            st.giveItems(ANALYSIS_REQUEST, 1)
            st.giveItems(SHILEN_RUNESTONE, 1)
        } else if (event.equals("30715-02.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SHILEN_RUNESTONE, 1)
            st.takeItems(ANALYSIS_REQUEST, 1)
            st.giveItems(MARINA_LETTER, 1)
        } else if (event.equals("30715-05.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(EXPERIMENT_TOOLS, 1)
            st.giveItems(ANALYSIS_RESULT, 1)
        }// MARINA
        // VIKTOR
        // TERRY

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId == ClassId.ROGUE || player.classId == ClassId.ELVEN_SCOUT || player.classId == ClassId.ASSASSIN)
                htmltext = if (player.level < 35) "30106-02.htm" else "30106-03.htm"
            else
                htmltext = "30106-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    DUFNER -> if (cond == 1)
                        htmltext = "30106-06.htm"
                    else if (cond > 1) {
                        if (!st.hasQuestItems(TERRY_REPORT))
                            htmltext = "30106-07.htm"
                        else {
                            htmltext = "30106-08.htm"
                            st.takeItems(TERRY_REPORT, 1)
                            st.giveItems(MARK_OF_SEEKER, 1)
                            st.rewardExpAndSp(72126, 11000)
                            player.broadcastPacket(SocialAction(player, 3))
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(false)
                        }
                    }

                    TERRY -> if (cond == 1)
                        htmltext = "30064-01.htm"
                    else if (cond == 2)
                        htmltext = "30064-04.htm"
                    else if (cond == 3)
                        htmltext = "30064-05.htm"
                    else if (cond == 4)
                        htmltext = "30064-08.htm"
                    else if (cond == 5)
                        htmltext = "30064-09.htm"
                    else if (cond == 6)
                        htmltext = "30064-11.htm"
                    else if (cond == 7) {
                        htmltext = "30064-12.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(VIKTOR_LETTER, 1)
                        st.giveItems(HAWKEYE_LETTER, 1)
                    } else if (cond == 8)
                        htmltext = "30064-13.htm"
                    else if (cond > 8 && cond < 14)
                        htmltext = "30064-14.htm"
                    else if (cond == 14) {
                        if (!st.hasQuestItems(TERRY_ORDER_3))
                            htmltext = "30064-15.htm"
                        else if (player.level < 36)
                            htmltext = "30064-20.htm"
                        else {
                            htmltext = "30064-21.htm"
                            st["cond"] = "15"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(TERRY_ORDER_3, 1)
                            st.giveItems(LIST_OF_HOST, 1)
                        }
                    } else if (cond == 15 || cond == 16)
                        htmltext = "30064-22.htm"
                    else if (cond == 17) {
                        if (!st.hasQuestItems(TERRY_REPORT)) {
                            htmltext = "30064-23.htm"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(LIST_OF_HOST, 1)
                            st.takeItems(ABYSS_RUNESTONE_1, 1)
                            st.takeItems(ABYSS_RUNESTONE_2, 1)
                            st.takeItems(ABYSS_RUNESTONE_3, 1)
                            st.takeItems(ABYSS_RUNESTONE_4, 1)
                            st.giveItems(TERRY_REPORT, 1)
                        } else
                            htmltext = "30064-24.htm"
                    }

                    VIKTOR -> if (cond == 6)
                        htmltext = "30684-01.htm"
                    else if (cond == 7)
                        htmltext = "30684-05.htm"
                    else if (cond == 8)
                        htmltext = "30684-12.htm"
                    else if (cond == 9)
                        htmltext = "30684-13.htm"
                    else if (cond == 10)
                        htmltext = "30684-14.htm"
                    else if (cond == 11)
                        htmltext = "30684-16.htm"
                    else if (cond > 11)
                        htmltext = "30684-17.htm"

                    MARINA -> if (cond == 11)
                        htmltext = "30715-01.htm"
                    else if (cond == 12)
                        htmltext = "30715-03.htm"
                    else if (cond == 13)
                        htmltext = "30715-04.htm"
                    else if (st.hasQuestItems(ANALYSIS_RESULT))
                        htmltext = "30715-06.htm"

                    BRUNON -> if (cond == 12) {
                        htmltext = "30526-01.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MARINA_LETTER, 1)
                        st.giveItems(EXPERIMENT_TOOLS, 1)
                    } else if (cond == 13)
                        htmltext = "30526-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            NEER_GHOUL_BERSERKER -> if (cond == 2 && st.dropItems(MYSTERIOUS_RUNESTONE, 1, 1, 100000))
                st["cond"] = "3"

            ANT_CAPTAIN -> if (cond == 4 && st.dropItems(ANT_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(
                    OL_MAHUM_RUNESTONE,
                    TURAK_BUGBEAR_RUNESTONE,
                    TUREK_RUNESTONE
                )
            )
                st["cond"] = "5"

            OL_MAHUM_CAPTAIN -> if (cond == 4 && st.dropItems(OL_MAHUM_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(
                    ANT_RUNESTONE,
                    TURAK_BUGBEAR_RUNESTONE,
                    TUREK_RUNESTONE
                )
            )
                st["cond"] = "5"

            TURAK_BUGBEAR_WARRIOR -> if (cond == 4 && st.dropItems(
                    TURAK_BUGBEAR_RUNESTONE,
                    1,
                    1,
                    250000
                ) && st.hasQuestItems(ANT_RUNESTONE, OL_MAHUM_RUNESTONE, TUREK_RUNESTONE)
            )
                st["cond"] = "5"

            TUREK_ORC_WARLORD -> if (cond == 4 && st.dropItems(TUREK_RUNESTONE, 1, 1, 250000) && st.hasQuestItems(
                    ANT_RUNESTONE,
                    OL_MAHUM_RUNESTONE,
                    TURAK_BUGBEAR_RUNESTONE
                )
            )
                st["cond"] = "5"

            MEDUSA -> if (cond == 9 && st.dropItems(MEDUSA_SCALES, 1, 10, 300000))
                st["cond"] = "10"

            MARSH_STAKATO_DRONE -> if ((cond == 15 || cond == 16) && st.dropItems(
                    ABYSS_RUNESTONE_1,
                    1,
                    1,
                    250000
                ) && st.hasQuestItems(ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_3, ABYSS_RUNESTONE_4)
            )
                st["cond"] = "17"

            BREKA_ORC_OVERLORD -> if ((cond == 15 || cond == 16) && st.dropItems(
                    ABYSS_RUNESTONE_2,
                    1,
                    1,
                    250000
                ) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_3, ABYSS_RUNESTONE_4)
            )
                st["cond"] = "17"

            ANT_WARRIOR_CAPTAIN -> if ((cond == 15 || cond == 16) && st.dropItems(
                    ABYSS_RUNESTONE_3,
                    1,
                    1,
                    250000
                ) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_4)
            )
                st["cond"] = "17"

            LETO_LIZARDMAN_WARRIOR -> if ((cond == 15 || cond == 16) && st.dropItems(
                    ABYSS_RUNESTONE_4,
                    1,
                    1,
                    250000
                ) && st.hasQuestItems(ABYSS_RUNESTONE_1, ABYSS_RUNESTONE_2, ABYSS_RUNESTONE_3)
            )
                st["cond"] = "17"
        }

        return null
    }

    companion object {
        private val qn = "Q213_TrialOfTheSeeker"

        // Items
        private val DUFNER_LETTER = 2647
        private val TERRY_ORDER_1 = 2648
        private val TERRY_ORDER_2 = 2649
        private val TERRY_LETTER = 2650
        private val VIKTOR_LETTER = 2651
        private val HAWKEYE_LETTER = 2652
        private val MYSTERIOUS_RUNESTONE = 2653
        private val OL_MAHUM_RUNESTONE = 2654
        private val TUREK_RUNESTONE = 2655
        private val ANT_RUNESTONE = 2656
        private val TURAK_BUGBEAR_RUNESTONE = 2657
        private val TERRY_BOX = 2658
        private val VIKTOR_REQUEST = 2659
        private val MEDUSA_SCALES = 2660
        private val SHILEN_RUNESTONE = 2661
        private val ANALYSIS_REQUEST = 2662
        private val MARINA_LETTER = 2663
        private val EXPERIMENT_TOOLS = 2664
        private val ANALYSIS_RESULT = 2665
        private val TERRY_ORDER_3 = 2666
        private val LIST_OF_HOST = 2667
        private val ABYSS_RUNESTONE_1 = 2668
        private val ABYSS_RUNESTONE_2 = 2669
        private val ABYSS_RUNESTONE_3 = 2670
        private val ABYSS_RUNESTONE_4 = 2671
        private val TERRY_REPORT = 2672

        // Rewards
        private val MARK_OF_SEEKER = 2673
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val TERRY = 30064
        private val DUFNER = 30106
        private val BRUNON = 30526
        private val VIKTOR = 30684
        private val MARINA = 30715

        // Monsters
        private val NEER_GHOUL_BERSERKER = 20198
        private val ANT_CAPTAIN = 20080
        private val OL_MAHUM_CAPTAIN = 20211
        private val TURAK_BUGBEAR_WARRIOR = 20249
        private val TUREK_ORC_WARLORD = 20495
        private val MEDUSA = 20158
        private val ANT_WARRIOR_CAPTAIN = 20088
        private val MARSH_STAKATO_DRONE = 20234
        private val BREKA_ORC_OVERLORD = 20270
        private val LETO_LIZARDMAN_WARRIOR = 20580
    }
}