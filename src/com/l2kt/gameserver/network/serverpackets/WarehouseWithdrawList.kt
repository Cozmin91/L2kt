package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class WarehouseWithdrawList(player: Player, private val _whType: Int) : L2GameServerPacket() {
    private var _playerAdena: Int = 0
    private var _items: Set<ItemInstance> = mutableSetOf()

    init {
        if (player.activeWarehouse != null){
            _playerAdena = player.adena
            _items = player.activeWarehouse?.items ?: mutableSetOf()
        }
    }

    override fun writeImpl() {
        writeC(0x42)
        writeH(_whType)
        writeD(_playerAdena)
        writeH(_items.size)

        for (temp in _items) {
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
                writeD(0x0000FFFF and temp.augmentation.getAugmentationId())
                writeD(temp.augmentation.getAugmentationId() shr 16)
            } else
                writeQ(0x00)
        }
    }

    companion object {
        const val PRIVATE = 1
        const val CLAN = 2
        const val CASTLE = 3 // not sure
        const val FREIGHT = 4 // not sure
    }
}