package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExAskJoinPartyRoom
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestAskJoinPartyRoom : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        // Send PartyRoom invite request (with activeChar) name to the target
        val target = World.getInstance().getPlayer(_name)
        if (target != null) {
            if (!target.isProcessingRequest) {
                activeChar.onTransactionRequest(target)
                target.sendPacket(ExAskJoinPartyRoom(activeChar.name))
            } else
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addCharName(
                        target
                    )
                )
        } else
            activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
    }
}