package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.SystemMessageId

class RequestChangePartyLeader : L2GameClientPacket() {
    private var _name: String? = null

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val party = player.party
        if (party == null || !party.isLeader(player)) {
            player.sendPacket(SystemMessageId.ONLY_A_PARTY_LEADER_CAN_TRANSFER_ONES_RIGHTS_TO_ANOTHER_PLAYER)
            return
        }

        party.changePartyLeader(_name)
    }
}