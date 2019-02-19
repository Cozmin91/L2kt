package com.l2kt.gameserver.network.clientpackets

class RequestRecipeShopMessageSet : L2GameClientPacket() {
    private var _name: String = ""

    override fun readImpl() {
        _name = readS()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (_name.isEmpty() || _name.length > MAX_MSG_LENGTH)
            return

        if (player.createList != null)
            player.createList!!.storeName = _name
    }

    companion object {
        private const val MAX_MSG_LENGTH = 29
    }
}