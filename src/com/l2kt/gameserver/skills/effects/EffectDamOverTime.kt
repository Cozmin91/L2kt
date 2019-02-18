package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectDamOverTime(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.DMG_OVER_TIME
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead())
            return false

        var damage = calc()
        if (damage >= effected.currentHp) {
            if (skill.isToggle) {
                effected.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP))
                return false
            }

            if (!skill.killByDOT()) {
                if (effected.currentHp <= 1)
                    return true

                damage = effected.currentHp - 1
            }
        }
        effected.reduceCurrentHpByDOT(damage, effector, skill)

        return true
    }
}