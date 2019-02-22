package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q232_TestOfTheLord : Quest(232, "Test of the Lord") {
    init {

        setItemsIds(
            VARKEES_CHARM,
            TANTUS_CHARM,
            HATOS_CHARM,
            TAKUNA_CHARM,
            CHIANTA_CHARM,
            MANAKIAS_ORDERS,
            BREKA_ORC_FANG,
            MANAKIAS_AMULET,
            HUGE_ORC_FANG,
            SUMARIS_LETTER,
            URUTU_BLADE,
            TIMAK_ORC_SKULL,
            SWORD_INTO_SKULL,
            NERUGA_AXE_BLADE,
            AXE_OF_CEREMONY,
            MARSH_SPIDER_FEELER,
            MARSH_SPIDER_FEET,
            HANDIWORK_SPIDER_BROOCH,
            MONSTEREYE_CORNEA,
            MONSTEREYE_WOODCARVING,
            BEAR_FANG_NECKLACE,
            MARTANKUS_CHARM,
            RAGNA_ORC_HEAD,
            RAGNA_CHIEF_NOTICE,
            IMMORTAL_FLAME
        )

        addStartNpc(KAKAI)
        addTalkId(
            KAKAI,
            CHIANTA,
            HATOS,
            SOMAK,
            SUMARI,
            TAKUNA,
            TANTUS,
            JAKAL,
            VARKEES,
            MANAKIA,
            ANCESTOR_MARTANKUS,
            FIRST_ORC
        )

        addKillId(20233, 20269, 20270, 20564, 20583, 20584, 20585, 20586, 20587, 20588, 20778, 20779)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30565-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ORDEAL_NECKLACE, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30565-05b.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30565-08.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SWORD_INTO_SKULL, 1)
            st.takeItems(AXE_OF_CEREMONY, 1)
            st.takeItems(MONSTEREYE_WOODCARVING, 1)
            st.takeItems(HANDIWORK_SPIDER_BROOCH, 1)
            st.takeItems(ORDEAL_NECKLACE, 1)
            st.takeItems(HUGE_ORC_FANG, 1)
            st.giveItems(BEAR_FANG_NECKLACE, 1)
        } else if (event.equals("30566-02.htm", ignoreCase = true)) {
            st.giveItems(VARKEES_CHARM, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30567-02.htm", ignoreCase = true)) {
            st.giveItems(TANTUS_CHARM, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30558-02.htm", ignoreCase = true)) {
            st.takeItems(57, 1000)
            st.giveItems(NERUGA_AXE_BLADE, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30568-02.htm", ignoreCase = true)) {
            st.giveItems(HATOS_CHARM, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30641-02.htm", ignoreCase = true)) {
            st.giveItems(TAKUNA_CHARM, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30642-02.htm", ignoreCase = true)) {
            st.giveItems(CHIANTA_CHARM, 1)
            st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30643-02.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            startQuestTimer("f_orc_despawn", 10000, null, player, false)
        } else if (event.equals("30649-04.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BEAR_FANG_NECKLACE, 1)
            st.giveItems(MARTANKUS_CHARM, 1)
        } else if (event.equals("30649-07.htm", ignoreCase = true)) {
            if (_firstOrc == null)
                _firstOrc = addSpawn(FIRST_ORC, 21036, -107690, -3038, 200000, false, 0, true)
        } else if (event.equals("f_orc_despawn", ignoreCase = true)) {
            if (_firstOrc != null) {
                _firstOrc!!.deleteMe()
                _firstOrc = null
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30565-01.htm"
            else if (player.classId != ClassId.ORC_SHAMAN)
                htmltext = "30565-02.htm"
            else if (player.level < 39)
                htmltext = "30565-03.htm"
            else
                htmltext = "30565-04.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VARKEES -> if (st.hasQuestItems(HUGE_ORC_FANG))
                        htmltext = "30566-05.htm"
                    else if (st.hasQuestItems(VARKEES_CHARM)) {
                        if (st.hasQuestItems(MANAKIAS_AMULET)) {
                            htmltext = "30566-04.htm"
                            st.takeItems(VARKEES_CHARM, -1)
                            st.takeItems(MANAKIAS_AMULET, -1)
                            st.giveItems(HUGE_ORC_FANG, 1)

                            if (st.hasQuestItems(
                                    SWORD_INTO_SKULL,
                                    AXE_OF_CEREMONY,
                                    MONSTEREYE_WOODCARVING,
                                    HANDIWORK_SPIDER_BROOCH,
                                    ORDEAL_NECKLACE
                                )
                            ) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30566-03.htm"
                    } else
                        htmltext = "30566-01.htm"

                    MANAKIA -> if (st.hasQuestItems(HUGE_ORC_FANG))
                        htmltext = "30515-05.htm"
                    else if (st.hasQuestItems(MANAKIAS_AMULET))
                        htmltext = "30515-04.htm"
                    else if (st.hasQuestItems(MANAKIAS_ORDERS)) {
                        if (st.getQuestItemsCount(BREKA_ORC_FANG) >= 20) {
                            htmltext = "30515-03.htm"
                            st.takeItems(MANAKIAS_ORDERS, -1)
                            st.takeItems(BREKA_ORC_FANG, -1)
                            st.giveItems(MANAKIAS_AMULET, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30515-02.htm"
                    } else {
                        htmltext = "30515-01.htm"
                        st.giveItems(MANAKIAS_ORDERS, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }

                    TANTUS -> if (st.hasQuestItems(AXE_OF_CEREMONY))
                        htmltext = "30567-05.htm"
                    else if (st.hasQuestItems(TANTUS_CHARM)) {
                        if (st.getQuestItemsCount(BONE_ARROW) >= 1000) {
                            htmltext = "30567-04.htm"
                            st.takeItems(BONE_ARROW, 1000)
                            st.takeItems(NERUGA_AXE_BLADE, 1)
                            st.takeItems(TANTUS_CHARM, 1)
                            st.giveItems(AXE_OF_CEREMONY, 1)

                            if (st.hasQuestItems(
                                    SWORD_INTO_SKULL,
                                    MONSTEREYE_WOODCARVING,
                                    HANDIWORK_SPIDER_BROOCH,
                                    ORDEAL_NECKLACE,
                                    HUGE_ORC_FANG
                                )
                            ) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30567-03.htm"
                    } else
                        htmltext = "30567-01.htm"

                    JAKAL -> if (st.hasQuestItems(AXE_OF_CEREMONY))
                        htmltext = "30558-05.htm"
                    else if (st.hasQuestItems(NERUGA_AXE_BLADE))
                        htmltext = "30558-04.htm"
                    else if (st.hasQuestItems(TANTUS_CHARM)) {
                        if (st.getQuestItemsCount(57) >= 1000)
                            htmltext = "30558-01.htm"
                        else
                            htmltext = "30558-03.htm"
                    }

                    HATOS -> if (st.hasQuestItems(SWORD_INTO_SKULL))
                        htmltext = "30568-05.htm"
                    else if (st.hasQuestItems(HATOS_CHARM)) {
                        if (st.hasQuestItems(URUTU_BLADE) && st.getQuestItemsCount(TIMAK_ORC_SKULL) >= 10) {
                            htmltext = "30568-04.htm"
                            st.takeItems(HATOS_CHARM, 1)
                            st.takeItems(URUTU_BLADE, 1)
                            st.takeItems(TIMAK_ORC_SKULL, -1)
                            st.giveItems(SWORD_INTO_SKULL, 1)

                            if (st.hasQuestItems(
                                    AXE_OF_CEREMONY,
                                    MONSTEREYE_WOODCARVING,
                                    HANDIWORK_SPIDER_BROOCH,
                                    ORDEAL_NECKLACE,
                                    HUGE_ORC_FANG
                                )
                            ) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30568-03.htm"
                    } else
                        htmltext = "30568-01.htm"

                    SUMARI -> if (st.hasQuestItems(URUTU_BLADE))
                        htmltext = "30564-03.htm"
                    else if (st.hasQuestItems(SUMARIS_LETTER))
                        htmltext = "30564-02.htm"
                    else if (st.hasQuestItems(HATOS_CHARM)) {
                        htmltext = "30564-01.htm"
                        st.giveItems(SUMARIS_LETTER, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }

                    SOMAK -> if (st.hasQuestItems(SWORD_INTO_SKULL))
                        htmltext = "30510-03.htm"
                    else if (st.hasQuestItems(URUTU_BLADE))
                        htmltext = "30510-02.htm"
                    else if (st.hasQuestItems(SUMARIS_LETTER)) {
                        htmltext = "30510-01.htm"
                        st.takeItems(SUMARIS_LETTER, 1)
                        st.giveItems(URUTU_BLADE, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }

                    TAKUNA -> if (st.hasQuestItems(HANDIWORK_SPIDER_BROOCH))
                        htmltext = "30641-05.htm"
                    else if (st.hasQuestItems(TAKUNA_CHARM)) {
                        if (st.getQuestItemsCount(MARSH_SPIDER_FEELER) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FEET) >= 10) {
                            htmltext = "30641-04.htm"
                            st.takeItems(MARSH_SPIDER_FEELER, -1)
                            st.takeItems(MARSH_SPIDER_FEET, -1)
                            st.takeItems(TAKUNA_CHARM, 1)
                            st.giveItems(HANDIWORK_SPIDER_BROOCH, 1)

                            if (st.hasQuestItems(
                                    SWORD_INTO_SKULL,
                                    AXE_OF_CEREMONY,
                                    MONSTEREYE_WOODCARVING,
                                    ORDEAL_NECKLACE,
                                    HUGE_ORC_FANG
                                )
                            ) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30641-03.htm"
                    } else
                        htmltext = "30641-01.htm"

                    CHIANTA -> if (st.hasQuestItems(MONSTEREYE_WOODCARVING))
                        htmltext = "30642-05.htm"
                    else if (st.hasQuestItems(CHIANTA_CHARM)) {
                        if (st.getQuestItemsCount(MONSTEREYE_CORNEA) >= 20) {
                            htmltext = "30642-04.htm"
                            st.takeItems(MONSTEREYE_CORNEA, -1)
                            st.takeItems(CHIANTA_CHARM, 1)
                            st.giveItems(MONSTEREYE_WOODCARVING, 1)

                            if (st.hasQuestItems(
                                    SWORD_INTO_SKULL,
                                    AXE_OF_CEREMONY,
                                    HANDIWORK_SPIDER_BROOCH,
                                    ORDEAL_NECKLACE,
                                    HUGE_ORC_FANG
                                )
                            ) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30642-03.htm"
                    } else
                        htmltext = "30642-01.htm"

                    KAKAI -> if (cond == 1)
                        htmltext = "30565-06.htm"
                    else if (cond == 2)
                        htmltext = "30565-07.htm"
                    else if (cond == 3)
                        htmltext = "30565-09.htm"
                    else if (cond > 3 && cond < 7)
                        htmltext = "30565-10.htm"
                    else if (cond == 7) {
                        htmltext = "30565-11.htm"

                        st.takeItems(IMMORTAL_FLAME, 1)
                        st.giveItems(MARK_LORD, 1)
                        st.rewardExpAndSp(92955, 16250)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ANCESTOR_MARTANKUS -> if (cond == 3)
                        htmltext = "30649-01.htm"
                    else if (cond == 4)
                        htmltext = "30649-05.htm"
                    else if (cond == 5) {
                        htmltext = "30649-06.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)

                        st.takeItems(MARTANKUS_CHARM, 1)
                        st.takeItems(RAGNA_ORC_HEAD, 1)
                        st.takeItems(RAGNA_CHIEF_NOTICE, 1)
                        st.giveItems(IMMORTAL_FLAME, 1)
                    } else if (cond == 6)
                        htmltext = "30649-07.htm"
                    else if (cond == 7)
                        htmltext = "30649-08.htm"

                    FIRST_ORC -> if (cond == 6)
                        htmltext = "30643-01.htm"
                    else if (cond == 7)
                        htmltext = "30643-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20564 -> if (st.hasQuestItems(CHIANTA_CHARM))
                st.dropItemsAlways(MONSTEREYE_CORNEA, 1, 20)

            20583, 20584, 20585 -> if (st.hasQuestItems(HATOS_CHARM))
                st.dropItems(TIMAK_ORC_SKULL, 1, 10, 710000)

            20586 -> if (st.hasQuestItems(HATOS_CHARM))
                st.dropItems(TIMAK_ORC_SKULL, 1, 10, 810000)

            20587, 20588 -> if (st.hasQuestItems(HATOS_CHARM))
                st.dropItemsAlways(TIMAK_ORC_SKULL, 1, 10)

            20233 -> if (st.hasQuestItems(TAKUNA_CHARM))
                st.dropItemsAlways(
                    if (st.getQuestItemsCount(MARSH_SPIDER_FEELER) >= 10) MARSH_SPIDER_FEET else MARSH_SPIDER_FEELER,
                    1,
                    10
                )

            20269 -> if (st.hasQuestItems(MANAKIAS_ORDERS))
                st.dropItems(BREKA_ORC_FANG, 1, 20, 410000)

            20270 -> if (st.hasQuestItems(MANAKIAS_ORDERS))
                st.dropItems(BREKA_ORC_FANG, 1, 20, 510000)

            20778, 20779 -> if (st.hasQuestItems(MARTANKUS_CHARM)) {
                if (!st.hasQuestItems(RAGNA_CHIEF_NOTICE)) {
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RAGNA_CHIEF_NOTICE, 1)
                } else if (!st.hasQuestItems(RAGNA_ORC_HEAD)) {
                    st["cond"] = "5"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RAGNA_ORC_HEAD, 1)
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q232_TestOfTheLord"

        // NPCs
        private val SOMAK = 30510
        private val MANAKIA = 30515
        private val JAKAL = 30558
        private val SUMARI = 30564
        private val KAKAI = 30565
        private val VARKEES = 30566
        private val TANTUS = 30567
        private val HATOS = 30568
        private val TAKUNA = 30641
        private val CHIANTA = 30642
        private val FIRST_ORC = 30643
        private val ANCESTOR_MARTANKUS = 30649

        // Items
        private val ORDEAL_NECKLACE = 3391
        private val VARKEES_CHARM = 3392
        private val TANTUS_CHARM = 3393
        private val HATOS_CHARM = 3394
        private val TAKUNA_CHARM = 3395
        private val CHIANTA_CHARM = 3396
        private val MANAKIAS_ORDERS = 3397
        private val BREKA_ORC_FANG = 3398
        private val MANAKIAS_AMULET = 3399
        private val HUGE_ORC_FANG = 3400
        private val SUMARIS_LETTER = 3401
        private val URUTU_BLADE = 3402
        private val TIMAK_ORC_SKULL = 3403
        private val SWORD_INTO_SKULL = 3404
        private val NERUGA_AXE_BLADE = 3405
        private val AXE_OF_CEREMONY = 3406
        private val MARSH_SPIDER_FEELER = 3407
        private val MARSH_SPIDER_FEET = 3408
        private val HANDIWORK_SPIDER_BROOCH = 3409
        private val MONSTEREYE_CORNEA = 3410
        private val MONSTEREYE_WOODCARVING = 3411
        private val BEAR_FANG_NECKLACE = 3412
        private val MARTANKUS_CHARM = 3413
        private val RAGNA_ORC_HEAD = 3414
        private val RAGNA_CHIEF_NOTICE = 3415
        private val BONE_ARROW = 1341
        private val IMMORTAL_FLAME = 3416

        // Rewards
        private val MARK_LORD = 3390
        private val DIMENSIONAL_DIAMOND = 7562

        private var _firstOrc: Npc? = null // Used to avoid to spawn multiple instances.
    }
}