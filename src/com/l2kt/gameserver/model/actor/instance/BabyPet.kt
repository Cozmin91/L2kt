package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2SkillType

import java.util.concurrent.Future

/**
 * A BabyPet can heal his owner. It got 2 heal power, weak or strong.
 *
 *  * If the owner's HP is more than 80%, do nothing.
 *  * If the owner's HP is under 15%, have 75% chances of using a strong heal.
 *  * Otherwise, have 25% chances for weak heal.
 *
 */
class BabyPet(objectId: Int, template: NpcTemplate, owner: Player, control: ItemInstance) :
    Pet(objectId, template, owner, control) {
    protected var _majorHeal: IntIntHolder? = null
    protected var _minorHeal: IntIntHolder? = null

    private var _castTask: Future<*>? = null

    override fun onSpawn() {
        super.onSpawn()

        var healPower = 0.0
        var skillLevel: Int
        for (skill in template.getSkills(SkillType.HEAL)) {
            if (skill.targetType != L2Skill.SkillTargetType.TARGET_OWNER_PET || skill.skillType !== L2SkillType.HEAL)
                continue

            // The skill level is calculated on the fly. Template got an skill level of 1.
            skillLevel = getSkillLevel(skill.id)
            if (skillLevel <= 0)
                continue

            if (healPower == 0.0) {
                // set both heal types to the same skill
                _majorHeal = IntIntHolder(skill.id, skillLevel)
                _minorHeal = _majorHeal
                healPower = skill.power
            } else {
                // another heal skill found - search for most powerful
                if (skill.power > healPower)
                    _majorHeal = IntIntHolder(skill.id, skillLevel)
                else
                    _minorHeal = IntIntHolder(skill.id, skillLevel)
            }
        }
        startCastTask()
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        stopCastTask()
        abortCast()
        return true
    }

    @Synchronized
    override fun unSummon(owner: Player) {
        stopCastTask()
        abortCast()

        super.unSummon(owner)
    }

    override fun doRevive() {
        super.doRevive()
        startCastTask()
    }

    private fun startCastTask() {
        if (_majorHeal != null && _castTask == null && !isDead)
        // cast task is not yet started and not dead (will start on revive)
            _castTask = ThreadPool.scheduleAtFixedRate(CastTask(this), 3000, 1000)
    }

    private fun stopCastTask() {
        if (_castTask != null) {
            _castTask!!.cancel(false)
            _castTask = null
        }
    }

    protected fun castSkill(skill: L2Skill) {
        // casting automatically stops any other action (such as autofollow or a move-to).
        // We need to gather the necessary info to restore the previous state.
        val previousFollowStatus = followStatus

        // pet not following and owner outside cast range
        if (!previousFollowStatus && !isInsideRadius(owner, skill.castRange, true, true))
            return

        target = owner
        useMagic(skill, false, false)

        owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_USES_S1).addSkillName(skill))

        // calling useMagic changes the follow status, if the babypet actually casts
        // (as opposed to failing due some factors, such as too low MP, etc).
        // if the status has actually been changed, revert it. Else, allow the pet to
        // continue whatever it was trying to do.
        // NOTE: This is important since the pet may have been told to attack a target.
        // reverting the follow status will abort this attack! While aborting the attack
        // in order to heal is natural, it is not acceptable to abort the attack on its own,
        // merely because the timer stroke and without taking any other action...
        if (previousFollowStatus != followStatus)
            followStatus = previousFollowStatus
    }

    private inner class CastTask(private val _baby: BabyPet) : Runnable {

        override fun run() {
            val owner = _baby.owner

            // if the owner is dead, merely wait for the owner to be resurrected
            // if the pet is still casting from the previous iteration, allow the cast to complete...
            if (owner != null && !owner.isDead && !owner.isInvul && !_baby.isCastingNow && !_baby.isBetrayed && !_baby.isMuted && !_baby.isOutOfControl && _baby.ai.desire.intention !== CtrlIntention.CAST) {
                var skill: L2Skill? = null

                if (_majorHeal != null) {
                    val hpPercent = owner.currentHp / owner.maxHp
                    if (hpPercent < 0.15) {
                        skill = _majorHeal!!.skill
                        if (!_baby.isSkillDisabled(skill) && Rnd[100] <= 75) {
                            if (_baby.currentMp >= skill!!.mpConsume) {
                                castSkill(skill)
                                return
                            }
                        }
                    } else if (_majorHeal!!.skill !== _minorHeal!!.skill && hpPercent < 0.8) {
                        // Cast _minorHeal only if it's different than _majorHeal, then pet has two heals available.
                        skill = _minorHeal!!.skill
                        if (!_baby.isSkillDisabled(skill) && Rnd[100] <= 25) {
                            if (_baby.currentMp >= skill!!.mpConsume) {
                                castSkill(skill)
                                return
                            }
                        }
                    }
                }
            }
        }
    }
}