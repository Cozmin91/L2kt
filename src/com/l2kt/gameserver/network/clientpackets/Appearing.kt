package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.UserInfo

class Appearing : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (activeChar.isTeleporting)
            activeChar.onTeleported()

        sendPacket(UserInfo(activeChar))
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }
}