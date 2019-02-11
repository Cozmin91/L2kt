package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.Clan

/**
 * format SdSS dddddddd d (Sddddd)
 */
class GMViewPledgeInfo(private val _clan: Clan, private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x90)
        writeS(_activeChar.name)
        writeD(_clan.clanId)
        writeD(0x00)
        writeS(_clan.name)
        writeS(_clan.leaderName)
        writeD(_clan.crestId) // -> no, it's no longer used (nuocnam) fix by game
        writeD(_clan.level)
        writeD(_clan.castleId)
        writeD(_clan.hideoutId)
        writeD(_clan.rank)
        writeD(_clan.reputationScore)
        writeD(0)
        writeD(0)

        writeD(_clan.allyId) // c2
        writeS(_clan.allyName) // c2
        writeD(_clan.allyCrestId) // c2
        writeD(if (_clan.isAtWar) 1 else 0) // c3
        writeD(_clan.membersCount)

        for (member in _clan.members) {
            if (member != null) {
                writeS(member.name)
                writeD(member.level)
                writeD(member.classId)
                writeD(member.sex.ordinal)
                writeD(member.raceOrdinal)
                writeD(if (member.isOnline) member.objectId else 0)
                writeD(if (member.sponsor != 0) 1 else 0)
            }
        }
    }
}