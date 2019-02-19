package com.l2kt.gameserver.model.actor.template

import com.l2kt.gameserver.model.PetDataEntry
import com.l2kt.gameserver.templates.StatsSet

class PetTemplate(set: StatsSet) : NpcTemplate(set) {

    /**
     * @return the itemId corresponding to first food type, if any.
     */
    val food1: Int = set.getInteger("food1")
    /**
     * @return the itemId corresponding to second food type, if any.
     */
    val food2: Int = set.getInteger("food2")

    /**
     * @return the auto feed limit, used for automatic use of food from pet's inventory (happens if % is reached). The value is under 1.0 format for easier management.
     */
    val autoFeedLimit: Double = set.getDouble("autoFeedLimit")
    /**
     * @return the hungry limit, used to lower stats (pet is weaker if % reached). The value is under 1.0 format for easier management.
     */
    val hungryLimit: Double = set.getDouble("hungryLimit")
    /**
     * @return the unsummon limit, used to check unsummon case (can't unsummon if % reached). The value is under 1.0 format for easier management.
     */
    val unsummonLimit: Double = set.getDouble("unsummonLimit")

    private val _dataEntries: Map<Int, PetDataEntry> = set.getMap("petData")

    /**
     * @param level : The level of pet to retrieve. It can be either actual or any other level.
     * @return the PetDataEntry corresponding to the level parameter.
     */
    fun getPetDataEntry(level: Int): PetDataEntry? {
        return _dataEntries[level]
    }

    /**
     * @param itemId : The itemId to check.
     * @return true if at least one template food id matches with the parameter.
     */
    fun canEatFood(itemId: Int): Boolean {
        return food1 == itemId || food2 == itemId
    }

    companion object {
        const val MAX_LOAD = 54510
    }
}