package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager

class ExShowCropInfo(private val _manorId: Int, nextPeriod: Boolean, private val _hideButtons: Boolean) :
    L2GameServerPacket() {
    private val manor = CastleManorManager.getInstance()
    private val _crops = if (nextPeriod && !manor.isManorApproved) null else manor.getCropProcure(_manorId, nextPeriod)

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x1D)
        writeC(if (_hideButtons) 0x01 else 0x00)
        writeD(_manorId)
        writeD(0)
        if (_crops == null) {
            writeD(0)
            return
        }

        writeD(_crops.size)
        for (crop in _crops) {
            writeD(crop.id)
            writeD(crop.amount)
            writeD(crop.startAmount)
            writeD(crop.price)
            writeC(crop.reward)

            val seed = CastleManorManager.getInstance().getSeedByCrop(crop.id)
            if (seed == null) {
                writeD(0) // Seed level
                writeC(0x01) // Reward 1
                writeD(0) // Reward 1 - item id
                writeC(0x01) // Reward 2
                writeD(0) // Reward 2 - item id
            } else {
                writeD(seed.level) // Seed level
                writeC(0x01) // Reward 1
                writeD(seed.getReward(1)) // Reward 1 - item id
                writeC(0x01) // Reward 2
                writeD(seed.getReward(2)) // Reward 2 - item id
            }
        }
    }
}