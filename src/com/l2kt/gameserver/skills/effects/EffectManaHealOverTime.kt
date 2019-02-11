package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectManaHealOverTime(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.MANA_HEAL_OVER_TIME
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead)
            return false

        var mp = effected.currentMp
        val maxmp = effected.maxMp.toDouble()
        mp += calc()

        if (mp > maxmp)
            mp = maxmp

        effected.currentMp = mp
        val sump = StatusUpdate(effected)
        sump.addAttribute(StatusUpdate.CUR_MP, mp.toInt())
        effected.sendPacket(sump)
        return true
    }
}