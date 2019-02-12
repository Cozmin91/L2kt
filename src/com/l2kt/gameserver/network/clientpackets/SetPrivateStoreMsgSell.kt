package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PrivateStoreMsgSell

class SetPrivateStoreMsgSell : L2GameClientPacket() {

    private var _storeMsg: String = ""

    override fun readImpl() {
        _storeMsg = readS()
    }

    override fun runImpl() {
        val player = client.activeChar
        if (player == null || player.sellList == null)
            return

        if (_storeMsg.length > MAX_MSG_LENGTH)
            return

        player.sellList.title = _storeMsg
        sendPacket(PrivateStoreMsgSell(player))
    }

    companion object {
        private const val MAX_MSG_LENGTH = 29
    }
}