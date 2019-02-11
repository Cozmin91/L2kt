package com.l2kt.gameserver.network.gameserverpackets

import com.l2kt.commons.logging.CLogger
import java.security.GeneralSecurityException
import java.security.interfaces.RSAPublicKey
import javax.crypto.Cipher

class BlowFishKey(blowfishKey: ByteArray, publicKey: RSAPublicKey) : GameServerBasePacket() {
    override val content: ByteArray
        get() = bytes

    init {
        writeC(0x00)
        val encrypted: ByteArray?
        try {
            val rsaCipher = Cipher.getInstance("RSA/ECB/nopadding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            encrypted = rsaCipher.doFinal(blowfishKey)

            writeD(encrypted!!.size)
            writeB(encrypted)
        } catch (e: GeneralSecurityException) {
            LOGGER.error("Error while encrypting blowfish key for transmission.", e)
        }

    }

    companion object {
        private val LOGGER = CLogger(BlowFishKey::class.java.name)
    }
}