package com.l2kt.gameserver.model.multisell

import java.util.*

/**
 * A datatype entry used to store multisell. A multisell is similar to a buylist, but that system has been developped in addition.<br></br>
 * <br></br>
 * It holds a List of [Entry] and a Set of allowed npcIds for security reasons (lazy initialization).
 */
open class ListContainer(val id: Int) {

    var applyTaxes: Boolean = false
    var maintainEnchantment: Boolean = false

    var entries: MutableList<Entry> = ArrayList()
        protected set
    var _npcsAllowed: MutableSet<Int> = mutableSetOf()

    val isNpcOnly: Boolean
        get() = _npcsAllowed.isNotEmpty()

    fun allowNpc(npcId: Int) {
        _npcsAllowed.add(npcId)
    }

    fun isNpcAllowed(npcId: Int): Boolean {
        return _npcsAllowed.contains(npcId)
    }
}