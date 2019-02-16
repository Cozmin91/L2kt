package com.l2kt.gameserver.model.item

import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.templates.StatsSet

/**
 * This datatype is used to store Recipe informations used by Dwarf to craft items.<br></br>
 * It holds a List of [IntIntHolder] for materials and a simple IntIntHolder for production.
 */
class Recipe(set: StatsSet) {
    val materials: List<IntIntHolder> = set.getIntIntHolderList("material")
    val product: IntIntHolder = set.getIntIntHolder("product")

    val id: Int = set.getInteger("id")
    val level: Int = set.getInteger("level")
    val recipeId: Int = set.getInteger("itemId")
    val recipeName: String = set.getString("alias")
    val successRate: Int = set.getInteger("successRate")
    val mpCost: Int = set.getInteger("mpConsume")
    val isDwarven: Boolean = set.getBool("isDwarven")

}