package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.EffectPoint
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.l2skills.L2SkillSignet
import com.l2kt.gameserver.skills.l2skills.L2SkillSignetCasttime
import com.l2kt.gameserver.templates.skills.L2EffectType
import java.util.*

class EffectSignet(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _skill: L2Skill? = null
    private var _actor: EffectPoint? = null
    private var _srcInArena: Boolean = false

    override fun getEffectType(): L2EffectType {
        return L2EffectType.SIGNET_EFFECT
    }

    override fun onStart(): Boolean {
        if (skill is L2SkillSignet)
            _skill = SkillTable.getInfo((skill as L2SkillSignet).effectId, level)
        else if (skill is L2SkillSignetCasttime)
            _skill = SkillTable.getInfo((skill as L2SkillSignetCasttime).effectId, level)

        _actor = effected as EffectPoint
        _srcInArena = effector.isInArena
        return true
    }

    override fun onActionTime(): Boolean {
        if (_skill == null)
            return true

        val mpConsume = _skill!!.mpConsume
        if (mpConsume > effector.currentMp) {
            effector.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP))
            return false
        }
        effector.reduceCurrentMp(mpConsume.toDouble())

        val targets = ArrayList<Creature>()
        for (cha in _actor!!.getKnownTypeInRadius(Creature::class.java, skill.skillRadius)) {
            if (_skill!!.isOffensive && !L2Skill.checkForAreaOffensiveSkills(effector, cha, _skill, _srcInArena))
                continue

            _actor!!.broadcastPacket(MagicSkillUse(_actor!!, cha, _skill!!.id, _skill!!.level, 0, 0))
            targets.add(cha)
        }

        if (!targets.isEmpty())
            effector.callSkill(_skill, targets.toTypedArray())

        return true
    }

    override fun onExit() {
        if (_actor != null)
            _actor!!.deleteMe()
    }
}