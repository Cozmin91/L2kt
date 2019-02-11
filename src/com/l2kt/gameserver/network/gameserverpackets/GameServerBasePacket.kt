package com.l2kt.gameserver.network.gameserverpackets

import java.io.ByteArrayOutputStream

abstract class GameServerBasePacket protected constructor() {
    private val _bao = ByteArrayOutputStream()

    val length: Int
        get() = _bao.size() + 2

    // reserve for checksum
    val bytes: ByteArray
        get() {
            writeD(0x00)

            val padding = _bao.size() % 8
            if (padding != 0) {
                for (i in padding..7) {
                    writeC(0x00)
                }
            }

            return _bao.toByteArray()
        }

    abstract val content: ByteArray

    protected fun writeD(value: Int) {
        _bao.write(value and 0xff)
        _bao.write(value shr 8 and 0xff)
        _bao.write(value shr 16 and 0xff)
        _bao.write(value shr 24 and 0xff)
    }

    protected fun writeH(value: Int) {
        _bao.write(value and 0xff)
        _bao.write(value shr 8 and 0xff)
    }

    protected fun writeC(value: Int) {
        _bao.write(value and 0xff)
    }

    protected fun writeF(org: Double) {
        val value = java.lang.Double.doubleToRawLongBits(org)
        _bao.write((value and 0xff).toInt())
        _bao.write((value shr 8 and 0xff).toInt())
        _bao.write((value shr 16 and 0xff).toInt())
        _bao.write((value shr 24 and 0xff).toInt())
        _bao.write((value shr 32 and 0xff).toInt())
        _bao.write((value shr 40 and 0xff).toInt())
        _bao.write((value shr 48 and 0xff).toInt())
        _bao.write((value shr 56 and 0xff).toInt())
    }

    protected fun writeS(text: String) {
        _bao.write(text.toByteArray(charset("UTF-16LE")))

        _bao.write(0)
        _bao.write(0)
    }

    protected fun writeB(array: ByteArray) {
            _bao.write(array)
    }
}