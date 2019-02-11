package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

class PledgeInfo(private val _clan: Clan) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x83)
        writeD(_clan.clanId)
        writeS(_clan.name)
        writeS(_clan.allyName)
    }
}