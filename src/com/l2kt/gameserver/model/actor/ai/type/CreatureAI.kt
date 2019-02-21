package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.Desire
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Folk
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.network.serverpackets.AutoAttackStop
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager
import com.l2kt.gameserver.templates.skills.L2SkillType

open class CreatureAI(character: Creature) : AbstractAI(character) {

    open val nextIntention: Desire?
        get() = null

    override fun onEvtAttacked(attacker: Creature?) {
        if (actor !is Folk)
            startAttackStance()
    }

    /**
     * Manage the Idle Intention : Stop Attack, Movement and Stand Up the actor.
     *
     *  * Set the AI Intention to IDLE
     *  * Init cast and attack target
     *  * Stop the actor attack stance.
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *
     */
    override fun onIntentionIdle() {
        // Set the AI Intention to IDLE
        changeIntention(CtrlIntention.IDLE, null, null)

        // Init cast and attack target
        target = null

        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Stop the attack stance.
        stopAttackStance()
    }

    /**
     * Manage the Active Intention : Stop Attack, Movement and Launch Think Event.
     *
     *  * Set the AI Intention to ACTIVE
     *  * Init cast and attack target
     *  * Stop the actor attack stance.
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Launch the Think Event
     *
     */
    override fun onIntentionActive() {
        // Check if the Intention is not already Active
        if (desire.intention !== CtrlIntention.ACTIVE) {
            // Set the AI Intention to ACTIVE
            changeIntention(CtrlIntention.ACTIVE, null, null)

            // Init cast and attack target
            target = null

            // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
            clientStopMoving(null)

            // Stop the attack stance.
            stopAttackStance()

            // Also enable random animations for this Creature if allowed
            // This is only for mobs - town npcs are handled in their constructor
            if (actor is Attackable)
                (actor as Npc).startRandomAnimationTimer()

            // Launch the Think Event
            onEvtThink()
        }
    }

    /**
     * Manage the Rest Intention. Set the AI Intention to IDLE.
     */
    override fun onIntentionRest() {
        setIntention(CtrlIntention.IDLE)
    }

    /**
     * Manage the Attack Intention : Stop current Attack (if necessary), Start a new Attack and Launch Think Event.
     *
     *  * Set the Intention of this AI to ATTACK
     *  * Set or change the AI attack target
     *  * Start the actor attack stance.
     *  * Launch the Think Event
     *
     */
    override fun onIntentionAttack(target: Creature?) {
        if (target == null) {
            clientActionFailed()
            return
        }

        if (desire.intention === CtrlIntention.REST) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        if (actor.isAllSkillsDisabled || actor.isCastingNow || actor.isAfraid) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        // Check if the Intention is already ATTACK
        if (desire.intention === CtrlIntention.ATTACK) {
            // Check if the AI already targets the Creature
            if (this.target !== target) {
                // Set the AI attack target (change target)
                this.target = target

                stopFollow()

                // Launch the Think Event
                notifyEvent(CtrlEvent.EVT_THINK, null)
            } else
                clientActionFailed() // else client freezes until cancel target
        } else {
            // Set the Intention of this AbstractAI to ATTACK
            changeIntention(CtrlIntention.ATTACK, target, null)

            // Set the AI attack target
            this.target = target

            stopFollow()

            // Launch the Think Event
            notifyEvent(CtrlEvent.EVT_THINK, null)
        }
    }

    /**
     * Launch a spell.
     *
     *  * Set the target
     *  * Set the AI skill used by INTENTION_CAST
     *  * Set the Intention of this AI to CAST
     *  * Launch the Think Event
     *
     */
    override fun onIntentionCast(skill: L2Skill, target: WorldObject?) {
        if (desire.intention === CtrlIntention.REST && skill.isMagic) {
            clientActionFailed()
            actor.setIsCastingNow(false)
            return
        }

        // Set the AI cast target
        this.target = target

        // Set the AI skill used by INTENTION_CAST
        _skill = skill

        // Change the Intention of this AbstractAI to CAST
        changeIntention(CtrlIntention.CAST, skill, target)

        // Launch the Think Event
        notifyEvent(CtrlEvent.EVT_THINK, null)
    }

