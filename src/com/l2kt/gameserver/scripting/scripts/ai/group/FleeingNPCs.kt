package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * A fleeing NPC.<br></br>
 * <br></br>
 * His behavior is to always flee, and never attack.
 */
class FleeingNPCs : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackId(20432)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        // Calculate random coords.
        val rndX = npc.x + Rnd[-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE]
        val rndY = npc.y + Rnd[-Config.MAX_DRIFT_RANGE, Config.MAX_DRIFT_RANGE]

        // Wait the NPC to be immobile to move him again. Also check destination point.
        if (!npc.isMoving && GeoEngine.getInstance().canMoveToTarget(npc.x, npc.y, npc.z, rndX, rndY, npc.z))
            npc.ai.setIntention(CtrlIntention.MOVE_TO, Location(rndX, rndY, npc.z))

        return null
    }
}