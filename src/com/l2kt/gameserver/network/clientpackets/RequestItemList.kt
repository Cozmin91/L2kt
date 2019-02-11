package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.ItemList

class RequestItemList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        if (!activeChar.isInventoryDisabled)
            sendPacket(ItemList(activeChar, true))
    }
}