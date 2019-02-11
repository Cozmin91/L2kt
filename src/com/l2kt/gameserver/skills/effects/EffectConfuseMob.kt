package com.l2kt.gameserver.skills.effects

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Chest
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType
import java.util.*

class EffectConfuseMob(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CONFUSE_MOB_ONLY
    }

    override fun onStart(): Boolean {
        effected.startConfused()
        onActionTime()
        return true
    }

    override fun onExit() {
        effected.stopConfused(this)
    }

    override fun onActionTime(): Boolean {
        val targetList = ArrayList<Creature>()

        for (obj in effected.getKnownType(Attackable::class.java)) {
            if (obj !is Chest)
                targetList.add(obj)
        }

        if (targetList.isEmpty())
            return true

        val target = Rnd[targetList]

        effected.target = target
        effected.ai.setIntention(CtrlIntention.ATTACK, target)

        val aggro = (5 + Rnd[5]) * effector.level
        (effected as Attackable).addDamageHate(target, 0, aggro)

        return true
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.CONFUSED.mask
    }
}