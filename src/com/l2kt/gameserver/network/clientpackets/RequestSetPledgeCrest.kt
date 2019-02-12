package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId

class RequestSetPledgeCrest : L2GameClientPacket() {
    private var _length: Int = 0
    private lateinit var _data: ByteArray

    override fun readImpl() {
        _length = readD()
        if (_length > 256)
            return

        _data = ByteArray(_length)
        readB(_data)
    }

    override fun runImpl() {
        if (_length < 0 || _length > 256)
            return

        val player = client.activeChar ?: return

        val clan = player.clan ?: return

        if (clan.dissolvingExpiryTime > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.CANNOT_SET_CREST_WHILE_DISSOLUTION_IN_PROGRESS)
            return
        }

        if (player.clanPrivileges and Clan.CP_CL_REGISTER_CREST != Clan.CP_CL_REGISTER_CREST) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        if (_length == 0 || _data.isEmpty()) {
            if (clan.crestId != 0) {
                clan.changeClanCrest(0)
                player.sendPacket(SystemMessageId.CLAN_CREST_HAS_BEEN_DELETED)
            }
        } else {
            if (clan.level < 3) {
                player.sendPacket(SystemMessageId.CLAN_LVL_3_NEEDED_TO_SET_CREST)
                return
            }

            val crestId = IdFactory.getInstance().nextId
            if (CrestCache.getInstance().saveCrest(CrestCache.CrestType.PLEDGE, crestId, _data)) {
                clan.changeClanCrest(crestId)
                player.sendPacket(SystemMessageId.CLAN_EMBLEM_WAS_SUCCESSFULLY_REGISTERED)
            }
        }
    }
}