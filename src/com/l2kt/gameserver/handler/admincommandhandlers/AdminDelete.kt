package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId

/**
 * This class handles following admin commands: - delete = deletes target
 */
class AdminDelete : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_delete")
            handleDelete(activeChar)

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_delete")

        private fun handleDelete(activeChar: Player) {
            val obj = activeChar.target
            if (obj != null && obj is Npc) {

                val spawn = obj.spawn
                if (spawn != null) {
                    spawn.setRespawnState(false)

                    if (RaidBossSpawnManager.getInstance().isDefined(spawn.npcId))
                        RaidBossSpawnManager.getInstance().deleteSpawn(spawn, true)
                    else
                        SpawnTable.deleteSpawn(spawn, true)
                }
                obj.deleteMe()

                activeChar.sendMessage("Deleted " + obj.name + " from " + obj.objectId + ".")
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        }
    }
}