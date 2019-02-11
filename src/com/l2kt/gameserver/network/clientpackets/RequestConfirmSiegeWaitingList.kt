package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.network.serverpackets.SiegeDefenderList

class RequestConfirmSiegeWaitingList : L2GameClientPacket() {
    private var _approved: Int = 0
    private var _castleId: Int = 0
    private var _clanId: Int = 0

    override fun readImpl() {
        _castleId = readD()
        _clanId = readD()
        _approved = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        // Check if the player has a clan
        if (player.clan == null)
            return

        val castle = CastleManager.getInstance().getCastleById(_castleId) ?: return

        // Check if leader of the clan who owns the castle?
        if (castle.ownerId != player.clanId || !player.isClanLeader)
            return

        val clan = ClanTable.getInstance().getClan(_clanId) ?: return

        if (!castle.siege.isRegistrationOver) {
            if (_approved == 1) {
                if (castle.siege.checkSide(clan, Siege.SiegeSide.PENDING))
                    castle.siege.registerClan(clan, Siege.SiegeSide.DEFENDER)
            } else {
                if (castle.siege.checkSides(clan, Siege.SiegeSide.PENDING, Siege.SiegeSide.DEFENDER))
                    castle.siege.unregisterClan(clan)
            }
        }

        // Update the defender list
        player.sendPacket(SiegeDefenderList(castle))
    }
}