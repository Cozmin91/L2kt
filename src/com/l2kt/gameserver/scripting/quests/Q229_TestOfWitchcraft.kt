package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q229_TestOfWitchcraft : Quest(229, "Test Of Witchcraft") {
    init {

        setItemsIds(
            ORIM_DIAGRAM,
            ALEXANDRIA_BOOK,
            IKER_LIST,
            DIRE_WYRM_FANG,
            LETO_LIZARDMAN_CHARM,
            EN_GOLEM_HEARTSTONE,
            LARA_MEMO,
            NESTLE_MEMO,
            LEOPOLD_JOURNAL,
            AKLANTOTH_GEM_1,
            AKLANTOTH_GEM_2,
            AKLANTOTH_GEM_3,
            AKLANTOTH_GEM_4,
            AKLANTOTH_GEM_5,
            AKLANTOTH_GEM_6,
            BRIMSTONE_1,
            ORIM_INSTRUCTIONS,
            ORIM_LETTER_1,
            ORIM_LETTER_2,
            SIR_VASPER_LETTER,
            VADIN_CRUCIFIX,
            TAMLIN_ORC_AMULET,
            VADIN_SANCTIONS,
            IKER_AMULET,
            SOULTRAP_CRYSTAL,
            PURGATORY_KEY,
            ZERUEL_BIND_CRYSTAL,
            BRIMSTONE_2,
            SWORD_OF_BINDING
        )

        addStartNpc(ORIM)
        addTalkId(
            LARA,
            ALEXANDRIA,
            IKER,
            VADIN,
            NESTLE,
            SIR_KLAUS_VASPER,
            LEOPOLD,
            KAIRA,
            ORIM,
            RODERIK,
            ENDRIGO,
            EVERT
        )

        addAttackId(NAMELESS_REVENANT, SKELETAL_MERCENARY, DREVANUL_PRINCE_ZERUEL)
        addKillId(
            DIRE_WYRM,
            ENCHANTED_STONE_GOLEM,
            LETO_LIZARDMAN,
            LETO_LIZARDMAN_ARCHER,
            LETO_LIZARDMAN_SOLDIER,
            LETO_LIZARDMAN_WARRIOR,
            LETO_LIZARDMAN_SHAMAN,
            LETO_LIZARDMAN_OVERLORD,
            TAMLIN_ORC,
            TAMLIN_ORC_ARCHER,
            NAMELESS_REVENANT,
            SKELETAL_MERCENARY,
            DREVANUL_PRINCE_ZERUEL
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // ORIM
        if (event.equals("30630-08.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ORIM_DIAGRAM, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30630-08a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30630-14.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.unset("gem456")
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(AKLANTOTH_GEM_1, 1)
            st.takeItems(AKLANTOTH_GEM_2, 1)
            st.takeItems(AKLANTOTH_GEM_3, 1)
            st.takeItems(AKLANTOTH_GEM_4, 1)
            st.takeItems(AKLANTOTH_GEM_5, 1)
            st.takeItems(AKLANTOTH_GEM_6, 1)
            st.takeItems(ALEXANDRIA_BOOK, 1)
            st.giveItems(BRIMSTONE_1, 1)
            addSpawn(DREVANUL_PRINCE_ZERUEL, 70381, 109638, -3726, 0, false, 120000, true)
        } else if (event.equals("30630-16.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BRIMSTONE_1, 1)
            st.giveItems(ORIM_INSTRUCTIONS, 1)
            st.giveItems(ORIM_LETTER_1, 1)
            st.giveItems(ORIM_LETTER_2, 1)
        } else if (event.equals("30630-22.htm", ignoreCase = true)) {
            st.takeItems(IKER_AMULET, 1)
            st.takeItems(ORIM_INSTRUCTIONS, 1)
            st.takeItems(PURGATORY_KEY, 1)
            st.takeItems(SWORD_OF_BINDING, 1)
            st.takeItems(ZERUEL_BIND_CRYSTAL, 1)
            st.giveItems(MARK_OF_WITCHCRAFT, 1)
            st.rewardExpAndSp(139796, 40000)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30098-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st["gem456"] = "1"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ORIM_DIAGRAM, 1)
            st.giveItems(ALEXANDRIA_BOOK, 1)
        } else if (event.equals("30110-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(IKER_LIST, 1)
        } else if (event.equals("30110-08.htm", ignoreCase = true)) {
            st.takeItems(ORIM_LETTER_2, 1)
            st.giveItems(IKER_AMULET, 1)
            st.giveItems(SOULTRAP_CRYSTAL, 1)

            if (st.hasQuestItems(SWORD_OF_BINDING)) {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else
                st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30476-02.htm", ignoreCase = true)) {
            st.giveItems(AKLANTOTH_GEM_2, 1)

            if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_3) && st.getInt("gem456") == 6) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else
                st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30063-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(LARA_MEMO, 1)
        } else if (event.equals("30314-02.htm", ignoreCase = true)) {
            st["gem456"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(NESTLE_MEMO, 1)
        } else if (event.equals("30435-02.htm", ignoreCase = true)) {
            st["gem456"] = "3"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(NESTLE_MEMO, 1)
            st.giveItems(LEOPOLD_JOURNAL, 1)
        } else if (event.equals("30417-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(ORIM_LETTER_1, 1)
            st.giveItems(SIR_VASPER_LETTER, 1)
        } else if (event.equals("30633-02.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BRIMSTONE_2, 1)

            if (!_drevanulPrinceZeruel) {
                addSpawn(DREVANUL_PRINCE_ZERUEL, 13395, 169807, -3708, 0, false, 299000, true)
                _drevanulPrinceZeruel = true

                // Resets Drevanul Prince Zeruel
                startQuestTimer("zeruel_cleanup", 300000, null, player, false)
            }
        } else if (event.equals("zeruel_despawn", ignoreCase = true)) {
            npc!!.abortAttack()
            npc.decayMe()
            return null
        } else if (event.equals("zeruel_cleanup", ignoreCase = true)) {
            _drevanulPrinceZeruel = false
            return null
        }// Drevanul Prince Zeruel's reset
        // Despawns Drevanul Prince Zeruel
        // EVERT
        // SIR KLAUS VASPER
        // LEOPOLD
        // NESTLE
        // LARA
        // KAIRA
        // IKER
        // ALEXANDRIA

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.KNIGHT && player.classId != ClassId.HUMAN_WIZARD && player.classId != ClassId.PALUS_KNIGHT)
                htmltext = "30630-01.htm"
            else if (player.level < 39)
                htmltext = "30630-02.htm"
            else
                htmltext = if (player.classId == ClassId.HUMAN_WIZARD) "30630-03.htm" else "30630-05.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val gem456 = st.getInt("gem456")

                when (npc.npcId) {
                    ORIM -> if (cond == 1)
                        htmltext = "30630-09.htm"
                    else if (cond == 2)
                        htmltext = "30630-10.htm"
                    else if (cond == 3)
                        htmltext = "30630-11.htm"
                    else if (cond == 4)
                        htmltext = "30630-14.htm"
                    else if (cond == 5)
                        htmltext = "30630-15.htm"
                    else if (cond == 6)
                        htmltext = "30630-17.htm"
                    else if (cond == 7) {
                        htmltext = "30630-18.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 8 || cond == 9)
                        htmltext = "30630-18.htm"
                    else if (cond == 10)
                        htmltext = "30630-19.htm"

                    ALEXANDRIA -> if (cond == 1)
                        htmltext = "30098-01.htm"
                    else if (cond == 2)
                        htmltext = "30098-04.htm"
                    else
                        htmltext = "30098-05.htm"

                    KAIRA -> if (st.hasQuestItems(AKLANTOTH_GEM_2))
                        htmltext = "30476-03.htm"
                    else if (cond == 2)
                        htmltext = "30476-01.htm"
                    else if (cond > 3)
                        htmltext = "30476-04.htm"

                    IKER -> if (st.hasQuestItems(AKLANTOTH_GEM_1))
                        htmltext = "30110-06.htm"
                    else if (st.hasQuestItems(IKER_LIST)) {
                        if (st.getQuestItemsCount(DIRE_WYRM_FANG) + st.getQuestItemsCount(LETO_LIZARDMAN_CHARM) + st.getQuestItemsCount(
                                EN_GOLEM_HEARTSTONE
                            ) < 60
                        )
                            htmltext = "30110-04.htm"
                        else {
                            htmltext = "30110-05.htm"
                            st.takeItems(IKER_LIST, 1)
                            st.takeItems(DIRE_WYRM_FANG, -1)
                            st.takeItems(EN_GOLEM_HEARTSTONE, -1)
                            st.takeItems(LETO_LIZARDMAN_CHARM, -1)
                            st.giveItems(AKLANTOTH_GEM_1, 1)

                            if (st.hasQuestItems(AKLANTOTH_GEM_2, AKLANTOTH_GEM_3) && gem456 == 6) {
                                st["cond"] = "3"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        }
                    } else if (cond == 2)
                        htmltext = "30110-01.htm"
                    else if (cond == 6 && !st.hasQuestItems(SOULTRAP_CRYSTAL))
                        htmltext = "30110-07.htm"
                    else if (cond >= 6 && cond < 10)
                        htmltext = "30110-09.htm"
                    else if (cond == 10)
                        htmltext = "30110-10.htm"

                    LARA -> if (st.hasQuestItems(AKLANTOTH_GEM_3))
                        htmltext = "30063-04.htm"
                    else if (st.hasQuestItems(LARA_MEMO))
                        htmltext = "30063-03.htm"
                    else if (cond == 2)
                        htmltext = "30063-01.htm"
                    else if (cond > 2)
                        htmltext = "30063-05.htm"

                    RODERIK, ENDRIGO -> if (st.hasAtLeastOneQuestItem(LARA_MEMO, AKLANTOTH_GEM_3))
                        htmltext = npc.npcId.toString() + "-01.htm"

                    NESTLE -> if (gem456 == 1)
                        htmltext = "30314-01.htm"
                    else if (gem456 == 2)
                        htmltext = "30314-03.htm"
                    else if (gem456 > 2)
                        htmltext = "30314-04.htm"

                    LEOPOLD -> if (gem456 == 2)
                        htmltext = "30435-01.htm"
                    else if (gem456 > 2 && gem456 < 6)
                        htmltext = "30435-03.htm"
                    else if (gem456 == 6)
                        htmltext = "30435-04.htm"
                    else if (cond > 3)
                        htmltext = "30435-05.htm"

                    SIR_KLAUS_VASPER -> if (st.hasAtLeastOneQuestItem(SIR_VASPER_LETTER, VADIN_CRUCIFIX))
                        htmltext = "30417-04.htm"
                    else if (st.hasQuestItems(VADIN_SANCTIONS)) {
                        htmltext = "30417-05.htm"
                        st.takeItems(VADIN_SANCTIONS, 1)
                        st.giveItems(SWORD_OF_BINDING, 1)

                        if (st.hasQuestItems(SOULTRAP_CRYSTAL)) {
                            st["cond"] = "7"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (cond == 6)
                        htmltext = "30417-01.htm"
                    else if (cond > 6)
                        htmltext = "30417-06.htm"

                    VADIN -> if (st.hasQuestItems(SIR_VASPER_LETTER)) {
                        htmltext = "30188-01.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.takeItems(SIR_VASPER_LETTER, 1)
                        st.giveItems(VADIN_CRUCIFIX, 1)
                    } else if (st.hasQuestItems(VADIN_CRUCIFIX)) {
                        if (st.getQuestItemsCount(TAMLIN_ORC_AMULET) < 20)
                            htmltext = "30188-02.htm"
                        else {
                            htmltext = "30188-03.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(TAMLIN_ORC_AMULET, -1)
                            st.takeItems(VADIN_CRUCIFIX, -1)
                            st.giveItems(VADIN_SANCTIONS, 1)
                        }
                    } else if (st.hasQuestItems(VADIN_SANCTIONS))
                        htmltext = "30188-04.htm"
                    else if (cond > 6)
                        htmltext = "30188-05.htm"

                    EVERT -> if (cond == 7 || cond == 8)
                        htmltext = "30633-01.htm"
                    else if (cond == 9) {
                        htmltext = "30633-02.htm"

                        if (!_drevanulPrinceZeruel) {
                            addSpawn(DREVANUL_PRINCE_ZERUEL, 13395, 169807, -3708, 0, false, 299000, true)
                            _drevanulPrinceZeruel = true

                            // Resets Drevanul Prince Zeruel
                            startQuestTimer("zeruel_cleanup", 300000, null, player, false)
                        }
                    } else if (cond == 10)
                        htmltext = "30633-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            NAMELESS_REVENANT -> if (st.hasQuestItems(LARA_MEMO) && !npc.isScriptValue(1)) {
                npc.scriptValue = 1
                npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!")
            }

            SKELETAL_MERCENARY -> if (st.getInt("gem456") > 2 && st.getInt("gem456") < 6 && !npc.isScriptValue(1)) {
                npc.scriptValue = 1
                npc.broadcastNpcSay("I absolutely cannot give it to you! It is my precious jewel!")
            }

            DREVANUL_PRINCE_ZERUEL -> if (cond == 4 && !npc.isScriptValue(1)) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)

                npc.scriptValue = 1
                npc.broadcastNpcSay("I'll take your lives later!!")

                startQuestTimer("zeruel_despawn", 1000, npc, player, false)
            } else if (cond == 9 && _drevanulPrinceZeruel) {
                if (st.getItemEquipped(7) == SWORD_OF_BINDING) {
                    _swordOfBinding = true

                    if (!npc.isScriptValue(1)) {
                        npc.scriptValue = 1
                        npc.broadcastNpcSay("That sword is really...!")
                    }
                } else
                    _swordOfBinding = false
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            DIRE_WYRM -> if (st.hasQuestItems(IKER_LIST))
                st.dropItemsAlways(DIRE_WYRM_FANG, 1, 20)

            ENCHANTED_STONE_GOLEM -> if (st.hasQuestItems(IKER_LIST))
                st.dropItemsAlways(EN_GOLEM_HEARTSTONE, 1, 20)

            LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER -> if (st.hasQuestItems(IKER_LIST))
                st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 500000)
            LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR -> if (st.hasQuestItems(IKER_LIST))
                st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 600000)
            LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD -> if (st.hasQuestItems(IKER_LIST))
                st.dropItems(LETO_LIZARDMAN_CHARM, 1, 20, 700000)

            NAMELESS_REVENANT -> if (st.hasQuestItems(LARA_MEMO)) {
                st.takeItems(LARA_MEMO, 1)
                st.giveItems(AKLANTOTH_GEM_3, 1)

                if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_2) && st.getInt("gem456") == 6) {
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            SKELETAL_MERCENARY -> {
                val gem456 = st.getInt("gem456")
                if (gem456 == 3) {
                    st["gem456"] = "4"
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(AKLANTOTH_GEM_4, 1)
                } else if (gem456 == 4) {
                    st["gem456"] = "5"
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(AKLANTOTH_GEM_5, 1)
                } else if (gem456 == 5) {
                    st["gem456"] = "6"
                    st.takeItems(LEOPOLD_JOURNAL, 1)
                    st.giveItems(AKLANTOTH_GEM_6, 1)

                    if (st.hasQuestItems(AKLANTOTH_GEM_1, AKLANTOTH_GEM_2, AKLANTOTH_GEM_3)) {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }
            }

            TAMLIN_ORC, TAMLIN_ORC_ARCHER -> if (st.hasQuestItems(VADIN_CRUCIFIX))
                st.dropItems(TAMLIN_ORC_AMULET, 1, 20, 500000)

            DREVANUL_PRINCE_ZERUEL -> if (cond == 9 && _drevanulPrinceZeruel) {
                if (_swordOfBinding) {
                    st["cond"] = "10"
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.takeItems(BRIMSTONE_2, 1)
                    st.takeItems(SOULTRAP_CRYSTAL, 1)
                    st.giveItems(PURGATORY_KEY, 1)
                    st.giveItems(ZERUEL_BIND_CRYSTAL, 1)
                    npc.broadcastNpcSay("No! I haven't completely finished the command for destruction and slaughter yet!!!")
                }
                cancelQuestTimer("zeruel_cleanup", null, player)
                _drevanulPrinceZeruel = false
            }
        }

        return null
    }

    companion object {
        private val qn = "Q229_TestOfWitchcraft"

        // Items
        private val ORIM_DIAGRAM = 3308
        private val ALEXANDRIA_BOOK = 3309
        private val IKER_LIST = 3310
        private val DIRE_WYRM_FANG = 3311
        private val LETO_LIZARDMAN_CHARM = 3312
        private val EN_GOLEM_HEARTSTONE = 3313
        private val LARA_MEMO = 3314
        private val NESTLE_MEMO = 3315
        private val LEOPOLD_JOURNAL = 3316
        private val AKLANTOTH_GEM_1 = 3317
        private val AKLANTOTH_GEM_2 = 3318
        private val AKLANTOTH_GEM_3 = 3319
        private val AKLANTOTH_GEM_4 = 3320
        private val AKLANTOTH_GEM_5 = 3321
        private val AKLANTOTH_GEM_6 = 3322
        private val BRIMSTONE_1 = 3323
        private val ORIM_INSTRUCTIONS = 3324
        private val ORIM_LETTER_1 = 3325
        private val ORIM_LETTER_2 = 3326
        private val SIR_VASPER_LETTER = 3327
        private val VADIN_CRUCIFIX = 3328
        private val TAMLIN_ORC_AMULET = 3329
        private val VADIN_SANCTIONS = 3330
        private val IKER_AMULET = 3331
        private val SOULTRAP_CRYSTAL = 3332
        private val PURGATORY_KEY = 3333
        private val ZERUEL_BIND_CRYSTAL = 3334
        private val BRIMSTONE_2 = 3335
        private val SWORD_OF_BINDING = 3029

        // Rewards
        private val MARK_OF_WITCHCRAFT = 3307
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val LARA = 30063
        private val ALEXANDRIA = 30098
        private val IKER = 30110
        private val VADIN = 30188
        private val NESTLE = 30314
        private val SIR_KLAUS_VASPER = 30417
        private val LEOPOLD = 30435
        private val KAIRA = 30476
        private val ORIM = 30630
        private val RODERIK = 30631
        private val ENDRIGO = 30632
        private val EVERT = 30633

        // Monsters
        private val DIRE_WYRM = 20557
        private val ENCHANTED_STONE_GOLEM = 20565
        private val LETO_LIZARDMAN = 20577
        private val LETO_LIZARDMAN_ARCHER = 20578
        private val LETO_LIZARDMAN_SOLDIER = 20579
        private val LETO_LIZARDMAN_WARRIOR = 20580
        private val LETO_LIZARDMAN_SHAMAN = 20581
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val TAMLIN_ORC = 20601
        private val TAMLIN_ORC_ARCHER = 20602
        private val NAMELESS_REVENANT = 27099
        private val SKELETAL_MERCENARY = 27100
        private val DREVANUL_PRINCE_ZERUEL = 27101

        // Checks
        private var _drevanulPrinceZeruel = false
        private var _swordOfBinding = false
    }
}