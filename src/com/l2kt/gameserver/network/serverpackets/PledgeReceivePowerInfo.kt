package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.ClanMember

class PledgeReceivePowerInfo(private val _member: ClanMember) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x3c)

        writeD(_member.powerGrade)
        writeS(_member.name)
        writeD(_member.clan.getPriviledgesByRank(_member.powerGrade))
    }
}