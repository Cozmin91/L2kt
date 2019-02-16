package com.l2kt.gameserver.model.item

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import java.util.*

/**
 * @author Fulminus
 */
class DropCategory(val categoryType: Int) {
    private val _drops: MutableList<DropData>
    private var _categoryChance: Int =
        0 // a sum of chances for calculating if an item will be dropped from this category
    private var _categoryBalancedChance: Int =
        0 // sum for balancing drop selection inside categories in high rate servers

    val allDrops: List<DropData>
        get() = _drops

    val isSweep: Boolean
        get() = categoryType == -1

    // this returns the chance for the category to be visited in order to check if
    // drops might come from it. Category -1 (spoil) must always be visited
    // (but may return 0 or many drops)
    val categoryChance: Int
        get() = if (categoryType >= 0) _categoryChance else DropData.MAX_CHANCE

    val categoryBalancedChance: Int
        get() = if (categoryType >= 0) _categoryBalancedChance else DropData.MAX_CHANCE

    init {
        _drops = ArrayList(0)
        _categoryChance = 0
        _categoryBalancedChance = 0
    }

    fun addDropData(drop: DropData, raid: Boolean) {
        _drops.add(drop)
        _categoryChance += drop.chance

        // for drop selection inside a category: max 100 % chance for getting an item, scaling all values to that.
        _categoryBalancedChance += Math.min(
            drop.chance * if (raid) Config.RATE_DROP_ITEMS_BY_RAID else Config.RATE_DROP_ITEMS,
            DropData.MAX_CHANCE.toDouble()
        ).toInt()
    }

    fun clearAllDrops() {
        _drops.clear()
    }

    /**
     * useful for seeded conditions...the category will attempt to drop only among items that are allowed to be dropped when a mob is seeded. Previously, this only included adena. According to sh1ny, sealstones are also acceptable drops. if no acceptable drops are in the category, nothing will be
     * dropped. otherwise, it will check for the item's chance to drop and either drop it or drop nothing.
     * @return acceptable drop when mob is seeded, if it exists. Null otherwise.
     */
    @Synchronized
    fun dropSeedAllowedDropsOnly(): DropData? {
        val drops = ArrayList<DropData>()
        var subCatChance = 0
        for (drop in allDrops) {
            if (drop.itemId == 57 || drop.itemId == 6360 || drop.itemId == 6361 || drop.itemId == 6362) {
                drops.add(drop)
                subCatChance += drop.chance
            }
        }

        if (subCatChance == 0)
            return null

        // among the results choose one.
        val randomIndex = Rnd[subCatChance]

        var sum = 0
        for (drop in drops) {
            sum += drop.chance

            if (sum > randomIndex)
            // drop this item and exit the function
                return drop
        }
        // since it is still within category, only drop one of the acceptable drops from the results.
        return null
    }

    /**
     * ONE of the drops in this category is to be dropped now. to see which one will be dropped, weight all items' chances such that their sum of chances equals MAX_CHANCE. since the individual drops have their base chance, we also ought to use the base category chance for the weight. So weight =
     * MAX_CHANCE/basecategoryDropChance. Then get a single random number within this range. The first item (in order of the list) whose contribution to the sum makes the sum greater than the random number, will be dropped. Edited: How _categoryBalancedChance works in high rate servers: Let's say
     * item1 has a drop chance (when considered alone, without category) of 1 % * RATE_DROP_ITEMS and item2 has 20 % * RATE_DROP_ITEMS, and the server's RATE_DROP_ITEMS is for example 50x. Without this balancer, the relative chance inside the category to select item1 to be dropped would be 1/26 and
     * item2 25/26, no matter what rates are used. In high rate servers people usually consider the 1 % individual drop chance should become higher than this relative chance (1/26) inside the category, since having the both items for example in their own categories would result in having a drop
     * chance for item1 50 % and item2 1000 %. _categoryBalancedChance limits the individual chances to 100 % max, making the chance for item1 to be selected from this category 50/(50+100) = 1/3 and item2 100/150 = 2/3. This change doesn't affect calculation when drop_chance * RATE_DROP_ITEMS < 100
     * %, meaning there are no big changes for low rate servers and no changes at all for 1x servers.
     * @param raid if true, use special config rate for raidboss.
     * @return selected drop from category, or null if nothing is dropped.
     */
    @Synchronized
    fun dropOne(raid: Boolean): DropData? {
        val randomIndex = Rnd[categoryBalancedChance]
        var sum = 0
        for (drop in allDrops) {
            sum += Math.min(
                drop.chance * if (raid) Config.RATE_DROP_ITEMS_BY_RAID else Config.RATE_DROP_ITEMS,
                DropData.MAX_CHANCE.toDouble()
            ).toInt()

            if (sum >= randomIndex)
            // drop this item and exit the function
                return drop
        }
        return null
    }
}