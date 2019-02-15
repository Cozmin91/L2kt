package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

/**
 * Handles the Lottery event, where player can buy tickets to gamble money.
 */
object LotteryManager {

    var id: Int = 0
    var prize: Int = 0
    var isSellableTickets: Boolean = false
    var isStarted: Boolean = false
    var endDate: Long = 0

    private val LOGGER = CLogger(LotteryManager::class.java.name)

    const val SECOND: Long = 1000
    const val MINUTE: Long = 60000

    private const val INSERT_LOTTERY = "INSERT INTO games(id, idnr, enddate, prize, newprize) VALUES (?, ?, ?, ?, ?)"
    private const val UPDATE_PRICE = "UPDATE games SET prize=?, newprize=? WHERE id = 1 AND idnr = ?"
    private const val UPDATE_LOTTERY =
        "UPDATE games SET finished=1, prize=?, newprize=?, number1=?, number2=?, prize1=?, prize2=?, prize3=? WHERE id=1 AND idnr=?"
    private const val SELECT_LAST_LOTTERY =
        "SELECT idnr, prize, newprize, enddate, finished FROM games WHERE id = 1 ORDER BY idnr DESC LIMIT 1"
    private const val SELECT_LOTTERY_ITEM =
        "SELECT enchant_level, custom_type2 FROM items WHERE item_id = 4442 AND custom_type1 = ?"
    private const val SELECT_LOTTERY_TICKET =
        "SELECT number1, number2, prize1, prize2, prize3 FROM games WHERE id = 1 and idnr = ?"

    fun decodeNumbers(enchant: Int, type2: Int): IntArray {
        var enchant = enchant
        var type2 = type2
        val res = IntArray(5)
        var id = 0
        var nr = 1

        while (enchant > 0) {
            val `val` = enchant / 2
            if (`val`.toDouble() != enchant.toDouble() / 2)
                res[id++] = nr

            enchant /= 2
            nr++
        }

        nr = 17

        while (type2 > 0) {
            val `val` = type2 / 2
            if (`val`.toDouble() != type2.toDouble() / 2)
                res[id++] = nr

            type2 /= 2
            nr++
        }

        return res
    }

    fun checkTicket(item: ItemInstance): IntArray {
        return checkTicket(item.customType1, item.enchantLevel, item.customType2)
    }

    fun checkTicket(id: Int, enchant: Int, type2: Int): IntArray {
        val res = intArrayOf(0, 0)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT_LOTTERY_TICKET).use { ps ->
                    ps.setInt(1, id)

                    val rs = ps.executeQuery()
                    if (rs.next()) {
                        var curenchant = rs.getInt("number1") and enchant
                        var curtype2 = rs.getInt("number2") and type2

                        if (curenchant == 0 && curtype2 == 0) {
                            rs.close()
                            ps.close()
                            return res
                        }

                        var count = 0

                        for (i in 1..16) {
                            val `val` = curenchant / 2
                            if (`val`.toDouble() != curenchant.toDouble() / 2)
                                count++

                            val val2 = curtype2 / 2
                            if (val2.toDouble() != curtype2.toDouble() / 2)
                                count++

                            curenchant = `val`
                            curtype2 = val2
                        }

                        when (count) {
                            0 -> {
                            }

                            5 -> {
                                res[0] = 1
                                res[1] = rs.getInt("prize1")
                            }

                            4 -> {
                                res[0] = 2
                                res[1] = rs.getInt("prize2")
                            }

                            3 -> {
                                res[0] = 3
                                res[1] = rs.getInt("prize3")
                            }

                            else -> {
                                res[0] = 4
                                res[1] = Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE
                            }
                        }
                    }
                    rs.close()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't check lottery ticket #{}.", e, id)
        }

