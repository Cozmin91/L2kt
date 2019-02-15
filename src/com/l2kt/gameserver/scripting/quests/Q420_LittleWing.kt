package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q420_LittleWing : Quest(420, "Little Wing") {
    init {

        setItemsIds(
            FAIRY_STONE,
            DELUXE_FAIRY_STONE,
            FAIRY_STONE_LIST,
            DELUXE_FAIRY_STONE_LIST,
            TOAD_LORD_BACK_SKIN,
            JUICE_OF_MONKSHOOD,
            SCALE_OF_DRAKE_EXARION,
            EGG_OF_DRAKE_EXARION,
            SCALE_OF_DRAKE_ZWOV,
            EGG_OF_DRAKE_ZWOV,
            SCALE_OF_DRAKE_KALIBRAN,
            EGG_OF_DRAKE_KALIBRAN,
            SCALE_OF_WYVERN_SUZET,
            EGG_OF_WYVERN_SUZET,
            SCALE_OF_WYVERN_SHAMHAI,
            EGG_OF_WYVERN_SHAMHAI
        )

        addStartNpc(COOPER, MIMYU)
        addTalkId(MARIA, CRONOS, BYRON, MIMYU, EXARION, ZWOV, KALIBRAN, SUZET, SHAMHAI, COOPER)

        addKillId(
            20202,
            20231,
            20233,
            20270,
            20551,
            20580,
            20589,
            20590,
            20591,
            20592,
            20593,
            20594,
            20595,
            20596,
            20597,
            20598,
            20599
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // COOPER
        if (event.equals("30829-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30610-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(FAIRY_STONE_LIST, 1)
        } else if (event.equals("30610-06.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(DELUXE_FAIRY_STONE_LIST, 1)
        } else if (event.equals("30610-12.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st["deluxestone"] = "1"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(FAIRY_STONE_LIST, 1)
        } else if (event.equals("30610-13.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st["deluxestone"] = "1"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(DELUXE_FAIRY_STONE_LIST, 1)
        } else if (event.equals("30608-03.htm", ignoreCase = true)) {
            if (!checkItems(st, false))
                htmltext = "30608-01.htm" // Avoid to continue while trade or drop mats before clicking bypass
            else {
                st.takeItems(COAL, 10)
                st.takeItems(CHARCOAL, 10)
                st.takeItems(GEMSTONE_D, 1)
                st.takeItems(SILVER_NUGGET, 3)
                st.takeItems(TOAD_LORD_BACK_SKIN, -1)
                st.takeItems(FAIRY_STONE_LIST, 1)
                st.giveItems(FAIRY_STONE, 1)
            }
        } else if (event.equals("30608-05.htm", ignoreCase = true)) {
            if (!checkItems(st, true))
                htmltext = "30608-01.htm" // Avoid to continue while trade or drop mats before clicking bypass
            else {
                st.takeItems(COAL, 10)
                st.takeItems(CHARCOAL, 10)
                st.takeItems(GEMSTONE_C, 1)
                st.takeItems(STONE_OF_PURITY, 1)
                st.takeItems(SILVER_NUGGET, 5)
                st.takeItems(TOAD_LORD_BACK_SKIN, -1)
                st.takeItems(DELUXE_FAIRY_STONE_LIST, 1)
                st.giveItems(DELUXE_FAIRY_STONE, 1)
            }
        } else if (event.equals("30711-03.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            if (st.hasQuestItems(DELUXE_FAIRY_STONE))
                htmltext = "30711-04.htm"
        } else if (event.equals("30747-02.htm", ignoreCase = true)) {
            st["mimyu"] = "1"
            st.takeItems(FAIRY_STONE, 1)
        } else if (event.equals("30747-04.htm", ignoreCase = true)) {
            st["mimyu"] = "1"
            st.takeItems(DELUXE_FAIRY_STONE, 1)
            st.giveItems(FAIRY_DUST, 1)
        } else if (event.equals("30747-07.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(JUICE_OF_MONKSHOOD, 1)
        } else if (event.equals("30747-12.htm", ignoreCase = true) && !st.hasQuestItems(FAIRY_DUST)) {
            htmltext = "30747-15.htm"
            giveRandomPet(st, false)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30747-13.htm", ignoreCase = true)) {
            giveRandomPet(st, st.hasQuestItems(FAIRY_DUST))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30747-14.htm", ignoreCase = true)) {
            if (st.hasQuestItems(FAIRY_DUST)) {
                st.takeItems(FAIRY_DUST, 1)
                giveRandomPet(st, true)
                if (Rnd[20] == 1)
                    st.giveItems(HATCHLING_SOFT_LEATHER, 1)
                else {
                    htmltext = "30747-14t.htm"
                    st.giveItems(FOOD_FOR_HATCHLING, 20)
                }
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "30747-13.htm"
        } else if (event.equals("30748-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JUICE_OF_MONKSHOOD, 1)
            st.giveItems(SCALE_OF_DRAKE_EXARION, 1)
        } else if (event.equals("30749-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JUICE_OF_MONKSHOOD, 1)
            st.giveItems(SCALE_OF_DRAKE_ZWOV, 1)
        } else if (event.equals("30750-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JUICE_OF_MONKSHOOD, 1)
            st.giveItems(SCALE_OF_DRAKE_KALIBRAN, 1)
        } else if (event.equals("30750-05.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(EGG_OF_DRAKE_KALIBRAN, 19)
            st.takeItems(SCALE_OF_DRAKE_KALIBRAN, 1)
        } else if (event.equals("30751-03.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JUICE_OF_MONKSHOOD, 1)
            st.giveItems(SCALE_OF_WYVERN_SUZET, 1)
        } else if (event.equals("30752-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(JUICE_OF_MONKSHOOD, 1)
            st.giveItems(SCALE_OF_WYVERN_SHAMHAI, 1)
        }// SHAMHAI
        // SUZET
        // KALIBRAN
        // ZWOV
        // EXARION
        // MIMYU
        // BYRON
        // MARIA
        // CRONOS

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> when (npc.npcId) {
                COOPER -> htmltext = if (player.level >= 35) "30829-01.htm" else "30829-03.htm"

                MIMYU -> {
                    _counter += 1
                    npc.teleToLocation(LOCATIONS[_counter % 3], 0)
                    return null
                }
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    COOPER -> htmltext = "30829-04.htm"

                    CRONOS -> if (cond == 1)
                        htmltext = "30610-01.htm"
                    else if (st.getInt("deluxestone") == 2)
                        htmltext = "30610-10.htm"
                    else if (cond == 2) {
                        if (st.hasAtLeastOneQuestItem(FAIRY_STONE, DELUXE_FAIRY_STONE)) {
                            if (st.getInt("deluxestone") == 1)
                                htmltext = "30610-14.htm"
                            else {
                                htmltext = "30610-08.htm"
                                st["cond"] = "3"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            }
                        } else
                            htmltext = "30610-07.htm"
                    } else if (cond == 3)
                        htmltext = "30610-09.htm"
                    else if (cond == 4 && st.hasAtLeastOneQuestItem(FAIRY_STONE, DELUXE_FAIRY_STONE))
                        htmltext = "30610-11.htm"

                    MARIA -> if (st.hasAtLeastOneQuestItem(FAIRY_STONE, DELUXE_FAIRY_STONE))
                        htmltext = "30608-06.htm"
                    else if (cond == 2) {
                        if (st.hasQuestItems(FAIRY_STONE_LIST))
                            htmltext = if (checkItems(st, false)) "30608-02.htm" else "30608-01.htm"
                        else if (st.hasQuestItems(DELUXE_FAIRY_STONE_LIST))
                            htmltext = if (checkItems(st, true)) "30608-04.htm" else "30608-01.htm"
                    }

                    BYRON -> {
                        val deluxestone = st.getInt("deluxestone")
                        if (deluxestone == 1) {
                            if (st.hasQuestItems(FAIRY_STONE)) {
                                htmltext = "30711-05.htm"
                                st["cond"] = "4"
                                st.unset("deluxestone")
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else if (st.hasQuestItems(DELUXE_FAIRY_STONE)) {
                                htmltext = "30711-06.htm"
                                st["cond"] = "4"
                                st.unset("deluxestone")
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                htmltext = "30711-10.htm"
                        } else if (deluxestone == 2)
                            htmltext = "30711-09.htm"
                        else if (cond == 3)
                            htmltext = "30711-01.htm"
                        else if (cond == 4) {
                            if (st.hasQuestItems(FAIRY_STONE))
                                htmltext = "30711-07.htm"
                            else if (st.hasQuestItems(DELUXE_FAIRY_STONE))
                                htmltext = "30711-08.htm"
                        }
                    }

                    MIMYU -> if (cond == 4) {
                        if (st.getInt("mimyu") == 1)
                            htmltext = "30747-06.htm"
                        else if (st.hasQuestItems(FAIRY_STONE))
                            htmltext = "30747-01.htm"
                        else if (st.hasQuestItems(DELUXE_FAIRY_STONE))
                            htmltext = "30747-03.htm"
                    } else if (cond == 5)
                        htmltext = "30747-08.htm"
                    else if (cond == 6) {
                        val eggs =
                            st.getQuestItemsCount(EGG_OF_DRAKE_EXARION) + st.getQuestItemsCount(EGG_OF_DRAKE_ZWOV) + st.getQuestItemsCount(
                                EGG_OF_DRAKE_KALIBRAN
                            ) + st.getQuestItemsCount(EGG_OF_WYVERN_SUZET) + st.getQuestItemsCount(EGG_OF_WYVERN_SHAMHAI)
                        if (eggs < 20)
                            htmltext = "30747-09.htm"
                        else
                            htmltext = "30747-10.htm"
                    } else if (cond == 7)
                        htmltext = "30747-11.htm"
                    else {
                        _counter += 1
                        npc.teleToLocation(LOCATIONS[_counter % 3], 0)
                        return null
                    }

                    EXARION -> if (cond == 5)
                        htmltext = "30748-01.htm"
                    else if (cond == 6) {
                        if (st.getQuestItemsCount(EGG_OF_DRAKE_EXARION) < 20)
                            htmltext = "30748-03.htm"
                        else {
                            htmltext = "30748-04.htm"
                            st["cond"] = "7"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(EGG_OF_DRAKE_EXARION, 19)
                            st.takeItems(SCALE_OF_DRAKE_EXARION, 1)
                        }
                    } else if (cond == 7)
                        htmltext = "30748-05.htm"

                    ZWOV -> if (cond == 5)
                        htmltext = "30749-01.htm"
                    else if (cond == 6) {
                        if (st.getQuestItemsCount(EGG_OF_DRAKE_ZWOV) < 20)
                            htmltext = "30749-03.htm"
                        else {
                            htmltext = "30749-04.htm"
                            st["cond"] = "7"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(EGG_OF_DRAKE_ZWOV, 19)
                            st.takeItems(SCALE_OF_DRAKE_ZWOV, 1)
                        }
                    } else if (cond == 7)
                        htmltext = "30749-05.htm"

                    KALIBRAN -> if (cond == 5)
                        htmltext = "30750-01.htm"
                    else if (cond == 6)
                        htmltext =
                                if (st.getQuestItemsCount(EGG_OF_DRAKE_KALIBRAN) < 20) "30750-03.htm" else "30750-04.htm"
                    else if (cond == 7)
                        htmltext = "30750-06.htm"

                    SUZET -> if (cond == 5)
                        htmltext = "30751-01.htm"
                    else if (cond == 6) {
                        if (st.getQuestItemsCount(EGG_OF_WYVERN_SUZET) < 20)
                            htmltext = "30751-04.htm"
                        else {
                            htmltext = "30751-05.htm"
                            st["cond"] = "7"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(EGG_OF_WYVERN_SUZET, 19)
                            st.takeItems(SCALE_OF_WYVERN_SUZET, 1)
                        }
                    } else if (cond == 7)
                        htmltext = "30751-06.htm"

                    SHAMHAI -> if (cond == 5)
                        htmltext = "30752-01.htm"
                    else if (cond == 6) {
                        if (st.getQuestItemsCount(EGG_OF_WYVERN_SHAMHAI) < 20)
                            htmltext = "30752-03.htm"
                        else {
                            htmltext = "30752-04.htm"
                            st["cond"] = "7"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(EGG_OF_WYVERN_SHAMHAI, 19)
                            st.takeItems(SCALE_OF_WYVERN_SHAMHAI, 1)
                        }
                    } else if (cond == 7)
                        htmltext = "30752-05.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20231 -> if (st.hasQuestItems(FAIRY_STONE_LIST))
                st.dropItems(TOAD_LORD_BACK_SKIN, 1, 10, 300000)
            else if (st.hasQuestItems(DELUXE_FAIRY_STONE_LIST))
                st.dropItems(TOAD_LORD_BACK_SKIN, 1, 20, 300000)

            20580 -> if (st.hasQuestItems(SCALE_OF_DRAKE_EXARION) && !st.dropItems(EGG_OF_DRAKE_EXARION, 1, 20, 500000))
                npc.broadcastNpcSay("If the eggs get taken, we're dead!")

            20233 -> if (st.hasQuestItems(SCALE_OF_DRAKE_ZWOV))
                st.dropItems(EGG_OF_DRAKE_ZWOV, 1, 20, 500000)

            20551 -> if (st.hasQuestItems(SCALE_OF_DRAKE_KALIBRAN) && !st.dropItems(
                    EGG_OF_DRAKE_KALIBRAN,
                    1,
                    20,
                    500000
                )
            )
                npc.broadcastNpcSay("Hey! Everybody watch the eggs!")

            20270 -> if (st.hasQuestItems(SCALE_OF_WYVERN_SUZET) && !st.dropItems(EGG_OF_WYVERN_SUZET, 1, 20, 500000))
                npc.broadcastNpcSay("I thought I'd caught one share... Whew!")

            20202 -> if (st.hasQuestItems(SCALE_OF_WYVERN_SHAMHAI))
                st.dropItems(EGG_OF_WYVERN_SHAMHAI, 1, 20, 500000)

            20589, 20590, 20591, 20592, 20593, 20594, 20595, 20596, 20597, 20598, 20599 -> if (st.hasQuestItems(
                    DELUXE_FAIRY_STONE
                ) && Rnd[100] < 30
            ) {
                st["deluxestone"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(DELUXE_FAIRY_STONE, 1)
                npc.broadcastNpcSay("The stone... the Elven stone... broke...")
            }
        }
        return null
    }

    companion object {
        private val qn = "Q420_LittleWing"

        // Needed items
        private val COAL = 1870
        private val CHARCOAL = 1871
        private val SILVER_NUGGET = 1873
        private val STONE_OF_PURITY = 1875
        private val GEMSTONE_D = 2130
        private val GEMSTONE_C = 2131

        // Items
        private val FAIRY_DUST = 3499

        private val FAIRY_STONE = 3816
        private val DELUXE_FAIRY_STONE = 3817
        private val FAIRY_STONE_LIST = 3818
        private val DELUXE_FAIRY_STONE_LIST = 3819
        private val TOAD_LORD_BACK_SKIN = 3820
        private val JUICE_OF_MONKSHOOD = 3821
        private val SCALE_OF_DRAKE_EXARION = 3822
        private val EGG_OF_DRAKE_EXARION = 3823
        private val SCALE_OF_DRAKE_ZWOV = 3824
        private val EGG_OF_DRAKE_ZWOV = 3825
        private val SCALE_OF_DRAKE_KALIBRAN = 3826
        private val EGG_OF_DRAKE_KALIBRAN = 3827
        private val SCALE_OF_WYVERN_SUZET = 3828
        private val EGG_OF_WYVERN_SUZET = 3829
        private val SCALE_OF_WYVERN_SHAMHAI = 3830
        private val EGG_OF_WYVERN_SHAMHAI = 3831

        // Rewards
        private val DRAGONFLUTE_OF_WIND = 3500
        private val DRAGONFLUTE_OF_STAR = 3501
        private val DRAGONFLUTE_OF_TWILIGHT = 3502
        private val HATCHLING_SOFT_LEATHER = 3912
        private val FOOD_FOR_HATCHLING = 4038

        // NPCs
        private val MARIA = 30608
        private val CRONOS = 30610
        private val BYRON = 30711
        private val MIMYU = 30747
        private val EXARION = 30748
        private val ZWOV = 30749
        private val KALIBRAN = 30750
        private val SUZET = 30751
        private val SHAMHAI = 30752
        private val COOPER = 30829

        // Spawn Points
        private val LOCATIONS = arrayOf(
            SpawnLocation(109816, 40854, -4640, 0),
            SpawnLocation(108940, 41615, -4643, 0),
            SpawnLocation(110395, 41625, -4642, 0)
        )

        private var _counter = 0

        private fun checkItems(st: QuestState, isDeluxe: Boolean): Boolean {
            // Conditions required for both cases.
            if (st.getQuestItemsCount(COAL) < 10 || st.getQuestItemsCount(CHARCOAL) < 10)
                return false

            if (isDeluxe) {
                if (st.getQuestItemsCount(GEMSTONE_C) >= 1 && st.getQuestItemsCount(SILVER_NUGGET) >= 5 && st.getQuestItemsCount(
                        STONE_OF_PURITY
                    ) >= 1 && st.getQuestItemsCount(TOAD_LORD_BACK_SKIN) >= 20
                )
                    return true
            } else {
                if (st.getQuestItemsCount(GEMSTONE_D) >= 1 && st.getQuestItemsCount(SILVER_NUGGET) >= 3 && st.getQuestItemsCount(
                        TOAD_LORD_BACK_SKIN
                    ) >= 10
                )
                    return true
            }
            return false
        }

        private fun giveRandomPet(st: QuestState, hasFairyDust: Boolean) {
            var pet = DRAGONFLUTE_OF_TWILIGHT
            val chance = Rnd[100]
            if (st.hasQuestItems(EGG_OF_DRAKE_EXARION)) {
                st.takeItems(EGG_OF_DRAKE_EXARION, 1)
                if (hasFairyDust) {
                    if (chance < 45)
                        pet = DRAGONFLUTE_OF_WIND
                    else if (chance < 75)
                        pet = DRAGONFLUTE_OF_STAR
                } else if (chance < 50)
                    pet = DRAGONFLUTE_OF_WIND
                else if (chance < 85)
                    pet = DRAGONFLUTE_OF_STAR
            } else if (st.hasQuestItems(EGG_OF_WYVERN_SUZET)) {
                st.takeItems(EGG_OF_WYVERN_SUZET, 1)
                if (hasFairyDust) {
                    if (chance < 55)
                        pet = DRAGONFLUTE_OF_WIND
                    else if (chance < 85)
                        pet = DRAGONFLUTE_OF_STAR
                } else if (chance < 65)
                    pet = DRAGONFLUTE_OF_WIND
                else if (chance < 95)
                    pet = DRAGONFLUTE_OF_STAR
            } else if (st.hasQuestItems(EGG_OF_DRAKE_KALIBRAN)) {
                st.takeItems(EGG_OF_DRAKE_KALIBRAN, 1)
                if (hasFairyDust) {
                    if (chance < 60)
                        pet = DRAGONFLUTE_OF_WIND
                    else if (chance < 90)
                        pet = DRAGONFLUTE_OF_STAR
                } else if (chance < 70)
                    pet = DRAGONFLUTE_OF_WIND
                else
                    pet = DRAGONFLUTE_OF_STAR
            } else if (st.hasQuestItems(EGG_OF_WYVERN_SHAMHAI)) {
                st.takeItems(EGG_OF_WYVERN_SHAMHAI, 1)
                if (hasFairyDust) {
                    if (chance < 70)
                        pet = DRAGONFLUTE_OF_WIND
                    else
                        pet = DRAGONFLUTE_OF_STAR
                } else if (chance < 85)
                    pet = DRAGONFLUTE_OF_WIND
                else
                    pet = DRAGONFLUTE_OF_STAR
            } else if (st.hasQuestItems(EGG_OF_DRAKE_ZWOV)) {
                st.takeItems(EGG_OF_DRAKE_ZWOV, 1)
                if (hasFairyDust) {
                    if (chance < 90)
                        pet = DRAGONFLUTE_OF_WIND
                    else
                        pet = DRAGONFLUTE_OF_STAR
                } else
                    pet = DRAGONFLUTE_OF_WIND
            }

            st.giveItems(pet, 1)
        }
    }
}