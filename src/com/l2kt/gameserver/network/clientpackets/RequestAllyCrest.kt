package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.network.serverpackets.AllyCrest

class RequestAllyCrest : L2GameClientPacket() {
    private var _crestId: Int = 0

    override fun readImpl() {
        _crestId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val data = CrestCache.getCrest(CrestCache.CrestType.ALLY, _crestId) ?: return

        player.sendPacket(AllyCrest(_crestId, data))
    }
}