package com.l2kt.gameserver.instancemanager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.FestivalMonster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.Experience
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.type.PeaceZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ScheduledFuture
import java.util.logging.Logger

object SevenSignsFestival {

    private var _managerInstance: FestivalManager? = null
    var _managerScheduledTask: ScheduledFuture<*>? = null

    var _signsCycle = SevenSigns.currentCycle
    var currentFestivalCycle: Int = 0
        set
    var _nextFestivalCycleStart: Long = 0
    var _nextFestivalStart: Long = 0
    var isFestivalInitialized: Boolean = false
        set
    var isFestivalInProgress: Boolean = false
        set
    var _accumulatedBonuses: MutableList<Int> = mutableListOf()

    internal var _noPartyRegister: Boolean = false
    private var _dawnPeace: MutableList<PeaceZone> = mutableListOf()
    private var _duskPeace: MutableList<PeaceZone> = mutableListOf()

    var _dawnFestivalParticipants: MutableMap<Int, MutableList<Int>> = HashMap()
    var _duskFestivalParticipants: MutableMap<Int, MutableList<Int>> = HashMap()

    var _dawnPreviousParticipants: MutableMap<Int, MutableList<Int>> = HashMap()
    var _duskPreviousParticipants: MutableMap<Int, MutableList<Int>> = HashMap()

    private val _dawnFestivalScores = HashMap<Int, Int>()
    private val _duskFestivalScores = HashMap<Int, Int>()

    private val _festivalData = HashMap<Int, MutableMap<Int, StatsSet>>()

    val _log = Logger.getLogger(SevenSignsFestival::class.java.name)

    private const val RESTORE_FESTIVAL = "SELECT festivalId, cabal, cycle, date, score, members FROM seven_signs_festival"
    private const val RESTORE_FESTIVAL_2 =
        "SELECT festival_cycle, accumulated_bonus0, accumulated_bonus1, accumulated_bonus2, accumulated_bonus3, accumulated_bonus4 FROM seven_signs_status WHERE id=0"
    private const val UPDATE =
        "UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?"
    private const val INSERT =
        "INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)"
    private const val GET_CLAN_NAME =
        "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)"

    /**
     * These length settings are important! :) All times are relative to the ELAPSED time (in ms) since a festival begins. Festival manager start is the time after the server starts to begin the first festival cycle. The cycle length should ideally be at least 2x longer than the festival length.
     * This allows ample time for players to sign-up to participate in the festival. The intermission is the time between the festival participants being moved to the "arenas" and the spawning of the first set of mobs. The monster swarm time is the time before the monsters swarm to the center of the
     * arena, after they are spawned. The chest spawn time is for when the bonus festival chests spawn, usually towards the end of the festival.
     */
    val FESTIVAL_SIGNUP_TIME = Config.ALT_FESTIVAL_CYCLE_LENGTH - Config.ALT_FESTIVAL_LENGTH - 60000

    // Key Constants \\
    private const val FESTIVAL_MAX_OFFSET_X = 230
    private const val FESTIVAL_MAX_OFFSET_Y = 230
    private const val FESTIVAL_DEFAULT_RESPAWN = 60 // Specify in seconds!

    const val FESTIVAL_COUNT = 5

    const val FESTIVAL_OFFERING_ID = 5901
    const val FESTIVAL_OFFERING_VALUE = 5

    // ////////////////////// \\\\\\\\\\\\\\\\\\\\\\\\\\
    /*
 * The following contains all the necessary spawn data for: - Player Start Locations - Witches - Monsters - Chests All data is given by: X, Y, Z (coords), Heading, NPC ID (if necessary) This may be moved externally in time, but the data should not change.
 */
    val FESTIVAL_DAWN_PLAYER_SPAWNS = arrayOf(
        intArrayOf(-79187, 113186, -4895, 0), // 31 and below
        intArrayOf(-75918, 110137, -4895, 0), // 42 and below
        intArrayOf(-73835, 111969, -4895, 0), // 53 and below
        intArrayOf(-76170, 113804, -4895, 0), // 64 and below
        intArrayOf(-78927, 109528, -4895, 0)
    )// No level limit

    val FESTIVAL_DUSK_PLAYER_SPAWNS = arrayOf(
        intArrayOf(-77200, 88966, -5151, 0), // 31 and below
        intArrayOf(-76941, 85307, -5151, 0), // 42 and below
        intArrayOf(-74855, 87135, -5151, 0), // 53 and below
        intArrayOf(-80208, 88222, -5151, 0), // 64 and below
        intArrayOf(-79954, 84697, -5151, 0)
    )// No level limit

    val FESTIVAL_DAWN_WITCH_SPAWNS = arrayOf(
        intArrayOf(-79183, 113052, -4891, 0, 31132), // 31 and below
        intArrayOf(-75916, 110270, -4891, 0, 31133), // 42 and below
        intArrayOf(-73979, 111970, -4891, 0, 31134), // 53 and below
        intArrayOf(-76174, 113663, -4891, 0, 31135), // 64 and below
        intArrayOf(-78930, 109664, -4891, 0, 31136)
    )// No level limit

    val FESTIVAL_DUSK_WITCH_SPAWNS = arrayOf(
        intArrayOf(-77199, 88830, -5147, 0, 31142), // 31 and below
        intArrayOf(-76942, 85438, -5147, 0, 31143), // 42 and below
        intArrayOf(-74990, 87135, -5147, 0, 31144), // 53 and below
        intArrayOf(-80207, 88222, -5147, 0, 31145), // 64 and below
        intArrayOf(-79952, 84833, -5147, 0, 31146)
    )// No level limit

    val FESTIVAL_DAWN_PRIMARY_SPAWNS = arrayOf(
        arrayOf(
            /* Level 31 and Below - Offering of the Branded */
            intArrayOf(-78537, 113839, -4895, -1, 18009),
            intArrayOf(-78466, 113852, -4895, -1, 18010),
            intArrayOf(-78509, 113899, -4895, -1, 18010),

            intArrayOf(-78481, 112557, -4895, -1, 18009),
            intArrayOf(-78559, 112504, -4895, -1, 18010),
            intArrayOf(-78489, 112494, -4895, -1, 18010),

            intArrayOf(-79803, 112543, -4895, -1, 18012),
            intArrayOf(-79854, 112492, -4895, -1, 18013),
            intArrayOf(-79886, 112557, -4895, -1, 18014),

            intArrayOf(-79821, 113811, -4895, -1, 18015),
            intArrayOf(-79857, 113896, -4895, -1, 18017),
            intArrayOf(-79878, 113816, -4895, -1, 18018),

            // Archers and Marksmen \\
            intArrayOf(-79190, 113660, -4895, -1, 18011),
            intArrayOf(-78710, 113188, -4895, -1, 18011),
            intArrayOf(-79190, 112730, -4895, -1, 18016),
            intArrayOf(-79656, 113188, -4895, -1, 18016)
        ), arrayOf(
            /* Level 42 and Below - Apostate Offering */
            intArrayOf(-76558, 110784, -4895, -1, 18019),
            intArrayOf(-76607, 110815, -4895, -1, 18020), // South West
            intArrayOf(-76559, 110820, -4895, -1, 18020),

            intArrayOf(-75277, 110792, -4895, -1, 18019),
            intArrayOf(-75225, 110801, -4895, -1, 18020), // South East
            intArrayOf(-75262, 110832, -4895, -1, 18020),

            intArrayOf(-75249, 109441, -4895, -1, 18022),
            intArrayOf(-75278, 109495, -4895, -1, 18023), // North East
            intArrayOf(-75223, 109489, -4895, -1, 18024),

            intArrayOf(-76556, 109490, -4895, -1, 18025),
            intArrayOf(-76607, 109469, -4895, -1, 18027), // North West
            intArrayOf(-76561, 109450, -4895, -1, 18028),

            // Archers and Marksmen \\
            intArrayOf(-76399, 110144, -4895, -1, 18021),
            intArrayOf(-75912, 110606, -4895, -1, 18021),
            intArrayOf(-75444, 110144, -4895, -1, 18026),
            intArrayOf(-75930, 109665, -4895, -1, 18026)
        ), arrayOf(
            /* Level 53 and Below - Witch's Offering */
            intArrayOf(-73184, 111319, -4895, -1, 18029),
            intArrayOf(-73135, 111294, -4895, -1, 18030), // South West
            intArrayOf(-73185, 111281, -4895, -1, 18030),

            intArrayOf(-74477, 111321, -4895, -1, 18029),
            intArrayOf(-74523, 111293, -4895, -1, 18030), // South East
            intArrayOf(-74481, 111280, -4895, -1, 18030),

            intArrayOf(-74489, 112604, -4895, -1, 18032),
            intArrayOf(-74491, 112660, -4895, -1, 18033), // North East
            intArrayOf(-74527, 112629, -4895, -1, 18034),

            intArrayOf(-73197, 112621, -4895, -1, 18035),
            intArrayOf(-73142, 112631, -4895, -1, 18037), // North West
            intArrayOf(-73182, 112656, -4895, -1, 18038),

            // Archers and Marksmen \\
            intArrayOf(-73834, 112430, -4895, -1, 18031),
            intArrayOf(-74299, 111959, -4895, -1, 18031),
            intArrayOf(-73841, 111491, -4895, -1, 18036),
            intArrayOf(-73363, 111959, -4895, -1, 18036)
        ), arrayOf(
            /* Level 64 and Below - Dark Omen Offering */
            intArrayOf(-75543, 114461, -4895, -1, 18039),
            intArrayOf(-75514, 114493, -4895, -1, 18040), // South West
            intArrayOf(-75488, 114456, -4895, -1, 18040),

            intArrayOf(-75521, 113158, -4895, -1, 18039),
            intArrayOf(-75504, 113110, -4895, -1, 18040), // South East
            intArrayOf(-75489, 113142, -4895, -1, 18040),

            intArrayOf(-76809, 113143, -4895, -1, 18042),
            intArrayOf(-76860, 113138, -4895, -1, 18043), // North East
            intArrayOf(-76831, 113112, -4895, -1, 18044),

            intArrayOf(-76831, 114441, -4895, -1, 18045),
            intArrayOf(-76840, 114490, -4895, -1, 18047), // North West
            intArrayOf(-76864, 114455, -4895, -1, 18048),

            // Archers and Marksmen \\
            intArrayOf(-75703, 113797, -4895, -1, 18041),
            intArrayOf(-76180, 114263, -4895, -1, 18041),
            intArrayOf(-76639, 113797, -4895, -1, 18046),
            intArrayOf(-76180, 113337, -4895, -1, 18046)
        ), arrayOf(
            /* No Level Limit - Offering of Forbidden Path */
            intArrayOf(-79576, 108881, -4895, -1, 18049),
            intArrayOf(-79592, 108835, -4895, -1, 18050), // South West
            intArrayOf(-79614, 108871, -4895, -1, 18050),

            intArrayOf(-79586, 110171, -4895, -1, 18049),
            intArrayOf(-79589, 110216, -4895, -1, 18050), // South East
            intArrayOf(-79620, 110177, -4895, -1, 18050),

            intArrayOf(-78825, 110182, -4895, -1, 18052),
            intArrayOf(-78238, 110182, -4895, -1, 18053), // North East
            intArrayOf(-78266, 110218, -4895, -1, 18054),

            intArrayOf(-78275, 108883, -4895, -1, 18055),
            intArrayOf(-78267, 108839, -4895, -1, 18057), // North West
            intArrayOf(-78241, 108871, -4895, -1, 18058),

            // Archers and Marksmen \\
            intArrayOf(-79394, 109538, -4895, -1, 18051),
            intArrayOf(-78929, 109992, -4895, -1, 18051),
            intArrayOf(-78454, 109538, -4895, -1, 18056),
            intArrayOf(-78929, 109053, -4895, -1, 18056)
        )
    )

