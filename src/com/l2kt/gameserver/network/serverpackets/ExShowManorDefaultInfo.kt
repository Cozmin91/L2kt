package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.Seed

class ExShowManorDefaultInfo(private val _hideButtons: Boolean) : L2GameServerPacket() {
    private val _crops: List<Seed> = CastleManorManager.crops

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x1E)
        writeC(if (_hideButtons) 0x01 else 0x00)
        writeD(_crops.size)
        for (crop in _crops) {
            writeD(crop.cropId) // crop Id
            writeD(crop.level) // level
            writeD(crop.seedReferencePrice) // seed price
            writeD(crop.cropReferencePrice) // crop price
            writeC(1) // Reward 1 type
            writeD(crop.getReward(1)) // Reward 1 itemId
            writeC(1) // Reward 2 type
            writeD(crop.getReward(2)) // Reward 2 itemId
        }
    }
}