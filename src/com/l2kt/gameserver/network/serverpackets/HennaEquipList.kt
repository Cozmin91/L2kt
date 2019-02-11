package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna

class HennaEquipList(private val _player: Player, private val _hennaEquipList: List<Henna>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xe2)
        writeD(_player.adena)
        writeD(3)
        writeD(_hennaEquipList.size)

        for (temp in _hennaEquipList) {
            // Player must have at least one dye in inventory to be able to see the henna that can be applied with it.
            if (_player.inventory!!.getItemByItemId(temp.dyeId) != null) {
                writeD(temp.symbolId) // symbolid
                writeD(temp.dyeId) // itemid of dye
                writeD(Henna.getRequiredDyeAmount()) // amount of dyes required
                writeD(temp.price) // amount of adenas required
                writeD(1) // meet the requirement or not
            }
        }
    }
}