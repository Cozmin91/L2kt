package com.l2kt.gameserver.scripting.scripts.village_master

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.HennaInfo
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

/**
 * @author fernandopm
 */
class FirstClassChange : Quest(-1, "village_master") {
    init {
        // Dark Elf
        Classes["PK"] = intArrayOf(32, 31, 2, 15, 16, 17, 18, GAZE_OF_ABYSS, 33)
        Classes["AS"] = intArrayOf(35, 31, 2, 19, 20, 21, 22, IRON_HEART, 33)
        Classes["DW"] = intArrayOf(39, 38, 2, 23, 24, 25, 26, JEWEL_OF_DARKNESS, 33)
        Classes["SO"] = intArrayOf(42, 38, 2, 27, 28, 29, 30, ORB_OF_ABYSS, 33)

        // Orc
        Classes["OR"] = intArrayOf(45, 44, 3, 9, 10, 11, 12, MARK_OF_RAIDER, 23)
        Classes["OM"] = intArrayOf(47, 44, 3, 13, 14, 15, 16, KHAVATARI_TOTEM, 23)
        Classes["OS"] = intArrayOf(50, 49, 3, 17, 18, 19, 20, MASK_OF_MEDIUM, 23)

        // Dwarf
        Classes["SC"] = intArrayOf(54, 53, 4, 5, 6, 7, 8, SCAV_MARKS, 11)
        Classes["AR"] = intArrayOf(56, 53, 4, 5, 6, 7, 8, ARTI_MARKS, 11)

        // Light Elf
        Classes["EK"] = intArrayOf(19, 18, 1, 18, 19, 20, 21, ELVEN_KNIGHT_BROOCH, 40)
        Classes["ES"] = intArrayOf(22, 18, 1, 22, 23, 24, 25, REORIA_RECOMMENDATION, 40)
        Classes["EW"] = intArrayOf(26, 25, 1, 15, 16, 17, 18, ETERNITY_DIAMOND, 33)
        Classes["EO"] = intArrayOf(29, 25, 1, 19, 20, 21, 22, LEAF_OF_ORACLE, 33)

        // Human
        Classes["HW"] = intArrayOf(1, 0, 0, 26, 27, 28, 29, MEDALLION_OF_WARRIOR, 40)
        Classes["HK"] = intArrayOf(4, 0, 0, 30, 31, 32, 33, SWORD_OF_RITUAL, 40)
        Classes["HR"] = intArrayOf(7, 0, 0, 34, 35, 36, 37, BEZIQUES_RECOMMENDATION, 40)
        Classes["HWI"] = intArrayOf(11, 10, 0, 23, 24, 25, 26, BEAD_OF_SEASON, 33)
        Classes["HC"] = intArrayOf(15, 10, 0, 27, 28, 29, 30, MARK_OF_FAITH, 33)
    }

