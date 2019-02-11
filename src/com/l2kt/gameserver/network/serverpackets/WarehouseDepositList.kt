package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class WarehouseDepositList(player: Player, private val _whType: Int) : L2GameServerPacket() {

    private val _playerAdena: Int = player.adena
    private val _items = mutableListOf<ItemInstance>()

    init {

        val isPrivate = _whType == PRIVATE
        for (temp in player.inventory!!.getAvailableItems(true, isPrivate)) {
            if (temp != null && temp.isDepositable(isPrivate))
                _items.add(temp)
        }
    }

    override fun writeImpl() {
        writeC(0x41)
        writeH(_whType)
        writeD(_playerAdena)
        writeH(_items.size)

        for (temp in _items) {
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
            writeD(temp.objectId)
            if (temp.isAugmented) {
                writeD(0x0000FFFF and temp.augmentation.augmentationId)
                writeD(temp.augmentation.augmentationId shr 16)
            } else
                writeQ(0x00)
        }
        _items.clear()
    }

    companion object {
        const val PRIVATE = 1
        const val CLAN = 2
        const val CASTLE = 3 // not sure
        const val FREIGHT = 4 // not sure
    }
}