package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.PackageSendableList

/**
 * Format: (c)d d: char object id (?)
 * @author -Wooden-
 */
class RequestPackageSendableItemList : L2GameClientPacket() {
    private var _objectID: Int = 0

    override fun readImpl() {
        _objectID = readD()
    }

    public override fun runImpl() {
        val player = client.activeChar ?: return

        val items = player.inventory!!.getAvailableItems(true, false) ?: return

        sendPacket(PackageSendableList(items, _objectID))
    }
}