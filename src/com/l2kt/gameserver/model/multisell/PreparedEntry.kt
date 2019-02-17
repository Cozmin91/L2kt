package com.l2kt.gameserver.model.multisell

import com.l2kt.gameserver.model.item.instance.ItemInstance
import java.util.*

/**
 * A dynamic layer of [Entry], which holds the tax amount and can retain previous [ItemInstance] enchantment.
 */
class PreparedEntry(
    template: Entry,
    item: ItemInstance?,
    applyTaxes: Boolean,
    maintainEnchantment: Boolean,
    taxRate: Double
) : Entry() {
    override var taxAmount = 0
        set(value) {
            super.taxAmount = value
        }

    init {
        var adenaAmount = 0

        ingredients = ArrayList(template.ingredients.size)
        for (ing in template.ingredients) {
            if (ing.itemId == 57) {
                // Tax ingredients added only if taxes enabled
                if (ing.isTaxIngredient) {
                    // if taxes are to be applied, modify/add the adena count based on the template adena/ancient adena count
                    if (applyTaxes)
                        taxAmount += Math.round(ing.itemCount * taxRate).toInt()
                } else
                    adenaAmount += ing.itemCount

                // do not yet add this adena amount to the list as non-taxIngredient adena might be entered later (order not guaranteed)
                continue
            }

            val newIngredient = ing.copy
            if (maintainEnchantment && item != null && ing.isArmorOrWeapon)
                newIngredient.enchantLevel = item.enchantLevel

            ingredients.add(newIngredient)
        }

        // now add the adena, if any.
        adenaAmount += taxAmount // do not forget tax
        if (adenaAmount > 0)
            ingredients.add(Ingredient(57, adenaAmount, false, false))

        // now copy products
        products = ArrayList(template.products.size)
        for (ing in template.products) {
            if (!ing.isStackable)
                isStackable = false

            val newProduct = ing.copy
            if (maintainEnchantment && item != null && ing.isArmorOrWeapon)
                newProduct.enchantLevel = item.enchantLevel

            products.add(newProduct)
        }
    }
}