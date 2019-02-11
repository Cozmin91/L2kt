package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.network.serverpackets.ExManagePartyRoomMember
import com.l2kt.gameserver.network.serverpackets.JoinParty

class RequestAnswerJoinParty : L2GameClientPacket() {
    private var _response: Int = 0

    override fun readImpl() {
        _response = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val requestor = player.activeRequester ?: return

        requestor.sendPacket(JoinParty(_response))

        var party = requestor.party
        if (_response == 1) {
            if (party == null)
                party = Party(requestor, player, requestor.lootRule)
            else
                party.addPartyMember(player)

            if (requestor.isInPartyMatchRoom) {
                val list = PartyMatchRoomList.getInstance()
                if (list != null) {
                    val room = list.getPlayerRoom(requestor)
                    if (room != null) {
                        if (player.isInPartyMatchRoom) {
                            if (list.getPlayerRoomId(requestor) == list.getPlayerRoomId(player)) {
                                val packet = ExManagePartyRoomMember(player, room, 1)
                                for (member in room.partyMembers)
                                    member.sendPacket(packet)
                            }
                        } else {
                            room.addMember(player)

                            val packet = ExManagePartyRoomMember(player, room, 1)
                            for (member in room.partyMembers)
                                member.sendPacket(packet)

                            player.partyRoom = room.id
                            player.broadcastUserInfo()
                        }
                    }
                }
            }
        }

        // Must be kept out of "ok" answer, can't be merged with higher content.
        if (party != null)
            party.pendingInvitation = false

        player.activeRequester = null
        requestor.onTransactionResponse()
    }
}