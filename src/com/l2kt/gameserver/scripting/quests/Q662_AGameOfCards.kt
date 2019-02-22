package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q662_AGameOfCards : Quest(662, "A Game Of Cards") {
    init {
        CARDS[0] = "?"
        CARDS[1] = "!"
        CARDS[2] = "="
        CARDS[3] = "T"
        CARDS[4] = "V"
        CARDS[5] = "O"
        CARDS[6] = "P"
        CARDS[7] = "S"
        CARDS[8] = "E"
        CARDS[9] = "H"
        CARDS[10] = "A"
        CARDS[11] = "R"
        CARDS[12] = "D"
        CARDS[13] = "I"
        CARDS[14] = "N"
    }

    init {
        CHANCES[18001] = 232000 // Blood Queen
        CHANCES[20672] = 357000 // Trives
        CHANCES[20673] = 373000 // Falibati
        CHANCES[20674] = 583000 // Doom Knight
        CHANCES[20677] = 435000 // Tulben
        CHANCES[20955] = 358000 // Ghostly Warrior
        CHANCES[20958] = 283000 // Death Agent
        CHANCES[20959] = 455000 // Dark Guard
        CHANCES[20961] = 365000 // Bloody Knight
        CHANCES[20962] = 348000 // Bloody Priest
        CHANCES[20965] = 457000 // Chimera Piece
        CHANCES[20966] = 493000 // Changed Creation
        CHANCES[20968] = 418000 // Nonexistant Man
        CHANCES[20972] = 350000 // Shaman of Ancient Times
        CHANCES[20973] = 453000 // Forgotten Ancient People
        CHANCES[21002] = 315000 // Doom Scout
        CHANCES[21004] = 320000 // Dismal Pole
        CHANCES[21006] = 335000 // Doom Servant
        CHANCES[21008] = 462000 // Doom Archer
        CHANCES[21010] = 397000 // Doom Warrior
        CHANCES[21109] = 507000 // Hames Orc Scout
        CHANCES[21112] = 552000 // Hames Orc Footman
        CHANCES[21114] = 587000 // Cursed Guardian
        CHANCES[21116] = 812000 // Hames Orc Overlord
        CHANCES[21278] = 483000 // Antelope
        CHANCES[21279] = 483000 // Antelope
        CHANCES[21280] = 483000 // Antelope
        CHANCES[21281] = 483000 // Antelope
        CHANCES[21286] = 515000 // Buffalo
        CHANCES[21287] = 515000 // Buffalo
        CHANCES[21288] = 515000 // Buffalo
        CHANCES[21289] = 515000 // Buffalo
        CHANCES[21508] = 493000 // Splinter Stakato
        CHANCES[21510] = 527000 // Splinter Stakato Soldier
        CHANCES[21513] = 562000 // Needle Stakato
        CHANCES[21515] = 598000 // Needle Stakato Soldier
        CHANCES[21520] = 458000 // Eye of Splendor
        CHANCES[21526] = 552000 // Wisdom of Splendor
        CHANCES[21530] = 488000 // Victory of Splendor
        CHANCES[21535] = 573000 // Signet of Splendor
    }

    init {

        setItemsIds(RED_GEM)

        addStartNpc(KLUMP)
        addTalkId(KLUMP)

        for (monster in CHANCES.keys)
            addKillId(monster)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30845-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["state"] = "0"
            st["stateEx"] = "0"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30845-04.htm", ignoreCase = true)) {
            val state = st.getInt("state")
            val stateEx = st.getInt("stateEx")

            if (state == 0 && stateEx == 0 && st.getQuestItemsCount(RED_GEM) >= 50)
                htmltext = "30845-05.htm"
        } else if (event.equals("30845-07.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30845-11.htm", ignoreCase = true)) {
            val state = st.getInt("state")
            val stateEx = st.getInt("stateEx")

            if (state == 0 && stateEx == 0 && st.getQuestItemsCount(RED_GEM) >= 50) {
                var i1 = Rnd[70] + 1
                var i2 = Rnd[70] + 1
                var i3 = Rnd[70] + 1
                var i4 = Rnd[70] + 1
                var i5 = Rnd[70] + 1

                if (i1 >= 57)
                    i1 = i1 - 56
                else if (i1 >= 43)
                    i1 = i1 - 42
                else if (i1 >= 29)
                    i1 = i1 - 28
                else if (i1 >= 15)
                    i1 = i1 - 14

                if (i2 >= 57)
                    i2 = i2 - 56
                else if (i2 >= 43)
                    i2 = i2 - 42
                else if (i2 >= 29)
                    i2 = i2 - 28
                else if (i2 >= 15)
                    i2 = i2 - 14

                if (i3 >= 57)
                    i3 = i3 - 56
                else if (i3 >= 43)
                    i3 = i3 - 42
                else if (i3 >= 29)
                    i3 = i3 - 28
                else if (i3 >= 15)
                    i3 = i3 - 14

                if (i4 >= 57)
                    i4 = i4 - 56
                else if (i4 >= 43)
                    i4 = i4 - 42
                else if (i4 >= 29)
                    i4 = i4 - 28
                else if (i4 >= 15)
                    i4 = i4 - 14

                if (i5 >= 57)
                    i5 = i5 - 56
                else if (i5 >= 43)
                    i5 = i5 - 42
                else if (i5 >= 29)
                    i5 = i5 - 28
                else if (i5 >= 15)
                    i5 = i5 - 14

                st["state"] = (i4 * 1000000 + i3 * 10000 + i2 * 100 + i1).toString()
                st["stateEx"] = i5.toString()

                st.takeItems(RED_GEM, 50)
            }
        } else if (event == "First" || event == "Second" || event == "Third" || event == "Fourth" || event == "Fifth")
        // reply 11 12 13 14 15
        {
            val state = st.getInt("state")
            val stateEx = st.getInt("stateEx")

            val i0: Int
            var i1: Int
            val i2: Int
            val i3: Int
            val i4: Int
            val i5: Int
            var i6: Int
            var i8: Int
            var i9: Int

            i0 = state
            i1 = stateEx
            i5 = i1 % 100
            i9 = i1 / 100
            i1 = i0 % 100
            i2 = i0 % 10000 / 100
            i3 = i0 % 1000000 / 10000
            i4 = i0 % 100000000 / 1000000

            if (event == "First") {
                if (i9 % 2 < 1)
                    i9 = i9 + 1
            } else if (event == "Second") {
                if (i9 % 4 < 2)
                    i9 = i9 + 2
            } else if (event == "Third") {
                if (i9 % 8 < 4)
                    i9 = i9 + 4
            } else if (event == "Fourth") {
                if (i9 % 16 < 8)
                    i9 = i9 + 8
            } else if (event == "Fifth") {
                if (i9 % 32 < 16)
                    i9 = i9 + 16
            }

            if (i9 % 32 < 31) {
                st["stateEx"] = (i9 * 100 + i5).toString()
                htmltext = getHtmlText("30845-12.htm")
            } else if (i9 % 32 == 31) {
                i6 = 0
                i8 = 0

                if (i1 >= 1 && i1 <= 14 && i2 >= 1 && i2 <= 14 && i3 >= 1 && i3 <= 14 && i4 >= 1 && i4 <= 14 && i5 >= 1 && i5 <= 14) {
                    if (i1 == i2) {
                        i6 = i6 + 10
                        i8 = i8 + 8
                    }

                    if (i1 == i3) {
                        i6 = i6 + 10
                        i8 = i8 + 4
                    }

                    if (i1 == i4) {
                        i6 = i6 + 10
                        i8 = i8 + 2
                    }

                    if (i1 == i5) {
                        i6 = i6 + 10
                        i8 = i8 + 1
                    }

                    if (i6 % 100 < 10) {
                        if (i8 % 16 < 8) {
                            if (i8 % 8 < 4) {
                                if (i2 == i3) {
                                    i6 = i6 + 10
                                    i8 = i8 + 4
                                }
                            }

                            if (i8 % 4 < 2) {
                                if (i2 == i4) {
                                    i6 = i6 + 10
                                    i8 = i8 + 2
                                }
                            }

                            if (i8 % 2 < 1) {
                                if (i2 == i5) {
                                    i6 = i6 + 10
                                    i8 = i8 + 1
                                }
                            }
                        }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 16 < 8) {
                            if (i8 % 8 < 4) {
                                if (i2 == i3) {
                                    i6 = i6 + 1
                                    i8 = i8 + 4
                                }
                            }

                            if (i8 % 4 < 2) {
                                if (i2 == i4) {
                                    i6 = i6 + 1
                                    i8 = i8 + 2
                                }
                            }

                            if (i8 % 2 < 1) {
                                if (i2 == i5) {
                                    i6 = i6 + 1
                                    i8 = i8 + 1
                                }
                            }
                        }
                    }

                    if (i6 % 100 < 10) {
                        if (i8 % 8 < 4) {
                            if (i8 % 4 < 2) {
                                if (i3 == i4) {
                                    i6 = i6 + 10
                                    i8 = i8 + 2
                                }
                            }

                            if (i8 % 2 < 1) {
                                if (i3 == i5) {
                                    i6 = i6 + 10
                                    i8 = i8 + 1
                                }
                            }
                        }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 8 < 4) {
                            if (i8 % 4 < 2) {
                                if (i3 == i4) {
                                    i6 = i6 + 1
                                    i8 = i8 + 2
                                }
                            }

                            if (i8 % 2 < 1) {
                                if (i3 == i5) {
                                    i6 = i6 + 1
                                    i8 = i8 + 1
                                }
                            }
                        }
                    }

                    if (i6 % 100 < 10) {
                        if (i8 % 4 < 2) {
                            if (i8 % 2 < 1) {
                                if (i4 == i5) {
                                    i6 = i6 + 10
                                    i8 = i8 + 1
                                }
                            }
                        }
                    } else if (i6 % 10 == 0) {
                        if (i8 % 4 < 2) {
                            if (i8 % 2 < 1) {
                                if (i4 == i5) {
                                    i6 = i6 + 1
                                    i8 = i8 + 1
                                }
                            }
                        }
                    }
                }

                if (i6 == 40) {
                    giveReward(st, ZIGGO_GEMSTONE, 43)
                    giveReward(st, EW_S, 3)
                    giveReward(st, EW_A, 1)
                    htmltext = getHtmlText("30845-13.htm")
                } else if (i6 == 30) {
                    giveReward(st, EW_S, 2)
                    giveReward(st, EW_C, 2)
                    htmltext = getHtmlText("30845-14.htm")
                } else if (i6 == 21 || i6 == 12) {
                    giveReward(st, EW_A, 1)
                    giveReward(st, EW_B, 2)
                    giveReward(st, EW_D, 1)
                    htmltext = getHtmlText("30845-15.htm")
                } else if (i6 == 20) {
                    giveReward(st, EW_C, 2)
                    htmltext = getHtmlText("30845-16.htm")
                } else if (i6 == 11) {
                    giveReward(st, EW_C, 1)
                    htmltext = getHtmlText("30845-17.htm")
                } else if (i6 == 10) {
                    giveReward(st, EA_D, 2)
                    htmltext = getHtmlText("30845-18.htm")
                } else if (i6 == 0)
                    htmltext = getHtmlText("30845-19.htm")

                st["state"] = "0"
                st["stateEx"] = "0"
            }

            htmltext = htmltext.replace("%FontColor1%", if (i9 % 2 < 1) "ffff00" else "ff6f6f")
                .replace("%Cell1%", if (i9 % 2 < 1) CARDS[0].orEmpty() else CARDS[i1].orEmpty())
            htmltext = htmltext.replace("%FontColor2%", if (i9 % 4 < 2) "ffff00" else "ff6f6f")
                .replace("%Cell2%", if (i9 % 4 < 2) CARDS[0].orEmpty() else CARDS[i2].orEmpty())
            htmltext = htmltext.replace("%FontColor3%", if (i9 % 8 < 4) "ffff00" else "ff6f6f")
                .replace("%Cell3%", if (i9 % 8 < 4) CARDS[0].orEmpty() else CARDS[i3].orEmpty())
            htmltext = htmltext.replace("%FontColor4%", if (i9 % 16 < 8) "ffff00" else "ff6f6f")
                .replace("%Cell4%", if (i9 % 16 < 8) CARDS[0].orEmpty() else CARDS[i4].orEmpty())
            htmltext = htmltext.replace("%FontColor5%", if (i9 % 32 < 16) "ffff00" else "ff6f6f")
                .replace("%Cell5%", if (i9 % 32 < 16) CARDS[0].orEmpty() else CARDS[i5].orEmpty())
        } else if (event.equals("30845-20.htm", ignoreCase = true))
        // reply 20
        {
            if (st.getQuestItemsCount(RED_GEM) < 50)
                htmltext = "30845-21.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 61) "30845-02.htm" else "30845-01.htm"

            Quest.STATE_STARTED -> {
                val state = st.getInt("state")
                val stateEx = st.getInt("stateEx")

                if (state == 0 && stateEx == 0)
                    htmltext = if (st.getQuestItemsCount(RED_GEM) < 50) "30845-04.htm" else "30845-05.htm"
                else if (state != 0 && stateEx != 0) {
                    val i0: Int
                    var i1: Int
                    val i2: Int
                    val i3: Int
                    val i4: Int
                    val i5: Int
                    val i9: Int

                    i0 = state
                    i1 = stateEx
                    i5 = i1 % 100
                    i9 = i1 / 100
                    i1 = i0 % 100
                    i2 = i0 % 10000 / 100
                    i3 = i0 % 1000000 / 10000
                    i4 = i0 % 100000000 / 1000000

                    htmltext = getHtmlText("30845-11a.htm")
                    htmltext = htmltext.replace("%FontColor1%", if (i9 % 2 < 1) "ffff00" else "ff6f6f")
                        .replace("%Cell1%", if (i9 % 2 < 1) CARDS[0].orEmpty() else CARDS[i1].orEmpty())
                    htmltext = htmltext.replace("%FontColor2%", if (i9 % 4 < 2) "ffff00" else "ff6f6f")
                        .replace("%Cell2%", if (i9 % 4 < 2) CARDS[0].orEmpty() else CARDS[i2].orEmpty())
                    htmltext = htmltext.replace("%FontColor3%", if (i9 % 8 < 4) "ffff00" else "ff6f6f")
                        .replace("%Cell3%", if (i9 % 8 < 4) CARDS[0].orEmpty() else CARDS[i3].orEmpty())
                    htmltext = htmltext.replace("%FontColor4%", if (i9 % 16 < 8) "ffff00" else "ff6f6f")
                        .replace("%Cell4%", if (i9 % 16 < 8) CARDS[0].orEmpty() else CARDS[i4].orEmpty())
                    htmltext = htmltext.replace("%FontColor5%", if (i9 % 32 < 16) "ffff00" else "ff6f6f")
                        .replace("%Cell5%", if (i9 % 32 < 16) CARDS[0].orEmpty() else CARDS[i5].orEmpty())
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = CHANCES[npc.npcId] ?: 0

        st.dropItems(RED_GEM, 1, 0, chance)
        return null
    }

    companion object {
        private const val qn = "Q662_AGameOfCards"

        // NPC
        private const val KLUMP = 30845

        // Quest Item
        private const val RED_GEM = 8765

        // Reward Items
        private const val EW_S = 959
        private const val EW_A = 729
        private const val EW_B = 947
        private const val EW_C = 951
        private const val EW_D = 955
        private const val EA_D = 956
        private const val ZIGGO_GEMSTONE = 8868

        // All cards
        private val CARDS = HashMap<Int, String>()

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()

        private fun giveReward(st: QuestState?, item: Int, count: Int) {
            val template = ItemTable.getTemplate(item)

            if (template!!.isStackable)
                st!!.giveItems(item, count)
            else {
                for (i in 0 until count)
                    st!!.giveItems(item, 1)
            }
        }
    }
}