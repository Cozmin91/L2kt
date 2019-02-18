package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectSpoil(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SPOIL
    }

    override fun onStart(): Boolean {
        if (effector !is Player)
            return false

        if (effected !is Monster)
            return false

        val target = effected as Monster
        if (target.isDead())
            return false

        if (target.spoilerId != 0) {
            effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED))
            return false
        }

        if (Formulas.calcMagicSuccess(effector, target, skill)) {
            target.spoilerId = effector.objectId
            effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPOIL_SUCCESS))
        }
        target.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, effector)
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}