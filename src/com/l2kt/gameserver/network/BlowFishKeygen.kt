package com.l2kt.gameserver.network

import com.l2kt.commons.random.Rnd

/**
 * Blowfish keygen for GameServer client connections
 * @author KenM
 */
object BlowFishKeygen {
    private const val CRYPT_KEYS_SIZE = 20
    private val CRYPT_KEYS = Array(CRYPT_KEYS_SIZE) { ByteArray(16) }

    /**
     * Returns a key from this keygen pool, the logical ownership is retained by this keygen.<BR></BR>
     * Thus when getting a key with interests other then read-only a copy must be performed.<BR></BR>
     * @return A key from this keygen pool.
     */
    val randomKey: ByteArray
        get() = CRYPT_KEYS[Rnd[CRYPT_KEYS_SIZE]]

    init {
        // init the GS encryption keys on class load

        for (i in 0 until CRYPT_KEYS_SIZE) {
            // randomize the 8 first bytes
            for (j in 0 until CRYPT_KEYS[i].size) {
                CRYPT_KEYS[i][j] = Rnd[255].toByte()
            }

            // the last 8 bytes are static
            CRYPT_KEYS[i][8] = 0xc8.toByte()
            CRYPT_KEYS[i][9] = 0x27.toByte()
            CRYPT_KEYS[i][10] = 0x93.toByte()
            CRYPT_KEYS[i][11] = 0x01.toByte()
            CRYPT_KEYS[i][12] = 0xa1.toByte()
            CRYPT_KEYS[i][13] = 0x6c.toByte()
            CRYPT_KEYS[i][14] = 0x31.toByte()
            CRYPT_KEYS[i][15] = 0x97.toByte()
        }
    }
}// block instantiation
