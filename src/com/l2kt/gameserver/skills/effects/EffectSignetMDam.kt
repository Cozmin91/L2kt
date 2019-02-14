package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.instance.EffectPoint
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.l2skills.L2SkillSignetCasttime
import com.l2kt.gameserver.templates.skills.L2EffectType
import java.util.*

class EffectSignetMDam(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _actor: EffectPoint? = null

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SIGNET_GROUND
    }

    override fun onStart(): Boolean {
        val template: NpcTemplate
        if (skill is L2SkillSignetCasttime)
            template = NpcData.getTemplate((skill as L2SkillSignetCasttime)._effectNpcId)!!
        else
            return false

        val effectPoint = EffectPoint(IdFactory.getInstance().nextId, template, effector)
        effectPoint.currentHp = effectPoint.maxHp.toDouble()
        effectPoint.currentMp = effectPoint.maxMp.toDouble()

        var worldPosition: Location? = null
        if (effector is Player && skill.targetType == L2Skill.SkillTargetType.TARGET_GROUND)
            worldPosition = (effector as Player).currentSkillWorldPosition

        effectPoint.setIsInvul(true)
        effectPoint.spawnMe(worldPosition ?: effector.position)

        _actor = effectPoint
        return true

    }

    override fun onActionTime(): Boolean {
        if (count >= totalCount - 2)
            return true

        val caster = effector as Player

        val mpConsume = skill.mpConsume

        val sps = caster.isChargedShot(ShotType.SPIRITSHOT)
        val bsps = caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT)

        val targets = ArrayList<Creature>()

        for (cha in _actor!!.getKnownTypeInRadius(Creature::class.java, skill.skillRadius)) {
            if (cha === caster)
                continue

            if (cha is Attackable || cha is Playable) {
                if (cha.isAlikeDead)
                    continue

                if (mpConsume > caster.currentMp) {
                    caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP)
                    return false
                }

                caster.reduceCurrentMp(mpConsume.toDouble())

                if (cha is Playable) {
                    if (caster.canAttackCharacter(cha)) {
                        targets.add(cha)
                        caster.updatePvPStatus(cha)
                    }
                } else
                    targets.add(cha)
            }
        }

        if (!targets.isEmpty()) {
            caster.broadcastPacket(MagicSkillLaunched(caster, skill.id, skill.level, targets))
            for (target in targets) {
                val mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, skill))
                val shld = Formulas.calcShldUse(caster, target, skill)

                val mdam = Formulas.calcMagicDam(caster, target, skill, shld, sps, bsps, mcrit).toInt()

                if (target is Summon)
                    target.broadcastStatusUpdate()

                if (mdam > 0) {
                    Formulas.calcCastBreak(target, mdam.toDouble())

                    caster.sendDamageMessage(target, mdam, mcrit, false, false)
                    target.reduceCurrentHp(mdam.toDouble(), caster, skill)
                }
                target.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, caster)
            }
        }
        return true
    }

    override fun onExit() {
        if (_actor != null)
            _actor!!.deleteMe()
    }
}