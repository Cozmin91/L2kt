package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

class AdminRideWyvern : IAdminCommandHandler {

    private var _petRideId: Int = 0

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_ride")) {
            // command disabled if CW is worn. Warn user.
            if (activeChar.isCursedWeaponEquipped) {
                activeChar.sendMessage("You can't use //ride owning a Cursed Weapon.")
                return false
            }

            val st = StringTokenizer(command, " ")
            st.nextToken() // skip command

            if (st.hasMoreTokens()) {
                val mount = st.nextToken()

                if (mount == "wyvern" || mount == "2")
                    _petRideId = 12621
                else if (mount == "strider" || mount == "1")
                    _petRideId = 12526
                else {
                    activeChar.sendMessage("Parameter '$mount' isn't recognized for that command.")
                    return false
                }
            } else {
                activeChar.sendMessage("You must enter a parameter for that command.")
                return false
            }

            // If code reached that place, it means _petRideId has been filled.
            if (activeChar.isMounted)
                activeChar.dismount()
            else if (activeChar.pet != null)
                activeChar.pet!!.unSummon(activeChar)

            activeChar.mount(_petRideId, 0)
        } else if (command == "admin_unride")
            activeChar.dismount()

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_ride", "admin_unride")
    }
}