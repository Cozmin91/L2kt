package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType

class EffectChameleonRest(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.RELAXING
    }

    override fun onStart(): Boolean {
        if (effected is Player)
            (effected as Player).sitDown(false)
        else
            effected.ai.setIntention(CtrlIntention.REST)

        return super.onStart()
    }

    override fun onActionTime(): Boolean {
        if (effected.isDead())
            return false

        if (skill.skillType !== L2SkillType.CONT)
            return false

        if (effected is Player) {
            if (!(effected as Player).isSitting)
                return false
        }

        val manaDam = calc()

        if (manaDam > effected.currentMp) {
            effected.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP))
            return false
        }

        effected.reduceCurrentMp(manaDam)
        return true
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.SILENT_MOVE.mask or L2EffectFlag.RELAXING.mask
    }
}