package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.DuelManager

class RequestDuelSurrender : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        DuelManager.getInstance().doSurrender(client.activeChar)
    }
}