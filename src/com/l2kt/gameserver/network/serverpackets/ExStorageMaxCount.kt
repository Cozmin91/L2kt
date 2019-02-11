package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class ExStorageMaxCount(player: Player) : L2GameServerPacket() {
    private val _inventoryLimit: Int = player.inventoryLimit
    private val _warehouseLimit: Int = player.wareHouseLimit
    private val _freightLimit: Int = player.freightLimit
    private val _privateSellLimit: Int = player.privateSellStoreLimit
    private val _privateBuyLimit: Int = player.privateBuyStoreLimit
    private val _dwarfRecipeLimit: Int = player.dwarfRecipeLimit
    private val _commonRecipeLimit: Int = player.commonRecipeLimit

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x2e)
        writeD(_inventoryLimit)
        writeD(_warehouseLimit)
        writeD(_freightLimit)
        writeD(_privateSellLimit)
        writeD(_privateBuyLimit)
        writeD(_dwarfRecipeLimit)
        writeD(_commonRecipeLimit)
    }
}