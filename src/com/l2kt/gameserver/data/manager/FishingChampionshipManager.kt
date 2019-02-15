package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.sql.ServerMemoTable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

/**
 * The championship tournament is held on a weekly cycle. During the competition, players vie to catch the biggest fish. The results are ranked based on the size of the fish they caught, with the biggest fish determining the winner.<br></br>
 * <br></br>
 * The top five fishers of the first week's tournament will be determined on Tuesday. At that time, a new tournament begins for the second week of the event. Winners for that week will be determined on the following Tuesday. The tournament will continue to run weekly with winners determined every
 * Tuesday.<br></br>
 * <br></br>
 * The current status of the tournament can be viewed by clicking the Contest button in the Fishing window. The current results can also be viewed by speaking with a Fisherman's Guild Member after the results of the first week have been calculated.<br></br>
 * <br></br>
 * **Catching the Big One**<br></br>
 * <br></br>
 * A big fish is measured by length only. Fish between 60.000 and 90.000 can be caught with any normal type of fishing lure, including Lure for Beginners. A Prize-Winning Fishing Lure increases your chances of catching an even larger fish.<br></br>
 * <br></br>
 * If you catch a fish worthy of ranking in the tournament, a system message displays to let you know that you have been registered in the current ranking.<br></br>
 * <br></br>
 * When more than one competitor has a winning fish that are all the same in size, the person who caught the first fish wins.<br></br>
 * <br></br>
 * **Claiming the Prize**<br></br>
 * <br></br>
 * Championship prizes are awarded by a Fisherman's Guild Member, and you only have one week to claim your prize. The Fisherman's Guild Member award a weekly prize of Adena based on ranking:
 *
 *  * 1st Place: 800,000 Adena
 *  * 2nd Place: 500,000 Adena
 *  * 3rd Place: 300,000 Adena
 *  * 4th Place: 200,000 Adena
 *  * 5th Place: 100,000 Adena
 *
 */
object FishingChampionshipManager {

    private val LOGGER = CLogger(FishingChampionshipManager::class.java.name)

    private const val INSERT = "INSERT INTO fishing_championship(player_name,fish_length,rewarded) VALUES (?,?,?)"
    private const val DELETE = "DELETE FROM fishing_championship"
    private const val SELECT = "SELECT `player_name`, `fish_length`, `rewarded` FROM fishing_championship"

    private val playersName = ArrayList<String>()
    private val fishLength = ArrayList<String>()
    private val winPlayersName = ArrayList<String>()
    private val winFishLength = ArrayList<String>()
    private val tmpPlayers = ArrayList<Fisher>()
    private val winPlayers = ArrayList<Fisher>()

    private var _endDate: Long = 0
    private var _minFishLength = 0.0
    private var _needRefresh = true

    val timeRemaining: Long
        get() = (_endDate - System.currentTimeMillis()) / 60000

    init {
        restoreData()
        refreshWinResult()
        recalculateMinLength()

        if (_endDate <= System.currentTimeMillis()) {
            _endDate = System.currentTimeMillis()
            finishChamp()
        } else
            ThreadPool.schedule(Runnable{ finishChamp() }, _endDate - System.currentTimeMillis())
    }

    private fun setEndOfChamp() {
        val cal = Calendar.getInstance()
        cal.timeInMillis = _endDate
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.add(Calendar.DAY_OF_MONTH, 6)
        cal.set(Calendar.DAY_OF_WEEK, 3)
        cal.set(Calendar.HOUR_OF_DAY, 19)

        _endDate = cal.timeInMillis
    }

