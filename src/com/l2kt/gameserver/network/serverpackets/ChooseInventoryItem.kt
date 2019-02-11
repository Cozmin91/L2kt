package com.l2kt.gameserver.network.serverpackets

class ChooseInventoryItem(private val _itemId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x6f)
        writeD(_itemId)
    }
}