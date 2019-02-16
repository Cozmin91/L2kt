package com.l2kt.gameserver.handler

import com.l2kt.gameserver.model.actor.instance.Player

interface IAdminCommandHandler {

    /**
     * this method is called at initialization to register all the item ids automatically
     * @return all known itemIds
     */
    val adminCommandList: Array<String>

    /**
     * this is the worker method that is called when someone uses an admin command.
     * @param activeChar
     * @param command
     * @return command success
     */
    fun useAdminCommand(command: String, activeChar: Player): Boolean
}
