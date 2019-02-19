package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.concurrent.ConcurrentHashMap

class Q230_TestOfTheSummoner : Quest(230, "Test of the Summoner") {
    init {

        setItemsIds(
            LETO_LIZARDMAN_AMULET,
            SAC_OF_REDSPORES,
            KARUL_BUGBEAR_TOTEM,
            SHARDS_OF_MANASHEN,
            BREKA_ORC_TOTEM,
            CRIMSON_BLOODSTONE,
            TALONS_OF_TYRANT,
            WINGS_OF_DRONEANT,
            TUSK_OF_WINDSUS,
            FANGS_OF_WYRM,
            LARA_LIST_1,
            LARA_LIST_2,
            LARA_LIST_3,
            LARA_LIST_4,
            LARA_LIST_5,
            GALATEA_LETTER,
            BEGINNER_ARCANA,
            ALMORS_ARCANA,
            CAMONIELL_ARCANA,
            BELTHUS_ARCANA,
            BASILLIA_ARCANA,
            CELESTIEL_ARCANA,
            BRYNTHEA_ARCANA,
            CRYSTAL_OF_PROGRESS_1,
            CRYSTAL_OF_INPROGRESS_1,
            CRYSTAL_OF_FOUL_1,
            CRYSTAL_OF_DEFEAT_1,
            CRYSTAL_OF_VICTORY_1,
            CRYSTAL_OF_PROGRESS_2,
            CRYSTAL_OF_INPROGRESS_2,
            CRYSTAL_OF_FOUL_2,
            CRYSTAL_OF_DEFEAT_2,
            CRYSTAL_OF_VICTORY_2,
            CRYSTAL_OF_PROGRESS_3,
            CRYSTAL_OF_INPROGRESS_3,
            CRYSTAL_OF_FOUL_3,
            CRYSTAL_OF_DEFEAT_3,
            CRYSTAL_OF_VICTORY_3,
            CRYSTAL_OF_PROGRESS_4,
            CRYSTAL_OF_INPROGRESS_4,
            CRYSTAL_OF_FOUL_4,
            CRYSTAL_OF_DEFEAT_4,
            CRYSTAL_OF_VICTORY_4,
            CRYSTAL_OF_PROGRESS_5,
            CRYSTAL_OF_INPROGRESS_5,
            CRYSTAL_OF_FOUL_5,
            CRYSTAL_OF_DEFEAT_5,
            CRYSTAL_OF_VICTORY_5,
            CRYSTAL_OF_PROGRESS_6,
            CRYSTAL_OF_INPROGRESS_6,
            CRYSTAL_OF_FOUL_6,
            CRYSTAL_OF_DEFEAT_6,
            CRYSTAL_OF_VICTORY_6
        )

        addStartNpc(GALATEA)
        addTalkId(GALATEA, ALMORS, CAMONIELL, BELTHUS, BASILLA, CELESTIEL, BRYNTHEA, LARA)

        addKillId(
            NOBLE_ANT,
            NOBLE_ANT_LEADER,
            WYRM,
            TYRANT,
            TYRANT_KINGPIN,
            BREKA_ORC,
            BREKA_ORC_ARCHER,
            BREKA_ORC_SHAMAN,
            BREKA_ORC_OVERLORD,
            BREKA_ORC_WARRIOR,
            FETTERED_SOUL,
            WINDSUS,
            GIANT_FUNGUS,
            MANASHEN_GARGOYLE,
            LETO_LIZARDMAN,
            LETO_LIZARDMAN_ARCHER,
            LETO_LIZARDMAN_SOLDIER,
            LETO_LIZARDMAN_WARRIOR,
            LETO_LIZARDMAN_SHAMAN,
            LETO_LIZARDMAN_OVERLORD,
            KARUL_BUGBEAR,
            PAKO_THE_CAT,
            UNICORN_RACER,
            SHADOW_TUREN,
            MIMI_THE_CAT,
            UNICORN_PHANTASM,
            SILHOUETTE_TILFO
        )
        addAttackId(PAKO_THE_CAT, UNICORN_RACER, SHADOW_TUREN, MIMI_THE_CAT, UNICORN_PHANTASM, SILHOUETTE_TILFO)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return null

        // GALATEA
        if (event == "30634-08.htm") {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["Belthus"] = "1"
            st["Brynthea"] = "1"
            st["Celestiel"] = "1"
            st["Camoniell"] = "1"
            st["Basilla"] = "1"
            st["Almors"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(GALATEA_LETTER, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30634-08a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event == "30063-02.htm")
        // Lara first time to give a list out
        {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GALATEA_LETTER, 1)

            val random = Rnd[5]

            st.giveItems(LARA_LISTS[random][0], 1)
            st["Lara"] = (random + 1).toString() // avoid 0
        } else if (event == "30063-04.htm")
        // Lara later to give a list out
        {
            val random = Rnd[5]

            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(LARA_LISTS[random][0], 1)
            st["Lara"] = (random + 1).toString()
        } else if (event == "30635-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30635-03.htm"
        } else if (event == "30635-04.htm") {
            st["Almors"] = "2" // set state ready to fight
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_1, -1) // just in case he cheated or lost
            st.takeItems(CRYSTAL_OF_DEFEAT_1, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_1, 1) // give Starting Crystal

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        } else if (event == "30636-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30636-03.htm"
        } else if (event == "30636-04.htm") {
            st["Camoniell"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_2, -1)
            st.takeItems(CRYSTAL_OF_DEFEAT_2, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_2, 1)

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        } else if (event == "30637-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30637-03.htm"
        } else if (event == "30637-04.htm") {
            st["Belthus"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_3, -1)
            st.takeItems(CRYSTAL_OF_DEFEAT_3, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_3, 1)

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        } else if (event == "30638-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30638-03.htm"
        } else if (event == "30638-04.htm") {
            st["Basilla"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_4, -1)
            st.takeItems(CRYSTAL_OF_DEFEAT_4, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_4, 1)

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        } else if (event == "30639-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30639-03.htm"
        } else if (event == "30639-04.htm") {
            st["Celestiel"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_5, -1)
            st.takeItems(CRYSTAL_OF_DEFEAT_5, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_5, 1)

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        } else if (event == "30640-02.htm") {
            if (st.hasQuestItems(BEGINNER_ARCANA))
                htmltext = "30640-03.htm"
        } else if (event == "30640-04.htm") {
            st["Brynthea"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(CRYSTAL_OF_FOUL_6, -1)
            st.takeItems(CRYSTAL_OF_DEFEAT_6, -1)
            st.takeItems(BEGINNER_ARCANA, 1)
            st.giveItems(CRYSTAL_OF_PROGRESS_6, 1)

            npc!!.target = player
            npc.doCast(SkillTable.getInfo(4126, 1))
        }// BRYNTHEA
        // CELESTIEL
        // BASILLA
        // BELTHUS
        // CAMONIELL
        // ALMORS
        // LARA

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        val cond = st.getInt("cond")
        val npcId = npc.npcId

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.HUMAN_WIZARD && player.classId != ClassId.ELVEN_WIZARD && player.classId != ClassId.DARK_WIZARD)
            // wizard, elven wizard, dark wizard
                htmltext = "30634-01.htm"
            else if (player.level < 39)
                htmltext = "30634-02.htm"
            else
                htmltext = "30634-03.htm"

            Quest.STATE_STARTED -> when (npcId) {
                LARA -> if (cond == 1)
                    htmltext = "30063-01.htm"
                else {
                    if (st.getInt("Lara") == 0)
                    // if you havent a part taken, give one
                        htmltext = "30063-03.htm"
                    else {
                        val laraPart = LARA_LISTS[st.getInt("Lara") - 1]
                        if (st.getQuestItemsCount(laraPart[1]) < 30 || st.getQuestItemsCount(laraPart[2]) < 30)
                            htmltext = "30063-05.htm"
                        else {
                            htmltext = "30063-06.htm"
                            st["cond"] = "3"
                            st.unset("Lara")
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(laraPart[0], 1)
                            st.takeItems(laraPart[1], -1)
                            st.takeItems(laraPart[2], -1)
                            st.giveItems(BEGINNER_ARCANA, 2)
                        }
                    }
                }

                GALATEA -> if (cond == 1)
                    htmltext = "30634-09.htm"
                else if (cond == 2 || cond == 3)
                    htmltext = if (!st.hasQuestItems(BEGINNER_ARCANA)) "30634-10.htm" else "30634-11.htm"
                else if (cond == 4) {
                    htmltext = "30634-12.htm"
                    st.takeItems(BEGINNER_ARCANA, -1)
                    st.takeItems(ALMORS_ARCANA, -1)
                    st.takeItems(BASILLIA_ARCANA, -1)
                    st.takeItems(BELTHUS_ARCANA, -1)
                    st.takeItems(BRYNTHEA_ARCANA, -1)
                    st.takeItems(CAMONIELL_ARCANA, -1)
                    st.takeItems(CELESTIEL_ARCANA, -1)
                    st.takeItems(LARA_LIST_1, -1)
                    st.takeItems(LARA_LIST_2, -1)
                    st.takeItems(LARA_LIST_3, -1)
                    st.takeItems(LARA_LIST_4, -1)
                    st.takeItems(LARA_LIST_5, -1)
                    st.giveItems(MARK_OF_SUMMONER, 1)
                    st.rewardExpAndSp(148409, 30000)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }

                ALMORS -> {
                    val almorsStat = st.getInt("Almors")
                    if (almorsStat == 1)
                        htmltext = "30635-01.htm"
                    else if (almorsStat == 2)
                        htmltext = "30635-08.htm"
                    else if (almorsStat == 3)
                    // in battle...
                        htmltext = "30635-09.htm"
                    else if (almorsStat == 4)
                    // haha... your summon lose
                        htmltext = "30635-05.htm"
                    else if (almorsStat == 5)
                        htmltext = "30635-06.htm"
                    else if (almorsStat == 6) {
                        htmltext = "30635-07.htm"
                        st["Almors"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_1, -1)
                        st.giveItems(ALMORS_ARCANA, 1)

                        if (st.hasQuestItems(
                                CAMONIELL_ARCANA,
                                BELTHUS_ARCANA,
                                BASILLIA_ARCANA,
                                CELESTIEL_ARCANA,
                                BRYNTHEA_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (almorsStat == 7)
                        htmltext = "30635-10.htm"
                }

                CAMONIELL -> {
                    val camoniellStat = st.getInt("Camoniell")
                    if (camoniellStat == 1)
                        htmltext = "30636-01.htm"
                    else if (camoniellStat == 2)
                        htmltext = "30636-08.htm"
                    else if (camoniellStat == 3)
                    // in battle...
                        htmltext = "30636-09.htm"
                    else if (camoniellStat == 4)
                    // haha... your summon lose
                        htmltext = "30636-05.htm"
                    else if (camoniellStat == 5)
                        htmltext = "30636-06.htm"
                    else if (camoniellStat == 6) {
                        htmltext = "30636-07.htm"
                        st["Camoniell"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_2, -1)
                        st.giveItems(CAMONIELL_ARCANA, 1)

                        if (st.hasQuestItems(
                                ALMORS_ARCANA,
                                BELTHUS_ARCANA,
                                BASILLIA_ARCANA,
                                CELESTIEL_ARCANA,
                                BRYNTHEA_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (camoniellStat == 7)
                        htmltext = "30636-10.htm"
                }

                BELTHUS -> {
                    val belthusStat = st.getInt("Belthus")
                    if (belthusStat == 1)
                        htmltext = "30637-01.htm"
                    else if (belthusStat == 2)
                        htmltext = "30637-08.htm"
                    else if (belthusStat == 3)
                    // in battle...
                        htmltext = "30637-09.htm"
                    else if (belthusStat == 4)
                    // haha... your summon lose
                        htmltext = "30637-05.htm"
                    else if (belthusStat == 5)
                        htmltext = "30637-06.htm"
                    else if (belthusStat == 6) {
                        htmltext = "30637-07.htm"
                        st["Belthus"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_3, -1)
                        st.giveItems(BELTHUS_ARCANA, 1)

                        if (st.hasQuestItems(
                                ALMORS_ARCANA,
                                CAMONIELL_ARCANA,
                                BASILLIA_ARCANA,
                                CELESTIEL_ARCANA,
                                BRYNTHEA_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (belthusStat == 7)
                        htmltext = "30637-10.htm"
                }

                BASILLA -> {
                    val basillaStat = st.getInt("Basilla")
                    if (basillaStat == 1)
                        htmltext = "30638-01.htm"
                    else if (basillaStat == 2)
                        htmltext = "30638-08.htm"
                    else if (basillaStat == 3)
                    // in battle...
                        htmltext = "30638-09.htm"
                    else if (basillaStat == 4)
                    // haha... your summon lose
                        htmltext = "30638-05.htm"
                    else if (basillaStat == 5)
                        htmltext = "30638-06.htm"
                    else if (basillaStat == 6) {
                        htmltext = "30638-07.htm"
                        st["Basilla"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_4, -1)
                        st.giveItems(BASILLIA_ARCANA, 1)

                        if (st.hasQuestItems(
                                ALMORS_ARCANA,
                                CAMONIELL_ARCANA,
                                BELTHUS_ARCANA,
                                CELESTIEL_ARCANA,
                                BRYNTHEA_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (basillaStat == 7)
                        htmltext = "30638-10.htm"
                }

                CELESTIEL -> {
                    val celestielStat = st.getInt("Celestiel")
                    if (celestielStat == 1)
                        htmltext = "30639-01.htm"
                    else if (celestielStat == 2)
                        htmltext = "30639-08.htm"
                    else if (celestielStat == 3)
                    // in battle...
                        htmltext = "30639-09.htm"
                    else if (celestielStat == 4)
                    // haha... your summon lose
                        htmltext = "30639-05.htm"
                    else if (celestielStat == 5)
                        htmltext = "30639-06.htm"
                    else if (celestielStat == 6) {
                        htmltext = "30639-07.htm"
                        st["Celestiel"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_5, -1)
                        st.giveItems(CELESTIEL_ARCANA, 1)

                        if (st.hasQuestItems(
                                ALMORS_ARCANA,
                                CAMONIELL_ARCANA,
                                BELTHUS_ARCANA,
                                BASILLIA_ARCANA,
                                BRYNTHEA_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (celestielStat == 7)
                        htmltext = "30639-10.htm"
                }

                BRYNTHEA -> {
                    val bryntheaStat = st.getInt("Brynthea")
                    if (bryntheaStat == 1)
                        htmltext = "30640-01.htm"
                    else if (bryntheaStat == 2)
                        htmltext = "30640-08.htm"
                    else if (bryntheaStat == 3)
                    // in battle...
                        htmltext = "30640-09.htm"
                    else if (bryntheaStat == 4)
                    // haha... your summon lose
                        htmltext = "30640-05.htm"
                    else if (bryntheaStat == 5)
                        htmltext = "30640-06.htm"
                    else if (bryntheaStat == 6) {
                        htmltext = "30640-07.htm"
                        st["Brynthea"] = "7"
                        st.takeItems(CRYSTAL_OF_VICTORY_6, -1)
                        st.giveItems(BRYNTHEA_ARCANA, 1)

                        if (st.hasQuestItems(
                                ALMORS_ARCANA,
                                CAMONIELL_ARCANA,
                                BELTHUS_ARCANA,
                                BASILLIA_ARCANA,
                                CELESTIEL_ARCANA
                            )
                        ) {
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (bryntheaStat == 7)
                        htmltext = "30640-10.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onDeath(killer: Creature, player: Player): String? {
        if (killer !is Attackable)
            return null

        val st = checkPlayerState(player, killer as Npc, Quest.STATE_STARTED) ?: return null

        when ((killer as Npc).npcId) {
            PAKO_THE_CAT -> if (st.getInt("Almors") == 3) {
                st["Almors"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_1, 1)
            }

            UNICORN_RACER -> if (st.getInt("Camoniell") == 3) {
                st["Camoniell"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_2, 1)
            }

            SHADOW_TUREN -> if (st.getInt("Belthus") == 3) {
                st["Belthus"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_3, 1)
            }

            MIMI_THE_CAT -> if (st.getInt("Basilla") == 3) {
                st["Basilla"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_4, 1)
            }

            UNICORN_PHANTASM -> if (st.getInt("Celestiel") == 3) {
                st["Celestiel"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_5, 1)
            }

            SILHOUETTE_TILFO -> if (st.getInt("Brynthea") == 3) {
                st["Brynthea"] = "4"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CRYSTAL_OF_DEFEAT_6, 1)
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        when (npcId) {
            GIANT_FUNGUS -> if (st.getInt("Lara") == 1)
                st.dropItems(SAC_OF_REDSPORES, 1, 30, 800000)

            LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER -> if (st.getInt("Lara") == 1)
                st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 250000)

            LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR -> if (st.getInt("Lara") == 1)
                st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 500000)

            LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD -> if (st.getInt("Lara") == 1)
                st.dropItems(LETO_LIZARDMAN_AMULET, 1, 30, 750000)

            MANASHEN_GARGOYLE -> if (st.getInt("Lara") == 2)
                st.dropItems(SHARDS_OF_MANASHEN, 1, 30, 800000)

            KARUL_BUGBEAR -> if (st.getInt("Lara") == 2)
                st.dropItems(KARUL_BUGBEAR_TOTEM, 1, 30, 800000)

            BREKA_ORC, BREKA_ORC_ARCHER, BREKA_ORC_WARRIOR -> if (st.getInt("Lara") == 3)
                st.dropItems(BREKA_ORC_TOTEM, 1, 30, 250000)

            BREKA_ORC_SHAMAN, BREKA_ORC_OVERLORD -> if (st.getInt("Lara") == 3)
                st.dropItems(BREKA_ORC_TOTEM, 1, 30, 500000)

            FETTERED_SOUL -> if (st.getInt("Lara") == 3)
                st.dropItems(CRIMSON_BLOODSTONE, 1, 30, 600000)

            WINDSUS -> if (st.getInt("Lara") == 4)
                st.dropItems(TUSK_OF_WINDSUS, 1, 30, 700000)

            TYRANT, TYRANT_KINGPIN -> if (st.getInt("Lara") == 4)
                st.dropItems(TALONS_OF_TYRANT, 1, 30, 500000)

            NOBLE_ANT, NOBLE_ANT_LEADER -> if (st.getInt("Lara") == 5)
                st.dropItems(WINGS_OF_DRONEANT, 1, 30, 600000)

            WYRM -> if (st.getInt("Lara") == 5)
                st.dropItems(FANGS_OF_WYRM, 1, 30, 500000)

            PAKO_THE_CAT -> if (st.getInt("Almors") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Almors"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_1, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_1, 1)
                npc.broadcastNpcSay("I'm sorry, Lord!")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }

            UNICORN_RACER -> if (st.getInt("Camoniell") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Camoniell"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_2, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_2, 1)
                npc.broadcastNpcSay("I LOSE")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }

            SHADOW_TUREN -> if (st.getInt("Belthus") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Belthus"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_3, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_3, 1)
                npc.broadcastNpcSay("Ugh! I lost...!")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }

            MIMI_THE_CAT -> if (st.getInt("Basilla") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Basilla"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_4, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_4, 1)
                npc.broadcastNpcSay("Lost! Sorry, Lord!")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }

            UNICORN_PHANTASM -> if (st.getInt("Celestiel") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Celestiel"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_5, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_5, 1)
                npc.broadcastNpcSay("I LOSE")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }

            SILHOUETTE_TILFO -> if (st.getInt("Brynthea") == 3 && _duelsInProgress.containsKey(npcId)) {
                st["Brynthea"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CRYSTAL_OF_INPROGRESS_6, -1)
                st.giveItems(CRYSTAL_OF_VICTORY_6, 1)
                npc.broadcastNpcSay("Ugh! Can this be happening?!")
                st.player.removeNotifyQuestOfDeath(st)
                _duelsInProgress.remove(npcId)
            }
        }

        return null
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        var st: QuestState? = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st!!.addNotifyOfDeath()

        val npcId = npc.npcId
        val isPet = attacker is Summon

        when (npcId) {
            PAKO_THE_CAT -> if (st.getInt("Almors") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Almors"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_1, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_1, 1)
                npc.broadcastNpcSay("Whhiisshh!")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Almors") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                // check if the attacker is the same pet as the one that attacked before.
                if (!isPet || attacker.pet !== duel?.pet)
                // if a foul occured find the player who had the duel in progress and give a foul crystal
                {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Almors"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_1, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_1, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_1, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("Rule violation!")
                            npc.doDie(npc)
                        }
                    }
                }
            }

            UNICORN_RACER -> if (st.getInt("Camoniell") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Camoniell"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_2, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_2, 1)
                npc.broadcastNpcSay("START DUEL")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Camoniell") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                if (!isPet || attacker.pet !== duel?.pet) {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Camoniell"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_2, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_2, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_2, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("RULE VIOLATION")
                            npc.doDie(npc)
                        }
                    }
                }
            }

            SHADOW_TUREN -> if (st.getInt("Belthus") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Belthus"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_3, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_3, 1)
                npc.broadcastNpcSay("So shall we start?!")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Belthus") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                if (!isPet || attacker.pet !== duel?.pet) {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Belthus"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_3, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_3, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_3, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("Rule violation!!!")
                            npc.doDie(npc)
                        }
                    }
                }
            }

            MIMI_THE_CAT -> if (st.getInt("Basilla") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Basilla"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_4, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_4, 1)
                npc.broadcastNpcSay("Whish! Fight!")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Basilla") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                if (!isPet || attacker.pet !== duel?.pet) {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Basilla"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_4, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_4, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_4, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("Rule violation!")
                            npc.doDie(npc)
                        }
                    }
                }
            }

            UNICORN_PHANTASM -> if (st.getInt("Celestiel") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Celestiel"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_5, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_5, 1)
                npc.broadcastNpcSay("START DUEL")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Celestiel") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                if (!isPet || attacker.pet !== duel?.pet) {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Celestiel"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_5, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_5, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_5, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("RULE VIOLATION")
                            npc.doDie(npc)
                        }
                    }
                }
            }

            SILHOUETTE_TILFO -> if (st.getInt("Brynthea") == 2 && isPet && npc.currentHp == npc.maxHp.toDouble()) {
                st["Brynthea"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CRYSTAL_OF_PROGRESS_6, -1)
                st.giveItems(CRYSTAL_OF_INPROGRESS_6, 1)
                npc.broadcastNpcSay("I'll walk all over you!")
                _duelsInProgress[npcId] = ProgressDuelMob(player, attacker.pet)
            } else if (st.getInt("Brynthea") == 3 && _duelsInProgress.containsKey(npcId)) {
                val duel = _duelsInProgress[npcId]
                if (!isPet || attacker.pet !== duel?.pet) {
                    val foulPlayer = duel?.attacker
                    if (foulPlayer != null) {
                        st = foulPlayer.getQuestState(qn)
                        if (st != null) {
                            st["Brynthea"] = "5"
                            st.takeItems(CRYSTAL_OF_PROGRESS_6, -1)
                            st.takeItems(CRYSTAL_OF_INPROGRESS_6, -1)
                            st.giveItems(CRYSTAL_OF_FOUL_6, 1)
                            st.player.removeNotifyQuestOfDeath(st)
                            npc.broadcastNpcSay("Rule violation!!!")
                            npc.doDie(npc)
                        }
                    }
                }
            }
        }

        return null
    }

    private inner class ProgressDuelMob(val attacker: Player?, val pet: Summon?)

    companion object {
        val qn = "Q230_TestOfTheSummoner"

        // Items
        private val LETO_LIZARDMAN_AMULET = 3337
        private val SAC_OF_REDSPORES = 3338
        private val KARUL_BUGBEAR_TOTEM = 3339
        private val SHARDS_OF_MANASHEN = 3340
        private val BREKA_ORC_TOTEM = 3341
        private val CRIMSON_BLOODSTONE = 3342
        private val TALONS_OF_TYRANT = 3343
        private val WINGS_OF_DRONEANT = 3344
        private val TUSK_OF_WINDSUS = 3345
        private val FANGS_OF_WYRM = 3346
        private val LARA_LIST_1 = 3347
        private val LARA_LIST_2 = 3348
        private val LARA_LIST_3 = 3349
        private val LARA_LIST_4 = 3350
        private val LARA_LIST_5 = 3351
        private val GALATEA_LETTER = 3352
        private val BEGINNER_ARCANA = 3353
        private val ALMORS_ARCANA = 3354
        private val CAMONIELL_ARCANA = 3355
        private val BELTHUS_ARCANA = 3356
        private val BASILLIA_ARCANA = 3357
        private val CELESTIEL_ARCANA = 3358
        private val BRYNTHEA_ARCANA = 3359
        private val CRYSTAL_OF_PROGRESS_1 = 3360
        private val CRYSTAL_OF_INPROGRESS_1 = 3361
        private val CRYSTAL_OF_FOUL_1 = 3362
        private val CRYSTAL_OF_DEFEAT_1 = 3363
        private val CRYSTAL_OF_VICTORY_1 = 3364
        private val CRYSTAL_OF_PROGRESS_2 = 3365
        private val CRYSTAL_OF_INPROGRESS_2 = 3366
        private val CRYSTAL_OF_FOUL_2 = 3367
        private val CRYSTAL_OF_DEFEAT_2 = 3368
        private val CRYSTAL_OF_VICTORY_2 = 3369
        private val CRYSTAL_OF_PROGRESS_3 = 3370
        private val CRYSTAL_OF_INPROGRESS_3 = 3371
        private val CRYSTAL_OF_FOUL_3 = 3372
        private val CRYSTAL_OF_DEFEAT_3 = 3373
        private val CRYSTAL_OF_VICTORY_3 = 3374
        private val CRYSTAL_OF_PROGRESS_4 = 3375
        private val CRYSTAL_OF_INPROGRESS_4 = 3376
        private val CRYSTAL_OF_FOUL_4 = 3377
        private val CRYSTAL_OF_DEFEAT_4 = 3378
        private val CRYSTAL_OF_VICTORY_4 = 3379
        private val CRYSTAL_OF_PROGRESS_5 = 3380
        private val CRYSTAL_OF_INPROGRESS_5 = 3381
        private val CRYSTAL_OF_FOUL_5 = 3382
        private val CRYSTAL_OF_DEFEAT_5 = 3383
        private val CRYSTAL_OF_VICTORY_5 = 3384
        private val CRYSTAL_OF_PROGRESS_6 = 3385
        private val CRYSTAL_OF_INPROGRESS_6 = 3386
        private val CRYSTAL_OF_FOUL_6 = 3387
        private val CRYSTAL_OF_DEFEAT_6 = 3388
        private val CRYSTAL_OF_VICTORY_6 = 3389

        // Rewards
        private val MARK_OF_SUMMONER = 3336
        private val DIMENSIONAL_DIAMOND = 7562

        // Npcs
        private val LARA = 30063
        private val GALATEA = 30634
        private val ALMORS = 30635
        private val CAMONIELL = 30636
        private val BELTHUS = 30637
        private val BASILLA = 30638
        private val CELESTIEL = 30639
        private val BRYNTHEA = 30640

        // Monsters
        private val NOBLE_ANT = 20089
        private val NOBLE_ANT_LEADER = 20090
        private val WYRM = 20176
        private val TYRANT = 20192
        private val TYRANT_KINGPIN = 20193
        private val BREKA_ORC = 20267
        private val BREKA_ORC_ARCHER = 20268
        private val BREKA_ORC_SHAMAN = 20269
        private val BREKA_ORC_OVERLORD = 20270
        private val BREKA_ORC_WARRIOR = 20271
        private val FETTERED_SOUL = 20552
        private val WINDSUS = 20553
        private val GIANT_FUNGUS = 20555
        private val MANASHEN_GARGOYLE = 20563
        private val LETO_LIZARDMAN = 20577
        private val LETO_LIZARDMAN_ARCHER = 20578
        private val LETO_LIZARDMAN_SOLDIER = 20579
        private val LETO_LIZARDMAN_WARRIOR = 20580
        private val LETO_LIZARDMAN_SHAMAN = 20581
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val KARUL_BUGBEAR = 20600

        // Quest Monsters
        private val PAKO_THE_CAT = 27102
        private val UNICORN_RACER = 27103
        private val SHADOW_TUREN = 27104
        private val MIMI_THE_CAT = 27105
        private val UNICORN_PHANTASM = 27106
        private val SILHOUETTE_TILFO = 27107

        private val LARA_LISTS = arrayOf(
            intArrayOf(LARA_LIST_1, SAC_OF_REDSPORES, LETO_LIZARDMAN_AMULET),
            intArrayOf(LARA_LIST_2, KARUL_BUGBEAR_TOTEM, SHARDS_OF_MANASHEN),
            intArrayOf(LARA_LIST_3, CRIMSON_BLOODSTONE, BREKA_ORC_TOTEM),
            intArrayOf(LARA_LIST_4, TUSK_OF_WINDSUS, TALONS_OF_TYRANT),
            intArrayOf(LARA_LIST_5, WINGS_OF_DRONEANT, FANGS_OF_WYRM)
        )

        private val _duelsInProgress = ConcurrentHashMap<Int, ProgressDuelMob>()
    }
}