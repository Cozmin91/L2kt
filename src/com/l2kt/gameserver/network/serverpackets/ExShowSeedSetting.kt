package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.manor.Seed
import com.l2kt.gameserver.model.manor.SeedProduction
import java.util.*

class ExShowSeedSetting(private val _manorId: Int) : L2GameServerPacket() {
    private val _seeds: Set<Seed>
    private val _current = HashMap<Int, SeedProduction>()
    private val _next = HashMap<Int, SeedProduction>()

    init {
        val manor = CastleManorManager.getInstance()
        _seeds = manor.getSeedsForCastle(_manorId)

        for (s in _seeds) {
            // Current period
            var sp = manor.getSeedProduct(_manorId, s.seedId, false)
            if (sp != null)
                _current[s.seedId] = sp

            // Next period
            sp = manor.getSeedProduct(_manorId, s.seedId, true)
            if (sp != null)
                _next[s.seedId] = sp
        }
    }

    public override fun writeImpl() {
        writeC(0xFE)
        writeH(0x1F)

        writeD(_manorId)
        writeD(_seeds.size)

        var sp: SeedProduction
        for (s in _seeds) {
            writeD(s.seedId) // seed id
            writeD(s.level) // level
            writeC(1)
            writeD(s.getReward(1)) // reward 1 id
            writeC(1)
            writeD(s.getReward(2)) // reward 2 id

            writeD(s.seedLimit) // next sale limit
            writeD(s.seedReferencePrice) // price for castle to produce 1
            writeD(s.seedMinPrice) // min seed price
            writeD(s.seedMaxPrice) // max seed price

            // Current period
            if (_current.containsKey(s.seedId)) {
                sp = _current[s.seedId]!!
                writeD(sp.startAmount) // sales
                writeD(sp.price) // price
            } else {
                writeD(0)
                writeD(0)
            }
            // Next period
            if (_next.containsKey(s.seedId)) {
                sp = _next[s.seedId]!!
                writeD(sp.startAmount) // sales
                writeD(sp.price) // price
            } else {
                writeD(0)
                writeD(0)
            }
        }
    }
}