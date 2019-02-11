package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager

class ExShowSeedInfo(private val _manorId: Int, nextPeriod: Boolean, private val _hideButtons: Boolean) :
    L2GameServerPacket() {
    val manor = CastleManorManager.getInstance()
    private val _seeds = if (nextPeriod && !manor.isManorApproved) null else manor.getSeedProduction(_manorId, nextPeriod)


    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x1C)
        writeC(if (_hideButtons) 0x01 else 0x00)
        writeD(_manorId)
        writeD(0)

        if (_seeds == null) {
            writeD(0)
            return
        }

        writeD(_seeds.size)
        for (seed in _seeds) {
            writeD(seed.id) // Seed id
            writeD(seed.amount) // Left to buy
            writeD(seed.startAmount) // Started amount
            writeD(seed.price) // Sell Price

            val s = CastleManorManager.getInstance().getSeed(seed.id)
            if (s == null) {
                writeD(0) // Seed level
                writeC(0x01) // Reward 1
                writeD(0) // Reward 1 - item id
                writeC(0x01) // Reward 2
                writeD(0) // Reward 2 - item id
            } else {
                writeD(s.level) // Seed level
                writeC(0x01) // Reward 1
                writeD(s.getReward(1)) // Reward 1 - item id
                writeC(0x01) // Reward 2
                writeD(s.getReward(2)) // Reward 2 - item id
            }
        }
    }
}