    val FESTIVAL_DUSK_PRIMARY_SPAWNS = arrayOf(
        arrayOf(
            /* Level 31 and Below - Offering of the Branded */
            intArrayOf(-76542, 89653, -5151, -1, 18009),
            intArrayOf(-76509, 89637, -5151, -1, 18010),
            intArrayOf(-76548, 89614, -5151, -1, 18010),

            intArrayOf(-76539, 88326, -5151, -1, 18009),
            intArrayOf(-76512, 88289, -5151, -1, 18010),
            intArrayOf(-76546, 88287, -5151, -1, 18010),

            intArrayOf(-77879, 88308, -5151, -1, 18012),
            intArrayOf(-77886, 88310, -5151, -1, 18013),
            intArrayOf(-77879, 88278, -5151, -1, 18014),

            intArrayOf(-77857, 89605, -5151, -1, 18015),
            intArrayOf(-77858, 89658, -5151, -1, 18017),
            intArrayOf(-77891, 89633, -5151, -1, 18018),

            // Archers and Marksmen \\
            intArrayOf(-76728, 88962, -5151, -1, 18011),
            intArrayOf(-77194, 88494, -5151, -1, 18011),
            intArrayOf(-77660, 88896, -5151, -1, 18016),
            intArrayOf(-77195, 89438, -5151, -1, 18016)
        ), arrayOf(
            /* Level 42 and Below - Apostate's Offering */
            intArrayOf(-77585, 84650, -5151, -1, 18019),
            intArrayOf(-77628, 84643, -5151, -1, 18020),
            intArrayOf(-77607, 84613, -5151, -1, 18020),

            intArrayOf(-76603, 85946, -5151, -1, 18019),
            intArrayOf(-77606, 85994, -5151, -1, 18020),
            intArrayOf(-77638, 85959, -5151, -1, 18020),

            intArrayOf(-76301, 85960, -5151, -1, 18022),
            intArrayOf(-76257, 85972, -5151, -1, 18023),
            intArrayOf(-76286, 85992, -5151, -1, 18024),

            intArrayOf(-76281, 84667, -5151, -1, 18025),
            intArrayOf(-76291, 84611, -5151, -1, 18027),
            intArrayOf(-76257, 84616, -5151, -1, 18028),

            // Archers and Marksmen \\
            intArrayOf(-77419, 85307, -5151, -1, 18021),
            intArrayOf(-76952, 85768, -5151, -1, 18021),
            intArrayOf(-76477, 85312, -5151, -1, 18026),
            intArrayOf(-76942, 84832, -5151, -1, 18026)
        ), arrayOf(
            /* Level 53 and Below - Witch's Offering */
            intArrayOf(-74211, 86494, -5151, -1, 18029),
            intArrayOf(-74200, 86449, -5151, -1, 18030),
            intArrayOf(-74167, 86464, -5151, -1, 18030),

            intArrayOf(-75495, 86482, -5151, -1, 18029),
            intArrayOf(-75540, 86473, -5151, -1, 18030),
            intArrayOf(-75509, 86445, -5151, -1, 18030),

            intArrayOf(-75509, 87775, -5151, -1, 18032),
            intArrayOf(-75518, 87826, -5151, -1, 18033),
            intArrayOf(-75542, 87780, -5151, -1, 18034),

            intArrayOf(-74214, 87789, -5151, -1, 18035),
            intArrayOf(-74169, 87801, -5151, -1, 18037),
            intArrayOf(-74198, 87827, -5151, -1, 18038),

            // Archers and Marksmen \\
            intArrayOf(-75324, 87135, -5151, -1, 18031),
            intArrayOf(-74852, 87606, -5151, -1, 18031),
            intArrayOf(-74388, 87146, -5151, -1, 18036),
            intArrayOf(-74856, 86663, -5151, -1, 18036)
        ), arrayOf(
            /* Level 64 and Below - Dark Omen Offering */
            intArrayOf(-79560, 89007, -5151, -1, 18039),
            intArrayOf(-79521, 89016, -5151, -1, 18040),
            intArrayOf(-79544, 89047, -5151, -1, 18040),

            intArrayOf(-79552, 87717, -5151, -1, 18039),
            intArrayOf(-79552, 87673, -5151, -1, 18040),
            intArrayOf(-79510, 87702, -5151, -1, 18040),

            intArrayOf(-80866, 87719, -5151, -1, 18042),
            intArrayOf(-80897, 87689, -5151, -1, 18043),
            intArrayOf(-80850, 87685, -5151, -1, 18044),

            intArrayOf(-80848, 89013, -5151, -1, 18045),
            intArrayOf(-80887, 89051, -5151, -1, 18047),
            intArrayOf(-80891, 89004, -5151, -1, 18048),

            // Archers and Marksmen \\
            intArrayOf(-80205, 87895, -5151, -1, 18041),
            intArrayOf(-80674, 88350, -5151, -1, 18041),
            intArrayOf(-80209, 88833, -5151, -1, 18046),
            intArrayOf(-79743, 88364, -5151, -1, 18046)
        ), arrayOf(
            /* No Level Limit - Offering of Forbidden Path */
            intArrayOf(-80624, 84060, -5151, -1, 18049),
            intArrayOf(-80621, 84007, -5151, -1, 18050),
            intArrayOf(-80590, 84039, -5151, -1, 18050),

            intArrayOf(-80605, 85349, -5151, -1, 18049),
            intArrayOf(-80639, 85363, -5151, -1, 18050),
            intArrayOf(-80611, 85385, -5151, -1, 18050),

            intArrayOf(-79311, 85353, -5151, -1, 18052),
            intArrayOf(-79277, 85384, -5151, -1, 18053),
            intArrayOf(-79273, 85539, -5151, -1, 18054),

            intArrayOf(-79297, 84054, -5151, -1, 18055),
            intArrayOf(-79285, 84006, -5151, -1, 18057),
            intArrayOf(-79260, 84040, -5151, -1, 18058),

            // Archers and Marksmen \\
            intArrayOf(-79945, 85171, -5151, -1, 18051),
            intArrayOf(-79489, 84707, -5151, -1, 18051),
            intArrayOf(-79952, 84222, -5151, -1, 18056),
            intArrayOf(-80423, 84703, -5151, -1, 18056)
        )
    )

