package com.l2kt.gameserver.handler

import com.l2kt.gameserver.model.actor.instance.Player

interface IUserCommandHandler {

    /**
     * this method is called at initialization to register all the item ids automatically
     * @return all known itemIds
     */
    val userCommandList: IntArray

    /**
     * this is the worker method that is called when someone uses an admin command.
     * @param id
     * @param activeChar
     * @return command success
     */
    fun useUserCommand(id: Int, activeChar: Player): Boolean
}
