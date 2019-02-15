package com.l2kt.gameserver.model

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.skills.effects.EffectFusion
import java.util.concurrent.Future
import java.util.logging.Logger

/**
 * @author kombat, Forsaiken
 */
class FusionSkill(caster: Creature, target: Creature, skill: L2Skill) {

    private var _skillCastRange: Int = 0
    private var _fusionId: Int = 0
    private var _fusionLevel: Int = 0
    var caster: Creature
        private set
    var target: Creature
        private set
    private var _geoCheckTask: Future<*>

    init {
        _skillCastRange = skill.castRange
        this.caster = caster
        this.target = target
        _fusionId = skill.triggeredId
        _fusionLevel = skill.triggeredLevel

        val effect = this.target.getFirstEffect(_fusionId)
        if (effect != null)
            (effect as EffectFusion).increaseEffect()
        else {
            val force = SkillTable.getInfo(_fusionId, _fusionLevel)
            if (force != null)
                force.getEffects(this.caster, this.target, null)
            else
                _log.warning("Triggered skill [$_fusionId;$_fusionLevel] not found!")
        }
        _geoCheckTask = ThreadPool.scheduleAtFixedRate(GeoCheckTask(), 1000, 1000)!!
    }

    fun onCastAbort() {
        caster.fusionSkill = null
        val effect = target.getFirstEffect(_fusionId)
        if (effect != null)
            (effect as EffectFusion).decreaseForce()

        _geoCheckTask.cancel(true)
    }

    inner class GeoCheckTask : Runnable {
        override fun run() {
            try {
                if (!MathUtil.checkIfInRange(_skillCastRange, caster, target, true))
                    caster.abortCast()

                if (!GeoEngine.getInstance().canSeeTarget(caster, target))
                    caster.abortCast()
            } catch (e: Exception) {
                // ignore
            }

        }
    }

    companion object {
        private val _log = Logger.getLogger(FusionSkill::class.java.name)
    }
}