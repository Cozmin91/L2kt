package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class OracleTeleport : Quest(-1, "teleports") {
    init {

        for (posters in RIFT_POSTERS) {
            addStartNpc(posters)
            addTalkId(posters)
        }

        for (teleporters in TELEPORTERS) {
            addStartNpc(teleporters)
            addTalkId(teleporters)
        }

        for (priests in TEMPLE_PRIEST) {
            addStartNpc(priests)
            addTalkId(priests)
        }

        for (dawn in TOWN_DAWN) {
            addStartNpc(dawn)
            addTalkId(dawn)
        }

        for (dusk in TOWN_DUSK) {
            addStartNpc(dusk)
            addTalkId(dusk)
        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = ""
        val st = player?.getQuestState(name)

        val npcId = npc!!.npcId
        if (event.equals("Return", ignoreCase = true)) {
            if (ArraysUtil.contains(TEMPLE_PRIEST, npcId) && st!!.state == Quest.STATE_STARTED) {
                val loc = RETURN_LOCS[st.getInt("id")]
                player.teleToLocation(loc.x, loc.y, loc.z, 0)
                player.setIsIn7sDungeon(false)
                st.exitQuest(true)
            } else if (ArraysUtil.contains(RIFT_POSTERS, npcId) && st!!.state == Quest.STATE_STARTED) {
                val loc = RETURN_LOCS[st.getInt("id")]
                player.teleToLocation(loc.x, loc.y, loc.z, 0)
                htmltext = "rift_back.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("Festival", ignoreCase = true)) {
            val id = st!!.getInt("id")
            if (ArraysUtil.contains(TOWN_DAWN, id)) {
                player.teleToLocation(-80157, 111344, -4901, 0)
                player.setIsIn7sDungeon(true)
            } else if (ArraysUtil.contains(TOWN_DUSK, id)) {
                player.teleToLocation(-81261, 86531, -5157, 0)
                player.setIsIn7sDungeon(true)
            } else
                htmltext = "oracle1.htm"
        } else if (event.equals("Dimensional", ignoreCase = true)) {
            htmltext = "oracle.htm"
            player!!.teleToLocation(-114755, -179466, -6752, 0)
        } else if (event.equals("5.htm", ignoreCase = true)) {
            val id = st!!.getInt("id")
            if (id > -1)
                htmltext = "5a.htm"

            var i = 0
            for (id1 in TELEPORTERS) {
                if (id1 == npcId)
                    break
                i++
            }

            st["id"] = Integer.toString(i)
            st.state = Quest.STATE_STARTED
            player.teleToLocation(-114755, -179466, -6752, 0)
        } else if (event.equals("6.htm", ignoreCase = true)) {
            htmltext = "6.htm"
            st!!.exitQuest(true)
        } else if (event.equals("zigurratDimensional", ignoreCase = true)) {
            val playerLevel = player!!.level
            if (playerLevel in 20..29)
                st!!.takeItems(57, 2000)
            else if (playerLevel in 30..39)
                st!!.takeItems(57, 4500)
            else if (playerLevel in 40..49)
                st!!.takeItems(57, 8000)
            else if (playerLevel in 50..59)
                st!!.takeItems(57, 12500)
            else if (playerLevel in 60..69)
                st!!.takeItems(57, 18000)
            else if (playerLevel >= 70)
                st!!.takeItems(57, 24500)

            var i = 0
            for (zigurrat in TELEPORTERS) {
                if (zigurrat == npcId)
                    break
                i++
            }

            st!!["id"] = Integer.toString(i)
            st.state = Quest.STATE_STARTED
            st.playSound(QuestState.SOUND_ACCEPT)
            htmltext = "ziggurat_rift.htm"
            player.teleToLocation(-114755, -179466, -6752, 0)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name)

        val npcId = npc.npcId

        if (ArraysUtil.contains(TOWN_DAWN, npcId)) {
            st!!.state = Quest.STATE_STARTED

            var i = 0
            for (dawn in TELEPORTERS) {
                if (dawn == npcId)
                    break
                i++
            }

            st["id"] = Integer.toString(i)
            st.playSound(QuestState.SOUND_ACCEPT)
            player.teleToLocation(-80157, 111344, -4901, 0)
            player.setIsIn7sDungeon(true)
        }

        if (ArraysUtil.contains(TOWN_DUSK, npcId)) {
            st!!.state = Quest.STATE_STARTED

            var i = 0
            for (dusk in TELEPORTERS) {
                if (dusk == npcId)
                    break
                i++
            }

            st["id"] = Integer.toString(i)
            st.playSound(QuestState.SOUND_ACCEPT)
            player.teleToLocation(-81261, 86531, -5157, 0)
            player.setIsIn7sDungeon(true)
        } else if (npcId in 31494..31507) {
            if (player.level < 20) {
                htmltext = "1.htm"
                st!!.exitQuest(true)
            } else if (player.getAllQuests(false).size >= 25) {
                htmltext = "1a.htm"
                st!!.exitQuest(true)
            } else if (!st!!.hasQuestItems(7079))
                htmltext = "3.htm"
            else {
                st.state = Quest.STATE_CREATED
                htmltext = "4.htm"
            }
        } else if (npcId in 31095..31111 || npcId in 31114..31126) {
            val playerLevel = player.level
            if (playerLevel < 20) {
                htmltext = "ziggurat_lowlevel.htm"
                st!!.exitQuest(true)
            } else if (player.getAllQuests(false).size >= 25) {
                player.sendPacket(SystemMessageId.TOO_MANY_QUESTS)
                st!!.exitQuest(true)
            } else if (!st!!.hasQuestItems(7079)) {
                htmltext = "ziggurat_nofrag.htm"
                st.exitQuest(true)
            } else if (playerLevel in 20..29 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 2000) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else if (playerLevel in 30..39 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 4500) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else if (playerLevel in 40..49 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 8000) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else if (playerLevel in 50..59 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 12500) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else if (playerLevel in 60..69 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 18000) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else if (playerLevel >= 70 && st.getQuestItemsCount(PcInventory.ADENA_ID) < 24500) {
                htmltext = "ziggurat_noadena.htm"
                st.exitQuest(true)
            } else
                htmltext = "ziggurat.htm"
        }

        return htmltext
    }

    companion object {
        private val TOWN_DAWN = intArrayOf(31078, 31079, 31080, 31081, 31083, 31084, 31082, 31692, 31694, 31997, 31168)

        private val TOWN_DUSK = intArrayOf(31085, 31086, 31087, 31088, 31090, 31091, 31089, 31693, 31695, 31998, 31169)

        private val TEMPLE_PRIEST = intArrayOf(31127, 31128, 31129, 31130, 31131, 31137, 31138, 31139, 31140, 31141)

        private val RIFT_POSTERS = intArrayOf(31488, 31489, 31490, 31491, 31492, 31493)

        private val TELEPORTERS = intArrayOf(
            31078,
            31079,
            31080,
            31081,
            31082,
            31083,
            31084,
            31692,
            31694,
            31997,
            31168,
            31085,
            31086,
            31087,
            31088,
            31089,
            31090,
            31091,
            31693,
            31695,
            31998,
            31169,
            31494,
            31495,
            31496,
            31497,
            31498,
            31499,
            31500,
            31501,
            31502,
            31503,
            31504,
            31505,
            31506,
            31507,
            31095,
            31096,
            31097,
            31098,
            31099,
            31100,
            31101,
            31102,
            31103,
            31104,
            31105,
            31106,
            31107,
            31108,
            31109,
            31110,
            31114,
            31115,
            31116,
            31117,
            31118,
            31119,
            31120,
            31121,
            31122,
            31123,
            31124,
            31125
        )

        private val RETURN_LOCS = arrayOf(
            Location(-80555, 150337, -3040),
            Location(-13953, 121404, -2984),
            Location(16354, 142820, -2696),
            Location(83369, 149253, -3400),
            Location(111386, 220858, -3544),
            Location(83106, 53965, -1488),
            Location(146983, 26595, -2200),
            Location(148256, -55454, -2779),
            Location(45664, -50318, -800),
            Location(86795, -143078, -1341),
            Location(115136, 74717, -2608),
            Location(-82368, 151568, -3120),
            Location(-14748, 123995, -3112),
            Location(18482, 144576, -3056),
            Location(81623, 148556, -3464),
            Location(112486, 220123, -3592),
            Location(82819, 54607, -1520),
            Location(147570, 28877, -2264),
            Location(149888, -56574, -2979),
            Location(44528, -48370, -800),
            Location(85129, -142103, -1542),
            Location(116642, 77510, -2688),
            Location(-41572, 209731, -5087),
            Location(-52872, -250283, -7908),
            Location(45256, 123906, -5411),
            Location(46192, 170290, -4981),
            Location(111273, 174015, -5437),
            Location(-20604, -250789, -8165),
            Location(-21726, 77385, -5171),
            Location(140405, 79679, -5427),
            Location(-52366, 79097, -4741),
            Location(118311, 132797, -4829),
            Location(172185, -17602, -4901),
            Location(83000, 209213, -5439),
            Location(-19500, 13508, -4901),
            Location(12525, -248496, -9580),
            Location(-41561, 209225, -5087),
            Location(45242, 124466, -5413),
            Location(110711, 174010, -5439),
            Location(-22341, 77375, -5173),
            Location(-52889, 79098, -4741),
            Location(117760, 132794, -4831),
            Location(171792, -17609, -4901),
            Location(82564, 209207, -5439),
            Location(-41565, 210048, -5085),
            Location(45278, 123608, -5411),
            Location(111510, 174013, -5437),
            Location(-21489, 77372, -5171),
            Location(-52016, 79103, -4739),
            Location(118557, 132804, -4829),
            Location(172570, -17605, -4899),
            Location(83347, 209215, -5437),
            Location(42495, 143944, -5381),
            Location(45666, 170300, -4981),
            Location(77138, 78389, -5125),
            Location(139903, 79674, -5429),
            Location(-20021, 13499, -4901),
            Location(113418, 84535, -6541),
            Location(-52940, -250272, -7907),
            Location(46499, 170301, -4979),
            Location(-20280, -250785, -8163),
            Location(140673, 79680, -5437),
            Location(-19182, 13503, -4899),
            Location(12837, -248483, -9579)
        )
    }
}