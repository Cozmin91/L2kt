package com.l2kt.gameserver.model

import com.l2kt.gameserver.model.actor.Playable

/**
 * This class is used to retain damage infos made on a L2Attackable. It is used for reward purposes.
 */
class RewardInfo(val attacker: Playable) {

    var damage: Int = 0
        private set

    fun addDamage(damage: Int) {
        this.damage += damage
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true

        return if (obj is RewardInfo) obj.attacker === attacker else false

    }

    override fun hashCode(): Int {
        return attacker.objectId
    }
}