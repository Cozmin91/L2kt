package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.group.Party

class RequestOustPartyMember : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val party = player.party
        if (party == null || !party.isLeader(player))
            return

        party.removePartyMember(_name, Party.MessageType.EXPELLED)
    }
}