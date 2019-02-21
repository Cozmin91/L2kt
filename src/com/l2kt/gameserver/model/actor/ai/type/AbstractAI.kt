package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.Desire
import com.l2kt.gameserver.model.actor.ai.NextAction
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager

import java.util.concurrent.Future

abstract class AbstractAI protected constructor(val actor: Creature) {
    val desire = Desire()

    private var _nextAction: NextAction? = null

    /** Flags about client's state, in order to know which messages to send  */
    @Volatile
    protected var _clientMoving: Boolean = false

    /** Different targets this AI maintains  */
    var target: WorldObject? = null
        protected set

    var followTarget: Creature? = null

    /** The skill we are currently casting by INTENTION_CAST  */
    protected var _skill: L2Skill? = null

    /** Different internal state flags  */
    private var _moveToPawnTimeout: Long = 0
    protected var _clientMovingToPawnOffset: Int = 0

    protected var _followTask: Future<*>? = null

    /**
     * Set the Intention of this AbstractAI.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method is USED by AI classes</B></FONT><BR></BR>
     * <BR></BR>
     * <B><U> Overridden in </U> : </B><BR></BR>
     * <B>L2AttackableAI</B> : Create an AI Task executed every 1s (if necessary)<BR></BR>
     * <B>L2PlayerAI</B> : Stores the current AI intention parameters to later restore it if necessary<BR></BR>
     * <BR></BR>
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     */
    @Synchronized
    open fun changeIntention(intention: CtrlIntention, arg0: Any?, arg1: Any?) {
        desire.update(intention, arg0, arg1)
    }

    /**
     * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR></BR>
     * <BR></BR>
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention (optional target)
     * @param arg1 The second parameter of the Intention (optional target)
     */
    @JvmOverloads
    fun setIntention(intention: CtrlIntention, arg0: Any? = null, arg1: Any? = null) {
        // Stop the follow mode if necessary
        if (intention !== CtrlIntention.FOLLOW && intention !== CtrlIntention.ATTACK)
            stopFollow()

        // Launch the onIntention method of the CreatureAI corresponding to the new Intention
        when (intention) {
            CtrlIntention.IDLE -> onIntentionIdle()
            CtrlIntention.ACTIVE -> onIntentionActive()
            CtrlIntention.REST -> onIntentionRest()
            CtrlIntention.ATTACK -> onIntentionAttack(arg0 as Creature?)
            CtrlIntention.CAST -> onIntentionCast(arg0 as L2Skill, arg1 as WorldObject?)
            CtrlIntention.MOVE_TO -> onIntentionMoveTo(arg0 as Location)
            CtrlIntention.FOLLOW -> onIntentionFollow(arg0 as Creature)
            CtrlIntention.PICK_UP -> onIntentionPickUp(arg0 as WorldObject)
            CtrlIntention.INTERACT -> onIntentionInteract(arg0 as WorldObject?)
        }

        // If do move or follow intention drop next action.
        if (_nextAction != null && _nextAction!!.intention === intention)
            _nextAction = null
    }

