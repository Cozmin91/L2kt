package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.network.serverpackets.PledgeInfo

class RequestPledgeInfo : L2GameClientPacket() {
    private var _clanId: Int = 0

    override fun readImpl() {
        _clanId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val clan = ClanTable.getInstance().getClan(_clanId) ?: return

        activeChar.sendPacket(PledgeInfo(clan))
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }
}