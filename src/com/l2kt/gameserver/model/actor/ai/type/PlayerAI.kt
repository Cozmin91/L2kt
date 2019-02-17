package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.Desire
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.StaticObject
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.AutoAttackStart
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

internal class PlayerAI(player: Player) : PlayableAI(player) {
    private var _thinking: Boolean = false // to prevent recursive thinking
    private val _nextIntention = Desire()

    override fun clientActionFailed() {
        actor.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override val nextIntention: Desire? get() = _nextIntention

    /**
     * Saves the current Intention for this L2PlayerAI if necessary and calls changeIntention in AbstractAI.<BR></BR>
     * <BR></BR>
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     */
    @Synchronized
    override fun changeIntention(intention: CtrlIntention, arg0: Any?, arg1: Any?) {
        // do nothing unless CAST intention
        // however, forget interrupted actions when starting to use an offensive skill
        if (intention !== CtrlIntention.CAST || arg0 != null && (arg0 as L2Skill).isOffensive) {
            _nextIntention.reset()
            super.changeIntention(intention, arg0, arg1)
            return
        }

        // do nothing if next intention is same as current one.
        if (desire.equals(intention, arg0!!, arg1!!))
            return

        // save current intention so it can be used after cast
        _nextIntention.update(desire)

        super.changeIntention(intention, arg0, arg1)
    }

    /**
     * Launch actions corresponding to the Event ReadyToAct.<BR></BR>
     * <BR></BR>
     * <B><U> Actions</U> :</B><BR></BR>
     * <BR></BR>
     *  * Launch actions corresponding to the Event Think<BR></BR>
     * <BR></BR>
     */
    override fun onEvtReadyToAct() {
        // Launch actions corresponding to the Event Think
        if (!_nextIntention.isBlank) {
            setIntention(_nextIntention.intention, _nextIntention.firstParameter, _nextIntention.secondParameter)
            _nextIntention.reset()
        }
        super.onEvtReadyToAct()
    }

    override fun onEvtCancel() {
        _nextIntention.reset()
        super.onEvtCancel()
    }

    /**
     * Finalize the casting of a skill. Drop latest intention before the actual CAST.
     */
    override fun onEvtFinishCasting() {
        if (desire.intention === CtrlIntention.CAST) {
            if (!_nextIntention.isBlank && _nextIntention.intention !== CtrlIntention.CAST)
            // previous state shouldn't be casting
                setIntention(_nextIntention.intention, _nextIntention.firstParameter, _nextIntention.secondParameter)
            else
                setIntention(CtrlIntention.IDLE)
        }
    }

    override fun onIntentionRest() {
        if (desire.intention !== CtrlIntention.REST) {
            changeIntention(CtrlIntention.REST, null, null)
            this.target = null
            clientStopMoving(null)
        }
    }

    override fun onIntentionActive() {
        setIntention(CtrlIntention.IDLE)
    }

    /**
     * Manage the Move To Intention : Stop current Attack and Launch a Move to Location Task.<BR></BR>
     * <BR></BR>
     * <B><U> Actions</U> : </B><BR></BR>
     * <BR></BR>
     *  * Stop the actor auto-attack server side AND client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Set the Intention of this AI to MOVE_TO
     *  * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)<BR></BR>
     * <BR></BR>
     */
    override fun onIntentionMoveTo(loc: Location) {
        // Deny the action if we are currently resting.
        if (desire.intention === CtrlIntention.REST) {
            clientActionFailed()
            return
        }

        // We delay MOVE_TO intention if character is disabled or is currently casting/attacking.
        if (actor.isAllSkillsDisabled || actor.isCastingNow || actor.isAttackingNow) {
            clientActionFailed()
            _nextIntention.update(CtrlIntention.MOVE_TO, loc, null)
            return
        }

        // Set the Intention of this AbstractAI to MOVE_TO
        changeIntention(CtrlIntention.MOVE_TO, loc, null)

        // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
        moveTo(loc.x, loc.y, loc.z)
    }

    override fun onIntentionInteract(`object`: WorldObject?) {
        // Deny the action if we are currently resting.
        if (desire.intention === CtrlIntention.REST) {
            clientActionFailed()
            return
        }

        // We delay INTERACT intention if character is disabled or is currently casting.
        if (actor.isAllSkillsDisabled || actor.isCastingNow) {
            clientActionFailed()
            _nextIntention.update(CtrlIntention.INTERACT, `object`, null)
            return
        }

        // Set the Intention of this AbstractAI to INTERACT
        changeIntention(CtrlIntention.INTERACT, `object`, null)

        // Set the AI interact target
        this.target = `object`

        // Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
        moveToPawn(`object`, 60)
    }

    override fun clientNotifyDead() {
        _clientMovingToPawnOffset = 0
        _clientMoving = false

        super.clientNotifyDead()
    }

    override fun startAttackStance() {
        if (!AttackStanceTaskManager.isInAttackStance(actor)) {
            val summon = (actor as Player).pet
            summon?.broadcastPacket(AutoAttackStart(summon.objectId))

            actor.broadcastPacket(AutoAttackStart(actor.objectId))
        }
        AttackStanceTaskManager.add(actor)
    }

    private fun thinkAttack() {
        val target = target as Creature?
        if (target == null) {
            this.target = null
            setIntention(CtrlIntention.ACTIVE)
            return
        }

        if (maybeMoveToPawn(target, actor.physicalAttackRange))
            return

        if (target.isAlikeDead) {
            if (target is Player && target.isFakeDeath)
                target.stopFakeDeath(true)
            else {
                setIntention(CtrlIntention.ACTIVE)
                return
            }
        }

        clientStopMoving(null)
        actor.doAttack(target)
    }

    private fun thinkCast() {
        val target = target as Creature?

        if(_skill == null)
            return

        if (_skill!!.targetType == L2Skill.SkillTargetType.TARGET_GROUND && actor is Player) {
            if (maybeMoveToPosition(actor.currentSkillWorldPosition, _skill!!.castRange)) {
                actor.setIsCastingNow(false)
                return
            }
        } else {
            if (checkTargetLost(target)) {
                // Notify the target
                if (_skill!!.isOffensive && target != null)
                    this.target = null

                actor.setIsCastingNow(false)
                return
            }

            if (target != null && maybeMoveToPawn(target, _skill!!.castRange)) {
                actor.setIsCastingNow(false)
                return
            }
        }

        if (_skill!!.hitTime > 50 && !_skill!!.isSimultaneousCast)
            clientStopMoving(null)

        actor.doCast(_skill)
    }

    private fun thinkPickUp() {
        if (actor.isAllSkillsDisabled || actor.isCastingNow || actor.isAttackingNow)
            return

        val target = target
        if (checkTargetLost(target))
            return

        if (maybeMoveToPawn(target, 36))
            return

        setIntention(CtrlIntention.IDLE)
        actor.actingPlayer!!.doPickupItem(target)
    }

    private fun thinkInteract() {
        if (actor.isAllSkillsDisabled || actor.isCastingNow)
            return

        val target = target
        if (checkTargetLost(target))
            return

        if (maybeMoveToPawn(target, 36))
            return

        if (target !is StaticObject)
            actor.actingPlayer!!.doInteract(target as Creature)

        setIntention(CtrlIntention.IDLE)
    }

    override fun onEvtThink() {
        // Check if the actor can't use skills and if a thinking action isn't already in progress
        if (_thinking && desire.intention !== CtrlIntention.CAST)
        // casting must always continue
            return

        // Start thinking action
        _thinking = true

        try {
            // Manage AI thoughts
            when (desire.intention) {
                CtrlIntention.ATTACK -> thinkAttack()
                CtrlIntention.CAST -> thinkCast()
                CtrlIntention.PICK_UP -> thinkPickUp()
                CtrlIntention.INTERACT -> thinkInteract()
            }
        } finally {
            // Stop thinking action
            _thinking = false
        }
    }
}