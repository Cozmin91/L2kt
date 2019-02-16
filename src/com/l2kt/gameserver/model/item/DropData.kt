package com.l2kt.gameserver.model.item

/**
 * Special thanks to nuocnam
 * @author LittleVexy
 */
class DropData {

    /**
     * Returns the ID of the item dropped
     * @return int
     */
    /**
     * Sets the ID of the item dropped
     * @param itemId : int designating the ID of the item
     */
    var itemId: Int = 0
    /**
     * Returns the minimum quantity of items dropped
     * @return int
     */
    /**
     * Sets the value for minimal quantity of dropped items
     * @param mindrop : int designating the quantity
     */
    var minDrop: Int = 0
    /**
     * Returns the maximum quantity of items dropped
     * @return int
     */
    /**
     * Sets the value for maximal quantity of dopped items
     * @param maxdrop : int designating the quantity of dropped items
     */
    var maxDrop: Int = 0
    /**
     * Returns the chance of having a drop
     * @return int
     */
    /**
     * Sets the chance of having the item for a drop
     * @param chance : int designating the chance
     */
    var chance: Int = 0

    /**
     * Returns a report of the object
     * @return String
     */
    override fun toString(): String {
        return "ItemID: " + itemId + " Min: " + minDrop + " Max: " + maxDrop + " Chance: " + chance / 10000.0 + "%"
    }

    /**
     * Returns if parameter "o" is a L2DropData and has the same itemID that the current object
     * @return boolean
     */
    override fun equals(o: Any?): Boolean {
        if (o is DropData) {
            val drop = o as DropData?
            return drop!!.itemId == itemId
        }
        return false
    }

    companion object {
        const val MAX_CHANCE = 1000000
    }
}