package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.CropProcure
import com.l2kt.gameserver.model.manor.Seed
import java.util.*

class ExShowCropSetting(private val _manorId: Int) : L2GameServerPacket() {
    private val _seeds: Set<Seed>
    private val _current = HashMap<Int, CropProcure>()
    private val _next = HashMap<Int, CropProcure>()

    init {
        val manor = CastleManorManager.getInstance()
        _seeds = manor.getSeedsForCastle(_manorId)
        for (s in _seeds) {
            // Current period
            var cp = manor.getCropProcure(_manorId, s.cropId, false)
            if (cp != null)
                _current[s.cropId] = cp

            // Next period
            cp = manor.getCropProcure(_manorId, s.cropId, true)
            if (cp != null)
                _next[s.cropId] = cp
        }
    }

    public override fun writeImpl() {
        writeC(0xFE) // Id
        writeH(0x20) // SubId

        writeD(_manorId) // manor id
        writeD(_seeds.size) // size

        var cp: CropProcure
        for (s in _seeds) {
            writeD(s.cropId) // crop id
            writeD(s.level) // seed level
            writeC(1)
            writeD(s.getReward(1)) // reward 1 id
            writeC(1)
            writeD(s.getReward(2)) // reward 2 id

            writeD(s.cropLimit) // next sale limit
            writeD(0) // ???
            writeD(s.cropMinPrice) // min crop price
            writeD(s.cropMaxPrice) // max crop price

            // Current period
            if (_current.containsKey(s.cropId)) {
                cp = _current[s.cropId]!!
                writeD(cp.startAmount) // buy
                writeD(cp.price) // price
                writeC(cp.reward) // reward
            } else {
                writeD(0)
                writeD(0)
                writeC(0)
            }
            // Next period
            if (_next.containsKey(s.cropId)) {
                cp = _next[s.cropId]!!
                writeD(cp.startAmount) // buy
                writeD(cp.price) // price
                writeC(cp.reward) // reward
            } else {
                writeD(0)
                writeD(0)
                writeC(0)
            }
        }
    }
}