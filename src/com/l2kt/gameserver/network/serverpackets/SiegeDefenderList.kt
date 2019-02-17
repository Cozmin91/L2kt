package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.entity.Siege

class SiegeDefenderList(private val _castle: Castle) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xcb)
        writeD(_castle.castleId)
        writeD(0x00) // 0
        writeD(0x01) // 1
        writeD(0x00) // 0

        val defenders = _castle.siege!!.defenderClans
        val pendingDefenders = _castle.siege!!.pendingClans
        val size = defenders.size + pendingDefenders.size

        if (size > 0) {
            writeD(size)
            writeD(size)

            for (clan in defenders) {
                writeD(clan.clanId)
                writeS(clan.name)
                writeS(clan.leaderName)
                writeD(clan.crestId)
                writeD(0x00) // signed time (seconds) (not storated by L2J)

                val side = _castle.siege!!.getSide(clan)
                if (side == Siege.SiegeSide.OWNER)
                    writeD(0x01)
                else if (side == Siege.SiegeSide.PENDING)
                    writeD(0x02)
                else if (side == Siege.SiegeSide.DEFENDER)
                    writeD(0x03)
                else
                    writeD(0x00)

                writeD(clan.allyId)
                writeS(clan.allyName)
                writeS("") // AllyLeaderName
                writeD(clan.allyCrestId)
            }

            for (clan in pendingDefenders) {
                writeD(clan.clanId)
                writeS(clan.name)
                writeS(clan.leaderName)
                writeD(clan.crestId)
                writeD(0x00) // signed time (seconds) (not storated by L2J)
                writeD(0x02) // waiting approval
                writeD(clan.allyId)
                writeS(clan.allyName)
                writeS("") // AllyLeaderName
                writeD(clan.allyCrestId)
            }
        } else {
            writeD(0x00)
            writeD(0x00)
        }
    }
}