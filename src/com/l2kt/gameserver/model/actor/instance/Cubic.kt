package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.l2skills.L2SkillDrain
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*
import java.util.concurrent.Future
import java.util.logging.Level
import java.util.logging.Logger

class Cubic(
    owner: Player,
    id: Int,
    level: Int,
    mAtk: Int,
    activationtime: Int,
    protected var _activationchance: Int,
    totallifetime: Int,
    private val _givenByOther: Boolean
) {

    var owner: Player
        protected set
    protected var _target: Creature? = null

    var id: Int = 0
        protected set
    var mAtk: Int = 0
        protected set
    protected var _activationtime: Int = 0
    protected var _active: Boolean = false

    protected var _skills: MutableList<L2Skill> = ArrayList()

    private var _disappearTask: Future<*>? = null
    private var _actionTask: Future<*>? = null

    init {
        this.owner = owner
        this.id = id
        this.mAtk = mAtk
        _activationtime = activationtime * 1000
        _active = false

        when (this.id) {
            STORM_CUBIC -> _skills.add(SkillTable.getInfo(4049, level)!!)

            VAMPIRIC_CUBIC -> _skills.add(SkillTable.getInfo(4050, level)!!)

            LIFE_CUBIC -> {
                _skills.add(SkillTable.getInfo(4051, level)!!)
                doAction()
            }

            VIPER_CUBIC -> _skills.add(SkillTable.getInfo(4052, level)!!)

            POLTERGEIST_CUBIC -> {
                _skills.add(SkillTable.getInfo(4053, level)!!)
                _skills.add(SkillTable.getInfo(4054, level)!!)
                _skills.add(SkillTable.getInfo(4055, level)!!)
            }

            BINDING_CUBIC -> _skills.add(SkillTable.getInfo(4164, level)!!)

            AQUA_CUBIC -> _skills.add(SkillTable.getInfo(4165, level)!!)

            SPARK_CUBIC -> _skills.add(SkillTable.getInfo(4166, level)!!)

            ATTRACT_CUBIC -> {
                _skills.add(SkillTable.getInfo(5115, level)!!)
                _skills.add(SkillTable.getInfo(5116, level)!!)
            }
        }
        _disappearTask = ThreadPool.schedule(Disappear(), totallifetime.toLong()) // disappear
    }

    @Synchronized
    fun doAction() {
        if (_active)
            return

        _active = true

        when (id) {
            AQUA_CUBIC, BINDING_CUBIC, SPARK_CUBIC, STORM_CUBIC, POLTERGEIST_CUBIC, VAMPIRIC_CUBIC, VIPER_CUBIC, ATTRACT_CUBIC -> _actionTask =
                ThreadPool.scheduleAtFixedRate(Action(_activationchance), 0, _activationtime.toLong())

            LIFE_CUBIC -> _actionTask = ThreadPool.scheduleAtFixedRate(Heal(), 0, _activationtime.toLong())
        }
    }

    fun getMCriticalHit(target: Creature, skill: L2Skill): Int {
        return owner.getMCriticalHit(target, skill)
    }

    fun stopAction() {
        _target = null
        if (_actionTask != null) {
            _actionTask!!.cancel(true)
            _actionTask = null
        }
        _active = false
    }

    fun cancelDisappear() {
        if (_disappearTask != null) {
            _disappearTask!!.cancel(true)
            _disappearTask = null
        }
    }

    /** this sets the enemy target for a cubic  */
    fun getCubicTarget() {
        try {
            _target = null
            val ownerTarget = owner.target ?: return

            // Duel targeting
            if (owner.isInDuel) {
                val PlayerA = DuelManager.getDuel(owner.duelId)!!.playerA
                val PlayerB = DuelManager.getDuel(owner.duelId)!!.playerB

                if (DuelManager.getDuel(owner.duelId)!!.isPartyDuel) {
                    val partyA = PlayerA.party
                    val partyB = PlayerB.party
                    var partyEnemy: Party? = null

                    if (partyA != null) {
                        if (partyA.containsPlayer(owner))
                            if (partyB != null)
                                partyEnemy = partyB
                            else
                                _target = PlayerB
                        else
                            partyEnemy = partyA
                    } else {
                        if (PlayerA == owner)
                            if (partyB != null)
                                partyEnemy = partyB
                            else
                                _target = PlayerB
                        else
                            _target = PlayerA
                    }

                    if (_target === PlayerA || _target === PlayerB)
                        if (_target === ownerTarget)
                            return

                    if (partyEnemy != null) {
                        if (partyEnemy.containsPlayer(ownerTarget))
                            _target = ownerTarget as Creature

                        return
                    }
                }

                if (PlayerA != owner && ownerTarget === PlayerA) {
                    _target = PlayerA
                    return
                }

                if (PlayerB != owner && ownerTarget === PlayerB) {
                    _target = PlayerB
                    return
                }

                _target = null
                return
            }

            // Olympiad targeting
            if (owner.isInOlympiadMode) {
                if (owner.isOlympiadStart) {
                    if (ownerTarget is Playable) {
                        val targetPlayer = ownerTarget.actingPlayer
                        if (targetPlayer != null && targetPlayer.olympiadGameId == owner.olympiadGameId && targetPlayer.olympiadSide != owner.olympiadSide)
                            _target = ownerTarget
                    }
                }
                return
            }

            // test owners target if it is valid then use it
            if (ownerTarget is Creature && ownerTarget !== owner.pet && ownerTarget !== owner) {
                // target mob which has aggro on you or your summon
                if (ownerTarget is Attackable) {
                    if (ownerTarget.aggroList[owner] != null && !ownerTarget.isDead) {
                        _target = ownerTarget
                        return
                    }

                    if (owner.pet != null) {
                        if (ownerTarget.aggroList[owner.pet] != null && !ownerTarget.isDead) {
                            _target = ownerTarget
                            return
                        }
                    }
                }

                // get target in pvp or in siege
                var enemy: Player? = null

                if (owner.pvpFlag > 0 && !owner.isInsideZone(ZoneId.PEACE) || owner.isInsideZone(ZoneId.PVP)) {
                    if (!ownerTarget.isDead)
                        enemy = ownerTarget.actingPlayer

                    if (enemy != null) {
                        var targetIt = true

                        val ownerParty = owner.party
                        if (ownerParty != null) {
                            if (ownerParty.containsPlayer(enemy))
                                targetIt = false
                            else if (ownerParty.commandChannel != null) {
                                if (ownerParty.commandChannel!!.containsPlayer(enemy))
                                    targetIt = false
                            }
                        }

                        if (owner.clan != null && !owner.isInsideZone(ZoneId.PVP)) {
                            if (owner.clan.isMember(enemy.objectId))
                                targetIt = false

                            if (owner.allyId > 0 && enemy.allyId > 0) {
                                if (owner.allyId == enemy.allyId)
                                    targetIt = false
                            }
                        }

                        if (enemy.pvpFlag.toInt() == 0 && !enemy.isInsideZone(ZoneId.PVP))
                            targetIt = false

                        if (enemy.isInsideZone(ZoneId.PEACE))
                            targetIt = false

                        if (owner.siegeState > 0 && owner.siegeState == enemy.siegeState)
                            targetIt = false

                        if (!enemy.isVisible)
                            targetIt = false

                        if (targetIt) {
                            _target = enemy
                            return
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _log.log(Level.SEVERE, "", e)
        }

    }

    private inner class Action internal constructor(private val _chance: Int) : Runnable {

        override fun run() {
            try {
                if (owner.isDead || !owner.isOnline) {
                    stopAction()
                    owner.delCubic(id)
                    owner.broadcastUserInfo()
                    cancelDisappear()
                    return
                }

                if (!AttackStanceTaskManager.isInAttackStance(owner)) {
                    stopAction()
                    return
                }

                if (Rnd[1, 100] < _chance) {
                    val skill = Rnd[_skills]
                    if (skill != null) {
                        // friendly skill, so we look a target in owner's party
                        if (skill.id == SKILL_CUBIC_HEAL)
                            cubicTargetForHeal()
                        else {
                            getCubicTarget()
                            if (!isInCubicRange(owner, _target))
                                _target = null
                        }// offensive skill, we look for an enemy target

                        val target = _target
                        if (target != null && !target.isDead) {
                            owner.broadcastPacket(MagicSkillUse(owner, target, skill.id, skill.level, 0, 0))

                            val type = skill.skillType
                            val handler = SkillHandler.getHandler(skill.skillType)
                            val targets = arrayOf<WorldObject>(target)

                            if (type === L2SkillType.PARALYZE || type === L2SkillType.STUN || type === L2SkillType.ROOT || type === L2SkillType.AGGDAMAGE)
                                useCubicDisabler(type, this@Cubic, skill, targets)
                            else if (type === L2SkillType.MDAM)
                                useCubicMdam(this@Cubic, skill, targets)
                            else if (type === L2SkillType.POISON || type === L2SkillType.DEBUFF || type === L2SkillType.DOT)
                                useCubicContinuous(this@Cubic, skill, targets)
                            else if (type === L2SkillType.DRAIN)
                                (skill as L2SkillDrain).useCubicSkill(this@Cubic, targets)
                            else
                                handler!!.useSkill(owner, skill, targets)
                        }
                    }
                }
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "", e)
            }

        }
    }

    fun useCubicContinuous(activeCubic: Cubic, skill: L2Skill, targets: Array<WorldObject>) {
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead)
                continue

            if (skill.isOffensive) {
                val shld = Formulas.calcShldUse(activeCubic.owner, obj, skill)
                val bss = activeCubic.owner.isChargedShot(ShotType.BLESSED_SPIRITSHOT)
                val acted = Formulas.calcCubicSkillSuccess(activeCubic, obj, skill, shld, bss)

                if (!acted) {
                    activeCubic.owner.sendPacket(SystemMessageId.ATTACK_FAILED)
                    continue
                }
            }

            // If this is a debuff, let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
            if (obj is Player && obj.isInDuel && skill.skillType === L2SkillType.DEBUFF && activeCubic.owner.duelId == obj.duelId) {
                for (debuff in skill.getEffects(activeCubic.owner, obj)) {
                    if (debuff != null)
                        DuelManager.onBuff(obj, debuff)
                }
            } else
                skill.getEffects(activeCubic, obj, null)
        }
    }

    fun useCubicMdam(activeCubic: Cubic, skill: L2Skill, targets: Array<WorldObject>) {
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isAlikeDead) {
                if (obj is Player)
                    obj.stopFakeDeath(true)
                else
                    continue
            }

            val mcrit = Formulas.calcMCrit(activeCubic.getMCriticalHit(obj, skill))
            val shld = Formulas.calcShldUse(activeCubic.owner, obj, skill)
            val bss = activeCubic.owner.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

            var damage = Formulas.calcMagicDam(activeCubic, obj, skill, mcrit, shld).toInt()

            // If target is reflecting the skill then no damage is done Ignoring vengance-like reflections
            if ((Formulas.calcSkillReflect(obj, skill).toInt() and Formulas.SKILL_REFLECT_SUCCEED.toInt()) > 0)
                damage = 0

            if (damage > 0) {
                // Manage cast break of the target (calculating rate, sending message...)
                Formulas.calcCastBreak(obj, damage.toDouble())

                activeCubic.owner.sendDamageMessage(obj, damage, mcrit, false, false)

                if (skill.hasEffects()) {
                    // activate attacked effects, if any
                    obj.stopSkillEffects(skill.id)

                    if (obj.getFirstEffect(skill) != null)
                        obj.removeEffect(obj.getFirstEffect(skill))

                    if (Formulas.calcCubicSkillSuccess(activeCubic, obj, skill, shld, bss))
                        skill.getEffects(activeCubic, obj, null)
                }

                obj.reduceCurrentHp(damage.toDouble(), activeCubic.owner, skill)
            }
        }
    }

    fun useCubicDisabler(type: L2SkillType, activeCubic: Cubic, skill: L2Skill, targets: Array<WorldObject>) {
        for (obj in targets) {
            if (obj !is Creature)
                continue

            if (obj.isDead)
                continue

            val shld = Formulas.calcShldUse(activeCubic.owner, obj, skill)
            val bss = activeCubic.owner.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

            when (type) {
                L2SkillType.STUN, L2SkillType.PARALYZE, L2SkillType.ROOT -> if (Formulas.calcCubicSkillSuccess(
                        activeCubic,
                        obj,
                        skill,
                        shld,
                        bss
                    )
                ) {
                    // If this is a debuff, let the duel manager know about it so the debuff can be removed after the duel (player & target must be in the same duel)
                    if (obj is Player && obj.isInDuel && skill.skillType === L2SkillType.DEBUFF && activeCubic.owner.duelId == obj.duelId) {
                        for (debuff in skill.getEffects(activeCubic.owner, obj)) {
                            if (debuff != null)
                                DuelManager.onBuff(obj, debuff)
                        }
                    } else
                        skill.getEffects(activeCubic, obj, null)
                }

                L2SkillType.CANCEL_DEBUFF -> run {
                    val effects = obj.allEffects
                    if (effects == null || effects.size == 0)
                        return@run

                    var count = if (skill.maxNegatedEffects > 0) 0 else -2
                    for (e in effects) {
                        if (e.skill.isDebuff && count < skill.maxNegatedEffects) {
                            // Do not remove raid curse skills
                            if (e.skill.id != 4215 && e.skill.id != 4515 && e.skill.id != 4082) {
                                e.exit()
                                if (count > -1)
                                    count++
                            }
                        }
                    }
                }

                L2SkillType.AGGDAMAGE -> if (Formulas.calcCubicSkillSuccess(activeCubic, obj, skill, shld, bss)) {
                    if (obj is Attackable)
                        obj.getAI().notifyEvent(
                            CtrlEvent.EVT_AGGRESSION,
                            activeCubic.owner,
                            (150 * skill.power / (obj.getLevel() + 7)).toInt()
                        )

                    skill.getEffects(activeCubic, obj, null)
                }
            }
        }
    }

    /**
     * @param owner
     * @param target
     * @return true if the target is inside of the owner's max Cubic range
     */
    fun isInCubicRange(owner: Creature?, target: Creature?): Boolean {
        if (owner == null || target == null)
            return false

        val x: Int
        val y: Int
        val z: Int
        val range = MAX_MAGIC_RANGE

        x = owner.x - target.x
        y = owner.y - target.y
        z = owner.z - target.z

        return x * x + y * y + z * z <= range * range
    }

    /** this sets the friendly target for a cubic  */
    fun cubicTargetForHeal() {
        var target: Creature? = null
        var percentleft = 100.0
        var party = owner.party

        // if owner is in a duel but not in a party duel, then it is the same as he does not have a party
        if (owner.isInDuel)
            if (!DuelManager.getDuel(owner.duelId)!!.isPartyDuel)
                party = null

        if (party != null && !owner.isInOlympiadMode) {
            // Get all Party Members in a spheric area near the Creature
            for (partyMember in party.members) {
                if (!partyMember.isDead) {
                    // if party member not dead, check if he is in castrange of heal cubic
                    if (isInCubicRange(owner, partyMember)) {
                        // member is in cubic casting range, check if he need heal and if he have the lowest HP
                        if (partyMember.currentHp < partyMember.maxHp) {
                            if (percentleft > partyMember.currentHp / partyMember.maxHp) {
                                percentleft = partyMember.currentHp / partyMember.maxHp
                                target = partyMember
                            }
                        }
                    }
                }

                if (partyMember.pet != null) {
                    if (partyMember.pet!!.isDead)
                        continue

                    // if party member's pet not dead, check if it is in castrange of heal cubic
                    if (!isInCubicRange(owner, partyMember.pet))
                        continue

                    // member's pet is in cubic casting range, check if he need heal and if he have
                    // the lowest HP
                    if (partyMember.pet!!.currentHp < partyMember.pet!!.maxHp) {
                        if (percentleft > partyMember.pet!!.currentHp / partyMember.pet!!.maxHp) {
                            percentleft = partyMember.pet!!.currentHp / partyMember.pet!!.maxHp
                            target = partyMember.pet
                        }
                    }
                }
            }
        } else {
            if (owner.currentHp < owner.maxHp) {
                percentleft = owner.currentHp / owner.maxHp
                target = owner
            }

            if (owner.pet != null && !owner.pet!!.isDead && owner.pet!!.currentHp < owner.pet!!.maxHp && percentleft > owner.pet!!.currentHp / owner.pet!!.maxHp && isInCubicRange(
                    owner,
                    owner.pet
                )
            )
                target = owner.pet
        }

        _target = target
    }

    fun givenByOther(): Boolean {
        return _givenByOther
    }

    private inner class Heal internal constructor() : Runnable {

        override fun run() {
            if (owner.isDead || !owner.isOnline) {
                stopAction()
                owner.delCubic(id)
                owner.broadcastUserInfo()
                cancelDisappear()
                return
            }

            try {
                var skill: L2Skill? = null
                for (sk in _skills) {
                    if (sk.id == SKILL_CUBIC_HEAL) {
                        skill = sk
                        break
                    }
                }

                if (skill != null) {
                    cubicTargetForHeal()
                    val target = _target
                    if (target != null && !target.isDead) {
                        if (target.maxHp - target.currentHp > skill.power) {
                            val targets = arrayOf<WorldObject>(target)

                            val handler = SkillHandler.getHandler(skill.skillType)
                            if (handler != null)
                                handler.useSkill(owner, skill, targets)
                            else
                                skill.useSkill(owner, targets)

                            val msu = MagicSkillUse(owner, target, skill.id, skill.level, 0, 0)
                            owner.broadcastPacket(msu)
                        }
                    }
                }
            } catch (e: Exception) {
                _log.log(Level.SEVERE, "", e)
            }

        }
    }

    private inner class Disappear internal constructor() : Runnable {

        override fun run() {
            stopAction()
            owner.delCubic(id)
            owner.broadcastUserInfo()
        }
    }

    companion object {
        protected val _log = Logger.getLogger(Cubic::class.java.name)

        // Type of cubics
        const val STORM_CUBIC = 1
        const val VAMPIRIC_CUBIC = 2
        const val LIFE_CUBIC = 3
        const val VIPER_CUBIC = 4
        const val POLTERGEIST_CUBIC = 5
        const val BINDING_CUBIC = 6
        const val AQUA_CUBIC = 7
        const val SPARK_CUBIC = 8
        const val ATTRACT_CUBIC = 9

        // Max range of cubic skills
        const val MAX_MAGIC_RANGE = 900

        // Cubic skills
        const val SKILL_CUBIC_HEAL = 4051
        const val SKILL_CUBIC_CURE = 5579
    }
}