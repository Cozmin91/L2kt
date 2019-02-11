package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.mmocore.SendablePacket

import com.l2kt.gameserver.network.L2GameClient

abstract class L2GameServerPacket : SendablePacket<L2GameClient>() {

    val type: String
        get() = "[S] " + javaClass.simpleName

    protected abstract fun writeImpl()

    override fun write() {
        if (Config.PACKET_HANDLER_DEBUG)
            LOGGER.info(type)

        try {
            writeImpl()
        } catch (t: Throwable) {
            LOGGER.error("Failed writing {} for {}. ", t, type, client.toString())
        }

    }

    open fun runImpl() {}

    companion object {
        val LOGGER = CLogger(L2GameServerPacket::class.java.name)
    }
}