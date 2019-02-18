package com.l2kt.gameserver.instancemanager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.instancemanager.AutoSpawnManager.AutoSpawnInstance
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SSQInfo
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet
import java.sql.SQLException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

object SevenSigns {

    private val _nextPeriodChange = Calendar.getInstance()
    private var _lastSave = Calendar.getInstance()

    lateinit var currentPeriod: PeriodType
    var currentCycle: Int = 0
    private var _dawnStoneScore: Double = 0.0
    private var _duskStoneScore: Double = 0.0
    private var _dawnFestivalScore: Int = 0
    private var _duskFestivalScore: Int = 0
    private var _previousWinner: CabalType? = null

    private val _playersData = HashMap<Int, StatsSet>()

    private val _sealOwners = HashMap<SealType, CabalType>()
    private val _duskScores = HashMap<SealType, Int>()
    private val _dawnScores = HashMap<SealType, Int>()

    private val _log = Logger.getLogger(SevenSigns::class.java.name)

    // SQL queries
    private const val LOAD_DATA =
        "SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs"
    private const val LOAD_STATUS = "SELECT * FROM seven_signs_status WHERE id=0"
    private const val INSERT_PLAYER = "INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)"
    private const val UPDATE_PLAYER =
        "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?"
    private const val UPDATE_STATUS =
        "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, " + "dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, " + "avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, " + "strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, " + "festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?," + "accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0"

    // Seven Signs constants
    const val SEVEN_SIGNS_DATA_FILE = "config/signs.properties"
    const val SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/"

    const val PERIOD_START_HOUR = 18
    const val PERIOD_START_MINS = 0
    const val PERIOD_START_DAY = Calendar.MONDAY

    // The quest event and seal validation periods last for approximately one week with a 15 minutes "interval" period sandwiched between them.
    const val PERIOD_MINOR_LENGTH = 900000
    val PERIOD_MAJOR_LENGTH = 604800000 - PERIOD_MINOR_LENGTH

    const val RECORD_SEVEN_SIGNS_ID = 5707
    const val CERTIFICATE_OF_APPROVAL_ID = 6388
    const val RECORD_SEVEN_SIGNS_COST = 500
    const val ADENA_JOIN_DAWN_COST = 50000

    // NPCs related constants
    const val ORATOR_NPC_ID = 31094
    const val PREACHER_NPC_ID = 31093
    const val MAMMON_MERCHANT_ID = 31113
    const val MAMMON_BLACKSMITH_ID = 31126
    const val MAMMON_MARKETEER_ID = 31092
    const val LILITH_NPC_ID = 25283
    const val ANAKIM_NPC_ID = 25286
    const val CREST_OF_DAWN_ID = 31170
    const val CREST_OF_DUSK_ID = 31171

    // Seal Stone related constants
    const val SEAL_STONE_BLUE_ID = 6360
    const val SEAL_STONE_GREEN_ID = 6361
    const val SEAL_STONE_RED_ID = 6362

    const val SEAL_STONE_BLUE_VALUE = 3
    const val SEAL_STONE_GREEN_VALUE = 5
    const val SEAL_STONE_RED_VALUE = 10

    // AutoSpawn instances
    private var _merchantSpawn: AutoSpawnInstance? = null
    private var _blacksmithSpawn: AutoSpawnInstance? = null
    private var _lilithSpawn: AutoSpawnInstance? = null
    private var _anakimSpawn: AutoSpawnInstance? = null
    private var _crestofdawnspawns: Map<Int, AutoSpawnInstance>? = null
    private var _crestofduskspawns: Map<Int, AutoSpawnInstance>? = null
    private var _oratorSpawns: Map<Int, AutoSpawnInstance>? = null
    private var _preacherSpawns: Map<Int, AutoSpawnInstance>? = null
    private var _marketeerSpawns: Map<Int, AutoSpawnInstance>? = null

    @JvmStatic fun calcScore(blueCount: Int, greenCount: Int, redCount: Int): Int {
        return blueCount * SEAL_STONE_BLUE_VALUE + greenCount * SEAL_STONE_GREEN_VALUE + redCount * SEAL_STONE_RED_VALUE
    }

