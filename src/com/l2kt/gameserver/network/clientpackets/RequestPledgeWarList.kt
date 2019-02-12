package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PledgeReceiveWarList

class RequestPledgeWarList : L2GameClientPacket() {
    private var _page: Int = 0
    private var _tab: Int = 0

    override fun readImpl() {
        _page = readD()
        _tab = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        val clan = player.clan ?: return

        val list: Set<Int>
        if (_tab == 0)
            list = clan.warList
        else {
            list = clan.attackerList
            _page = Math.max(0, if (_page > list.size / 13) 0 else _page)
        }

        player.sendPacket(PledgeReceiveWarList(list, _tab, _page))
    }
}