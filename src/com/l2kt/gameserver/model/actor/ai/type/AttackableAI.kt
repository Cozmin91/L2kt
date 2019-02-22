package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.*
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.*
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate.AIType
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.concurrent.Future

internal open class AttackableAI(attackable: Attackable) : CreatureAI(attackable), Runnable {

    /** The L2Attackable AI task executed every 1s (call onEvtThink method)  */
    protected var _aiTask: Future<*>? = null

    /** The delay after wich the attacked is stopped  */
    protected var _attackTimeout: Long = 0

    /** The L2Attackable aggro counter  */
    protected var _globalAggro: Int = 0

    /** The flag used to indicate that a thinking action is in progress ; prevent recursive thinking  */
    protected var _thinking: Boolean = false

    private val activeChar: Attackable
        get() = actor as Attackable

    init {

        _attackTimeout = java.lang.Long.MAX_VALUE
        _globalAggro = -10 // 10 seconds timeout of ATTACK after respawn
    }

    override fun run() {
        // Launch actions corresponding to the Event Think
        onEvtThink()
    }

    /**
     * <B><U> Actor is a L2GuardInstance</U> :</B>
     *
     *  * The target isn't a Folk or a Door
     *  * The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)
     *  * The target is in the actor Aggro range and is at the same height
     *  * The Player target has karma (=PK)
     *  * The L2MonsterInstance target is aggressive
     *
     * <B><U> Actor is a L2SiegeGuardInstance</U> :</B>
     *
     *  * The target isn't a Folk or a Door
     *  * The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)
     *  * The target is in the actor Aggro range and is at the same height
     *  * A siege is in progress
     *  * The Player target isn't a Defender
     *
     * <B><U> Actor is a L2FriendlyMobInstance</U> :</B>
     *
     *  * The target isn't a Folk, a Door or another L2Npc
     *  * The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)
     *  * The target is in the actor Aggro range and is at the same height
     *  * The Player target has karma (=PK)
     *
     * <B><U> Actor is a L2MonsterInstance</U> :</B>
     *
     *  * The target isn't a Folk, a Door or another L2Npc
     *  * The target isn't dead, isn't invulnerable, isn't in silent moving mode AND too far (>100)
     *  * The target is in the actor Aggro range and is at the same height
     *  * The actor is Aggressive
     *
     * @param target The targeted Creature
     * @return True if the target is autoattackable (depends on the actor type).
     */
    protected open fun autoAttackCondition(target: Creature?): Boolean {
        // Check if the target isn't null, a Door or dead.
        if (target == null || target is Door || target.isAlikeDead)
            return false

        val me = activeChar

        if (target is Playable) {
            // Check if target is in the Aggro range
            if (!me.isInsideRadius(target, me.template.aggroRange, true, false))
                return false

            // Check if the AI isn't a Raid Boss, can See Silent Moving players and the target isn't in silent move mode
            if (!me.isRaidRelated && !me.canSeeThroughSilentMove() && target.isSilentMoving)
                return false

            // Check if the target is a Player
            val targetPlayer = target.actingPlayer
            if (targetPlayer != null) {
                // GM checks ; check if the target is invisible or got access level
                if (targetPlayer.isGM && (targetPlayer.appearance.invisible || !targetPlayer.accessLevel.canTakeAggro))
                    return false

                // Check if player is an allied Varka.
                if (ArraysUtil.contains(me.template.clans, "varka_silenos_clan") && targetPlayer.isAlliedWithVarka)
                    return false

                // Check if player is an allied Ketra.
                if (ArraysUtil.contains(me.template.clans, "ketra_orc_clan") && targetPlayer.isAlliedWithKetra)
                    return false

                // check if the target is within the grace period for JUST getting up from fake death
                if (targetPlayer.isRecentFakeDeath)
                    return false

                if (me is RiftInvader && targetPlayer.isInParty && targetPlayer.party!!.isInDimensionalRift && !targetPlayer.party!!.dimensionalRift!!.isInCurrentRoomZone(
                        me
                    )
                )
                    return false
            }
        }

        // Check if the actor is a L2GuardInstance
        if (me is Guard) {
            // Check if the Player target has karma (=PK)
            if (target is Player && target.karma > 0)
                return GeoEngine.canSeeTarget(me, target)

            // Check if the L2MonsterInstance target is aggressive
            return if (target is Monster && Config.GUARD_ATTACK_AGGRO_MOB) target.isAggressive && GeoEngine.canSeeTarget(
                me,
                target
            ) else false

        } else if (me is FriendlyMonster) {
            // Check if the Player target has karma (=PK)
            return if (target is Player && target.karma > 0) GeoEngine.canSeeTarget(me, target) else false // Los Check

        } else {
            if (target is Attackable && me.isConfused)
                return GeoEngine.canSeeTarget(me, target)

            if (target is Npc)
                return false

            // depending on config, do not allow mobs to attack _new_ players in peacezones,
            // unless they are already following those players from outside the peacezone.
            return if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE)) false else me.isAggressive && GeoEngine.canSeeTarget(
                me,
                target
            )

