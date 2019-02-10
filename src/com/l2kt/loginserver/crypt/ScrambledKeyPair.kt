package com.l2kt.loginserver.crypt

import java.math.BigInteger
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey

class ScrambledKeyPair(val keyPair: KeyPair) {
    val scrambledModulus: ByteArray

    init {
        scrambledModulus = scrambleModulus((keyPair.public as RSAPublicKey).modulus)
    }

    private fun scrambleModulus(modulus: BigInteger): ByteArray {
        var scrambledMod = modulus.toByteArray()

        if (scrambledMod.size == 0x81 && scrambledMod[0].toInt() == 0x00) {
            val temp = ByteArray(0x80)
            System.arraycopy(scrambledMod, 1, temp, 0, 0x80)
            scrambledMod = temp
        }
        // step 1 : 0x4d-0x50 <-> 0x00-0x04
        for (i in 0..3) {
            val temp = scrambledMod[i]
            scrambledMod[i] = scrambledMod[0x4d + i]
            scrambledMod[0x4d + i] = temp
        }
        // step 2 : xor first 0x40 bytes with last 0x40 bytes
        for (i in 0..63)
            scrambledMod[i] = (scrambledMod[i].toInt() xor scrambledMod[0x40 + i].toInt()).toByte()
        // step 3 : xor bytes 0x0d-0x10 with bytes 0x34-0x38
        for (i in 0..3)
            scrambledMod[0x0d + i] = (scrambledMod[0x0d + i].toInt() xor scrambledMod[0x34 + i].toInt()).toByte()
        // step 4 : xor last 0x40 bytes with first 0x40 bytes
        for (i in 0..63)
            scrambledMod[0x40 + i] = (scrambledMod[0x40 + i].toInt() xor scrambledMod[i].toInt()).toByte()

        return scrambledMod
    }
}