package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.ClanMember

/**
 * @author -Wooden-
 */
class PledgeReceiveMemberInfo(private val _member: ClanMember) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x3d)

        writeD(_member.pledgeType)
        writeS(_member.name)
        writeS(_member.title) // title
        writeD(_member.powerGrade) // power

        // clan or subpledge name
        if (_member.pledgeType != 0)
            writeS(_member.clan.getSubPledge(_member.pledgeType)?.name)
        else
            writeS(_member.clan.name)

        writeS(_member.apprenticeOrSponsorName) // name of this member's apprentice/sponsor
    }
}