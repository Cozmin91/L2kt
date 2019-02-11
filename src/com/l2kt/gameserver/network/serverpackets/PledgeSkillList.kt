package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

class PledgeSkillList(private val _clan: Clan) : L2GameServerPacket() {

    override fun writeImpl() {
        val skills = _clan.clanSkills.values

        writeC(0xfe)
        writeH(0x39)

        writeD(skills.size)

        for (sk in skills) {
            writeD(sk.id)
            writeD(sk.level)
        }
    }
}