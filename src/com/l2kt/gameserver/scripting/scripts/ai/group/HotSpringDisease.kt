package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class HotSpringDisease : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackActId(*MONSTERS_DISEASES[0])
    }

    override fun onAttackAct(npc: Npc, victim: Player): String? {
        for (i in 0..5) {
            if (MONSTERS_DISEASES[0][i] != npc.npcId)
                continue

            tryToApplyEffect(npc, victim, MALARIA)
            tryToApplyEffect(npc, victim, MONSTERS_DISEASES[1][i])
        }
        return super.onAttackAct(npc, victim)
    }

    companion object {
        // Diseases
        private const val MALARIA = 4554

        // Chance
        private const val DISEASE_CHANCE = 1

        // Monsters
        private val MONSTERS_DISEASES = arrayOf(
            intArrayOf(21314, 21316, 21317, 21319, 21321, 21322),
            intArrayOf(4551, 4552, 4553, 4552, 4551, 4553)
        )

        private fun tryToApplyEffect(npc: Npc, victim: Player, skillId: Int) {
            if (Rnd[100] < DISEASE_CHANCE) {
                var level = 1

                val effects = victim.allEffects
                if (effects.isNotEmpty()) {
                    for (e in effects) {
                        if (e.skill.id != skillId)
                            continue

                        level += e.skill.level
                        e.exit()
                    }
                }

                if (level > 10)
                    level = 10

                SkillTable.getInfo(skillId, level)!!.getEffects(npc, victim)
            }
        }
    }
}