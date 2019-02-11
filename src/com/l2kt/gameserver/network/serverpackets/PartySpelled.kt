package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Servitor

class PartySpelled(private val _activeChar: Creature?) : L2GameServerPacket() {
    private val _effects = mutableListOf<Effect>()

    private inner class Effect(var _skillId: Int, var _dat: Int, var _duration: Int)

    override fun writeImpl() {
        if (_activeChar == null)
            return

        writeC(0xee)
        writeD(if (_activeChar is Servitor) 2 else if (_activeChar is Pet) 1 else 0)
        writeD(_activeChar.objectId)
        writeD(_effects.size)
        for (temp in _effects) {
            writeD(temp._skillId)
            writeH(temp._dat)
            writeD(temp._duration / 1000)
        }
    }

    fun addPartySpelledEffect(skillId: Int, dat: Int, duration: Int) {
        _effects.add(Effect(skillId, dat, duration))
    }
}