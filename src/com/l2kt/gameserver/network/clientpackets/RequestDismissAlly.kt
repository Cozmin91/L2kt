package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId

class RequestDismissAlly : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!activeChar.isClanLeader) {
            activeChar.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER)
            return
        }
        activeChar.clan.dissolveAlly(activeChar)
    }
}