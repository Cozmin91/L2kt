package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.actor.instance.SiegeSummon
import com.l2kt.gameserver.model.base.Experience
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.templates.StatsSet

class L2SkillSummon(set: StatsSet) : L2Skill(set) {

    private val _npcId: Int = set.getInteger("npcId", 0)
    private val _expPenalty: Float = set.getFloat("expPenalty", 0f)
    val isCubic: Boolean = set.getBool("isCubic", false)

    // Activation time for a cubic
    private val _activationtime: Int = set.getInteger("activationtime", 8)
    // Activation chance for a cubic.
    private val _activationchance: Int = set.getInteger("activationchance", 30)

    // What is the total lifetime of summons (in millisecs)
    val totalLifeTime: Int = set.getInteger("summonTotalLifeTime", 1200000)
    // How much lifetime is lost per second of idleness (non-fighting)
    val timeLostIdle: Int = set.getInteger("summonTimeLostIdle", 0)
    // How much time is lost per second of activity (fighting)
    val timeLostActive: Int = set.getInteger("summonTimeLostActive", 0)

    // item consume time in milliseconds
    /**
     * @return Returns the itemConsume time in milliseconds.
     */
    val itemConsumeTime: Int = set.getInteger("itemConsumeTime", 0)
    // item consume count over time
    /**
     * @return Returns the itemConsume count over time.
     */
    val itemConsumeOT: Int = set.getInteger("itemConsumeCountOT", 0)
    // item consume id over time
    /**
     * @return Returns the itemConsumeId over time.
     */
    val itemConsumeIdOT: Int = set.getInteger("itemConsumeIdOT", 0)
    // how many times to consume an item
    val itemConsumeSteps: Int = set.getInteger("itemConsumeSteps", 0)

    fun checkCondition(activeChar: Creature): Boolean {
        if (activeChar is Player) {

            if (isCubic) {
                // Player is always able to cast mass cubic skill
                if (targetType != L2Skill.SkillTargetType.TARGET_SELF)
                    return true

                if (activeChar.cubics.size > activeChar.getSkillLevel(SKILL_CUBIC_MASTERY)) {
                    activeChar.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED)
                    return false
                }
            } else {
                if (activeChar.isInObserverMode)
                    return false

                if (activeChar.pet != null) {
                    activeChar.sendPacket(SystemMessageId.SUMMON_ONLY_ONE)
                    return false
                }
            }
        }
        return super.checkCondition(activeChar, null, false)
    }

    override fun useSkill(caster: Creature, targets: Array<WorldObject>) {
        if (caster.isAlikeDead || caster !is Player)
            return

        if (_npcId == 0) {
            caster.sendMessage("Summon skill $id not described yet")
            return
        }

        if (isCubic) {
            var _cubicSkillLevel = level
            if (_cubicSkillLevel > 100)
                _cubicSkillLevel = Math.round(((level - 100) / 7 + 8).toFloat())

            if (targets.size > 1)
            // Mass cubic skill
            {
                for (obj in targets) {
                    if (obj !is Player)
                        continue

                    val mastery = obj.getSkillLevel(SKILL_CUBIC_MASTERY)

                    // Player can have only 1 cubic if they don't own cubic mastery - we should replace old cubic with new one.
                    if (mastery == 0 && !obj.cubics.isEmpty()) {
                        for (c in obj.cubics.values) {
                            c!!.stopAction()
                        }
                        obj.cubics.clear()
                    }

                    if (obj.cubics.containsKey(_npcId)) {
                        val cubic = obj.getCubic(_npcId)
                        cubic.stopAction()
                        cubic.cancelDisappear()
                        obj.delCubic(_npcId)
                    }

                    if (obj.cubics.size > mastery)
                        continue

                    if (obj == caster)
                        obj.addCubic(_npcId, _cubicSkillLevel, power, _activationtime, _activationchance, totalLifeTime,false)
                    else
                        obj.addCubic(_npcId, _cubicSkillLevel, power, _activationtime, _activationchance, totalLifeTime, true)

                    obj.broadcastUserInfo()
                }
                return
            }

            if (caster.cubics.containsKey(_npcId)) {
                val cubic = caster.getCubic(_npcId)
                cubic.stopAction()
                cubic.cancelDisappear()
                caster.delCubic(_npcId)
            }

            if (caster.cubics.size > caster.getSkillLevel(SKILL_CUBIC_MASTERY)) {
                caster.sendPacket(SystemMessageId.CUBIC_SUMMONING_FAILED)
                return
            }

            caster.addCubic(_npcId, _cubicSkillLevel, power, _activationtime, _activationchance, totalLifeTime, false)
            caster.broadcastUserInfo()
            return
        }

        if (caster.pet != null || caster.isMounted)
            return

        val summon: Servitor
        val summonTemplate = NpcData.getTemplate(_npcId)

        if (summonTemplate != null && summonTemplate.isType("SiegeSummon"))
            summon = SiegeSummon(IdFactory.getInstance().nextId, summonTemplate, caster, this)
        else
            summon = Servitor(IdFactory.getInstance().nextId, summonTemplate!!, caster, this)

        summon.name = summonTemplate?.name ?: ""
        summon.title = caster.name
        summon.expPenalty = _expPenalty

        if (summon.level >= Experience.LEVEL.size) {
            summon.stat.exp = Experience.LEVEL[Experience.LEVEL.size - 1]
            L2Skill._log.warning("Summon (" + summon.name + ") NpcID: " + summon.npcId + " has a level above 75. Please rectify.")
        } else
            summon.stat.exp = Experience.LEVEL[summon.level % Experience.LEVEL.size]

        summon.currentHp = summon.maxHp.toDouble()
        summon.currentMp = summon.maxMp.toDouble()
        summon.heading = caster.heading
        summon.setRunning()
        caster.pet = summon

        val x = caster.x
        val y = caster.y
        val z = caster.z

        summon.spawnMe(GeoEngine.canMoveToTargetLoc(x, y, z, x + 20, y + 20, z))
        summon.followStatus = true
    }

    companion object {
        const val SKILL_CUBIC_MASTERY = 143
    }
}