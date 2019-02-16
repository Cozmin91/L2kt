package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExManagePartyRoomMember
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Format: (ch) d
 * @author -Wooden-
 */
class AnswerJoinPartyRoom : L2GameClientPacket() {
    private var _answer: Int = 0 // 1 or 0

    override fun readImpl() {
        _answer = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val partner = player.activeRequester
        if (partner == null || World.getPlayer(partner.objectId) == null) {
            // Partner hasn't be found, cancel the invitation
            player.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            player.activeRequester = null
            return
        }

        // If answer is positive, join the requester's PartyRoom.
        if (_answer == 1 && !partner.isRequestExpired) {
            val room = PartyMatchRoomList.getRoom(partner.partyRoom) ?: return

            if (player.level >= room.minLvl && player.level <= room.maxLvl) {
                // Remove from waiting list
                PartyMatchWaitingList.removePlayer(player)

                player.partyRoom = partner.partyRoom

                player.sendPacket(PartyMatchDetail(room))
                player.sendPacket(ExPartyRoomMember(room, 0))

                for (_member in room.partyMembers) {
                    if (_member == null)
                        continue

                    _member.sendPacket(ExManagePartyRoomMember(player, room, 0))
                    _member.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addCharName(
                            player
                        )
                    )
                }
                room.addMember(player)

                // Info Broadcast
                player.broadcastUserInfo()
            } else
                player.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM)
        } else
            partner.sendPacket(SystemMessageId.PARTY_MATCHING_REQUEST_NO_RESPONSE)// Else, send a message to requester.

        // reset transaction timers
        player.activeRequester = null
        partner.onTransactionResponse()
    }
}