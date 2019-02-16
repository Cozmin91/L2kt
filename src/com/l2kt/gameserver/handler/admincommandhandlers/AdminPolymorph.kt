package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

/**
 * This class handles polymorph commands.
 */
class AdminPolymorph : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (activeChar.isMounted)
            return false

        var target: WorldObject? = activeChar.target
        if (target == null)
            target = activeChar

        if (command.startsWith("admin_polymorph")) {
            try {
                val st = StringTokenizer(command)
                st.nextToken()

                var info: WorldObject.PolyType = WorldObject.PolyType.NPC
                if (st.countTokens() > 1)
                    info = WorldObject.PolyType.valueOf(st.nextToken().toUpperCase())

                val npcId = Integer.parseInt(st.nextToken())

                if (!target.polymorph(info, npcId)) {
                    activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT)
                    return true
                }

                activeChar.sendMessage("You polymorphed " + target.name + " into a " + info + " using id: " + npcId + ".")
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //polymorph <type> <id>")
            }

        } else if (command.startsWith("admin_unpolymorph")) {
            if (target.polyType === WorldObject.PolyType.DEFAULT) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return true
            }

            target.unpolymorph()

            activeChar.sendMessage("You successfully unpolymorphed " + target.name + ".")
        }

        if (command.contains("menu"))
            AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm")

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS =
            arrayOf("admin_polymorph", "admin_unpolymorph", "admin_polymorph_menu", "admin_unpolymorph_menu")
    }
}