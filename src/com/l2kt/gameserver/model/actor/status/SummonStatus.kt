package com.l2kt.gameserver.model.actor.status

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.entity.Duel.DuelState
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class SummonStatus(activeChar: Summon) : PlayableStatus(activeChar) {

    override fun reduceHp(value: Double, attacker: Creature) {
        reduceHp(value, attacker, true, false, false)
    }

    override fun reduceHp(
        value: Double,
        attacker: Creature?,
        awake: Boolean,
        isDOT: Boolean,
        isHPConsumption: Boolean
    ) {
        if (activeChar.isDead)
            return

        val owner = activeChar.owner

        // We deny the duel, no matter if damage has been done or not.
        if (attacker != null) {
            val attackerPlayer = attacker.actingPlayer
            if (attackerPlayer != null && (owner == null || owner.duelId != attackerPlayer.duelId))
                attackerPlayer.duelState = DuelState.INTERRUPTED
        }

        super.reduceHp(value, attacker, awake, isDOT, isHPConsumption)

        // Since damages have been done, we can send damage message and EVT_ATTACKED notification.
        if (attacker != null) {
            if (!isDOT && owner != null)
                owner.sendPacket(
                    SystemMessage.getSystemMessage(if (activeChar is Servitor) SystemMessageId.SUMMON_RECEIVED_DAMAGE_S2_BY_S1 else SystemMessageId.PET_RECEIVED_S2_DAMAGE_BY_S1).addCharName(
                        attacker
                    ).addNumber(value.toInt())
                )

            activeChar.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, attacker)
        }
    }

    override val activeChar: Summon
        get() = super.activeChar as Summon
}