package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * d h (h dddhh dhhh)
 */
class TradeStart(private val _activeChar: Player) : L2GameServerPacket() {
    private val _itemList: Array<ItemInstance> = _activeChar.inventory!!.getAvailableItems(true, false)

    override fun writeImpl() {
        if (_activeChar.activeTradeList == null || _activeChar.activeTradeList.partner == null)
            return

        writeC(0x1E)
        writeD(_activeChar.activeTradeList.partner.objectId)
        writeH(_itemList.size)

        for (temp in _itemList) {
            if (temp.item == null)
                continue

            val item = temp.item

            writeH(item.type1)
            writeD(temp.objectId)
            writeD(temp.itemId)
            writeD(temp.count)
            writeH(item.type2)
            writeH(temp.customType1)
            writeD(item.bodyPart)
            writeH(temp.enchantLevel)
            writeH(temp.customType2)
            writeH(0x00)
        }
    }
}