    /**
     * Launch the CreatureAI onEvt method corresponding to the Event.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR></BR>
     * <BR></BR>
     * @param evt The event whose the AI must be notified
     * @param arg0 The first parameter of the Event (optional target)
     * @param arg1 The second parameter of the Event (optional target)
     */
    @JvmOverloads
    fun notifyEvent(evt: CtrlEvent, arg0: Any? = null, arg1: Any? = null) {
        if (!actor.isVisible && !actor.isTeleporting || !actor.hasAI())
            return

        when (evt) {
            CtrlEvent.EVT_THINK -> onEvtThink()
            CtrlEvent.EVT_ATTACKED -> onEvtAttacked(arg0 as Creature?)
            CtrlEvent.EVT_AGGRESSION -> onEvtAggression(arg0 as Creature?, (arg1 as Number).toInt())
            CtrlEvent.EVT_STUNNED -> onEvtStunned(arg0 as Creature?)
            CtrlEvent.EVT_PARALYZED -> onEvtParalyzed(arg0 as Creature?)
            CtrlEvent.EVT_SLEEPING -> onEvtSleeping(arg0 as Creature?)
            CtrlEvent.EVT_ROOTED -> onEvtRooted(arg0 as Creature?)
            CtrlEvent.EVT_CONFUSED -> onEvtConfused(arg0 as Creature?)
            CtrlEvent.EVT_MUTED -> onEvtMuted(arg0 as Creature?)
            CtrlEvent.EVT_EVADED -> onEvtEvaded(arg0 as Creature?)
            CtrlEvent.EVT_READY_TO_ACT -> if (!actor.isCastingNow && !actor.isCastingSimultaneouslyNow)
                onEvtReadyToAct()
            CtrlEvent.EVT_ARRIVED -> if (!actor.isCastingNow && !actor.isCastingSimultaneouslyNow)
                onEvtArrived()
            CtrlEvent.EVT_ARRIVED_BLOCKED -> onEvtArrivedBlocked(arg0 as SpawnLocation?)
            CtrlEvent.EVT_CANCEL -> onEvtCancel()
            CtrlEvent.EVT_DEAD -> onEvtDead()
            CtrlEvent.EVT_FAKE_DEATH -> onEvtFakeDeath()
            CtrlEvent.EVT_FINISH_CASTING -> onEvtFinishCasting()
        }

        // Do next action.
        if (_nextAction != null && _nextAction!!.event === evt) {
            _nextAction!!.run()
            _nextAction = null
        }
    }

    protected abstract fun onIntentionIdle()

    protected abstract fun onIntentionActive()

    protected abstract fun onIntentionRest()

    protected abstract fun onIntentionAttack(target: Creature?)

    protected abstract fun onIntentionCast(skill: L2Skill, target: WorldObject?)

    protected abstract fun onIntentionMoveTo(loc: Location)

    protected abstract fun onIntentionFollow(target: Creature)

    protected abstract fun onIntentionPickUp(item: WorldObject)

    protected abstract fun onIntentionInteract(`object`: WorldObject?)

    protected abstract fun onEvtThink()

    protected abstract fun onEvtAttacked(attacker: Creature?)

    protected abstract fun onEvtAggression(target: Creature?, aggro: Int)

    protected abstract fun onEvtStunned(attacker: Creature?)

    protected abstract fun onEvtParalyzed(attacker: Creature?)

    protected abstract fun onEvtSleeping(attacker: Creature?)

    protected abstract fun onEvtRooted(attacker: Creature?)

    protected abstract fun onEvtConfused(attacker: Creature?)

    protected abstract fun onEvtMuted(attacker: Creature?)

    protected abstract fun onEvtEvaded(attacker: Creature?)

    protected abstract fun onEvtReadyToAct()

    protected abstract fun onEvtArrived()

    protected abstract fun onEvtArrivedBlocked(loc: SpawnLocation?)

    protected abstract fun onEvtCancel()

    protected abstract fun onEvtDead()

    protected abstract fun onEvtFakeDeath()

    protected abstract fun onEvtFinishCasting()

    /**
     * Cancel action client side by sending Server->Client packet ActionFailed to the Player actor.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR></BR>
     * <BR></BR>
     */
    protected open fun clientActionFailed() {}

