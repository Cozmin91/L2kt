package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

internal class EffectManaDamOverTime(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.MANA_DMG_OVER_TIME
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead)
            return false

        val manaDam = calc()

        if (manaDam > effected.currentMp) {
            if (skill.isToggle) {
                val sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP)
                effected.sendPacket(sm)
                return false
            }
        }

        effected.reduceCurrentMp(manaDam)
        return true
    }
}