package com.l2kt.gameserver.scripting.scripts.village_master

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.HennaInfo
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

/**
 * @author fernandopm
 */
class SecondClassChange : Quest(-1, "village_master") {
    init {
        // Dark Elfs
        Classes["SK"] = intArrayOf(33, 32, 2, 26, 27, 28, 29, MARK_OF_DUTY, MARK_OF_FATE, MARK_OF_WITCHCRAFT, 56)
        Classes["BD"] = intArrayOf(34, 32, 2, 30, 31, 32, 33, MARK_OF_CHALLENGER, MARK_OF_FATE, MARK_OF_DUELIST, 56)
        Classes["SE"] = intArrayOf(43, 42, 2, 34, 35, 36, 37, MARK_OF_PILGRIM, MARK_OF_FATE, MARK_OF_REFORMER, 56)
        Classes["AW"] = intArrayOf(36, 35, 2, 38, 39, 40, 41, MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SEARCHER, 56)
        Classes["PR"] = intArrayOf(37, 35, 2, 42, 43, 44, 45, MARK_OF_SEEKER, MARK_OF_FATE, MARK_OF_SAGITTARIUS, 56)
        Classes["SH"] = intArrayOf(40, 39, 2, 46, 47, 48, 49, MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_MAGUS, 56)
        Classes["PS"] = intArrayOf(41, 39, 2, 50, 51, 52, 53, MARK_OF_SCHOLAR, MARK_OF_FATE, MARK_OF_SUMMONER, 56)
        // Orcs
        Classes["TY"] = intArrayOf(48, 47, 3, 16, 17, 18, 19, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_DUELIST, 34)
        Classes["DE"] = intArrayOf(46, 45, 3, 20, 21, 22, 23, MARK_OF_CHALLENGER, MARK_OF_GLORY, MARK_OF_CHAMPION, 34)
        Classes["OL"] = intArrayOf(51, 50, 3, 24, 25, 26, 27, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_LORD, 34)
        Classes["WC"] = intArrayOf(52, 50, 3, 28, 29, 30, 31, MARK_OF_PILGRIM, MARK_OF_GLORY, MARK_OF_WARSPIRIT, 34)
        // Dwarf
        Classes["BH"] = intArrayOf(
            55, 54, 4, 109, // i can't use 09 so i put 109 :P
            10, 11, 12, MARK_OF_GUILDSMAN, MARK_OF_PROSPERITY, MARK_OF_SEARCHER, 15
        )
        Classes["WS"] =
                intArrayOf(57, 56, 4, 16, 17, 18, 19, MARK_OF_GUILDSMAN, MARK_OF_PROSPERITY, MARK_OF_MAESTRO, 22)
        // Human & Elfs Fighters
        Classes["TK"] = intArrayOf(20, 19, 1, 36, 37, 38, 39, MARK_OF_DUTY, MARK_OF_LIFE, MARK_OF_HEALER, 78)
        Classes["SS"] = intArrayOf(21, 19, 1, 40, 41, 42, 43, MARK_OF_CHALLENGER, MARK_OF_LIFE, MARK_OF_DUELIST, 78)
        Classes["PL"] = intArrayOf(5, 4, 0, 44, 45, 46, 47, MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_HEALER, 78)
        Classes["DA"] = intArrayOf(6, 4, 0, 48, 49, 50, 51, MARK_OF_DUTY, MARK_OF_TRUST, MARK_OF_WITCHCRAFT, 78)
        Classes["TH"] = intArrayOf(8, 7, 0, 52, 53, 54, 55, MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SEARCHER, 78)
        Classes["HE"] = intArrayOf(9, 7, 0, 56, 57, 58, 59, MARK_OF_SEEKER, MARK_OF_TRUST, MARK_OF_SAGITTARIUS, 78)
        Classes["PW"] = intArrayOf(23, 22, 1, 60, 61, 62, 63, MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SEARCHER, 78)
        Classes["SR"] = intArrayOf(24, 22, 1, 64, 65, 66, 67, MARK_OF_SEEKER, MARK_OF_LIFE, MARK_OF_SAGITTARIUS, 78)
        Classes["GL"] = intArrayOf(2, 1, 0, 68, 69, 70, 71, MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_DUELIST, 78)
        Classes["WL"] = intArrayOf(3, 1, 0, 72, 73, 74, 75, MARK_OF_CHALLENGER, MARK_OF_TRUST, MARK_OF_CHAMPION, 78)
        // Human & Elfs Mages (nukers)
        Classes["EW"] = intArrayOf(27, 26, 1, 18, 19, 20, 21, MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_MAGUS, 40)
        Classes["ES"] = intArrayOf(28, 26, 1, 22, 23, 24, 25, MARK_OF_SCHOLAR, MARK_OF_LIFE, MARK_OF_SUMMONER, 40)
        Classes["HS"] = intArrayOf(12, 11, 0, 26, 27, 28, 29, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_MAGUS, 40)
        Classes["HN"] = intArrayOf(13, 11, 0, 30, 31, 32, 33, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_WITCHCRAFT, 40)
        Classes["HW"] = intArrayOf(14, 11, 0, 34, 35, 36, 37, MARK_OF_SCHOLAR, MARK_OF_TRUST, MARK_OF_SUMMONER, 40)
        // Human & Elfs Mages (buffers)
        Classes["BI"] = intArrayOf(16, 15, 0, 16, 17, 18, 19, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_HEALER, 26)
        Classes["PH"] = intArrayOf(17, 15, 0, 20, 21, 22, 23, MARK_OF_PILGRIM, MARK_OF_TRUST, MARK_OF_REFORMER, 26)
        Classes["EE"] = intArrayOf(30, 29, 1, 12, 13, 14, 15, MARK_OF_PILGRIM, MARK_OF_LIFE, MARK_OF_HEALER, 26)
    }

