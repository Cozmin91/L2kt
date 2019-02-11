package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

class PledgeShowInfoUpdate(private val _clan: Clan) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x88)
        writeD(_clan.clanId)
        writeD(_clan.crestId)
        writeD(_clan.level)
        writeD(_clan.castleId)
        writeD(_clan.hideoutId)
        writeD(_clan.rank)
        writeD(_clan.reputationScore)
        writeD(0)
        writeD(0)
        writeD(_clan.allyId)
        writeS(_clan.allyName) // c5
        writeD(_clan.allyCrestId) // c5
        writeD(if (_clan.isAtWar) 1 else 0) // c5
    }
}