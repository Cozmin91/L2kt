package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

internal class SummonAI(summon: Summon) : PlayableAI(summon) {

    @Volatile
    private var _thinking: Boolean = false // to prevent recursive thinking
    @Volatile
    private var _startFollow = (actor as Summon).followStatus
    private var _lastAttack: Creature? = null

    override fun onIntentionIdle() {
        stopFollow()
        _startFollow = false
        onIntentionActive()
    }

    override fun onIntentionActive() {
        val summon = actor as Summon
        if (_startFollow)
            setIntention(CtrlIntention.FOLLOW, summon.owner)
        else
            super.onIntentionActive()
    }

    private fun thinkAttack() {
        var target = target as Creature?

        if (checkTargetLostOrDead(target)) {
            this.target = null
            return
        }

        if (maybeMoveToPawn(target, actor.physicalAttackRange))
            return

        clientStopMoving(null)
        actor.doAttack(target)
    }

    private fun thinkCast() {
        var target = target
        if (checkTargetLost(target)) {
            this.target = null
            return
        }

        val `val` = _startFollow
        if (maybeMoveToPawn(target, _skill!!.castRange))
            return

        clientStopMoving(null)
        (actor as Summon).followStatus = false
        setIntention(CtrlIntention.IDLE)

        _startFollow = `val`
        actor.doCast(_skill)
    }

    private fun thinkPickUp() {
        val target = target
        if (checkTargetLost(target))
            return

        if (maybeMoveToPawn(target, 36))
            return

        setIntention(CtrlIntention.IDLE)
        (actor as Summon).doPickupItem(target)
    }

    private fun thinkInteract() {
        val target = target
        if (checkTargetLost(target))
            return

        if (maybeMoveToPawn(target, 36))
            return

        setIntention(CtrlIntention.IDLE)
    }

    override fun onEvtThink() {
        if (_thinking || actor.isCastingNow || actor.isAllSkillsDisabled)
            return

        _thinking = true
        try {
            when (desire.intention) {
                CtrlIntention.ATTACK -> thinkAttack()
                CtrlIntention.CAST -> thinkCast()
                CtrlIntention.PICK_UP -> thinkPickUp()
                CtrlIntention.INTERACT -> thinkInteract()
            }
        } finally {
            _thinking = false
        }
    }

    override fun onEvtFinishCasting() {
        if (_lastAttack == null)
            (actor as Summon).followStatus = _startFollow
        else {
            setIntention(CtrlIntention.ATTACK, _lastAttack)
            _lastAttack = null
        }
    }

    override fun onEvtAttacked(attacker: Creature?) {
        super.onEvtAttacked(attacker)

        avoidAttack(attacker)
    }

    override fun onEvtEvaded(attacker: Creature?) {
        super.onEvtEvaded(attacker)

        avoidAttack(attacker)
    }

    override fun startAttackStance() {
        actor.actingPlayer!!.ai.startAttackStance()
    }

    private fun avoidAttack(attacker: Creature?) {
        val owner = (actor as Summon).owner

        // Must have a owner, the attacker can't be the owner and the owner must be in a short radius. The owner must be under attack stance (the summon CAN'T be under attack stance with current writing style).
        if (owner == null || owner == attacker || !owner.isInsideRadius(
                actor,
                2 * AVOID_RADIUS,
                true,
                false
            ) || !AttackStanceTaskManager.isInAttackStance(owner)
        )
            return

        // Current summon intention must be ACTIVE or FOLLOW type.
        if (desire.intention !== CtrlIntention.ACTIVE && desire.intention !== CtrlIntention.FOLLOW)
            return

        // Summon mustn't be under movement, must be alive and not be movement disabled.
        if (_clientMoving || actor.isDead() || actor.isMovementDisabled())
            return

        val ownerX = owner.x
        val ownerY = owner.y
        val angle = Math.toRadians(Rnd[-90, 90].toDouble()) + Math.atan2(
            (ownerY - actor.y).toDouble(),
            (ownerX - actor.x).toDouble()
        )

        val targetX = ownerX + (AVOID_RADIUS * Math.cos(angle)).toInt()
        val targetY = ownerY + (AVOID_RADIUS * Math.sin(angle)).toInt()

        // If the location is valid, move the summon.
        if (GeoEngine.canMoveToTarget(actor.x, actor.y, actor.z, targetX, targetY, actor.z))
            moveTo(targetX, targetY, actor.z)
    }

    fun notifyFollowStatusChange() {
        _startFollow = !_startFollow
        when (desire.intention) {
            CtrlIntention.ACTIVE, CtrlIntention.FOLLOW, CtrlIntention.IDLE, CtrlIntention.MOVE_TO, CtrlIntention.PICK_UP -> (actor as Summon).followStatus =
                _startFollow
        }
    }

    fun setStartFollowController(`val`: Boolean) {
        _startFollow = `val`
    }

    override fun onIntentionCast(skill: L2Skill, target: WorldObject?) {
        if (desire.intention === CtrlIntention.ATTACK)
            _lastAttack = target as Creature?
        else
            _lastAttack = null

        super.onIntentionCast(skill, target)
    }

    companion object {
        private val AVOID_RADIUS = 70
    }
}