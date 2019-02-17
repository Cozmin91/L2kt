package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId

internal abstract class PlayableAI(playable: Playable) : CreatureAI(playable) {

    override fun onIntentionAttack(target: Creature?) {
        if (target is Playable) {
            val targetPlayer = target.actingPlayer
            val actorPlayer = actor.actingPlayer

            if (!target.isInsideZone(ZoneId.PVP)) {
                if (targetPlayer!!.protectionBlessing && actorPlayer!!.level - targetPlayer.level >= 10 && actorPlayer.karma > 0) {
                    // If attacker have karma, level >= 10 and target have Newbie Protection Buff
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                    clientActionFailed()
                    return
                }

                if (actorPlayer!!.protectionBlessing && targetPlayer.level - actorPlayer.level >= 10 && targetPlayer.karma > 0) {
                    // If target have karma, level >= 10 and actor have Newbie Protection Buff
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                    clientActionFailed()
                    return
                }
            }

            if (targetPlayer!!.isCursedWeaponEquipped && actorPlayer!!.level <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                clientActionFailed()
                return
            }

            if (actorPlayer!!.isCursedWeaponEquipped && targetPlayer.level <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                clientActionFailed()
                return
            }
        }
        super.onIntentionAttack(target)
    }

    override fun onIntentionCast(skill: L2Skill, target: WorldObject?) {
        if (target is Playable && skill.isOffensive) {
            val targetPlayer = target.actingPlayer
            val actorPlayer = actor.actingPlayer

            if (!target.isInsideZone(ZoneId.PVP)) {
                if (targetPlayer!!.protectionBlessing && actorPlayer!!.level - targetPlayer.level >= 10 && actorPlayer.karma > 0) {
                    // If attacker have karma, level >= 10 and target have Newbie Protection Buff
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                    clientActionFailed()
                    actor.setIsCastingNow(false)
                    return
                }

                if (actorPlayer!!.protectionBlessing && targetPlayer.level - actorPlayer.level >= 10 && targetPlayer.karma > 0) {
                    // If target have karma, level >= 10 and actor have Newbie Protection Buff
                    actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                    clientActionFailed()
                    actor.setIsCastingNow(false)
                    return
                }
            }

            if (targetPlayer!!.isCursedWeaponEquipped && actorPlayer!!.level <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                clientActionFailed()
                actor.setIsCastingNow(false)
                return
            }

            if (actorPlayer!!.isCursedWeaponEquipped && targetPlayer.level <= 20) {
                actorPlayer.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                clientActionFailed()
                actor.setIsCastingNow(false)
                return
            }
        }
        super.onIntentionCast(skill, target)
    }

    override fun stopAttackStance() {}
}