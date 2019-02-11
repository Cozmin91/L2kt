package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class RecipeShopSellList(private val _buyer: Player, private val _manufacturer: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        val createList = _manufacturer.createList
        if (createList != null) {
            writeC(0xd9)
            writeD(_manufacturer.objectId)
            writeD(_manufacturer.currentMp.toInt())
            writeD(_manufacturer.maxMp)
            writeD(_buyer.adena)

            val list = createList.list
            writeD(list.size)

            for (item in list) {
                writeD(item.id)
                writeD(0x00) // unknown
                writeD(item.value)
            }
        }
    }
}