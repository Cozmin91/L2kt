package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q663_SeductiveWhispers : Quest(663, "Seductive Whispers") {
    init {
        CARDS[0] = "No such card"
        CARDS[11] = "<font color=\"ff453d\"> Sun Card: 1 </font>"
        CARDS[12] = "<font color=\"ff453d\"> Sun Card: 2 </font>"
        CARDS[13] = "<font color=\"ff453d\"> Sun Card: 3 </font>"
        CARDS[14] = "<font color=\"ff453d\"> Sun Card: 4 </font>"
        CARDS[15] = "<font color=\"ff453d\"> Sun Card: 5 </font>"
        CARDS[21] = "<font color=\"fff802\"> Moon Card: 1 </font>"
        CARDS[22] = "<font color=\"fff802\"> Moon Card: 2 </font>"
        CARDS[23] = "<font color=\"fff802\"> Moon Card: 3 </font>"
        CARDS[24] = "<font color=\"fff802\"> Moon Card: 4 </font>"
        CARDS[25] = "<font color=\"fff802\"> Moon Card: 5 </font>"
    }

    init {
        CHANCES[20674] = 807000 // Doom Knight
        CHANCES[20678] = 372000 // Tortured Undead
        CHANCES[20954] = 460000 // Hungered Corpse
        CHANCES[20955] = 537000 // Ghost War
        CHANCES[20956] = 540000 // Past Knight
        CHANCES[20957] = 565000 // Nihil Invader
        CHANCES[20958] = 425000 // Death Agent
        CHANCES[20959] = 682000 // Dark Guard
        CHANCES[20960] = 372000 // Bloody Ghost
        CHANCES[20961] = 547000 // Bloody Knight
        CHANCES[20962] = 522000 // Bloody Priest
        CHANCES[20963] = 498000 // Bloody Lord
        CHANCES[20974] = 1000000 // Spiteful Soul Leader
        CHANCES[20975] = 975000 // Spiteful Soul Wizard
        CHANCES[20976] = 825000 // Spiteful Soul Fighter
        CHANCES[20996] = 385000 // Spiteful Ghost of Ruins
        CHANCES[20997] = 342000 // Soldier of Grief
        CHANCES[20998] = 377000 // Cruel Punisher
        CHANCES[20999] = 450000 // Roving Soul
        CHANCES[21000] = 395000 // Soul of Ruins
        CHANCES[21001] = 535000 // Wretched Archer
        CHANCES[21002] = 472000 // Doom Scout
        CHANCES[21006] = 502000 // Doom Servant
        CHANCES[21007] = 540000 // Doom Guard
        CHANCES[21008] = 692000 // Doom Archer
        CHANCES[21009] = 740000 // Doom Trooper
        CHANCES[21010] = 595000 // Doom Warrior
    }

    init {

        setItemsIds(SPIRIT_BEAD)

        addStartNpc(WILBERT)
        addTalkId(WILBERT)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        val state = st.getInt("state")

        if (event.equals("30846-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["state"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30846-09.htm", ignoreCase = true) && state % 10 <= 4) {
            if (state / 10 < 1) {
                if (st.getQuestItemsCount(SPIRIT_BEAD) >= 50) {
                    st.takeItems(SPIRIT_BEAD, 50)
                    st["state"] = "5"
                } else
                    htmltext = "30846-10.htm"
            } else {
                st["state"] = (state / 10 * 10 + 5).toString()
                st["stateEx"] = "0"
                htmltext = "30846-09a.htm"
            }
        } else if (event.equals("30846-14.htm", ignoreCase = true) && state % 10 == 5 && state / 1000 == 0) {
            val i0 = st.getInt("stateEx")

            val i1 = i0 % 10
            val i2 = (i0 - i1) / 10

            val param1 = Rnd[2] + 1
            val param2 = Rnd[5] + 1

            val i5 = state / 10

            val param3 = param1 * 10 + param2

            if (param1 == i2) {
                val i3 = param2 + i1

                if (i3 % 5 == 0 && i3 != 10) {
                    if (state % 100 / 10 >= 7) {
                        st["state"] = "4"
                        st.rewardItems(ADENA, 2384000)
                        st.rewardItems(ENCHANT_WEAPON_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        htmltext = getHTML("30846-14.htm", i0, param3, player.name)
                    } else {
                        st["state"] = (state / 10 * 10 + 7).toString()
                        htmltext = getHTML("30846-13.htm", i0, param3, player.name).replace(
                            "%wincount%",
                            (i5 + 1).toString()
                        )
                    }
                } else {
                    st["state"] = (state / 10 * 10 + 6).toString()
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-12.htm", i0, param3, player.name)
                }
            } else {
                if (param2 == 5 || i1 == 5) {
                    if (state % 100 / 10 >= 7) {
                        st["state"] = "4"
                        st.rewardItems(ADENA, 2384000)
                        st.rewardItems(ENCHANT_WEAPON_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        htmltext = getHTML("30846-14.htm", i0, param3, player.name)
                    } else {
                        st["state"] = (state / 10 * 10 + 7).toString()
                        htmltext = getHTML("30846-13.htm", i0, param3, player.name).replace(
                            "%wincount%",
                            (i5 + 1).toString()
                        )
                    }
                } else {
                    st["state"] = (state / 10 * 10 + 6).toString()
                    st["stateEx"] = (param1 * 10 + param2).toString()
                    htmltext = getHTML("30846-12.htm", i0, param3, player.name)
                }
            }
        } else if (event.equals("30846-19.htm", ignoreCase = true) && state % 10 == 6 && state / 1000 == 0) {
            val i0 = st.getInt("stateEx")

            val i1 = i0 % 10
            val i2 = (i0 - i1) / 10

            val param1 = Rnd[2] + 1
            val param2 = Rnd[5] + 1
            val param3 = param1 * 10 + param2

            if (param1 == i2) {
                val i3 = param1 + i1

                if (i3 % 5 == 0 && i3 != 10) {
                    st["state"] = "1"
                    st["stateEx"] = "0"
                    htmltext = getHTML("30846-19.htm", i0, param3, player.name)
                } else {
                    st["state"] = (state / 10 * 10 + 5).toString()
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-18.htm", i0, param3, player.name)
                }
            } else {
                if (param2 == 5 || i1 == 5) {
                    st["state"] = "1"
                    htmltext = getHTML("30846-19.htm", i0, param3, player.name)
                } else {
                    st["state"] = (state / 10 * 10 + 5).toString()
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-18.htm", i0, param3, player.name)
                }
            }
        } else if (event.equals("30846-20.htm", ignoreCase = true) && state % 10 == 7 && state / 1000 == 0) {
            st["state"] = ((state / 10 + 1) * 10 + 4).toString()
            st["stateEx"] = "0"
        } else if (event.equals("30846-21.htm", ignoreCase = true) && state % 10 == 7 && state / 1000 == 0) {
            val round = state / 10

            if (round == 0)
                st.rewardItems(ADENA, 40000)
            else if (round == 1)
                st.rewardItems(ADENA, 80000)
            else if (round == 2) {
                st.rewardItems(ADENA, 110000)
                st.rewardItems(ENCHANT_WEAPON_D, 1)
            } else if (round == 3) {
                st.rewardItems(ADENA, 199000)
                st.rewardItems(ENCHANT_WEAPON_C, 1)
            } else if (round == 4) {
                st.rewardItems(ADENA, 388000)
                st.rewardItems(RECIPES[Rnd[RECIPES.size]], 1)
            } else if (round == 5) {
                st.rewardItems(ADENA, 675000)
                st.rewardItems(BLADES[Rnd[BLADES.size]], 1)
            } else if (round == 6) {
                st.rewardItems(ADENA, 1284000)
                st.rewardItems(ENCHANT_WEAPON_B, 1)
                st.rewardItems(ENCHANT_ARMOR_B, 1)
                st.rewardItems(ENCHANT_WEAPON_B, 1)
                st.rewardItems(ENCHANT_ARMOR_B, 1)
            }

            st["state"] = "1"
            st["stateEx"] = "0"
        } else if (event.equals("30846-22.htm", ignoreCase = true) && state % 10 == 1) {
            if (st.hasQuestItems(SPIRIT_BEAD)) {
                st["state"] = "1005"
                st.takeItems(SPIRIT_BEAD, 1)
            } else
                htmltext = "30846-22a.htm"
        } else if (event.equals("30846-25.htm", ignoreCase = true) && state == 1005) {
            val i0 = st.getInt("stateEx")

            val i1 = i0 % 10
            val i2 = (i0 - i1) / 10

            val param1 = Rnd[2] + 1
            val param2 = Rnd[5] + 1
            val param3 = param1 * 10 + param2

            if (param1 == i2) {
                val i3 = param2 + i1

                if (i3 % 5 == 0 && i3 != 10) {
                    st["state"] = "1"
                    st["stateEx"] = "0"
                    st.rewardItems(ADENA, 800)
                    htmltext = getHTML("30846-25.htm", i0, param3, player.name).replace("%card1%", i1.toString())
                } else {
                    st["state"] = "1006"
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-24.htm", i0, param3, player.name)
                }
            } else {
                if (param2 == 5 || i2 == 5) {
                    st["state"] = "1"
                    st["stateEx"] = "0"
                    st.rewardItems(ADENA, 800)
                    htmltext = getHTML("30846-25.htm", i0, param3, player.name).replace("%card1%", i1.toString())
                } else {
                    st["state"] = "1006"
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-24.htm", i0, param3, player.name)
                }
            }
        } else if (event.equals("30846-29.htm", ignoreCase = true) && state == 1006) {
            val i0 = st.getInt("stateEx")

            val i1 = i0 % 10
            val i2 = (i0 - i1) / 10

            val param1 = Rnd[2] + 1
            val param2 = Rnd[5] + 1
            val param3 = param1 * 10 + param2

            if (param1 == i2) {
                val i3 = param2 + i1

                if (i3 % 5 == 0 && i3 != 10) {
                    st["state"] = "1"
                    st["stateEx"] = "0"
                    st.rewardItems(ADENA, 800)
                    htmltext = getHTML("30846-29.htm", i0, param3, player.name).replace("%card1%", i1.toString())
                } else {
                    st["state"] = "1005"
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-28.htm", i0, param3, player.name)
                }
            } else {
                if (param2 == 5 || i1 == 5) {
                    st["state"] = "1"
                    st["stateEx"] = "0"
                    htmltext = getHTML("30846-29.htm", i0, param3, player.name)
                } else {
                    st["state"] = "1005"
                    st["stateEx"] = param3.toString()
                    htmltext = getHTML("30846-28.htm", i0, param3, player.name)
                }
            }
        } else if (event.equals("30846-30.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 50) "30846-02.htm" else "30846-01.htm"

            Quest.STATE_STARTED -> {
                val state = st.getInt("state")

                if (state < 4) {
                    if (st.hasQuestItems(SPIRIT_BEAD))
                        htmltext = "30846-05.htm"
                    else
                        htmltext = "30846-04.htm"
                } else if (state % 10 == 4)
                    htmltext = "30846-05a.htm"
                else if (state % 10 == 5)
                    htmltext = "30846-11.htm"
                else if (state % 10 == 6)
                    htmltext = "30846-15.htm"
                else if (state % 10 == 7) {
                    val round = state % 100 / 10

                    if (round >= 7) {
                        st.rewardItems(ADENA, 2384000)
                        st.rewardItems(ENCHANT_WEAPON_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        st.rewardItems(ENCHANT_ARMOR_A, 1)
                        htmltext = "30846-17.htm"
                    } else
                        htmltext = getHtmlText("30846-16.htm").replace("%wincount%", (state / 10 + 1).toString())
                } else if (state == 1005)
                    htmltext = "30846-23.htm"
                else if (state == 1006)
                    htmltext = "30846-26.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = CHANCES[npc.npcId] ?: 0
        st.dropItems(SPIRIT_BEAD, 1, 0, chance)

        return null
    }

    private fun getHTML(html: String, index: Int, param3: Int, name: String): String {
        return getHtmlText(html).replace("%card1pic%", CARDS[index] ?: "").replace("%card2pic%", CARDS[param3] ?: "").replace("%name%", name)
    }

    companion object {
        private const val qn = "Q663_SeductiveWhispers"

        // NPC
        private const val WILBERT = 30846

        // Quest item
        private const val SPIRIT_BEAD = 8766

        // Rewards
        private const val ADENA = 57
        private const val ENCHANT_WEAPON_A = 729
        private const val ENCHANT_ARMOR_A = 730
        private const val ENCHANT_WEAPON_B = 947
        private const val ENCHANT_ARMOR_B = 948
        private const val ENCHANT_WEAPON_C = 951
        private const val ENCHANT_WEAPON_D = 955

        private val RECIPES = intArrayOf(2353, 4963, 4967, 5000, 5001, 5002, 5004, 5005, 5006, 5007)

        private val BLADES = intArrayOf(2115, 4104, 4108, 4114, 4115, 4116, 4118, 4119, 4120, 4121)

        // Text of cards
        private val CARDS = HashMap<Int, String>()

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}