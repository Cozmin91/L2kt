package com.l2kt.gameserver.model.tradelist

import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item

class TradeItem {
    var objectId: Int = 0
    val item: Item
    var enchant: Int = 0
    var count: Int = 0
    var price: Int = 0

    constructor(item: ItemInstance, count: Int, price: Int) {
        objectId = item.objectId
        this.item = item.item
        enchant = item.enchantLevel
        this.count = count
        this.price = price
    }

    constructor(item: Item, count: Int, price: Int) {
        objectId = 0
        this.item = item
        enchant = 0
        this.count = count
        this.price = price
    }

    constructor(item: TradeItem, count: Int, price: Int) {
        objectId = item.objectId
        this.item = item.item
        enchant = item.enchant
        this.count = count
        this.price = price
    }
}