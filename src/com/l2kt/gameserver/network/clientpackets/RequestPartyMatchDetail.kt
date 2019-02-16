package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExManagePartyRoomMember
import com.l2kt.gameserver.network.serverpackets.ExPartyRoomMember
import com.l2kt.gameserver.network.serverpackets.PartyMatchDetail
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RequestPartyMatchDetail : L2GameClientPacket() {
    private var _roomid: Int = 0
    private var _unk1: Int = 0
    private var _unk2: Int = 0
    private var _unk3: Int = 0

    override fun readImpl() {
        _roomid = readD()
        _unk1 = readD()
        _unk2 = readD()
        _unk3 = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val room = PartyMatchRoomList.getInstance().getRoom(_roomid) ?: return

        if (activeChar.level >= room.minLvl && activeChar.level <= room.maxLvl) {
            PartyMatchWaitingList.removePlayer(activeChar)

            activeChar.partyRoom = _roomid

            activeChar.sendPacket(PartyMatchDetail(room))
            activeChar.sendPacket(ExPartyRoomMember(room, 0))

            for (member in room.partyMembers) {
                if (member == null)
                    continue

                member.sendPacket(ExManagePartyRoomMember(activeChar, room, 0))
                member.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_ENTERED_PARTY_ROOM).addCharName(
                        activeChar
                    )
                )
            }
            room.addMember(activeChar)
            activeChar.broadcastUserInfo()
        } else
            activeChar.sendPacket(SystemMessageId.CANT_ENTER_PARTY_ROOM)
    }
}