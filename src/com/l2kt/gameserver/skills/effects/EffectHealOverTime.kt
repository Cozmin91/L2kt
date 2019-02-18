package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ExRegenMax
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectHealOverTime(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.HEAL_OVER_TIME
    }

    override fun onStart(): Boolean {
        if (effected is Player && totalCount > 0 && period > 0)
            effected.sendPacket(ExRegenMax(totalCount * period, period, calc()))

        return true
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead() || effected is Door)
            return false

        val maxHp = effected.maxHp.toDouble()

        var newHp = effected.currentHp + calc()
        if (newHp > maxHp)
            newHp = maxHp

        effected.currentHp = newHp

        val su = StatusUpdate(effected)
        su.addAttribute(StatusUpdate.CUR_HP, newHp.toInt())
        effected.sendPacket(su)
        return true
    }
}