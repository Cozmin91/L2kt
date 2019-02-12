package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.network.serverpackets.SiegeDefenderList

class RequestSiegeDefenderList : L2GameClientPacket() {
    private var _castleId: Int = 0

    override fun readImpl() {
        _castleId = readD()
    }

    override fun runImpl() {
        client.activeChar ?: return

        val castle = CastleManager.getInstance().getCastleById(_castleId) ?: return

        sendPacket(SiegeDefenderList(castle))
    }
}