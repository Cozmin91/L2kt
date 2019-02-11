package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.pledge.Clan

/**
 * format dSS dddddddddSdd d (Sddddd) dddSS dddddddddSdd d (Sdddddd)
 */
class PledgeShowMemberListAll(private val _clan: Clan, private val _pledgeType: Int) : L2GameServerPacket() {
    private val _pledgeName: String = when {
        _pledgeType == 0
            // main clan
        -> _clan.name
        _clan.getSubPledge(_pledgeType) != null -> _clan.getSubPledge(_pledgeType).name
        else -> ""
    }

    override fun writeImpl() {
        writeC(0x53)

        writeD(if (_pledgeType == 0) 0 else 1)
        writeD(_clan.clanId)
        writeD(_pledgeType)
        writeS(_pledgeName)
        writeS(_clan.getSubPledgeLeaderName(_pledgeType))

        writeD(_clan.crestId)
        writeD(_clan.level)
        writeD(_clan.castleId)
        writeD(_clan.hideoutId)
        writeD(_clan.rank)
        writeD(_clan.reputationScore)
        writeD(0) // 0
        writeD(0) // 0
        writeD(_clan.allyId)
        writeS(_clan.allyName)
        writeD(_clan.allyCrestId)
        writeD(if (_clan.isAtWar) 1 else 0)// new c3
        writeD(_clan.getSubPledgeMembersCount(_pledgeType))

        for (m in _clan.members) {
            if (m.pledgeType != _pledgeType)
                continue

            writeS(m.name)
            writeD(m.level)
            writeD(m.classId)

            val player = m.playerInstance
            if (player != null) {
                writeD(player.appearance.sex.ordinal) // no visible effect
                writeD(player.race.ordinal)// writeD(1);
            } else {
                writeD(0x01) // no visible effect
                writeD(0x01) // writeD(1);
            }

            writeD(if (m.isOnline) m.objectId else 0)
            writeD(if (m.sponsor != 0 || m.apprentice != 0) 1 else 0)
        }
    }
}