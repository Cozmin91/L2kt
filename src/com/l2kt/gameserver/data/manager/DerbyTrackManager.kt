package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.extensions.toAllPlayersInZoneType
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.HistoryInfo
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.zone.type.DerbyTrackZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.DeleteObject
import com.l2kt.gameserver.network.serverpackets.MonRaceInfo
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object DerbyTrackManager {

    val LOGGER = CLogger(DerbyTrackManager::class.java.name)
    val _runners: MutableList<Npc> = ArrayList() // List holding initial npcs, shuffled on a new race.
    val _history = TreeMap<Int, HistoryInfo>() // List holding old race records.
    val _betsPerLane: MutableMap<Int, Long> =
        ConcurrentHashMap() // Map holding all bets for each lane ; values setted to 0 after every race.
    val _odds: MutableList<Double> =
        ArrayList() // List holding sorted odds per lane ; cleared at new odds calculation.

    var raceNumber = 1
    var _finalCountdown = 0
    var currentRaceState = RaceState.RACE_END

    lateinit var racePacket: MonRaceInfo

    private const val SAVE_HISTORY = "INSERT INTO mdt_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)"
    private const val LOAD_HISTORY = "SELECT * FROM mdt_history"
    private const val LOAD_BETS = "SELECT * FROM mdt_bets"
    private const val SAVE_BETS = "REPLACE INTO mdt_bets (lane_id, bet) VALUES (?,?)"
    private const val CLEAR_BETS = "UPDATE mdt_bets SET bet = 0"

    val SOUND_1 = PlaySound(1, "S_Race")
    val SOUND_2 = PlaySound("ItemSound2.race_start")

    val CODES = arrayOf(intArrayOf(-1, 0), intArrayOf(0, 15322), intArrayOf(13765, -1))


    var runners: List<Npc>? = null
        private set // Holds the actual list of 8 runners.
    var speeds: Array<IntArray>? = null
        private set
    var first: Int = 0
        private set // Index going from 0-7.
    var second: Int = 0
        private set // Index going from 0-7.

    val lastHistoryEntries: List<HistoryInfo>
        get() = _history.descendingMap().values.take(8)

    val odds: List<Double>
        get() = _odds

    enum class RaceState {
        ACCEPTING_BETS,
        WAITING,
        STARTING_RACE,
        RACE_END
    }

    init {
        // Feed _history with previous race results.
        loadHistory()

        // Feed _betsPerLane with stored informations on bets.
        loadBets()

        // Feed _runners, we will only have to shuffle it when needed.
        try {
            for (i in 31003..31026) {
                val template = NpcData.getTemplate(i) ?: continue

                val _constructor =
                    Class.forName("com.l2kt.gameserver.model.actor.instance." + template.type).constructors[0]

                _runners.add(_constructor.newInstance(IdFactory.getInstance().nextId, template) as Npc)
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't initialize runners.", e)
        }

        speeds = Array(8) { IntArray(20) }

        ThreadPool.scheduleAtFixedRate(Announcement(), 0, 1000)
    }

    /**
     * @param index : The actual index of List to check on.
     * @return the name of the Npc.
     */
    fun getRunnerName(index: Int): String {
        val npc = runners!![index]
        return if (npc == null) "" else npc.name
    }

    fun getHistoryInfo(raceNumber: Int): HistoryInfo? {
        return _history[raceNumber]
    }

    fun newRace() {
        // Edit _history.
        _history[raceNumber] = HistoryInfo(raceNumber, 0, 0, 0.0)

        // Randomize _runners.
        Collections.shuffle(_runners)

        // Setup 8 new creatures ; pickup the first 8 from _runners.
        runners = _runners.subList(0, 8)
    }

    fun newSpeeds() {
        speeds = Array(8) { IntArray(20) }

        var total = 0
        var winnerDistance = 0
        var secondDistance = 0

        // For each lane.
        for (i in 0..7) {
            // Reset value upon new lane.
            total = 0

            // Test the 20 segments.
            for (j in 0..19) {
                if (j == 19)
                    speeds!![i][j] = 100
                else
                    speeds!![i][j] = Rnd[60] + 65

                // feed actual total to current lane total.
                total += speeds!![i][j]
            }

            // The current total for this line is superior or equals to previous winner ; it means we got a new winner, and the old winner becomes second.
            if (total >= winnerDistance) {
                // Old winner becomes second.
                second = first

                // Old winner distance is the second.
                secondDistance = winnerDistance

                // Find the good index.
                first = i

                // Set the new limit to bypass winner position.
                winnerDistance = total
            } else if (total >= secondDistance) {
                // Find the good index.
                second = i

                // Set the new limit to bypass second position.
                secondDistance = total
            }// The total wasn't enough to
        }
    }

    /**
     * Load past races informations, feeding _history arrayList.<br></br>
     * Also sets _raceNumber, based on latest HistoryInfo loaded.
     */
    fun loadHistory() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_HISTORY).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val savedRaceNumber = rs.getInt("race_id")

                            _history[savedRaceNumber] =
                                    HistoryInfo(
                                        savedRaceNumber,
                                        rs.getInt("first"),
                                        rs.getInt("second"),
                                        rs.getDouble("odd_rate")
                                    )

                            // Calculate the current race number.
                            if (raceNumber <= savedRaceNumber)
                                raceNumber = savedRaceNumber + 1
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Can't load Derby Track history.", e)
        }

        LOGGER.info("Loaded {} Derby Track records, currently on race #{}.", _history.size, raceNumber)
    }

    /**
     * Save an [HistoryInfo] record into database.
     * @param history The HistoryInfo to store.
     */
    fun saveHistory(history: HistoryInfo?) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SAVE_HISTORY).use { ps ->
                    ps.setInt(1, history!!.raceId)
                    ps.setInt(2, history.first)
                    ps.setInt(3, history.second)
                    ps.setDouble(4, history.oddRate)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Can't save Derby Track history.", e)
        }

    }

    /**
     * Load current bets per lane ; initialize the map keys.
     */
    fun loadBets() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_BETS).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            setBetOnLane(rs.getInt("lane_id"), rs.getLong("bet"), false)
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Can't load Derby Track bets.", e)
        }

    }

    /**
     * Save the current lane bet into database.
     * @param lane : The lane to affect.
     * @param sum : The sum to set.
     */
    fun saveBet(lane: Int, sum: Long) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SAVE_BETS).use { ps ->
                    ps.setInt(1, lane)
                    ps.setLong(2, sum)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Can't save Derby Track bet.", e)
        }

    }

    /**
     * Clear all lanes bets, either on database or Map.
     */
    fun clearBets() {
        for (key in _betsPerLane.keys)
            _betsPerLane[key] = 0L

        try {
            L2DatabaseFactory.connection.use { con -> con.prepareStatement(CLEAR_BETS).use { ps -> ps.execute() } }
        } catch (e: Exception) {
            LOGGER.error("Can't clear Derby Track bets.", e)
        }

    }

    /**
     * Setup lane bet, based on previous value (if any).
     * @param lane : The lane to edit.
     * @param amount : The amount to add.
     * @param saveOnDb : Should it be saved on db or not.
     */
    fun setBetOnLane(lane: Int, amount: Long, saveOnDb: Boolean) {
        val sum = (_betsPerLane).getOrDefault(lane, 0L) + amount

        _betsPerLane[lane] = sum

        if (saveOnDb)
            saveBet(lane, sum)
    }

    /**
     * Calculate odds for every lane, based on others lanes.
     */
    fun calculateOdds() {
        // Clear previous List holding old odds.
        _odds.clear()

        // Sort bets lanes per lane.
        val sortedLanes = TreeMap(_betsPerLane)

        // Pass a first loop in order to calculate total sum of all lanes.
        var sumOfAllLanes: Long = 0
        for (amount in sortedLanes.values)
            sumOfAllLanes += amount

        // As we get the sum, we can now calculate the odd rate of each lane.
        for (amount in sortedLanes.values)
            _odds.add(if (amount == 0L) 0.0 else Math.max(1.25, sumOfAllLanes * 0.7 / amount))
    }

    private class Announcement : Runnable {

        override fun run() {
            if (_finalCountdown > 1200)
                _finalCountdown = 0

            when (_finalCountdown) {
                0 -> {
                    newRace()
                    newSpeeds()

                    currentRaceState = RaceState.ACCEPTING_BETS
                    racePacket = MonRaceInfo(CODES[0][0], CODES[0][1], runners!!, speeds!!)

                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        racePacket,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE).addNumber(
                            raceNumber
                        )
                    )
                }

                30 // 30 sec
                    , 60 // 1 min
                    , 90 // 1 min 30 sec
                    , 120 // 2 min
                    , 150 // 2 min 30
                    , 180 // 3 min
                    , 210 // 3 min 30
                    , 240 // 4 min
                    , 270 // 4 min 30 sec
                    , 330 // 5 min 30 sec
                    , 360 // 6 min
                    , 390 // 6 min 30 sec
                    , 420 // 7 min
                    , 450 // 7 min 30
                    , 480 // 8 min
                    , 510 // 8 min 30
                    , 540 // 9 min
                    , 570 // 9 min 30 sec
                    , 630 // 10 min 30 sec
                    , 660 // 11 min
                    , 690 // 11 min 30 sec
                    , 720 // 12 min
                    , 750 // 12 min 30
                    , 780 // 13 min
                    , 810 // 13 min 30
                    , 870 // 14 min 30 sec
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(
                        raceNumber
                    )
                )

                300 // 5 min
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(
                        raceNumber
                    ),
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(10)
                )

                600 // 10 min
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(
                        raceNumber
                    ),
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(5)
                )

                840 // 14 min
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(
                        raceNumber
                    ),
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(1)
                )

                900 // 15 min
                -> {
                    currentRaceState = RaceState.WAITING

                    calculateOdds()

                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(
                            raceNumber
                        ),
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_TICKET_SALES_CLOSED)
                    )
                }

                960 // 16 min
                    , 1020 // 17 min
                -> {
                    val minutes = if (_finalCountdown == 960) 2 else 1
                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S2_BEGINS_IN_S1_MINUTES).addNumber(
                            minutes
                        )
                    )
                }

                1050 // 17 min 30 sec
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_BEGINS_IN_30_SECONDS)
                )

                1070 // 17 min 50 sec
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS)
                )

                1075 // 17 min 55 sec
                    , 1076 // 17 min 56 sec
                    , 1077 // 17 min 57 sec
                    , 1078 // 17 min 58 sec
                    , 1079 // 17 min 59 sec
                -> {
                    val seconds = 1080 - _finalCountdown
                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS).addNumber(seconds)
                    )
                }

                1080 // 18 min
                -> {
                    currentRaceState = RaceState.STARTING_RACE
                    racePacket = MonRaceInfo(CODES[1][0], CODES[1][1], runners!!, speeds!!)

                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START),
                        SOUND_1,
                        SOUND_2,
                        racePacket
                    )
                }

                1085 // 18 min 5 sec
                -> {
                    racePacket = MonRaceInfo(CODES[2][0], CODES[2][1], runners!!, speeds!!)

                    toAllPlayersInZoneType(DerbyTrackZone::class.java, racePacket)
                }

                1115 // 18 min 35 sec
                -> {
                    currentRaceState = RaceState.RACE_END

                    // Retrieve current HistoryInfo and populate it with data, then stores it in database.
                    val info = getHistoryInfo(raceNumber)
                    if (info != null) {
                        info.first = first
                        info.second = second
                        info.oddRate = _odds[first]

                        saveHistory(info)
                    }

                    // Clear bets.
                    clearBets()

                    toAllPlayersInZoneType(
                        DerbyTrackZone::class.java,
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2).addNumber(
                            first + 1
                        ).addNumber(second + 1),
                        SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_RACE_END).addNumber(raceNumber)
                    )
                    raceNumber++
                }

                1140 // 19 min
                -> toAllPlayersInZoneType(
                    DerbyTrackZone::class.java,
                    DeleteObject(runners!![0]),
                    DeleteObject(runners!![1]),
                    DeleteObject(runners!![2]),
                    DeleteObject(runners!![3]),
                    DeleteObject(runners!![4]),
                    DeleteObject(runners!![5]),
                    DeleteObject(runners!![6]),
                    DeleteObject(runners!![7])
                )
            }
            _finalCountdown += 1
        }
    }
}