    val FESTIVAL_DAWN_SECONDARY_SPAWNS = arrayOf(
        arrayOf(
            /* 31 and Below */
            intArrayOf(-78757, 112834, -4895, -1, 18016),
            intArrayOf(-78581, 112834, -4895, -1, 18016),

            intArrayOf(-78822, 112526, -4895, -1, 18011),
            intArrayOf(-78822, 113702, -4895, -1, 18011),
            intArrayOf(-78822, 113874, -4895, -1, 18011),

            intArrayOf(-79524, 113546, -4895, -1, 18011),
            intArrayOf(-79693, 113546, -4895, -1, 18011),
            intArrayOf(-79858, 113546, -4895, -1, 18011),

            intArrayOf(-79545, 112757, -4895, -1, 18016),
            intArrayOf(-79545, 112586, -4895, -1, 18016)
        ), arrayOf(
            /* 42 and Below */
            intArrayOf(-75565, 110580, -4895, -1, 18026),
            intArrayOf(-75565, 110740, -4895, -1, 18026),

            intArrayOf(-75577, 109776, -4895, -1, 18021),
            intArrayOf(-75413, 109776, -4895, -1, 18021),
            intArrayOf(-75237, 109776, -4895, -1, 18021),

            intArrayOf(-76274, 109468, -4895, -1, 18021),
            intArrayOf(-76274, 109635, -4895, -1, 18021),
            intArrayOf(-76274, 109795, -4895, -1, 18021),

            intArrayOf(-76351, 110500, -4895, -1, 18056),
            intArrayOf(-76528, 110500, -4895, -1, 18056)
        ), arrayOf(
            /* 53 and Below */
            intArrayOf(-74191, 111527, -4895, -1, 18036),
            intArrayOf(-74191, 111362, -4895, -1, 18036),

            intArrayOf(-73495, 111611, -4895, -1, 18031),
            intArrayOf(-73327, 111611, -4895, -1, 18031),
            intArrayOf(-73154, 111611, -4895, -1, 18031),

            intArrayOf(-73473, 112301, -4895, -1, 18031),
            intArrayOf(-73473, 112475, -4895, -1, 18031),
            intArrayOf(-73473, 112649, -4895, -1, 18031),

            intArrayOf(-74270, 112326, -4895, -1, 18036),
            intArrayOf(-74443, 112326, -4895, -1, 18036)
        ), arrayOf(
            /* 64 and Below */
            intArrayOf(-75738, 113439, -4895, -1, 18046),
            intArrayOf(-75571, 113439, -4895, -1, 18046),

            intArrayOf(-75824, 114141, -4895, -1, 18041),
            intArrayOf(-75824, 114309, -4895, -1, 18041),
            intArrayOf(-75824, 114477, -4895, -1, 18041),

            intArrayOf(-76513, 114158, -4895, -1, 18041),
            intArrayOf(-76683, 114158, -4895, -1, 18041),
            intArrayOf(-76857, 114158, -4895, -1, 18041),

            intArrayOf(-76535, 113357, -4895, -1, 18056),
            intArrayOf(-76535, 113190, -4895, -1, 18056)
        ), arrayOf(
            /* No Level Limit */
            intArrayOf(-79350, 109894, -4895, -1, 18056),
            intArrayOf(-79534, 109894, -4895, -1, 18056),

            intArrayOf(-79285, 109187, -4895, -1, 18051),
            intArrayOf(-79285, 109019, -4895, -1, 18051),
            intArrayOf(-79285, 108860, -4895, -1, 18051),

            intArrayOf(-78587, 109172, -4895, -1, 18051),
            intArrayOf(-78415, 109172, -4895, -1, 18051),
            intArrayOf(-78249, 109172, -4895, -1, 18051),

            intArrayOf(-78575, 109961, -4895, -1, 18056),
            intArrayOf(-78575, 110130, -4895, -1, 18056)
        )
    )

    val FESTIVAL_DUSK_SECONDARY_SPAWNS = arrayOf(
        arrayOf(
            /* 31 and Below */
            intArrayOf(-76844, 89304, -5151, -1, 18011),
            intArrayOf(-76844, 89479, -5151, -1, 18011),
            intArrayOf(-76844, 89649, -5151, -1, 18011),

            intArrayOf(-77544, 89326, -5151, -1, 18011),
            intArrayOf(-77716, 89326, -5151, -1, 18011),
            intArrayOf(-77881, 89326, -5151, -1, 18011),

            intArrayOf(-77561, 88530, -5151, -1, 18016),
            intArrayOf(-77561, 88364, -5151, -1, 18016),

            intArrayOf(-76762, 88615, -5151, -1, 18016),
            intArrayOf(-76594, 88615, -5151, -1, 18016)
        ), arrayOf(
            /* 42 and Below */
            intArrayOf(-77307, 84969, -5151, -1, 18021),
            intArrayOf(-77307, 84795, -5151, -1, 18021),
            intArrayOf(-77307, 84623, -5151, -1, 18021),

            intArrayOf(-76614, 84944, -5151, -1, 18021),
            intArrayOf(-76433, 84944, -5151, -1, 18021),
            intArrayOf(-7626 - 1, 84944, -5151, -1, 18021),

            intArrayOf(-76594, 85745, -5151, -1, 18026),
            intArrayOf(-76594, 85910, -5151, -1, 18026),

            intArrayOf(-77384, 85660, -5151, -1, 18026),
            intArrayOf(-77555, 85660, -5151, -1, 18026)
        ), arrayOf(
            /* 53 and Below */
            intArrayOf(-74517, 86782, -5151, -1, 18031),
            intArrayOf(-74344, 86782, -5151, -1, 18031),
            intArrayOf(-74185, 86782, -5151, -1, 18031),

            intArrayOf(-74496, 87464, -5151, -1, 18031),
            intArrayOf(-74496, 87636, -5151, -1, 18031),
            intArrayOf(-74496, 87815, -5151, -1, 18031),

            intArrayOf(-75298, 87497, -5151, -1, 18036),
            intArrayOf(-75460, 87497, -5151, -1, 18036),

            intArrayOf(-75219, 86712, -5151, -1, 18036),
            intArrayOf(-75219, 86531, -5151, -1, 18036)
        ), arrayOf(
            /* 64 and Below */
            intArrayOf(-79851, 88703, -5151, -1, 18041),
            intArrayOf(-79851, 88868, -5151, -1, 18041),
            intArrayOf(-79851, 89040, -5151, -1, 18041),

            intArrayOf(-80548, 88722, -5151, -1, 18041),
            intArrayOf(-80711, 88722, -5151, -1, 18041),
            intArrayOf(-80883, 88722, -5151, -1, 18041),

            intArrayOf(-80565, 87916, -5151, -1, 18046),
            intArrayOf(-80565, 87752, -5151, -1, 18046),

            intArrayOf(-79779, 87996, -5151, -1, 18046),
            intArrayOf(-79613, 87996, -5151, -1, 18046)
        ), arrayOf(
            /* No Level Limit */
            intArrayOf(-79271, 84330, -5151, -1, 18051),
            intArrayOf(-79448, 84330, -5151, -1, 18051),
            intArrayOf(-79601, 84330, -5151, -1, 18051),

            intArrayOf(-80311, 84367, -5151, -1, 18051),
            intArrayOf(-80311, 84196, -5151, -1, 18051),
            intArrayOf(-80311, 84015, -5151, -1, 18051),

            intArrayOf(-80556, 85049, -5151, -1, 18056),
            intArrayOf(-80384, 85049, -5151, -1, 18056),

            intArrayOf(-79598, 85127, -5151, -1, 18056),
            intArrayOf(-79598, 85303, -5151, -1, 18056)
        )
    )

    val FESTIVAL_DAWN_CHEST_SPAWNS = arrayOf(
        arrayOf(
            /* Level 31 and Below */
            intArrayOf(-78999, 112957, -4927, -1, 18109),
            intArrayOf(-79153, 112873, -4927, -1, 18109),
            intArrayOf(-79256, 112873, -4927, -1, 18109),
            intArrayOf(-79368, 112957, -4927, -1, 18109),

            intArrayOf(-79481, 113124, -4927, -1, 18109),
            intArrayOf(-79481, 113275, -4927, -1, 18109),

            intArrayOf(-79364, 113398, -4927, -1, 18109),
            intArrayOf(-79213, 113500, -4927, -1, 18109),
            intArrayOf(-79099, 113500, -4927, -1, 18109),
            intArrayOf(-78960, 113398, -4927, -1, 18109),

            intArrayOf(-78882, 113235, -4927, -1, 18109),
            intArrayOf(-78882, 113099, -4927, -1, 18109)
        ), arrayOf(
            /* Level 42 and Below */
            intArrayOf(-76119, 110383, -4927, -1, 18110),
            intArrayOf(-75980, 110442, -4927, -1, 18110),
            intArrayOf(-75848, 110442, -4927, -1, 18110),
            intArrayOf(-75720, 110383, -4927, -1, 18110),

            intArrayOf(-75625, 110195, -4927, -1, 18110),
            intArrayOf(-75625, 110063, -4927, -1, 18110),

            intArrayOf(-75722, 109908, -4927, -1, 18110),
            intArrayOf(-75863, 109832, -4927, -1, 18110),
            intArrayOf(-75989, 109832, -4927, -1, 18110),
            intArrayOf(-76130, 109908, -4927, -1, 18110),

            intArrayOf(-76230, 110079, -4927, -1, 18110),
            intArrayOf(-76230, 110215, -4927, -1, 18110)
        ), arrayOf(
            /* Level 53 and Below */
            intArrayOf(-74055, 111781, -4927, -1, 18111),
            intArrayOf(-74144, 111938, -4927, -1, 18111),
            intArrayOf(-74144, 112075, -4927, -1, 18111),
            intArrayOf(-74055, 112173, -4927, -1, 18111),

            intArrayOf(-73885, 112289, -4927, -1, 18111),
            intArrayOf(-73756, 112289, -4927, -1, 18111),

            intArrayOf(-73574, 112141, -4927, -1, 18111),
            intArrayOf(-73511, 112040, -4927, -1, 18111),
            intArrayOf(-73511, 111912, -4927, -1, 18111),
            intArrayOf(-73574, 111772, -4927, -1, 18111),

            intArrayOf(-73767, 111669, -4927, -1, 18111),
            intArrayOf(-73899, 111669, -4927, -1, 18111)
        ), arrayOf(
            /* Level 64 and Below */
            intArrayOf(-76008, 113566, -4927, -1, 18112),
            intArrayOf(-76159, 113485, -4927, -1, 18112),
            intArrayOf(-76267, 113485, -4927, -1, 18112),
            intArrayOf(-76386, 113566, -4927, -1, 18112),

            intArrayOf(-76482, 113748, -4927, -1, 18112),
            intArrayOf(-76482, 113885, -4927, -1, 18112),

            intArrayOf(-76371, 114029, -4927, -1, 18112),
            intArrayOf(-76220, 114118, -4927, -1, 18112),
            intArrayOf(-76092, 114118, -4927, -1, 18112),
            intArrayOf(-75975, 114029, -4927, -1, 18112),

            intArrayOf(-75861, 11385 - 1, -4927, -1, 18112),
            intArrayOf(-75861, 113713, -4927, -1, 18112)
        ), arrayOf(
            /* No Level Limit */
            intArrayOf(-79100, 109782, -4927, -1, 18113),
            intArrayOf(-78962, 109853, -4927, -1, 18113),
            intArrayOf(-78851, 109853, -4927, -1, 18113),
            intArrayOf(-78721, 109782, -4927, -1, 18113),

            intArrayOf(-78615, 109596, -4927, -1, 18113),
            intArrayOf(-78615, 109453, -4927, -1, 18113),

            intArrayOf(-78746, 109300, -4927, -1, 18113),
            intArrayOf(-78881, 109203, -4927, -1, 18113),
            intArrayOf(-79027, 109203, -4927, -1, 18113),
            intArrayOf(-79159, 109300, -4927, -1, 18113),

            intArrayOf(-79240, 109480, -4927, -1, 18113),
            intArrayOf(-79240, 109615, -4927, -1, 18113)
        )
    )

