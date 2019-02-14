package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestPetitionCancel : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
            if (activeChar.isGM)
                PetitionManager.getInstance().endActivePetition(activeChar)
            else
                activeChar.sendPacket(SystemMessageId.PETITION_UNDER_PROCESS)
        } else {
            if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar)) {
                if (PetitionManager.getInstance().cancelActivePetition(activeChar)) {
                    val numRemaining =
                        Config.MAX_PETITIONS_PER_PLAYER - PetitionManager.getInstance().getPlayerTotalPetitionCount(
                            activeChar
                        )
                    activeChar.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.PETITION_CANCELED_SUBMIT_S1_MORE_TODAY).addNumber(
                            numRemaining
                        )
                    )

                    // Notify all GMs that the player's pending petition has been cancelled.
                    val msgContent = activeChar.name + " has canceled a pending petition."
                    AdminData
                        .broadcastToGMs(CreatureSay(activeChar.objectId, 17, "Petition System", msgContent))
                } else
                    activeChar.sendPacket(SystemMessageId.FAILED_CANCEL_PETITION_TRY_LATER)
            } else
                activeChar.sendPacket(SystemMessageId.PETITION_NOT_SUBMITTED)
        }
    }
}