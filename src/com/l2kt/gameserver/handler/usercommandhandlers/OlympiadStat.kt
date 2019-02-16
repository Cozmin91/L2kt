package com.l2kt.gameserver.handler.usercommandhandlers

import com.l2kt.gameserver.handler.IUserCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class OlympiadStat : IUserCommandHandler {

    override fun useUserCommand(id: Int, activeChar: Player): Boolean {
        if (!activeChar.isNoble) {
            activeChar.sendPacket(SystemMessageId.NOBLESSE_ONLY)
            return false
        }

        val nobleObjId = activeChar.objectId
        val sm =
            SystemMessage.getSystemMessage(SystemMessageId.THE_CURRENT_RECORD_FOR_THIS_OLYMPIAD_SESSION_IS_S1_MATCHES_S2_WINS_S3_DEFEATS_YOU_HAVE_EARNED_S4_OLYMPIAD_POINTS)
        sm.addNumber(Olympiad.getInstance().getCompetitionDone(nobleObjId))
        sm.addNumber(Olympiad.getInstance().getCompetitionWon(nobleObjId))
        sm.addNumber(Olympiad.getInstance().getCompetitionLost(nobleObjId))
        sm.addNumber(Olympiad.getInstance().getNoblePoints(nobleObjId))
        activeChar.sendPacket(sm)
        return true
    }

    override val userCommandList: IntArray get() = COMMAND_IDS

    companion object {
        private val COMMAND_IDS = intArrayOf(109)
    }
}