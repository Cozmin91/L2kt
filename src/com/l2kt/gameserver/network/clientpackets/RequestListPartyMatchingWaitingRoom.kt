package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom

class RequestListPartyMatchingWaitingRoom : L2GameClientPacket() {
    private var _page: Int = 0
    private var _minlvl: Int = 0
    private var _maxlvl: Int = 0
    private var _mode: Int = 0 // 1 - waitlist 0 - room waitlist

    override fun readImpl() {
        _page = readD()
        _minlvl = readD()
        _maxlvl = readD()
        _mode = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(ExListPartyMatchingWaitingRoom(activeChar, _page, _minlvl, _maxlvl, _mode))
    }
}