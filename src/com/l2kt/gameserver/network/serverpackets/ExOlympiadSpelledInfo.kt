package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

/**
 * @author godson
 */
class ExOlympiadSpelledInfo(player: Player) : L2GameServerPacket() {
    private val _playerID = player.objectId
    private val _effects = mutableListOf<Effect>()

    private class Effect(var _skillId: Int, var _level: Int, var _duration: Int)

    fun addEffect(skillId: Int, level: Int, duration: Int) {
        _effects.add(Effect(skillId, level, duration))
    }

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x2a)
        writeD(_playerID)
        writeD(_effects.size)
        for (temp in _effects) {
            writeD(temp._skillId)
            writeH(temp._level)
            writeD(temp._duration / 1000)
        }
    }
}