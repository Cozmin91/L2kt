package com.l2kt.gameserver.network.clientpackets

class RequestShortCutDel : L2GameClientPacket() {
    private var _slot: Int = 0
    private var _page: Int = 0

    override fun readImpl() {
        val id = readD()
        _slot = id % 12
        _page = id / 12
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (_page > 9 || _page < 0)
            return

        activeChar.deleteShortCut(_slot, _page)
    }
}