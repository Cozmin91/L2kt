package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.holder.IntIntHolder
import java.util.*

/**
 * format d d(dd)
 */
class StatusUpdate(`object`: WorldObject) : L2GameServerPacket() {

    private val _objectId: Int
    private val _attributes: MutableList<IntIntHolder>

    init {
        _attributes = ArrayList()
        _objectId = `object`.objectId
    }

    fun addAttribute(id: Int, level: Int) {
        _attributes.add(IntIntHolder(id, level))
    }

    override fun writeImpl() {
        writeC(0x0e)
        writeD(_objectId)
        writeD(_attributes.size)

        for (temp in _attributes) {
            writeD(temp.id)
            writeD(temp.value)
        }
    }

    companion object {
        const val LEVEL = 0x01
        const val EXP = 0x02
        const val STR = 0x03
        const val DEX = 0x04
        const val CON = 0x05
        const val INT = 0x06
        const val WIT = 0x07
        const val MEN = 0x08

        const val CUR_HP = 0x09
        const val MAX_HP = 0x0a
        const val CUR_MP = 0x0b
        const val MAX_MP = 0x0c

        const val SP = 0x0d
        const val CUR_LOAD = 0x0e
        const val MAX_LOAD = 0x0f

        const val P_ATK = 0x11
        const val ATK_SPD = 0x12
        const val P_DEF = 0x13
        const val EVASION = 0x14
        const val ACCURACY = 0x15
        const val CRITICAL = 0x16
        const val M_ATK = 0x17
        const val CAST_SPD = 0x18
        const val M_DEF = 0x19
        const val PVP_FLAG = 0x1a
        const val KARMA = 0x1b

        const val CUR_CP = 0x21
        const val MAX_CP = 0x22
    }
}