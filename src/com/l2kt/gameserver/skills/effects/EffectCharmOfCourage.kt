package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.EtcStatusUpdate
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectCharmOfCourage(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CHARMOFCOURAGE
    }

    override fun onStart(): Boolean {
        if (effected is Player) {
            effected.broadcastPacket(EtcStatusUpdate(effected as Player))
            return true
        }
        return false
    }

    override fun onExit() {
        if (effected is Player)
            effected.broadcastPacket(EtcStatusUpdate(effected as Player))
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.CHARM_OF_COURAGE.mask
    }
}