    private fun restoreData() {
        _endDate = ServerMemoTable.getLong("fishChampionshipEnd", 0)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val rewarded = rs.getInt("rewarded")
                            if (rewarded == 0)
                                tmpPlayers.add(Fisher(rs.getString("player_name"), rs.getDouble("fish_length"), 0))
                            else if (rewarded > 0)
                                winPlayers.add(
                                    Fisher(
                                        rs.getString("player_name"),
                                        rs.getDouble("fish_length"),
                                        rewarded
                                    )
                                )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore fishing championship data.", e)
        }

    }

    @Synchronized
    private fun refreshResult() {
        _needRefresh = false

        playersName.clear()
        fishLength.clear()

        var fisher1: Fisher
        var fisher2: Fisher

        for (x in 0 until tmpPlayers.size) {
            for (y in 0..tmpPlayers.size - 2) {
                fisher1 = tmpPlayers[y]
                fisher2 = tmpPlayers[y + 1]
                if (fisher1.length < fisher2.length) {
                    tmpPlayers[y] = fisher2
                    tmpPlayers[y + 1] = fisher1
                }
            }
        }

        for (x in 0 until tmpPlayers.size) {
            playersName.add(tmpPlayers[x].name)
            fishLength.add(tmpPlayers[x].length.toString())
        }
    }

    private fun refreshWinResult() {
        winPlayersName.clear()
        winFishLength.clear()

        var fisher1: Fisher
        var fisher2: Fisher

        for (x in 0 until winPlayers.size) {
            for (y in 0..winPlayers.size - 2) {
                fisher1 = winPlayers[y]
                fisher2 = winPlayers[y + 1]
                if (fisher1.length < fisher2.length) {
                    winPlayers[y] = fisher2
                    winPlayers[y + 1] = fisher1
                }
            }
        }

        for (x in 0 until winPlayers.size) {
            winPlayersName.add(winPlayers[x].name)
            winFishLength.add(winPlayers[x].length.toString())
        }
    }

    private fun finishChamp() {
        winPlayers.clear()
        for (fisher in tmpPlayers) {
            fisher.rewardType = 1
            winPlayers.add(fisher)
        }
        tmpPlayers.clear()

        refreshWinResult()
        setEndOfChamp()
        shutdown()

        LOGGER.info("A new Fishing Championship event period has started.")
        ThreadPool.schedule(Runnable{ finishChamp() }, _endDate - System.currentTimeMillis())
    }

    private fun recalculateMinLength() {
        var minLen = 99999.0
        for (fisher in tmpPlayers) {
            if (fisher.length < minLen)
                minLen = fisher.length
        }
        _minFishLength = minLen
    }

    @Synchronized
    fun newFish(player: Player, lureId: Int) {
        if (!Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
            return

        var len = Rnd[60, 89] + Rnd[0, 1000] / 1000.0
        if (lureId in 8484..8486)
            len += Rnd[0, 3000] / 1000.0

        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CAUGHT_FISH_S1_LENGTH).addString(len.toString()))

        if (tmpPlayers.size < 5) {
            for (fisher in tmpPlayers) {
                if (fisher.name.equals(player.name, ignoreCase = true)) {
                    if (fisher.length < len) {
                        fisher.length = len
                        player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING)
                        recalculateMinLength()
                    }
                    return
                }
            }
            tmpPlayers.add(Fisher(player.name, len, 0))
            player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING)
            recalculateMinLength()
        } else if (_minFishLength < len) {
            for (fisher in tmpPlayers) {
                if (fisher.name.equals(player.name, ignoreCase = true)) {
                    if (fisher.length < len) {
                        fisher.length = len
                        player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING)
                        recalculateMinLength()
                    }
                    return
                }
            }

            var minFisher: Fisher? = null
            var minLen = 99999.0
            for (fisher in tmpPlayers) {
                if (fisher.length < minLen) {
                    minFisher = fisher
                    minLen = minFisher.length
                }
            }
            tmpPlayers.remove(minFisher)
            tmpPlayers.add(Fisher(player.name, len, 0))
            player.sendPacket(SystemMessageId.REGISTERED_IN_FISH_SIZE_RANKING)
            recalculateMinLength()
        }
    }

    fun getWinnerName(par: Int): String {
        return if (winPlayersName.size >= par) winPlayersName[par - 1] else "None"

    }

    fun getCurrentName(par: Int): String {
        return if (playersName.size >= par) playersName[par - 1] else "None"

    }

    fun getFishLength(par: Int): String {
        return if (winFishLength.size >= par) winFishLength[par - 1] else "0"

    }

    fun getCurrentFishLength(par: Int): String {
        return if (fishLength.size >= par) fishLength[par - 1] else "0"

    }

    fun isWinner(playerName: String): Boolean {
        for (name in winPlayersName) {
            if (name == playerName)
                return true
        }
        return false
    }

    fun getReward(player: Player) {
        for (fisher in winPlayers) {
            if (fisher.name.equals(player.name, ignoreCase = true)) {
                if (fisher.rewardType != 2) {
                    var rewardCnt = 0
                    for (x in winPlayersName.indices) {
                        if (winPlayersName[x].equals(player.name, ignoreCase = true)) {
                            when (x) {
                                0 -> rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_1

                                1 -> rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_2

                                2 -> rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_3

                                3 -> rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_4

                                4 -> rewardCnt = Config.ALT_FISH_CHAMPIONSHIP_REWARD_5
                            }
                        }
                    }

                    fisher.rewardType = 2

                    if (rewardCnt > 0) {
                        player.addItem(
                            "fishing_reward",
                            Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM,
                            rewardCnt,
                            null,
                            true
                        )

                        val html = NpcHtmlMessage(0)
                        html.setFile("data/html/fisherman/championship/fish_event_reward001.htm")
                        player.sendPacket(html)
                    }
                }
            }
        }
    }

    fun showMidResult(player: Player) {
        val html = NpcHtmlMessage(0)

        if (_needRefresh) {
            html.setFile("data/html/fisherman/championship/fish_event003.htm")
            player.sendPacket(html)

            refreshResult()
            ThreadPool.schedule(Runnable{ _needRefresh = true }, 60000)
            return
        }

        html.setFile("data/html/fisherman/championship/fish_event002.htm")

        val sb = StringBuilder(100)
        for (x in 1..5) {
            StringUtil.append(sb, "<tr><td width=70 align=center>", x, "</td>")
            StringUtil.append(sb, "<td width=110 align=center>", getCurrentName(x), "</td>")
            StringUtil.append(sb, "<td width=80 align=center>", getCurrentFishLength(x), "</td></tr>")
        }
        html.replace("%TABLE%", sb.toString())
        html.replace("%prizeItem%", ItemTable.getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM)!!.name)
        html.replace("%prizeFirst%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_1)
        html.replace("%prizeTwo%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_2)
        html.replace("%prizeThree%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_3)
        html.replace("%prizeFour%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_4)
        html.replace("%prizeFive%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_5)
        player.sendPacket(html)
    }

    fun showChampScreen(player: Player, objectId: Int) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/fisherman/championship/fish_event001.htm")

        val sb = StringBuilder(100)
        for (x in 1..5) {
            StringUtil.append(sb, "<tr><td width=70 align=center>", x, "</td>")
            StringUtil.append(sb, "<td width=110 align=center>", getWinnerName(x), "</td>")
            StringUtil.append(sb, "<td width=80 align=center>", getFishLength(x), "</td></tr>")
        }
        html.replace("%TABLE%", sb.toString())
        html.replace("%prizeItem%", ItemTable.getTemplate(Config.ALT_FISH_CHAMPIONSHIP_REWARD_ITEM)!!.name)
        html.replace("%prizeFirst%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_1)
        html.replace("%prizeTwo%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_2)
        html.replace("%prizeThree%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_3)
        html.replace("%prizeFour%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_4)
        html.replace("%prizeFive%", Config.ALT_FISH_CHAMPIONSHIP_REWARD_5)
        html.replace("%refresh%", timeRemaining)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    fun shutdown() {
        ServerMemoTable.set("fishChampionshipEnd", _endDate)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE).use { ps ->
                    con.prepareStatement(INSERT).use { ps2 ->
                        ps.execute()
                        ps.close()

                        for (fisher in winPlayers) {
                            ps2.setString(1, fisher.name)
                            ps2.setDouble(2, fisher.length)
                            ps2.setInt(3, fisher.rewardType)
                            ps2.addBatch()
                        }

                        for (fisher in tmpPlayers) {
                            ps2.setString(1, fisher.name)
                            ps2.setDouble(2, fisher.length)
                            ps2.setInt(3, 0)
                            ps2.addBatch()
                        }
                        ps2.executeBatch()
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update fishing championship data.", e)
        }

    }

    private data class Fisher(val name: String, var length: Double, var rewardType: Int)
}