package com.l2kt.loginserver.network.serverpackets

import java.io.ByteArrayOutputStream
import java.io.IOException

abstract class ServerBasePacket protected constructor() {
    private var bao: ByteArrayOutputStream = ByteArrayOutputStream()

    val length: Int
        get() = bao.size() + 2

    // reserve for checksum
    val bytes: ByteArray
        get() {
            writeD(0x00)

            val padding = bao.size() % 8
            if (padding != 0) {
                for (i in padding..7)
                    writeC(0x00)
            }

            return bao.toByteArray()
        }

    abstract val content: ByteArray

    protected fun writeD(value: Int) {
        bao.write(value and 0xff)
        bao.write(value shr 8 and 0xff)
        bao.write(value shr 16 and 0xff)
        bao.write(value shr 24 and 0xff)
    }

    protected fun writeH(value: Int) {
        bao.write(value and 0xff)
        bao.write(value shr 8 and 0xff)
    }

    protected fun writeC(value: Int) {
        bao.write(value and 0xff)
    }

    protected fun writeF(org: Double) {
        val value = java.lang.Double.doubleToRawLongBits(org)
        bao.write((value and 0xff).toInt())
        bao.write((value shr 8 and 0xff).toInt())
        bao.write((value shr 16 and 0xff).toInt())
        bao.write((value shr 24 and 0xff).toInt())
        bao.write((value shr 32 and 0xff).toInt())
        bao.write((value shr 40 and 0xff).toInt())
        bao.write((value shr 48 and 0xff).toInt())
        bao.write((value shr 56 and 0xff).toInt())
    }

    protected fun writeS(text: String?) {
        try {
            if (text != null) {
                bao.write(text.toByteArray(charset("UTF-16LE")))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        bao.write(0)
        bao.write(0)
    }

    protected fun writeB(array: ByteArray) {
        try {
            bao.write(array)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}