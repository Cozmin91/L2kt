package com.l2kt.loginserver.network.clientpackets

import java.nio.charset.Charset

/**
 * This class ...
 * @version $Revision: 1.2.4.1 $ $Date: 2005/03/27 15:30:12 $
 */
abstract class ClientBasePacket(private val _decrypt: ByteArray) {
    private var _off: Int = 0

    init {
        _off = 1 // skip packet type id
    }

    fun readD(): Int {
        var result = _decrypt[_off++].toInt() and 0xff
        result = result or (_decrypt[_off++].toInt() shl 8 and 0xff00)
        result = result or (_decrypt[_off++].toInt() shl 0x10 and 0xff0000)
        result = result or (_decrypt[_off++].toInt() shl 0x18 and -0x1000000)
        return result
    }

    fun readC(): Int {
        return _decrypt[_off++].toInt() and 0xff
    }

    fun readH(): Int {
        var result = _decrypt[_off++].toInt() and 0xff
        result = result or (_decrypt[_off++].toInt() shl 8 and 0xff00)
        return result
    }

    fun readF(): Double {
        var result = (_decrypt[_off++].toInt() and 0xff).toLong()
        result = result or (_decrypt[_off++].toInt() shl 8 and 0xff00).toLong()
        result = result or (_decrypt[_off++].toInt() shl 0x10 and 0xff0000).toLong()
        result = result or (_decrypt[_off++].toInt() shl 0x18 and -0x1000000).toLong()
        result = result or (_decrypt[_off++].toLong() shl 0x20 and 0xff00000000L)
        result = result or (_decrypt[_off++].toLong() shl 0x28 and 0xff0000000000L)
        result = result or (_decrypt[_off++].toLong() shl 0x30 and 0xff000000000000L)
        result = result or (_decrypt[_off++].toLong() shl 0x38 and -0x100000000000000L)
        return java.lang.Double.longBitsToDouble(result)
    }

    fun readS(): String {
        try {
            var result = String(_decrypt, _off, _decrypt.size - _off, Charset.forName("UTF-16LE"))
            result = result.substring(0, result.indexOf("\u0000"))
            _off += result.length * 2 + 2
            return result
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ""
    }

    fun readB(length: Int): ByteArray {
        val result = ByteArray(length)
        for (i in 0 until length) {
            result[i] = _decrypt[_off + i]
        }
        _off += length
        return result
    }
}
