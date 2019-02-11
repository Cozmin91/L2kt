package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

/**
 * format ddddd
 */
class PledgeStatusChanged(private val _clan: Clan) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xcd)
        writeD(_clan.leaderId)
        writeD(_clan.clanId)
        writeD(_clan.crestId)
        writeD(_clan.allyId)
        writeD(_clan.allyCrestId)
        writeD(0)
        writeD(0)
    }
}