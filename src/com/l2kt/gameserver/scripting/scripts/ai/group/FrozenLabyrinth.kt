package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Frozen Labyrinth<br></br>
 * Those mobs split if you use physical attacks on them.
 */
class FrozenLabyrinth : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addSkillSeeId(PRONGHORN, FROST_BUFFALO)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        // Offensive physical skill casted on npc.
        if (skill != null && !skill.isMagic && skill.isOffensive && targets[0] === npc) {
            var spawnId = LOST_BUFFALO
            if (npc.npcId == PRONGHORN)
                spawnId = PRONGHORN_SPIRIT

            var diff = 0
            for (i in 0 until Rnd[6, 8]) {
                val x = if (diff < 60) npc.x + diff else npc.x
                val y = if (diff >= 60) npc.y + (diff - 40) else npc.y

                val monster = addSpawn(spawnId, x, y, npc.z, npc.heading, false, 120000, false) as Attackable
                attack(monster, caster)
                diff += 20
            }
            npc.deleteMe()
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    companion object {
        private const val PRONGHORN_SPIRIT = 22087
        private const val PRONGHORN = 22088
        private const val LOST_BUFFALO = 22093
        private const val FROST_BUFFALO = 22094
    }
}