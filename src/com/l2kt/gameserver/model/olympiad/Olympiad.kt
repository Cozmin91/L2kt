package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.model.actor.instance.OlympiadManagerNpc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.zone.type.OlympiadStadiumZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.NpcSay
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet
import java.sql.PreparedStatement
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ScheduledFuture

object Olympiad {

    var _olympiadEnd: Long = 0
    var _validationEnd: Long = 0

    /**
     * The current period of the olympiad.<br></br>
     * **0 -** Competition period<br></br>
     * **1 -** Validation Period
     */
    var _period: Int = 0
    var _nextWeeklyChange: Long = 0
    var currentCycle: Int = 0
        set
    private var _compEnd: Long = 0
    private var _compStart: Calendar? = null

    var _scheduledCompStart: ScheduledFuture<*>? = null
    var _scheduledCompEnd: ScheduledFuture<*>? = null
    var _scheduledOlympiadEnd: ScheduledFuture<*>? = null
    var _scheduledWeeklyTask: ScheduledFuture<*>? = null
    var _scheduledValdationTask: ScheduledFuture<*>? = null
    var _gameManager: ScheduledFuture<*>? = null
    var _gameAnnouncer: ScheduledFuture<*>? = null

    val _log = CLogger(Olympiad::class.java.name)

    private val _nobles = HashMap<Int, StatsSet>()
    private val _noblesRank = HashMap<Int, Int>()

    val _heroesToBe: MutableList<StatsSet> = ArrayList()

    const val OLYMPIAD_HTML_PATH = "data/html/olympiad/"

    private const val OLYMPIAD_LOAD_DATA =
        "SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0"
    private const val OLYMPIAD_SAVE_DATA =
        "INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?"

    private const val OLYMPIAD_LOAD_NOBLES =
        "SELECT olympiad_nobles.char_id, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id"
    private const val OLYMPIAD_SAVE_NOBLES =
        "INSERT INTO olympiad_nobles (`char_id`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`, `competitions_drawn`) VALUES (?,?,?,?,?,?,?)"
    private const val OLYMPIAD_UPDATE_NOBLES =
        "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE char_id = ?"
    private val OLYMPIAD_GET_HEROS =
        "SELECT olympiad_nobles.char_id, characters.char_name FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC"
    private val GET_ALL_CLASSIFIED_NOBLESS =
        "SELECT char_id from olympiad_nobles_eom WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC"
    private val GET_EACH_CLASS_LEADER =
        "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.char_id AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10"

    private const val OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles"
    private const val OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom"
    private const val OLYMPIAD_MONTH_CREATE =
        "INSERT INTO olympiad_nobles_eom SELECT char_id, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles"

    private val COMP_START = Config.ALT_OLY_START_TIME // 6PM
    private val COMP_MIN = Config.ALT_OLY_MIN // 00 mins
    private val COMP_PERIOD = Config.ALT_OLY_CPERIOD // 6 hours
    val WEEKLY_PERIOD = Config.ALT_OLY_WPERIOD // 1 week
    val VALIDATION_PERIOD = Config.ALT_OLY_VPERIOD // 24 hours

    val DEFAULT_POINTS = Config.ALT_OLY_START_POINTS
    val WEEKLY_POINTS = Config.ALT_OLY_WEEKLY_POINTS

    const val CHAR_ID = "char_id"
    const val CLASS_ID = "class_id"
    const val CHAR_NAME = "char_name"
    const val POINTS = "olympiad_points"
    const val COMP_DONE = "competitions_done"
    const val COMP_WON = "competitions_won"
    const val COMP_LOST = "competitions_lost"
    const val COMP_DRAWN = "competitions_drawn"
    var _inCompPeriod: Boolean = false
    var _compStarted = false

    private val millisToOlympiadEnd: Long
        get() = _olympiadEnd - Calendar.getInstance().timeInMillis

