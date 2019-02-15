package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.SeedProduction
import java.util.*

class BuyListSeed(private val _money: Int, private val _manorId: Int) : L2GameServerPacket() {
    private val _list: MutableList<SeedProduction>

    init {

        _list = ArrayList()
        for (s in CastleManorManager.getSeedProduction(_manorId, false)) {
            if (s.amount > 0 && s.price > 0)
                _list.add(s)
        }
    }

    override fun writeImpl() {
        writeC(0xE8)

        writeD(_money)
        writeD(_manorId)

        if (!_list.isEmpty()) {
            writeH(_list.size)
            for (s in _list) {
                writeH(0x04)
                writeD(s.id)
                writeD(s.id)
                writeD(s.amount)
                writeH(0x04)
                writeH(0x00)
                writeD(s.price)
            }
        }
    }
}