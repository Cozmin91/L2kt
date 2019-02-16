package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.*
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectFear(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.FEAR
    }

    override fun onStart(): Boolean {
        if (effected is Player && effector is Player) {
            when (skill.id) {
                1376, 1169, 65, 1092, 98, 1272, 1381, 763 -> {
                }
                else -> return false
            }
        }

        if (effected is Folk || effected is SiegeFlag || effected is SiegeSummon)
            return false

        if (effected.isAfraid)
            return false

        effected.startFear()
        onActionTime()
        return true
    }

    override fun onExit() {
        effected.stopFear(true)
    }

    override fun onActionTime(): Boolean {
        if (effected !is Pet)
            effected.setRunning()

        val victimX = effected.x
        val victimY = effected.y
        val victimZ = effected.z

        val posX = victimX + (if (victimX > effector.x) 1 else -1) * FEAR_RANGE
        val posY = victimY + (if (victimY > effector.y) 1 else -1) * FEAR_RANGE

        effected.ai.setIntention(
            CtrlIntention.MOVE_TO,
            GeoEngine.canMoveToTargetLoc(victimX, victimY, victimZ, posX, posY, victimZ)
        )
        return true
    }

    override fun onSameEffect(effect: L2Effect?): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.FEAR.mask
    }

    companion object {
        const val FEAR_RANGE = 500
    }
}