package com.l2kt.gameserver.network.serverpackets

import java.util.*

class ExEnchantSkillInfo(
    private val _id: Int,
    private val _level: Int,
    private val _spCost: Int,
    private val _xpCost: Int,
    private val _rate: Int
) : L2GameServerPacket() {
    private val _reqs: ArrayList<Req> = ArrayList()

    internal inner class Req(var type: Int, var id: Int, var count: Int, var unk: Int)

    fun addRequirement(type: Int, id: Int, count: Int, unk: Int) {
        _reqs.add(Req(type, id, count, unk))
    }

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x18)

        writeD(_id)
        writeD(_level)
        writeD(_spCost)
        writeQ(_xpCost.toLong())
        writeD(_rate)

        writeD(_reqs.size)

        for (temp in _reqs) {
            writeD(temp.type)
            writeD(temp.id)
            writeD(temp.count)
            writeD(temp.unk)
        }
    }
}