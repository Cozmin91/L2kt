package com.l2kt.gameserver.handler

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * Mother class of all itemHandlers.
 */
interface IItemHandler {

    /**
     * Launch task associated to the item.
     * @param playable L2Playable designating the player
     * @param item ItemInstance designating the item to use
     * @param forceUse ctrl hold on item use
     */
    fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean)

    companion object {
        val LOGGER = CLogger(IItemHandler::class.java.name)
    }
}