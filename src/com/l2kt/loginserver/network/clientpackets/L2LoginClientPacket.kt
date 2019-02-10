package com.l2kt.loginserver.network.clientpackets

import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.mmocore.ReceivablePacket

import com.l2kt.loginserver.network.LoginClient

abstract class L2LoginClientPacket : ReceivablePacket<LoginClient>() {

    override fun read(): Boolean {
        return try {
            readImpl()
        } catch (e: Exception) {
            LOGGER.error("Failed reading {}. ", e, javaClass.simpleName)
            false
        }

    }

    protected abstract fun readImpl(): Boolean

    companion object {
        val LOGGER = CLogger(L2LoginClientPacket::class.java.name)
    }
}