    init {

        addStartNpc(*SECONDCLASSNPCS)
        addTalkId(*SECONDCLASSNPCS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        var suffix = ""
        if (Classes.containsKey(event)) {
            // 0 = newClass, 1 = reqClass, 2 = reqRace, 3 = no/no, 4 = no/ok, 5 = ok/no, 6 = ok/ok, 7,8,9 = Required Items 10 = denied class
            val array = Classes[event]!!
            if (player.classId.id == array[1] && player.race.ordinal == array[2]) {
                if (player.level < 40)
                    suffix = "-" + (if (st.hasQuestItems(array[7], array[8], array[9])) array[4] else array[3])
                else {
                    if (st.hasQuestItems(array[7], array[8], array[9])) {
                        st.playSound(QuestState.SOUND_FANFARE)
                        st.takeItems(array[7], -1)
                        st.takeItems(array[8], -1)
                        st.takeItems(array[9], -1)
                        player.setClassId(array[0])
                        player.baseClass = array[0]
                        player.sendPacket(HennaInfo(player))
                        player.broadcastUserInfo()
                        suffix = "-" + array[6]
                    } else
                        suffix = "-" + array[5]
                }

                htmltext = getClassHtml(player) + suffix + ".htm"
                st.exitQuest(true)
            } else
                htmltext = getClassHtml(player) + "-" + array[10] + ".htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        if (player.isSubClassActive) {
            st.exitQuest(true)
            return htmltext
        }

        when (npc.npcId) {
            31328 // Dark Elfs
                , 30195, 30699, 30474, 31324, 30862, 30910, 31285, 31331, 31334, 31974, 32096 -> if (player.race == ClassRace.DARK_ELF) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.PALUS_KNIGHT)
                        htmltext = "master_de-01.htm"
                    else if (player.classId == ClassId.SHILLIEN_ORACLE)
                        htmltext = "master_de-08.htm"
                    else if (player.classId == ClassId.ASSASSIN)
                        htmltext = "master_de-12.htm"
                    else if (player.classId == ClassId.DARK_WIZARD)
                        htmltext = "master_de-19.htm"
                } else
                    htmltext = if (player.classId.level() == 0) "master_de-55.htm" else "master_de-54.htm"
            } else
                htmltext = "master_de-56.htm"

