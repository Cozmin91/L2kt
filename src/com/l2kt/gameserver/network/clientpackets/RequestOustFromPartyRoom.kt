package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExClosePartyRoom
import com.l2kt.gameserver.network.serverpackets.PartyMatchList

class RequestOustFromPartyRoom : L2GameClientPacket() {
    private var _charid: Int = 0

    override fun readImpl() {
        _charid = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val member = World.getInstance().getPlayer(_charid) ?: return

        val room = PartyMatchRoomList.getInstance().getPlayerRoom(member) ?: return

        if (room.owner != activeChar)
            return

        if (activeChar.isInParty && member.isInParty && activeChar.party!!.leaderObjectId == member.party!!.leaderObjectId)
            activeChar.sendPacket(SystemMessageId.CANNOT_DISMISS_PARTY_MEMBER)
        else {
            room.deleteMember(member)
            member.partyRoom = 0

            // Close the PartyRoom window
            member.sendPacket(ExClosePartyRoom.STATIC_PACKET)

            // Add player back on waiting list
            PartyMatchWaitingList.getInstance().addPlayer(member)

            // Send Room list
            member.sendPacket(
                PartyMatchList(
                    member,
                    0,
                    MapRegionData.getInstance().getClosestLocation(member.x, member.y),
                    member.level
                )
            )

            // Clean player's LFP title
            member.broadcastUserInfo()

            member.sendPacket(SystemMessageId.OUSTED_FROM_PARTY_ROOM)
        }
    }
}