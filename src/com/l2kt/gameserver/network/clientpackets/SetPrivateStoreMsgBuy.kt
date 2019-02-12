package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PrivateStoreMsgBuy

class SetPrivateStoreMsgBuy : L2GameClientPacket() {

    private var _storeMsg: String = ""

    override fun readImpl() {
        _storeMsg = readS()
    }

    override fun runImpl() {
        val player = client.activeChar
        if (player == null || player.buyList == null)
            return

        // store message is limited to 29 characters.
        if (_storeMsg.length > MAX_MSG_LENGTH)
            return

        player.buyList.title = _storeMsg
        player.sendPacket(PrivateStoreMsgBuy(player))
    }

    companion object {
        private const val MAX_MSG_LENGTH = 29
    }
}