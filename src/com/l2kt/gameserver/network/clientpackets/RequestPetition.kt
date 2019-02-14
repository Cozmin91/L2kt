package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestPetition : L2GameClientPacket() {
    private var _content: String = ""
    private var _type: Int = 0 // 1 = on : 0 = off;

    override fun readImpl() {
        _content = readS()
        _type = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!AdminData.isGmOnline(false)) {
            activeChar.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW)
            activeChar.sendPacket(PlaySound("systemmsg_e.702"))
            return
        }

        if (!Config.PETITIONING_ALLOWED) {
            activeChar.sendPacket(SystemMessageId.GAME_CLIENT_UNABLE_TO_CONNECT_TO_PETITION_SERVER)
            return
        }

        if (PetitionManager.getInstance().isPlayerPetitionPending(activeChar)) {
            activeChar.sendPacket(SystemMessageId.ONLY_ONE_ACTIVE_PETITION_AT_TIME)
            return
        }

        if (PetitionManager.getInstance().pendingPetitions.size == Config.MAX_PETITIONS_PENDING) {
            activeChar.sendPacket(SystemMessageId.PETITION_SYSTEM_CURRENT_UNAVAILABLE)
            return
        }

        val totalPetitions = PetitionManager.getInstance().getPlayerTotalPetitionCount(activeChar) + 1
        if (totalPetitions > Config.MAX_PETITIONS_PER_PLAYER) {
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.WE_HAVE_RECEIVED_S1_PETITIONS_TODAY).addNumber(
                    totalPetitions
                )
            )
            return
        }

        if (_content.length > 255) {
            activeChar.sendPacket(SystemMessageId.PETITION_MAX_CHARS_255)
            return
        }

        val petitionId = PetitionManager.getInstance().submitPetition(activeChar, _content, _type)

        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(
                petitionId
            )
        )
        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.SUBMITTED_YOU_S1_TH_PETITION_S2_LEFT).addNumber(
                totalPetitions
            ).addNumber(Config.MAX_PETITIONS_PER_PLAYER - totalPetitions)
        )
        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.S1_PETITION_ON_WAITING_LIST).addNumber(
                PetitionManager.getInstance().pendingPetitions.size
            )
        )
    }
}