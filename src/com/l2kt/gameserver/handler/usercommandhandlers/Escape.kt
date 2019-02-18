package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound

class Escape : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        if (activeChar.isCastingNow || activeChar.isSitting || activeChar.isMovementDisabled || activeChar.isOutOfControl || activeChar.isInOlympiadMode || activeChar.isInObserverMode || activeChar.isFestivalParticipant || activeChar.isInJail || activeChar.isInsideZone(
                ZoneId.BOSS
            )
        ) {
            activeChar.sendPacket(SystemMessageId.NO_UNSTUCK_PLEASE_SEND_PETITION)
            return false
        }

        activeChar.stopMove(null)

        // Official timer 5 minutes, for GM 1 second
        if (activeChar.isGM)
            activeChar.doCast(SkillTable.getInfo(2100, 1)!!)
        else {
            activeChar.sendPacket(PlaySound("systemmsg_e.809"))
            activeChar.sendPacket(SystemMessageId.STUCK_TRANSPORT_IN_FIVE_MINUTES)

            activeChar.doCast(SkillTable.getInfo(2099, 1)!!)
        }

        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(52)
    }
}