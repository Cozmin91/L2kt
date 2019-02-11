package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.tradelist.TradeItem
import com.l2kt.gameserver.model.tradelist.TradeList

class TradeItemUpdate(trade: TradeList, activeChar: Player) : L2GameServerPacket() {
    private val _items: Set<ItemInstance> = activeChar.inventory!!.items
    private val _currentTrade: List<TradeItem> = trade.items

    private fun getItemCount(objectId: Int): Int {
        for (item in _items)
            if (item.objectId == objectId)
                return item.count

        return 0
    }

    override fun writeImpl() {
        writeC(0x74)
        writeH(_currentTrade.size)

        for (item in _currentTrade) {
            var availableCount = getItemCount(item.objectId) - item.count
            var stackable = item.item.isStackable

            if (availableCount == 0) {
                availableCount = 1
                stackable = false
            }

            writeH(if (stackable) 3 else 2)
            writeH(item.item.type1)
            writeD(item.objectId)
            writeD(item.item.itemId)
            writeD(availableCount)
            writeH(item.item.type2)
            writeH(0x00)
            writeD(item.item.bodyPart)
            writeH(item.enchant)
            writeH(0x00)
            writeH(0x00)
        }
    }
}