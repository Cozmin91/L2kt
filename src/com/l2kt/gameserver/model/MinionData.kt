package com.l2kt.gameserver.model

import com.l2kt.commons.random.Rnd

/**
 * This class defines the spawn data of a Minion type.<BR></BR>
 * In a group mob, there are one master called RaidBoss and several slaves called Minions.
 */
class MinionData {
    /** The Identifier of the L2Minion  */
    /**
     * @return the Identifier of the Minion to spawn.
     */
    /**
     * Set the Identifier of the Minion to spawn.
     * @param id The Creature Identifier to spawn
     */
    var minionId: Int = 0

    /** The number of this Minion Type to spawn  */
    /**
     * @return the amount of this Minion type to spawn.
     */
    /**
     * Set the amount of this Minion type to spawn.
     * @param amount The quantity of this Minion type to spawn
     */
    var amount: Int = 0
        get() {
            if (_minionAmountMax > _minionAmountMin) {
                amount = Rnd[_minionAmountMin, _minionAmountMax]
                return field
            }

            return _minionAmountMin
        }
    private var _minionAmountMin: Int = 0
    private var _minionAmountMax: Int = 0

    /**
     * Set the minimum of minions to amount.
     * @param amountMin The minimum quantity of this Minion type to spawn
     */
    fun setAmountMin(amountMin: Int) {
        _minionAmountMin = amountMin
    }

    /**
     * Set the maximum of minions to amount.
     * @param amountMax The maximum quantity of this Minion type to spawn
     */
    fun setAmountMax(amountMax: Int) {
        _minionAmountMax = amountMax
    }
}