    val millisToValidationEnd: Long
        get() = if (_validationEnd > Calendar.getInstance().timeInMillis) _validationEnd - Calendar.getInstance().timeInMillis else 10L

    val isOlympiadEnd: Boolean
        get() = _period != 0

    private val millisToCompBegin: Long
        get() {
            if (_compStart!!.timeInMillis < Calendar.getInstance().timeInMillis && _compEnd > Calendar.getInstance().timeInMillis)
                return 10L

            return if (_compStart!!.timeInMillis > Calendar.getInstance().timeInMillis) _compStart!!.timeInMillis - Calendar.getInstance().timeInMillis else setNewCompBegin()

        }

    val millisToCompEnd: Long
        get() = _compEnd - Calendar.getInstance().timeInMillis

    private val millisToWeekChange: Long
        get() = if (_nextWeeklyChange > Calendar.getInstance().timeInMillis) _nextWeeklyChange - Calendar.getInstance().timeInMillis else 10L

    init {
        load()

        if (_period == 0)
            init()
    }

    val nobleCount: Int
        get() = _nobles.size

    fun getNobleStats(playerId: Int): StatsSet? {
        return _nobles[playerId]
    }

    /**
     * @param charId the noble object Id.
     * @param data the stats set data to add.
     * @return the old stats set if the noble is already present, null otherwise.
     */
    fun addNobleStats(charId: Int, data: StatsSet): StatsSet? {
        return _nobles.put(charId, data)
    }

    private fun load() {
        var loaded = false
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(OLYMPIAD_LOAD_DATA)
                val rset = statement.executeQuery()

                while (rset.next()) {
                    currentCycle = rset.getInt("current_cycle")
                    _period = rset.getInt("period")
                    _olympiadEnd = rset.getLong("olympiad_end")
                    _validationEnd = rset.getLong("validation_end")
                    _nextWeeklyChange = rset.getLong("next_weekly_change")
                    loaded = true
                }

                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.warn("Olympiad: Error loading olympiad data from database: ", e)
        }

        if (!loaded) {
            _log.info("Olympiad: failed to load data from database, default values are used.")

            currentCycle = 1
            _period = 0
            _olympiadEnd = 0
            _validationEnd = 0
            _nextWeeklyChange = 0
        }

        when (_period) {
            0 -> if (_olympiadEnd == 0L || _olympiadEnd < Calendar.getInstance().timeInMillis)
                setNewOlympiadEnd()
            else
                scheduleWeeklyChange()
            1 -> if (_validationEnd > Calendar.getInstance().timeInMillis) {
                loadNoblesRank()
                _scheduledValdationTask = ThreadPool.schedule(ValidationEndTask(), millisToValidationEnd)
            } else {
                currentCycle++
                _period = 0
                deleteNobles()
                setNewOlympiadEnd()
            }
            else -> {
                _log.warn("Olympiad: something went wrong loading period: $_period")
                return
            }
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(OLYMPIAD_LOAD_NOBLES)
                val rset = statement.executeQuery()
                var statData: StatsSet

                while (rset.next()) {
                    statData = StatsSet()
                    statData[CLASS_ID] = rset.getInt(CLASS_ID).toDouble()
                    statData[CHAR_NAME] = rset.getString(CHAR_NAME)
                    statData[POINTS] = rset.getInt(POINTS).toDouble()
                    statData[COMP_DONE] = rset.getInt(COMP_DONE).toDouble()
                    statData[COMP_WON] = rset.getInt(COMP_WON).toDouble()
                    statData[COMP_LOST] = rset.getInt(COMP_LOST).toDouble()
                    statData[COMP_DRAWN] = rset.getInt(COMP_DRAWN).toDouble()
                    statData["to_save"] = false

                    addNobleStats(rset.getInt(CHAR_ID), statData)
                }

                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.warn("Olympiad: Error loading noblesse data from database: ", e)
        }

