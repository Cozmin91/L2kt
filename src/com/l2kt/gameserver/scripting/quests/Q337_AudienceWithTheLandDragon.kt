package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q337_AudienceWithTheLandDragon : Quest(337, "Audience with the Land Dragon") {
    init {

        setItemsIds(
            FEATHER_OF_GABRIELLE,
            MARK_OF_WATCHMAN,
            REMAINS_OF_SACRIFIED,
            TOTEM_OF_LAND_DRAGON,
            KRANROT_SKIN,
            HAMRUT_LEG,
            MARSH_DRAKE_TALONS,
            MARSH_STALKER_HORN,
            FIRST_FRAGMENT_OF_ABYSS_JEWEL,
            MARA_FANG,
            SECOND_FRAGMENT_OF_ABYSS_JEWEL,
            MUSFEL_FANG,
            HERALD_OF_SLAYER,
            THIRD_FRAGMENT_OF_ABYSS_JEWEL
        )

        addStartNpc(GABRIELLE)
        addTalkId(GABRIELLE, ORVEN, KENDRA, CHAKIRIS, KAIENA, MOKE, HELTON, GILMORE, THEODRIC)

        addAttackId(ABYSSAL_JEWEL_1, ABYSSAL_JEWEL_2, ABYSSAL_JEWEL_3)
        addKillId(
            BLOOD_QUEEN,
            SACRIFICE_OF_THE_SACRIFICED,
            HARIT_LIZARDMAN_SHAMAN,
            HARIT_LIZARDMAN_MATRIARCH,
            HARIT_LIZARDMAN_ZEALOT,
            KRANROT,
            HAMRUT,
            MARSH_DRAKE,
            MARSH_STALKER,
            JEWEL_GUARDIAN_MARA,
            JEWEL_GUARDIAN_MUSFEL,
            CAVE_MAIDEN_1,
            CAVE_MAIDEN_2,
            CAVE_KEEPER_1,
            CAVE_KEEPER_2,
            JEWEL_GUARDIAN_PYTON
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext: String? = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Gabrielle
        if (event.equals("30753-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["drop1"] = "1"
            st["drop2"] = "1"
            st["drop3"] = "1"
            st["drop4"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(FEATHER_OF_GABRIELLE, 1)
        } else if (event.equals("30753-09.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(MARK_OF_WATCHMAN) >= 4) {
                st["cond"] = "2"
                st["drop5"] = "2"
                st["drop6"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(MARK_OF_WATCHMAN, 4)
            } else
                htmltext = null
        } else if (event.equals("30755-05.htm", ignoreCase = true)) {
            if (st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL)) {
                st.takeItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL, 1)
                st.takeItems(HERALD_OF_SLAYER, 1)
                st.giveItems(PORTAL_STONE, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = null
        }// Theodric

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 50) "30753-02.htm" else "30753-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GABRIELLE -> if (cond == 1)
                        htmltext = if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4) "30753-06.htm" else "30753-08.htm"
                    else if (cond == 2) {
                        if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
                            htmltext = "30753-10.htm"
                        else {
                            htmltext = "30753-11.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(FEATHER_OF_GABRIELLE, 1)
                            st.takeItems(MARK_OF_WATCHMAN, 1)
                            st.giveItems(HERALD_OF_SLAYER, 1)
                        }
                    } else if (cond == 3)
                        htmltext = "30753-12.htm"
                    else if (cond == 4)
                        htmltext = "30753-13.htm"

                    ORVEN -> if (cond == 1) {
                        if (st.getInt("drop1") == 1) {
                            if (st.hasQuestItems(REMAINS_OF_SACRIFIED)) {
                                htmltext = "30857-02.htm"
                                st.unset("drop1")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(REMAINS_OF_SACRIFIED, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30857-01.htm"
                        } else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
                            htmltext = "30857-03.htm"
                        else
                            htmltext = "30857-04.htm"
                    }

                    KENDRA -> if (cond == 1) {
                        if (st.getInt("drop2") == 1) {
                            if (st.hasQuestItems(TOTEM_OF_LAND_DRAGON)) {
                                htmltext = "30851-02.htm"
                                st.unset("drop2")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(TOTEM_OF_LAND_DRAGON, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30851-01.htm"
                        } else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
                            htmltext = "30851-03.htm"
                        else
                            htmltext = "30851-04.htm"
                    }

                    CHAKIRIS -> if (cond == 1) {
                        if (st.getInt("drop3") == 1) {
                            if (st.hasQuestItems(KRANROT_SKIN, HAMRUT_LEG)) {
                                htmltext = "30705-02.htm"
                                st.unset("drop3")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(KRANROT_SKIN, 1)
                                st.takeItems(HAMRUT_LEG, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30705-01.htm"
                        } else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
                            htmltext = "30705-03.htm"
                        else
                            htmltext = "30705-04.htm"
                    }

                    KAIENA -> if (cond == 1) {
                        if (st.getInt("drop4") == 1) {
                            if (st.hasQuestItems(MARSH_DRAKE_TALONS, MARSH_STALKER_HORN)) {
                                htmltext = "30720-02.htm"
                                st.unset("drop4")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(MARSH_DRAKE_TALONS, 1)
                                st.takeItems(MARSH_STALKER_HORN, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30720-01.htm"
                        } else if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 4)
                            htmltext = "30720-03.htm"
                        else
                            htmltext = "30720-04.htm"
                    }

                    MOKE -> if (cond == 2) {
                        when (st.getInt("drop5")) {
                            2 -> {
                                htmltext = "30498-01.htm"
                                st["drop5"] = "1"
                            }

                            1 -> if (st.hasQuestItems(FIRST_FRAGMENT_OF_ABYSS_JEWEL, MARA_FANG)) {
                                htmltext = "30498-03.htm"
                                st.unset("drop5")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(FIRST_FRAGMENT_OF_ABYSS_JEWEL, 1)
                                st.takeItems(MARA_FANG, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30498-02.htm"

                            0 -> if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
                                htmltext = "30498-04.htm"
                            else
                                htmltext = "30498-05.htm"
                        }
                    }

                    HELTON -> if (cond == 2) {
                        when (st.getInt("drop6")) {
                            2 -> {
                                htmltext = "30678-01.htm"
                                st["drop6"] = "1"
                            }

                            1 -> if (st.hasQuestItems(SECOND_FRAGMENT_OF_ABYSS_JEWEL, MUSFEL_FANG)) {
                                htmltext = "30678-03.htm"
                                st.unset("drop6")
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(SECOND_FRAGMENT_OF_ABYSS_JEWEL, 1)
                                st.takeItems(MUSFEL_FANG, 1)
                                st.giveItems(MARK_OF_WATCHMAN, 1)
                            } else
                                htmltext = "30678-02.htm"

                            0 -> if (st.getQuestItemsCount(MARK_OF_WATCHMAN) < 2)
                                htmltext = "30678-04.htm"
                            else
                                htmltext = "30678-05.htm"
                        }
                    }

                    GILMORE -> if (cond == 1 || cond == 2)
                        htmltext = "30754-01.htm"
                    else if (cond == 3) {
                        htmltext = "30754-02.htm"
                        st["cond"] = "4"
                        st["drop7"] = "1"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 4)
                        htmltext =
                                if (!st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL)) "30754-04.htm" else "30754-05.htm"

                    THEODRIC -> if (cond == 1 || cond == 2)
                        htmltext = "30755-01.htm"
                    else if (cond == 3)
                        htmltext = "30755-02.htm"
                    else if (cond == 4)
                        htmltext =
                                if (!st.hasQuestItems(THIRD_FRAGMENT_OF_ABYSS_JEWEL)) "30755-03.htm" else "30755-04.htm"
                }
            }
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        for (npcInfo in DROP_ON_ATTACK) {
            if (npcInfo[0] != npcId)
                continue

            if (npcInfo[1] != st.getInt("cond"))
                break

            val percentHp = (npc.currentHp + damage) * 100 / npc.maxHp

            // reward jewel fragment
            if (percentHp < 33) {
                if (Rnd[100] < 33 && st.getInt("drop" + npcInfo[2]) == 1) {
                    val itemId = npcInfo[3]
                    if (!st.hasQuestItems(itemId)) {
                        st.giveItems(itemId, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }
                }
            } else if (percentHp < 66) {
                if (Rnd[100] < 33 && st.getInt("drop" + npcInfo[2]) == 1) {
                    val spawn: Boolean
                    if (npcId == ABYSSAL_JEWEL_3)
                        spawn = _jewel3
                    else if (npcId == ABYSSAL_JEWEL_2)
                        spawn = _jewel2
                    else
                        spawn = _jewel1

                    if (spawn) {
                        for (i in 0 until npcInfo[4]) {
                            val mob = addSpawn(
                                npcInfo[5],
                                npc.x + Rnd[-150, 150],
                                npc.y + Rnd[-150, 150],
                                npc.z,
                                npc.heading,
                                true,
                                60000,
                                false
                            )
                            mob!!.setRunning()
                            (mob as Attackable).addDamageHate(attacker, 0, 500)
                            mob.ai.setIntention(CtrlIntention.ATTACK, attacker)
                        }

                        if (npcId == ABYSSAL_JEWEL_3)
                            _jewel3 = false
                        else if (npcId == ABYSSAL_JEWEL_2)
                            _jewel2 = false
                        else
                            _jewel1 = false
                    }
                }

            } else if (percentHp > 90) {
                if (npcId == ABYSSAL_JEWEL_3)
                    _jewel3 = true
                else if (npcId == ABYSSAL_JEWEL_2)
                    _jewel2 = true
                else
                    _jewel1 = true
            }// reset spawned if npc regenerated to 90% HP and more
            // spawn monsters and register spawned
            break
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        val npcId = npc.npcId

        when (npcId) {
            SACRIFICE_OF_THE_SACRIFICED // Orven's request
                , HARIT_LIZARDMAN_ZEALOT // Kendra's request
                , KRANROT// Chakiris's request
                , HAMRUT, MARSH_DRAKE// Kaiena's request
                , MARSH_STALKER, JEWEL_GUARDIAN_MARA// Moke's request
                , JEWEL_GUARDIAN_MUSFEL// Helton's request
            -> for (npcInfo in DROPS_ON_KILL) {
                if (npcInfo[0] != npcId)
                    continue

                if (npcInfo[1] == cond && st.getInt("drop" + npcInfo[2]) == 1) {
                    val itemId = npcInfo[3]
                    if (!st.hasQuestItems(itemId)) {
                        st.giveItems(itemId, 1)
                        st.playSound(QuestState.SOUND_ITEMGET)
                    }
                }
                break
            }

            BLOOD_QUEEN// Orven's request
            -> if (cond == 1 && st.getInt("drop1") == 1 && !st.hasQuestItems(REMAINS_OF_SACRIFIED)) {
                for (i in 0..7)
                    addSpawn(
                        SACRIFICE_OF_THE_SACRIFICED,
                        npc.x + Rnd[-100, 100],
                        npc.y + Rnd[-100, 100],
                        npc.z,
                        npc.heading,
                        true,
                        60000,
                        false
                    )
            }

            HARIT_LIZARDMAN_SHAMAN// Kendra's request
                , HARIT_LIZARDMAN_MATRIARCH -> if (cond == 1 && Rnd[5] == 0 && st.getInt("drop2") == 1 && !st.hasQuestItems(
                    TOTEM_OF_LAND_DRAGON
                )
            ) {
                for (i in 0..2)
                    addSpawn(
                        HARIT_LIZARDMAN_ZEALOT,
                        npc.x + Rnd[-50, 50],
                        npc.y + Rnd[-50, 50],
                        npc.z,
                        npc.heading,
                        true,
                        60000,
                        false
                    )
            }

            CAVE_MAIDEN_1// Gilmore's request
                , CAVE_MAIDEN_2, CAVE_KEEPER_1, CAVE_KEEPER_2 -> if (cond == 4 && Rnd[5] == 0 && !st.hasQuestItems(
                    THIRD_FRAGMENT_OF_ABYSS_JEWEL
                )
            )
                addSpawn(
                    ABYSSAL_JEWEL_3,
                    npc.x + Rnd[-50, 50],
                    npc.y + Rnd[-50, 50],
                    npc.z,
                    npc.heading,
                    true,
                    60000,
                    false
                )
        }

        return null
    }

    companion object {
        private val qn = "Q337_AudienceWithTheLandDragon"

        // Variables
        private var _jewel1 = false
        private var _jewel2 = false
        private var _jewel3 = false

        // NPCs
        private val GABRIELLE = 30753
        private val ORVEN = 30857 // 1
        private val KENDRA = 30851 // 2
        private val CHAKIRIS = 30705 // 3
        private val KAIENA = 30720 // 4
        private val MOKE = 30498 // 1st abyssal
        private val HELTON = 30678 // 2nd abyssal
        private val GILMORE = 30754 // 3rd abyssal
        private val THEODRIC = 30755

        // Mobs
        private val BLOOD_QUEEN = 18001 // 1
        private val SACRIFICE_OF_THE_SACRIFICED = 27171 // 1
        private val HARIT_LIZARDMAN_SHAMAN = 20644 // 2
        private val HARIT_LIZARDMAN_MATRIARCH = 20645 // 2
        private val HARIT_LIZARDMAN_ZEALOT = 27172 // 2
        private val KRANROT = 20650 // 3
        private val HAMRUT = 20649 // 3
        private val MARSH_DRAKE = 20680 // 4
        private val MARSH_STALKER = 20679 // 4
        private val ABYSSAL_JEWEL_1 = 27165 // 1st abyssal
        private val JEWEL_GUARDIAN_MARA = 27168
        private val ABYSSAL_JEWEL_2 = 27166 // 2nd abyssal
        private val JEWEL_GUARDIAN_MUSFEL = 27169
        private val CAVE_MAIDEN_1 = 20134 // 3rd abyssal
        private val CAVE_MAIDEN_2 = 20287
        private val CAVE_KEEPER_1 = 20246
        private val CAVE_KEEPER_2 = 20277
        private val ABYSSAL_JEWEL_3 = 27167
        private val JEWEL_GUARDIAN_PYTON = 27170

        // Items
        private val FEATHER_OF_GABRIELLE = 3852
        private val MARK_OF_WATCHMAN = 3864
        private val REMAINS_OF_SACRIFIED = 3857 // 1
        private val TOTEM_OF_LAND_DRAGON = 3858 // 2
        private val KRANROT_SKIN = 3855 // 3
        private val HAMRUT_LEG = 3856 // 3
        private val MARSH_DRAKE_TALONS = 3854 // 4
        private val MARSH_STALKER_HORN = 3853 // 4
        private val FIRST_FRAGMENT_OF_ABYSS_JEWEL = 3859 // 1st abyssal
        private val MARA_FANG = 3862
        private val SECOND_FRAGMENT_OF_ABYSS_JEWEL = 3860 // 2nd abyssal
        private val MUSFEL_FANG = 3863
        private val HERALD_OF_SLAYER = 3890
        private val THIRD_FRAGMENT_OF_ABYSS_JEWEL = 3861 // 3rd abyssal
        private val PORTAL_STONE = 3865

        /**
         * 0..npcId, 1..cond, 2..cond2, 3..chance, 4..itemId
         */
        private val DROPS_ON_KILL = arrayOf(
            intArrayOf(SACRIFICE_OF_THE_SACRIFICED, 1, 1, REMAINS_OF_SACRIFIED),
            intArrayOf(HARIT_LIZARDMAN_ZEALOT, 1, 2, TOTEM_OF_LAND_DRAGON),
            intArrayOf(KRANROT, 1, 3, KRANROT_SKIN),
            intArrayOf(HAMRUT, 1, 3, HAMRUT_LEG),
            intArrayOf(MARSH_DRAKE, 1, 4, MARSH_DRAKE_TALONS),
            intArrayOf(MARSH_STALKER, 1, 4, MARSH_STALKER_HORN),
            intArrayOf(JEWEL_GUARDIAN_MARA, 2, 5, MARA_FANG),
            intArrayOf(JEWEL_GUARDIAN_MUSFEL, 2, 6, MUSFEL_FANG)
        )

        /**
         * 0..npcId, 1..cond, 2..cond2, 3..itemId, 4..amount of mobs, 5..mob
         */
        private val DROP_ON_ATTACK = arrayOf(
            intArrayOf(ABYSSAL_JEWEL_1, 2, 5, FIRST_FRAGMENT_OF_ABYSS_JEWEL, 20, JEWEL_GUARDIAN_MARA),
            intArrayOf(ABYSSAL_JEWEL_2, 2, 6, SECOND_FRAGMENT_OF_ABYSS_JEWEL, 20, JEWEL_GUARDIAN_MUSFEL),
            intArrayOf(ABYSSAL_JEWEL_3, 4, 7, THIRD_FRAGMENT_OF_ABYSS_JEWEL, 3, JEWEL_GUARDIAN_PYTON)
        )
    }
}