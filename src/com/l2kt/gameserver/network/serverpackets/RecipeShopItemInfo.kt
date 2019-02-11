package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class RecipeShopItemInfo(private val _player: Player, private val _recipeId: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xda)
        writeD(_player.objectId)
        writeD(_recipeId)
        writeD(_player.currentMp.toInt())
        writeD(_player.maxMp)
        writeD(-0x1)
    }
}