package com.l2kt.gameserver.handler

import com.l2kt.gameserver.model.actor.instance.Player

/**
 * Interface for chat handlers
 * @author durgus
 */
interface IChatHandler {

    /**
     * Returns a list of all chat types registered to this handler
     * @return
     */
    val chatTypeList: IntArray

    /**
     * Handles a specific type of chat messages
     * @param type
     * @param activeChar
     * @param target
     * @param text
     */
    fun handleChat(type: Int, activeChar: Player, target: String, text: String)
}
