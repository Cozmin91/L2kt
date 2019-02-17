package com.l2kt.gameserver.model.craft

import java.util.*

/**
 * A datacontainer used by private workshop system. It retains a List of [ManufactureItem]s, the store name and the shop state.
 */
class ManufactureList {
    private val _list = ArrayList<ManufactureItem>()

    private var _confirmed: Boolean = false
    var storeName: String? = null

    val list: MutableList<ManufactureItem>
        get() = _list

    fun setConfirmedTrade(confirmed: Boolean) {
        _confirmed = confirmed
    }

    fun hasConfirmed(): Boolean {
        return _confirmed
    }

    fun add(item: ManufactureItem) {
        _list.add(item)
    }
}