    init {

        addStartNpc(*FIRSTCLASSNPCS)
        addTalkId(*FIRSTCLASSNPCS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState("FirstClassChange") ?: return htmltext

        var suffix = ""

        if (Classes.containsKey(event)) {
            // 0 = newClass, 1 = reqClass, 2 = reqRace, 3 = no/no, 4 = no/ok, 5 = ok/no, 6 = ok/ok, 7 = reqItem, 8 = deniedClass
            val array = Classes[event]!!

            if (player.classId.id == array[1] && player.race.ordinal == array[2]) {
                val gotItem = st.hasQuestItems(array[7])

                if (player.level < 20)
                    suffix = "-" + (if (gotItem) array[4] else array[3])
                else {
                    if (gotItem) {
                        suffix = "-" + array[6]

                        st.takeItems(array[7], 1)
                        st.giveItems(SHADOW_WEAPON_COUPON_DGRADE, 15)
                        st.playSound(QuestState.SOUND_FANFARE)

                        player.setClassId(array[0])
                        player.baseClass = array[0]
                        player.sendPacket(HennaInfo(player))
                        player.broadcastUserInfo()
                    } else
                        suffix = "-" + array[5]
                }

                htmltext = npc?.npcId.toString() + suffix + ".htm"
                st.exitQuest(true)
            } else
                htmltext = npc?.npcId.toString() + "-" + array[8] + ".htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = noQuestMsg
        val st = player.getQuestState("FirstClassChange") ?: return htmltext

        if (player.isSubClassActive) {
            st.exitQuest(true)
            return htmltext
        }

        val npcId = npc.npcId
        when (npcId) {
            30290 // Dark Elf
                , 30297, 30462 -> if (player.race == ClassRace.DARK_ELF) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 31)
                        htmltext = npcId.toString() + "-01.htm"
                    else if (player.classId.id == 38)
                        htmltext = npcId.toString() + "-08.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-32.htm"
                else
                    htmltext = npcId.toString() + "-31.htm"
            } else
                htmltext = npcId.toString() + "-33.htm"

            30358 // Thifiell (dark elf)
            -> if (player.race == ClassRace.DARK_ELF) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 31)
                        htmltext = npcId.toString() + "-01.htm"
                    else if (player.classId.id == 38)
                        htmltext = npcId.toString() + "-02.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-12.htm"
                else
                    htmltext = npcId.toString() + "-13.htm"
            } else
                htmltext = npcId.toString() + "-11.htm"

