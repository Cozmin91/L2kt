package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q348_AnArrogantSearch : Quest(348, "An Arrogant Search") {

    // NPCs instances, in order to avoid infinite instances creation speaking to chests.
    private var _elberoth: Npc? = null
    private var _shadowFang: Npc? = null
    private var _angelKiller: Npc? = null

    init {

        setItemsIds(
            TITAN_POWERSTONE,
            HANELLIN_FIRST_LETTER,
            HANELLIN_SECOND_LETTER,
            HANELLIN_THIRD_LETTER,
            FIRST_KEY_OF_ARK,
            SECOND_KEY_OF_ARK,
            THIRD_KEY_OF_ARK,
            BOOK_OF_SAINT,
            BLOOD_OF_SAINT,
            BOUGH_OF_SAINT,
            WHITE_FABRIC_TRIBE,
            WHITE_FABRIC_ANGELS
        )

        addStartNpc(HANELLIN)
        addTalkId(
            HANELLIN,
            CLAUDIA_ATHEBALDT,
            MARTIEN,
            HARNE,
            HOLY_ARK_OF_SECRECY_1,
            HOLY_ARK_OF_SECRECY_2,
            HOLY_ARK_OF_SECRECY_3,
            ARK_GUARDIAN_CORPSE,
            GUSTAV_ATHEBALDT,
            HARDIN,
            IASON_HEINE
        )

        addSpawnId(ARK_GUARDIAN_ELBEROTH, ARK_GUARDIAN_SHADOW_FANG, ANGEL_KILLER)
        addAttackId(
            ARK_GUARDIAN_ELBEROTH,
            ARK_GUARDIAN_SHADOW_FANG,
            ANGEL_KILLER,
            PLANTINUM_TRIBE_SHAMAN,
            PLANTINUM_TRIBE_OVERLORD
        )

        addKillId(
            LESSER_GIANT_MAGE,
            LESSER_GIANT_ELDER,
            ARK_GUARDIAN_ELBEROTH,
            ARK_GUARDIAN_SHADOW_FANG,
            ANGEL_KILLER,
            PLANTINUM_TRIBE_SHAMAN,
            PLANTINUM_TRIBE_OVERLORD,
            GUARDIAN_ANGEL,
            SEAL_ANGEL
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30864-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30864-09.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TITAN_POWERSTONE, 1)
        } else if (event.equals("30864-17.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(HANELLIN_FIRST_LETTER, 1)
            st.giveItems(HANELLIN_SECOND_LETTER, 1)
            st.giveItems(HANELLIN_THIRD_LETTER, 1)
        } else if (event.equals("30864-36.htm", ignoreCase = true)) {
            st["cond"] = "24"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.rewardItems(57, Rnd[1, 2] * 12000)
        } else if (event.equals("30864-37.htm", ignoreCase = true)) {
            st["cond"] = "25"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30864-51.htm", ignoreCase = true)) {
            st["cond"] = "26"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(WHITE_FABRIC_ANGELS, if (st.hasQuestItems(BLOODED_FABRIC)) 9 else 10)
        } else if (event.equals("30864-58.htm", ignoreCase = true)) {
            st["cond"] = "27"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30864-57.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30864-56.htm", ignoreCase = true)) {
            st["cond"] = "29"
            st["gustav"] = "0" // st.unset doesn't work.
            st["hardin"] = "0"
            st["iason"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(WHITE_FABRIC_ANGELS, 10)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (st.hasQuestItems(BLOODED_FABRIC))
                htmltext = "30864-00.htm"
            else if (player.level < 60)
                htmltext = "30864-01.htm"
            else
                htmltext = "30864-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HANELLIN -> if (cond == 1)
                        htmltext = "30864-02.htm"
                    else if (cond == 2)
                        htmltext = if (!st.hasQuestItems(TITAN_POWERSTONE)) "30864-06.htm" else "30864-07.htm"
                    else if (cond == 4)
                        htmltext = "30864-09.htm"
                    else if (cond > 4 && cond < 21)
                        htmltext = if (player.inventory!!.hasAtLeastOneItem(
                                BOOK_OF_SAINT,
                                BLOOD_OF_SAINT,
                                BOUGH_OF_SAINT
                            )
                        ) "30864-28.htm" else "30864-24.htm"
                    else if (cond == 21) {
                        htmltext = "30864-29.htm"
                        st["cond"] = "22"
                        st.takeItems(BOOK_OF_SAINT, 1)
                        st.takeItems(BLOOD_OF_SAINT, 1)
                        st.takeItems(BOUGH_OF_SAINT, 1)
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 22) {
                        if (st.hasQuestItems(WHITE_FABRIC_TRIBE))
                            htmltext = "30864-31.htm"
                        else if (st.getQuestItemsCount(ANTIDOTE) < 5 || !st.hasQuestItems(HEALING_POTION))
                            htmltext = "30864-30.htm"
                        else {
                            htmltext = "30864-31.htm"
                            st.takeItems(ANTIDOTE, 5)
                            st.takeItems(HEALING_POTION, 1)
                            st.giveItems(WHITE_FABRIC_TRIBE, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        }
                    } else if (cond == 24)
                        htmltext = "30864-38.htm"
                    else if (cond == 25) {
                        if (st.hasQuestItems(WHITE_FABRIC_TRIBE))
                            htmltext = "30864-39.htm"
                        else if (st.hasQuestItems(BLOODED_FABRIC))
                            htmltext = "30864-49.htm"
                        else {
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(true)
                        }// Use the only fabric on Baium, drop the quest.
                    } else if (cond == 26) {
                        val count = st.getQuestItemsCount(BLOODED_FABRIC)

                        if (count + st.getQuestItemsCount(WHITE_FABRIC_ANGELS) < 10) {
                            htmltext = "30864-54.htm"
                            st.takeItems(BLOODED_FABRIC, -1)
                            st.rewardItems(57, 1000 * count + 4000)
                            st.exitQuest(true)
                        } else if (count < 10)
                            htmltext = "30864-52.htm"
                        else if (count >= 10)
                            htmltext = "30864-53.htm"
                    } else if (cond == 27) {
                        if (st.getInt("gustav") + st.getInt("hardin") + st.getInt("iason") == 3) {
                            htmltext = "30864-60.htm"
                            st["cond"] = "28"
                            st.rewardItems(57, 49000)
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else if (st.hasQuestItems(BLOODED_FABRIC) && st.getInt("usedonbaium") != 1)
                            htmltext = "30864-59.htm"
                        else {
                            htmltext = "30864-61.htm"
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(true)
                        }
                    } else if (cond == 28)
                        htmltext = "30864-55.htm"
                    else if (cond == 29) {
                        val count = st.getQuestItemsCount(BLOODED_FABRIC)

                        if (count + st.getQuestItemsCount(WHITE_FABRIC_ANGELS) < 10) {
                            htmltext = "30864-54.htm"
                            st.takeItems(BLOODED_FABRIC, -1)
                            st.rewardItems(57, 5000 * count)
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(true)
                        } else if (count < 10)
                            htmltext = "30864-52.htm"
                        else if (count >= 10)
                            htmltext = "30864-53.htm"
                    }

                    GUSTAV_ATHEBALDT -> if (cond == 27) {
                        if (st.getQuestItemsCount(BLOODED_FABRIC) >= 3 && st.getInt("gustav") == 0) {
                            st["gustav"] = "1"
                            htmltext = "30760-01.htm"
                            st.takeItems(BLOODED_FABRIC, 3)
                        } else if (st.getInt("gustav") == 1)
                            htmltext = "30760-02.htm"
                        else {
                            htmltext = "30760-03.htm"
                            st["usedonbaium"] = "1"
                        }
                    }

                    HARDIN -> if (cond == 27) {
                        if (st.hasQuestItems(BLOODED_FABRIC) && st.getInt("hardin") == 0) {
                            st["hardin"] = "1"
                            htmltext = "30832-01.htm"
                            st.takeItems(BLOODED_FABRIC, 1)
                        } else if (st.getInt("hardin") == 1)
                            htmltext = "30832-02.htm"
                        else {
                            htmltext = "30832-03.htm"
                            st["usedonbaium"] = "1"
                        }
                    }

                    IASON_HEINE -> if (cond == 27) {
                        if (st.getQuestItemsCount(BLOODED_FABRIC) >= 6 && st.getInt("iason") == 0) {
                            st["iason"] = "1"
                            htmltext = "30969-01.htm"
                            st.takeItems(BLOODED_FABRIC, 6)
                        } else if (st.getInt("iason") == 1)
                            htmltext = "30969-02.htm"
                        else {
                            htmltext = "30969-03.htm"
                            st["usedonbaium"] = "1"
                        }
                    }

                    HARNE -> if (cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BLOOD_OF_SAINT)) {
                            if (st.hasQuestItems(HANELLIN_FIRST_LETTER)) {
                                htmltext = "30144-01.htm"
                                st["cond"] = "17"
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(HANELLIN_FIRST_LETTER, 1)
                                st.addRadar(-418, 44174, -3568)
                            } else if (!st.hasQuestItems(FIRST_KEY_OF_ARK)) {
                                htmltext = "30144-03.htm"
                                st.addRadar(-418, 44174, -3568)
                            } else
                                htmltext = "30144-04.htm"
                        } else
                            htmltext = "30144-05.htm"
                    }

                    CLAUDIA_ATHEBALDT -> if (cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BOOK_OF_SAINT)) {
                            if (st.hasQuestItems(HANELLIN_SECOND_LETTER)) {
                                htmltext = "31001-01.htm"
                                st["cond"] = "9"
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(HANELLIN_SECOND_LETTER, 1)
                                st.addRadar(181472, 7158, -2725)
                            } else if (!st.hasQuestItems(SECOND_KEY_OF_ARK)) {
                                htmltext = "31001-03.htm"
                                st.addRadar(181472, 7158, -2725)
                            } else
                                htmltext = "31001-04.htm"
                        } else
                            htmltext = "31001-05.htm"
                    }

                    MARTIEN -> if (cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BOUGH_OF_SAINT)) {
                            if (st.hasQuestItems(HANELLIN_THIRD_LETTER)) {
                                htmltext = "30645-01.htm"
                                st["cond"] = "13"
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(HANELLIN_THIRD_LETTER, 1)
                                st.addRadar(50693, 158674, 376)
                            } else if (!st.hasQuestItems(THIRD_KEY_OF_ARK)) {
                                htmltext = "30645-03.htm"
                                st.addRadar(50693, 158674, 376)
                            } else
                                htmltext = "30645-04.htm"
                        } else
                            htmltext = "30645-05.htm"
                    }

                    ARK_GUARDIAN_CORPSE -> if (!st.hasQuestItems(HANELLIN_FIRST_LETTER) && cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(FIRST_KEY_OF_ARK) && !st.hasQuestItems(BLOOD_OF_SAINT)) {
                            if (st.getInt("angelkiller") == 0) {
                                htmltext = "30980-01.htm"
                                if (_angelKiller == null)
                                    _angelKiller = addSpawn(ANGEL_KILLER, npc, false, 0, true)

                                if (st.getInt("cond") != 18) {
                                    st["cond"] = "18"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                }
                            } else {
                                htmltext = "30980-02.htm"
                                st.giveItems(FIRST_KEY_OF_ARK, 1)
                                st.playSound(QuestState.SOUND_ITEMGET)

                                st.unset("angelkiller")
                            }
                        } else
                            htmltext = "30980-03.htm"
                    }

                    HOLY_ARK_OF_SECRECY_1 -> if (!st.hasQuestItems(HANELLIN_FIRST_LETTER) && cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BLOOD_OF_SAINT)) {
                            if (st.hasQuestItems(FIRST_KEY_OF_ARK)) {
                                htmltext = "30977-02.htm"
                                st["cond"] = "20"
                                st.playSound(QuestState.SOUND_MIDDLE)

                                st.takeItems(FIRST_KEY_OF_ARK, 1)
                                st.giveItems(BLOOD_OF_SAINT, 1)

                                if (st.hasQuestItems(BOOK_OF_SAINT, BOUGH_OF_SAINT))
                                    st["cond"] = "21"
                            } else
                                htmltext = "30977-04.htm"
                        } else
                            htmltext = "30977-03.htm"
                    }

                    HOLY_ARK_OF_SECRECY_2 -> if (!st.hasQuestItems(HANELLIN_SECOND_LETTER) && cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BOOK_OF_SAINT)) {
                            if (!st.hasQuestItems(SECOND_KEY_OF_ARK)) {
                                htmltext = "30978-01.htm"
                                if (_elberoth == null)
                                    _elberoth = addSpawn(ARK_GUARDIAN_ELBEROTH, npc, false, 0, true)
                            } else {
                                htmltext = "30978-02.htm"
                                st["cond"] = "12"
                                st.playSound(QuestState.SOUND_MIDDLE)

                                st.takeItems(SECOND_KEY_OF_ARK, 1)
                                st.giveItems(BOOK_OF_SAINT, 1)

                                if (st.hasQuestItems(BLOOD_OF_SAINT, BOUGH_OF_SAINT))
                                    st["cond"] = "21"
                            }
                        } else
                            htmltext = "30978-03.htm"
                    }

                    HOLY_ARK_OF_SECRECY_3 -> if (!st.hasQuestItems(HANELLIN_THIRD_LETTER) && cond >= 5 && cond <= 22) {
                        if (!st.hasQuestItems(BOUGH_OF_SAINT)) {
                            if (!st.hasQuestItems(THIRD_KEY_OF_ARK)) {
                                htmltext = "30979-01.htm"
                                if (_shadowFang == null)
                                    _shadowFang = addSpawn(ARK_GUARDIAN_SHADOW_FANG, npc, false, 0, true)
                            } else {
                                htmltext = "30979-02.htm"
                                st["cond"] = "16"
                                st.playSound(QuestState.SOUND_MIDDLE)

                                st.takeItems(THIRD_KEY_OF_ARK, 1)
                                st.giveItems(BOUGH_OF_SAINT, 1)

                                if (st.hasQuestItems(BLOOD_OF_SAINT, BOOK_OF_SAINT))
                                    st["cond"] = "21"
                            }
                        } else
                            htmltext = "30979-03.htm"
                    }
                }
            }
        }

        return htmltext
    }

    override fun onSpawn(npc: Npc): String? {
        when (npc.npcId) {
            ARK_GUARDIAN_ELBEROTH -> npc.broadcastNpcSay("This does not belong to you. Take your hands out!")

            ARK_GUARDIAN_SHADOW_FANG -> npc.broadcastNpcSay("I don't believe it! Grrr!")

            ANGEL_KILLER -> npc.broadcastNpcSay("I have the key, do you wish to steal it?")
        }

        return null
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            ARK_GUARDIAN_ELBEROTH -> if (npc.scriptValue == 0) {
                npc.broadcastNpcSay("...I feel very sorry, but I have taken your life.")
                npc.scriptValue = 1
            }

            ARK_GUARDIAN_SHADOW_FANG -> if (npc.scriptValue == 0) {
                npc.broadcastNpcSay("I will cover this mountain with your blood!")
                npc.scriptValue = 1
            }

            ANGEL_KILLER -> {
                if (npc.scriptValue == 0) {
                    npc.broadcastNpcSay("Haha.. Really amusing! As for the key, search the corpse!")
                    npc.scriptValue = 1
                }

                if (npc.currentHp / npc.maxHp < 0.50) {
                    npc.abortAttack()
                    npc.broadcastNpcSay("Can't get rid of you... Did you get the key from the corpse?")
                    npc.decayMe()

                    st["cond"] = "19"
                    st["angelkiller"] = "1"
                    st.playSound(QuestState.SOUND_MIDDLE)

                    _angelKiller = null
                }
            }

            PLANTINUM_TRIBE_OVERLORD, PLANTINUM_TRIBE_SHAMAN -> {
                val cond = st.getInt("cond")
                if ((cond == 24 || cond == 25) && Rnd[500] < 1 && st.hasQuestItems(WHITE_FABRIC_TRIBE)) {
                    st.takeItems(WHITE_FABRIC_TRIBE, 1)
                    st.giveItems(BLOODED_FABRIC, 1)

                    if (cond != 24)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    else {
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }
                }
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            LESSER_GIANT_ELDER, LESSER_GIANT_MAGE -> if (cond == 2)
                st.dropItems(TITAN_POWERSTONE, 1, 1, 100000)

            ARK_GUARDIAN_ELBEROTH -> {
                if (cond >= 5 && cond <= 22 && !st.hasQuestItems(SECOND_KEY_OF_ARK)) {
                    st["cond"] = "11"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(SECOND_KEY_OF_ARK, 1)
                    npc.broadcastNpcSay("Oh, dull-witted.. God, they...")
                }
                _elberoth = null
            }

            ARK_GUARDIAN_SHADOW_FANG -> {
                if (cond >= 5 && cond <= 22 && !st.hasQuestItems(THIRD_KEY_OF_ARK)) {
                    st["cond"] = "15"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(THIRD_KEY_OF_ARK, 1)
                    npc.broadcastNpcSay("You do not know.. Seven seals are.. coughs")
                }
                _shadowFang = null
            }

            SEAL_ANGEL, GUARDIAN_ANGEL -> if ((cond == 26 || cond == 29) && Rnd[4] < 1 && st.hasQuestItems(
                    WHITE_FABRIC_ANGELS
                )
            ) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(WHITE_FABRIC_ANGELS, 1)
                st.giveItems(BLOODED_FABRIC, 1)
            }

            ANGEL_KILLER -> _angelKiller = null
        }

        return null
    }

    companion object {
        private val qn = "Q348_AnArrogantSearch"

        // Items
        private val TITAN_POWERSTONE = 4287
        private val HANELLIN_FIRST_LETTER = 4288
        private val HANELLIN_SECOND_LETTER = 4289
        private val HANELLIN_THIRD_LETTER = 4290
        private val FIRST_KEY_OF_ARK = 4291
        private val SECOND_KEY_OF_ARK = 4292
        private val THIRD_KEY_OF_ARK = 4293
        private val BOOK_OF_SAINT = 4397
        private val BLOOD_OF_SAINT = 4398
        private val BOUGH_OF_SAINT = 4399
        private val WHITE_FABRIC_TRIBE = 4294
        private val WHITE_FABRIC_ANGELS = 5232
        private val BLOODED_FABRIC = 4295

        private val ANTIDOTE = 1831
        private val HEALING_POTION = 1061

        // NPCs
        private val HANELLIN = 30864
        private val CLAUDIA_ATHEBALDT = 31001
        private val MARTIEN = 30645
        private val HARNE = 30144
        private val ARK_GUARDIAN_CORPSE = 30980
        private val HOLY_ARK_OF_SECRECY_1 = 30977
        private val HOLY_ARK_OF_SECRECY_2 = 30978
        private val HOLY_ARK_OF_SECRECY_3 = 30979
        private val GUSTAV_ATHEBALDT = 30760
        private val HARDIN = 30832
        private val IASON_HEINE = 30969

        // Monsters
        private val LESSER_GIANT_MAGE = 20657
        private val LESSER_GIANT_ELDER = 20658
        private val PLANTINUM_TRIBE_SHAMAN = 20828
        private val PLANTINUM_TRIBE_OVERLORD = 20829
        private val GUARDIAN_ANGEL = 20859
        private val SEAL_ANGEL = 20860

        // Quest Monsters
        private val ANGEL_KILLER = 27184
        private val ARK_GUARDIAN_ELBEROTH = 27182
        private val ARK_GUARDIAN_SHADOW_FANG = 27183
    }
}