package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * Frenzy behavior, so far 5 types of orcs.<br></br>
 * Few others monsters got that skillid, need to investigate later :
 *
 *  * Halisha's Officer
 *  * Executioner of Halisha
 *  * Alpine Kookaburra
 *  * Alpine Buffalo
 *  * Alpine Cougar
 *
 */
class FrenzyOnAttack : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackId(20270, 20495, 20588, 20778, 21116)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        // The only requirements are HPs < 25% and not already under the buff. It's not 100% aswell.
        if (npc.currentHp / npc.maxHp < 0.25 && npc.getFirstEffect(ULTIMATE_BUFF) == null && Rnd[10] == 0) {
            npc.broadcastNpcSay(Rnd[ORCS_WORDS]!!)
            npc.target = npc
            npc.doCast(ULTIMATE_BUFF)
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private val ULTIMATE_BUFF = SkillTable.getInfo(4318, 1)

        private val ORCS_WORDS = arrayOf(
            "Dear ultimate power!!!",
            "The battle has just begun!",
            "I never thought I'd use this against a novice!",
            "You won't take me down easily."
        )
    }
}