package com.l2kt.gameserver.model.multisell

import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype which is part of multisell system. It is either the "result" or the "required part" of a multisell action.
 */
class Ingredient(var itemId: Int, var itemCount: Int, var isTaxIngredient: Boolean, var maintainIngredient: Boolean) {
    var enchantLevel: Int = 0

    var template: Item? = null
        private set

    /**
     * @return a new Ingredient instance with the same values as this.
     */
    val copy: Ingredient
        get() = Ingredient(itemId, itemCount, isTaxIngredient, maintainIngredient)

    val isStackable: Boolean
        get() = if (template == null) true else template!!.isStackable

    val isArmorOrWeapon: Boolean
        get() = if (template == null) false else template is Armor || template is Weapon

    val weight: Int
        get() = if (template == null) 0 else template!!.weight

    constructor(set: StatsSet) : this(
        set.getInteger("id"),
        set.getInteger("count"),
        set.getBool("isTaxIngredient", false),
        set.getBool("maintainIngredient", false)
    ) {
    }

    init {

        if (this.itemId > 0)
            template = ItemTable.getTemplate(this.itemId)
    }
}