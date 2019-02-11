package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.ClanMember

class PledgePowerGradeList(private val _ranks: Set<Int>, private val _members: Collection<ClanMember>) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x3b)
        writeD(_ranks.size)
        for (rank in _ranks) {
            writeD(rank)
            writeD(_members.stream().filter { m -> m.powerGrade == rank }.count().toInt())
        }
    }
}