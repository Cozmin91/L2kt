package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q333_HuntOfTheBlackLion : Quest(333, "Hunt Of The Black Lion") {
    init {

        setItemsIds(
            LION_CLAW,
            LION_EYE,
            GUILD_COIN,
            UNDEAD_ASH,
            BLOODY_AXE_INSIGNIA,
            DELU_FANG,
            STAKATO_TALON,
            SOPHYA_LETTER_1,
            SOPHYA_LETTER_2,
            SOPHYA_LETTER_3,
            SOPHYA_LETTER_4
        )

        addStartNpc(SOPHYA)
        addTalkId(SOPHYA, REDFOOT, RUPIO, UNDRIAS, LOCKIRIN, MORGAN)

        for (i in DROPLIST)
            addKillId(i[1])
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30735-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30735-10.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(SOPHYA_LETTER_1)) {
                st.giveItems(SOPHYA_LETTER_1, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("30735-11.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(SOPHYA_LETTER_2)) {
                st.giveItems(SOPHYA_LETTER_2, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("30735-12.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(SOPHYA_LETTER_3)) {
                st.giveItems(SOPHYA_LETTER_3, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("30735-13.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(SOPHYA_LETTER_4)) {
                st.giveItems(SOPHYA_LETTER_4, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("30735-16.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(LION_CLAW) > 9) {
                st.takeItems(LION_CLAW, 10)

                val eyes = st.getQuestItemsCount(LION_EYE)
                if (eyes < 5) {
                    htmltext = "30735-17a.htm"

                    st.giveItems(LION_EYE, 1)

                    val random = Rnd[100]
                    if (random < 25)
                        st.giveItems(HEALING_POTION, 20)
                    else if (random < 50)
                        st.giveItems(
                            if (player.isMageClass) SPIRITSHOT_D else SOULSHOT_D,
                            if (player.isMageClass) 50 else 100
                        )
                    else if (random < 75)
                        st.giveItems(SCROLL_OF_ESCAPE, 20)
                    else
                        st.giveItems(SWIFT_ATTACK_POTION, 3)
                } else if (eyes < 9) {
                    htmltext = "30735-18b.htm"

                    st.giveItems(LION_EYE, 1)

                    val random = Rnd[100]
                    if (random < 25)
                        st.giveItems(HEALING_POTION, 25)
                    else if (random < 50)
                        st.giveItems(
                            if (player.isMageClass) SPIRITSHOT_D else SOULSHOT_D,
                            if (player.isMageClass) 100 else 200
                        )
                    else if (random < 75)
                        st.giveItems(SCROLL_OF_ESCAPE, 20)
                    else
                        st.giveItems(SWIFT_ATTACK_POTION, 3)
                } else {
                    htmltext = "30735-19b.htm"

                    val random = Rnd[100]
                    if (random < 25)
                        st.giveItems(HEALING_POTION, 50)
                    else if (random < 50)
                        st.giveItems(
                            if (player.isMageClass) SPIRITSHOT_D else SOULSHOT_D,
                            if (player.isMageClass) 200 else 400
                        )
                    else if (random < 75)
                        st.giveItems(SCROLL_OF_ESCAPE, 30)
                    else
                        st.giveItems(SWIFT_ATTACK_POTION, 4)
                }
            }
        } else if (event.equals("30735-20.htm", ignoreCase = true)) {
            st.takeItems(SOPHYA_LETTER_1, -1)
            st.takeItems(SOPHYA_LETTER_2, -1)
            st.takeItems(SOPHYA_LETTER_3, -1)
            st.takeItems(SOPHYA_LETTER_4, -1)
        } else if (event.equals("30735-26.htm", ignoreCase = true)) {
            st.takeItems(LION_CLAW, -1)
            st.takeItems(LION_EYE, -1)
            st.takeItems(GUILD_COIN, -1)
            st.takeItems(BLACK_LION_MARK, -1)
            st.takeItems(SOPHYA_LETTER_1, -1)
            st.takeItems(SOPHYA_LETTER_2, -1)
            st.takeItems(SOPHYA_LETTER_3, -1)
            st.takeItems(SOPHYA_LETTER_4, -1)
            st.giveItems(ADENA, 12400)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30736-03.htm", ignoreCase = true)) {
            val cargo1 = st.hasQuestItems(CARGO_BOX_1)
            val cargo2 = st.hasQuestItems(CARGO_BOX_2)
            val cargo3 = st.hasQuestItems(CARGO_BOX_3)
            val cargo4 = st.hasQuestItems(CARGO_BOX_4)

            if ((cargo1 || cargo2 || cargo3 || cargo4) && player.adena > 649) {
                st.takeItems(ADENA, 650)

                if (cargo1)
                    st.takeItems(CARGO_BOX_1, 1)
                else if (cargo2)
                    st.takeItems(CARGO_BOX_2, 1)
                else if (cargo3)
                    st.takeItems(CARGO_BOX_3, 1)
                else
                    st.takeItems(CARGO_BOX_4, 1)

                val i0 = Rnd[100]
                val i1 = Rnd[100]
                if (i0 < 40) {
                    if (i1 < 33) {
                        htmltext = "30736-04a.htm"
                        st.giveItems(GLUDIO_APPLE, 1)
                    } else if (i1 < 66) {
                        htmltext = "30736-04b.htm"
                        st.giveItems(CORN_MEAL, 1)
                    } else {
                        htmltext = "30736-04c.htm"
                        st.giveItems(WOLF_PELTS, 1)
                    }
                } else if (i0 < 60) {
                    if (i1 < 33) {
                        htmltext = "30736-04d.htm"
                        st.giveItems(MOONSTONE, 1)
                    } else if (i1 < 66) {
                        htmltext = "30736-04e.htm"
                        st.giveItems(GLUDIO_WHEAT_FLOWER, 1)
                    } else {
                        htmltext = "30736-04f.htm"
                        st.giveItems(SPIDERSILK_ROPE, 1)
                    }
                } else if (i0 < 70) {
                    if (i1 < 33) {
                        htmltext = "30736-04g.htm"
                        st.giveItems(ALEXANDRITE, 1)
                    } else if (i1 < 66) {
                        htmltext = "30736-04h.htm"
                        st.giveItems(SILVER_TEA, 1)
                    } else {
                        htmltext = "30736-04i.htm"
                        st.giveItems(GOLEM_PART, 1)
                    }
                } else if (i0 < 75) {
                    if (i1 < 33) {
                        htmltext = "30736-04j.htm"
                        st.giveItems(FIRE_EMERALD, 1)
                    } else if (i1 < 66) {
                        htmltext = "30736-04k.htm"
                        st.giveItems(SILK_FROCK, 1)
                    } else {
                        htmltext = "30736-04l.htm"
                        st.giveItems(PORCELAN_URN, 1)
                    }
                } else if (i0 < 76) {
                    htmltext = "30736-04m.htm"
                    st.giveItems(IMPERIAL_DIAMOND, 1)
                } else if (Rnd.nextBoolean()) {
                    htmltext = "30736-04n.htm"

                    if (i1 < 25)
                        st.giveItems(STATUE_SHILIEN_HEAD, 1)
                    else if (i1 < 50)
                        st.giveItems(STATUE_SHILIEN_TORSO, 1)
                    else if (i1 < 75)
                        st.giveItems(STATUE_SHILIEN_ARM, 1)
                    else
                        st.giveItems(STATUE_SHILIEN_LEG, 1)
                } else {
                    htmltext = "30736-04o.htm"

                    if (i1 < 25)
                        st.giveItems(TABLET_FRAGMENT_1, 1)
                    else if (i1 < 50)
                        st.giveItems(TABLET_FRAGMENT_2, 1)
                    else if (i1 < 75)
                        st.giveItems(TABLET_FRAGMENT_3, 1)
                    else
                        st.giveItems(TABLET_FRAGMENT_4, 1)
                }
            } else
                htmltext = "30736-05.htm"
        } else if (event.equals("30736-07.htm", ignoreCase = true)) {
            val state = st.getInt("state")
            if (player.adena > 200 + state * 200) {
                if (state < 3) {
                    val i0 = Rnd[100]
                    if (i0 < 5)
                        htmltext = "30736-08a.htm"
                    else if (i0 < 10)
                        htmltext = "30736-08b.htm"
                    else if (i0 < 15)
                        htmltext = "30736-08c.htm"
                    else if (i0 < 20)
                        htmltext = "30736-08d.htm"
                    else if (i0 < 25)
                        htmltext = "30736-08e.htm"
                    else if (i0 < 30)
                        htmltext = "30736-08f.htm"
                    else if (i0 < 35)
                        htmltext = "30736-08g.htm"
                    else if (i0 < 40)
                        htmltext = "30736-08h.htm"
                    else if (i0 < 45)
                        htmltext = "30736-08i.htm"
                    else if (i0 < 50)
                        htmltext = "30736-08j.htm"
                    else if (i0 < 55)
                        htmltext = "30736-08k.htm"
                    else if (i0 < 60)
                        htmltext = "30736-08l.htm"
                    else if (i0 < 65)
                        htmltext = "30736-08m.htm"
                    else if (i0 < 70)
                        htmltext = "30736-08n.htm"
                    else if (i0 < 75)
                        htmltext = "30736-08o.htm"
                    else if (i0 < 80)
                        htmltext = "30736-08p.htm"
                    else if (i0 < 85)
                        htmltext = "30736-08q.htm"
                    else if (i0 < 90)
                        htmltext = "30736-08r.htm"
                    else if (i0 < 95)
                        htmltext = "30736-08s.htm"
                    else
                        htmltext = "30736-08t.htm"

                    st.takeItems(ADENA, 200 + state * 200)
                    st["state"] = (state + 1).toString()
                } else
                    htmltext = "30736-08.htm"
            }
        } else if (event.equals("30471-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(STATUE_SHILIEN_HEAD, STATUE_SHILIEN_TORSO, STATUE_SHILIEN_ARM, STATUE_SHILIEN_LEG)) {
                st.takeItems(STATUE_SHILIEN_HEAD, 1)
                st.takeItems(STATUE_SHILIEN_TORSO, 1)
                st.takeItems(STATUE_SHILIEN_ARM, 1)
                st.takeItems(STATUE_SHILIEN_LEG, 1)

                if (Rnd.nextBoolean()) {
                    htmltext = "30471-04.htm"
                    st.giveItems(COMPLETE_STATUE, 1)
                } else
                    htmltext = "30471-05.htm"
            }
        } else if (event.equals("30471-06.htm", ignoreCase = true)) {
            if (st.hasQuestItems(TABLET_FRAGMENT_1, TABLET_FRAGMENT_2, TABLET_FRAGMENT_3, TABLET_FRAGMENT_4)) {
                st.takeItems(TABLET_FRAGMENT_1, 1)
                st.takeItems(TABLET_FRAGMENT_2, 1)
                st.takeItems(TABLET_FRAGMENT_3, 1)
                st.takeItems(TABLET_FRAGMENT_4, 1)

                if (Rnd.nextBoolean()) {
                    htmltext = "30471-07.htm"
                    st.giveItems(COMPLETE_TABLET, 1)
                } else
                    htmltext = "30471-08.htm"
            }
        } else if (event.equals("30130-04.htm", ignoreCase = true) && st.hasQuestItems(COMPLETE_STATUE)) {
            st.takeItems(COMPLETE_STATUE, 1)
            st.giveItems(ADENA, 30000)
        } else if (event.equals("30531-04.htm", ignoreCase = true) && st.hasQuestItems(COMPLETE_TABLET)) {
            st.takeItems(COMPLETE_TABLET, 1)
            st.giveItems(ADENA, 30000)
        } else if (event.equals("30737-06.htm", ignoreCase = true)) {
            val cargo1 = st.hasQuestItems(CARGO_BOX_1)
            val cargo2 = st.hasQuestItems(CARGO_BOX_2)
            val cargo3 = st.hasQuestItems(CARGO_BOX_3)
            val cargo4 = st.hasQuestItems(CARGO_BOX_4)

            if (cargo1 || cargo2 || cargo3 || cargo4) {
                if (cargo1)
                    st.takeItems(CARGO_BOX_1, 1)
                else if (cargo2)
                    st.takeItems(CARGO_BOX_2, 1)
                else if (cargo3)
                    st.takeItems(CARGO_BOX_3, 1)
                else
                    st.takeItems(CARGO_BOX_4, 1)

                val coins = st.getQuestItemsCount(GUILD_COIN)
                if (coins < 40) {
                    htmltext = "30737-03.htm"
                    st.giveItems(ADENA, 100)
                } else if (coins < 80) {
                    htmltext = "30737-04.htm"
                    st.giveItems(ADENA, 200)
                } else {
                    htmltext = "30737-05.htm"
                    st.giveItems(ADENA, 300)
                }

                if (coins < 80)
                    st.giveItems(GUILD_COIN, 1)
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
            Quest.STATE_CREATED -> if (player.level < 25)
                htmltext = "30735-01.htm"
            else if (!st.hasQuestItems(BLACK_LION_MARK))
                htmltext = "30735-02.htm"
            else
                htmltext = "30735-03.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SOPHYA -> if (!st.hasAtLeastOneQuestItem(
                        SOPHYA_LETTER_1,
                        SOPHYA_LETTER_2,
                        SOPHYA_LETTER_3,
                        SOPHYA_LETTER_4
                    )
                )
                    htmltext = "30735-14.htm"
                else {
                    if (!st.hasAtLeastOneQuestItem(UNDEAD_ASH, BLOODY_AXE_INSIGNIA, DELU_FANG, STAKATO_TALON))
                        htmltext = if (st.hasAtLeastOneQuestItem(
                                CARGO_BOX_1,
                                CARGO_BOX_2,
                                CARGO_BOX_3,
                                CARGO_BOX_4
                            )
                        ) "30735-15a.htm" else "30735-15.htm"
                    else {
                        val count =
                            st.getQuestItemsCount(UNDEAD_ASH) + st.getQuestItemsCount(BLOODY_AXE_INSIGNIA) + st.getQuestItemsCount(
                                DELU_FANG
                            ) + st.getQuestItemsCount(STAKATO_TALON)

                        st.takeItems(UNDEAD_ASH, -1)
                        st.takeItems(BLOODY_AXE_INSIGNIA, -1)
                        st.takeItems(DELU_FANG, -1)
                        st.takeItems(STAKATO_TALON, -1)
                        st.giveItems(ADENA, count * 35)

                        if (count >= 20 && count < 50)
                            st.giveItems(LION_CLAW, 1)
                        else if (count >= 50 && count < 100)
                            st.giveItems(LION_CLAW, 2)
                        else if (count >= 100)
                            st.giveItems(LION_CLAW, 3)

                        htmltext = if (st.hasAtLeastOneQuestItem(
                                CARGO_BOX_1,
                                CARGO_BOX_2,
                                CARGO_BOX_3,
                                CARGO_BOX_4
                            )
                        ) "30735-23.htm" else "30735-22.htm"
                    }
                }

                REDFOOT -> htmltext = if (st.hasAtLeastOneQuestItem(
                        CARGO_BOX_1,
                        CARGO_BOX_2,
                        CARGO_BOX_3,
                        CARGO_BOX_4
                    )
                ) "30736-02.htm" else "30736-01.htm"

                RUPIO -> if (st.hasQuestItems(
                        STATUE_SHILIEN_HEAD,
                        STATUE_SHILIEN_TORSO,
                        STATUE_SHILIEN_ARM,
                        STATUE_SHILIEN_LEG
                    ) || st.hasQuestItems(TABLET_FRAGMENT_1, TABLET_FRAGMENT_2, TABLET_FRAGMENT_3, TABLET_FRAGMENT_4)
                )
                    htmltext = "30471-02.htm"
                else
                    htmltext = "30471-01.htm"

                UNDRIAS -> if (!st.hasQuestItems(COMPLETE_STATUE))
                    htmltext = if (st.hasQuestItems(
                            STATUE_SHILIEN_HEAD,
                            STATUE_SHILIEN_TORSO,
                            STATUE_SHILIEN_ARM,
                            STATUE_SHILIEN_LEG
                        )
                    ) "30130-02.htm" else "30130-01.htm"
                else
                    htmltext = "30130-03.htm"

                LOCKIRIN -> if (!st.hasQuestItems(COMPLETE_TABLET))
                    htmltext = if (st.hasQuestItems(
                            TABLET_FRAGMENT_1,
                            TABLET_FRAGMENT_2,
                            TABLET_FRAGMENT_3,
                            TABLET_FRAGMENT_4
                        )
                    ) "30531-02.htm" else "30531-01.htm"
                else
                    htmltext = "30531-03.htm"

                MORGAN -> htmltext = if (st.hasAtLeastOneQuestItem(
                        CARGO_BOX_1,
                        CARGO_BOX_2,
                        CARGO_BOX_3,
                        CARGO_BOX_4
                    )
                ) "30737-02.htm" else "30737-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        for (info in DROPLIST) {
            if (st.hasQuestItems(info[0]) && npc.npcId == info[1]) {
                st.dropItems(info[2], 1, 0, info[3])
                st.dropItems(info[4], 1, 0, info[5])
                break
            }
        }

        return null
    }

    companion object {
        private val qn = "Q333_HuntOfTheBlackLion"

        // NPCs
        private val SOPHYA = 30735
        private val REDFOOT = 30736
        private val RUPIO = 30471
        private val UNDRIAS = 30130
        private val LOCKIRIN = 30531
        private val MORGAN = 30737

        // Needs for start
        private val BLACK_LION_MARK = 1369

        // Quest items
        private val LION_CLAW = 3675
        private val LION_EYE = 3676
        private val GUILD_COIN = 3677
        private val UNDEAD_ASH = 3848
        private val BLOODY_AXE_INSIGNIA = 3849
        private val DELU_FANG = 3850
        private val STAKATO_TALON = 3851
        private val SOPHYA_LETTER_1 = 3671
        private val SOPHYA_LETTER_2 = 3672
        private val SOPHYA_LETTER_3 = 3673
        private val SOPHYA_LETTER_4 = 3674

        private val CARGO_BOX_1 = 3440
        private val CARGO_BOX_2 = 3441
        private val CARGO_BOX_3 = 3442
        private val CARGO_BOX_4 = 3443
        private val GLUDIO_APPLE = 3444
        private val CORN_MEAL = 3445
        private val WOLF_PELTS = 3446
        private val MOONSTONE = 3447
        private val GLUDIO_WHEAT_FLOWER = 3448
        private val SPIDERSILK_ROPE = 3449
        private val ALEXANDRITE = 3450
        private val SILVER_TEA = 3451
        private val GOLEM_PART = 3452
        private val FIRE_EMERALD = 3453
        private val SILK_FROCK = 3454
        private val PORCELAN_URN = 3455
        private val IMPERIAL_DIAMOND = 3456
        private val STATUE_SHILIEN_HEAD = 3457
        private val STATUE_SHILIEN_TORSO = 3458
        private val STATUE_SHILIEN_ARM = 3459
        private val STATUE_SHILIEN_LEG = 3460
        private val COMPLETE_STATUE = 3461
        private val TABLET_FRAGMENT_1 = 3462
        private val TABLET_FRAGMENT_2 = 3463
        private val TABLET_FRAGMENT_3 = 3464
        private val TABLET_FRAGMENT_4 = 3465
        private val COMPLETE_TABLET = 3466

        // Neutral items
        private val ADENA = 57
        private val SWIFT_ATTACK_POTION = 735
        private val SCROLL_OF_ESCAPE = 736
        private val HEALING_POTION = 1061
        private val SOULSHOT_D = 1463
        private val SPIRITSHOT_D = 2510

        // Tabs: Part, NpcId, ItemId, Item Chance, Box Id, Box Chance
        private val DROPLIST = arrayOf(
            // Part #1 - Execution Ground
            intArrayOf(SOPHYA_LETTER_1, 20160, UNDEAD_ASH, 500000, CARGO_BOX_1, 90000), // Neer Crawler
            intArrayOf(SOPHYA_LETTER_1, 20171, UNDEAD_ASH, 500000, CARGO_BOX_1, 60000), // Specter
            intArrayOf(SOPHYA_LETTER_1, 20197, UNDEAD_ASH, 500000, CARGO_BOX_1, 70000), // Sorrow Maiden
            intArrayOf(SOPHYA_LETTER_1, 20198, UNDEAD_ASH, 500000, CARGO_BOX_1, 80000), // Neer Ghoul Berserker
            intArrayOf(SOPHYA_LETTER_1, 20200, UNDEAD_ASH, 500000, CARGO_BOX_1, 100000), // Strain
            intArrayOf(SOPHYA_LETTER_1, 20201, UNDEAD_ASH, 500000, CARGO_BOX_1, 110000), // Ghoul

            // Part #2 - Partisan Hideaway
            intArrayOf(SOPHYA_LETTER_2, 20207, BLOODY_AXE_INSIGNIA, 500000, CARGO_BOX_2, 60000), // Ol Mahum Guerilla
            intArrayOf(SOPHYA_LETTER_2, 20208, BLOODY_AXE_INSIGNIA, 500000, CARGO_BOX_2, 70000), // Ol Mahum Raider
            intArrayOf(SOPHYA_LETTER_2, 20209, BLOODY_AXE_INSIGNIA, 500000, CARGO_BOX_2, 80000), // Ol Mahum Marksman
            intArrayOf(SOPHYA_LETTER_2, 20210, BLOODY_AXE_INSIGNIA, 500000, CARGO_BOX_2, 90000), // Ol Mahum Sergeant
            intArrayOf(SOPHYA_LETTER_2, 20211, BLOODY_AXE_INSIGNIA, 500000, CARGO_BOX_2, 100000), // Ol Mahum Captain

            // Part #3 - Near Giran Town
            intArrayOf(SOPHYA_LETTER_3, 20251, DELU_FANG, 500000, CARGO_BOX_3, 100000), // Delu Lizardman
            intArrayOf(SOPHYA_LETTER_3, 20252, DELU_FANG, 500000, CARGO_BOX_3, 110000), // Delu Lizardman Scout
            intArrayOf(SOPHYA_LETTER_3, 20253, DELU_FANG, 500000, CARGO_BOX_3, 120000), // Delu Lizardman Warrior

            // Part #4 - Cruma Area
            intArrayOf(SOPHYA_LETTER_4, 20157, STAKATO_TALON, 500000, CARGO_BOX_4, 100000), // Marsh Stakato
            intArrayOf(SOPHYA_LETTER_4, 20230, STAKATO_TALON, 500000, CARGO_BOX_4, 110000), // Marsh Stakato Worker
            intArrayOf(SOPHYA_LETTER_4, 20232, STAKATO_TALON, 500000, CARGO_BOX_4, 120000), // Marsh Stakato Soldier
            intArrayOf(SOPHYA_LETTER_4, 20234, STAKATO_TALON, 500000, CARGO_BOX_4, 130000)
        )// Marsh Stakato Drone
    }
}