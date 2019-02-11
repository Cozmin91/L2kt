package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.entity.Castle

class SiegeAttackerList(private val _castle: Castle) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xca)
        writeD(_castle.castleId)
        writeD(0x00) // 0
        writeD(0x01) // 1
        writeD(0x00) // 0

        val attackers = _castle.siege.attackerClans
        val size = attackers.size

        if (size > 0) {
            writeD(size)
            writeD(size)

            for (clan in attackers) {
                writeD(clan.clanId)
                writeS(clan.name)
                writeS(clan.leaderName)
                writeD(clan.crestId)
                writeD(0x00) // signed time (seconds) (not storated by L2J)
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