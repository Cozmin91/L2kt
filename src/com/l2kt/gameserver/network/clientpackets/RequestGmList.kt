package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.AdminData

class RequestGmList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        AdminData.sendListToPlayer(activeChar)
    }
}