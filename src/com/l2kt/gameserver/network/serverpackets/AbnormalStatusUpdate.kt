package com.l2kt.gameserver.network.serverpackets

class AbnormalStatusUpdate : L2GameServerPacket() {
    private val _effects = mutableListOf<Effect>()

    private class Effect(var _skillId: Int, var _level: Int, var _duration: Int)

    fun addEffect(skillId: Int, level: Int, duration: Int) {
        _effects.add(Effect(skillId, level, duration))
    }

    override fun writeImpl() {
        writeC(0x7f)
        writeH(_effects.size)

        for (temp in _effects) {
            writeD(temp._skillId)
            writeH(temp._level)

            if (temp._duration == -1)
                writeD(-1)
            else
                writeD(temp._duration / 1000)
        }
    }
}