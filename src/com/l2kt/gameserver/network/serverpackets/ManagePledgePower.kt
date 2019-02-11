package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

class ManagePledgePower(private val _clan: Clan, private val _action: Int, private val _rank: Int) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x30)
        writeD(_rank)
        writeD(_action)
        writeD(_clan.getPriviledgesByRank(_rank))
    }
}