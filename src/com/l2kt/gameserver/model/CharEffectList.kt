package com.l2kt.gameserver.model

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AbnormalStatusUpdate
import com.l2kt.gameserver.network.serverpackets.ExOlympiadSpelledInfo
import com.l2kt.gameserver.network.serverpackets.PartySpelled
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class CharEffectList(// Owner of this list
    private val _owner: Creature?
) {

    private var _buffs: MutableList<L2Effect> = mutableListOf()
    private var _debuffs: MutableList<L2Effect> = mutableListOf()

    // The table containing the List of all stacked effect in progress for each Stack group Identifier
    private var _stackedEffects: MutableMap<String, MutableList<L2Effect>> = mutableMapOf()

    @Volatile
    private var _hasBuffsRemovedOnAnyAction = false
    @Volatile
    private var _hasBuffsRemovedOnDamage = false
    @Volatile
    private var _hasDebuffsRemovedOnDamage = false

    private var _addQueue: LinkedBlockingQueue<L2Effect> = LinkedBlockingQueue()
    private var _removeQueue: LinkedBlockingQueue<L2Effect> = LinkedBlockingQueue()
    private val queueLock = AtomicBoolean()
    private var _effectFlags: Int = 0

    // only party icons need to be updated
    private var _partyOnly = false

    private var _effectCache: Array<L2Effect> = arrayOf()
    @Volatile
    private var _rebuildCache = true
    private val _buildEffectLock = Any()

    /**
     * Returns all effects affecting stored in this CharEffectList
     * @return
     */
    // If no effect is active, return EMPTY_EFFECTS
    // If we dont need to rebuild the cache, just return the current one.
    // Create a copy of the effects
    // Add all buffs and all debuffs
    // Return all effects in an array
    val allEffects: Array<L2Effect>
        get() {
            if ((_buffs == null || _buffs!!.isEmpty()) && (_debuffs == null || _debuffs!!.isEmpty()))
                return EMPTY_EFFECTS

            synchronized(_buildEffectLock) {
                if (!_rebuildCache)
                    return _effectCache

                _rebuildCache = false
                val temp = ArrayList<L2Effect>()
                if (_buffs != null && !_buffs!!.isEmpty())
                    temp.addAll(_buffs!!)
                if (_debuffs != null && !_debuffs!!.isEmpty())
                    temp.addAll(_debuffs!!)
                val tempArray = arrayOfNulls<L2Effect>(temp.size)
                temp.toArray(tempArray)
                _effectCache = tempArray.filterNotNull().toTypedArray()
                return _effectCache
            }
        }

    /**
     * Return the number of buffs in this CharEffectList not counting Songs/Dances
     * @return
     */
    val buffCount: Int
        get() {
            if (_buffs == null || _buffs!!.isEmpty())
                return 0

            var buffCount = 0
            for (e in _buffs!!) {
                if (e != null && e.showIcon && !e.skill.is7Signs) {
                    when (e.skill.skillType) {
                        L2SkillType.BUFF, L2SkillType.COMBATPOINTHEAL, L2SkillType.REFLECT, L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT -> buffCount++
                    }
                }
            }
            return buffCount
        }

    /**
     * Return the number of Songs/Dances in this CharEffectList
     * @return
     */
    val danceCount: Int
        get() {
            if (_buffs == null || _buffs!!.isEmpty())
                return 0

            var danceCount = 0
            for (e in _buffs!!) {
                if (e != null && e.skill.isDance && e.inUse)
                    danceCount++
            }
            return danceCount
        }

    /**
     * Returns the first effect matching the given EffectType
     * @param tp
     * @return
     */
    fun getFirstEffect(tp: L2EffectType): L2Effect? {
        var effectNotInUse: L2Effect? = null

        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e == null)
                    continue

                if (e.effectType === tp) {
                    if (e.inUse)
                        return e

                    effectNotInUse = e
                }
            }
        }

        if (effectNotInUse == null && _debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e == null)
                    continue

                if (e.effectType === tp) {
                    if (e.inUse)
                        return e

                    effectNotInUse = e
                }
            }
        }
        return effectNotInUse
    }

    /**
     * Returns the first effect matching the given L2Skill
     * @param skill
     * @return
     */
    fun getFirstEffect(skill: L2Skill): L2Effect? {
        var effectNotInUse: L2Effect? = null

        if (skill.isDebuff) {
            if (_debuffs != null && !_debuffs!!.isEmpty()) {
                for (e in _debuffs!!) {
                    if (e == null)
                        continue

                    if (e.skill === skill) {
                        if (e.inUse)
                            return e

                        effectNotInUse = e
                    }
                }
            }
        } else {
            if (_buffs != null && !_buffs!!.isEmpty()) {
                for (e in _buffs!!) {
                    if (e == null)
                        continue

                    if (e.skill === skill) {
                        if (e.inUse)
                            return e

                        effectNotInUse = e
                    }
                }
            }
        }
        return effectNotInUse
    }

    /**
     * @param skillId The skill id to check.
     * @return the first effect matching the given skillId.
     */
    fun getFirstEffect(skillId: Int): L2Effect? {
        var effectNotInUse: L2Effect? = null

        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e == null)
                    continue

                if (e.skill.id == skillId) {
                    if (e.inUse)
                        return e

                    effectNotInUse = e
                }
            }
        }

        if (effectNotInUse == null && _debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e == null)
                    continue
                if (e.skill.id == skillId) {
                    if (e.inUse)
                        return e

                    effectNotInUse = e
                }
            }
        }
        return effectNotInUse
    }

    /**
     * Checks if the given skill stacks with an existing one.
     * @param checkSkill the skill to be checked
     * @return Returns whether or not this skill will stack
     */
    private fun doesStack(checkSkill: L2Skill): Boolean {
        if (_buffs == null || _buffs!!.isEmpty())
            return false

        if (checkSkill._effectTemplates == null || checkSkill._effectTemplates.isEmpty())
            return false

        val stackType = checkSkill._effectTemplates[0].stackType
        if (stackType == null || "none" == stackType)
            return false

        for (e in _buffs!!) {
            if (e.stackType != null && e.stackType == stackType)
                return true
        }
        return false
    }

    /**
     * Exits all effects in this CharEffectList
     */
    fun stopAllEffects() {
        // Get all active skills effects from this list
        val effects = allEffects

        // Exit them
        for (e in effects!!) {
            e?.exit(true)
        }
    }

    /**
     * Exits all effects in this CharEffectList
     */
    fun stopAllEffectsExceptThoseThatLastThroughDeath() {
        // Get all active skills effects from this list
        val effects = allEffects

        // Exit them
        for (e in effects!!) {
            if (e != null && !e.skill.isStayAfterDeath)
                e.exit(true)
        }
    }

    /**
     * Exit all toggle-type effects
     */
    fun stopAllToggles() {
        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e != null && e.skill.isToggle)
                    e.exit()
            }
        }
    }

    /**
     * Exit all effects having a specified type
     * @param type
     */
    fun stopEffects(type: L2EffectType) {
        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                // Get active skills effects of the selected type
                if (e != null && e.effectType === type)
                    e.exit()
            }
        }

        if (_debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                // Get active skills effects of the selected type
                if (e != null && e.effectType === type)
                    e.exit()
            }
        }
    }

    /**
     * Exits all effects created by a specific skillId
     * @param skillId
     */
    fun stopSkillEffects(skillId: Int) {
        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e != null && e.skill.id == skillId)
                    e.exit()
            }
        }

        if (_debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e != null && e.skill.id == skillId)
                    e.exit()
            }
        }
    }

    /**
     * Exits all effects created by a specific skill type
     * @param skillType skill type
     * @param negateLvl
     */
    fun stopSkillEffects(skillType: L2SkillType, negateLvl: Int) {
        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e != null && (e.skill.skillType === skillType || e.skill.effectType != null && e.skill.effectType === skillType) && (negateLvl == -1 || e.skill.effectType != null && e.skill.effectAbnormalLvl >= 0 && e.skill.effectAbnormalLvl <= negateLvl || e.skill.abnormalLvl >= 0 && e.skill.abnormalLvl <= negateLvl))
                    e.exit()
            }
        }

        if (_debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e != null && (e.skill.skillType === skillType || e.skill.effectType != null && e.skill.effectType === skillType) && (negateLvl == -1 || e.skill.effectType != null && e.skill.effectAbnormalLvl >= 0 && e.skill.effectAbnormalLvl <= negateLvl || e.skill.abnormalLvl >= 0 && e.skill.abnormalLvl <= negateLvl))
                    e.exit()
            }
        }
    }

    /**
     * Exits all buffs effects of the skills with "removedOnAnyAction" set. Called on any action except movement (attack, cast).
     */
    fun stopEffectsOnAction() {
        if (_hasBuffsRemovedOnAnyAction) {
            if (_buffs != null && !_buffs!!.isEmpty()) {
                for (e in _buffs!!) {
                    if (e != null && e.skill.isRemovedOnAnyActionExceptMove)
                        e.exit(true)
                }
            }
        }
    }

    fun stopEffectsOnDamage(awake: Boolean) {
        if (_hasBuffsRemovedOnDamage) {
            if (_buffs != null && !_buffs!!.isEmpty()) {
                for (e in _buffs!!) {
                    if (e != null && e.skill.isRemovedOnDamage && (awake || e.skill.skillType !== L2SkillType.SLEEP))
                        e.exit(true)
                }
            }
        }

        if (_hasDebuffsRemovedOnDamage) {
            if (_debuffs != null && !_debuffs!!.isEmpty()) {
                for (e in _debuffs!!) {
                    if (e != null && e.skill.isRemovedOnDamage && (awake || e.skill.skillType !== L2SkillType.SLEEP))
                        e.exit(true)
                }
            }
        }
    }

    fun updateEffectIcons(partyOnly: Boolean) {
        if (_buffs == null && _debuffs == null)
            return

        if (partyOnly)
            _partyOnly = true

        queueRunner()
    }

    fun queueEffect(effect: L2Effect?, remove: Boolean) {
        if (effect == null)
            return

        if (remove)
            _removeQueue!!.offer(effect)
        else
            _addQueue!!.offer(effect)

        queueRunner()
    }

    private fun queueRunner() {
        if (!queueLock.compareAndSet(false, true))
            return

        try {
            do {
                // remove has more priority than add so removing all effects from queue first


                while (_removeQueue!!.peek() != null) {
                    val effect = _removeQueue!!.poll()
                    removeEffectFromQueue(effect)
                    _partyOnly = false
                }


                if ((_addQueue.peek()) != null) {
                    val effect = _addQueue!!.poll()
                    addEffectFromQueue(effect)
                    _partyOnly = false
                }
            } while (!_addQueue!!.isEmpty() || !_removeQueue!!.isEmpty())

            computeEffectFlags()
            updateEffectIcons()
        } finally {
            queueLock.set(false)
        }
    }

    protected fun removeEffectFromQueue(effect: L2Effect?) {
        if (effect == null)
            return

        val effectList: MutableList<L2Effect>

        // array modified, then rebuild on next request
        _rebuildCache = true

        if (effect.skill.isDebuff) {
            if (_debuffs == null)
                return

            effectList = _debuffs
        } else {
            if (_buffs == null)
                return

            effectList = _buffs
        }

        if ("none" == effect.stackType) {
            // Remove Func added by this effect from the Creature Calculator
            _owner!!.removeStatsByOwner(effect)
        } else {
            if (_stackedEffects == null)
                return

            // Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
            val stackQueue = _stackedEffects!![effect.stackType]

            if (stackQueue == null || stackQueue.isEmpty())
                return

            val index = stackQueue.indexOf(effect)

            // Remove the effect from the stack group
            if (index >= 0) {
                stackQueue.remove(effect)
                // Check if the first stacked effect was the effect to remove
                if (index == 0) {
                    // Remove all its Func objects from the Creature calculator set
                    _owner!!.removeStatsByOwner(effect)

                    // Check if there's another effect in the Stack Group
                    if (!stackQueue.isEmpty()) {
                        val newStackedEffect = listsContains(stackQueue[0])
                        if (newStackedEffect != null) {
                            // Set the effect to In Use
                            if (newStackedEffect.setInUse(true))
                            // Add its list of Funcs to the Calculator set of the Creature
                                _owner.addStatFuncs(newStackedEffect.statFuncs)
                        }
                    }
                }

                if (stackQueue.isEmpty())
                    _stackedEffects!!.remove(effect.stackType)
                else
                // Update the Stack Group table _stackedEffects of the Creature
                    _stackedEffects!![effect.stackType] = stackQueue
            }
        }

        // Remove the active skill L2effect from _effects of the Creature
        if (effectList.remove(effect) && _owner is Player && effect.showIcon) {
            val sm: SystemMessage
            if (effect.skill.isToggle)
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED)
            else
                sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED)

            sm.addSkillName(effect)
            _owner.sendPacket(sm)
        }
    }

    protected fun addEffectFromQueue(newEffect: L2Effect?) {
        if (newEffect == null)
            return

        val newSkill = newEffect.skill

        // array modified, then rebuild on next request
        _rebuildCache = true

        if (isAffected(newEffect.effectFlags) && !newEffect.onSameEffect(null)) {
            newEffect.stopEffectTask()
            return
        }

        if (newSkill.isDebuff) {
            if (_debuffs == null)
                _debuffs = CopyOnWriteArrayList()

            for (e in _debuffs!!) {
                if (e != null && e.skill.id == newEffect.skill.id && e.effectType === newEffect.effectType && e.stackOrder == newEffect.stackOrder && e.stackType == newEffect.stackType) {
                    // Started scheduled timer needs to be canceled.
                    newEffect.stopEffectTask()
                    return
                }
            }
            _debuffs!!.add(newEffect)
        } else {
            if (_buffs == null)
                _buffs = CopyOnWriteArrayList()

            for (e in _buffs!!) {
                if (e != null && e.skill.id == newEffect.skill.id && e.effectType === newEffect.effectType && e.stackOrder == newEffect.stackOrder && e.stackType == newEffect.stackType) {
                    e.exit() // exit this
                }
            }

            // if max buffs, no herb effects are used, even if they would replace one old
            if (newEffect.isHerbEffect && buffCount >= _owner!!.maxBuffCount) {
                newEffect.stopEffectTask()
                return
            }

            // Remove first buff when buff list is full
            if (!doesStack(newSkill) && !newSkill.is7Signs) {
                var effectsToRemove = buffCount - _owner!!.maxBuffCount
                if (effectsToRemove >= 0) {
                    when (newSkill.skillType) {
                        L2SkillType.BUFF, L2SkillType.REFLECT, L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT, L2SkillType.COMBATPOINTHEAL -> for (e in _buffs) {

                            if (e.skill.skillType == L2SkillType.BUFF || e.skill.skillType == L2SkillType.REFLECT || e.skill.skillType == L2SkillType.HEAL_PERCENT || e.skill.skillType == L2SkillType.MANAHEAL_PERCENT || e.skill.skillType == L2SkillType.COMBATPOINTHEAL) {
                                e.exit()
                                effectsToRemove--
                            }
                            else continue // continue for()
                            // break switch()
                            if (effectsToRemove < 0)
                                break // break for()
                        }
                    }
                }
            }

            // Icons order: buffs then toggles
            if (newSkill.isToggle)
                _buffs!!.add(newEffect)
            else {
                var pos = 0
                for (e in _buffs!!) {
                    if (e == null || e.skill.isToggle || e.skill.is7Signs)
                        continue

                    pos++
                }
                _buffs!!.add(pos, newEffect)
            }
        }

        // Check if a stack group is defined for this effect
        if ("none" == newEffect.stackType) {
            // Set this L2Effect to In Use
            if (newEffect.setInUse(true))
            // Add Funcs of this effect to the Calculator set of the Creature
                _owner!!.addStatFuncs(newEffect.statFuncs)

            return
        }

        var stackQueue: MutableList<L2Effect>?
        var effectToAdd: L2Effect? = null
        var effectToRemove: L2Effect? = null
        if (_stackedEffects == null)
            _stackedEffects = HashMap()

        // Get the list of all stacked effects corresponding to the stack type of the L2Effect to add
        stackQueue = _stackedEffects!![newEffect.stackType]

        if (stackQueue != null) {
            var pos = 0
            if (!stackQueue.isEmpty()) {
                // Get the first stacked effect of the Stack group selected
                effectToRemove = listsContains(stackQueue[0])

                // Create an Iterator to go through the list of stacked effects in progress on the Creature
                val queueIterator = stackQueue.iterator()

                while (queueIterator.hasNext()) {
                    if (newEffect.stackOrder < queueIterator.next().stackOrder)
                        pos++
                    else
                        break
                }
                // Add the new effect to the Stack list in function of its position in the Stack group
                stackQueue.add(pos, newEffect)

                // skill.exit() could be used, if the users don't wish to see "effect
                // removed" always when a timer goes off, even if the buff isn't active
                // any more (has been replaced). but then check e.g. npc hold and raid petrification.
                if (Config.EFFECT_CANCELING && !newEffect.isHerbEffect && stackQueue.size > 1) {
                    if (newSkill.isDebuff)
                        _debuffs!!.remove(stackQueue.removeAt(1))
                    else
                        _buffs!!.remove(stackQueue.removeAt(1))
                }
            } else
                stackQueue.add(0, newEffect)
        } else {
            stackQueue = ArrayList()
            stackQueue.add(0, newEffect)
        }

        // Update the Stack Group table _stackedEffects of the Creature
        _stackedEffects!![newEffect.stackType] = stackQueue

        // Get the first stacked effect of the Stack group selected
        if (!stackQueue.isEmpty())
            effectToAdd = listsContains(stackQueue[0])

        if (effectToRemove !== effectToAdd) {
            if (effectToRemove != null) {
                // Remove all Func objects corresponding to this stacked effect from the Calculator set of the Creature
                _owner!!.removeStatsByOwner(effectToRemove)

                // Set the L2Effect to Not In Use
                effectToRemove.inUse = false
            }

            if (effectToAdd != null) {
                // Set this L2Effect to In Use
                if (effectToAdd.setInUse(true))
                // Add all Func objects corresponding to this stacked effect to the Calculator set of the Creature
                    _owner!!.addStatFuncs(effectToAdd.statFuncs)
            }
        }
    }

    protected fun updateEffectIcons() {
        if (_owner == null)
            return

        if (_owner !is Playable) {
            updateEffectFlags()
            return
        }

        var mi: AbnormalStatusUpdate? = null
        var ps: PartySpelled? = null
        var os: ExOlympiadSpelledInfo? = null

        if (_owner is Player) {
            if (_partyOnly)
                _partyOnly = false
            else
                mi = AbnormalStatusUpdate()

            if (_owner.isInParty)
                ps = PartySpelled(_owner)

            if (_owner.isInOlympiadMode && _owner.isOlympiadStart)
                os = ExOlympiadSpelledInfo((_owner as Player?)!!)
        } else if (_owner is Summon)
            ps = PartySpelled(_owner)

        var foundRemovedOnAction = false
        var foundRemovedOnDamage = false

        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e == null)
                    continue

                if (e.skill.isRemovedOnAnyActionExceptMove)
                    foundRemovedOnAction = true
                if (e.skill.isRemovedOnDamage)
                    foundRemovedOnDamage = true

                if (!e.showIcon)
                    continue

                if (e.effectType == L2EffectType.SIGNET_GROUND) continue

                if (e.inUse) {
                    if (mi != null)
                        e.addIcon(mi)

                    if (ps != null)
                        e.addPartySpelledIcon(ps)

                    if (os != null)
                        e.addOlympiadSpelledIcon(os)
                }
            }
        }

        _hasBuffsRemovedOnAnyAction = foundRemovedOnAction
        _hasBuffsRemovedOnDamage = foundRemovedOnDamage
        foundRemovedOnDamage = false

        if (_debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e == null)
                    continue

                if (e.skill.isRemovedOnAnyActionExceptMove)
                    foundRemovedOnAction = true
                if (e.skill.isRemovedOnDamage)
                    foundRemovedOnDamage = true

                if (!e.showIcon)
                    continue

                if (e.effectType == L2EffectType.SIGNET_GROUND) continue

                if (e.inUse) {
                    if (mi != null)
                        e.addIcon(mi)

                    if (ps != null)
                        e.addPartySpelledIcon(ps)

                    if (os != null)
                        e.addOlympiadSpelledIcon(os)
                }
            }
        }

        _hasDebuffsRemovedOnDamage = foundRemovedOnDamage

        if (mi != null)
            _owner.sendPacket(mi)

        if (ps != null) {
            if (_owner is Summon) {
                val summonOwner = _owner.owner
                if (summonOwner != null) {
                    val party = summonOwner.party
                    if (party != null)
                        party.broadcastPacket(ps)
                    else
                        summonOwner.sendPacket(ps)
                }
            } else if (_owner is Player && _owner.isInParty)
                _owner.party!!.broadcastPacket(ps)
        }

        if (os != null) {
            val game = OlympiadGameManager.getOlympiadTask((_owner as Player).olympiadGameId)
            if (game != null && game.isBattleStarted)
                game.zone.broadcastPacketToObservers(os)
        }
    }

    protected fun updateEffectFlags() {
        var foundRemovedOnAction = false
        var foundRemovedOnDamage = false

        if (_buffs != null && !_buffs!!.isEmpty()) {
            for (e in _buffs!!) {
                if (e == null)
                    continue

                if (e.skill.isRemovedOnAnyActionExceptMove)
                    foundRemovedOnAction = true
                if (e.skill.isRemovedOnDamage)
                    foundRemovedOnDamage = true
            }
        }
        _hasBuffsRemovedOnAnyAction = foundRemovedOnAction
        _hasBuffsRemovedOnDamage = foundRemovedOnDamage
        foundRemovedOnDamage = false

        if (_debuffs != null && !_debuffs!!.isEmpty()) {
            for (e in _debuffs!!) {
                if (e == null)
                    continue

                if (e.skill.isRemovedOnDamage)
                    foundRemovedOnDamage = true
            }
        }
        _hasDebuffsRemovedOnDamage = foundRemovedOnDamage
    }

    /**
     * Returns effect if contains in _buffs or _debuffs and null if not found
     * @param effect
     * @return
     */
    private fun listsContains(effect: L2Effect): L2Effect? {
        if (_buffs != null && !_buffs!!.isEmpty() && _buffs!!.contains(effect))
            return effect
        return if (_debuffs != null && !_debuffs!!.isEmpty() && _debuffs!!.contains(effect)) effect else null

    }

    /**
     * Recalculate effect bits flag.<br></br>
     * Please no concurrency access
     */
    private fun computeEffectFlags() {
        var flags = 0

        if (_buffs != null) {
            for (e in _buffs!!) {
                if (e == null)
                    continue

                flags = flags or e.effectFlags
            }
        }

        if (_debuffs != null) {
            for (e in _debuffs!!) {
                if (e == null)
                    continue

                flags = flags or e.effectFlags
            }
        }

        _effectFlags = flags
    }

    /**
     * Check if target is affected with special buff
     * @param flag flag of special buff
     * @return boolean true if affected
     */
    fun isAffected(flag: L2EffectFlag): Boolean {
        return isAffected(flag.mask)
    }

    fun isAffected(mask: Int): Boolean {
        return _effectFlags and mask != 0
    }

    /**
     * Clear and null all queues and lists Use only during delete character from the world.
     */
    fun clear() {
        _addQueue = LinkedBlockingQueue()
        _removeQueue = LinkedBlockingQueue()
        _buffs = mutableListOf()
        _debuffs = mutableListOf()
        _stackedEffects = mutableMapOf()
    }

    companion object {
        private val EMPTY_EFFECTS = arrayOf<L2Effect>()
    }
}