    val FESTIVAL_DUSK_CHEST_SPAWNS = arrayOf(
        arrayOf(
            /* Level 31 and Below */
            intArrayOf(-77016, 88726, -5183, -1, 18114),
            intArrayOf(-77136, 88646, -5183, -1, 18114),
            intArrayOf(-77247, 88646, -5183, -1, 18114),
            intArrayOf(-77380, 88726, -5183, -1, 18114),

            intArrayOf(-77512, 88883, -5183, -1, 18114),
            intArrayOf(-77512, 89053, -5183, -1, 18114),

            intArrayOf(-77378, 89287, -5183, -1, 18114),
            intArrayOf(-77254, 89238, -5183, -1, 18114),
            intArrayOf(-77095, 89238, -5183, -1, 18114),
            intArrayOf(-76996, 89287, -5183, -1, 18114),

            intArrayOf(-76901, 89025, -5183, -1, 18114),
            intArrayOf(-76901, 88891, -5183, -1, 18114)
        ), arrayOf(
            /* Level 42 and Below */
            intArrayOf(-77128, 85553, -5183, -1, 18115),
            intArrayOf(-77036, 85594, -5183, -1, 18115),
            intArrayOf(-76919, 85594, -5183, -1, 18115),
            intArrayOf(-76755, 85553, -5183, -1, 18115),

            intArrayOf(-76635, 85392, -5183, -1, 18115),
            intArrayOf(-76635, 85216, -5183, -1, 18115),

            intArrayOf(-76761, 85025, -5183, -1, 18115),
            intArrayOf(-76908, 85004, -5183, -1, 18115),
            intArrayOf(-77041, 85004, -5183, -1, 18115),
            intArrayOf(-77138, 85025, -5183, -1, 18115),

            intArrayOf(-77268, 85219, -5183, -1, 18115),
            intArrayOf(-77268, 85410, -5183, -1, 18115)
        ), arrayOf(
            /* Level 53 and Below */
            intArrayOf(-75150, 87303, -5183, -1, 18116),
            intArrayOf(-75150, 87175, -5183, -1, 18116),
            intArrayOf(-75150, 87175, -5183, -1, 18116),
            intArrayOf(-75150, 87303, -5183, -1, 18116),

            intArrayOf(-74943, 87433, -5183, -1, 18116),
            intArrayOf(-74767, 87433, -5183, -1, 18116),

            intArrayOf(-74556, 87306, -5183, -1, 18116),
            intArrayOf(-74556, 87184, -5183, -1, 18116),
            intArrayOf(-74556, 87184, -5183, -1, 18116),
            intArrayOf(-74556, 87306, -5183, -1, 18116),

            intArrayOf(-74757, 86830, -5183, -1, 18116),
            intArrayOf(-74927, 86830, -5183, -1, 18116)
        ), arrayOf(
            /* Level 64 and Below */
            intArrayOf(-80010, 88128, -5183, -1, 18117),
            intArrayOf(-80113, 88066, -5183, -1, 18117),
            intArrayOf(-80220, 88066, -5183, -1, 18117),
            intArrayOf(-80359, 88128, -5183, -1, 18117),

            intArrayOf(-80467, 88267, -5183, -1, 18117),
            intArrayOf(-80467, 88436, -5183, -1, 18117),

            intArrayOf(-80381, 88639, -5183, -1, 18117),
            intArrayOf(-80278, 88577, -5183, -1, 18117),
            intArrayOf(-80142, 88577, -5183, -1, 18117),
            intArrayOf(-80028, 88639, -5183, -1, 18117),

            intArrayOf(-79915, 88466, -5183, -1, 18117),
            intArrayOf(-79915, 88322, -5183, -1, 18117)
        ), arrayOf(
            /* No Level Limit */
            intArrayOf(-80153, 84947, -5183, -1, 18118),
            intArrayOf(-80003, 84962, -5183, -1, 18118),
            intArrayOf(-79848, 84962, -5183, -1, 18118),
            intArrayOf(-79742, 84947, -5183, -1, 18118),

            intArrayOf(-79668, 84772, -5183, -1, 18118),
            intArrayOf(-79668, 84619, -5183, -1, 18118),

            intArrayOf(-79772, 84471, -5183, -1, 18118),
            intArrayOf(-79888, 84414, -5183, -1, 18118),
            intArrayOf(-80023, 84414, -5183, -1, 18118),
            intArrayOf(-80166, 84471, -5183, -1, 18118),

            intArrayOf(-80253, 84600, -5183, -1, 18118),
            intArrayOf(-80253, 84780, -5183, -1, 18118)
        )
    )

    /**
     * Returns true if the monster ID given is of an archer/marksman type.
     * @param npcId
     * @return boolean isArcher
     */
    fun isFestivalArcher(npcId: Int): Boolean {
        if (npcId < 18009 || npcId > 18108)
            return false

        val identifier = npcId % 10
        return identifier == 4 || identifier == 9
    }

    /**
     * Returns true if the monster ID given is a festival chest.
     * @param npcId
     * @return boolean isChest
     */
    fun isFestivalChest(npcId: Int): Boolean {
        return npcId < 18109 || npcId > 18118
    }

