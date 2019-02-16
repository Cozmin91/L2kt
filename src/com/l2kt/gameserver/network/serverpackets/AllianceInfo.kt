package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.ClanInfo
import com.l2kt.gameserver.network.clientpackets.RequestAllyInfo

/**
 * Sent in response to [RequestAllyInfo], if applicable.
 * @author afk5min
 */
class AllianceInfo(allianceId: Int) : L2GameServerPacket() {
    val name: String
    val total: Int
    val online: Int
    val leaderC: String
    val leaderP: String
    lateinit var allies: Array<ClanInfo>

    init {
        val leader = ClanTable.getClan(allianceId)!!
        name = leader.allyName ?: ""
        leaderC = leader.name
        leaderP = leader.leaderName

        val allies = ClanTable.getClanAllies(allianceId)
        var total = 0
        var online = 0
        for ((idx, clan) in allies.withIndex()) {
            val ci = ClanInfo(clan)
            this.allies[idx] = ci
            total += ci.total
            online += ci.online
        }

        this.total = total
        this.online = online
    }

    override fun writeImpl() {
        writeC(0xb4)

        writeS(name)
        writeD(total)
        writeD(online)
        writeS(leaderC)
        writeS(leaderP)

        writeD(allies.size)
        for (aci in allies) {
            writeS(aci.clan.name)
            writeD(0x00)
            writeD(aci.clan.level)
            writeS(aci.clan.leaderName)
            writeD(aci.total)
            writeD(aci.online)
        }
    }
}