package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom

class ExPartyRoomMember(private val _room: PartyMatchRoom, private val _mode: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x0e)
        writeD(_mode)
        writeD(_room.members)
        for (member in _room.partyMembers) {
            writeD(member.objectId)
            writeS(member.name)
            writeD(member.getActiveClass())
            writeD(member.level)
            writeD(MapRegionData.getClosestLocation(member.x, member.y))
            if (_room.owner == member)
                writeD(1)
            else {
                if (_room.owner.isInParty && member.isInParty && _room.owner.party!!.leaderObjectId == member.party!!.leaderObjectId)
                    writeD(2)
                else
                    writeD(0)
            }
        }
    }
}