    private fun addReputationPointsForPartyMemberClan(playerName: String) {
        val player = World.getPlayer(playerName)
        if (player != null) {
            if (player.clan != null) {
                player.clan.addReputationScore(100)
                player.clan.broadcastToOnlineMembers(
                    SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(
                        playerName
                    ).addNumber(100)
                )
            }
        } else {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val st = con.prepareStatement(GET_CLAN_NAME)
                    st.setString(1, playerName)

                    val rset = st.executeQuery()
                    if (rset.next()) {
                        val clanName = rset.getString("clan_name")
                        if (clanName != null) {
                            val clan = ClanTable.getClanByName(clanName)
                            if (clan != null) {
                                clan.addReputationScore(100)
                                clan.broadcastToOnlineMembers(
                                    SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_WAS_IN_HIGHEST_RANKED_PARTY_IN_FESTIVAL_OF_DARKNESS_AND_GAINED_S2_REPUTATION).addString(
                                        playerName
                                    ).addNumber(100)
                                )
                            }
                        }
                    }

                    rset.close()
                    st.close()
                }
            } catch (e: Exception) {
                _log.warning("could not get clan name of $playerName: $e")
            }

        }
    }

    /**
     * Primarily used to terminate the Festival Manager, when the Seven Signs period changes.
     * @return ScheduledFuture festManagerScheduler
     */
    val festivalManagerSchedule: ScheduledFuture<*>?
        get() {
            if (_managerScheduledTask == null)
                startFestivalManager()

            return _managerScheduledTask
        }

    val minsToNextCycle: Int
        get() = if (SevenSigns.isSealValidationPeriod) -1 else Math.round(((_nextFestivalCycleStart - System.currentTimeMillis()) / 60000).toFloat())

    val minsToNextFestival: Int
        get() = if (SevenSigns.isSealValidationPeriod) -1 else Math.round(((_nextFestivalStart - System.currentTimeMillis()) / 60000).toFloat()) + 1

    val timeToNextFestivalStr: String
        get() = if (SevenSigns.isSealValidationPeriod) "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>" else "<font color=\"FF0000\">The next festival will begin in $minsToNextFestival minute(s).</font>"

    val totalAccumulatedBonus: Int
        get() {
            var totalAccumBonus = 0

            for (accumBonus in _accumulatedBonuses)
                totalAccumBonus += accumBonus

            return totalAccumBonus
        }

    enum class FestivalType constructor(val maxScore: Int, val festivalTypeName: String, val maxLevel: Int) {
        MAX_31(60, "Level 31 or lower", 31),
        MAX_42(70, "Level 42 or lower", 42),
        MAX_53(100, "Level 53 or lower", 53),
        MAX_64(120, "Level 64 or lower", 64),
        MAX_NONE(150, "No Level Limit", Experience.MAX_LEVEL - 1);


        companion object {
            val VALUES = values()
        }
    }

    init {
        run {
            restoreFestivalData()
            if (SevenSigns.isSealValidationPeriod) {
                _log.info("SevenSignsFestival: Initialization bypassed due to Seal Validation in effect.")
                return@run
            }

            startFestivalManager()
        }
    }

    /**
     * Used to start the Festival Manager, if the current period is not Seal Validation.
     */
    fun startFestivalManager() {
        // Start the Festival Manager for the first time after the server has started
        // at the specified time, then invoke it automatically after every cycle.
        val fm = FestivalManager()
        setNextFestivalStart(Config.ALT_FESTIVAL_MANAGER_START + FESTIVAL_SIGNUP_TIME)
        _managerScheduledTask =
                ThreadPool.scheduleAtFixedRate(fm, Config.ALT_FESTIVAL_MANAGER_START, Config.ALT_FESTIVAL_CYCLE_LENGTH)

        _log.info("SevenSignsFestival: The first Festival of Darkness cycle begins in " + Config.ALT_FESTIVAL_MANAGER_START / 60000 + " minute(s).")
    }

    /**
     * Restores saved festival data, basic settings from the properties file and past high score data from the database.
     */
    fun restoreFestivalData() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var st = con.prepareStatement(RESTORE_FESTIVAL)
                var rset = st.executeQuery()

                while (rset.next()) {
                    val festivalCycle = rset.getInt("cycle")
                    var festivalId = rset.getInt("festivalId")
                    val cabal = rset.getString("cabal")

                    val set = StatsSet()
                    set["festivalId"] = festivalId.toDouble()
                    set.set("cabal", SevenSigns.CabalType.valueOf(cabal))
                    set["cycle"] = festivalCycle.toDouble()
                    set["date"] = rset.getString("date")
                    set["score"] = rset.getInt("score").toDouble()
                    set["members"] = rset.getString("members")

                    if (cabal.equals("dawn", ignoreCase = true))
                        festivalId += FESTIVAL_COUNT

                    val map: MutableMap<Int, StatsSet> = _festivalData[festivalCycle] ?: mutableMapOf()

                    map[festivalId] = set

                    _festivalData[festivalCycle] = map
                }

                rset.close()
                st.close()

                st = con.prepareStatement(RESTORE_FESTIVAL_2)
                rset = st.executeQuery()

                while (rset.next()) {
                    currentFestivalCycle = rset.getInt("festival_cycle")

                    for (i in 0 until FESTIVAL_COUNT)
                        _accumulatedBonuses.add(i, rset.getInt("accumulated_bonus" + i.toString()))
                }

                rset.close()
                st.close()
            }
        } catch (e: SQLException) {
            _log.severe("SevenSignsFestival: Failed to load configuration: $e")
        }

    }

    /**
     * Stores current festival data, basic settings to the properties file and past high score data to the database.<BR></BR>
     * <BR></BR>
     * If updateSettings = true, then all Seven Signs data is updated in the database.
     * @param updateSettings if true, will save Seven Signs status aswell.
     */
    fun saveFestivalData(updateSettings: Boolean) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statementUpdate = con.prepareStatement(UPDATE)
                val statementInsert = con.prepareStatement(INSERT)

                for (map in _festivalData.values) {
                    for (set in map.values) {
                        val festivalCycle = set.getInteger("cycle")
                        val festivalId = set.getInteger("festivalId")
                        val cabal = set.getString("cabal")

                        // Try to update an existing record.
                        statementUpdate.setLong(1, java.lang.Long.valueOf(set.getString("date")))
                        statementUpdate.setInt(2, set.getInteger("score"))
                        statementUpdate.setString(3, set.getString("members"))
                        statementUpdate.setInt(4, festivalCycle)
                        statementUpdate.setString(5, cabal)
                        statementUpdate.setInt(6, festivalId)

                        // If there was no record to update, assume it doesn't exist and add a new one, otherwise continue with the next record to store.
                        if (statementUpdate.executeUpdate() > 0)
                            continue

                        statementInsert.setInt(1, festivalId)
                        statementInsert.setString(2, cabal)
                        statementInsert.setInt(3, festivalCycle)
                        statementInsert.setLong(4, java.lang.Long.valueOf(set.getString("date")))
                        statementInsert.setInt(5, set.getInteger("score"))
                        statementInsert.setString(6, set.getString("members"))
                        statementInsert.execute()
                        statementInsert.clearParameters()
                    }
                }
                statementUpdate.close()
                statementInsert.close()

                // Updates Seven Signs DB data also, so call only if really necessary.
                if (updateSettings)
                    SevenSigns.saveSevenSignsStatus()
            }
        } catch (e: SQLException) {
            _log.severe("SevenSignsFestival: Failed to save configuration: $e")
        }

    }

    fun rewardHighestRanked() {
        for (i in 0 until FESTIVAL_COUNT) {
            val set = getOverallHighestScoreData(i)
            if (set != null) {
                for (playerName in set.getString("members").split(",").dropLastWhile { it.isEmpty() }.toTypedArray())
                    addReputationPointsForPartyMemberClan(playerName)
            }
        }
    }

    /**
     * Used to reset all festival data at the beginning of a new quest event period.
     * @param updateSettings
     */
    fun resetFestivalData(updateSettings: Boolean) {
        currentFestivalCycle = 0
        _signsCycle = SevenSigns.currentCycle

        // Set all accumulated bonuses back to 0.
        for (i in 0 until FESTIVAL_COUNT)
            _accumulatedBonuses[i] = 0

        _dawnFestivalParticipants.clear()
        _duskFestivalParticipants.clear()

        _dawnPreviousParticipants.clear()
        _duskPreviousParticipants.clear()

        _dawnFestivalScores.clear()
        _duskFestivalScores.clear()

        // Set up a new data set for the current cycle of festivals
        val map = HashMap<Int, StatsSet>()

        for (i in 0 until FESTIVAL_COUNT * 2) {
            var festivalId = i

            if (i >= FESTIVAL_COUNT)
                festivalId -= FESTIVAL_COUNT

            // Create a new StatsSet with "default" data for Dusk
            val set = StatsSet()
            set["festivalId"] = festivalId.toDouble()
            set["cycle"] = _signsCycle.toDouble()
            set["date"] = "0"
            set["score"] = 0.0
            set["members"] = ""

            if (i >= FESTIVAL_COUNT)
                set["cabal"] = SevenSigns.CabalType.DAWN
            else
                set["cabal"] = SevenSigns.CabalType.DUSK

            map[i] = set
        }

        // Add the newly created cycle data to the existing festival data, and
        // subsequently save it to the database.
        _festivalData[_signsCycle] = map

        saveFestivalData(updateSettings)

        // Remove any unused blood offerings from online players.
        for (player in World.players) {
            val bloodOfferings = player.inventory!!.getItemByItemId(FESTIVAL_OFFERING_ID)
            if (bloodOfferings != null)
                player.destroyItem("SevenSigns", bloodOfferings, null, false)
        }

        _log.info("SevenSignsFestival: Reinitialized engine for next competition period.")
    }

    fun setNextCycleStart() {
        _nextFestivalCycleStart = System.currentTimeMillis() + Config.ALT_FESTIVAL_CYCLE_LENGTH
    }

    fun setNextFestivalStart(milliFromNow: Long) {
        _nextFestivalStart = System.currentTimeMillis() + milliFromNow
    }

    /**
     * Returns the current festival ID and oracle ID that the specified player is in, but will return the default of {-1, -1} if the player is not found as a participant.
     * @param player
     * @return int[] playerFestivalInfo
     */
    fun getFestivalForPlayer(player: Player): IntArray {
        val playerFestivalInfo = intArrayOf(-1, -1)
        var festivalId = 0

        while (festivalId < FESTIVAL_COUNT) {
            var participants: List<Int>? = _dawnFestivalParticipants[festivalId]

            // If there are no participants in this festival, move on to the next.
            if (participants != null && participants.contains(player.objectId)) {
                playerFestivalInfo[0] = SevenSigns.CabalType.DAWN.ordinal
                playerFestivalInfo[1] = festivalId

                return playerFestivalInfo
            }

            participants = _duskFestivalParticipants[++festivalId]

            if (participants != null && participants.contains(player.objectId)) {
                playerFestivalInfo[0] = SevenSigns.CabalType.DUSK.ordinal
                playerFestivalInfo[1] = festivalId

                return playerFestivalInfo
            }

            festivalId++
        }

        // Return default data if the player is not found as a participant.
        return playerFestivalInfo
    }

    fun isParticipant(player: Player): Boolean {
        if (SevenSigns.isSealValidationPeriod)
            return false

        if (_managerInstance == null)
            return false

        for (participants in _dawnFestivalParticipants.values)
            if (participants != null && participants.contains(player.objectId))
                return true

        for (participants in _duskFestivalParticipants.values)
            if (participants != null && participants.contains(player.objectId))
                return true

        return false
    }

    fun getParticipants(oracle: SevenSigns.CabalType, festivalId: Int): List<Int> {
        return if (oracle === SevenSigns.CabalType.DAWN) _dawnFestivalParticipants[festivalId] ?: mutableListOf() else _duskFestivalParticipants[festivalId] ?: mutableListOf()

    }

    fun getPreviousParticipants(oracle: SevenSigns.CabalType, festivalId: Int): List<Int> {
        return if (oracle === SevenSigns.CabalType.DAWN) _dawnPreviousParticipants[festivalId] ?: mutableListOf() else _duskPreviousParticipants[festivalId] ?: mutableListOf()

    }

    fun setParticipants(oracle: SevenSigns.CabalType, festivalId: Int, festivalParty: Party?) {
        var participants: MutableList<Int>? = null

        if (festivalParty != null) {
            participants = ArrayList(festivalParty.membersCount)
            for (player in festivalParty.members)
                participants.add(player.objectId)
        }

        if (oracle === SevenSigns.CabalType.DAWN)
            _dawnFestivalParticipants[festivalId] = participants  ?: mutableListOf()
        else
            _duskFestivalParticipants[festivalId] = participants ?: mutableListOf()
    }

    fun updateParticipants(player: Player, festivalParty: Party?) {
        if (!isParticipant(player))
            return

        val playerFestInfo = getFestivalForPlayer(player)
        val oracle = SevenSigns.CabalType.VALUES[playerFestInfo[0]]
        val festivalId = playerFestInfo[1]

        if (festivalId > -1) {
            if (isFestivalInitialized) {
                val festivalInst = _managerInstance!!.getFestivalInstance(oracle, festivalId)

                // leader has left
                if (festivalParty == null) {
                    for (partyMemberObjId in getParticipants(oracle, festivalId)) {
                        val partyMember = World.getPlayer(partyMemberObjId) ?: continue

                        festivalInst!!.relocatePlayer(partyMember, true)
                    }
                } else
                    festivalInst!!.relocatePlayer(player, true)
            }

            setParticipants(oracle, festivalId, festivalParty)

            // Check on disconnect if min player in party
            if (festivalParty != null && festivalParty.membersCount < Config.ALT_FESTIVAL_MIN_PLAYER) {
                updateParticipants(player, null) // under minimum count
                festivalParty.removePartyMember(player, Party.MessageType.EXPELLED)
            }
        }
    }

    fun getFinalScore(oracle: SevenSigns.CabalType, festivalId: Int): Int {
        return if (oracle === SevenSigns.CabalType.DAWN) _dawnFestivalScores[festivalId] ?: 0 else _duskFestivalScores[festivalId] ?: 0

    }

    fun getHighestScore(oracle: SevenSigns.CabalType, festivalId: Int): Int {
        return getHighestScoreData(oracle, festivalId)?.getInteger("score") ?: 0
    }

    /**
     * Returns a stats set containing the highest score **this cycle** for the the specified cabal and associated festival ID.
     * @param oracle
     * @param festivalId
     * @return StatsSet festivalDat
     */
    fun getHighestScoreData(oracle: SevenSigns.CabalType, festivalId: Int): StatsSet? {
        var offsetId = festivalId

        if (oracle === SevenSigns.CabalType.DAWN)
            offsetId += 5

        return _festivalData[_signsCycle]?.get(offsetId)
    }

    /**
     * Returns a stats set containing the highest ever recorded score data for the specified festival.
     * @param festivalId
     * @return StatsSet result
     */
    fun getOverallHighestScoreData(festivalId: Int): StatsSet? {
        var set: StatsSet? = null
        var highestScore = 0

        for (map in _festivalData.values) {
            for (setToTest in map.values) {
                val currFestID = setToTest.getInteger("festivalId")
                val festivalScore = setToTest.getInteger("score")

                if (currFestID != festivalId)
                    continue

                if (festivalScore > highestScore) {
                    highestScore = festivalScore
                    set = setToTest
                }
            }
        }

        return set
    }

    /**
     * Set the final score details for the last participants of the specified festival data. Returns **true** if the score is higher than that previously recorded **this cycle**.
     * @param player
     * @param oracle
     * @param festival
     * @param offeringScore
     * @return boolean isHighestScore
     */
    fun setFinalScore(
        player: Player,
        oracle: SevenSigns.CabalType,
        festival: FestivalType,
        offeringScore: Int
    ): Boolean {
        val festivalId = festival.ordinal

        val currDawnHighScore = getHighestScore(SevenSigns.CabalType.DAWN, festivalId)
        val currDuskHighScore = getHighestScore(SevenSigns.CabalType.DUSK, festivalId)

        var thisCabalHighScore = 0
        var otherCabalHighScore = 0

        if (oracle === SevenSigns.CabalType.DAWN) {
            thisCabalHighScore = currDawnHighScore
            otherCabalHighScore = currDuskHighScore

            _dawnFestivalScores[festivalId] = offeringScore
        } else {
            thisCabalHighScore = currDuskHighScore
            otherCabalHighScore = currDawnHighScore

            _duskFestivalScores[festivalId] = offeringScore
        }

        val set = getHighestScoreData(oracle, festivalId) ?: StatsSet()

        // Check if this is the highest score for this level range so far for the player's cabal.
        if (offeringScore > thisCabalHighScore) {
            // If the current score is greater than that for the other cabal,
            // then they already have the points from this festival.
            if (thisCabalHighScore < otherCabalHighScore)
                return false

            val partyMembers = ArrayList<String>()
            for (partyMember in getPreviousParticipants(oracle, festivalId))
                PlayerInfoTable.getPlayerName(partyMember)?.let { partyMembers.add(it) }

            // Update the highest scores and party list.
            set["date"] = System.currentTimeMillis().toString()
            set["score"] = offeringScore.toDouble()
            set["members"] = partyMembers.joinToString(",")

            // Only add the score to the cabal's overall if it's higher than the other cabal's score.
            if (offeringScore > otherCabalHighScore) {
                // Give this cabal the festival points, while deducting them from the other.
                SevenSigns.addFestivalScore(oracle, festival.maxScore)

                _log.info("SevenSignsFestival: This is the highest score overall so far for the " + festival.name + " festival!")
            }

            saveFestivalData(true)

            return true
        }

        return false
    }

    fun getAccumulatedBonus(festivalId: Int): Int {
        return _accumulatedBonuses[festivalId]
    }

    fun addAccumulatedBonus(festivalId: Int, stoneType: Int, stoneAmount: Int) {
        var eachStoneBonus = 0

        when (stoneType) {
            SevenSigns.SEAL_STONE_BLUE_ID -> eachStoneBonus = SevenSigns.SEAL_STONE_BLUE_VALUE
            SevenSigns.SEAL_STONE_GREEN_ID -> eachStoneBonus = SevenSigns.SEAL_STONE_GREEN_VALUE
            SevenSigns.SEAL_STONE_RED_ID -> eachStoneBonus = SevenSigns.SEAL_STONE_RED_VALUE
        }

        val newTotalBonus = _accumulatedBonuses[festivalId] + stoneAmount * eachStoneBonus
        _accumulatedBonuses[festivalId] = newTotalBonus
    }

    /**
     * Calculate and return the proportion of the accumulated bonus for the festival where the player was in the winning party, if the winning party's cabal won the event. The accumulated bonus is then updated, with the player's share deducted.
     * @param player
     * @return playerBonus (the share of the bonus for the party)
     */
    fun distribAccumulatedBonus(player: Player): Int {
        if (SevenSigns.getPlayerCabal(player.objectId) !== SevenSigns.cabalHighestScore)
            return 0

        val map = _festivalData[_signsCycle] ?: return 0

        val playerName = player.name

        var playerBonus = 0
        for (set in map.values) {
            val members = set.getString("members")
            if (members.indexOf(playerName) > -1) {
                val festivalId = set.getInteger("festivalId")
                val numPartyMembers = members.split(",").dropLastWhile { it.isEmpty() }.toTypedArray().size
                val totalAccumBonus = _accumulatedBonuses[festivalId]

                playerBonus = totalAccumBonus / numPartyMembers
                _accumulatedBonuses[festivalId] = totalAccumBonus - playerBonus
                break
            }
        }

        return playerBonus
    }

    /**
     * Basically a wrapper-call to signal to increase the challenge of the specified festival.
     * @param oracle
     * @param festivalId
     * @return boolean isChalIncreased
     */
    fun increaseChallenge(oracle: SevenSigns.CabalType, festivalId: Int): Boolean {
        return _managerInstance!!.getFestivalInstance(oracle, festivalId)!!.increaseChallenge()
    }

    /**
     * Add zone for use with announcements in the oracles.
     * @param zone : Zone to be added.
     * @param dawn : Is dawn zone.
     */
    fun addPeaceZone(zone: PeaceZone, dawn: Boolean) {
        if (dawn) {
            if (_dawnPeace == null)
                _dawnPeace = ArrayList(2)

            if (!_dawnPeace!!.contains(zone))
                _dawnPeace!!.add(zone)
        } else {
            if (_duskPeace == null)
                _duskPeace = ArrayList(2)

            if (!_duskPeace!!.contains(zone))
                _duskPeace!!.add(zone)
        }
    }

    /**
     * Used to send a "shout" message to all players currently present in an Oracle. Primarily used for Festival Guide and Witch related speech.
     * @param senderName
     * @param message
     */
    fun sendMessageToAll(senderName: String, message: String) {
        val cs = CreatureSay(0, Say2.SHOUT, senderName, message)

        if (_dawnPeace != null)
            for (zone in _dawnPeace!!)
                zone.broadcastPacket(cs)

        if (_duskPeace != null)
            for (zone in _duskPeace!!)
                zone.broadcastPacket(cs)
    }

    /**
     * The FestivalManager class is the main runner of all the festivals. It is used for easier integration and management of all running festivals.
     * @author Tempy
     */
    private class FestivalManager : Runnable {
        var _festivalInstances: MutableMap<Int, L2DarknessFestival> = HashMap()

        init {
            _managerInstance = this

            // Increment the cycle counter.
            currentFestivalCycle++

            // Set the next start timers.
            setNextCycleStart()
            setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME)
        }

        @Synchronized
        override fun run() {
            try {
                // The manager shouldn't be running if Seal Validation is in effect.
                if (SevenSigns.isSealValidationPeriod)
                    return

                // If the next period is due to start before the end of this
                // festival cycle, then don't run it.
                if (SevenSigns.milliToPeriodChange < Config.ALT_FESTIVAL_CYCLE_LENGTH)
                    return

                if (minsToNextFestival == 2)
                    sendMessageToAll("Festival Guide", "The main event will start in 2 minutes. Please register now.")

                // Stand by until the allowed signup period has elapsed.
                try {
                    Thread.sleep(FESTIVAL_SIGNUP_TIME)
                } catch (e: InterruptedException) {
                }

                // Clear past participants, they can no longer register their score if not done so already.
                _dawnPreviousParticipants.clear()
                _duskPreviousParticipants.clear()

                // Get rid of random monsters that avoided deletion after last festival
                for (festivalInst in _festivalInstances.values)
                    festivalInst.unspawnMobs()

                // Start only if participants signed up
                _noPartyRegister = true

                while (_noPartyRegister) {
                    if (_duskFestivalParticipants.isEmpty() && _dawnFestivalParticipants.isEmpty()) {
                        try {
                            setNextCycleStart()
                            setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME)
                            Thread.sleep(Config.ALT_FESTIVAL_CYCLE_LENGTH - FESTIVAL_SIGNUP_TIME)
                            for (festivalInst in _festivalInstances.values) {
                                if (!festivalInst._npcInsts!!.isEmpty())
                                    festivalInst.unspawnMobs()
                            }
                        } catch (e: InterruptedException) {
                        }

                    } else
                        _noPartyRegister = false
                }

                /* INITIATION */
                // Set the festival timer to 0, as it is just beginning.
                var elapsedTime: Long = 0

                // Create the instances for the festivals in both Oracles,
                // but only if they have participants signed up for them.
                for (i in 0 until FESTIVAL_COUNT) {
                    if (_duskFestivalParticipants[i] != null)
                        _festivalInstances[10 + i] = L2DarknessFestival(SevenSigns.CabalType.DUSK, i)

                    if (_dawnFestivalParticipants[i] != null)
                        _festivalInstances[20 + i] = L2DarknessFestival(SevenSigns.CabalType.DAWN, i)
                }

                // Prevent future signups while festival is in progress.
                isFestivalInitialized = true

                setNextFestivalStart(Config.ALT_FESTIVAL_CYCLE_LENGTH)
                sendMessageToAll("Festival Guide", "The main event is now starting.")

                // Stand by for a short length of time before starting the festival.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_FIRST_SPAWN)
                } catch (e: InterruptedException) {
                }

                elapsedTime = Config.ALT_FESTIVAL_FIRST_SPAWN

                // Participants can now opt to increase the challenge, if desired.
                isFestivalInProgress = true

                /* PROPOGATION */
                // Sequentially set all festivals to begin, spawn the Festival Witch and notify participants.
                for (festivalInst in _festivalInstances.values) {
                    festivalInst.festivalStart()
                    festivalInst.sendMessageToParticipants("The main event is now starting.")
                }

                // After a short time period, move all idle spawns to the center of the arena.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN)
                } catch (e: InterruptedException) {
                }

                elapsedTime += Config.ALT_FESTIVAL_FIRST_SWARM - Config.ALT_FESTIVAL_FIRST_SPAWN

                for (festivalInst in _festivalInstances.values)
                    festivalInst.moveMonstersToCenter()

                // Stand by until the time comes for the second spawn.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM)
                } catch (e: InterruptedException) {
                }

                // Spawn an extra set of monsters (archers) on the free platforms with
                // a faster respawn when killed.
                for (festivalInst in _festivalInstances.values) {
                    festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN / 2, 2)

                    val end = (Config.ALT_FESTIVAL_LENGTH - Config.ALT_FESTIVAL_SECOND_SPAWN) / 60000
                    festivalInst.sendMessageToParticipants("The Festival of Darkness will end in $end minute(s).")
                }

                elapsedTime += Config.ALT_FESTIVAL_SECOND_SPAWN - Config.ALT_FESTIVAL_FIRST_SWARM

                // After another short time period, again move all idle spawns to the center of the arena.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN)
                } catch (e: InterruptedException) {
                }

                for (festivalInst in _festivalInstances.values)
                    festivalInst.moveMonstersToCenter()

                elapsedTime += Config.ALT_FESTIVAL_SECOND_SWARM - Config.ALT_FESTIVAL_SECOND_SPAWN

                // Stand by until the time comes for the chests to be spawned.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM)
                } catch (e: InterruptedException) {
                }

                // Spawn the festival chests, which enable the team to gain greater rewards
                // for each chest they kill.
                for (festivalInst in _festivalInstances.values) {
                    festivalInst.spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 3)
                    festivalInst.sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.") // FIXME What is the correct npcString?
                }

                elapsedTime += Config.ALT_FESTIVAL_CHEST_SPAWN - Config.ALT_FESTIVAL_SECOND_SWARM

                // Stand by and wait until it's time to end the festival.
                try {
                    Thread.sleep(Config.ALT_FESTIVAL_LENGTH - elapsedTime)
                } catch (e: InterruptedException) {
                }

                // Participants can no longer opt to increase the challenge, as the festival will soon close.
                isFestivalInProgress = false

                /* TERMINATION */
                // Sequentially begin the ending sequence for all running festivals.
                for (festivalInst in _festivalInstances.values)
                    festivalInst.festivalEnd()

                // Clear the participants list for the next round of signups.
                _dawnFestivalParticipants.clear()
                _duskFestivalParticipants.clear()

                // Allow signups for the next festival cycle.
                isFestivalInitialized = false

                sendMessageToAll("Festival Witch", "That will do! I'll move you to the outside soon.")
            } catch (e: Exception) {
                _log.warning(e.message)
            }

        }

        /**
         * Returns the running instance of a festival for the given Oracle and festivalID. <BR></BR>
         * A <B>null</B> value is returned if there are no participants in that festival.
         * @param oracle
         * @param festivalId
         * @return L2DarknessFestival festivalInst
         */
        fun getFestivalInstance(oracle: SevenSigns.CabalType, festivalId: Int): L2DarknessFestival? {
            var festivalId = festivalId
            if (!isFestivalInitialized)
                return null

            /*
			 * Compute the offset if a Dusk instance is required. ID: 0 1 2 3 4 Dusk 1: 10 11 12 13 14 Dawn 2: 20 21 22 23 24
			 */

            festivalId += if (oracle === SevenSigns.CabalType.DUSK) 10 else 20
            return _festivalInstances[festivalId]
        }
    }

    /**
     * Each running festival is represented by an L2DarknessFestival class. It contains all the spawn information and data for the running festival. All festivals are managed by the FestivalManager class, which must be initialized first.
     * @author Tempy
     */
    private class L2DarknessFestival(val _cabal: SevenSigns.CabalType, val _levelRange: Int) {
        var _challengeIncreased: Boolean = false

        private var _startLocation: FestivalSpawn? = null
        private var _witchSpawn: FestivalSpawn? = null

        private var _witchInst: Npc? = null
        val _npcInsts: MutableList<FestivalMonster>?

        private var _participants: MutableList<Int> = mutableListOf()
        private val _originalLocations: MutableMap<Int, FestivalSpawn>

        init {
            _originalLocations = HashMap()
            _npcInsts = ArrayList()

            if (_cabal === SevenSigns.CabalType.DAWN) {
                _participants = _dawnFestivalParticipants[_levelRange] ?: mutableListOf()
                _witchSpawn = FestivalSpawn(FESTIVAL_DAWN_WITCH_SPAWNS[_levelRange])
                _startLocation = FestivalSpawn(FESTIVAL_DAWN_PLAYER_SPAWNS[_levelRange])
            } else {
                _participants = _duskFestivalParticipants[_levelRange] ?: mutableListOf()
                _witchSpawn = FestivalSpawn(FESTIVAL_DUSK_WITCH_SPAWNS[_levelRange])
                _startLocation = FestivalSpawn(FESTIVAL_DUSK_PLAYER_SPAWNS[_levelRange])
            }

            festivalInit()
        }

        fun festivalInit() {
            var isPositive: Boolean

            // Teleport all players to arena and notify them.
            if (!_participants.isEmpty()) {
                for (participantObjId in _participants) {
                    val participant = World.getPlayer(participantObjId) ?: continue

                    _originalLocations[participantObjId] =
                            FestivalSpawn(participant.x, participant.y, participant.z, participant.heading)

                    // Randomize the spawn point around the specific centerpoint for each player.
                    var x = _startLocation!!._x
                    var y = _startLocation!!._y

                    isPositive = Rnd[2] == 1

                    if (isPositive) {
                        x += Rnd[FESTIVAL_MAX_OFFSET_X]
                        y += Rnd[FESTIVAL_MAX_OFFSET_Y]
                    } else {
                        x -= Rnd[FESTIVAL_MAX_OFFSET_X]
                        y -= Rnd[FESTIVAL_MAX_OFFSET_Y]
                    }

                    participant.ai.setIntention(CtrlIntention.IDLE)
                    participant.teleToLocation(x, y, _startLocation!!._z, 20)

                    // Remove all buffs from all participants on entry. Works like the skill Cancel.
                    participant.stopAllEffectsExceptThoseThatLastThroughDeath()

                    // Remove any stray blood offerings in inventory
                    val bloodOfferings = participant.inventory!!.getItemByItemId(FESTIVAL_OFFERING_ID)
                    if (bloodOfferings != null)
                        participant.destroyItem("SevenSigns", bloodOfferings, null, true)
                }
            }

            val witchTemplate = NpcData.getTemplate(_witchSpawn!!._npcId)

            // Spawn the festival witch for this arena
            try {
                val npcSpawn = L2Spawn(witchTemplate)

                npcSpawn.setLoc(_witchSpawn!!._x, _witchSpawn!!._y, _witchSpawn!!._z, _witchSpawn!!._heading)
                npcSpawn.respawnDelay = 1

                // Needed as doSpawn() is required to be called also for the NpcInstance it returns.
                npcSpawn.setRespawnState(true)

                SpawnTable.addNewSpawn(npcSpawn, false)
                _witchInst = npcSpawn.doSpawn(false)
            } catch (e: Exception) {
                _log.warning("SevenSignsFestival: Error while spawning Festival Witch ID " + _witchSpawn!!._npcId + ": " + e)
            }

            // Make it appear as though the Witch has apparated there.
            var msu = MagicSkillUse(_witchInst!!, _witchInst!!, 2003, 1, 1, 0)
            _witchInst!!.broadcastPacket(msu)

            // And another one...:D
            msu = MagicSkillUse(_witchInst!!, _witchInst!!, 2133, 1, 1, 0)
            _witchInst!!.broadcastPacket(msu)

            // Send a message to all participants from the witch.
            sendMessageToParticipants("The festival will begin in 2 minutes.")
        }

        fun festivalStart() {
            spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 0)
        }

        fun moveMonstersToCenter() {
            for (festivalMob in _npcInsts!!) {
                if (festivalMob.isDead)
                    continue

                // Only move monsters that are idle or doing their usual functions.
                val currIntention = festivalMob.ai.desire.intention

                if (currIntention != CtrlIntention.IDLE && currIntention != CtrlIntention.ACTIVE)
                    continue

                var x = _startLocation!!._x
                var y = _startLocation!!._y

                if (Rnd.nextBoolean()) {
                    x += Rnd[FESTIVAL_MAX_OFFSET_X]
                    y += Rnd[FESTIVAL_MAX_OFFSET_Y]
                } else {
                    x -= Rnd[FESTIVAL_MAX_OFFSET_X]
                    y -= Rnd[FESTIVAL_MAX_OFFSET_Y]
                }

                festivalMob.setRunning()
                festivalMob.ai.setIntention(CtrlIntention.MOVE_TO, Location(x, y, _startLocation!!._z))
            }
        }

        /**
         * Used to spawn monsters unique to the festival. <BR></BR>
         * Valid SpawnTypes:<BR></BR>
         * 0 - All Primary Monsters (starting monsters) <BR></BR>
         * 1 - Same as 0, but without archers/marksmen. (used for challenge increase) <BR></BR>
         * 2 - Secondary Monsters (archers) <BR></BR>
         * 3 - Festival Chests
         * @param respawnDelay
         * @param spawnType
         */
        fun spawnFestivalMonsters(respawnDelay: Int, spawnType: Int) {
            var _npcSpawns: Array<IntArray>? = null

            when (spawnType) {
                0, 1 -> _npcSpawns =
                        if (_cabal === SevenSigns.CabalType.DAWN) FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] else FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange]

                2 -> _npcSpawns =
                        if (_cabal === SevenSigns.CabalType.DAWN) FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] else FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange]

                3 -> _npcSpawns =
                        if (_cabal === SevenSigns.CabalType.DAWN) FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] else FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange]
            }

            if (_npcSpawns != null) {
                for (_npcSpawn in _npcSpawns) {
                    val currSpawn = FestivalSpawn(_npcSpawn)

                    // Only spawn archers/marksmen if specified to do so.
                    if (spawnType == 1 && isFestivalArcher(currSpawn._npcId))
                        continue

                    val npcTemplate = NpcData.getTemplate(currSpawn._npcId)

                    try {
                        val npcSpawn = L2Spawn(npcTemplate)
                        npcSpawn.setLoc(currSpawn._x, currSpawn._y, currSpawn._z, Rnd[65536])
                        npcSpawn.respawnDelay = respawnDelay
                        npcSpawn.setRespawnState(true)

                        SpawnTable.addNewSpawn(npcSpawn, false)
                        val festivalMob = npcSpawn.doSpawn(false) as FestivalMonster

                        // Set the offering bonus to 2x or 5x the amount per kill, if this spawn is part of an increased challenge or is a festival chest.
                        if (spawnType == 1)
                            festivalMob.setOfferingBonus(2)
                        else if (spawnType == 3)
                            festivalMob.setOfferingBonus(5)

                        _npcInsts!!.add(festivalMob)
                    } catch (e: Exception) {
                        _log.warning("SevenSignsFestival: Error while spawning NPC ID " + currSpawn._npcId + ": " + e)
                    }

                }
            }
        }

        fun increaseChallenge(): Boolean {
            if (_challengeIncreased)
                return false

            // Set this flag to true to make sure that this can only be done once.
            _challengeIncreased = true

            // Spawn more festival monsters, but this time with a twist.
            spawnFestivalMonsters(FESTIVAL_DEFAULT_RESPAWN, 1)
            return true
        }

        fun sendMessageToParticipants(message: String) {
            if (!_participants.isEmpty())
                _witchInst!!.broadcastPacket(CreatureSay(_witchInst!!.objectId, Say2.ALL, "Festival Witch", message))
        }

        fun festivalEnd() {
            if (!_participants.isEmpty()) {
                for (participantObjId in _participants) {
                    val participant = World.getPlayer(participantObjId) ?: continue

                    relocatePlayer(participant, false)
                    participant.sendMessage("The festival has ended. Your party leader must now register your score before the next festival takes place.")
                }

                if (_cabal === SevenSigns.CabalType.DAWN)
                    _dawnPreviousParticipants[_levelRange] = _participants
                else
                    _duskPreviousParticipants[_levelRange] = _participants
            }
            _participants = mutableListOf()

            unspawnMobs()
        }

        fun unspawnMobs() {
            // Delete all the NPCs in the current festival arena.
            if (_witchInst != null) {
                _witchInst!!.spawn.setRespawnState(false)
                _witchInst!!.deleteMe()
                SpawnTable.deleteSpawn(_witchInst!!.spawn, false)
            }

            if (_npcInsts != null)
                for (monsterInst in _npcInsts)
                    if (monsterInst != null) {
                        monsterInst.spawn.setRespawnState(false)
                        monsterInst.deleteMe()
                        SpawnTable.deleteSpawn(monsterInst.spawn, false)
                    }
        }

        fun relocatePlayer(participant: Player?, isRemoving: Boolean) {
            if (participant == null)
                return

            try {
                val origPosition = _originalLocations[participant.objectId]

                if (isRemoving)
                    _originalLocations.remove(participant.objectId)

                participant.ai.setIntention(CtrlIntention.IDLE)
                participant.teleToLocation(origPosition!!._x, origPosition._y, origPosition._z, 20)
                participant.sendMessage("You have been removed from the festival arena.")
            } catch (e: Exception) {
                // If an exception occurs, just move the player to the nearest town.
                participant.teleToLocation(MapRegionData.TeleportType.TOWN)
                participant.sendMessage("You have been removed from the festival arena.")
            }

        }
    }

    private class FestivalSpawn {
        val _x: Int
        val _y: Int
        val _z: Int
        val _heading: Int
        val _npcId: Int

        constructor(x: Int, y: Int, z: Int, heading: Int) {
            _x = x
            _y = y
            _z = z

            // Generate a random heading if no positive one given.
            _heading = if (heading < 0) Rnd[65536] else heading

            _npcId = -1
        }

        constructor(spawnData: IntArray) {
            _x = spawnData[0]
            _y = spawnData[1]
            _z = spawnData[2]

            _heading = if (spawnData[3] < 0) Rnd[65536] else spawnData[3]

            if (spawnData.size > 4)
                _npcId = spawnData[4]
            else
                _npcId = -1
        }
    }
}