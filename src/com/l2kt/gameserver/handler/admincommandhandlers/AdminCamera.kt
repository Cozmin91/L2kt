package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.CameraMode
import com.l2kt.gameserver.network.serverpackets.ExShowScreenMessage
import com.l2kt.gameserver.network.serverpackets.NormalCamera
import com.l2kt.gameserver.network.serverpackets.SpecialCamera

class AdminCamera : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_camera ")) {
            try {
                val target = activeChar.target as Creature
                val com = command.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()

                target.broadcastPacket(
                    SpecialCamera(
                        target.objectId,
                        Integer.parseInt(com[1]),
                        Integer.parseInt(com[2]),
                        Integer.parseInt(com[3]),
                        Integer.parseInt(com[4]),
                        Integer.parseInt(com[5]),
                        Integer.parseInt(com[6]),
                        Integer.parseInt(com[7]),
                        Integer.parseInt(com[8]),
                        Integer.parseInt(com[9])
                    )
                )
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //camera dist yaw pitch time duration turn rise widescreen unknown")
                return false
            }

        } else if (command == "admin_cameramode") {
            // lolcheck. But basically, chance to be invisible AND rooted is kinda null, except with this command
            if (!(activeChar.appearance.invisible && activeChar.isImmobilized)) {
                activeChar.target = null
                activeChar.setIsImmobilized(true)
                activeChar.sendPacket(CameraMode(1))

                // Make the character disappears (from world too)
                activeChar.appearance.setInvisible()
                activeChar.broadcastUserInfo()
                activeChar.decayMe()
                activeChar.spawnMe()

                activeChar.sendPacket(
                    ExShowScreenMessage(
                        1,
                        0,
                        2,
                        false,
                        1,
                        0,
                        0,
                        false,
                        5000,
                        true,
                        "To remove this text, press ALT+H. To exit, press ALT+H and type //cameramode"
                    )
                )
            } else {
                activeChar.setIsImmobilized(false)
                activeChar.sendPacket(CameraMode(0))
                activeChar.sendPacket(NormalCamera.STATIC_PACKET)

                // Make the character appears (to world too)
                activeChar.appearance.setVisible()
                activeChar.broadcastUserInfo()

                // Teleport back the player to beginning point
                activeChar.teleToLocation(activeChar.x, activeChar.y, activeChar.z, 0)
            }
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_camera", "admin_cameramode")
    }
}