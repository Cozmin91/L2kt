package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class EffectSilentMove(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun onStart(): Boolean {
        super.onStart()
        return true
    }

    override fun onExit() {
        super.onExit()
    }

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SILENT_MOVE
    }

    override fun onActionTime(): Boolean {
        if (skill.skillType !== L2SkillType.CONT)
            return false

        if (effected.isDead)
            return false

        val manaDam = calc()

        if (manaDam > effected.currentMp) {
            val sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP)
            effected.sendPacket(sm)
            return false
        }

        effected.reduceCurrentMp(manaDam)
        return true
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.SILENT_MOVE.mask
    }
}