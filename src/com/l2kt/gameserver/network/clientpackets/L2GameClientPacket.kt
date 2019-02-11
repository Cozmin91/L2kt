package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.mmocore.ReceivablePacket
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import java.nio.BufferUnderflowException

/**
 * Packets received by the gameserver from clients.
 */
abstract class L2GameClientPacket : ReceivablePacket<L2GameClient>() {

    /**
     * @return A String with this packet name for debuging purposes
     */
    val type: String
        get() = "[C] " + javaClass.simpleName

    protected abstract fun readImpl()

    protected abstract fun runImpl()

    override fun read(): Boolean {
        if (Config.PACKET_HANDLER_DEBUG)
            LOGGER.info(type)

        try {
            readImpl()
            return true
        } catch (e: Exception) {
            if (e is BufferUnderflowException) {
                client.onBufferUnderflow()
                return false
            }
            LOGGER.error("Failed reading {} for {}. ", e, type, client.toString())
        }

        return false
    }

    override fun run() {
        try {
            runImpl()

            // Depending of the packet send, removes spawn protection
            if (triggersOnActionRequest()) {
                val player = client.activeChar
                if (player != null && player.isSpawnProtected)
                    player.onActionRequest()
            }
        } catch (t: Throwable) {
            LOGGER.error("Failed reading {} for {}. ", t, type, client.toString())

            if (this is EnterWorld)
                getClient().closeNow()
        }

    }

    protected fun sendPacket(gsp: L2GameServerPacket) {
        client.sendPacket(gsp)
    }

    /**
     * Overriden with true value on some packets that should disable spawn protection
     * @return
     */
    protected open fun triggersOnActionRequest(): Boolean {
        return true
    }

    companion object {
        val LOGGER = CLogger(L2GameClientPacket::class.java.name)
    }
}