    /**
     * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn <I>(broadcast)</I>.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR></BR>
     * <BR></BR>
     * @param pawn
     * @param offset
     */
    protected open fun moveToPawn(pawn: WorldObject?, offset: Int) {
        var offset = offset
        // Check if actor can move
        if (!actor.isMovementDisabled) {
            if (offset < 10)
                offset = 10

            // prevent possible extra calls to this function (there is none?).
            if (_clientMoving && target === pawn) {
                if (_clientMovingToPawnOffset == offset) {
                    if (System.currentTimeMillis() < _moveToPawnTimeout) {
                        clientActionFailed()
                        return
                    }
                } else if (actor.isOnGeodataPath) {
                    // minimum time to calculate new route is 2 seconds
                    if (System.currentTimeMillis() < _moveToPawnTimeout + 1000) {
                        clientActionFailed()
                        return
                    }
                }
            }

            // Set AI movement data
            _clientMoving = true
            _clientMovingToPawnOffset = offset
            target = pawn
            _moveToPawnTimeout = System.currentTimeMillis() + 1000

            if (pawn == null) {
                clientActionFailed()
                return
            }

            // Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
            actor.moveToLocation(pawn.x, pawn.y, pawn.z, offset)

            if (!actor.isMoving) {
                clientActionFailed()
                return
            }

            // Broadcast MoveToPawn/MoveToLocation packet
            if (pawn is Creature) {
                if (actor.isOnGeodataPath) {
                    actor.broadcastPacket(MoveToLocation(actor))
                    _clientMovingToPawnOffset = 0
                } else
                    actor.broadcastPacket(MoveToPawn(actor, pawn, offset))
            } else
                actor.broadcastPacket(MoveToLocation(actor))
        } else
            clientActionFailed()
    }

    /**
     * Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation <I>(broadcast)</I>.<br></br>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT>
     * @param x
     * @param y
     * @param z
     */
    protected open fun moveTo(x: Int, y: Int, z: Int) {
        // Chek if actor can move
        if (!actor.isMovementDisabled) {
            // Set AI movement data
            _clientMoving = true
            _clientMovingToPawnOffset = 0

            // Calculate movement data for a move to location action and add the actor to movingObjects of GameTimeController
            actor.moveToLocation(x, y, z, 0)

            // Broadcast MoveToLocation packet
            actor.broadcastPacket(MoveToLocation(actor))

        } else
            clientActionFailed()
    }

    /**
     * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation <I>(broadcast)</I>.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR></BR>
     * <BR></BR>
     * @param loc
     */
    protected open fun clientStopMoving(loc: SpawnLocation?) {
        // Stop movement of the Creature
        if (actor.isMoving)
            actor.stopMove(loc)

        _clientMovingToPawnOffset = 0

        if (_clientMoving || loc != null) {
            _clientMoving = false

            actor.broadcastPacket(StopMove(actor))

            if (loc != null)
                actor.broadcastPacket(StopRotation(actor.objectId, loc.heading, 0))
        }
    }

    // Client has already arrived to target, no need to force StopMove packet
    protected open fun clientStoppedMoving() {
        if (_clientMovingToPawnOffset > 0)
        // movetoPawn needs to be stopped
        {
            _clientMovingToPawnOffset = 0
            actor.broadcastPacket(StopMove(actor))
        }
        _clientMoving = false
    }

    /**
     * Activate the attack stance on clients, broadcasting [AutoAttackStart] packets. Refresh the timer if already on stance.
     */
    open fun startAttackStance() {
        if (!AttackStanceTaskManager.isInAttackStance(actor))
            actor.broadcastPacket(AutoAttackStart(actor.objectId))

        AttackStanceTaskManager.add(actor)
    }

    /**
     * Deactivate the attack stance on clients, broadcasting [AutoAttackStop] packets.
     */
    open fun stopAttackStance() {
        actor.broadcastPacket(AutoAttackStop(actor.objectId))

        AttackStanceTaskManager.remove(actor)
    }

    /**
     * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die <I>(broadcast)</I>.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR></BR>
     * <BR></BR>
     */
    protected open fun clientNotifyDead() {
        // Broadcast Die packet
        actor.broadcastPacket(Die(actor))

        // Init AI
        desire.update(CtrlIntention.IDLE, null, null)
        target = null

        // Cancel the follow task if necessary
        stopFollow()
    }