        synchronized(this) {
            if (_period == 0)
                _log.info("Olympiad: Currently in Competition period.")
            else
                _log.info("Olympiad: Currently in Validation period.")

            var milliToEnd: Long
            if (_period == 0)
                milliToEnd = millisToOlympiadEnd
            else
                milliToEnd = millisToValidationEnd

            _log.info("Olympiad: " + Math.round((milliToEnd / 60000).toFloat()) + " minutes until period ends.")

            if (_period == 0) {
                milliToEnd = millisToWeekChange
                _log.info("Olympiad: Next weekly change is in " + Math.round((milliToEnd / 60000).toFloat()) + " minutes.")
            }
        }

        _log.info("Olympiad: Loaded " + _nobles.size + " nobles.")
    }

    fun loadNoblesRank() {
        _noblesRank.clear()

        val tmpPlace = HashMap<Int, Int>()

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS)
                val rset = statement.executeQuery()

                var place = 1
                while (rset.next())
                    tmpPlace[rset.getInt(CHAR_ID)] = place++

                rset.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.warn("Olympiad: Error loading noblesse data from database for Ranking: ", e)
        }

        var rank1 = Math.round(tmpPlace.size * 0.01).toInt()
        var rank2 = Math.round(tmpPlace.size * 0.10).toInt()
        var rank3 = Math.round(tmpPlace.size * 0.25).toInt()
        var rank4 = Math.round(tmpPlace.size * 0.50).toInt()

        if (rank1 == 0) {
            rank1 = 1
            rank2++
            rank3++
            rank4++
        }

        for (charId in tmpPlace.keys) {
            if (tmpPlace[charId]!! <= rank1)
                _noblesRank[charId] = 1
            else if (tmpPlace[charId]!! <= rank2)
                _noblesRank[charId] = 2
            else if (tmpPlace[charId]!! <= rank3)
                _noblesRank[charId] = 3
            else if (tmpPlace[charId]!! <= rank4)
                _noblesRank[charId] = 4
            else
                _noblesRank[charId] = 5
        }
    }

    fun init() {
        if (_period == 1)
            return

        _compStart = Calendar.getInstance()
        _compStart!!.set(Calendar.HOUR_OF_DAY, COMP_START)
        _compStart!!.set(Calendar.MINUTE, COMP_MIN)
        _compEnd = _compStart!!.timeInMillis + COMP_PERIOD

        if (_scheduledOlympiadEnd != null)
            _scheduledOlympiadEnd!!.cancel(true)

        _scheduledOlympiadEnd = ThreadPool.schedule(OlympiadEndTask(), millisToOlympiadEnd)

        updateCompStatus()
    }

    class OlympiadEndTask : Runnable {
        override fun run() {
            SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(currentCycle)
                .toAllOnlinePlayers()

            if (_scheduledWeeklyTask != null)
                _scheduledWeeklyTask!!.cancel(true)

            saveNobleData()

            _period = 1
            sortHeroesToBe()
            Hero.resetData()
            Hero.computeNewHeroes(_heroesToBe)

            saveOlympiadStatus()
            updateMonthlyData()

            val validationEnd = Calendar.getInstance()
            _validationEnd = validationEnd.timeInMillis + VALIDATION_PERIOD

            loadNoblesRank()
            _scheduledValdationTask = ThreadPool.schedule(ValidationEndTask(), millisToValidationEnd)
        }
    }

    class ValidationEndTask : Runnable {
        override fun run() {
            _period = 0
            currentCycle++

            deleteNobles()
            setNewOlympiadEnd()
            init()
        }
    }

    private fun updateCompStatus() {
        synchronized(this) {
            val milliToStart = millisToCompBegin

            val numSecs = (milliToStart / 1000 % 60).toDouble()
            var countDown = (milliToStart / 1000 - numSecs) / 60
            val numMins = Math.floor(countDown % 60).toInt()
            countDown = (countDown - numMins) / 60
            val numHours = Math.floor(countDown % 24).toInt()
            val numDays = Math.floor((countDown - numHours) / 24).toInt()

            _log.info("Olympiad: Competition period starts in $numDays days, $numHours hours and $numMins mins.")
            _log.info("Olympiad: Event starts/started : " + _compStart!!.time)
        }

        _scheduledCompStart = ThreadPool.schedule(Runnable{
            if (isOlympiadEnd)
                return@Runnable

            _inCompPeriod = true

            SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED).toAllOnlinePlayers()
            _log.info("Olympiad: Olympiad game started.")

            _gameManager = ThreadPool.scheduleAtFixedRate(OlympiadGameManager, 30000, 30000)
            if (Config.ALT_OLY_ANNOUNCE_GAMES)
                _gameAnnouncer = ThreadPool.scheduleAtFixedRate(OlympiadAnnouncer(), 30000, 500)

            val regEnd = millisToCompEnd - 600000
            if (regEnd > 0)
                ThreadPool.schedule(Runnable{
                    SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED)
                        .toAllOnlinePlayers()
                }, regEnd)

            _scheduledCompEnd = ThreadPool.schedule(Runnable innerRunnable@{
                if (isOlympiadEnd)
                    return@innerRunnable

                _inCompPeriod = false
                SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED).toAllOnlinePlayers()
                _log.info("Olympiad: Olympiad game ended.")

                while (OlympiadGameManager.isBattleStarted)
                // cleared in game manager
                {
                    // wait 1 minutes for end of pendings games
                    try {
                        Thread.sleep(60000)
                    } catch (e: InterruptedException) {
                    }

                }

                if (_gameManager != null) {
                    _gameManager!!.cancel(false)
                    _gameManager = null
                }

                if (_gameAnnouncer != null) {
                    _gameAnnouncer!!.cancel(false)
                    _gameAnnouncer = null
                }

                saveOlympiadStatus()

                init()
            }, millisToCompEnd)
        }, millisToCompBegin)
    }

    fun manualSelectHeroes() {
        if (_scheduledOlympiadEnd != null)
            _scheduledOlympiadEnd!!.cancel(true)

        _scheduledOlympiadEnd = ThreadPool.schedule(OlympiadEndTask(), 0)
    }

    fun setNewOlympiadEnd() {
        SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(currentCycle)
            .toAllOnlinePlayers()

        val currentTime = Calendar.getInstance()
        currentTime.add(Calendar.MONTH, 1)
        currentTime.set(Calendar.DAY_OF_MONTH, 1)
        currentTime.set(Calendar.AM_PM, Calendar.AM)
        currentTime.set(Calendar.HOUR, 12)
        currentTime.set(Calendar.MINUTE, 0)
        currentTime.set(Calendar.SECOND, 0)
        _olympiadEnd = currentTime.timeInMillis

        val nextChange = Calendar.getInstance()
        _nextWeeklyChange = nextChange.timeInMillis + WEEKLY_PERIOD
        scheduleWeeklyChange()
    }

    fun inCompPeriod(): Boolean {
        return _inCompPeriod
    }

    private fun setNewCompBegin(): Long {
        _compStart = Calendar.getInstance()
        _compStart!!.set(Calendar.HOUR_OF_DAY, COMP_START)
        _compStart!!.set(Calendar.MINUTE, COMP_MIN)
        _compStart!!.add(Calendar.HOUR_OF_DAY, 24)
        _compEnd = _compStart!!.timeInMillis + COMP_PERIOD

        _log.info("Olympiad: New schedule @ " + _compStart!!.time)

        return _compStart!!.timeInMillis - Calendar.getInstance().timeInMillis
    }

    private fun scheduleWeeklyChange() {
        _scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(Runnable{
            addWeeklyPoints()
            _log.info("Olympiad: Added weekly points to nobles.")

            val nextChange = Calendar.getInstance()
            _nextWeeklyChange = nextChange.timeInMillis + WEEKLY_PERIOD
        }, millisToWeekChange, WEEKLY_PERIOD)
    }

    @Synchronized
    fun addWeeklyPoints() {
        if (_period == 1)
            return

        var currentPoints: Int
        for (nobleInfo in _nobles.values) {
            currentPoints = nobleInfo.getInteger(POINTS)
            currentPoints += WEEKLY_POINTS
            nobleInfo[POINTS] = currentPoints.toDouble()
        }
    }

    fun playerInStadia(player: Player): Boolean {
        return ZoneManager.getZone(player, OlympiadStadiumZone::class.java) != null
    }

    /**
     * Save noblesse data to database
     */
    @Synchronized
    fun saveNobleData() {
        if (_nobles == null || _nobles.isEmpty())
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                var statement: PreparedStatement
                for ((charId, value) in _nobles) {
                    val nobleInfo = value ?: continue

                    val classId = nobleInfo.getInteger(CLASS_ID)
                    val points = nobleInfo.getInteger(POINTS)
                    val compDone = nobleInfo.getInteger(COMP_DONE)
                    val compWon = nobleInfo.getInteger(COMP_WON)
                    val compLost = nobleInfo.getInteger(COMP_LOST)
                    val compDrawn = nobleInfo.getInteger(COMP_DRAWN)
                    val toSave = nobleInfo.getBool("to_save")

                    if (toSave) {
                        statement = con.prepareStatement(OLYMPIAD_SAVE_NOBLES)
                        statement.setInt(1, charId)
                        statement.setInt(2, classId)
                        statement.setInt(3, points)
                        statement.setInt(4, compDone)
                        statement.setInt(5, compWon)
                        statement.setInt(6, compLost)
                        statement.setInt(7, compDrawn)

                        nobleInfo["to_save"] = false
                    } else {
                        statement = con.prepareStatement(OLYMPIAD_UPDATE_NOBLES)
                        statement.setInt(1, points)
                        statement.setInt(2, compDone)
                        statement.setInt(3, compWon)
                        statement.setInt(4, compLost)
                        statement.setInt(5, compDrawn)
                        statement.setInt(6, charId)
                    }
                    statement.execute()
                    statement.close()
                }
            }
        } catch (e: SQLException) {
            _log.error("Olympiad: Failed to save noblesse data to database: ", e)
        }

    }

    /**
     * Save current olympiad status and update noblesse table in database
     */
    fun saveOlympiadStatus() {
        saveNobleData()

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(OLYMPIAD_SAVE_DATA)

                statement.setInt(1, currentCycle)
                statement.setInt(2, _period)
                statement.setLong(3, _olympiadEnd)
                statement.setLong(4, _validationEnd)
                statement.setLong(5, _nextWeeklyChange)
                statement.setInt(6, currentCycle)
                statement.setInt(7, _period)
                statement.setLong(8, _olympiadEnd)
                statement.setLong(9, _validationEnd)
                statement.setLong(10, _nextWeeklyChange)

                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.error("Olympiad: Failed to save olympiad data to database: ", e)
        }

    }

    fun updateMonthlyData() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var statement = con.prepareStatement(OLYMPIAD_MONTH_CLEAR)
                statement.execute()
                statement.close()
                statement = con.prepareStatement(OLYMPIAD_MONTH_CREATE)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.error("Olympiad: Failed to update monthly noblese data: ", e)
        }

    }

    fun sortHeroesToBe() {
        _heroesToBe.clear()

        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(OLYMPIAD_GET_HEROS)
                for (id in ClassId.VALUES) {
                    if (id.level() != 3)
                        continue

                    statement.setInt(1, id.id)
                    val rset = statement.executeQuery()
                    statement.clearParameters()

                    if (rset.next()) {
                        val hero = StatsSet()
                        hero[CLASS_ID] = id.id.toDouble()
                        hero[CHAR_ID] = rset.getInt(CHAR_ID).toDouble()
                        hero[CHAR_NAME] = rset.getString(CHAR_NAME)

                        _heroesToBe.add(hero)
                    }
                    rset.close()
                }
                statement.close()
            }
        } catch (e: SQLException) {
            _log.warn("Olympiad: Couldnt load heroes to be from DB")
        }

    }

    fun getClassLeaderBoard(classId: Int): List<String> {
        val names = ArrayList<String>()
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(GET_EACH_CLASS_LEADER)
                statement.setInt(1, classId)
                val rset = statement.executeQuery()

                while (rset.next())
                    names.add(rset.getString(CHAR_NAME))

                statement.close()
                rset.close()
            }
        } catch (e: SQLException) {
            _log.warn("Olympiad: Couldn't load olympiad leaders from DB!")
        }

        return names
    }

    fun getNoblessePasses(player: Player?, clear: Boolean): Int {
        if (player == null || _period != 1 || _noblesRank.isEmpty())
            return 0

        val objId = player.objectId
        if (!_noblesRank.containsKey(objId))
            return 0

        val noble = _nobles[objId]
        if (noble == null || noble.getInteger(POINTS) == 0)
            return 0

        val rank = _noblesRank[objId]
        var points =
            if (player.isHero || Hero.isInactiveHero(player.objectId)) Config.ALT_OLY_HERO_POINTS else 0
        when (rank) {
            1 -> points += Config.ALT_OLY_RANK1_POINTS
            2 -> points += Config.ALT_OLY_RANK2_POINTS
            3 -> points += Config.ALT_OLY_RANK3_POINTS
            4 -> points += Config.ALT_OLY_RANK4_POINTS
            else -> points += Config.ALT_OLY_RANK5_POINTS
        }

        if (clear)
            noble[POINTS] = 0.0

        points *= Config.ALT_OLY_GP_PER_POINT
        return points
    }

    fun getNoblePoints(objId: Int): Int {
        return if (_nobles == null || !_nobles.containsKey(objId)) 0 else _nobles[objId]!!.getInteger(POINTS)

    }

    fun getLastNobleOlympiadPoints(objId: Int): Int {
        var result = 0
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE char_id = ?")
                statement.setInt(1, objId)
                val rs = statement.executeQuery()
                if (rs.first())
                    result = rs.getInt(1)
                rs.close()
                statement.close()
            }
        } catch (e: Exception) {
            _log.warn("Could not load last olympiad points:", e)
        }

        return result
    }

    fun getCompetitionDone(objId: Int): Int {
        return if (_nobles == null || !_nobles.containsKey(objId)) 0 else _nobles[objId]!!.getInteger(COMP_DONE)

    }

    fun getCompetitionWon(objId: Int): Int {
        return if (_nobles == null || !_nobles.containsKey(objId)) 0 else _nobles[objId]!!.getInteger(COMP_WON)

    }

    fun getCompetitionLost(objId: Int): Int {
        return if (_nobles == null || !_nobles.containsKey(objId)) 0 else _nobles[objId]!!.getInteger(COMP_LOST)

    }

    fun deleteNobles() {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement(OLYMPIAD_DELETE_ALL)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            _log.warn("Olympiad: Couldn't delete nobles from DB!")
        }

        _nobles.clear()
    }

    private class OlympiadAnnouncer : Runnable {
        private val _tasks: Array<OlympiadGameTask> = OlympiadGameManager.olympiadTasks.filterNotNull().toTypedArray()

        override fun run() {
            for (task in _tasks) {
                if (!task.needAnnounce())
                    continue

                val game = task.game ?: continue

                val announcement: String
                if (game.type === CompetitionType.NON_CLASSED)
                    announcement = "Olympiad class-free individual match is going to begin in Arena " +
                            (game.stadiumId + 1) + " in a moment."
                else
                    announcement = "Olympiad class individual match is going to begin in Arena " +
                            (game.stadiumId + 1) + " in a moment."

                for (manager in OlympiadManagerNpc.getInstances())
                    manager.broadcastPacket(NpcSay(manager.objectId, Say2.SHOUT, manager.npcId, announcement))
            }
        }
    }
}