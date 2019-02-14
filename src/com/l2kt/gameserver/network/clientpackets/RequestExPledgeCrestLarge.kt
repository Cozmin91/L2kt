package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.network.serverpackets.ExPledgeCrestLarge

class RequestExPledgeCrestLarge : L2GameClientPacket() {
    private var _crestId: Int = 0

    override fun readImpl() {
        _crestId = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val data = CrestCache.getCrest(CrestCache.CrestType.PLEDGE_LARGE, _crestId) ?: return

        player.sendPacket(ExPledgeCrestLarge(_crestId, data))
    }
}