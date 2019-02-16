package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail

class RequestWithdrawParty : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        val party = player.party ?: return

        party.removePartyMember(player, Party.MessageType.LEFT)

        if (player.isInPartyMatchRoom) {
            val room = PartyMatchRoomList.getPlayerRoom(player)
            if (room != null) {
                player.sendPacket(PartyMatchDetail(room))
                player.sendPacket(ExPartyRoomMember(room, 0))
                player.sendPacket(ExClosePartyRoom.STATIC_PACKET)

                room.deleteMember(player)
            }
            player.partyRoom = 0
            player.broadcastUserInfo()
        }
    }
}