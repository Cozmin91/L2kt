package com.l2kt.gameserver.model.holder.skillnode

import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used by enchant skill types. It extends [IntIntHolder].
 */
class EnchantSkillNode(set: StatsSet) : IntIntHolder(set.getInteger("id"), set.getInteger("lvl")) {
    val exp: Int = set.getInteger("exp")
    val sp: Int = set.getInteger("sp")
    private val _enchantRates = IntArray(5)

    var item: IntIntHolder? = null
        private set

    init {

        _enchantRates[0] = set.getInteger("rate76")
        _enchantRates[1] = set.getInteger("rate77")
        _enchantRates[2] = set.getInteger("rate78")
        _enchantRates[3] = set.getInteger("rate79")
        _enchantRates[4] = set.getInteger("rate80")

        if (set.containsKey("itemNeeded"))
            item = set.getIntIntHolder("itemNeeded")
    }

    fun getEnchantRate(level: Int): Int {
        return _enchantRates[level - 76]
    }
}