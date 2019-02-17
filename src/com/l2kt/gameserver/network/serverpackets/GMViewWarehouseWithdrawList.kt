package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.pledge.Clan

class GMViewWarehouseWithdrawList : L2GameServerPacket {
    private val _items: Set<ItemInstance>
    private val _playerName: String
    private val _money: Int

    constructor(player: Player) {
        _items = player.warehouse.items
        _playerName = player.name
        _money = player.warehouse.adena
    }

    constructor(clan: Clan) {
        _playerName = clan.leaderName
        _items = clan.warehouse.items
        _money = clan.warehouse.adena
    }

    override fun writeImpl() {
        writeC(0x95)
        writeS(_playerName)
        writeD(_money)
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
            writeH(if (temp.isWeapon) (item as Weapon).soulShotCount else 0x00)
            writeH(if (temp.isWeapon) (item as Weapon).spiritShotCount else 0x00)
            writeD(temp.objectId)
            writeD(if (temp.isWeapon && temp.isAugmented) 0x0000FFFF and temp.getAugmentation()!!.getAugmentationId() else 0)
            writeD(if (temp.isWeapon && temp.isAugmented) temp.getAugmentation()!!.getAugmentationId() shr 16 else 0)
        }
    }
}