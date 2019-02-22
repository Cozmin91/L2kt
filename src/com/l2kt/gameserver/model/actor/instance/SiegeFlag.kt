package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class SiegeFlag(player: Player, objectId: Int, template: NpcTemplate) : Npc(objectId, template) {
    private val _clan: Clan?

    override val isAttackable: Boolean
        get() = true

    init {

        _clan = player.clan

        // Player clan became null during flag initialization ; don't bother setting clan flag.
        if (_clan != null)
            _clan.flag = this
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return true
    }

    override fun doDie(killer: Creature?): Boolean {
        if (!super.doDie(killer))
            return false

        // Reset clan flag to null.
        if (_clan != null)
            _clan.flag = null

        return true
    }

    override fun onForcedAttack(player: Player) {
        onAction(player)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player) && Math.abs(player.z - z) < 100)
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)
            }
        }
    }

    override fun reduceCurrentHp(damage: Double, attacker: Creature, skill: L2Skill) {
        // Send warning to owners of headquarters that theirs base is under attack.
        if (_clan != null && isScriptValue(0)) {
            _clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.BASE_UNDER_ATTACK))

            scriptValue = 1
            ThreadPool.schedule(Runnable{ scriptValue = 0 }, 30000)
        }
        super.reduceCurrentHp(damage, attacker, skill)
    }

    override fun addFuncsToNewCharacter() {}
}