package com.l2kt.gameserver.network.serverpackets

import java.math.BigInteger
import java.util.*

/**
 * This class is made to create packets with any format
 * @author Maktakien
 */
class AdminForgePacket : L2GameServerPacket() {
    private val _parts = ArrayList<Part>()

    private inner class Part(var b: Byte, var str: String)

    override fun writeImpl() {
        for (p in _parts)
            generate(p.b, p.str)
    }

    fun generate(b: Byte, string: String): Boolean {
        if (b == 'C'.toByte() || b == 'c'.toByte()) {
            writeC(Integer.decode(string)!!)
            return true
        } else if (b == 'D'.toByte() || b == 'd'.toByte()) {
            writeD(Integer.decode(string)!!)
            return true
        } else if (b == 'H'.toByte() || b == 'h'.toByte()) {
            writeH(Integer.decode(string)!!)
            return true
        } else if (b == 'F'.toByte() || b == 'f'.toByte()) {
            writeF(java.lang.Double.parseDouble(string))
            return true
        } else if (b == 'S'.toByte() || b == 's'.toByte()) {
            writeS(string)
            return true
        } else if (b == 'B'.toByte() || b == 'b'.toByte() || b == 'X'.toByte() || b == 'x'.toByte()) {
            writeB(BigInteger(string).toByteArray())
            return true
        }
        return false
    }

    fun addPart(b: Byte, string: String) {
        _parts.add(Part(b, string))
    }
}