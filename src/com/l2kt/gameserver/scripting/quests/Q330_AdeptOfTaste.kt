package com.l2kt.gameserver.scripting.quests

import java.util.HashMap

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q330_AdeptOfTaste : Quest(330, "Adept of Taste") {
    init {
        CHANCES[20204] = intArrayOf(92, 100)
        CHANCES[20229] = intArrayOf(80, 95)
        CHANCES[20223] = intArrayOf(70, 77)
        CHANCES[20154] = intArrayOf(70, 77)
        CHANCES[20155] = intArrayOf(87, 96)
        CHANCES[20156] = intArrayOf(77, 85)
    }

    init {

        setItemsIds(
            INGREDIENT_LIST,
            RED_MANDRAGORA_SAP,
            WHITE_MANDRAGORA_SAP,
            HONEY,
            GOLDEN_HONEY,
            DIONIAN_POTATO,
            GREEN_MOSS_BUNDLE,
            BROWN_MOSS_BUNDLE,
            MONSTER_EYE_MEAT,
            MIRIEN_REVIEW_1,
            MIRIEN_REVIEW_2,
            MIRIEN_REVIEW_3,
            MIRIEN_REVIEW_4,
            MIRIEN_REVIEW_5,
            JONAS_STEAK_DISH_1,
            JONAS_STEAK_DISH_2,
            JONAS_STEAK_DISH_3,
            JONAS_STEAK_DISH_4,
            JONAS_STEAK_DISH_5,
            SONIA_BOTANY_BOOK,
            RED_MANDRAGORA_ROOT,
            WHITE_MANDRAGORA_ROOT,
            JACOB_INSECT_BOOK,
            NECTAR,
            ROYAL_JELLY,
            PANO_CONTRACT,
            HOBGOBLIN_AMULET,
            GLYVKA_BOTANY_BOOK,
            GREEN_MARSH_MOSS,
            BROWN_MARSH_MOSS,
            ROLANT_CREATURE_BOOK,
            MONSTER_EYE_BODY
        )

        addStartNpc(JONAS) // Jonas
        addTalkId(JONAS, SONIA, GLYVKA, ROLLANT, JACOB, PANO, MIRIEN)

        addKillId(20147, 20154, 20155, 20156, 20204, 20223, 20226, 20228, 20229, 20265, 20266)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30469-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(INGREDIENT_LIST, 1)
        } else if (event.equals("30062-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(SONIA_BOTANY_BOOK, 1)
            st.takeItems(RED_MANDRAGORA_ROOT, -1)
            st.takeItems(WHITE_MANDRAGORA_ROOT, -1)
            st.giveItems(RED_MANDRAGORA_SAP, 1)

        } else if (event.equals("30073-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(JACOB_INSECT_BOOK, 1)
            st.takeItems(NECTAR, -1)
            st.takeItems(ROYAL_JELLY, -1)
            st.giveItems(HONEY, 1)
        } else if (event.equals("30067-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(GLYVKA_BOTANY_BOOK, 1)
            st.takeItems(GREEN_MARSH_MOSS, -1)
            st.takeItems(BROWN_MARSH_MOSS, -1)
            st.giveItems(GREEN_MOSS_BUNDLE, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 24) "30469-01.htm" else "30469-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                JONAS -> if (st.hasQuestItems(INGREDIENT_LIST)) {
                    if (!hasAllIngredients(st))
                        htmltext = "30469-04.htm"
                    else {
                        val dish: Int

                        val specialIngredientsNumber =
                            st.getQuestItemsCount(WHITE_MANDRAGORA_SAP) + st.getQuestItemsCount(GOLDEN_HONEY) + st.getQuestItemsCount(
                                BROWN_MOSS_BUNDLE
                            )

                        if (Rnd.nextBoolean()) {
                            htmltext = "30469-05t" + Integer.toString(specialIngredientsNumber + 2) + ".htm"
                            dish = 1443 + specialIngredientsNumber
                        } else {
                            htmltext = "30469-05t" + Integer.toString(specialIngredientsNumber + 1) + ".htm"
                            dish = 1442 + specialIngredientsNumber
                        }

                        // Sound according dish.
                        st.playSound(if (dish == JONAS_STEAK_DISH_5) QuestState.SOUND_JACKPOT else QuestState.SOUND_ITEMGET)

                        st.takeItems(INGREDIENT_LIST, 1)
                        st.takeItems(RED_MANDRAGORA_SAP, 1)
                        st.takeItems(WHITE_MANDRAGORA_SAP, 1)
                        st.takeItems(HONEY, 1)
                        st.takeItems(GOLDEN_HONEY, 1)
                        st.takeItems(DIONIAN_POTATO, 1)
                        st.takeItems(GREEN_MOSS_BUNDLE, 1)
                        st.takeItems(BROWN_MOSS_BUNDLE, 1)
                        st.takeItems(MONSTER_EYE_MEAT, 1)
                        st.giveItems(dish, 1)
                    }
                } else if (st.hasAtLeastOneQuestItem(
                        JONAS_STEAK_DISH_1,
                        JONAS_STEAK_DISH_2,
                        JONAS_STEAK_DISH_3,
                        JONAS_STEAK_DISH_4,
                        JONAS_STEAK_DISH_5
                    )
                )
                    htmltext = "30469-06.htm"
                else if (st.hasAtLeastOneQuestItem(
                        MIRIEN_REVIEW_1,
                        MIRIEN_REVIEW_2,
                        MIRIEN_REVIEW_3,
                        MIRIEN_REVIEW_4,
                        MIRIEN_REVIEW_5
                    )
                ) {
                    if (st.hasQuestItems(MIRIEN_REVIEW_1)) {
                        htmltext = "30469-06t1.htm"
                        st.takeItems(MIRIEN_REVIEW_1, 1)
                        st.rewardItems(57, 7500)
                        st.rewardExpAndSp(6000, 0)
                    } else if (st.hasQuestItems(MIRIEN_REVIEW_2)) {
                        htmltext = "30469-06t2.htm"
                        st.takeItems(MIRIEN_REVIEW_2, 1)
                        st.rewardItems(57, 9000)
                        st.rewardExpAndSp(7000, 0)
                    } else if (st.hasQuestItems(MIRIEN_REVIEW_3)) {
                        htmltext = "30469-06t3.htm"
                        st.takeItems(MIRIEN_REVIEW_3, 1)
                        st.rewardItems(57, 5800)
                        st.giveItems(JONAS_SALAD_RECIPE, 1)
                        st.rewardExpAndSp(9000, 0)
                    } else if (st.hasQuestItems(MIRIEN_REVIEW_4)) {
                        htmltext = "30469-06t4.htm"
                        st.takeItems(MIRIEN_REVIEW_4, 1)
                        st.rewardItems(57, 6800)
                        st.giveItems(JONAS_SAUCE_RECIPE, 1)
                        st.rewardExpAndSp(10500, 0)
                    } else if (st.hasQuestItems(MIRIEN_REVIEW_5)) {
                        htmltext = "30469-06t5.htm"
                        st.takeItems(MIRIEN_REVIEW_5, 1)
                        st.rewardItems(57, 7800)
                        st.giveItems(JONAS_STEAK_RECIPE, 1)
                        st.rewardExpAndSp(12000, 0)
                    }
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                MIRIEN -> if (st.hasQuestItems(INGREDIENT_LIST))
                    htmltext = "30461-01.htm"
                else if (st.hasAtLeastOneQuestItem(
                        JONAS_STEAK_DISH_1,
                        JONAS_STEAK_DISH_2,
                        JONAS_STEAK_DISH_3,
                        JONAS_STEAK_DISH_4,
                        JONAS_STEAK_DISH_5
                    )
                ) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    if (st.hasQuestItems(JONAS_STEAK_DISH_1)) {
                        htmltext = "30461-02t1.htm"
                        st.takeItems(JONAS_STEAK_DISH_1, 1)
                        st.giveItems(MIRIEN_REVIEW_1, 1)
                    } else if (st.hasQuestItems(JONAS_STEAK_DISH_2)) {
                        htmltext = "30461-02t2.htm"
                        st.takeItems(JONAS_STEAK_DISH_2, 1)
                        st.giveItems(MIRIEN_REVIEW_2, 1)
                    } else if (st.hasQuestItems(JONAS_STEAK_DISH_3)) {
                        htmltext = "30461-02t3.htm"
                        st.takeItems(JONAS_STEAK_DISH_3, 1)
                        st.giveItems(MIRIEN_REVIEW_3, 1)
                    } else if (st.hasQuestItems(JONAS_STEAK_DISH_4)) {
                        htmltext = "30461-02t4.htm"
                        st.takeItems(JONAS_STEAK_DISH_4, 1)
                        st.giveItems(MIRIEN_REVIEW_4, 1)
                    } else if (st.hasQuestItems(JONAS_STEAK_DISH_5)) {
                        htmltext = "30461-02t5.htm"
                        st.takeItems(JONAS_STEAK_DISH_5, 1)
                        st.giveItems(MIRIEN_REVIEW_5, 1)
                    }
                } else if (st.hasAtLeastOneQuestItem(
                        MIRIEN_REVIEW_1,
                        MIRIEN_REVIEW_2,
                        MIRIEN_REVIEW_3,
                        MIRIEN_REVIEW_4,
                        MIRIEN_REVIEW_5
                    )
                )
                    htmltext = "30461-04.htm"

                SONIA -> if (!st.hasQuestItems(RED_MANDRAGORA_SAP) && !st.hasQuestItems(WHITE_MANDRAGORA_SAP)) {
                    if (!st.hasQuestItems(SONIA_BOTANY_BOOK)) {
                        htmltext = "30062-01.htm"
                        st.giveItems(SONIA_BOTANY_BOOK, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else {
                        if (st.getQuestItemsCount(RED_MANDRAGORA_ROOT) < 40 || st.getQuestItemsCount(
                                WHITE_MANDRAGORA_ROOT
                            ) < 40
                        )
                            htmltext = "30062-02.htm"
                        else if (st.getQuestItemsCount(WHITE_MANDRAGORA_ROOT) >= 40) {
                            htmltext = "30062-06.htm"
                            st.takeItems(SONIA_BOTANY_BOOK, 1)
                            st.takeItems(RED_MANDRAGORA_ROOT, -1)
                            st.takeItems(WHITE_MANDRAGORA_ROOT, -1)
                            st.giveItems(WHITE_MANDRAGORA_SAP, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30062-03.htm"
                    }
                } else
                    htmltext = "30062-07.htm"

                JACOB -> if (!st.hasQuestItems(HONEY) && !st.hasQuestItems(GOLDEN_HONEY)) {
                    if (!st.hasQuestItems(JACOB_INSECT_BOOK)) {
                        htmltext = "30073-01.htm"
                        st.giveItems(JACOB_INSECT_BOOK, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else {
                        if (st.getQuestItemsCount(NECTAR) < 20)
                            htmltext = "30073-02.htm"
                        else {
                            if (st.getQuestItemsCount(ROYAL_JELLY) < 10)
                                htmltext = "30073-03.htm"
                            else {
                                htmltext = "30073-06.htm"
                                st.takeItems(JACOB_INSECT_BOOK, 1)
                                st.takeItems(NECTAR, -1)
                                st.takeItems(ROYAL_JELLY, -1)
                                st.giveItems(GOLDEN_HONEY, 1)
                                st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        }
                    }
                } else
                    htmltext = "30073-07.htm"

                PANO -> if (!st.hasQuestItems(DIONIAN_POTATO)) {
                    if (!st.hasQuestItems(PANO_CONTRACT)) {
                        htmltext = "30078-01.htm"
                        st.giveItems(PANO_CONTRACT, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else {
                        if (st.getQuestItemsCount(HOBGOBLIN_AMULET) < 30)
                            htmltext = "30078-02.htm"
                        else {
                            htmltext = "30078-03.htm"
                            st.takeItems(PANO_CONTRACT, 1)
                            st.takeItems(HOBGOBLIN_AMULET, -1)
                            st.giveItems(DIONIAN_POTATO, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        }
                    }
                } else
                    htmltext = "30078-04.htm"

                GLYVKA -> if (!st.hasQuestItems(GREEN_MOSS_BUNDLE) && !st.hasQuestItems(BROWN_MOSS_BUNDLE)) {
                    if (!st.hasQuestItems(GLYVKA_BOTANY_BOOK)) {
                        st.giveItems(GLYVKA_BOTANY_BOOK, 1)
                        htmltext = "30067-01.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else {
                        if (st.getQuestItemsCount(GREEN_MARSH_MOSS) < 20 || st.getQuestItemsCount(BROWN_MARSH_MOSS) < 20)
                            htmltext = "30067-02.htm"
                        else if (st.getQuestItemsCount(BROWN_MARSH_MOSS) >= 20) {
                            htmltext = "30067-06.htm"
                            st.takeItems(GLYVKA_BOTANY_BOOK, 1)
                            st.takeItems(GREEN_MARSH_MOSS, -1)
                            st.takeItems(BROWN_MARSH_MOSS, -1)
                            st.giveItems(BROWN_MOSS_BUNDLE, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30067-03.htm"
                    }
                } else
                    htmltext = "30067-07.htm"

                ROLLANT -> if (!st.hasQuestItems(MONSTER_EYE_MEAT)) {
                    if (!st.hasQuestItems(ROLANT_CREATURE_BOOK)) {
                        htmltext = "30069-01.htm"
                        st.giveItems(ROLANT_CREATURE_BOOK, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    } else {
                        if (st.getQuestItemsCount(MONSTER_EYE_BODY) < 30)
                            htmltext = "30069-02.htm"
                        else {
                            htmltext = "30069-03.htm"
                            st.takeItems(ROLANT_CREATURE_BOOK, 1)
                            st.takeItems(MONSTER_EYE_BODY, -1)
                            st.giveItems(MONSTER_EYE_MEAT, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        }
                    }
                } else
                    htmltext = "30069-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        when (npcId) {
            20265 -> if (st.hasQuestItems(ROLANT_CREATURE_BOOK))
                st.dropItems(MONSTER_EYE_BODY, if (Rnd[97] < 77) 2 else 3, 30, 970000)

            20266 -> if (st.hasQuestItems(ROLANT_CREATURE_BOOK))
                st.dropItemsAlways(MONSTER_EYE_BODY, if (Rnd[10] < 7) 1 else 2, 30)

            20226 -> if (st.hasQuestItems(GLYVKA_BOTANY_BOOK))
                st.dropItems(if (Rnd[96] < 87) GREEN_MARSH_MOSS else BROWN_MARSH_MOSS, 1, 20, 960000)

            20228 -> if (st.hasQuestItems(GLYVKA_BOTANY_BOOK))
                st.dropItemsAlways(if (Rnd[10] < 9) GREEN_MARSH_MOSS else BROWN_MARSH_MOSS, 1, 20)

            20147 -> if (st.hasQuestItems(PANO_CONTRACT))
                st.dropItemsAlways(HOBGOBLIN_AMULET, 1, 30)

            20204, 20229 -> if (st.hasQuestItems(JACOB_INSECT_BOOK)) {
                val random = Rnd[100]
                val chances = CHANCES[npcId]!!
                if (random < chances[0])
                    st.dropItemsAlways(NECTAR, 1, 20)
                else if (random < chances[1])
                    st.dropItemsAlways(ROYAL_JELLY, 1, 10)
            }

            20223, 20154, 20155, 20156 -> if (st.hasQuestItems(SONIA_BOTANY_BOOK)) {
                val random = Rnd[100]
                val chances = CHANCES[npcId]!!
                if (random < chances[1])
                    st.dropItemsAlways(if (random < chances[0]) RED_MANDRAGORA_ROOT else WHITE_MANDRAGORA_ROOT, 1, 40)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q330_AdeptOfTaste"

        // NPCs
        private val SONIA = 30062
        private val GLYVKA = 30067
        private val ROLLANT = 30069
        private val JACOB = 30073
        private val PANO = 30078
        private val MIRIEN = 30461
        private val JONAS = 30469

        // Items
        private val INGREDIENT_LIST = 1420
        private val SONIA_BOTANY_BOOK = 1421
        private val RED_MANDRAGORA_ROOT = 1422
        private val WHITE_MANDRAGORA_ROOT = 1423
        private val RED_MANDRAGORA_SAP = 1424
        private val WHITE_MANDRAGORA_SAP = 1425
        private val JACOB_INSECT_BOOK = 1426
        private val NECTAR = 1427
        private val ROYAL_JELLY = 1428
        private val HONEY = 1429
        private val GOLDEN_HONEY = 1430
        private val PANO_CONTRACT = 1431
        private val HOBGOBLIN_AMULET = 1432
        private val DIONIAN_POTATO = 1433
        private val GLYVKA_BOTANY_BOOK = 1434
        private val GREEN_MARSH_MOSS = 1435
        private val BROWN_MARSH_MOSS = 1436
        private val GREEN_MOSS_BUNDLE = 1437
        private val BROWN_MOSS_BUNDLE = 1438
        private val ROLANT_CREATURE_BOOK = 1439
        private val MONSTER_EYE_BODY = 1440
        private val MONSTER_EYE_MEAT = 1441
        private val JONAS_STEAK_DISH_1 = 1442
        private val JONAS_STEAK_DISH_2 = 1443
        private val JONAS_STEAK_DISH_3 = 1444
        private val JONAS_STEAK_DISH_4 = 1445
        private val JONAS_STEAK_DISH_5 = 1446
        private val MIRIEN_REVIEW_1 = 1447
        private val MIRIEN_REVIEW_2 = 1448
        private val MIRIEN_REVIEW_3 = 1449
        private val MIRIEN_REVIEW_4 = 1450
        private val MIRIEN_REVIEW_5 = 1451

        // Rewards
        private val JONAS_SALAD_RECIPE = 1455
        private val JONAS_SAUCE_RECIPE = 1456
        private val JONAS_STEAK_RECIPE = 1457

        // Drop chances
        private val CHANCES = HashMap<Int, IntArray>()

        private fun hasAllIngredients(st: QuestState): Boolean {
            return st.hasQuestItems(DIONIAN_POTATO, MONSTER_EYE_MEAT) && st.hasAtLeastOneQuestItem(
                WHITE_MANDRAGORA_SAP,
                RED_MANDRAGORA_SAP
            ) && st.hasAtLeastOneQuestItem(GOLDEN_HONEY, HONEY) && st.hasAtLeastOneQuestItem(
                BROWN_MOSS_BUNDLE,
                GREEN_MOSS_BUNDLE
            )
        }
    }
}