            30500 // Orcs
                , 30505, 30508, 32097 -> if (player.race == ClassRace.ORC) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 44)
                        htmltext = npcId.toString() + "-01.htm"
                    else if (player.classId.id == 49)
                        htmltext = npcId.toString() + "-06.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-21.htm"
                else
                    htmltext = npcId.toString() + "-22.htm"
            } else
                htmltext = npcId.toString() + "-23.htm"

            30565 // Kakai (Orcs)
            -> if (player.race == ClassRace.ORC) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 44)
                        htmltext = npcId.toString() + "-01.htm"
                    else if (player.classId.id == 49)
                        htmltext = npcId.toString() + "-06.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-09.htm"
                else
                    htmltext = npcId.toString() + "-10.htm"
            } else
                htmltext = npcId.toString() + "-11.htm"

            30503 // Dwarf
                , 30594, 30498, 32092, 32093, 30504, 30595, 30499 -> if (player.race == ClassRace.DWARF) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 53)
                        htmltext = npcId.toString() + "-01.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-09.htm"
                else
                    htmltext = npcId.toString() + "-10.htm"
            } else
                htmltext = npcId.toString() + "-11.htm"

            30525 // Bronk and Reed(dwarf)
                , 30520 -> if (player.race == ClassRace.DWARF) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 53)
                        htmltext = npcId.toString() + "-01.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-05.htm"
                else
                    htmltext = npcId.toString() + "-06.htm"
            } else
                htmltext = npcId.toString() + "-07.htm"

            30070 // Elfs and humans mages
                , 30037, 30289, 32098 -> if (player.race == ClassRace.ELF) {
                if (player.isMageClass) {
                    if (player.classId.level() == 0) {
                        if (player.classId.id == 25)
                            htmltext = npcId.toString() + "-01.htm"
                    } else if (player.classId.level() == 1)
                        htmltext = npcId.toString() + "-31.htm"
                    else
                        htmltext = npcId.toString() + "-32.htm"
                } else
                    htmltext = npcId.toString() + "-33.htm"
            } else if (player.race == ClassRace.HUMAN) {
                if (player.isMageClass) {
                    if (player.classId.level() == 0) {
                        if (player.classId.id == 10)
                            htmltext = npcId.toString() + "-08.htm"
                    } else if (player.classId.level() == 1)
                        htmltext = npcId.toString() + "-31.htm"
                    else
                        htmltext = npcId.toString() + "-32.htm"
                } else
                    htmltext = npcId.toString() + "-33.htm"
            } else
                htmltext = npcId.toString() + "-33.htm"

            30154 // Asterios (Elf fighters and mages)
            -> if (player.race == ClassRace.ELF) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 18)
                        htmltext = npcId.toString() + "-01.htm"
                    else if (player.classId.id == 25)
                        htmltext = npcId.toString() + "-02.htm"
                } else if (player.classId.level() == 1)
                    htmltext = npcId.toString() + "-12.htm"
                else
                    htmltext = npcId.toString() + "-13.htm"
            } else
                htmltext = npcId.toString() + "-11.htm"

            30031 // Biotin (Human mages)
            -> if (player.race == ClassRace.HUMAN) {
                if (player.isMageClass) {
                    if (player.classId.level() == 0) {
                        if (player.classId.id == 10)
                            htmltext = npcId.toString() + "-01.htm"
                    } else if (player.classId.level() == 1)
                        htmltext = npcId.toString() + "-06.htm"
                    else
                        htmltext = npcId.toString() + "-07.htm"
                } else
                    htmltext = npcId.toString() + "-08.htm"
            } else
                htmltext = npcId.toString() + "-08.htm"

            30373 // Human and Elfs fighters
                , 30288, 30066 -> if (player.race == ClassRace.HUMAN) {
                htmltext = if (player.classId.level() == 0) {
                    if (player.classId.id == 0)
                        npcId.toString() + "-08.htm"
                    else
                        npcId.toString() + "-40.htm"
                } else if (player.classId.level() == 1)
                    npcId.toString() + "-38.htm"
                else
                    npcId.toString() + "-39.htm"
            } else if (player.race == ClassRace.ELF) {
                htmltext = if (player.classId.level() == 0) {
                    if (player.classId.id == 18)
                        npcId.toString() + "-01.htm"
                    else
                        npcId.toString() + "-40.htm"
                } else if (player.classId.level() == 1)
                    npcId.toString() + "-38.htm"
                else
                    npcId.toString() + "-39.htm"
            } else
                htmltext = npcId.toString() + "-40.htm"

            30026 // Bitz (Human fighters)
            -> htmltext = if (player.race == ClassRace.HUMAN) {
                if (player.classId.level() == 0) {
                    if (player.classId.id == 0)
                        npcId.toString() + "-01.htm"
                    else
                        npcId.toString() + "-10.htm"
                } else if (player.classId.level() == 1)
                    npcId.toString() + "-08.htm"
                else
                    npcId.toString() + "-09.htm"
            } else
                npcId.toString() + "-10.htm"
        }
        st.exitQuest(true)

        return htmltext
    }

    companion object {
        // Quest Items Dark Elf
        private const val GAZE_OF_ABYSS = 1244
        private const val IRON_HEART = 1252
        private const val JEWEL_OF_DARKNESS = 1261
        private const val ORB_OF_ABYSS = 1270

        // Quest Items Orcs
        private const val MARK_OF_RAIDER = 1592
        private const val KHAVATARI_TOTEM = 1615
        private const val MASK_OF_MEDIUM = 1631

        // Quest Items Dwarf
        private const val ARTI_MARKS = 1635
        private const val SCAV_MARKS = 1642

        // Quest Items Light Elf
        private const val ELVEN_KNIGHT_BROOCH = 1204
        private const val REORIA_RECOMMENDATION = 1217
        private const val ETERNITY_DIAMOND = 1230
        private const val LEAF_OF_ORACLE = 1235

        // Quest Items Human
        private const val MEDALLION_OF_WARRIOR = 1145
        private const val SWORD_OF_RITUAL = 1161
        private const val BEZIQUES_RECOMMENDATION = 1190
        private const val BEAD_OF_SEASON = 1292
        private const val MARK_OF_FAITH = 1201

        // Reward Item
        private const val SHADOW_WEAPON_COUPON_DGRADE = 8869

        // Classes
        private val Classes = HashMap<String, IntArray>()

        // Also used by ShadowWeapon script.
        val FIRSTCLASSNPCS = intArrayOf(30026, 30031, 30037, 30066, 30070, 30154, 30288, 30289, 30290, 30297, 30358, 30373, 30462, 30498, 30499, 30500, 30503, 30504, 30505, 30508, 30520, 30525, 30565, 30594, 30595, 32092, 32093, 32097, 32098
        )
    }
}