            // Check if the actor is Aggressive
        }// The actor is a L2Npc
        // The actor is a L2FriendlyMobInstance
    }

    override fun stopAITask() {
        if (_aiTask != null) {
            _aiTask!!.cancel(false)
            _aiTask = null
        }
        super.stopAITask()
    }

    /**
     * Set the Intention of this CreatureAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, IDLE will be change in ACTIVE</B></FONT><BR></BR>
     * <BR></BR>
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     */
    @Synchronized
    override fun changeIntention(intention: CtrlIntention, arg0: Any?, arg1: Any?) {
        var intention = intention
        if (intention === CtrlIntention.IDLE || intention === CtrlIntention.ACTIVE) {
            // Check if actor is not dead
            val npc = activeChar
            if (!npc.isAlikeDead) {
                // If no players are around, set the Intention to ACTIVE
                if (!npc.getKnownType(Player::class.java).isEmpty())
                    intention = CtrlIntention.ACTIVE
                else {
                    if (npc.spawn != null) {
                        val range = Config.MAX_DRIFT_RANGE
                        if (!npc.isInsideRadius(
                                npc.spawn.locX,
                                npc.spawn.locY,
                                npc.spawn.locZ,
                                range + range,
                                true,
                                false
                            )
                        )
                            intention = CtrlIntention.ACTIVE
                    }
                }
            }

            if (intention === CtrlIntention.IDLE) {
                // Set the Intention of this L2AttackableAI to IDLE
                super.changeIntention(CtrlIntention.IDLE, null, null)

                // Stop AI task and detach AI from NPC
                stopAITask()

                // Cancel the AI
                actor.detachAI()
                return
            }
        }

        // Set the Intention of this L2AttackableAI to intention
        super.changeIntention(intention, arg0, arg1)

        // If not idle - create an AI task (schedule onEvtThink repeatedly)
        if (_aiTask == null)
            _aiTask = ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    /**
     * Manage the Attack Intention :
     *
     *  * Stop current Attack (if necessary).
     *  * Calculate attack timeout.
     *  * Start a new Attack and Launch Think Event.
     *
     * @param target The Creature to attack
     */
    override fun onIntentionAttack(target: Creature?) {
        // Calculate the attack timeout
        _attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT

        // Check buff.
        checkBuffAndSetBackTarget(target)

        // Manage the attack intention : stop current attack (if necessary), start a new attack and launch Think event.
        super.onIntentionAttack(target)
    }

    private fun thinkCast() {
        if (checkTargetLost(target)) {
            target = null
            return
        }

        if (maybeMoveToPawn(target, _skill?.castRange ?: -1))
            return

        clientStopMoving(null)
        setIntention(CtrlIntention.ACTIVE)
        actor.doCast(_skill)
    }

    /**
     * Manage AI standard thinks of a L2Attackable (called by onEvtThink).
     *
     *  * Update every 1s the _globalAggro counter to come close to 0
     *  * If the actor is Aggressive and can attack, add all autoAttackable Creature in its Aggro Range to its _aggroList, chose a target and order to attack it
     *  * If the actor is a L2GuardInstance that can't attack, order to it to return to its home location
     *  * If the actor is a L2MonsterInstance that can't attack, order to it to random walk (1/100)
     *
     */
    protected open fun thinkActive() {
        val npc = activeChar

        // Update every 1s the _globalAggro counter to come close to 0
        if (_globalAggro != 0) {
            if (_globalAggro < 0)
                _globalAggro++
            else
                _globalAggro--
        }

        // Add all autoAttackable Creature in L2Attackable Aggro Range to its _aggroList with 0 damage and 1 hate
        // A L2Attackable isn't aggressive during 10s after its spawn because _globalAggro is set to -10
        if (_globalAggro >= 0) {
            // Get all visible objects inside its Aggro Range
            for (target in npc.getKnownType(Creature::class.java)) {
                // Check to see if this is a festival mob spawn. If it is, then check to see if the aggro trigger is a festival participant...if so, move to attack it.
                if (npc is FestivalMonster && target is Player) {
                    if (!target.isFestivalParticipant)
                        continue
                }

                // For each Creature check if the target is autoattackable
                if (autoAttackCondition(target))
                // check aggression
                {
                    // Add the attacker to the L2Attackable _aggroList
                    if (npc.getHating(target) == 0)
                        npc.addDamageHate(target, 0, 0)
                }
            }

            if (!npc.isCoreAIDisabled) {
                // Chose a target from its aggroList and order to attack the target
                val hated = (if (npc.isConfused) target else npc.mostHated) as Creature?
                if (hated != null) {
                    // Get the hate level of the L2Attackable against this Creature target contained in _aggroList
                    if (npc.getHating(hated) + _globalAggro > 0) {
                        // Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
                        npc.setRunning()

                        // Set the AI Intention to ATTACK
                        setIntention(CtrlIntention.ATTACK, hated)
                    }
                    return
                }
            }
        }

        // If this is a festival monster, then it remains in the same location.
        if (npc is FestivalMonster)
            return

        // Check buffs.
        if (checkBuffAndSetBackTarget(actor.target))
            return

        // Minions following leader
        val master = npc.getMaster()
        if (master != null && !master.isAlikeDead) {
            if (!npc.isCastingNow) {
                val offset = (100.0 + npc.collisionRadius + master.collisionRadius).toInt()
                val minRadius = (master.collisionRadius + 30).toInt()

                if (master.isRunning)
                    npc.setRunning()
                else
                    npc.setWalking()

                if (npc.getPlanDistanceSq(master.x, master.y) > offset * offset) {
                    var x1 = Rnd[minRadius * 2, offset * 2] // x
                    var y1 = Rnd[x1, offset * 2] // distance

                    y1 = Math.sqrt((y1 * y1 - x1 * x1).toDouble()).toInt() // y

                    if (x1 > offset + minRadius)
                        x1 = master.x + x1 - offset
                    else
                        x1 = master.x - x1 + minRadius

                    if (y1 > offset + minRadius)
                        y1 = master.y + y1 - offset
                    else
                        y1 = master.y - y1 + minRadius

                    // Move the actor to Location (x,y,z) server side AND client side by sending Server->Client packet CharMoveToLocation (broadcast)
                    moveTo(x1, y1, master.z)
                    return
                }
            }
        } else {
            // Return to home if too far.
            if (npc.returnHome())
                return

            // Random walk otherwise.
            if (npc.spawn != null && !npc.isNoRndWalk && Rnd[RANDOM_WALK_RATE] == 0) {
                var x1 = npc.spawn.locX
                var y1 = npc.spawn.locY
                var z1 = npc.spawn.locZ

                val range = Config.MAX_DRIFT_RANGE

                x1 = Rnd[range * 2] // x
                y1 = Rnd[x1, range * 2] // distance
                y1 = Math.sqrt((y1 * y1 - x1 * x1).toDouble()).toInt() // y
                x1 += npc.spawn.locX - range
                y1 += npc.spawn.locY - range
                z1 = npc.z

                // Move the actor to Location (x,y,z)
                moveTo(x1, y1, z1)
            }
        }
    }

    /**
     * Manage AI attack thoughts of a L2Attackable (called by onEvtThink).
     *
     *  * Update the attack timeout if actor is running.
     *  * If target is dead or timeout is expired, stop this attack and set the Intention to ACTIVE.
     *  * Call all WorldObject of its Faction inside the Faction Range.
     *  * Choose a target and order to attack it with magic skill or physical attack.
     *
     */
    protected open fun thinkAttack() {
        val npc = activeChar
        if (npc.isCastingNow)
            return

        // Pickup most hated character.
        var attackTarget = npc.mostHated

        // If target doesn't exist, is too far or if timeout is expired.
        if (attackTarget == null || _attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(
                npc,
                attackTarget,
                true
            ) > 2000
        ) {
            // Stop hating this target after the attack timeout or if target is dead
            npc.stopHating(attackTarget)
            setIntention(CtrlIntention.ACTIVE)
            npc.setWalking()
            return
        }

        // Corpse AIs, as AI scripts, are stopped here.
        if (npc.isCoreAIDisabled)
            return

        target = attackTarget
        npc.target = attackTarget

        /**
         * COMMON INFORMATIONS<br></br>
         * Used for range and distance check.
         */

        val actorCollision = npc.collisionRadius.toInt()
        val combinedCollision = (actorCollision + attackTarget.collisionRadius).toInt()
        val dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.x, attackTarget.y))

        var range = combinedCollision
        if (attackTarget.isMoving)
            range += 15

        if (npc.isMoving)
            range += 15

        /**
         * CAST CHECK<br></br>
         * The mob succeeds a skill check ; make all possible checks to define the skill to launch. If nothing is found, go in MELEE CHECK.<br></br>
         * It will check skills arrays in that order :
         *
         *  * suicide skill at 15% max HPs
         *  * buff skill if such effect isn't existing
         *  * heal skill if self or ally is under 75% HPs (priority to others healers and mages)
         *  * debuff skill if such effect isn't existing
         *  * damage skill, in that order : short range and long range
         *
         */

        if (willCastASpell()) {
            // This list is used in order to avoid multiple calls on skills lists. Tests are made one after the other, and content is replaced when needed.
            var defaultList: List<L2Skill>

            // -------------------------------------------------------------------------------
            // Suicide possibility if HPs are < 15%.
            defaultList = npc.template.getSkills(SkillType.SUICIDE)
            if (!defaultList.isEmpty() && npc.currentHp / npc.maxHp < 0.15) {
                val skill = Rnd[defaultList]
                if (cast(skill, dist, range + skill!!.skillRadius))
                    return
            }

            // -------------------------------------------------------------------------------
            // Heal
            defaultList = npc.template.getSkills(SkillType.HEAL)
            if (!defaultList.isEmpty()) {
                // First priority is to heal the master.
                val master = npc.getMaster()
                if (master != null && !master.isDead && master.currentHp / master.maxHp < 0.75) {
                    for (sk in defaultList) {
                        if (sk.targetType == L2Skill.SkillTargetType.TARGET_SELF)
                            continue

                        if (!checkSkillCastConditions(sk))
                            continue

                        val overallRange =
                            (sk.castRange.toDouble() + actorCollision.toDouble() + master.collisionRadius).toInt()
                        if (!MathUtil.checkIfInRange(
                                overallRange,
                                npc,
                                master,
                                false
                            ) && sk.targetType != L2Skill.SkillTargetType.TARGET_PARTY && !npc.isMovementDisabled
                        ) {
                            moveToPawn(master, overallRange)
                            return
                        }

                        if (GeoEngine.canSeeTarget(npc, master)) {
                            clientStopMoving(null)
                            npc.target = master
                            npc.doCast(sk)
                            return
                        }
                    }
                }

                // Second priority is to heal himself.
                if (npc.currentHp / npc.maxHp < 0.75) {
                    for (sk in defaultList) {
                        if (!checkSkillCastConditions(sk))
                            continue

                        clientStopMoving(null)
                        npc.target = npc
                        npc.doCast(sk)
                        return
                    }
                }

                for (sk in defaultList) {
                    if (!checkSkillCastConditions(sk))
                        continue

                    if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                        val actorClans = npc.template.clans
                        for (obj in npc.getKnownTypeInRadius(Attackable::class.java, sk.castRange + actorCollision)) {
                            if (obj.isDead)
                                continue

                            if (!ArraysUtil.contains(actorClans, obj.template.clans))
                                continue

                            if (obj.currentHp / obj.maxHp < 0.75) {
                                if (GeoEngine.canSeeTarget(npc, obj)) {
                                    clientStopMoving(null)
                                    npc.target = obj
                                    npc.doCast(sk)
                                    return
                                }
                            }
                        }

                        if (sk.targetType == L2Skill.SkillTargetType.TARGET_PARTY) {
                            clientStopMoving(null)
                            npc.doCast(sk)
                            return
                        }
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // Buff
            defaultList = npc.template.getSkills(SkillType.BUFF)
            if (!defaultList.isEmpty()) {
                for (sk in defaultList) {
                    if (!checkSkillCastConditions(sk))
                        continue

                    if (npc.getFirstEffect(sk) == null) {
                        clientStopMoving(null)

                        npc.target = npc
                        npc.doCast(sk)
                        npc.target = attackTarget
                        return
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // Debuff - 10% luck to get debuffed.
            defaultList = npc.template.getSkills(SkillType.DEBUFF)
            if (Rnd[100] < 10 && !defaultList.isEmpty()) {
                for (sk in defaultList) {
                    if (!checkSkillCastConditions(sk) || sk.castRange.toDouble() + npc.collisionRadius + attackTarget.collisionRadius <= dist && !canAura(
                            sk
                        )
                    )
                        continue

                    if (!GeoEngine.canSeeTarget(npc, attackTarget))
                        continue

                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null)
                        npc.doCast(sk)
                        return
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // General attack skill - short range is checked, then long range.
            defaultList = npc.template.getSkills(SkillType.SHORT_RANGE)
            if (!defaultList.isEmpty() && dist <= 150) {
                val skill = Rnd[defaultList]
                if (cast(skill, dist, skill!!.castRange))
                    return
            } else {
                defaultList = npc.template.getSkills(SkillType.LONG_RANGE)
                if (!defaultList.isEmpty() && dist > 150) {
                    val skill = Rnd[defaultList]
                    if (cast(skill, dist, skill!!.castRange))
                        return
                }
            }
        }

        /**
         * MELEE CHECK<br></br>
         * The mob failed a skill check ; make him flee if AI authorizes it, else melee attack.
         */

        // The range takes now in consideration physical attack range.
        range += npc.physicalAttackRange

        if (npc.isMovementDisabled) {
            // If distance is too big, choose another target.
            if (dist > range)
                attackTarget = targetReconsider(range, true)

            // Any AI type, even healer or mage, will try to melee attack if it can't do anything else (desesperate situation).
            if (attackTarget != null)
                actor.doAttack(attackTarget)

            return
        }

        /**
         * MOVE AROUND CHECK<br></br>
         * In case many mobs are trying to hit from same place, move a bit, circling around the target
         */

        if (Rnd[100] <= 3) {
            for (nearby in npc.getKnownTypeInRadius(Attackable::class.java, actorCollision)) {
                if (nearby !== attackTarget) {
                    var newX = combinedCollision + Rnd[40]
                    if (Rnd.nextBoolean())
                        newX = attackTarget.x + newX
                    else
                        newX = attackTarget.x - newX

                    var newY = combinedCollision + Rnd[40]
                    if (Rnd.nextBoolean())
                        newY = attackTarget.y + newY
                    else
                        newY = attackTarget.y - newY

                    if (!npc.isInsideRadius(newX, newY, actorCollision, false)) {
                        val newZ = npc.z + 30
                        if (GeoEngine.canMoveToTarget(npc.x, npc.y, npc.z, newX, newY, newZ))
                            moveTo(newX, newY, newZ)
                    }
                    return
                }
            }
        }

        /**
         * FLEE CHECK<br></br>
         * Test the flee possibility. Archers got 25% chance to flee.
         */

        if (npc.template.aiType == AIType.ARCHER && dist <= 60 + combinedCollision && Rnd[4] > 1) {
            val posX = npc.x + if (attackTarget.x < npc.x) 300 else -300
            val posY = npc.y + if (attackTarget.y < npc.y) 300 else -300
            val posZ = npc.z + 30

            if (GeoEngine.canMoveToTarget(npc.x, npc.y, npc.z, posX, posY, posZ)) {
                setIntention(CtrlIntention.MOVE_TO, Location(posX, posY, posZ))
                return
            }
        }

        /**
         * BASIC MELEE ATTACK
         */

        if (dist > range || !GeoEngine.canSeeTarget(npc, attackTarget)) {
            if (attackTarget.isMoving)
                range -= 30

            if (range < 5)
                range = 5

            moveToPawn(attackTarget, range)
            return
        }

        actor.doAttack(target as Creature)
    }

    protected fun cast(sk: L2Skill?, distance: Double, range: Int): Boolean {
        if (sk == null)
            return false

        val caster = activeChar

        if (caster.isCastingNow && !sk.isSimultaneousCast)
            return false

        if (!checkSkillCastConditions(sk))
            return false

        val attackTarget = target as Creature? ?: return false

        when (sk.skillType) {
            L2SkillType.BUFF -> {
                if (caster.getFirstEffect(sk) == null) {
                    clientStopMoving(null)
                    caster.target = caster
                    caster.doCast(sk)
                    return true
                }

                // ----------------------------------------
                // If actor already have buff, start looking at others same faction mob to cast
                if (sk.targetType == L2Skill.SkillTargetType.TARGET_SELF)
                    return false

                if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.target = target
                        caster.doCast(sk)
                        caster.target = attackTarget
                        return true
                    }
                }

                if (canParty(sk)) {
                    clientStopMoving(null)
                    caster.target = caster
                    caster.doCast(sk)
                    caster.target = attackTarget
                    return true
                }
            }

            L2SkillType.HEAL, L2SkillType.HOT, L2SkillType.HEAL_PERCENT, L2SkillType.HEAL_STATIC, L2SkillType.BALANCE_LIFE -> {
                // Minion case.
                if (sk.targetType != L2Skill.SkillTargetType.TARGET_SELF) {
                    val master = caster.getMaster()
                    if (master != null && !master.isDead && Rnd[100] > master.currentHp / master.maxHp * 100) {
                        val overallRange =
                            (sk.castRange.toDouble() + caster.collisionRadius + master.collisionRadius).toInt()
                        if (!MathUtil.checkIfInRange(
                                overallRange,
                                caster,
                                master,
                                false
                            ) && sk.targetType != L2Skill.SkillTargetType.TARGET_PARTY && !caster.isMovementDisabled
                        )
                            moveToPawn(master, overallRange)

                        if (GeoEngine.canSeeTarget(caster, master)) {
                            clientStopMoving(null)
                            caster.target = master
                            caster.doCast(sk)
                            return true
                        }
                    }
                }

                // Personal case.
                var percentage = caster.currentHp / caster.maxHp * 100
                if (Rnd[100] < (100 - percentage) / 3) {
                    clientStopMoving(null)
                    caster.target = caster
                    caster.doCast(sk)
                    return true
                }

                if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    for (obj in caster.getKnownTypeInRadius(
                        Attackable::class.java,
                        (sk.castRange + caster.collisionRadius).toInt()
                    )) {
                        if (obj.isDead)
                            continue

                        if (!ArraysUtil.contains(caster.template.clans, obj.template.clans))
                            continue

                        percentage = obj.currentHp / obj.maxHp * 100
                        if (Rnd[100] < (100 - percentage) / 10) {
                            if (GeoEngine.canSeeTarget(caster, obj)) {
                                clientStopMoving(null)
                                caster.target = obj
                                caster.doCast(sk)
                                return true
                            }
                        }
                    }
                }

                if (sk.targetType == L2Skill.SkillTargetType.TARGET_PARTY) {
                    for (obj in caster.getKnownTypeInRadius(
                        Attackable::class.java,
                        (sk.skillRadius + caster.collisionRadius).toInt()
                    )) {
                        if (!ArraysUtil.contains(caster.template.clans, obj.template.clans))
                            continue

                        if (obj.currentHp < obj.maxHp && Rnd[100] <= 20) {
                            clientStopMoving(null)
                            caster.target = caster
                            caster.doCast(sk)
                            return true
                        }
                    }
                }
            }

            L2SkillType.DEBUFF, L2SkillType.POISON, L2SkillType.DOT, L2SkillType.MDOT, L2SkillType.BLEED -> {
                if (GeoEngine.canSeeTarget(
                        caster,
                        attackTarget
                    ) && !canAOE(sk) && !attackTarget.isDead && distance <= range
                ) {
                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (canAOE(sk)) {
                    if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }

                    if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                }
            }

            L2SkillType.SLEEP -> {
                if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    if (!attackTarget.isDead && distance <= range) {
                        if (distance > range || attackTarget.isMoving) {
                            if (attackTarget.getFirstEffect(sk) == null) {
                                clientStopMoving(null)
                                caster.doCast(sk)
                                return true
                            }
                        }
                    }

                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (canAOE(sk)) {
                    if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }

                    if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                }
            }

            L2SkillType.ROOT, L2SkillType.STUN, L2SkillType.PARALYZE -> {
                if (GeoEngine.canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range) {
                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (canAOE(sk)) {
                    if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    } else if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                }
            }

            L2SkillType.MUTE, L2SkillType.FEAR -> {
                if (GeoEngine.canSeeTarget(caster, attackTarget) && !canAOE(sk) && distance <= range) {
                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (canAOE(sk)) {
                    if (sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }

                    if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                } else if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                }
            }

            L2SkillType.CANCEL, L2SkillType.NEGATE -> {
                // decrease cancel probability
                if (Rnd[50] != 0)
                    return true

                if (sk.targetType == L2Skill.SkillTargetType.TARGET_ONE) {
                    if (attackTarget.getFirstEffect(L2EffectType.BUFF) != null && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }

                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.target = target
                        caster.doCast(sk)
                        caster.target = attackTarget
                        return true
                    }
                } else if (canAOE(sk)) {
                    if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AURA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AURA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        )
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    } else if ((sk.targetType == L2Skill.SkillTargetType.TARGET_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_BEHIND_AREA || sk.targetType == L2Skill.SkillTargetType.TARGET_FRONT_AREA) && GeoEngine.canSeeTarget(
                            caster,
                            attackTarget
                        ) && !attackTarget.isDead && distance <= range
                    ) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }
                }
            }

            else -> {
                if (!canAura(sk)) {
                    if (GeoEngine.canSeeTarget(caster, attackTarget) && !attackTarget.isDead && distance <= range) {
                        clientStopMoving(null)
                        caster.doCast(sk)
                        return true
                    }

                    val target = targetReconsider(sk.castRange, true)
                    if (target != null) {
                        clientStopMoving(null)
                        caster.target = target
                        caster.doCast(sk)
                        caster.target = attackTarget
                        return true
                    }
                } else {
                    clientStopMoving(null)
                    caster.doCast(sk)
                    return true
                }
            }
        }

        return false
    }

    /**
     * @param skill the skill to check.
     * @return `true` if the skill is available for casting `false` otherwise.
     */
    protected fun checkSkillCastConditions(skill: L2Skill): Boolean {
        // Not enough MP.
        if (skill.mpConsume >= activeChar.currentMp)
            return false

        // Character is in "skill disabled" mode.
        if (activeChar.isSkillDisabled(skill))
            return false

        // Is a magic skill and character is magically muted or is a physical skill and character is physically muted.
        return if (skill.isMagic && activeChar.isMuted || activeChar.isPhysicalMuted) false else true

    }

    /**
     * This method checks if the actor will cast a skill or not.
     * @return true if the actor will cast a spell, false otherwise.
     */
    protected fun willCastASpell(): Boolean {
        when (activeChar.template.aiType) {
            NpcTemplate.AIType.HEALER, NpcTemplate.AIType.MAGE -> return !activeChar.isMuted

            else -> if (activeChar.isPhysicalMuted)
                return false
        }
        return Rnd[100] < 10
    }

    /**
     * Method used when the actor can't attack his current target (immobilize state, for exemple).
     *
     *  * If the actor got an hate list, pickup a new target from it.
     *  * If the actor didn't find a target on his hate list, check if he is aggro type and pickup a new target using his knownlist.
     *
     * @param range The range to check (skill range for skill ; physical range for melee).
     * @param rangeCheck That boolean is used to see if a check based on the distance must be made (skill check).
     * @return The new Creature victim.
     */
    protected open fun targetReconsider(range: Int, rangeCheck: Boolean): Creature? {
        val actor = activeChar

        // Verify first if aggro list is empty, if not search a victim following his aggro position.
        if (!actor.aggroList.isEmpty()) {
            // Store aggro value && most hated, in order to add it to the random target we will choose.
            val previousMostHated = actor.mostHated
            val aggroMostHated = actor.getHating(previousMostHated)

            for (obj in actor.hateList) {
                if (!autoAttackCondition(obj))
                    continue

                if (rangeCheck) {
                    // Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
                    var dist = Math.sqrt(actor.getPlanDistanceSq(obj.x, obj.y)) - obj.collisionRadius
                    if (actor.isMoving)
                        dist -= 15.0

                    if (obj.isMoving)
                        dist -= 15.0

                    if (dist > range)
                        continue
                }

                // Stop to hate the most hated.
                actor.stopHating(previousMostHated)

                // Add previous most hated aggro to that new victim.
                actor.addDamageHate(obj, 0, if (aggroMostHated > 0) aggroMostHated else 2000)
                return obj
            }
        }

        // If hate list gave nothing, then verify first if the actor is aggressive, and then pickup a victim from his knownlist.
        if (actor.isAggressive) {
            for (target in actor.getKnownTypeInRadius(Creature::class.java, actor.template.aggroRange)) {
                if (!autoAttackCondition(target))
                    continue

                if (rangeCheck) {
                    // Verify the distance, -15 if the victim is moving, -15 if the npc is moving.
                    var dist = Math.sqrt(actor.getPlanDistanceSq(target.x, target.y)) - target.collisionRadius
                    if (actor.isMoving)
                        dist -= 15.0

                    if (target.isMoving)
                        dist -= 15.0

                    if (dist > range)
                        continue
                }

                // Only 1 aggro, as the hate list is supposed to be cleaned. Simulate an aggro range entrance.
                actor.addDamageHate(target, 0, 1)
                return target
            }
        }

        // Return null if no new victim has been found.
        return null
    }

    /**
     * Method used for chaotic mode (RBs / GBs and their minions).
     */
    fun aggroReconsider() {
        val actor = activeChar

        // Don't bother with aggro lists lower or equal to 1.
        if (actor.hateList.size <= 1)
            return

        // Choose a new victim, and make checks to see if it fits.
        val mostHated = actor.mostHated
        val victim =
            Rnd[actor.hateList.filter { v -> autoAttackCondition(v) }]

        if (victim != null && mostHated !== victim) {
            // Add most hated aggro to the victim aggro.
            actor.addDamageHate(victim, 0, actor.getHating(mostHated))
            setIntention(CtrlIntention.ATTACK, victim)
        }
    }

    /**
     * Manage AI thinking actions of a L2Attackable.
     */
    override fun onEvtThink() {
        // Check if the thinking action is already in progress.
        if (_thinking || actor.isAllSkillsDisabled)
            return

        // Start thinking action.
        _thinking = true

        try {
            // Manage AI thoughts.
            when (desire.intention) {
                CtrlIntention.ACTIVE -> thinkActive()
                CtrlIntention.ATTACK -> thinkAttack()
                CtrlIntention.CAST -> thinkCast()
            }
        } finally {
            // Stop thinking action.
            _thinking = false
        }
    }

    /**
     * Launch actions corresponding to the Event Attacked.
     *
     *  * Init the attack : Calculate the attack timeout, Set the _globalAggro to 0, Add the attacker to the actor _aggroList
     *  * Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
     *  * Set the Intention to ATTACK
     *
     * @param attacker The Creature that attacks the actor
     */
    override fun onEvtAttacked(attacker: Creature?) {
        val me = activeChar

        // Calculate the attack timeout
        _attackTimeout = System.currentTimeMillis() + MAX_ATTACK_TIMEOUT

        // Set the _globalAggro to 0 to permit attack even just after spawn
        if (_globalAggro < 0)
            _globalAggro = 0

        // Add the attacker to the _aggroList of the actor
        me.addDamageHate(attacker, 0, 1)

        // Set the Intention to ATTACK and make the character running, but only if the AI isn't disabled.
        if (!me.isCoreAIDisabled && (desire.intention !== CtrlIntention.ATTACK || me.mostHated !== target)) {
            me.setRunning()

            setIntention(CtrlIntention.ATTACK, attacker)
        }

        if (me is Monster) {
            var master: Monster? = me

            if (master!!.hasMinions())
                master.minionList.onAssist(me, attacker)
            else {
                master = master.getMaster()
                if (master != null && master.hasMinions())
                    master.minionList.onAssist(me, attacker)
            }
        }

        if (attacker != null) {
            // Faction check.
            val actorClans = me.template.clans
            if (actorClans != null && me.attackByList.contains(attacker)) {
                for (called in me.getKnownTypeInRadius(Attackable::class.java, me.template.clanRange)) {
                    // Caller hasn't AI or is dead.
                    if (!called.hasAI() || called.isDead)
                        continue

                    // Caller clan doesn't correspond to the called clan.
                    if (!ArraysUtil.contains(actorClans, called.template.clans))
                        continue

                    // Called mob doesnt care about that type of caller id (the bitch !).
                    if (ArraysUtil.contains(called.template.ignoredIds, me.npcId))
                        continue

                    // Check if the WorldObject is inside the Faction Range of the actor
                    val calledIntention = called.ai.desire.intention
                    if ((calledIntention === CtrlIntention.IDLE || calledIntention === CtrlIntention.ACTIVE || calledIntention === CtrlIntention.MOVE_TO && !called.isRunning) && GeoEngine.canSeeTarget(
                            me,
                            called
                        )
                    ) {
                        if (attacker is Playable) {
                            val scripts = called.template.getEventQuests(EventType.ON_FACTION_CALL)
                            if (scripts != null) {
                                val player = attacker.actingPlayer
                                val isSummon = attacker is Summon

                                for (quest in scripts)
                                    quest.notifyFactionCall(called, me, player, isSummon)
                            }
                        } else {
                            called.addDamageHate(attacker, 0, me.getHating(attacker))
                            called.ai.setIntention(CtrlIntention.ATTACK, attacker)
                        }
                    }
                }
            }
        }

        super.onEvtAttacked(attacker)
    }

    /**
     * Launch actions corresponding to the Event Aggression.
     *
     *  * Add the target to the actor _aggroList or update hate if already present
     *  * Set the actor Intention to ATTACK (if actor is L2GuardInstance check if it isn't too far from its home location)
     *
     * @param target The Creature that attacks
     * @param aggro The value of hate to add to the actor against the target
     */
    override fun onEvtAggression(target: Creature?, aggro: Int) {
        val me = activeChar

        // Add the target to the actor _aggroList or update hate if already present
        me.addDamageHate(target, 0, aggro)

        // Set the Intention to ATTACK and make the character running, but only if the AI isn't disabled.
        if (!me.isCoreAIDisabled && desire.intention !== CtrlIntention.ATTACK) {
            me.setRunning()

            setIntention(CtrlIntention.ATTACK, target)
        }

        if (me is Monster) {
            var master: Monster? = me

            if (master!!.hasMinions())
                master.minionList.onAssist(me, target)
            else {
                master = master.getMaster()
                if (master != null && master.hasMinions())
                    master.minionList.onAssist(me, target)
            }
        }

        if (target == null)
            return

        // Faction check.
        val actorClans = me.template.clans
        if (actorClans != null && me.attackByList.contains(target)) {
            for (called in me.getKnownTypeInRadius(Attackable::class.java, me.template.clanRange)) {
                // Caller hasn't AI or is dead.
                if (!called.hasAI() || called.isDead)
                    continue

                // Caller clan doesn't correspond to the called clan.
                if (!ArraysUtil.contains(actorClans, called.template.clans))
                    continue

                // Called mob doesnt care about that type of caller id (the bitch !).
                if (ArraysUtil.contains(called.template.ignoredIds, me.npcId))
                    continue

                // Check if the WorldObject is inside the Faction Range of the actor
                val calledIntention = called.ai.desire.intention
                if ((calledIntention === CtrlIntention.IDLE || calledIntention === CtrlIntention.ACTIVE || calledIntention === CtrlIntention.MOVE_TO && !called.isRunning) && GeoEngine.canSeeTarget(
                        me,
                        called
                    )
                ) {
                    if (target is Playable) {
                        val scripts = called.template.getEventQuests(EventType.ON_FACTION_CALL)
                        if (scripts != null) {
                            val player = target.actingPlayer
                            val isSummon = target is Summon

                            for (quest in scripts)
                                quest.notifyFactionCall(called, me, player, isSummon)
                        }
                    } else {
                        called.addDamageHate(target, 0, me.getHating(target))
                        called.ai.setIntention(CtrlIntention.ATTACK, target)
                    }
                }
            }
        }
    }

    override fun onIntentionActive() {
        // Cancel attack timeout
        _attackTimeout = java.lang.Long.MAX_VALUE

        super.onIntentionActive()
    }

    fun setGlobalAggro(value: Int) {
        _globalAggro = value
    }

    private fun checkBuffAndSetBackTarget(target: WorldObject?): Boolean {
        if (Rnd[RANDOM_WALK_RATE] != 0)
            return false

        for (sk in activeChar.template.getSkills(SkillType.BUFF)) {
            if (activeChar.getFirstEffect(sk) != null)
                continue

            clientStopMoving(null)

            actor.target = actor
            actor.doCast(sk)
            actor.target = target
            return true
        }
        return false
    }

    companion object {
        protected val RANDOM_WALK_RATE = 30
        protected val MAX_ATTACK_TIMEOUT = 90000 // 1m30
    }
}