    /**
     * Update the state of this actor client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the Player player.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : Low level function, used by AI subclasses</B></FONT><BR></BR>
     * <BR></BR>
     * @param player The L2PcIstance to notify with state of this Creature
     */
    open fun describeStateToPlayer(player: Player) {
        if (desire.intention === CtrlIntention.MOVE_TO) {
            if (_clientMovingToPawnOffset != 0 && followTarget != null)
                player.sendPacket(MoveToPawn(actor, followTarget!!, _clientMovingToPawnOffset))
            else
                player.sendPacket(MoveToLocation(actor))
        }
        // else if (getIntention() == CtrlIntention.CAST) TODO
    }

    /**
     * Create and Launch an AI Follow Task to execute every 1s.<BR></BR>
     * <BR></BR>
     * @param target The Creature to follow
     */
    @Synchronized
    fun startFollow(target: Creature) {
        if (_followTask != null) {
            _followTask!!.cancel(false)
            _followTask = null
        }

        // Create and Launch an AI Follow Task to execute every 1s
        followTarget = target
        _followTask = ThreadPool.scheduleAtFixedRate(FollowTask(), 5, FOLLOW_INTERVAL.toLong())
    }

    /**
     * Create and Launch an AI Follow Task to execute every 0.5s, following at specified range.
     * @param target The Creature to follow
     * @param range
     */
    @Synchronized
    fun startFollow(target: Creature?, range: Int) {
        if (_followTask != null) {
            _followTask!!.cancel(false)
            _followTask = null
        }

        followTarget = target
        _followTask = ThreadPool.scheduleAtFixedRate(FollowTask(range), 5, ATTACK_FOLLOW_INTERVAL.toLong())
    }

    /**
     * Stop an AI Follow Task.
     */
    @Synchronized
    fun stopFollow() {
        if (_followTask != null) {
            // Stop the Follow Task
            _followTask!!.cancel(false)
            _followTask = null
        }
        followTarget = null
    }

    /**
     * Stop all Ai tasks and futures.
     */
    open fun stopAITask() {
        stopFollow()
    }

    /**
     * @param nextAction the _nextAction to set
     */
    fun setNextAction(nextAction: NextAction) {
        _nextAction = nextAction
    }

    override fun toString(): String {
        return "Actor: $actor"
    }

    private inner class FollowTask : Runnable {
        protected var _range = 70

        constructor() {}

        constructor(range: Int) {
            _range = range
        }

        override fun run() {
            if (_followTask == null)
                return

            val followTarget = followTarget
            if (followTarget == null) {
                if (actor is Summon)
                    actor.followStatus = false

                setIntention(CtrlIntention.IDLE)
                return
            }

            if (!actor.isInsideRadius(followTarget, _range, true, false))
                moveToPawn(followTarget, _range)
        }
    }

    companion object {
        private val FOLLOW_INTERVAL = 1000
        private val ATTACK_FOLLOW_INTERVAL = 500
    }
}
/**
 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR></BR>
 * <BR></BR>
 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR></BR>
 * <BR></BR>
 * @param intention The new Intention to set to the AI
 */
/**
 * Launch the CreatureAI onIntention method corresponding to the new Intention.<BR></BR>
 * <BR></BR>
 * <FONT COLOR=#FF0000><B> <U>Caution</U> : Stop the FOLLOW mode if necessary</B></FONT><BR></BR>
 * <BR></BR>
 * @param intention The new Intention to set to the AI
 * @param arg0 The first parameter of the Intention (optional target)
 */
/**
 * Launch the CreatureAI onEvt method corresponding to the Event.<BR></BR>
 * <BR></BR>
 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR></BR>
 * <BR></BR>
 * @param evt The event whose the AI must be notified
 */
/**
 * Launch the CreatureAI onEvt method corresponding to the Event.<BR></BR>
 * <BR></BR>
 * <FONT COLOR=#FF0000><B> <U>Caution</U> : The current general intention won't be change (ex : If the character attack and is stunned, he will attack again after the stunned periode)</B></FONT><BR></BR>
 * <BR></BR>
 * @param evt The event whose the AI must be notified
 * @param arg0 The first parameter of the Event (optional target)
 */