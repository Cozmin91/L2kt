package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AskJoinAlly
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestJoinAlly : L2GameClientPacket() {
    private var _id: Int = 0

    override fun readImpl() {
        _id = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = activeChar.clan
        if (clan == null) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
            return
        }

        val target = World.getInstance().getPlayer(_id)
        if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET)
            return
        }

        if (!Clan.checkAllyJoinCondition(activeChar, target))
            return

        if (!activeChar.request.setRequest(target, this))
            return

        target.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.S2_ALLIANCE_LEADER_OF_S1_REQUESTED_ALLIANCE).addString(
                clan.allyName
            ).addCharName(activeChar)
        )
        target.sendPacket(AskJoinAlly(activeChar.objectId, clan.allyName))
        return
    }
}