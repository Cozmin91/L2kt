package com.l2kt.gameserver.model

import com.l2kt.gameserver.model.actor.Creature

/**
 * This class contains all AggroInfo of the L2Attackable against the attacker Creature.
 *
 *  * attacker : The attacker Creature concerned by this AggroInfo of this L2Attackable
 *  * hate : Hate level of this L2Attackable against the attacker Creature (hate = damage)
 *  * damage : Number of damages that the attacker Creature gave to this L2Attackable
 *
 */
class AggroInfo(val attacker: Creature) {

    var hate: Int = 0
        private set
    var damage: Int = 0
        private set

    fun checkHate(owner: Creature): Int {
        if (attacker.isAlikeDead || !attacker.isVisible || !owner.getKnownType(Creature::class.java).contains(attacker))
            hate = 0

        return hate
    }

    fun addHate(value: Int) {
        hate = Math.min(hate + value.toLong(), 999999999).toInt()
    }

    fun stopHate() {
        hate = 0
    }

    fun addDamage(value: Int) {
        damage = Math.min(damage + value.toLong(), 999999999).toInt()
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj)
            return true

        return if (obj is AggroInfo) obj.attacker === attacker else false

    }

    override fun hashCode(): Int {
        return attacker.objectId
    }
}