package com.l2kt.gameserver.model.craft

import com.l2kt.gameserver.data.xml.RecipeData
import com.l2kt.gameserver.model.holder.IntIntHolder

/**
 * A datatype extending [IntIntHolder]. It is part of private workshop system, and is used to hold individual entries.
 */
class ManufactureItem(recipeId: Int, cost: Int) : IntIntHolder(recipeId, cost) {
    val isDwarven: Boolean = RecipeData.getRecipeList(recipeId)!!.isDwarven

}