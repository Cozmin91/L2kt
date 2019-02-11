package com.l2kt.loginserver.crypt

import java.io.IOException

class NewCrypt(blowfishKey: ByteArray) {
    private val _crypt: BlowfishEngine
    private val _decrypt: BlowfishEngine

    init {
        _crypt = BlowfishEngine()
        _crypt.init(true, blowfishKey)
        _decrypt = BlowfishEngine()
        _decrypt.init(false, blowfishKey)
    }

    constructor(key: String) : this(key.toByteArray()) {}

    @Throws(IOException::class)
    fun decrypt(raw: ByteArray): ByteArray {
        val result = ByteArray(raw.size)
        val count = raw.size / 8

        for (i in 0 until count)
            _decrypt.processBlock(raw, i * 8, result, i * 8)

        return result
    }

    @Throws(IOException::class)
    fun decrypt(raw: ByteArray, offset: Int, size: Int) {
        val result = ByteArray(size)
        val count = size / 8

        for (i in 0 until count)
            _decrypt.processBlock(raw, offset + i * 8, result, i * 8)

        System.arraycopy(result, 0, raw, offset, size)
    }

    @Throws(IOException::class)
    fun crypt(raw: ByteArray): ByteArray {
        val count = raw.size / 8
        val result = ByteArray(raw.size)

        for (i in 0 until count)
            _crypt.processBlock(raw, i * 8, result, i * 8)

        return result
    }

    @Throws(IOException::class)
    fun crypt(raw: ByteArray, offset: Int, size: Int) {
        val count = size / 8
        val result = ByteArray(size)

        for (i in 0 until count)
            _crypt.processBlock(raw, offset + i * 8, result, i * 8)

        System.arraycopy(result, 0, raw, offset, size)
    }

    companion object {

        fun verifyChecksum(raw: ByteArray): Boolean {
            return NewCrypt.verifyChecksum(raw, 0, raw.size)
        }

        fun verifyChecksum(raw: ByteArray, offset: Int, size: Int): Boolean {
            // check if size is multiple of 4 and if there is more then only the checksum
            if (size and 3 != 0 || size <= 4)
                return false

            var chksum: Long = 0
            val count = size - 4
            var check: Long
            var i: Int = offset

            while (i < count) {
                check = (raw[i].toInt() and 0xff).toLong()
                check = check or (raw[i + 1].toInt() shl 8 and 0xff00).toLong()
                check = check or (raw[i + 2].toInt() shl 0x10 and 0xff0000).toLong()
                check = check or (raw[i + 3].toInt() shl 0x18 and -0x1000000).toLong()

                chksum = chksum xor check
                i += 4
            }

            check = (raw[i].toInt() and 0xff).toLong()
            check = check or (raw[i + 1].toInt() shl 8 and 0xff00).toLong()
            check = check or (raw[i + 2].toInt() shl 0x10 and 0xff0000).toLong()
            check = check or (raw[i + 3].toInt() shl 0x18 and -0x1000000).toLong()

            return check == chksum
        }

        fun appendChecksum(raw: ByteArray) {
            NewCrypt.appendChecksum(raw, 0, raw.size)
        }

        fun appendChecksum(raw: ByteArray, offset: Int, size: Int) {
            var chksum: Long = 0
            val count = size - 4
            var ecx: Long
            var i: Int

            i = offset
            while (i < count) {
                ecx = (raw[i].toInt() and 0xff).toLong()
                ecx = ecx or (raw[i + 1].toInt() shl 8 and 0xff00).toLong()
                ecx = ecx or (raw[i + 2].toInt() shl 0x10 and 0xff0000).toLong()
                ecx = ecx or (raw[i + 3].toInt() shl 0x18 and -0x1000000).toLong()

                chksum = chksum xor ecx
                i += 4
            }

            ecx = (raw[i].toInt() and 0xff).toLong()
            ecx = ecx or (raw[i + 1].toInt() shl 8 and 0xff00).toLong()
            ecx = ecx or (raw[i + 2].toInt() shl 0x10 and 0xff0000).toLong()
            ecx = ecx or (raw[i + 3].toInt() shl 0x18 and -0x1000000).toLong()

            raw[i] = (chksum and 0xff).toByte()
            raw[i + 1] = (chksum shr 0x08 and 0xff).toByte()
            raw[i + 2] = (chksum shr 0x10 and 0xff).toByte()
            raw[i + 3] = (chksum shr 0x18 and 0xff).toByte()
        }

        /**
         * Packet is first XOR encoded with `key`.<br></br>
         * Then, the last 4 bytes are overwritten with the the XOR "key".<br></br>
         * Thus this assume that there is enough room for the key to fit without overwriting data.
         * @param raw The raw bytes to be encrypted
         * @param key The 4 bytes (int) XOR key
         */
        fun encXORPass(raw: ByteArray, key: Int) {
            NewCrypt.encXORPass(raw, 0, raw.size, key)
        }

        /**
         * Packet is first XOR encoded with `key`.<br></br>
         * Then, the last 4 bytes are overwritten with the the XOR "key".<br></br>
         * Thus this assume that there is enough room for the key to fit without overwriting data.
         * @param raw The raw bytes to be encrypted
         * @param offset The begining of the data to be encrypted
         * @param size Length of the data to be encrypted
         * @param key The 4 bytes (int) XOR key
         */
        fun encXORPass(raw: ByteArray, offset: Int, size: Int, key: Int) {
            val stop = size - 8
            var pos = 4 + offset
            var edx: Int
            var ecx = key // Initial xor key

            while (pos < stop) {
                edx = raw[pos].toInt() and 0xFF
                edx = edx or (raw[pos + 1].toInt() and 0xFF shl 8)
                edx = edx or (raw[pos + 2].toInt() and 0xFF shl 16)
                edx = edx or (raw[pos + 3].toInt() and 0xFF shl 24)

                ecx += edx

                edx = edx xor ecx

                raw[pos++] = (edx and 0xFF).toByte()
                raw[pos++] = (edx shr 8 and 0xFF).toByte()
                raw[pos++] = (edx shr 16 and 0xFF).toByte()
                raw[pos++] = (edx shr 24 and 0xFF).toByte()
            }

            raw[pos++] = (ecx and 0xFF).toByte()
            raw[pos++] = (ecx shr 8 and 0xFF).toByte()
            raw[pos++] = (ecx shr 16 and 0xFF).toByte()
            raw[pos] = (ecx shr 24 and 0xFF).toByte()
        }
    }
}