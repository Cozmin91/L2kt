package com.l2kt.gameserver.network.serverpackets

class AcquireSkillInfo(
    private val _id: Int,
    private val _level: Int,
    private val _spCost: Int,
    private val _mode: Int
) : L2GameServerPacket() {
    private val _reqs = mutableListOf<Req>()

    private class Req(var type: Int, var itemId: Int, var count: Int, var unk: Int)

    fun addRequirement(type: Int, id: Int, count: Int, unk: Int) {
        _reqs.add(Req(type, id, count, unk))
    }

    override fun writeImpl() {
        writeC(0x8b)
        writeD(_id)
        writeD(_level)
        writeD(_spCost)
        writeD(_mode) // c4

        writeD(_reqs.size)

        for (temp in _reqs) {
            writeD(temp.type)
            writeD(temp.itemId)
            writeD(temp.count)
            writeD(temp.unk)
        }
    }
}