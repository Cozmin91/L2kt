package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.model.actor.instance.Player

class RecipeItemMakeInfo : L2GameServerPacket {
    private val _id: Int
    private val _activeChar: Player
    private val _status: Int

    constructor(id: Int, player: Player, status: Int) {
        _id = id
        _activeChar = player
        _status = status
    }

    constructor(id: Int, player: Player) {
        _id = id
        _activeChar = player
        _status = -1
    }

    override fun writeImpl() {
        val recipe = RecipeData.getInstance().getRecipeList(_id)
        if (recipe != null) {
            writeC(0xD7)

            writeD(_id)
            writeD(if (recipe.isDwarven) 0 else 1)
            writeD(_activeChar.currentMp.toInt())
            writeD(_activeChar.maxMp)
            writeD(_status)
        }
    }
}