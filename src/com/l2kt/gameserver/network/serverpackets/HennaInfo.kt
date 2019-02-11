package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna

class HennaInfo(private val _activeChar: Player) : L2GameServerPacket() {
    private val _hennas = arrayOfNulls<Henna>(3)
    private var _count: Int = 0

    init {
        _count = 0

        for (i in 0..2) {
            val henna = _activeChar.getHenna(i + 1)
            if (henna != null)
                _hennas[_count++] = henna
        }
    }

    override fun writeImpl() {
        writeC(0xe4)

        writeC(_activeChar.hennaStatINT) // equip INT
        writeC(_activeChar.hennaStatSTR) // equip STR
        writeC(_activeChar.hennaStatCON) // equip CON
        writeC(_activeChar.hennaStatMEN) // equip MEM
        writeC(_activeChar.hennaStatDEX) // equip DEX
        writeC(_activeChar.hennaStatWIT) // equip WIT

        // Henna slots
        val classId = _activeChar.classId.level()
        if (classId == 1)
            writeD(2)
        else if (classId > 1)
            writeD(3)
        else
            writeD(0)

        writeD(_count) // size
        for (i in 0 until _count) {
            writeD(_hennas[i]!!.symbolId)
            writeD(if (_hennas[i]!!.canBeUsedBy(_activeChar)) _hennas[i]!!.symbolId else 0)
        }
    }
}