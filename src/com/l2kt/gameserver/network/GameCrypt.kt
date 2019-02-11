package com.l2kt.gameserver.network

class GameCrypt {
    private val _inKey = ByteArray(16)
    private val _outKey = ByteArray(16)
    private var _isEnabled: Boolean = false

    fun setKey(key: ByteArray) {
        System.arraycopy(key, 0, _inKey, 0, 16)
        System.arraycopy(key, 0, _outKey, 0, 16)
    }

    fun decrypt(raw: ByteArray, offset: Int, size: Int) {
        if (!_isEnabled)
            return

        var temp = 0
        for (i in 0 until size) {
            val temp2 = raw[offset + i].toInt() and 0xFF
            raw[offset + i] = (temp2 xor _inKey[i and 15].toInt() xor temp).toByte()
            temp = temp2
        }

        var old = _inKey[8].toInt() and 0xff
        old = old or (_inKey[9].toInt() shl 8 and 0xff00)
        old = old or (_inKey[10].toInt() shl 0x10 and 0xff0000)
        old = old or (_inKey[11].toInt() shl 0x18 and -0x1000000)

        old += size

        _inKey[8] = (old and 0xff).toByte()
        _inKey[9] = (old shr 0x08 and 0xff).toByte()
        _inKey[10] = (old shr 0x10 and 0xff).toByte()
        _inKey[11] = (old shr 0x18 and 0xff).toByte()
    }

    fun encrypt(raw: ByteArray, offset: Int, size: Int) {
        if (!_isEnabled) {
            _isEnabled = true
            return
        }

        var temp = 0
        for (i in 0 until size) {
            val temp2 = raw[offset + i].toInt() and 0xFF
            temp = temp2 xor _outKey[i and 15].toInt() xor temp
            raw[offset + i] = temp.toByte()
        }

        var old = _outKey[8].toInt() and 0xff
        old = old or (_outKey[9].toInt() shl 8 and 0xff00)
        old = old or (_outKey[10].toInt() shl 0x10 and 0xff0000)
        old = old or (_outKey[11].toInt() shl 0x18 and -0x1000000)

        old += size

        _outKey[8] = (old and 0xff).toByte()
        _outKey[9] = (old shr 0x08 and 0xff).toByte()
        _outKey[10] = (old shr 0x10 and 0xff).toByte()
        _outKey[11] = (old shr 0x18 and 0xff).toByte()
    }
}
