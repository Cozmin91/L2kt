package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList

class RequestExitPartyMatchingWaitingRoom : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        PartyMatchWaitingList.getInstance().removePlayer(activeChar)
    }
}