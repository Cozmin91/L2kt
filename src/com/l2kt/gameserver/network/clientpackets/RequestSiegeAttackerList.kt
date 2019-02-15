package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.network.serverpackets.SiegeAttackerList

class RequestSiegeAttackerList : L2GameClientPacket() {
    private var _castleId: Int = 0

    override fun readImpl() {
        _castleId = readD()
    }

    override fun runImpl() {
        client.activeChar ?: return

        val castle = CastleManager.getCastleById(_castleId) ?: return

        sendPacket(SiegeAttackerList(castle))
    }
}