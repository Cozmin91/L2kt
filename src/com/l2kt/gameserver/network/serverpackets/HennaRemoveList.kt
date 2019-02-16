package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Henna

class HennaRemoveList(private val _player: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xe5)
        writeD(_player.adena)
        writeD(_player.hennaEmptySlots)
        writeD(Math.abs(_player.hennaEmptySlots - 3))

        for (i in 1..3) {
            val henna = _player.getHenna(i)
            if (henna != null) {
                writeD(henna.symbolId)
                writeD(henna.dyeId)
                writeD(Henna.requiredDyeAmount / 2)
                writeD(henna.price / 5)
                writeD(0x01)
            }
        }
    }
}