package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.Recipe

class RecipeShopManageList(private val _seller: Player, private val _isDwarven: Boolean) : L2GameServerPacket() {

    private var _recipes: Collection<Recipe>? = null

    init {

        _recipes = if (_isDwarven && _seller.hasDwarvenCraft())
            _seller.dwarvenRecipeBook
        else
            _seller.commonRecipeBook

        // clean previous recipes
        if (_seller.createList != null) {
            val it = _seller.createList!!.list.iterator()
            while (it.hasNext()) {
                val item = it.next()
                if (item.isDwarven != _isDwarven || !_seller.hasRecipeList(item.id))
                    it.remove()
            }
        }
    }

    override fun writeImpl() {
        writeC(0xd8)
        writeD(_seller.objectId)
        writeD(_seller.adena)
        writeD(if (_isDwarven) 0x00 else 0x01)

        if (_recipes == null)
            writeD(0)
        else {
            writeD(_recipes!!.size)// number of items in recipe book

            for ((i, recipe) in _recipes!!.withIndex()) {
                writeD(recipe.id)
                writeD(i + 1)
            }
        }

        if (_seller.createList == null)
            writeD(0)
        else {
            val list = _seller.createList!!.list
            writeD(list.size)

            for (item in list) {
                writeD(item.id)
                writeD(0x00)
                writeD(item.value)
            }
        }
    }
}