    /**
     * Launch a movement to a [Location] if conditions are met.
     *
     *  * Set the Intention of this AI to MOVE_TO.
     *  * Move the actor to Location (x,y,z).
     *
     */
    override fun onIntentionMoveTo(loc: Location) {
        if (desire.intention === CtrlIntention.REST) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        if (actor.isAllSkillsDisabled || actor.isCastingNow) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        // Set the Intention of this AbstractAI to MOVE_TO
        changeIntention(CtrlIntention.MOVE_TO, loc, null)

        // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet MoveToLocation (broadcast)
        moveTo(loc.x, loc.y, loc.z)
    }

    /**
     * Follow the [Creature] set as parameter if conditions are met.
     *
     *  * Set the Intention of this AI to FOLLOW.
     *  * Launch a task executed every 1s to calculate the movement on the fly.
     *
     */
    override fun onIntentionFollow(target: Creature) {
        if (desire.intention === CtrlIntention.REST) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        if (actor.isAllSkillsDisabled || actor.isCastingNow) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        if (actor.isMovementDisabled) {
            // Cancel action client side by sending Server->Client packet ActionFailed to the Player actor
            clientActionFailed()
            return
        }

        // Dead actors can`t follow
        if (actor.isDead) {
            clientActionFailed()
            return
        }

        // do not follow yourself
        if (actor === target) {
            clientActionFailed()
            return
        }

        // Set the Intention of this AbstractAI to FOLLOW
        changeIntention(CtrlIntention.FOLLOW, target, null)

        // Create and Launch an AI Follow Task to execute every 1s
        startFollow(target)
    }

    /**
     * Manage the PickUp Intention : Set the pick up target and Launch a Move To Pawn Task (offset=20).
     *
     *  * Set the AI pick up target
     *  * Set the Intention of this AI to PICK_UP
     *  * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
     *
     */
    override fun onIntentionPickUp(`object`: WorldObject) {
        // Actor is resting, return.
        if (desire.intention === CtrlIntention.REST) {
            clientActionFailed()
            return
        }

        // Actor is currently busy casting, return.
        if (actor.isAllSkillsDisabled || actor.isCastingNow || actor.isAttackingNow) {
            clientActionFailed()
            return
        }

        if (`object` is ItemInstance && `object`.location !== ItemInstance.ItemLocation.VOID)
            return

        // Set the Intention of this AbstractAI to PICK_UP
        changeIntention(CtrlIntention.PICK_UP, `object`, null)

        // Set the AI pick up target
        target = `object`

        // Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
        moveToPawn(`object`, 20)
    }

    /**
     * Manage the Interact Intention : Set the interact target and Launch a Move To Pawn Task (offset=60).
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Set the AI interact target
     *  * Set the Intention of this AI to INTERACT
     *  * Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
     *
     */
    override fun onIntentionInteract(`object`: WorldObject?) {}

    override fun onEvtThink() {}

    override fun onEvtAggression(target: Creature?, aggro: Int) {}

    /**
     * Launch actions corresponding to the Event Stunned then onAttacked Event.
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature
     *  * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
     *  * Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
     *
     */
    override fun onEvtStunned(attacker: Creature?) {
        // Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
        actor.broadcastPacket(AutoAttackStop(actor.objectId))
        AttackStanceTaskManager.remove(actor)

        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
        onEvtAttacked(attacker)
    }

