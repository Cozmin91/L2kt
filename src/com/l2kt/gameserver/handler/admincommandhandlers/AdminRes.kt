package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.taskmanager.DecayTaskManager

/**
 * This class handles following admin commands:<br></br>
 * - res = resurrects a player<br></br>
 * - res_monster = resurrects a Npc/Monster/...
 */
class AdminRes : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_res "))
            handleRes(activeChar, command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        else if (command == "admin_res")
            handleRes(activeChar)
        else if (command.startsWith("admin_res_monster "))
            handleNonPlayerRes(
                activeChar,
                command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            )
        else if (command == "admin_res_monster")
            handleNonPlayerRes(activeChar)

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_res", "admin_res_monster")

        private fun handleRes(activeChar: Player, resParam: String? = null) {
            var obj: WorldObject? = activeChar.target

            if (resParam != null) {
                // Check if a player name was specified as a param.
                val plyr = World.getPlayer(resParam)

                if (plyr != null)
                    obj = plyr
                else {
                    // Otherwise, check if the param was a radius.
                    try {
                        val radius = Integer.parseInt(resParam)

                        for (knownPlayer in activeChar.getKnownTypeInRadius(Player::class.java, radius))
                            doResurrect(knownPlayer)

                        activeChar.sendMessage("Resurrected all players within a $radius unit radius.")
                        return
                    } catch (e: NumberFormatException) {
                        activeChar.sendMessage("Enter a valid player name or radius.")
                        return
                    }

                }
            }

            if (obj == null)
                obj = activeChar

            doResurrect((obj as Creature?)!!)
        }

        private fun handleNonPlayerRes(activeChar: Player, radiusStr: String = "") {
            val obj = activeChar.target

            try {
                var radius = 0

                if (!radiusStr.isEmpty()) {
                    radius = Integer.parseInt(radiusStr)

                    for (knownChar in activeChar.getKnownTypeInRadius(Creature::class.java, radius))
                        if (knownChar !is Player)
                            doResurrect(knownChar)

                    activeChar.sendMessage("Resurrected all non-players within a $radius unit radius.")
                }
            } catch (e: NumberFormatException) {
                activeChar.sendMessage("Enter a valid radius.")
                return
            }

            if (obj is Player) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            doResurrect(obj as Creature)
        }

        private fun doResurrect(targetChar: Creature) {
            if (!targetChar.isDead)
                return

            // If the target is a player, then restore the XP lost on death.
            if (targetChar is Player)
                targetChar.restoreExp(100.0)
            else
                DecayTaskManager.cancel(targetChar)// If the target is an NPC, then abort it's auto decay and respawn.

            targetChar.doRevive()
        }
    }
}