package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AskJoinPledge
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestJoinPledge : L2GameClientPacket() {
    private var _target: Int = 0
    var pledgeType: Int = 0
        private set

    override fun readImpl() {
        _target = readD()
        pledgeType = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan ?: return

        val target = World.getPlayer(_target)
        if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET)
            return
        }

        if (!clan.checkClanJoinCondition(activeChar, target, pledgeType))
            return

        if (!activeChar.request.setRequest(target, this))
            return

        target.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_INVITED_YOU_TO_JOIN_THE_CLAN_S2).addCharName(
                activeChar
            ).addString(clan.name)
        )
        target.sendPacket(AskJoinPledge(activeChar.objectId, clan.name))
    }
}