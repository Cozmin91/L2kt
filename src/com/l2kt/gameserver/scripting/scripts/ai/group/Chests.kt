package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Chest
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class Chests : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(NPC_IDS, EventType.ON_ATTACK, EventType.ON_SKILL_SEE)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (npc is Chest) {
            // This behavior is only run when the target of skill is the passed npc.
            if (!ArraysUtil.contains(targets, npc))
                return super.onSkillSee(npc, caster, skill, targets, isPet)

// If this chest has already been interacted, no further AI decisions are needed.
            if (!npc.isInteracted) {
                npc.setInteracted()

                // If it's the first interaction, check if this is a box or mimic.
                if (Rnd[100] < 40 && skill != null) {
                    when (skill.id) {
                        SKILL_BOX_KEY, SKILL_DELUXE_KEY -> {
                            // check the chance to open the box.
                            var keyLevelNeeded = npc.level / 10 - skill.level
                            if (keyLevelNeeded < 0)
                                keyLevelNeeded *= -1

                            // Regular keys got 60% to succeed.
                            val chance = (if (skill.id == SKILL_BOX_KEY) 60 else 100) - keyLevelNeeded * 40

                            // Success, die with rewards.
                            if (Rnd[100] < chance) {
                                npc.setSpecialDrop()
                                npc.doDie(caster)
                            } else
                                npc.deleteMe()// Used a key but failed to open: disappears with no rewards.
                            // TODO: replace for a better system (as chests attack once before decaying)
                        }

                        else -> npc.doCast(
                            SkillTable.getInfo(
                                4143,
                                Math.min(10, Math.round((npc.level / 10).toFloat()))
                            )
                        )
                    }
                } else
                    attack(npc, if (isPet) caster?.pet else caster)// Mimic behavior : attack the caster.
            }
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc is Chest) {

            // If this has already been interacted, no further AI decisions are needed.
            if (!npc.isInteracted) {
                npc.setInteracted()

                // If it was a box, cast a suicide type skill.
                if (Rnd[100] < 40)
                    npc.doCast(SkillTable.getInfo(4143, Math.min(10, Math.round((npc.level / 10).toFloat()))))
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private const val SKILL_DELUXE_KEY = 2229
        private const val SKILL_BOX_KEY = 2065

        private val NPC_IDS = intArrayOf(
            18265,
            18266,
            18267,
            18268,
            18269,
            18270,
            18271,
            18272,
            18273,
            18274,
            18275,
            18276,
            18277,
            18278,
            18279,
            18280,
            18281,
            18282,
            18283,
            18284,
            18285,
            18286,
            18287,
            18288,
            18289,
            18290,
            18291,
            18292,
            18293,
            18294,
            18295,
            18296,
            18297,
            18298,
            21671,
            21694,
            21717,
            21740,
            21763,
            21786,
            21801,
            21802,
            21803,
            21804,
            21805,
            21806,
            21807,
            21808,
            21809,
            21810,
            21811,
            21812,
            21813,
            21814,
            21815,
            21816,
            21817,
            21818,
            21819,
            21820,
            21821,
            21822
        )
    }
}