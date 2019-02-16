package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.serverpackets.ExMPCCShowPartyMemberInfo

/**
 * Format:(ch) d
 * @author chris_00
 */
class RequestExMPCCShowPartyMembersInfo : L2GameClientPacket() {
    private var _partyLeaderId: Int = 0

    override fun readImpl() {
        _partyLeaderId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val player = World.getPlayer(_partyLeaderId)
        if (player != null && player.isInParty)
            activeChar.sendPacket(ExMPCCShowPartyMemberInfo(player.party!!))
    }
}