            30513 // Orcs
                , 30681, 30704, 30865, 30913, 31288, 31326, 31977 -> if (player.race == ClassRace.ORC) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.MONK)
                        htmltext = "master_orc-01.htm"
                    else if (player.classId == ClassId.ORC_RAIDER)
                        htmltext = "master_orc-05.htm"
                    else if (player.classId == ClassId.ORC_SHAMAN)
                        htmltext = "master_orc-09.htm"
                } else
                    htmltext = if (player.classId.level() == 0) "master_orc-33.htm" else "master_orc-32.htm"
            } else
                htmltext = "master_orc-34.htm"

            30511 // Dwarf for Bounty Hunter
                , 30676, 30685, 30845, 30894, 31269, 31314, 31958 -> if (player.race == ClassRace.DWARF) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.SCAVENGER)
                        htmltext = "master_dwarf-01.htm"
                    else if (player.classId == ClassId.ARTISAN)
                        htmltext = "master_dwarf-15.htm"
                } else
                    htmltext = if (player.classId.level() == 0) "master_dwarf-13.htm" else "master_dwarf-14.htm"
            } else
                htmltext = "master_dwarf-15.htm"

            30512 // Dwarf for Warsmith
                , 30677, 30687, 30847, 30897, 31272, 31317, 31961 -> if (player.race == ClassRace.DWARF) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.SCAVENGER)
                        htmltext = "master_dwarf-22.htm"
                    else if (player.classId == ClassId.ARTISAN)
                        htmltext = "master_dwarf-05.htm"
                } else
                    htmltext = if (player.classId.level() == 0) "master_dwarf-20.htm" else "master_dwarf-21.htm"
            } else
                htmltext = "master_dwarf-22.htm"

            30109 // Human & Elfs Fighters
                , 30187, 30689, 30849, 30900, 31965, 32094 -> if (player.race == ClassRace.HUMAN || player.race == ClassRace.ELF) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.ELVEN_KNIGHT)
                        htmltext = "master_human_elf_fighter-01.htm"
                    else if (player.classId == ClassId.KNIGHT)
                        htmltext = "master_human_elf_fighter-08.htm"
                    else if (player.classId == ClassId.ROGUE)
                        htmltext = "master_human_elf_fighter-15.htm"
                    else if (player.classId == ClassId.ELVEN_SCOUT)
                        htmltext = "master_human_elf_fighter-22.htm"
                    else if (player.classId == ClassId.WARRIOR)
                        htmltext = "master_human_elf_fighter-29.htm"
                    else
                        htmltext = "master_human_elf_fighter-78.htm"
                } else
                    htmltext =
                            if (player.classId.level() == 0) "master_human_elf_fighter-76.htm" else "master_human_elf_fighter-77.htm"
            } else
                htmltext = "master_human_elf_fighter-78.htm"

            30115 // Human & Elfs Mages (nukers)
                , 30174, 30176, 30694, 30854, 31996 -> if (player.race == ClassRace.ELF || player.race == ClassRace.HUMAN) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.ELVEN_WIZARD)
                        htmltext = "master_human_elf_mystic-01.htm"
                    else if (player.classId == ClassId.HUMAN_WIZARD)
                        htmltext = "master_human_elf_mystic-08.htm"
                    else
                        htmltext = "master_human_elf_mystic-40.htm"
                } else
                    htmltext =
                            if (player.classId.level() == 0) "master_human_elf_mystic-38.htm" else "master_human_elf_mystic-39.htm"
            } else
                htmltext = "master_human_elf_mystic-40.htm"

            30120 // Human & Elfs Mages (buffers)
                , 30191, 30857, 30905, 31276, 31321, 31279, 31755, 31968, 32095, 31336 -> if (player.race == ClassRace.HUMAN || player.race == ClassRace.ELF) {
                if (player.classId.level() == 1) {
                    if (player.classId == ClassId.ELVEN_ORACLE)
                        htmltext = "master_human_elf_buffer-01.htm"
                    else if (player.classId == ClassId.CLERIC)
                        htmltext = "master_human_elf_buffer-05.htm"
                    else
                        htmltext = "master_human_elf_buffer-26.htm"
                } else
                    htmltext =
                            if (player.classId.level() == 0) "master_human_elf_buffer-24.htm" else "master_human_elf_buffer-25.htm"
            } else
                htmltext = "master_human_elf_buffer-26.htm"
        }
        st.exitQuest(true)

        return htmltext
    }

    companion object {
        private const val qn = "SecondClassChange"

        // 2nd class change items
        private const val MARK_OF_CHALLENGER = 2627
        private const val MARK_OF_DUTY = 2633
        private const val MARK_OF_SEEKER = 2673
        private const val MARK_OF_SCHOLAR = 2674
        private const val MARK_OF_PILGRIM = 2721
        private const val MARK_OF_DUELIST = 2762
        private const val MARK_OF_SEARCHER = 2809
        private const val MARK_OF_REFORMER = 2821
        private const val MARK_OF_MAGUS = 2840
        private const val MARK_OF_FATE = 3172
        private const val MARK_OF_SAGITTARIUS = 3293
        private const val MARK_OF_WITCHCRAFT = 3307
        private const val MARK_OF_SUMMONER = 3336
        private const val MARK_OF_WARSPIRIT = 2879
        private const val MARK_OF_GLORY = 3203
        private const val MARK_OF_CHAMPION = 3276
        private const val MARK_OF_LORD = 3390
        private const val MARK_OF_GUILDSMAN = 3119
        private const val MARK_OF_PROSPERITY = 3238
        private const val MARK_OF_MAESTRO = 2867
        private const val MARK_OF_TRUST = 2734
        private const val MARK_OF_HEALER = 2820
        private const val MARK_OF_LIFE = 3140

        private val Classes = HashMap<String, IntArray>()

        val SECONDCLASSNPCS = intArrayOf(
            // Dark Elfs
            31328,
            30195,
            30699,
            30474,
            31324,
            30862,
            30910,
            31285,
            31331,
            31334,
            31974,
            32096,
            // Orcs
            30513,
            30681,
            30704,
            30865,
            30913,
            31288,
            31326,
            31977,
            // Dwarf
            30511,
            30676,
            30685,
            30845,
            30894,
            31269,
            31314,
            31958,
            30512,
            30677,
            30687,
            30847,
            30897,
            31272,
            31317,
            31961,
            // Human & Elfs Fighters
            30109,
            30187,
            30689,
            30849,
            30900,
            31965,
            32094,
            // Human & Elfs Mages (nukers)
            30115,
            30174,
            30176,
            30694,
            30854,
            31996,
            // Human & Elfs Mages (buffers)
            30120,
            30191,
            30857,
            30905,
            31276,
            31321,
            31279,
            31755,
            31968,
            32095,
            31336
        )

        /**
         * @param player : The player to make checks on.
         * @return a String corresponding to html directory.
         */
        private fun getClassHtml(player: Player): String {
            val change = when (player.race) {
                ClassRace.DARK_ELF -> "master_de"

                ClassRace.DWARF -> "master_dwarf"

                ClassRace.ORC -> "master_orc"

                ClassRace.HUMAN, ClassRace.ELF -> if (player.isMageClass)
                    if (player.classId == ClassId.HUMAN_WIZARD || player.classId == ClassId.ELVEN_WIZARD) "master_human_elf_mystic" else "master_human_elf_buffer"
                else
                    "master_human_elf_fighter"
            }

            return change
        }
    }
}