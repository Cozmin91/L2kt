package com.l2kt.gameserver.model.buylist

import java.util.*

/**
 * A datatype used to hold buylists. Each buylist got a Map of [Product].<br></br>
 * For security reasons and to avoid crafted packets, we added npcId aswell.
 */
class NpcBuyList(val listId: Int) {
    private val _products = LinkedHashMap<Int, Product>()

    var npcId: Int = 0

    val products: Collection<Product>
        get() = _products.values

    fun getProductByItemId(itemId: Int): Product? {
        return _products[itemId]
    }

    fun addProduct(product: Product) {
        _products[product.itemId] = product
    }

    fun isNpcAllowed(npcId: Int): Boolean {
        return this.npcId == npcId
    }
}