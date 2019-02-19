package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.ManagePledgePower

class RequestPledgePower : L2GameClientPacket() {
    private var _rank: Int = 0
    private var _action: Int = 0
    private var _privs: Int = 0

    override fun readImpl() {
        _rank = readD()
        _action = readD()

        if (_action == 2)
            _privs = readD()
        else
            _privs = 0
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val clan = player.clan ?: return

        if (_action == 2) {
            if (player.isClanLeader) {
                if (_rank == 9)
                    _privs = (_privs and Clan.CP_CL_VIEW_WAREHOUSE) + (_privs and Clan.CP_CH_OPEN_DOOR) +
                            (_privs and Clan.CP_CS_OPEN_DOOR)

                player.clan!!.setPriviledgesForRank(_rank, _privs)
            }
        } else
            player.sendPacket(ManagePledgePower(clan, _action, _rank))
    }
}