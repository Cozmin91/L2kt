package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.CropProcure

class ExShowProcureCropDetail(private val _cropId: Int) : L2GameServerPacket() {
    private val _castleCrops = mutableMapOf<Int, CropProcure>()

    init {

        for (c in CastleManager.castles) {
            val cropItem = CastleManorManager.getCropProcure(c.castleId, _cropId, false)
            if (cropItem != null && cropItem.amount > 0)
                _castleCrops[c.castleId] = cropItem
        }
    }

    public override fun writeImpl() {
        writeC(0xFE)
        writeH(0x22)

        writeD(_cropId)
        writeD(_castleCrops.size)

        for ((key, crop) in _castleCrops) {

            writeD(key)
            writeD(crop.amount)
            writeD(crop.price)
            writeC(crop.reward)
        }
    }
}