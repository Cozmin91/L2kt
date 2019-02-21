package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.SetSummonRemainTime
import com.l2kt.gameserver.skills.l2skills.L2SkillSummon
import com.l2kt.gameserver.taskmanager.DecayTaskManager
import java.util.concurrent.Future

open class Servitor(objectId: Int, template: NpcTemplate, owner: Player, skill: L2Skill?) :
    Summon(objectId, template, owner) {
    var expPenalty = 0f
    var itemConsumeId = 0
        private set
    var itemConsumeCount = 0
        private set
    var itemConsumeSteps = 0
        private set
    var totalLifeTime = 1200000
        private set
    var timeLostIdle = 1000
        private set
    var timeLostActive = 1000
        private set
    var timeRemaining: Int = 0
        private set
    var nextItemConsumeTime: Int = 0

    var lastShowntimeRemaining: Int = 0

    private var _summonLifeTask: Future<*>? = null

    init {

        if (skill != null) {
            val summonSkill = skill as L2SkillSummon?
            itemConsumeId = summonSkill!!.itemConsumeIdOT
            itemConsumeCount = summonSkill.itemConsumeOT
            itemConsumeSteps = summonSkill.itemConsumeSteps
            totalLifeTime = summonSkill.totalLifeTime
            timeLostIdle = summonSkill.timeLostIdle
            timeLostActive = summonSkill.timeLostActive
        }
        timeRemaining = totalLifeTime
        lastShowntimeRemaining = totalLifeTime

        if (itemConsumeId == 0 || itemConsumeSteps == 0)
            nextItemConsumeTime = -1 // do not consume
        else
            nextItemConsumeTime = totalLifeTime - totalLifeTime / (itemConsumeSteps + 1)

        _summonLifeTask = ThreadPool.scheduleAtFixedRate(SummonLifetime(getOwner(), this), 1000, 1000)
    }

    override fun getLevel(): Int {
        return (if (template != null) template.level else 0).toInt()
    }

    override fun getSummonType(): Int {
        return 1
    }

    fun decNextItemConsumeTime(value: Int) {
        nextItemConsumeTime -= value
    }

    fun decTimeRemaining(value: Int) {
        timeRemaining -= value
    }

    fun addExpAndSp(addToExp: Int, addToSp: Int) {
        owner.addExpAndSp(addToExp.toLong(), addToSp)
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        // Send aggro of mobs to summoner.
        for (mob in getKnownType(Attackable::class.java)) {
            if (mob.isDead)
                continue

            val info = mob.aggroList[this]
            if (info != null)
                mob.addDamageHate(owner, info.damage, info.hate)
        }

        // Popup for summon if phoenix buff was on
        if (isPhoenixBlessed)
            owner.reviveRequest(owner, null, true)

        DecayTaskManager.add(this, template.corpseTime)

        if (_summonLifeTask != null) {
            _summonLifeTask!!.cancel(false)
            _summonLifeTask = null
        }
        return true

    }

    override fun unSummon(owner: Player) {
        if (_summonLifeTask != null) {
            _summonLifeTask!!.cancel(false)
            _summonLifeTask = null
        }
        super.unSummon(owner)
    }

    override fun destroyItem(
        process: String,
        objectId: Int,
        count: Int,
        reference: WorldObject,
        sendMessage: Boolean
    ): Boolean {
        return owner.destroyItem(process, objectId, count, reference, sendMessage)
    }

    override fun destroyItemByItemId(
        process: String,
        itemId: Int,
        count: Int,
        reference: WorldObject,
        sendMessage: Boolean
    ): Boolean {
        return owner.destroyItemByItemId(process, itemId, count, reference, sendMessage)
    }

    override fun doPickupItem(`object`: WorldObject) {}

    private class SummonLifetime(private val _player: Player, private val _summon: Servitor) : Runnable {

        override fun run() {
            val oldTimeRemaining = _summon.timeRemaining.toDouble()
            val maxTime = _summon.totalLifeTime
            val newTimeRemaining: Double = _summon.timeRemaining.toDouble()

            // if pet is attacking
            if (_summon.isAttackingNow)
                _summon.decTimeRemaining(_summon.timeLostActive)
            else
                _summon.decTimeRemaining(_summon.timeLostIdle)

            // check if the summon's lifetime has ran out
            if (newTimeRemaining < 0)
                _summon.unSummon(_player)
            else if (newTimeRemaining <= _summon.nextItemConsumeTime && oldTimeRemaining > _summon.nextItemConsumeTime) {
                _summon.decNextItemConsumeTime(maxTime / (_summon.itemConsumeSteps + 1))

                // check if owner has enought itemConsume, if requested
                if (_summon.itemConsumeCount > 0 && _summon.itemConsumeId != 0 && !_summon.isDead && !_summon.destroyItemByItemId(
                        "Consume",
                        _summon.itemConsumeId,
                        _summon.itemConsumeCount,
                        _player,
                        true
                    )
                )
                    _summon.unSummon(_player)
            }

            // prevent useless packet-sending when the difference isn't visible.
            if (_summon.lastShowntimeRemaining - newTimeRemaining > maxTime / 352) {
                _player.sendPacket(SetSummonRemainTime(maxTime, newTimeRemaining.toInt()))
                _summon.lastShowntimeRemaining = newTimeRemaining.toInt()
                _summon.updateEffectIcons()
            }
        }
    }
}