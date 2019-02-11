package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class RecipeShopMsg(private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xdb)
        writeD(_activeChar.objectId)
        writeS(_activeChar.createList.storeName)
    }
}