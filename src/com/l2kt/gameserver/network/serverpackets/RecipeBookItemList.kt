package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Recipe

class RecipeBookItemList(player: Player, private val _isDwarven: Boolean) : L2GameServerPacket() {
    private val _recipes: Collection<Recipe>? = if (_isDwarven) player.dwarvenRecipeBook else player.commonRecipeBook
    private val _maxMp: Int = player.maxMp

    override fun writeImpl() {
        writeC(0xD6)

        writeD(if (_isDwarven) 0x00 else 0x01)
        writeD(_maxMp)

        if (_recipes == null)
            writeD(0)
        else {
            writeD(_recipes.size)

            for ((i, recipe) in _recipes.withIndex()) {
                writeD(recipe.id)
                writeD(i + 1)
            }
        }
    }
}