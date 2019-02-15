package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.Sex
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import com.l2kt.gameserver.templates.skills.L2SkillType

/**
 * This script holds MoS monsters behavior. If they see you with an equipped weapon, they will speak and attack you.
 */
class Monastery : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addEventIds(BROTHERS_SEEKERS_MONKS, EventType.ON_AGGRO, EventType.ON_SPAWN, EventType.ON_SPELL_FINISHED)
        addEventIds(GUARDIANS_BEHOLDERS, EventType.ON_SKILL_SEE)
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        if (!npc.isInCombat) {
            if (player?.activeWeaponInstance != null) {
                npc.target = player
                npc.broadcastNpcSay((if (player.appearance.sex === Sex.FEMALE) "Sister " else "Brother ") + player.name + ", move your weapon away!")

                when (npc.npcId) {
                    22124, 22126 -> npc.doCast(SkillTable.getInfo(4589, 8))

                    else -> attack(npc as Attackable, player)
                }
            } else if ((npc as Attackable).mostHated == null)
                return null
        }
        return super.onAggro(npc, player, isPet)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (skill?.skillType === L2SkillType.AGGDAMAGE && targets.isNotEmpty()) {
            for (obj in targets) {
                if (obj == npc) {
                    npc.broadcastNpcSay((if (caster?.appearance?.sex === Sex.FEMALE) "Sister " else "Brother ") + caster?.name + ", move your weapon away!")
                    attack(npc as Attackable, caster)
                    break
                }
            }
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onSpawn(npc: Npc): String? {
        for (target in npc.getKnownTypeInRadius(Player::class.java, npc.template.aggroRange)) {
            if (!target.isDead && GeoEngine.getInstance().canSeeTarget(npc, target)) {
                if (target.activeWeaponInstance != null && !npc.isInCombat && npc.target == null) {
                    npc.target = target
                    npc.broadcastNpcSay((if (target.appearance.sex === Sex.FEMALE) "Sister " else "Brother ") + target.name + ", move your weapon away!")

                    when (npc.npcId) {
                        22124, 22126, 22127 -> npc.doCast(SkillTable.getInfo(4589, 8))

                        else -> attack(npc as Attackable, target)
                    }
                }
            }
        }
        return super.onSpawn(npc)
    }

    override fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        if (skill?.id == 4589)
            attack(npc as Attackable, player)

        return super.onSpellFinished(npc, player, skill)
    }

    companion object {
        private val BROTHERS_SEEKERS_MONKS = intArrayOf(22124, 22125, 22126, 22127, 22129)

        private val GUARDIANS_BEHOLDERS = intArrayOf(22134, 22135)
    }
}