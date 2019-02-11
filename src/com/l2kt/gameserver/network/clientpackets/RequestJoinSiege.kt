package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SiegeInfo

class RequestJoinSiege : L2GameClientPacket() {
    private var _castleId: Int = 0
    private var _isAttacker: Int = 0
    private var _isJoining: Int = 0

    override fun readImpl() {
        _castleId = readD()
        _isAttacker = readD()
        _isJoining = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (!player.isClanLeader) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return
        }

        val castle = CastleManager.getInstance().getCastleById(_castleId) ?: return

        if (_isJoining == 1) {
            if (System.currentTimeMillis() < player.clan.dissolvingExpiryTime) {
                player.sendPacket(SystemMessageId.CANT_PARTICIPATE_IN_SIEGE_WHILE_DISSOLUTION_IN_PROGRESS)
                return
            }

            if (_isAttacker == 1)
                castle.siege.registerAttacker(player)
            else
                castle.siege.registerDefender(player)
        } else
            castle.siege.unregisterClan(player.clan)

        player.sendPacket(SiegeInfo(castle))
    }
}