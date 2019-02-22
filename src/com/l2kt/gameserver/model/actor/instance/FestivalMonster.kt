package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * L2FestivalMonsterInstance This class manages all attackable festival NPCs, spawned during the Festival of Darkness.
 * @author Tempy
 */
class FestivalMonster(objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    protected var _bonusMultiplier = 1

    fun setOfferingBonus(bonusMultiplier: Int) {
        _bonusMultiplier = bonusMultiplier
    }

    /**
     * Return True if the attacker is not another L2FestivalMonsterInstance.
     */
    override fun isAutoAttackable(attacker: Creature): Boolean {
        return attacker !is FestivalMonster

    }

    /**
     * All mobs in the festival are aggressive, and have high aggro range.
     */
    override val isAggressive: Boolean
        get() = true

    /**
     * All mobs in the festival don't need random animation.
     */
    override fun hasRandomAnimation(): Boolean {
        return false
    }

    /**
     * Add a blood offering item to the leader of the party.
     */
    override fun doItemDrop(template: NpcTemplate, attacker: Creature?) {
        val player = attacker?.actingPlayer
        if (player == null || !player.isInParty)
            return

        player.party!!.leader.addItem("Sign", SevenSignsFestival.FESTIVAL_OFFERING_ID, _bonusMultiplier, attacker, true)

        super.doItemDrop(template, attacker)
    }
}