package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoom
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail

class RequestPartyMatchList : L2GameClientPacket() {
    private var _roomid: Int = 0
    private var _membersmax: Int = 0
    private var _lvlmin: Int = 0
    private var _lvlmax: Int = 0
    private var _loot: Int = 0
    private lateinit var _roomtitle: String

    override fun readImpl() {
        _roomid = readD()
        _membersmax = readD()
        _lvlmin = readD()
        _lvlmax = readD()
        _loot = readD()
        _roomtitle = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (_roomid > 0) {
            val room = PartyMatchRoomList.getInstance().getRoom(_roomid)
            if (room != null) {
                room.maxMembers = _membersmax
                room.minLvl = _lvlmin
                room.maxLvl = _lvlmax
                room.lootType = _loot
                room.title = _roomtitle

                for (member in room.partyMembers) {
                    if (member == null)
                        continue

                    member.sendPacket(PartyMatchDetail(room))
                    member.sendPacket(SystemMessageId.PARTY_ROOM_REVISED)
                }
            }
        } else {
            val maxId = PartyMatchRoomList.getInstance().maxId

            val room = PartyMatchRoom(maxId, _roomtitle, _loot, _lvlmin, _lvlmax, _membersmax, player)

            // Remove from waiting list, and add to current room
            PartyMatchWaitingList.getInstance().removePlayer(player)
            PartyMatchRoomList.getInstance().addPartyMatchRoom(maxId, room)

            val party = player.party
            if (party != null) {
                for (member in party.members) {
                    if (member == player)
                        continue

                    member.partyRoom = maxId

                    room.addMember(member)
                }
            }

            player.sendPacket(PartyMatchDetail(room))
            player.sendPacket(ExPartyRoomMember(room, 1))

            player.sendPacket(SystemMessageId.PARTY_ROOM_CREATED)

            player.partyRoom = maxId
            player.broadcastUserInfo()
        }
    }
}