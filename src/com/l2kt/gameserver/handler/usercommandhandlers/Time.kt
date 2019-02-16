package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager

class Time : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        val hour = GameTimeTaskManager.gameHour
        val minute = GameTimeTaskManager.gameMinute

        val min = (if (minute < 10) "0" else "") + minute

        activeChar.sendPacket(
            SystemMessage.getSystemMessage(if (GameTimeTaskManager.isNight) SystemMessageId.TIME_S1_S2_IN_THE_NIGHT else SystemMessageId.TIME_S1_S2_IN_THE_DAY).addNumber(
                hour
            ).addString(min)
        )
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(77)
    }
}