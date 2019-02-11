package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.AbnormalEffect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectClanGate(env: Env, template: EffectTemplate) : L2Effect(env, template) {

    override fun onStart(): Boolean {
        effected.startAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE)
        if (effected is Player) {
            val clan = (effected as Player).clan
            if (clan != null) {
                val msg = SystemMessage.getSystemMessage(SystemMessageId.COURT_MAGICIAN_CREATED_PORTAL)
                clan.broadcastToOtherOnlineMembers(msg, effected as Player)
            }
        }

        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.stopAbnormalEffect(AbnormalEffect.MAGIC_CIRCLE)
    }

    override fun getEffectType(): L2EffectType {
        return L2EffectType.CLAN_GATE
    }
}