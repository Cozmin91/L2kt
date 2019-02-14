package com.l2kt.gameserver.model

/**
 * This class contains all infos of the L2Attackable against the absorber Creature.
 *
 *  * _absorbedHP : The amount of HP at the moment attacker used the item.
 *  * _itemObjectId : The item id of the Soul Crystal used.
 *
 */
class AbsorbInfo(var itemId: Int) {
    var isRegistered: Boolean = false
    private var _absorbedHpPercent: Int = 0

    fun setAbsorbedHpPercent(percent: Int) {
        _absorbedHpPercent = percent
    }

    fun isValid(itemId: Int): Boolean {
        return this.itemId == itemId && _absorbedHpPercent < 50
    }
}