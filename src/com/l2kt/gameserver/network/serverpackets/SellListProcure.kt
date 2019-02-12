package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class SellListProcure(player: Player, castleId: Int) : L2GameServerPacket() {
    private val _sellList: MutableMap<ItemInstance, Int> = mutableMapOf()

    private val _money: Int = player.adena

    init {

        for (c in CastleManorManager.getInstance().getCropProcure(castleId, false)) {
            val item = player.inventory!!.getItemByItemId(c.id)
            if (item != null && c.amount > 0)
                _sellList[item] = c.amount
        }
    }

    override fun writeImpl() {
        writeC(0xE9)
        writeD(_money)
        writeD(0x00)
        writeH(_sellList.size)

        for ((item, value) in _sellList) {

            writeH(item.item.type1)
            writeD(item.objectId)
            writeD(item.itemId)
            writeD(value)
            writeH(item.item.type2)
            writeH(0)
            writeD(0)
        }
    }
}