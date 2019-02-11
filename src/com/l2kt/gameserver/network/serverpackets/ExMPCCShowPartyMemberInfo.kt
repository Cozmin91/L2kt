package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.group.Party

class ExMPCCShowPartyMemberInfo(private val _party: Party) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x4a)

        writeD(_party.membersCount)
        for (member in _party.members) {
            writeS(member.name)
            writeD(member.objectId)
            writeD(member.classId.id)
        }
    }
}