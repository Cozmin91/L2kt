package com.l2kt.loginserver.network.gameserverpackets

import com.l2kt.loginserver.network.clientpackets.ClientBasePacket
import java.security.GeneralSecurityException
import java.security.interfaces.RSAPrivateKey
import java.util.logging.Logger
import javax.crypto.Cipher

class BlowFishKey(decrypt: ByteArray, privateKey: RSAPrivateKey) : ClientBasePacket(decrypt) {
    lateinit var key: ByteArray
        internal set

    init {
        val size = readD()
        val tempKey = readB(size)
        try {
            val tempDecryptKey: ByteArray
            val rsaCipher = Cipher.getInstance("RSA/ECB/nopadding")
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            tempDecryptKey = rsaCipher.doFinal(tempKey)
            // there are nulls before the key we must remove them
            var i = 0
            val len = tempDecryptKey.size
            while (i < len) {
                if (tempDecryptKey[i].toInt() != 0)
                    break
                i++
            }
            key = ByteArray(len - i)
            System.arraycopy(tempDecryptKey, i, key, 0, len - i)
        } catch (e: GeneralSecurityException) {
            log.severe("Error While decrypting blowfish key (RSA)")
            e.printStackTrace()
        }

    }

    companion object {
        private val log = Logger.getLogger(BlowFishKey::class.java.name)
    }
}