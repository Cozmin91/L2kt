package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.HennaData
import com.l2kt.gameserver.model.item.Henna
import com.l2kt.gameserver.network.SystemMessageId

class RequestHennaEquip : L2GameClientPacket() {
    private var _symbolId: Int = 0

    override fun readImpl() {
        _symbolId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val henna = HennaData.getHenna(_symbolId) ?: return

        if (!henna.canBeUsedBy(activeChar)) {
            activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL)
            return
        }

        if (activeChar.hennaEmptySlots == 0) {
            activeChar.sendPacket(SystemMessageId.SYMBOLS_FULL)
            return
        }

        val ownedDyes = activeChar.inventory!!.getItemByItemId(henna.dyeId)
        val count = ownedDyes?.count ?: 0

        if (count < Henna.requiredDyeAmount) {
            activeChar.sendPacket(SystemMessageId.CANT_DRAW_SYMBOL)
            return
        }

        // reduceAdena sends a message.
        if (!activeChar.reduceAdena("Henna", henna.price, activeChar.currentFolk, true))
            return

        // destroyItemByItemId sends a message.
        if (!activeChar.destroyItemByItemId("Henna", henna.dyeId, Henna.requiredDyeAmount, activeChar, true))
            return

        activeChar.addHenna(henna)
    }
}