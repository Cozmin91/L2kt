package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * @author DS
 */
object OlympiadManager {
    private val _nonClassBasedRegisters: MutableList<Int>
    private val _classBasedRegisters: MutableMap<Int, MutableList<Int>>

    val registeredNonClassBased: MutableList<Int>
        get() = _nonClassBasedRegisters

    val registeredClassBased: MutableMap<Int, MutableList<Int>>
        get() = _classBasedRegisters

    init {
        _nonClassBasedRegisters = CopyOnWriteArrayList()
        _classBasedRegisters = ConcurrentHashMap()
    }

    fun hasEnoughRegisteredClassed(): MutableList<MutableList<Int>>? {
        var result: MutableList<MutableList<Int>>? = null
        for ((_, value) in _classBasedRegisters) {
            if (value.size >= Config.ALT_OLY_CLASSED) {
                if (result == null)
                    result = ArrayList()

                result.add(value)
            }
        }
        return result
    }

    fun hasEnoughRegisteredNonClassed(): Boolean {
        return _nonClassBasedRegisters.size >= Config.ALT_OLY_NONCLASSED
    }

    fun clearRegistered() {
        _nonClassBasedRegisters.clear()
        _classBasedRegisters.clear()
    }

    fun isRegistered(noble: Player): Boolean {
        return isRegistered(noble, false)
    }

    private fun isRegistered(player: Player, showMessage: Boolean): Boolean {
        val objId = Integer.valueOf(player.objectId)

        if (_nonClassBasedRegisters.contains(objId)) {
            if (showMessage)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_FOR_ALL_CLASSES_WAITING_TO_PARTICIPATE_IN_THE_GAME))

            return true
        }

        val classed = _classBasedRegisters[player.baseClass]
        if (classed != null && classed.contains(objId)) {
            if (showMessage)
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_ARE_ALREADY_ON_THE_WAITING_LIST_TO_PARTICIPATE_IN_THE_GAME_FOR_YOUR_CLASS))

            return true
        }

        return false
    }

    fun isRegisteredInComp(noble: Player): Boolean {
        return isRegistered(noble, false) || isInCompetition(noble, false)
    }

    fun registerNoble(player: Player, type: CompetitionType): Boolean {
        if (!Olympiad._inCompPeriod) {
            player.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS)
            return false
        }

        if (Olympiad.millisToCompEnd < 600000) {
            player.sendPacket(SystemMessageId.GAME_REQUEST_CANNOT_BE_MADE)
            return false
        }

        when (type) {
            CompetitionType.CLASSED -> {
                if (!checkNoble(player))
                    return false

                var classed: MutableList<Int>? = _classBasedRegisters[player.baseClass]
                if (classed != null)
                    classed.add(player.objectId)
                else {
                    classed = CopyOnWriteArrayList()
                    classed.add(player.objectId)
                    _classBasedRegisters[player.baseClass] = classed
                }

                player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_CLASSIFIED_GAMES)
            }

            CompetitionType.NON_CLASSED -> {
                if (!checkNoble(player))
                    return false

                _nonClassBasedRegisters.add(player.objectId)
                player.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_NO_CLASS_GAMES)
            }
        }
        return true
    }

    fun unRegisterNoble(noble: Player): Boolean {
        if (!Olympiad._inCompPeriod) {
            noble.sendPacket(SystemMessageId.THE_OLYMPIAD_GAME_IS_NOT_CURRENTLY_IN_PROGRESS)
            return false
        }

        if (!noble.isNoble) {
            noble.sendPacket(SystemMessageId.NOBLESSE_ONLY)
            return false
        }

        if (!isRegistered(noble, false)) {
            noble.sendPacket(SystemMessageId.YOU_HAVE_NOT_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_A_GAME)
            return false
        }

        if (isInCompetition(noble, false))
            return false

        val objId = Integer.valueOf(noble.objectId)
        if (_nonClassBasedRegisters.remove(objId)) {
            noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME)
            return true
        }

        val classed = _classBasedRegisters[noble.baseClass]
        if (classed != null && classed.remove(objId)) {
            _classBasedRegisters.remove(noble.baseClass)
            _classBasedRegisters[noble.baseClass] = classed

            noble.sendPacket(SystemMessageId.YOU_HAVE_BEEN_DELETED_FROM_THE_WAITING_LIST_OF_A_GAME)
            return true
        }

        return false
    }

    fun removeDisconnectedCompetitor(player: Player) {
        val task = OlympiadGameManager.getOlympiadTask(player.olympiadGameId)
        if (task != null && task.isGameStarted)
            task.game?.handleDisconnect(player)

        val objId = Integer.valueOf(player.objectId)
        if (_nonClassBasedRegisters.remove(objId))
            return

        val classed = _classBasedRegisters[player.baseClass]
        if (classed != null && classed.remove(objId))
            return
    }

    /**
     * @param player - messages will be sent to this Player
     * @return true if all requirements are met
     */
    private fun checkNoble(player: Player): Boolean {
        if (!player.isNoble) {
            player.sendPacket(SystemMessageId.ONLY_NOBLESS_CAN_PARTICIPATE_IN_THE_OLYMPIAD)
            return false
        }

        if (player.isSubClassActive) {
            player.sendPacket(SystemMessageId.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER)
            return false
        }

        if (player.isCursedWeaponEquipped) {
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(
                    player.cursedWeaponEquippedId
                )
            )
            return false
        }

        if (player.inventoryLimit * 0.8 <= player.inventory!!.size) {
            player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD)
            return false
        }

        if (isRegistered(player, true))
            return false

        if (isInCompetition(player, true))
            return false

        var statDat: StatsSet? = Olympiad.getNobleStats(player.objectId)
        if (statDat == null) {
            statDat = StatsSet()
            statDat[Olympiad.CLASS_ID] = player.baseClass.toDouble()
            statDat[Olympiad.CHAR_NAME] = player.name
            statDat[Olympiad.POINTS] = Olympiad.DEFAULT_POINTS.toDouble()
            statDat[Olympiad.COMP_DONE] = 0.0
            statDat[Olympiad.COMP_WON] = 0.0
            statDat[Olympiad.COMP_LOST] = 0.0
            statDat[Olympiad.COMP_DRAWN] = 0.0
            statDat["to_save"] = true

            Olympiad.addNobleStats(player.objectId, statDat)
        }

        val points = Olympiad.getNoblePoints(player.objectId)
        if (points <= 0) {
            val message = NpcHtmlMessage(0)
            message.setFile("data/html/olympiad/noble_nopoints1.htm")
            message.replace("%objectId%", player.targetId)
            player.sendPacket(message)
            return false
        }

        return true
    }

    private fun isInCompetition(player: Player, showMessage: Boolean): Boolean {
        if (!Olympiad._inCompPeriod)
            return false

        var i = OlympiadGameManager.numberOfStadiums
        while (--i >= 0) {
            val game = OlympiadGameManager.getOlympiadTask(i)!!.game ?: continue

            if (game.containsParticipant(player.objectId)) {
                if (showMessage)
                    player.sendPacket(SystemMessageId.YOU_HAVE_ALREADY_BEEN_REGISTERED_IN_A_WAITING_LIST_OF_AN_EVENT)

                return true
            }
        }
        return false
    }
}