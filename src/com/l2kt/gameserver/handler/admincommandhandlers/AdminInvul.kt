package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

/**
 * This class handles following admin commands: - invul = turns invulnerability on/off
 */
class AdminInvul : IAdminCommandHandler {

    override fun useAdminCommand(command: String, player: Player): Boolean {
        if (command == "admin_setinvul") {
            var `object`: WorldObject? = player.target
            if (`object` == null)
                `object` = player

            if (`object` !is Creature) {
                player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return false
            }

            val target = `object` as Creature?
            target!!.setIsMortal(!target.isMortal)

            player.sendMessage(target.name + if (!target.isMortal) " is now invulnerable." else " is now mortal.")
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_setinvul")
    }
}