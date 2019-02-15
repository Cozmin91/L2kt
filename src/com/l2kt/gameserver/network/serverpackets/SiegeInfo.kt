package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.pledge.Clan
import java.util.*

class SiegeInfo(private val _castle: Castle) : L2GameServerPacket() {

    override fun writeImpl() {
        val player = client.activeChar ?: return

        writeC(0xc9)
        writeD(_castle.castleId)
        writeD(if (_castle.ownerId == player.clanId && player.isClanLeader) 0x01 else 0x00)
        writeD(_castle.ownerId)

        var clan: Clan? = null
        if (_castle.ownerId > 0)
            clan = ClanTable.getClan(_castle.ownerId)

        if (clan != null) {
            writeS(clan.name)
            writeS(clan.leaderName)
            writeD(clan.allyId)
            writeS(clan.allyName)
        } else {
            writeS("NPC")
            writeS("")
            writeD(0)
            writeS("")
        }

        writeD((Calendar.getInstance().timeInMillis / 1000).toInt())
        writeD((_castle.siege.siegeDate.timeInMillis / 1000).toInt())
        writeD(0x00)
    }
}