package com.l2kt.gameserver.model.actor.ai.type

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeGuard
import com.l2kt.gameserver.model.actor.template.NpcTemplate.AIType
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneId

internal class SiegeGuardAI(guard: SiegeGuard) : AttackableAI(guard) {

    private val activeChar: SiegeGuard
        get() = actor as SiegeGuard

    /**
     * Following conditions are checked for a siege defender :
     *
     *  * if target isn't a player or a summon.
     *  * if target is dead.
     *  * if target is a GM in hide mode.
     *  * if player is silent moving.
     *  * if the target can't be seen and is a defender.
     *
     * @param target The targeted Creature.
     * @return True if the target is autoattackable (depends on the actor type).
     */
    override fun autoAttackCondition(target: Creature?): Boolean {
        if (target !is Playable || target.isAlikeDead())
            return false

        val player = target.actingPlayer ?: return false

        // Check if the target isn't GM on hide mode.
        if (player.isGM && player.appearance.invisible)
            return false

        // Check if the target isn't in silent move mode AND too far
        return if (player.isSilentMoving && !actor.isInsideRadius(
                player,
                250,
                false,
                false
            )
        ) false else actor.isAutoAttackable(target) && GeoEngine.canSeeTarget(actor, target)

        // Los Check Here
    }

    /**
     * Set the Intention of this CreatureAI and create an AI Task executed every 1s (call onEvtThink method) for this L2Attackable.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If actor _knowPlayer isn't EMPTY, IDLE will be change in ACTIVE</B></FONT>
     * @param intention The new Intention to set to the AI
     * @param arg0 The first parameter of the Intention
     * @param arg1 The second parameter of the Intention
     */
    @Synchronized
    override fun changeIntention(intention: CtrlIntention, arg0: Any?, arg1: Any?) {
        var intention = intention
        // Active becomes idle if only a summon is present
        if (intention === CtrlIntention.IDLE) {
            // Check if actor is not dead
            if (!actor.isAlikeDead) {
                // If no players are around, set the Intention to ACTIVE
                if (!activeChar.getKnownType(Player::class.java).isEmpty())
                    intention = CtrlIntention.ACTIVE
            }

            if (intention === CtrlIntention.IDLE) {
                // Set the Intention of this L2AttackableAI to IDLE
                super.changeIntention(CtrlIntention.IDLE, null, null)

                // Stop AI task and detach AI from NPC
                if (_aiTask != null) {
                    _aiTask!!.cancel(true)
                    _aiTask = null
                }

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
     * Manage AI standard thinks of a L2Attackable (called by onEvtThink).
     *
     *  * Update every 1s the _globalAggro counter to come close to 0
     *  * If the actor is Aggressive and can attack, add all autoAttackable Creature in its Aggro Range to its _aggroList, chose a target and order to attack it
     *  * If the actor can't attack, order to it to return to its home location
     *
     */
    override fun thinkActive() {
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
            val npc = actor as Attackable
            for (target in npc.getKnownTypeInRadius(Creature::class.java, npc.template.clanRange)) {
                if (autoAttackCondition(target))
                // check aggression
                {
                    // Get the hate level of the L2Attackable against this target, and add the attacker to the L2Attackable _aggroList
                    if (npc.getHating(target) == 0)
                        npc.addDamageHate(target, 0, 1)
                }
            }

            // Chose a target from its aggroList
            val hated = (if (actor.isConfused()) target else npc.mostHated) as Creature?
            if (hated != null) {
                // Get the hate level of the L2Attackable against this Creature target contained in _aggroList
                if (npc.getHating(hated) + _globalAggro > 0) {
                    // Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
                    actor.setRunning()

                    // Set the AI Intention to ATTACK
                    setIntention(CtrlIntention.ATTACK, hated)
                }
                return
            }
        }
        // Order to the L2SiegeGuardInstance to return to its home location because there's no target to attack
        activeChar.returnHome()
    }

    /**
     * Manage AI attack thinks of a L2Attackable (called by onEvtThink).
     *
     *  * Update the attack timeout if actor is running
     *  * If target is dead or timeout is expired, stop this attack and set the Intention to ACTIVE
     *  * Call all WorldObject of its Faction inside the Faction Range
     *  * Chose a target and order to attack it with magic skill or physical attack
     *
     */
    override fun thinkAttack() {
        val actor = activeChar
        if (actor.isCastingNow)
            return

        /**
         * RETURN HOME<br></br>
         * Check if the siege guard isn't too far ; if yes, then move him back to home.
         */
        if (!actor.isInsideZone(ZoneId.SIEGE)) {
            actor.returnHome()
            return
        }

        // Pickup most hated character.
        var attackTarget = actor.mostHated

        // If target doesn't exist, is too far or if timeout is expired.
        if (attackTarget == null || _attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(
                actor,
                attackTarget,
                true
            ) > 2000
        ) {
            // Stop hating this target after the attack timeout or if target is dead
            actor.stopHating(attackTarget)

            // Search the nearest target. If a target is found, continue regular process, else drop angry behavior.
            attackTarget = targetReconsider(actor.template.clanRange, false)
            if (attackTarget == null) {
                setIntention(CtrlIntention.ACTIVE)
                actor.setWalking()
                return
            }
        }

        /**
         * COMMON INFORMATIONS<br></br>
         * Used for range and distance check.
         */

        val actorCollision = actor.collisionRadius.toInt()
        val combinedCollision = (actorCollision + attackTarget.collisionRadius).toInt()
        val dist = Math.sqrt(actor.getPlanDistanceSq(attackTarget.x, attackTarget.y))

        var range = combinedCollision
        if (attackTarget.isMoving)
            range += 15

        if (actor.isMoving)
            range += 15

        /**
         * Target setup.
         */

        target = attackTarget
        actor.target = attackTarget

        /**
         * Cast a spell.
         */

        if (willCastASpell()) {
            // This list is used in order to avoid multiple calls on skills lists. Tests are made one after the other, and content is replaced when needed.
            var defaultList: List<L2Skill>

            // -------------------------------------------------------------------------------
            // Heal
            defaultList = actor.template.getSkills(SkillType.HEAL)
            if (!defaultList.isEmpty()) {
                val clans = actor.template.clans

                // Go through all characters around the actor that belongs to its faction.
                for (cha in actor.getKnownTypeInRadius(Creature::class.java, 1000)) {
                    // Don't bother about dead, not visible, or healthy characters.
                    if (cha.isAlikeDead || !GeoEngine.canSeeTarget(actor, cha) || cha.currentHp / cha.maxHp > 0.75)
                        continue

                    // Will affect only defenders or NPCs from same faction.
                    if (!actor.isAttackingDisabled && cha is Player && actor.castle.siege.checkSides(
                            cha.clan,
                            SiegeSide.DEFENDER,
                            SiegeSide.OWNER
                        ) || cha is Npc && ArraysUtil.contains(clans, cha.template.clans)
                    ) {
                        for (sk in defaultList) {
                            if (!MathUtil.checkIfInRange(sk.castRange, actor, cha, true))
                                continue

                            clientStopMoving(null)

                            actor.target = cha
                            actor.doCast(sk)
                            actor.target = attackTarget
                            return
                        }
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // Buff
            defaultList = actor.template.getSkills(SkillType.BUFF)
            if (!defaultList.isEmpty()) {
                for (sk in defaultList) {
                    if (!checkSkillCastConditions(sk))
                        continue

                    if (actor.getFirstEffect(sk) == null) {
                        clientStopMoving(null)

                        actor.target = actor
                        actor.doCast(sk)
                        actor.target = attackTarget
                        return
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // Debuff - 10% luck to get debuffed.
            defaultList = actor.template.getSkills(SkillType.DEBUFF)
            if (Rnd[100] < 10 && !defaultList.isEmpty()) {
                for (sk in defaultList) {
                    if (!checkSkillCastConditions(sk) || sk.castRange + range <= dist && !canAura(sk))
                        continue

                    if (!GeoEngine.canSeeTarget(actor, attackTarget))
                        continue

                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null)
                        actor.doCast(sk)
                        return
                    }
                }
            }

            // -------------------------------------------------------------------------------
            // General attack skill - short range is checked, then long range.
            defaultList = actor.template.getSkills(SkillType.SHORT_RANGE)
            if (!defaultList.isEmpty() && dist <= 150) {
                val skill = Rnd[defaultList]
                if (cast(skill, dist, skill!!.castRange))
                    return
            } else {
                defaultList = actor.template.getSkills(SkillType.LONG_RANGE)
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
        range += actor.physicalAttackRange

        if (actor.isMovementDisabled) {
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
            for (nearby in actor.getKnownTypeInRadius(Attackable::class.java, actorCollision)) {
                if (nearby !== attackTarget) {
                    var newX = combinedCollision + Rnd[40]
                    if (Rnd.nextBoolean())
                        newX += attackTarget.x
                    else
                        newX = attackTarget.x - newX

                    var newY = combinedCollision + Rnd[40]
                    if (Rnd.nextBoolean())
                        newY += attackTarget.y
                    else
                        newY = attackTarget.y - newY

                    if (!actor.isInsideRadius(newX, newY, actorCollision, false)) {
                        val newZ = actor.z + 30
                        if (GeoEngine.canMoveToTarget(actor.x, actor.y, actor.z, newX, newY, newZ))
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

        if (actor.template.aiType == AIType.ARCHER && dist <= 60 + combinedCollision && Rnd[4] > 1) {
            val posX = actor.x + if (attackTarget.x < actor.x) 300 else -300
            val posY = actor.y + if (attackTarget.y < actor.y) 300 else -300
            val posZ = actor.z + 30

            if (GeoEngine.canMoveToTarget(actor.x, actor.y, actor.z, posX, posY, posZ)) {
                setIntention(CtrlIntention.MOVE_TO, Location(posX, posY, posZ))
                return
            }
        }

        /**
         * BASIC MELEE ATTACK
         */

        if (maybeMoveToPawn(target, actor.physicalAttackRange))
            return

        clientStopMoving(null)
        actor.doAttack(target as Creature)
    }

    /**
     * Method used when the actor can't attack his current target (immobilize state, for exemple).
     *
     *  * If the actor got an hate list, pickup a new target from it.
     *  * If the selected target is a defenser, drop from the list and pickup another.
     *
     * @param range The range to check (skill range for skill ; physical range for melee).
     * @param rangeCheck That boolean is used to see if a check based on the distance must be made (skill check).
     * @return The new Creature victim.
     */
    override fun targetReconsider(range: Int, rangeCheck: Boolean): Creature? {
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
        return null
    }

    override fun stopAITask() {
        super.stopAITask()
        actor.detachAI()
    }
}