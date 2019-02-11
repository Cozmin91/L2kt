package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Folk
import com.l2kt.gameserver.model.actor.instance.SiegeSummon
import com.l2kt.gameserver.network.serverpackets.StartRotation
import com.l2kt.gameserver.network.serverpackets.StopRotation
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectBluff(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.BLUFF
    }

    override fun onStart(): Boolean {
        if (effected is SiegeSummon || effected is Folk || effected.isRaidRelated || effected is Npc && (effected as Npc).npcId == 35062)
            return false

        effected.broadcastPacket(StartRotation(effected.objectId, effected.heading, 1, 65535))
        effected.broadcastPacket(StopRotation(effected.objectId, effector.heading, 65535))
        effected.heading = effector.heading
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}