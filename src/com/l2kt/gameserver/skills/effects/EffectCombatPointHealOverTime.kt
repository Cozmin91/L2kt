package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectCombatPointHealOverTime(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.COMBAT_POINT_HEAL_OVER_TIME
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead())
            return false

        var cp = effected.currentCp
        val maxcp = effected.maxCp.toDouble()
        cp += calc()

        if (cp > maxcp)
            cp = maxcp

        effected.currentCp = cp
        val sump = StatusUpdate(effected)
        sump.addAttribute(StatusUpdate.CUR_CP, cp.toInt())
        effected.sendPacket(sump)
        return true
    }
}
