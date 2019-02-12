package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.network.SystemMessageId

class RequestSetAllyCrest : L2GameClientPacket() {
    private var _length: Int = 0
    private lateinit var _data: ByteArray

    override fun readImpl() {
        _length = readD()
        if (_length > 192)
            return

        _data = ByteArray(_length)
        readB(_data)
    }

    override fun runImpl() {
        if (_length < 0 || _length > 192)
            return

        val player = client.activeChar
        if (player == null || player.allyId == 0)
            return

        val clan = ClanTable.getInstance().getClan(player.allyId)
        if (player.clanId != clan.clanId || !player.isClanLeader)
            return

        if (_length == 0 || _data.isEmpty()) {
            if (clan.allyCrestId != 0) {
                clan.changeAllyCrest(0, false)
                player.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED)
            }
        } else {
            val crestId = IdFactory.getInstance().nextId
            if (CrestCache.getInstance().saveCrest(CrestCache.CrestType.ALLY, crestId, _data)) {
                clan.changeAllyCrest(crestId, false)
                player.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED)
            }
        }
    }
}