    // If we hit next week, just turn back 1 week
    // Because of the short duration of this period, just check it from last save
    // Because of previous "date" column usage, check only if it already contains usable data for us
    private val isNextPeriodChangeInPast: Boolean
        get() {
            val lastPeriodChange = Calendar.getInstance()
            when (currentPeriod) {
                SevenSigns.PeriodType.SEAL_VALIDATION, SevenSigns.PeriodType.COMPETITION -> {
                    lastPeriodChange.set(Calendar.DAY_OF_WEEK, PERIOD_START_DAY)
                    lastPeriodChange.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR)
                    lastPeriodChange.set(Calendar.MINUTE, PERIOD_START_MINS)
                    lastPeriodChange.set(Calendar.SECOND, 0)
                    if (Calendar.getInstance().before(lastPeriodChange))
                        lastPeriodChange.add(Calendar.HOUR, -24 * 7)
                }

                SevenSigns.PeriodType.RECRUITING, SevenSigns.PeriodType.RESULTS -> lastPeriodChange.timeInMillis = _lastSave.timeInMillis +
                        PERIOD_MINOR_LENGTH
            }
            return if (_lastSave.timeInMillis > 7 && _lastSave.before(lastPeriodChange)) true else false

        }

    private val daysToPeriodChange: Int
        get() {
            val numDays = _nextPeriodChange.get(Calendar.DAY_OF_WEEK) - PERIOD_START_DAY

            return if (numDays < 0) 0 - numDays else 7 - numDays

        }

    val milliToPeriodChange: Long
        get() {
            val currTimeMillis = System.currentTimeMillis()
            val changeTimeMillis = _nextPeriodChange.timeInMillis

            return changeTimeMillis - currTimeMillis
        }

    val isRecruitingPeriod: Boolean
        get() = currentPeriod == PeriodType.RECRUITING

    val isSealValidationPeriod: Boolean
        get() = currentPeriod == PeriodType.SEAL_VALIDATION

    val isCompResultsPeriod: Boolean
        get() = currentPeriod == PeriodType.RESULTS

    val cabalHighestScore: CabalType
        get() {
            val duskScore = getCurrentScore(CabalType.DUSK)
            val dawnScore = getCurrentScore(CabalType.DAWN)

            if (duskScore == dawnScore)
                return CabalType.NORMAL

            return if (duskScore > dawnScore) CabalType.DUSK else CabalType.DAWN

        }

    val sealOwners: Map<SealType, CabalType>
        get() = _sealOwners

    enum class CabalType constructor(val shortName: String, val fullName: String) {
        NORMAL("No Cabal", "No Cabal"),
        DUSK("dusk", "Revolutionaries of Dusk"),
        DAWN("dawn", "Lords of Dawn");


        companion object {
            val VALUES = values()
        }
    }

    enum class SealType private constructor(val shortName: String, val fullName: String) {
        NONE("", ""),
        AVARICE("Avarice", "Seal of Avarice"),
        GNOSIS("Gnosis", "Seal of Gnosis"),
        STRIFE("Strife", "Seal of Strife");


        companion object {

            val VALUES = values()
        }
    }

    enum class PeriodType constructor(val periodTypeName: String, val messageId: SystemMessageId) {
        RECRUITING("Quest Event Initialization", SystemMessageId.PREPARATIONS_PERIOD_BEGUN),
        COMPETITION("Competition (Quest Event)", SystemMessageId.COMPETITION_PERIOD_BEGUN),
        RESULTS("Quest Event Results", SystemMessageId.RESULTS_PERIOD_BEGUN),
        SEAL_VALIDATION("Seal Validation", SystemMessageId.VALIDATION_PERIOD_BEGUN);


        companion object {

            val VALUES = values()
        }
    }

    init {
        restoreSevenSignsData()

        _log.info("SevenSigns: Currently on " + currentPeriod?.name + " period.")
        initializeSeals()

        val winningCabal = cabalHighestScore
        if (isSealValidationPeriod) {
            if (winningCabal == CabalType.NORMAL)
                _log.info("SevenSigns: The competition ended with a tie last week.")
            else
                _log.info("SevenSigns: " + winningCabal.fullName + " were victorious last week.")
        } else if (winningCabal == CabalType.NORMAL)
            _log.info("SevenSigns: The competition will end in a tie this week.")
        else
            _log.info("SevenSigns: " + winningCabal.fullName + " are leading this week.")

        var milliToChange: Long = 0
        if (isNextPeriodChangeInPast)
            _log.info("SevenSigns: Next period change was in the past, changing periods now.")
        else {
            setCalendarForNextPeriodChange()
            milliToChange = milliToPeriodChange
        }

        // Schedule a time for the next period change.
        ThreadPool.schedule(SevenSignsPeriodChange(), milliToChange)

        val numSecs = (milliToChange / 1000 % 60).toDouble()
        var countDown = (milliToChange / 1000 - numSecs) / 60
        val numMins = Math.floor(countDown % 60).toInt()
        countDown = (countDown - numMins) / 60
        val numHours = Math.floor(countDown % 24).toInt()
        val numDays = Math.floor((countDown - numHours) / 24).toInt()

        _log.info("SevenSigns: Next period begins in $numDays days, $numHours hours and $numMins mins.")
    }

    /**
     * Registers all random spawns and auto-chats for Seven Signs NPCs, along with spawns for the Preachers of Doom and Orators of Revelations at the beginning of the Seal Validation period.
     */
    fun spawnSevenSignsNPC() {
        _merchantSpawn = AutoSpawnManager.getAutoSpawnInstance(MAMMON_MERCHANT_ID, false)
        _blacksmithSpawn = AutoSpawnManager.getAutoSpawnInstance(MAMMON_BLACKSMITH_ID, false)
        _marketeerSpawns = AutoSpawnManager.getAutoSpawnInstances(MAMMON_MARKETEER_ID)
        _lilithSpawn = AutoSpawnManager.getAutoSpawnInstance(LILITH_NPC_ID, false)
        _anakimSpawn = AutoSpawnManager.getAutoSpawnInstance(ANAKIM_NPC_ID, false)
        _crestofdawnspawns = AutoSpawnManager.getAutoSpawnInstances(CREST_OF_DAWN_ID)
        _crestofduskspawns = AutoSpawnManager.getAutoSpawnInstances(CREST_OF_DUSK_ID)
        _oratorSpawns = AutoSpawnManager.getAutoSpawnInstances(ORATOR_NPC_ID)
        _preacherSpawns = AutoSpawnManager.getAutoSpawnInstances(PREACHER_NPC_ID)

        if (isSealValidationPeriod || isCompResultsPeriod) {
            for (spawnInst in _marketeerSpawns!!.values)
                AutoSpawnManager.setSpawnActive(spawnInst, true)

            val winningCabal = cabalHighestScore

            val gnosisSealOwner = getSealOwner(SealType.GNOSIS)
            if (gnosisSealOwner == winningCabal && gnosisSealOwner != CabalType.NORMAL) {
                if (!Config.ANNOUNCE_MAMMON_SPAWN)
                    _blacksmithSpawn!!.setBroadcast(false)

                if (!AutoSpawnManager.getAutoSpawnInstance(
                        _blacksmithSpawn!!.objectId,
                        true
                    )!!.isSpawnActive
                )
                    AutoSpawnManager.setSpawnActive(_blacksmithSpawn, true)

                for (spawnInst in _oratorSpawns!!.values)
                    if (!AutoSpawnManager.getAutoSpawnInstance(spawnInst.objectId, true)!!.isSpawnActive)
                        AutoSpawnManager.setSpawnActive(spawnInst, true)

                for (spawnInst in _preacherSpawns!!.values)
                    if (!AutoSpawnManager.getAutoSpawnInstance(spawnInst.objectId, true)!!.isSpawnActive)
                        AutoSpawnManager.setSpawnActive(spawnInst, true)
            } else {
                AutoSpawnManager.setSpawnActive(_blacksmithSpawn, false)

                for (spawnInst in _oratorSpawns!!.values)
                    AutoSpawnManager.setSpawnActive(spawnInst, false)

                for (spawnInst in _preacherSpawns!!.values)
                    AutoSpawnManager.setSpawnActive(spawnInst, false)
            }

            val avariceSealOwner = getSealOwner(SealType.AVARICE)
            if (avariceSealOwner == winningCabal && avariceSealOwner != CabalType.NORMAL) {
                if (!Config.ANNOUNCE_MAMMON_SPAWN)
                    _merchantSpawn!!.setBroadcast(false)

                if (!AutoSpawnManager.getAutoSpawnInstance(
                        _merchantSpawn!!.objectId,
                        true
                    )!!.isSpawnActive
                )
                    AutoSpawnManager.setSpawnActive(_merchantSpawn, true)

                when (winningCabal) {
                    SevenSigns.CabalType.DAWN -> {
                        // Spawn Lilith, unspawn Anakim.
                        if (!AutoSpawnManager.getAutoSpawnInstance(
                                _lilithSpawn!!.objectId,
                                true
                            )!!.isSpawnActive
                        )
                            AutoSpawnManager.setSpawnActive(_lilithSpawn, true)

                        AutoSpawnManager.setSpawnActive(_anakimSpawn, false)

                        // Spawn Dawn crests.
                        for (dawnCrest in _crestofdawnspawns!!.values) {
                            if (!AutoSpawnManager.getAutoSpawnInstance(
                                    dawnCrest.objectId,
                                    true
                                )!!.isSpawnActive
                            )
                                AutoSpawnManager.setSpawnActive(dawnCrest, true)
                        }

                        // Unspawn Dusk crests.
                        for (duskCrest in _crestofduskspawns!!.values)
                            AutoSpawnManager.setSpawnActive(duskCrest, false)
                    }

                    SevenSigns.CabalType.DUSK -> {
                        // Spawn Anakim, unspawn Lilith.
                        if (!AutoSpawnManager.getAutoSpawnInstance(
                                _anakimSpawn!!.objectId,
                                true
                            )!!.isSpawnActive
                        )
                            AutoSpawnManager.setSpawnActive(_anakimSpawn, true)

                        AutoSpawnManager.setSpawnActive(_lilithSpawn, false)

                        // Spawn Dusk crests.
                        for (duskCrest in _crestofduskspawns!!.values) {
                            if (!AutoSpawnManager.getAutoSpawnInstance(
                                    duskCrest.objectId,
                                    true
                                )!!.isSpawnActive
                            )
                                AutoSpawnManager.setSpawnActive(duskCrest, true)
                        }

                        // Unspawn Dawn crests.
                        for (dawnCrest in _crestofdawnspawns!!.values)
                            AutoSpawnManager.setSpawnActive(dawnCrest, false)
                    }
                }
            } else {
                // Unspawn merchant of mammon, Lilith, Anakim.
                AutoSpawnManager.setSpawnActive(_merchantSpawn, false)
                AutoSpawnManager.setSpawnActive(_lilithSpawn, false)
                AutoSpawnManager.setSpawnActive(_anakimSpawn, false)

                // Unspawn Dawn crests.
                for (dawnCrest in _crestofdawnspawns!!.values)
                    AutoSpawnManager.setSpawnActive(dawnCrest, false)

                // Unspawn Dusk crests.
                for (duskCrest in _crestofduskspawns!!.values)
                    AutoSpawnManager.setSpawnActive(duskCrest, false)
            }
        } else {
            // Unspawn merchant of mammon, Lilith, Anakim.
            AutoSpawnManager.setSpawnActive(_merchantSpawn, false)
            AutoSpawnManager.setSpawnActive(_blacksmithSpawn, false)
            AutoSpawnManager.setSpawnActive(_lilithSpawn, false)
            AutoSpawnManager.setSpawnActive(_anakimSpawn, false)

            // Unspawn Dawn crests.
            for (dawnCrest in _crestofdawnspawns!!.values)
                AutoSpawnManager.setSpawnActive(dawnCrest, false)

            // Unspawn Dusk crests.
            for (duskCrest in _crestofduskspawns!!.values)
                AutoSpawnManager.setSpawnActive(duskCrest, false)

            // Unspawn Orators.
            for (spawnInst in _oratorSpawns!!.values)
                AutoSpawnManager.setSpawnActive(spawnInst, false)

            // Unspawn Preachers.
            for (spawnInst in _preacherSpawns!!.values)
                AutoSpawnManager.setSpawnActive(spawnInst, false)

            // Unspawn marketeer of mammon.
            for (spawnInst in _marketeerSpawns!!.values)
                AutoSpawnManager.setSpawnActive(spawnInst, false)
        }
    }

    /**
     * Calculate the number of days until the next period.<BR></BR>
     * A period starts at 18:00 pm (local time), like on official servers.
     */
    fun setCalendarForNextPeriodChange() {
        when (currentPeriod) {
            SevenSigns.PeriodType.SEAL_VALIDATION, SevenSigns.PeriodType.COMPETITION -> {
                var daysToChange = daysToPeriodChange

                if (daysToChange == 7)
                    if (_nextPeriodChange.get(Calendar.HOUR_OF_DAY) < PERIOD_START_HOUR)
                        daysToChange = 0
                    else if (_nextPeriodChange.get(Calendar.HOUR_OF_DAY) == PERIOD_START_HOUR && _nextPeriodChange.get(
                            Calendar.MINUTE
                        ) < PERIOD_START_MINS
                    )
                        daysToChange = 0

                if (daysToChange > 0)
                    _nextPeriodChange.add(Calendar.DATE, daysToChange)

                _nextPeriodChange.set(Calendar.HOUR_OF_DAY, PERIOD_START_HOUR)
                _nextPeriodChange.set(Calendar.MINUTE, PERIOD_START_MINS)
                _nextPeriodChange.set(Calendar.SECOND, 0)
                _nextPeriodChange.set(Calendar.MILLISECOND, 0)
            }

            SevenSigns.PeriodType.RECRUITING, SevenSigns.PeriodType.RESULTS -> _nextPeriodChange.add(
                Calendar.MILLISECOND,
                PERIOD_MINOR_LENGTH
            )
        }
        _log.info("SevenSigns: Next period change set to " + _nextPeriodChange.time)
    }

    fun getCurrentScore(cabal: CabalType): Int {
        val totalStoneScore = _dawnStoneScore + _duskStoneScore

        when (cabal) {
            SevenSigns.CabalType.DAWN -> return Math.round((_dawnStoneScore / if (totalStoneScore.toFloat() == 0f) 1 else totalStoneScore.toInt()).toFloat() * 500) + _dawnFestivalScore
            SevenSigns.CabalType.DUSK -> return Math.round((_duskStoneScore / if (totalStoneScore.toFloat() == 0f) 1 else totalStoneScore.toInt()).toFloat() * 500) + _duskFestivalScore
        }

        return 0
    }

    fun getCurrentStoneScore(cabal: CabalType): Double {
        when (cabal) {
            SevenSigns.CabalType.DAWN -> return _dawnStoneScore
            SevenSigns.CabalType.DUSK -> return _duskStoneScore
        }

        return 0.0
    }

    fun getCurrentFestivalScore(cabal: CabalType): Int {
        when (cabal) {
            SevenSigns.CabalType.DAWN -> return _dawnFestivalScore
            SevenSigns.CabalType.DUSK -> return _duskFestivalScore
        }

        return 0
    }

    fun getSealOwner(seal: SealType): CabalType? {
        return _sealOwners[seal]
    }

    fun getSealProportion(seal: SealType, cabal: CabalType): Int {
        when (cabal) {
            SevenSigns.CabalType.DAWN -> return _dawnScores[seal] ?: 0

            SevenSigns.CabalType.DUSK -> return _duskScores[seal] ?: 0
        }

        return 0
    }

    fun getTotalMembers(cabal: CabalType): Int {
        var cabalMembers = 0

        for (set in _playersData.values)
            if (set.getEnum("cabal", CabalType::class.java) == cabal)
                cabalMembers++

        return cabalMembers
    }

    fun getPlayerStoneContrib(objectId: Int): Int {
        val set = _playersData[objectId] ?: return 0

        return set.getInteger("red_stones") + set.getInteger("green_stones") + set.getInteger("blue_stones")
    }

    fun getPlayerContribScore(objectId: Int): Int {
        val set = _playersData[objectId] ?: return 0

        return set.getInteger("contribution_score")
    }

    fun getPlayerAdenaCollect(objectId: Int): Int {
        val set = _playersData[objectId] ?: return 0

        return set.getInteger("ancient_adena_amount")
    }

    fun getPlayerSeal(objectId: Int): SealType {
        val set = _playersData[objectId] ?: return SealType.NONE

        return set.getEnum("seal", SealType::class.java)
    }

    fun getPlayerCabal(objectId: Int): CabalType {
        val set = _playersData[objectId] ?: return CabalType.NORMAL

        return set.getEnum("cabal", CabalType::class.java)
    }

    /**
     * Restores all Seven Signs data and settings, usually called at server startup.
     */
    fun restoreSevenSignsData() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var st = con.prepareStatement(LOAD_DATA)
                var rset = st.executeQuery()

                while (rset.next()) {
                    val objectId = rset.getInt("char_obj_id")

                    val set = StatsSet()
                    set["char_obj_id"] = objectId.toDouble()
                    set.set("cabal", CabalType.valueOf(rset.getString("cabal")))
                    set.set("seal", SealType.valueOf(rset.getString("seal")))
                    set["red_stones"] = rset.getInt("red_stones").toDouble()
                    set["green_stones"] = rset.getInt("green_stones").toDouble()
                    set["blue_stones"] = rset.getInt("blue_stones").toDouble()
                    set["ancient_adena_amount"] = rset.getDouble("ancient_adena_amount")
                    set["contribution_score"] = rset.getDouble("contribution_score")

                    _playersData[objectId] = set
                }

                rset.close()
                st.close()

                st = con.prepareStatement(LOAD_STATUS)
                rset = st.executeQuery()

                while (rset.next()) {
                    currentCycle = rset.getInt("current_cycle")
                    currentPeriod = PeriodType.valueOf(rset.getString("active_period"))
                    _previousWinner = CabalType.valueOf(rset.getString("previous_winner"))

                    _dawnStoneScore = rset.getDouble("dawn_stone_score")
                    _dawnFestivalScore = rset.getInt("dawn_festival_score")
                    _duskStoneScore = rset.getDouble("dusk_stone_score")
                    _duskFestivalScore = rset.getInt("dusk_festival_score")

                    _sealOwners[SealType.AVARICE] = CabalType.valueOf(rset.getString("avarice_owner"))
                    _sealOwners[SealType.GNOSIS] = CabalType.valueOf(rset.getString("gnosis_owner"))
                    _sealOwners[SealType.STRIFE] = CabalType.valueOf(rset.getString("strife_owner"))

                    _dawnScores[SealType.AVARICE] = rset.getInt("avarice_dawn_score")
                    _dawnScores[SealType.GNOSIS] = rset.getInt("gnosis_dawn_score")
                    _dawnScores[SealType.STRIFE] = rset.getInt("strife_dawn_score")

                    _duskScores[SealType.AVARICE] = rset.getInt("avarice_dusk_score")
                    _duskScores[SealType.GNOSIS] = rset.getInt("gnosis_dusk_score")
                    _duskScores[SealType.STRIFE] = rset.getInt("strife_dusk_score")

                    _lastSave.timeInMillis = rset.getLong("date")
                }

                rset.close()
                st.close()
            }
        } catch (e: SQLException) {
            _log.log(Level.SEVERE, "SevenSigns: Unable to load data to database: " + e.message, e)
        }

    }

    /**
     * Saves all Seven Signs player data.<br></br>
     * Should be called on period change and shutdown only.
     */
    fun saveSevenSignsData() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_PLAYER).use { st ->
                    for (set in _playersData.values) {
                        st.setString(1, set.getString("cabal"))
                        st.setString(2, set.getString("seal"))
                        st.setInt(3, set.getInteger("red_stones"))
                        st.setInt(4, set.getInteger("green_stones"))
                        st.setInt(5, set.getInteger("blue_stones"))
                        st.setDouble(6, set.getDouble("ancient_adena_amount"))
                        st.setDouble(7, set.getDouble("contribution_score"))
                        st.setInt(8, set.getInteger("char_obj_id"))
                        st.addBatch()
                    }
                    st.executeBatch()
                }
            }
        } catch (e: SQLException) {
            _log.log(Level.SEVERE, "SevenSigns: Unable to save data to database: " + e.message, e)
        }

    }

    fun saveSevenSignsStatus() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_STATUS).use { st ->
                    st.setInt(1, currentCycle)
                    st.setString(2, currentPeriod.toString())
                    st.setString(3, _previousWinner.toString())
                    st.setDouble(4, _dawnStoneScore)
                    st.setInt(5, _dawnFestivalScore)
                    st.setDouble(6, _duskStoneScore)
                    st.setInt(7, _duskFestivalScore)
                    st.setString(8, _sealOwners[SealType.AVARICE].toString())
                    st.setString(9, _sealOwners[SealType.GNOSIS].toString())
                    st.setString(10, _sealOwners[SealType.STRIFE].toString())
                    st.setInt(11, _dawnScores[SealType.AVARICE] ?: 0)
                    st.setInt(12, _dawnScores[SealType.GNOSIS] ?: 0)
                    st.setInt(13, _dawnScores[SealType.STRIFE] ?: 0)
                    st.setInt(14, _duskScores[SealType.AVARICE] ?: 0)
                    st.setInt(15, _duskScores[SealType.GNOSIS] ?: 0)
                    st.setInt(16, _duskScores[SealType.STRIFE] ?: 0)
                    st.setInt(17, SevenSignsFestival.currentFestivalCycle)

                    for (i in 0 until SevenSignsFestival.FESTIVAL_COUNT)
                        st.setInt(18 + i, SevenSignsFestival.getAccumulatedBonus(i))

                    _lastSave = Calendar.getInstance()
                    st.setLong(18 + SevenSignsFestival.FESTIVAL_COUNT, _lastSave.timeInMillis)
                    st.execute()
                }
            }
        } catch (e: SQLException) {
            _log.log(Level.SEVERE, "SevenSigns: Unable to save status to database: " + e.message, e)
        }

    }

    /**
     * Used to reset the cabal details of all players, and update the database.<BR></BR>
     * Primarily used when beginning a new cycle, and should otherwise never be called.
     */
    fun resetPlayerData() {
        for (set in _playersData.values) {
            set["cabal"] = CabalType.NORMAL
            set["seal"] = SealType.NONE
            set["contribution_score"] = 0.0
        }
    }

    /**
     * Used to specify cabal-related details for the specified player.<br></br>
     * This method checks to see if the player has registered before and will update the database if necessary.
     * @param objectId
     * @param cabal
     * @param seal
     * @return the cabal ID the player has joined.
     */
    fun setPlayerInfo(objectId: Int, cabal: CabalType, seal: SealType): CabalType {
        var set: StatsSet? = _playersData[objectId]
        if (set != null) {
            set["cabal"] = cabal
            set["seal"] = seal
        } else {
            set = StatsSet()
            set["char_obj_id"] = objectId.toDouble()
            set["cabal"] = cabal
            set["seal"] = seal
            set["red_stones"] = 0.0
            set["green_stones"] = 0.0
            set["blue_stones"] = 0.0
            set["ancient_adena_amount"] = 0.0
            set["contribution_score"] = 0.0

            _playersData[objectId] = set

            // Update data in database, as we have a new player signing up.
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(INSERT_PLAYER).use { st ->
                        st.setInt(1, objectId)
                        st.setString(2, cabal.toString())
                        st.setString(3, seal.toString())
                        st.execute()
                    }
                }
            } catch (e: SQLException) {
                _log.log(Level.SEVERE, "SevenSigns: Failed to save data: " + e.message, e)
            }

        }

        // Increasing Seal total score for the player chosen Seal.
        if (cabal == CabalType.DAWN)
            _dawnScores[seal] = _dawnScores[seal]?.plus(1) ?: 0
        else
            _duskScores[seal] = _duskScores[seal]?.plus(1) ?: 0

        return cabal
    }

    /**
     * @param objectId
     * @return the amount of ancient adena the specified player can claim, if any.
     */
    fun getAncientAdenaReward(objectId: Int): Int {
        val set = _playersData[objectId]
        val rewardAmount = set?.getInteger("ancient_adena_amount")

        set?.set("red_stones", 0.0)
        set?.set("green_stones", 0.0)
        set?.set("blue_stones", 0.0)
        set?.set("ancient_adena_amount", 0.0)

        return rewardAmount ?: 0
    }

    /**
     * Used to add the specified player's seal stone contribution points to the current total for their cabal. Returns the point score the contribution was worth.<br></br>
     * Each stone count <B>must be</B> broken down and specified by the stone's color.
     * @param objectId The objectId of the player.
     * @param blueCount Amount of blue stones.
     * @param greenCount Amount of green stones.
     * @param redCount Amount of red stones.
     * @return
     */
    fun addPlayerStoneContrib(objectId: Int, blueCount: Int, greenCount: Int, redCount: Int): Int {
        val set = _playersData[objectId]

        val contribScore = calcScore(blueCount, greenCount, redCount)
        val totalAncientAdena = set?.getInteger("ancient_adena_amount")?.plus(contribScore)
        val totalContribScore = set?.getInteger("contribution_score")!! + contribScore

        if (totalContribScore > Config.ALT_MAXIMUM_PLAYER_CONTRIB)
            return -1

        set["red_stones"] = (set.getInteger("red_stones") + redCount).toDouble()
        set["green_stones"] = (set.getInteger("green_stones") + greenCount).toDouble()
        set["blue_stones"] = (set.getInteger("blue_stones") + blueCount).toDouble()
        set["ancient_adena_amount"] = totalAncientAdena?.toDouble() ?: 0.0
        set["contribution_score"] = totalContribScore.toDouble() ?: 0.0

        when (getPlayerCabal(objectId)) {
            SevenSigns.CabalType.DAWN -> _dawnStoneScore += contribScore.toDouble()

            SevenSigns.CabalType.DUSK -> _duskStoneScore += contribScore.toDouble()
        }

        return contribScore
    }

    /**
     * Adds the specified number of festival points to the specified cabal. Remember, the same number of points are <B>deducted from the rival cabal</B> to maintain proportionality.
     * @param cabal
     * @param amount
     */
    fun addFestivalScore(cabal: CabalType, amount: Int) {
        if (cabal == CabalType.DUSK) {
            _duskFestivalScore += amount

            // To prevent negative scores!
            if (_dawnFestivalScore >= amount)
                _dawnFestivalScore -= amount
        } else {
            _dawnFestivalScore += amount

            if (_duskFestivalScore >= amount)
                _duskFestivalScore -= amount
        }
    }

    /**
     * Used to initialize the seals for each cabal. (Used at startup or at beginning of a new cycle). This method should be called after <B>resetSeals()</B> and <B>calcNewSealOwners()</B> on a new cycle.
     */
    fun initializeSeals() {
        for ((currentSeal, sealOwner) in _sealOwners) {

            if (sealOwner != CabalType.NORMAL) {
                if (isSealValidationPeriod)
                    _log.info("SevenSigns: The " + sealOwner.fullName + " have won the " + currentSeal.fullName + ".")
                else
                    _log.info("SevenSigns: The " + currentSeal.fullName + " is currently owned by " + sealOwner.fullName + ".")
            } else
                _log.info("SevenSigns: The " + currentSeal.fullName + " remains unclaimed.")
        }
    }

    /**
     * Only really used at the beginning of a new cycle, this method resets all seal-related data.
     */
    fun resetSeals() {
        _dawnScores[SealType.AVARICE] = 0
        _dawnScores[SealType.GNOSIS] = 0
        _dawnScores[SealType.STRIFE] = 0

        _duskScores[SealType.AVARICE] = 0
        _duskScores[SealType.GNOSIS] = 0
        _duskScores[SealType.STRIFE] = 0
    }

    /**
     * Calculates the ownership of the three Seals of the Seven Signs, based on various criterias.<BR></BR>
     * Should only ever called at the beginning of a new cycle.
     */
    fun calcNewSealOwners() {
        for (seal in _dawnScores.keys) {
            val prevSealOwner = _sealOwners[seal]

            val dawnProportion = getSealProportion(seal, CabalType.DAWN)
            val totalDawnMembers = Math.max(1, getTotalMembers(CabalType.DAWN))
            val dawnPercent = Math.round(dawnProportion.toFloat() / totalDawnMembers.toFloat() * 100)

            val duskProportion = getSealProportion(seal, CabalType.DUSK)
            val totalDuskMembers = Math.max(1, getTotalMembers(CabalType.DUSK))
            val duskPercent = Math.round(duskProportion.toFloat() / totalDuskMembers.toFloat() * 100)

            var newSealOwner = CabalType.NORMAL

            // If a Seal was already closed or owned by the opponent and the new winner wants to assume ownership of the Seal, 35% or more of the members of the Cabal must have chosen the Seal. If they chose less than 35%, they cannot own the Seal.
            // If the Seal was owned by the winner in the previous Seven Signs, they can retain that seal if 10% or more members have chosen it. If they want to possess a new Seal, at least 35% of the members of the Cabal must have chosen the new Seal.
            when (prevSealOwner) {
                SevenSigns.CabalType.NORMAL -> when (cabalHighestScore) {
                    SevenSigns.CabalType.DAWN -> if (dawnPercent >= 35)
                        newSealOwner = CabalType.DAWN

                    SevenSigns.CabalType.DUSK -> if (duskPercent >= 35)
                        newSealOwner = CabalType.DUSK
                }

                SevenSigns.CabalType.DAWN -> when (cabalHighestScore) {
                    SevenSigns.CabalType.NORMAL -> if (dawnPercent >= 10)
                        newSealOwner = CabalType.DAWN

                    SevenSigns.CabalType.DAWN -> if (dawnPercent >= 10)
                        newSealOwner = CabalType.DAWN

                    SevenSigns.CabalType.DUSK -> if (duskPercent >= 35)
                        newSealOwner = CabalType.DUSK
                    else if (dawnPercent >= 10)
                        newSealOwner = CabalType.DAWN
                }

                SevenSigns.CabalType.DUSK -> when (cabalHighestScore) {
                    SevenSigns.CabalType.NORMAL -> if (duskPercent >= 10)
                        newSealOwner = CabalType.DUSK

                    SevenSigns.CabalType.DAWN -> if (dawnPercent >= 35)
                        newSealOwner = CabalType.DAWN
                    else if (duskPercent >= 10)
                        newSealOwner = CabalType.DUSK

                    SevenSigns.CabalType.DUSK -> if (duskPercent >= 10)
                        newSealOwner = CabalType.DUSK
                }
            }

            _sealOwners[seal] = newSealOwner

            // Alert all online players to new seal status.
            when (seal) {
                SevenSigns.SealType.AVARICE -> if (newSealOwner == CabalType.DAWN)
                    SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_AVARICE).toAllOnlinePlayers()
                else if (newSealOwner == CabalType.DUSK)
                    SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_AVARICE).toAllOnlinePlayers()

                SevenSigns.SealType.GNOSIS -> if (newSealOwner == CabalType.DAWN)
                    SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_GNOSIS).toAllOnlinePlayers()
                else if (newSealOwner == CabalType.DUSK)
                    SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_GNOSIS).toAllOnlinePlayers()

                SevenSigns.SealType.STRIFE -> {
                    if (newSealOwner == CabalType.DAWN)
                        SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_STRIFE).toAllOnlinePlayers()
                    else if (newSealOwner == CabalType.DUSK)
                        SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_STRIFE).toAllOnlinePlayers()

                    CastleManager.validateTaxes(newSealOwner)
                }
            }
        }
    }

    /**
     * This method is called to remove all players from catacombs and necropolises, who belong to the losing cabal.<BR></BR>
     * **Should only ever called at the beginning of Seal Validation.**
     * @param winningCabal
     */
    fun teleLosingCabalFromDungeons(winningCabal: CabalType) {
        for (player in World.players) {
            if (player.isGM || !player.isIn7sDungeon)
                continue

            val set = _playersData[player.objectId]
            if (set != null) {
                val playerCabal = set.getEnum("cabal", CabalType::class.java)
                if (isSealValidationPeriod || isCompResultsPeriod) {
                    if (playerCabal == winningCabal)
                        continue
                } else if (playerCabal == CabalType.NORMAL)
                    continue
            }

            player.teleToLocation(MapRegionData.TeleportType.TOWN)
            player.isIn7sDungeon = false
        }
    }

    /**
     * The primary controller of period change of the Seven Signs system. This runs all related tasks depending on the period that is about to begin.
     */
    private class SevenSignsPeriodChange : Runnable {
        override fun run() {
            // Remember the period check here refers to the period just ENDED!
            val periodEnded = currentPeriod

            // Increment the period.
            currentPeriod = PeriodType.VALUES[(currentPeriod.ordinal + 1) % PeriodType.VALUES.size]

            when (periodEnded) {
                SevenSigns.PeriodType.RECRUITING // Initialization
                -> {
                    // Start the Festival of Darkness cycle.
                    SevenSignsFestival.startFestivalManager()

                    // Reset castles certificates count.
                    CastleManager.resetCertificates()

                    // Send message that Competition has begun.
                    SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_BEGUN).toAllOnlinePlayers()
                }

                SevenSigns.PeriodType.COMPETITION // Results Calculation
                -> {
                    // Send message that Competition has ended.
                    SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_ENDED).toAllOnlinePlayers()

                    val winningCabal = cabalHighestScore

                    // Schedule a stop of the festival engine and reward highest ranking members from cycle
                    SevenSignsFestival.festivalManagerSchedule?.cancel(false)
                    SevenSignsFestival.rewardHighestRanked()

                    calcNewSealOwners()

                    when (winningCabal) {
                        SevenSigns.CabalType.DAWN -> SystemMessage.getSystemMessage(SystemMessageId.DAWN_WON).toAllOnlinePlayers()

                        SevenSigns.CabalType.DUSK -> SystemMessage.getSystemMessage(SystemMessageId.DUSK_WON).toAllOnlinePlayers()
                    }

                    _previousWinner = winningCabal
                }

                SevenSigns.PeriodType.RESULTS // Seal Validation
                -> {
                    // Perform initial Seal Validation set up.
                    initializeSeals()

                    // Buff/Debuff members of the event when Seal of Strife captured.
                    giveSosEffect(getSealOwner(SealType.STRIFE))

                    // Send message that Seal Validation has begun.
                    SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_BEGUN).toAllOnlinePlayers()

                    _log.info(
                        "SevenSigns: The " + _previousWinner?.fullName + " have won the competition with " + getCurrentScore(_previousWinner!!) + " points!"
                    )
                }

                SevenSigns.PeriodType.SEAL_VALIDATION // Reset for New Cycle
                -> {
                    // Ensure a cycle restart when this period ends.
                    currentPeriod = PeriodType.RECRUITING

                    // Send message that Seal Validation has ended.
                    SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_ENDED).toAllOnlinePlayers()

                    // Clear Seal of Strife influence.
                    removeSosEffect()

                    // Reset all data
                    resetPlayerData()
                    resetSeals()

                    currentCycle++

                    // Reset all Festival-related data and remove any unused blood offerings.
                    // NOTE: A full update of Festival data in the database is also performed.
                    SevenSignsFestival.resetFestivalData(false)

                    _dawnStoneScore = 0.0
                    _duskStoneScore = 0.0

                    _dawnFestivalScore = 0
                    _duskFestivalScore = 0
                }
            }

            // Make sure all Seven Signs data is saved for future use.
            saveSevenSignsData()
            saveSevenSignsStatus()

            teleLosingCabalFromDungeons(cabalHighestScore)

            // Spawns NPCs and change sky color.
            SSQInfo.sendSky().toAllOnlinePlayers()
            spawnSevenSignsNPC()

            _log.info("SevenSigns: The " + currentPeriod.name + " period has begun!")

            setCalendarForNextPeriodChange()

            ThreadPool.schedule(SevenSignsPeriodChange(), milliToPeriodChange)
        }
    }

    /**
     * Buff/debuff players following their membership to Seal of Strife.
     * @param strifeOwner The cabal owning the Seal of Strife.
     */
    fun giveSosEffect(strifeOwner: CabalType?) {
        for (player in World.players) {
            val cabal = getPlayerCabal(player.objectId)
            if (cabal != CabalType.NORMAL) {
                if (cabal == strifeOwner)
                    player.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.skill, false)
                else
                    player.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.skill, false)
            }
        }
    }

    /**
     * Stop Seal of Strife effects on all online characters.
     */
    fun removeSosEffect() {
        for (player in World.players) {
            player.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.skill!!.id, false)
            player.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.skill!!.id, false)
        }
    }
}