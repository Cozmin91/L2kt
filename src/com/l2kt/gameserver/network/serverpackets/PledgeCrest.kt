package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.cache.CrestCache

class PledgeCrest(private val _crestId: Int) : L2GameServerPacket() {
    private val _data = CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE, _crestId)

    override fun writeImpl() {
        writeC(0x6c)
        writeD(_crestId)
        if (_data != null) {
            writeD(_data.size)
            writeB(_data)
        } else
            writeD(0)
    }
}