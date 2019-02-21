package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * This class represents Friendly Mobs lying over the world.<br></br>
 * These friendly mobs should only attack players with karma > 0 and it is always aggro, since it just attacks players with karma.
 */
class FriendlyMonster(objectId: Int, template: NpcTemplate) : Attackable(objectId, template) {

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return attacker is Player && attacker.karma > 0
    }

    override fun isAggressive(): Boolean {
        return true
    }
}