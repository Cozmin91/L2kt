package com.l2kt.gameserver.model.manor

import com.l2kt.Config
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.templates.StatsSet

class Seed(set: StatsSet) {
    val seedId: Int = set.getInteger("id")
    val cropId: Int = set.getInteger("cropId")
    val level: Int = set.getInteger("level")
    val matureId: Int = set.getInteger("matureId")
    private val _reward1: Int = set.getInteger("reward1")
    private val _reward2: Int = set.getInteger("reward2")
    val castleId: Int = set.getInteger("castleId")
    val isAlternative: Boolean = set.getBool("isAlternative")
    private val _limitSeeds: Int = set.getInteger("seedsLimit")
    private val _limitCrops: Int = set.getInteger("cropsLimit")
    val seedReferencePrice: Int = ItemTable.getTemplate(seedId)?.referencePrice ?: 1
    val cropReferencePrice: Int = ItemTable.getTemplate(cropId)?.referencePrice ?: 1

    val seedLimit: Int
        get() = _limitSeeds * Config.RATE_DROP_MANOR

    val cropLimit: Int
        get() = _limitCrops * Config.RATE_DROP_MANOR

    val seedMaxPrice: Int
        get() = seedReferencePrice * 10

    val seedMinPrice: Int
        get() = (seedReferencePrice * 0.6).toInt()

    val cropMaxPrice: Int
        get() = cropReferencePrice * 10

    val cropMinPrice: Int
        get() = (cropReferencePrice * 0.6).toInt()

    fun getReward(type: Int): Int {
        return if (type == 1) _reward1 else _reward2
    }

    override fun toString(): String {
        return "SeedData [_id=$seedId, _level=$level, _crop=$cropId, _mature=$matureId, _type1=$_reward1, _type2=$_reward2, _manorId=$castleId, _isAlternative=$isAlternative, _limitSeeds=$_limitSeeds, _limitCrops=$_limitCrops]"
    }
}