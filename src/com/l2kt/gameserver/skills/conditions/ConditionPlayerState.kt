package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionPlayerState(private val _check: PlayerState, private val _required: Boolean) : Condition() {
    enum class PlayerState {
        RESTING,
        MOVING,
        RUNNING,
        RIDING,
        FLYING,
        BEHIND,
        FRONT,
        OLYMPIAD
    }

    override fun testImpl(env: Env): Boolean {
        val character = env.character
        val player = env.player

        when (_check) {
            ConditionPlayerState.PlayerState.RESTING -> return if (player == null) !_required else player.isSitting == _required

            ConditionPlayerState.PlayerState.MOVING -> return character!!.isMoving == _required

            ConditionPlayerState.PlayerState.RUNNING -> return character!!.isMoving == _required && character.isRunning == _required

            ConditionPlayerState.PlayerState.RIDING -> return character!!.isRiding == _required

            ConditionPlayerState.PlayerState.FLYING -> return character!!.isFlying == _required

            ConditionPlayerState.PlayerState.BEHIND -> return character!!.isBehindTarget == _required

            ConditionPlayerState.PlayerState.FRONT -> return character!!.isInFrontOfTarget == _required

            ConditionPlayerState.PlayerState.OLYMPIAD -> return if (player == null) !_required else player.isInOlympiadMode == _required
        }
        return !_required
    }
}