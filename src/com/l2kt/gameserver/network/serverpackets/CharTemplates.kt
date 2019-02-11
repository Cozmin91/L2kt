package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import java.util.*

class CharTemplates : L2GameServerPacket() {
    private val _chars = ArrayList<PlayerTemplate>()

    fun addChar(template: PlayerTemplate) {
        _chars.add(template)
    }

    override fun writeImpl() {
        writeC(0x17)
        writeD(_chars.size)

        for (temp in _chars) {

            writeD(temp.race.ordinal)
            writeD(temp.classId.id)
            writeD(0x46)
            writeD(temp.baseSTR)
            writeD(0x0a)
            writeD(0x46)
            writeD(temp.baseDEX)
            writeD(0x0a)
            writeD(0x46)
            writeD(temp.baseCON)
            writeD(0x0a)
            writeD(0x46)
            writeD(temp.baseINT)
            writeD(0x0a)
            writeD(0x46)
            writeD(temp.baseWIT)
            writeD(0x0a)
            writeD(0x46)
            writeD(temp.baseMEN)
            writeD(0x0a)
        }
    }
}