        return res
    }

    init {
        id = 1
        prize = Config.ALT_LOTTERY_PRIZE
        isSellableTickets = false
        isStarted = false
        endDate = System.currentTimeMillis()

        if (Config.ALLOW_LOTTERY)
            StartLottery().run()
    }

    fun increasePrize(count: Int) {
        prize += count

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_PRICE).use { ps ->
                    ps.setInt(1, prize)
                    ps.setInt(2, prize)
                    ps.setInt(3, id)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't increase current lottery prize.", e)
        }

    }

    private class StartLottery : Runnable {

        override fun run() {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(SELECT_LAST_LOTTERY).use { ps ->
                        ps.executeQuery().use { rs ->
                            if (rs.next()) {
                                id = rs.getInt("idnr")

                                if (rs.getInt("finished") == 1) {
                                    id++
                                    prize = rs.getInt("newprize")
                                } else {
                                    prize = rs.getInt("prize")
                                    endDate = rs.getLong("enddate")

                                    if (endDate <= System.currentTimeMillis() + 2 * MINUTE) {
                                        FinishLottery().run()
                                        rs.close()
                                        ps.close()
                                        return
                                    }

                                    if (endDate > System.currentTimeMillis()) {
                                        isStarted = true
                                        ThreadPool.schedule(FinishLottery(), endDate - System.currentTimeMillis())

                                        if (endDate > System.currentTimeMillis() + 12 * MINUTE) {
                                            isSellableTickets = true
                                            ThreadPool.schedule(
                                                StopSellingTickets(),
                                                endDate - System.currentTimeMillis() - 10 * MINUTE
                                            )
                                        }
                                        rs.close()
                                        ps.close()
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't restore lottery data.", e)
            }

            isSellableTickets = true
            isStarted = true

            "Lottery tickets are now available for Lucky Lottery #$id.".announceToOnlinePlayers()

            val finishTime = Calendar.getInstance()
            finishTime.timeInMillis = endDate
            finishTime.set(Calendar.MINUTE, 0)
            finishTime.set(Calendar.SECOND, 0)

            if (finishTime.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                finishTime.set(Calendar.HOUR_OF_DAY, 19)
                endDate = finishTime.timeInMillis
                endDate += 604800000
            } else {
                finishTime.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                finishTime.set(Calendar.HOUR_OF_DAY, 19)
                endDate = finishTime.timeInMillis
            }

            ThreadPool.schedule(StopSellingTickets(), endDate - System.currentTimeMillis() - 10 * MINUTE)
            ThreadPool.schedule(FinishLottery(), endDate - System.currentTimeMillis())

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(INSERT_LOTTERY).use { ps ->
                        ps.setInt(1, 1)
                        ps.setInt(2, id)
                        ps.setLong(3, endDate)
                        ps.setInt(4, prize)
                        ps.setInt(5, prize)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't store new lottery data.", e)
            }

        }
    }

    private class StopSellingTickets : Runnable {

        override fun run() {
            isSellableTickets = false

            SystemMessage.getSystemMessage(SystemMessageId.LOTTERY_TICKET_SALES_TEMP_SUSPENDED).toAllOnlinePlayers()
        }
    }

    private class FinishLottery : Runnable {

        override fun run() {
            val luckynums = IntArray(5)
            var luckynum = 0

            for (i in 0..4) {
                var found = true

                while (found) {
                    luckynum = Rnd[20] + 1
                    found = false

                    for (j in 0 until i)
                        if (luckynums[j] == luckynum)
                            found = true
                }

                luckynums[i] = luckynum
            }

            var enchant = 0
            var type2 = 0

            for (i in 0..4) {
                if (luckynums[i] < 17)
                    enchant += Math.pow(2.0, (luckynums[i] - 1).toDouble()).toInt()
                else
                    type2 += Math.pow(2.0, (luckynums[i] - 17).toDouble()).toInt()
            }

            var count1 = 0
            var count2 = 0
            var count3 = 0
            var count4 = 0

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(SELECT_LOTTERY_ITEM).use { ps ->
                        ps.setInt(1, id)

                        val rs = ps.executeQuery()
                        while (rs.next()) {
                            var curenchant = rs.getInt("enchant_level") and enchant
                            var curtype2 = rs.getInt("custom_type2") and type2

                            if (curenchant == 0 && curtype2 == 0)
                                continue

                            var count = 0

                            for (i in 1..16) {
                                val `val` = curenchant / 2

                                if (`val`.toDouble() != curenchant.toDouble() / 2)
                                    count++

                                val val2 = curtype2 / 2

                                if (val2.toDouble() != curtype2.toDouble() / 2)
                                    count++

                                curenchant = `val`
                                curtype2 = val2
                            }

                            if (count == 5)
                                count1++
                            else if (count == 4)
                                count2++
                            else if (count == 3)
                                count3++
                            else if (count > 0)
                                count4++
                        }
                        rs.close()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't restore lottery data.", e)
            }

            val prize4 = count4 * Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE
            var prize1 = 0
            var prize2 = 0
            var prize3 = 0

            if (count1 > 0)
                prize1 = ((prize - prize4) * Config.ALT_LOTTERY_5_NUMBER_RATE / count1).toInt()

            if (count2 > 0)
                prize2 = ((prize - prize4) * Config.ALT_LOTTERY_4_NUMBER_RATE / count2).toInt()

            if (count3 > 0)
                prize3 = ((prize - prize4) * Config.ALT_LOTTERY_3_NUMBER_RATE / count3).toInt()

            // Calculate new prize.
            val newPrize = Config.ALT_LOTTERY_PRIZE + prize - (prize1 + prize2 + prize3 + prize4)

            if (count1 > 0)
            // There are winners.
                SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_WINNER_S1_IS_S2_ADENA_WE_HAVE_S3_PRIZE_WINNER).addNumber(
                    id
                ).addNumber(prize).addNumber(count1).toAllOnlinePlayers()
            else
            // There are no winners.
                SystemMessage.getSystemMessage(SystemMessageId.AMOUNT_FOR_LOTTERY_S1_IS_S2_ADENA_NO_WINNER).addNumber(id).addNumber(
                    prize
                ).toAllOnlinePlayers()

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_LOTTERY).use { ps ->
                        ps.setInt(1, prize)
                        ps.setInt(2, newPrize)
                        ps.setInt(3, enchant)
                        ps.setInt(4, type2)
                        ps.setInt(5, prize1)
                        ps.setInt(6, prize2)
                        ps.setInt(7, prize3)
                        ps.setInt(8, id)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't store finished lottery data.", e)
            }

            ThreadPool.schedule(StartLottery(), MINUTE)
            id++

            isStarted = false
        }
    }
}