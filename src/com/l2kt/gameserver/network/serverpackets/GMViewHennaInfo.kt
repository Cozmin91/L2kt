package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna

class GMViewHennaInfo(private val _activeChar: Player) : L2GameServerPacket() {
    private val _hennas = arrayOfNulls<Henna>(3)
    private var _count: Int = 0

    init {
        _count = 0

        for (i in 0..2) {
            val h = _activeChar.getHenna(i + 1)
            if (h != null)
                _hennas[_count++] = h
        }
    }

    override fun writeImpl() {
        writeC(0xea)

        writeC(_activeChar.hennaStatINT)
        writeC(_activeChar.hennaStatSTR)
        writeC(_activeChar.hennaStatCON)
        writeC(_activeChar.hennaStatMEN)
        writeC(_activeChar.hennaStatDEX)
        writeC(_activeChar.hennaStatWIT)

        writeD(3) // slots?

        writeD(_count) // size
        for (i in 0 until _count) {
            writeD(_hennas[i]!!.symbolId)
            writeD(if (_hennas[i]!!.canBeUsedBy(_activeChar)) _hennas[i]!!.symbolId else 0)
        }
    }
}