    /**
     * Launch actions corresponding to the Event Paralyzed then onAttacked Event.
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature
     *  * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
     *  * Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
     *
     */
    override fun onEvtParalyzed(attacker: Creature?) {
        // Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
        actor.broadcastPacket(AutoAttackStop(actor.objectId))
        AttackStanceTaskManager.remove(actor)

        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Launch actions corresponding to the Event onAttacked (only for L2AttackableAI after the stunning periode)
        onEvtAttacked(attacker)
    }

    /**
     * Launch actions corresponding to the Event Sleeping.
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Break an attack and send Server->Client ActionFailed packet and a System Message to the Creature
     *  * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
     *
     */
    override fun onEvtSleeping(attacker: Creature?) {
        // Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
        actor.broadcastPacket(AutoAttackStop(actor.objectId))
        AttackStanceTaskManager.remove(actor)

        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)
    }

    /**
     * Launch actions corresponding to the Event Rooted.
     *
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Launch actions corresponding to the Event onAttacked
     *
     */
    override fun onEvtRooted(attacker: Creature?) {
        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Launch actions corresponding to the Event onAttacked
        onEvtAttacked(attacker)
    }

    /**
     * Launch actions corresponding to the Event Confused.
     *
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Launch actions corresponding to the Event onAttacked
     *
     */
    override fun onEvtConfused(attacker: Creature?) {
        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Launch actions corresponding to the Event onAttacked
        onEvtAttacked(attacker)
    }

    /**
     * Launch actions corresponding to the Event Muted.
     *
     *  * Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
     *
     */
    override fun onEvtMuted(attacker: Creature?) {
        // Break a cast and send Server->Client ActionFailed packet and a System Message to the Creature
        onEvtAttacked(attacker)
    }

    override fun onEvtEvaded(attacker: Creature?) {
        // do nothing
    }

    /**
     * Launch actions corresponding to the Event ReadyToAct.
     *
     *  * Launch actions corresponding to the Event Think
     *
     */
    override fun onEvtReadyToAct() {
        // Launch actions corresponding to the Event Think
        onEvtThink()
    }

    /**
     * Launch actions corresponding to the Event Arrived.
     *
     *  * If the Intention was MOVE_TO, set the Intention to ACTIVE
     *  * Launch actions corresponding to the Event Think
     *
     */
    override fun onEvtArrived() {
        actor.revalidateZone(true)

        if (actor.moveToNextRoutePoint())
            return

        if (actor is Attackable)
            actor.setIsReturningToSpawnPoint(false)

        clientStoppedMoving()

        // If the Intention was MOVE_TO, set the Intention to ACTIVE
        if (desire.intention === CtrlIntention.MOVE_TO)
            setIntention(CtrlIntention.ACTIVE)

        // Launch actions corresponding to the Event Think
        onEvtThink()
    }

    /**
     * Launch actions corresponding to the Event ArrivedBlocked.
     *
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * If the Intention was MOVE_TO, set the Intention to ACTIVE
     *  * Launch actions corresponding to the Event Think
     *
     */
    override fun onEvtArrivedBlocked(loc: SpawnLocation?) {
        // If the Intention was MOVE_TO, set the Intention to ACTIVE
        if (desire.intention === CtrlIntention.MOVE_TO || desire.intention === CtrlIntention.CAST)
            setIntention(CtrlIntention.ACTIVE)

        // Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(loc)

        // Launch actions corresponding to the Event Think
        onEvtThink()
    }

    /**
     * Launch actions corresponding to the Event Cancel.
     *
     *  * Stop an AI Follow Task
     *  * Launch actions corresponding to the Event Think
     *
     */
    override fun onEvtCancel() {
        actor.abortCast()

        // Stop an AI Follow Task
        stopFollow()

        if (!AttackStanceTaskManager.isInAttackStance(actor))
            actor.broadcastPacket(AutoAttackStop(actor.objectId))

        // Launch actions corresponding to the Event Think
        onEvtThink()
    }

    /**
     * Launch actions corresponding to the Event Dead.
     *
     *  * Stop an AI Follow Task
     *  * Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
     *
     */
    override fun onEvtDead() {
        // Stop an AI Tasks
        stopAITask()

        // Kill the actor client side by sending Server->Client packet AutoAttackStop, StopMove/StopRotation, Die (broadcast)
        clientNotifyDead()

        if (actor !is Playable)
            actor.setWalking()
    }

    /**
     * Launch actions corresponding to the Event Fake Death.
     *
     *  * Stop an AI Follow Task
     *
     */
    override fun onEvtFakeDeath() {
        // Stop an AI Follow Task
        stopFollow()

        // Stop the actor movement and send Server->Client packet StopMove/StopRotation (broadcast)
        clientStopMoving(null)

        // Init AI
        desire.update(CtrlIntention.IDLE, null, null)
        target = null
    }

    override fun onEvtFinishCasting() {
        // do nothing
    }

    protected fun maybeMoveToPosition(worldPosition: Location?, offset: Int): Boolean {
        if (worldPosition == null)
            return false

        if (offset < 0)
            return false // skill radius -1

        if (!actor.isInsideRadius(
                worldPosition.x,
                worldPosition.y,
                (offset + actor.collisionRadius).toInt(),
                false
            )
        ) {
            if (actor.isMovementDisabled)
                return true

            if (this !is PlayerAI && this !is SummonAI)
                actor.setRunning()

            stopFollow()

            var x = actor.x
            var y = actor.y

            val dx = (worldPosition.x - x).toDouble()
            val dy = (worldPosition.y - y).toDouble()

            var dist = Math.sqrt(dx * dx + dy * dy)

            val sin = dy / dist
            val cos = dx / dist

            dist -= (offset - 5).toDouble()

            x += (dist * cos).toInt()
            y += (dist * sin).toInt()

            moveTo(x, y, worldPosition.z)
            return true
        }

        if (followTarget != null)
            stopFollow()

        return false
    }

    /**
     * Manage the Move to Pawn action in function of the distance and of the Interact area.
     *
     *  * Get the distance between the current position of the Creature and the target (x,y)
     *  * If the distance > offset+20, move the actor (by running) to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
     *  * If the distance <= offset+20, Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *
     * @param target The targeted WorldObject
     * @param offset The Interact area radius
     * @return True if a movement must be done
     */
    protected fun maybeMoveToPawn(target: WorldObject?, offset: Int): Boolean {
        var offset = offset
        if (target == null || offset < 0)
        // skill radius -1
            return false

        offset += actor.collisionRadius.toInt()
        if (target is Creature)
            offset += target.collisionRadius.toInt()

        if (!actor.isInsideRadius(target, offset, false, false)) {
            if (followTarget != null) {
                // allow larger hit range when the target is moving (check is run only once per second)
                if (!actor.isInsideRadius(target, offset + 100, false, false))
                    return true

                stopFollow()
                return false
            }

            if (actor.isMovementDisabled) {
                if (desire.intention === CtrlIntention.ATTACK) {
                    setIntention(CtrlIntention.IDLE)
                    clientActionFailed()
                }

                return true
            }

            // If not running, set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
            if (this !is PlayerAI && this !is SummonAI)
                actor.setRunning()

            stopFollow()

            if (target is Creature && target !is Door) {
                if (target.isMoving)
                    offset -= 30

                if (offset < 5)
                    offset = 5

                startFollow(target as Creature?, offset)
            } else {
                // Move the actor to Pawn server side AND client side by sending Server->Client packet MoveToPawn (broadcast)
                moveToPawn(target, offset)
            }
            return true
        }

        if (followTarget != null)
            stopFollow()

        return false
    }

    /**
     * Modify current Intention and actions if the target is lost or dead.
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Set the Intention of this AbstractAI to ACTIVE
     *
     * @param target The targeted WorldObject
     * @return True if the target is lost or dead (false if fakedeath)
     */
    protected fun checkTargetLostOrDead(target: Creature?): Boolean {
        if (target == null || target.isAlikeDead) {
            if (target is Player && target.isFakeDeath) {
                target.stopFakeDeath(true)
                return false
            }

            // Set the Intention of this AbstractAI to ACTIVE
            setIntention(CtrlIntention.ACTIVE)
            return true
        }
        return false
    }

    /**
     * Modify current Intention and actions if the target is lost.
     *
     *  * Stop the actor auto-attack client side by sending Server->Client packet AutoAttackStop (broadcast)
     *  * Stop the actor movement server side AND client side by sending Server->Client packet StopMove/StopRotation (broadcast)
     *  * Set the Intention of this AbstractAI to ACTIVE
     *
     * @param target The targeted WorldObject
     * @return True if the target is lost
     */
    protected fun checkTargetLost(target: WorldObject?): Boolean {
        if (target is Player) {
            val victim = target as Player?
            if (victim!!.isFakeDeath) {
                victim.stopFakeDeath(true)
                return false
            }
        }

        if (target == null) {
            // Set the Intention of this AbstractAI to ACTIVE
            setIntention(CtrlIntention.ACTIVE)
            return true
        }
        return false
    }

    fun canAura(sk: L2Skill): Boolean {
        if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
            for (target in actor.getKnownTypeInRadius(Creature::class.java, sk.skillRadius)) {
                if (target === this.target)
                    return true
            }
        }
        return false
    }

    fun canAOE(sk: L2Skill): Boolean {
        if (sk.skillType !== L2SkillType.NEGATE || sk.skillType !== L2SkillType.CANCEL) {
            if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                var cancast = true
                for (target in actor.getKnownTypeInRadius(Creature::class.java, sk.skillRadius)) {
                    if (!GeoEngine.canSeeTarget(actor, target))
                        continue

                    if (target is Attackable && !actor.isConfused)
                        continue

                    if (target.getFirstEffect(sk) != null)
                        cancast = false
                }

                if (cancast)
                    return true
            } else if (sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) {
                var cancast = true
                for (target in (target as Creature).getKnownTypeInRadius(Creature::class.java, sk.skillRadius)) {
                    if (!GeoEngine.canSeeTarget(actor, target))
                        continue

                    if (target is Attackable && !actor.isConfused)
                        continue

                    val effects = target.allEffects
                    if (effects.isNotEmpty())
                        cancast = true
                }
                if (cancast)
                    return true
            }
        } else {
            if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                var cancast = false
                for (target in actor.getKnownTypeInRadius(Creature::class.java, sk.skillRadius)) {
                    if (!GeoEngine.canSeeTarget(actor, target))
                        continue

                    if (target is Attackable && !actor.isConfused)
                        continue

                    val effects = target.allEffects
                    if (effects.isNotEmpty())
                        cancast = true
                }
                if (cancast)
                    return true
            } else if (sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) {
                var cancast = true
                for (target in (target as Creature).getKnownTypeInRadius(Creature::class.java, sk.skillRadius)) {
                    if (!GeoEngine.canSeeTarget(actor, target))
                        continue

                    if (target is Attackable && !actor.isConfused)
                        continue

                    if (target.getFirstEffect(sk) != null)
                        cancast = false
                }

                if (cancast)
                    return true
            }
        }
        return false
    }

    fun canParty(sk: L2Skill): Boolean {
        if (sk.targetType != L2Skill.SkillTargetType.TARGET_PARTY)
            return false

        var count = 0
        var ccount = 0

        val actorClans = (actor as Npc).template.clans
        for (target in actor.getKnownTypeInRadius(Attackable::class.java, sk.skillRadius)) {
            if (!GeoEngine.canSeeTarget(actor, target))
                continue

            if (!ArraysUtil.contains(actorClans, target.template.clans))
                continue

            count++

            if (target.getFirstEffect(sk) != null)
                ccount++
        }

        return ccount < count

    }
}