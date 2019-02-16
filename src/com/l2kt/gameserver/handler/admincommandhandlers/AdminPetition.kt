package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * This class handles commands for GMs to respond to petitions.
 * @author Tempy
 */
class AdminPetition : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        var petitionId = -1

        try {
            petitionId = Integer.parseInt(command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
        } catch (e: Exception) {
        }

        if (command == "admin_view_petitions")
            PetitionManager.sendPendingPetitionList(activeChar)
        else if (command.startsWith("admin_view_petition"))
            PetitionManager.viewPetition(activeChar, petitionId)
        else if (command.startsWith("admin_accept_petition")) {
            if (PetitionManager.isPlayerInConsultation(activeChar)) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME))
                return true
            }

            if (PetitionManager.isPetitionInProcess(petitionId)) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS))
                return true
            }

            if (!PetitionManager.acceptPetition(activeChar, petitionId))
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_UNDER_PETITION_CONSULTATION))
        } else if (command.startsWith("admin_reject_petition")) {
            if (!PetitionManager.rejectPetition(activeChar, petitionId))
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER))
        } else if (command == "admin_reset_petitions") {
            if (PetitionManager.isPetitionInProcess) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_UNDER_PROCESS))
                return false
            }
            PetitionManager.pendingPetitions.clear()
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_view_petitions",
            "admin_view_petition",
            "admin_accept_petition",
            "admin_reject_petition",
            "admin_reset_petitions"
        )
    }
}