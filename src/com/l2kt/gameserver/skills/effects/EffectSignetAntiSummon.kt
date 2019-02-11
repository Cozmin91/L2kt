package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.instance.EffectPoint
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectSignetAntiSummon(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _actor: EffectPoint? = null

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SIGNET_GROUND
    }

    override fun onStart(): Boolean {
        _actor = effected as EffectPoint
        return true
    }

    override fun onActionTime(): Boolean {
        if (count == totalCount - 1)
            return true

        val mpConsume = skill.mpConsume
        val caster = effector as Player

        for (cha in _actor!!.getKnownTypeInRadius(Playable::class.java, skill.skillRadius)) {
            if (!caster.canAttackCharacter(cha))
                continue

            val owner = cha.actingPlayer
            if (owner != null && owner.pet != null) {
                if (mpConsume > effector.currentMp) {
                    effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP))
                    return false
                }
                effector.reduceCurrentMp(mpConsume.toDouble())

                owner.pet!!.unSummon(owner)
                owner.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, effector)
            }
        }
        return true
    }

    override fun onExit() {
        if (_actor != null)
            _actor!!.deleteMe()
    }
}