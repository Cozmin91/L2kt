package com.l2kt.gameserver.model

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.L2GameClientPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.concurrent.ScheduledFuture

/**
 * A request between two [Player]s. It is associated to a 15 seconds timer, where both partner and packet references are destroyed.<br></br>
 * <br></br>
 * On request response, the task is canceled.
 */
class Request(private val _player: Player) {
    /**
     * @return the [Player] partner of a request.
     */
    /**
     * Set the [Player] partner of a request.
     * @param partner : The player to set as partner.
     */
    var partner: Player? = null
        private set

    /**
     * @return the [L2GameClientPacket] originally sent by the requestor.
     */
    /**
     * Set the [L2GameClientPacket] originally sent by the requestor.
     * @param packet : The packet to set.
     */
    var requestPacket: L2GameClientPacket? = null
        private set

    private var _requestTimer: ScheduledFuture<*>? = null

    /**
     * @return true if a request is in progress.
     */
    val isProcessingRequest: Boolean
        get() = partner != null

    private fun clear() {
        partner = null
        requestPacket = null
    }

    /**
     * Check if a request can be made ; if successful, put [Player]s on request state.
     * @param partner : The player partner to check.
     * @param packet : The packet to register.
     * @return true if the request has succeeded.
     */
    @Synchronized
    fun setRequest(partner: Player?, packet: L2GameClientPacket): Boolean {
        if (partner == null) {
            _player.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET)
            return false
        }

        if (partner.request.isProcessingRequest) {
            _player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(partner))
            return false
        }

        if (isProcessingRequest) {
            _player.sendPacket(SystemMessageId.WAITING_FOR_ANOTHER_REPLY)
            return false
        }

        this.partner = partner
        requestPacket = packet
        clearRequestOnTimeout()

        this.partner!!.request.partner = _player
        this.partner!!.request.requestPacket = packet
        this.partner!!.request.clearRequestOnTimeout()
        return true
    }

    private fun clearRequestOnTimeout() {
        _requestTimer = ThreadPool.schedule(Runnable{ clear() }, REQUEST_TIMEOUT.toLong())
    }

    /**
     * Clear [Player] request state. Should be called after answer packet receive.
     */
    fun onRequestResponse() {
        if (_requestTimer != null) {
            _requestTimer!!.cancel(true)
            _requestTimer = null
        }

        clear()

        if (partner != null)
            partner!!.request.clear()
    }

    companion object {
        private const val REQUEST_TIMEOUT = 15000
    }
}