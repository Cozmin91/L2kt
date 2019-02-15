package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q228_TestOfMagus : Quest(228, "Test Of Magus") {
    init {

        setItemsIds(
            RUKAL_LETTER,
            PARINA_LETTER,
            LILAC_CHARM,
            GOLDEN_SEED_1,
            GOLDEN_SEED_2,
            GOLDEN_SEED_3,
            SCORE_OF_ELEMENTS,
            DAZZLING_DROP,
            FLAME_CRYSTAL,
            HARPY_FEATHER,
            WYRM_WINGBONE,
            WINDSUS_MANE,
            EN_MONSTEREYE_SHELL,
            EN_STONEGOLEM_POWDER,
            EN_IRONGOLEM_SCRAP,
            TONE_OF_WATER,
            TONE_OF_FIRE,
            TONE_OF_WIND,
            TONE_OF_EARTH,
            SALAMANDER_CHARM,
            SYLPH_CHARM,
            UNDINE_CHARM,
            SERPENT_CHARM
        )

        addStartNpc(RUKAL)
        addTalkId(PARINA, EARTH_SNAKE, FLAME_SALAMANDER, WIND_SYLPH, WATER_UNDINE, CASIAN, RUKAL)

        addKillId(
            HARPY,
            MARSH_STAKATO,
            WYRM,
            MARSH_STAKATO_WORKER,
            TOAD_LORD,
            MARSH_STAKATO_SOLDIER,
            MARSH_STAKATO_DRONE,
            WINDSUS,
            ENCHANTED_MONSTEREYE,
            ENCHANTED_STONE_GOLEM,
            ENCHANTED_IRON_GOLEM,
            SINGING_FLOWER_PHANTASM,
            SINGING_FLOWER_NIGHTMARE,
            SINGING_FLOWER_DARKLING,
            GHOST_FIRE
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // RUKAL
        if (event.equals("30629-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(RUKAL_LETTER, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30629-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30629-10.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GOLDEN_SEED_1, 1)
            st.takeItems(GOLDEN_SEED_2, 1)
            st.takeItems(GOLDEN_SEED_3, 1)
            st.takeItems(LILAC_CHARM, 1)
            st.giveItems(SCORE_OF_ELEMENTS, 1)
        } else if (event.equals("30391-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RUKAL_LETTER, 1)
            st.giveItems(PARINA_LETTER, 1)
        } else if (event.equals("30612-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PARINA_LETTER, 1)
            st.giveItems(LILAC_CHARM, 1)
        } else if (event.equals("30412-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(SYLPH_CHARM, 1)
        } else if (event.equals("30409-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(SERPENT_CHARM, 1)
        }// EARTH SNAKE
        // WIND SYLPH
        // CASIAN
        // PARINA

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.HUMAN_WIZARD && player.classId != ClassId.ELVEN_WIZARD && player.classId != ClassId.DARK_WIZARD)
                htmltext = "30629-01.htm"
            else if (player.level < 39)
                htmltext = "30629-02.htm"
            else
                htmltext = "30629-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    RUKAL -> if (cond == 1)
                        htmltext = "30629-05.htm"
                    else if (cond == 2)
                        htmltext = "30629-06.htm"
                    else if (cond == 3)
                        htmltext = "30629-07.htm"
                    else if (cond == 4)
                        htmltext = "30629-08.htm"
                    else if (cond == 5)
                        htmltext = "30629-11.htm"
                    else if (cond == 6) {
                        htmltext = "30629-12.htm"
                        st.takeItems(SCORE_OF_ELEMENTS, 1)
                        st.takeItems(TONE_OF_EARTH, 1)
                        st.takeItems(TONE_OF_FIRE, 1)
                        st.takeItems(TONE_OF_WATER, 1)
                        st.takeItems(TONE_OF_WIND, 1)
                        st.giveItems(MARK_OF_MAGUS, 1)
                        st.rewardExpAndSp(139039, 40000)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    PARINA -> if (cond == 1)
                        htmltext = "30391-01.htm"
                    else if (cond == 2)
                        htmltext = "30391-03.htm"
                    else if (cond == 3 || cond == 4)
                        htmltext = "30391-04.htm"
                    else if (cond > 4)
                        htmltext = "30391-05.htm"

                    CASIAN -> if (cond == 2)
                        htmltext = "30612-01.htm"
                    else if (cond == 3)
                        htmltext = "30612-03.htm"
                    else if (cond == 4)
                        htmltext = "30612-04.htm"
                    else if (cond > 4)
                        htmltext = "30612-05.htm"

                    WATER_UNDINE -> if (cond == 5) {
                        if (st.hasQuestItems(UNDINE_CHARM)) {
                            if (st.getQuestItemsCount(DAZZLING_DROP) < 20)
                                htmltext = "30413-02.htm"
                            else {
                                htmltext = "30413-03.htm"
                                st.takeItems(DAZZLING_DROP, 20)
                                st.takeItems(UNDINE_CHARM, 1)
                                st.giveItems(TONE_OF_WATER, 1)

                                if (st.hasQuestItems(TONE_OF_FIRE, TONE_OF_WIND, TONE_OF_EARTH)) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        } else if (!st.hasQuestItems(TONE_OF_WATER)) {
                            htmltext = "30413-01.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.giveItems(UNDINE_CHARM, 1)
                        } else
                            htmltext = "30413-04.htm"
                    } else if (cond == 6)
                        htmltext = "30413-04.htm"

                    FLAME_SALAMANDER -> if (cond == 5) {
                        if (st.hasQuestItems(SALAMANDER_CHARM)) {
                            if (st.getQuestItemsCount(FLAME_CRYSTAL) < 5)
                                htmltext = "30411-02.htm"
                            else {
                                htmltext = "30411-03.htm"
                                st.takeItems(FLAME_CRYSTAL, 5)
                                st.takeItems(SALAMANDER_CHARM, 1)
                                st.giveItems(TONE_OF_FIRE, 1)

                                if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_WIND, TONE_OF_EARTH)) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        } else if (!st.hasQuestItems(TONE_OF_FIRE)) {
                            htmltext = "30411-01.htm"
                            st.giveItems(SALAMANDER_CHARM, 1)
                        } else
                            htmltext = "30411-04.htm"
                    } else if (cond == 6)
                        htmltext = "30411-04.htm"

                    WIND_SYLPH -> if (cond == 5) {
                        if (st.hasQuestItems(SYLPH_CHARM)) {
                            if (st.getQuestItemsCount(HARPY_FEATHER) + st.getQuestItemsCount(WYRM_WINGBONE) + st.getQuestItemsCount(
                                    WINDSUS_MANE
                                ) < 40
                            )
                                htmltext = "30412-03.htm"
                            else {
                                htmltext = "30412-04.htm"
                                st.takeItems(HARPY_FEATHER, 20)
                                st.takeItems(SYLPH_CHARM, 1)
                                st.takeItems(WINDSUS_MANE, 10)
                                st.takeItems(WYRM_WINGBONE, 10)
                                st.giveItems(TONE_OF_WIND, 1)

                                if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_FIRE, TONE_OF_EARTH)) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        } else if (!st.hasQuestItems(TONE_OF_WIND))
                            htmltext = "30412-01.htm"
                        else
                            htmltext = "30412-05.htm"
                    } else if (cond == 6)
                        htmltext = "30412-05.htm"

                    EARTH_SNAKE -> if (cond == 5) {
                        if (st.hasQuestItems(SERPENT_CHARM)) {
                            if (st.getQuestItemsCount(EN_MONSTEREYE_SHELL) + st.getQuestItemsCount(EN_STONEGOLEM_POWDER) + st.getQuestItemsCount(
                                    EN_IRONGOLEM_SCRAP
                                ) < 30
                            )
                                htmltext = "30409-04.htm"
                            else {
                                htmltext = "30409-05.htm"
                                st.takeItems(EN_IRONGOLEM_SCRAP, 10)
                                st.takeItems(EN_MONSTEREYE_SHELL, 10)
                                st.takeItems(EN_STONEGOLEM_POWDER, 10)
                                st.takeItems(SERPENT_CHARM, 1)
                                st.giveItems(TONE_OF_EARTH, 1)

                                if (st.hasQuestItems(TONE_OF_WATER, TONE_OF_FIRE, TONE_OF_WIND)) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        } else if (!st.hasQuestItems(TONE_OF_EARTH))
                            htmltext = "30409-01.htm"
                        else
                            htmltext = "30409-06.htm"
                    } else if (cond == 6)
                        htmltext = "30409-06.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        if (cond == 3) {
            when (npc.npcId) {
                SINGING_FLOWER_PHANTASM -> if (!st.hasQuestItems(GOLDEN_SEED_1)) {
                    npc.broadcastNpcSay("I am a tree of nothing... a tree... that knows where to return...")
                    st.dropItemsAlways(GOLDEN_SEED_1, 1, 1)
                    if (st.hasQuestItems(GOLDEN_SEED_2, GOLDEN_SEED_3))
                        st["cond"] = "4"
                }

                SINGING_FLOWER_NIGHTMARE -> if (!st.hasQuestItems(GOLDEN_SEED_2)) {
                    npc.broadcastNpcSay("I am a creature that shows the truth of the place deep in my heart...")
                    st.dropItemsAlways(GOLDEN_SEED_2, 1, 1)
                    if (st.hasQuestItems(GOLDEN_SEED_1, GOLDEN_SEED_3))
                        st["cond"] = "4"
                }

                SINGING_FLOWER_DARKLING -> if (!st.hasQuestItems(GOLDEN_SEED_3)) {
                    npc.broadcastNpcSay("I am a mirror of darkness... a virtual image of darkness...")
                    st.dropItemsAlways(GOLDEN_SEED_3, 1, 1)
                    if (st.hasQuestItems(GOLDEN_SEED_1, GOLDEN_SEED_2))
                        st["cond"] = "4"
                }
            }
        } else if (cond == 5) {
            when (npc.npcId) {
                GHOST_FIRE -> if (st.hasQuestItems(SALAMANDER_CHARM))
                    st.dropItems(FLAME_CRYSTAL, 1, 5, 500000)

                TOAD_LORD, MARSH_STAKATO, MARSH_STAKATO_WORKER -> if (st.hasQuestItems(UNDINE_CHARM))
                    st.dropItems(DAZZLING_DROP, 1, 20, 300000)

                MARSH_STAKATO_SOLDIER -> if (st.hasQuestItems(UNDINE_CHARM))
                    st.dropItems(DAZZLING_DROP, 1, 20, 400000)

                MARSH_STAKATO_DRONE -> if (st.hasQuestItems(UNDINE_CHARM))
                    st.dropItems(DAZZLING_DROP, 1, 20, 500000)

                HARPY -> if (st.hasQuestItems(SYLPH_CHARM))
                    st.dropItemsAlways(HARPY_FEATHER, 1, 20)

                WYRM -> if (st.hasQuestItems(SYLPH_CHARM))
                    st.dropItems(WYRM_WINGBONE, 1, 10, 500000)

                WINDSUS -> if (st.hasQuestItems(SYLPH_CHARM))
                    st.dropItems(WINDSUS_MANE, 1, 10, 500000)

                ENCHANTED_MONSTEREYE -> if (st.hasQuestItems(SERPENT_CHARM))
                    st.dropItemsAlways(EN_MONSTEREYE_SHELL, 1, 10)

                ENCHANTED_STONE_GOLEM -> if (st.hasQuestItems(SERPENT_CHARM))
                    st.dropItemsAlways(EN_STONEGOLEM_POWDER, 1, 10)

                ENCHANTED_IRON_GOLEM -> if (st.hasQuestItems(SERPENT_CHARM))
                    st.dropItemsAlways(EN_IRONGOLEM_SCRAP, 1, 10)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q228_TestOfMagus"

        // Items
        private val RUKAL_LETTER = 2841
        private val PARINA_LETTER = 2842
        private val LILAC_CHARM = 2843
        private val GOLDEN_SEED_1 = 2844
        private val GOLDEN_SEED_2 = 2845
        private val GOLDEN_SEED_3 = 2846
        private val SCORE_OF_ELEMENTS = 2847
        private val DAZZLING_DROP = 2848
        private val FLAME_CRYSTAL = 2849
        private val HARPY_FEATHER = 2850
        private val WYRM_WINGBONE = 2851
        private val WINDSUS_MANE = 2852
        private val EN_MONSTEREYE_SHELL = 2853
        private val EN_STONEGOLEM_POWDER = 2854
        private val EN_IRONGOLEM_SCRAP = 2855
        private val TONE_OF_WATER = 2856
        private val TONE_OF_FIRE = 2857
        private val TONE_OF_WIND = 2858
        private val TONE_OF_EARTH = 2859
        private val SALAMANDER_CHARM = 2860
        private val SYLPH_CHARM = 2861
        private val UNDINE_CHARM = 2862
        private val SERPENT_CHARM = 2863

        // Rewards
        private val MARK_OF_MAGUS = 2840
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val PARINA = 30391
        private val EARTH_SNAKE = 30409
        private val FLAME_SALAMANDER = 30411
        private val WIND_SYLPH = 30412
        private val WATER_UNDINE = 30413
        private val CASIAN = 30612
        private val RUKAL = 30629

        // Monsters
        private val HARPY = 20145
        private val MARSH_STAKATO = 20157
        private val WYRM = 20176
        private val MARSH_STAKATO_WORKER = 20230
        private val TOAD_LORD = 20231
        private val MARSH_STAKATO_SOLDIER = 20232
        private val MARSH_STAKATO_DRONE = 20234
        private val WINDSUS = 20553
        private val ENCHANTED_MONSTEREYE = 20564
        private val ENCHANTED_STONE_GOLEM = 20565
        private val ENCHANTED_IRON_GOLEM = 20566
        private val SINGING_FLOWER_PHANTASM = 27095
        private val SINGING_FLOWER_NIGHTMARE = 27096
        private val SINGING_FLOWER_DARKLING = 27097
        private val GHOST_FIRE = 27098
    }
}