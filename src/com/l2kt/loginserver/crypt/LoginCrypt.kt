package com.l2kt.loginserver.crypt

import com.l2kt.commons.random.Rnd
import java.io.IOException

class LoginCrypt {

    private var _staticCrypt: NewCrypt? = null
    private var _crypt: NewCrypt? = null
    private var _static = true

    fun setKey(key: ByteArray) {
        _staticCrypt = NewCrypt(STATIC_BLOWFISH_KEY)
        _crypt = NewCrypt(key)
    }

    @Throws(IOException::class)
    fun decrypt(raw: ByteArray, offset: Int, size: Int): Boolean {
        _crypt!!.decrypt(raw, offset, size)
        return NewCrypt.verifyChecksum(raw, offset, size)
    }

    @Throws(IOException::class)
    fun encrypt(raw: ByteArray, offset: Int, size: Int): Int {
        var size = size
        size += 4

        if (_static) {
            // reserve for XOR "key"
            size += 4

            // padding
            size += 8 - size % 8
            NewCrypt.encXORPass(raw, offset, size, Rnd.nextInt())
            _staticCrypt!!.crypt(raw, offset, size)

            _static = false
        } else {
            // padding
            size += 8 - size % 8
            NewCrypt.appendChecksum(raw, offset, size)
            _crypt!!.crypt(raw, offset, size)
        }
        return size
    }

    companion object {
        private val STATIC_BLOWFISH_KEY = byteArrayOf(
            0x6b.toByte(),
            0x60.toByte(),
            0xcb.toByte(),
            0x5b.toByte(),
            0x82.toByte(),
            0xce.toByte(),
            0x90.toByte(),
            0xb1.toByte(),
            0xcc.toByte(),
            0x2b.toByte(),
            0x6c.toByte(),
            0x55.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte(),
            0x6c.toByte()
        )
    }
}
