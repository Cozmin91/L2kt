package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.partymatching.PartyMatchRoom

class ExManagePartyRoomMember(
    private val _activeChar: Player,
    private val _room: PartyMatchRoom,
    private val _mode: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x10)
        writeD(_mode)
        writeD(_activeChar.objectId)
        writeS(_activeChar.name)
        writeD(_activeChar.activeClass)
        writeD(_activeChar.level)
        writeD(MapRegionData.getInstance().getClosestLocation(_activeChar.x, _activeChar.y))
        if (_room.owner == _activeChar)
            writeD(1)
        else {
            if (_room.owner.isInParty && _activeChar.isInParty && _room.owner.party!!.leaderObjectId == _activeChar.party!!.leaderObjectId)
                writeD(2)
            else
                writeD(0)
        }
    }
}