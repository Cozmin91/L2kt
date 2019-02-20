package com.l2kt.gameserver.model.actor.status

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.stat.CreatureStat
import com.l2kt.gameserver.skills.Formulas
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

open class CreatureStatus(open val activeChar: Creature) {

    private val _statusListener = ConcurrentHashMap.newKeySet<Creature>()

    private var _currentHp = 0.0
    private var _currentMp = 0.0

    private var _regTask: Future<*>? = null
    protected var _flagsRegenActive: Byte = 0

    /**
     * @return The list of Creature to inform, or null if empty.
     */
    val statusListener: Set<Creature>
        get() = _statusListener

    open var currentCp: Double
        get() = 0.0
        set(newCp) {}

    var currentHp: Double
        get() = _currentHp
        set(newHp) = setCurrentHp(newHp, true)

    var currentMp: Double
        get() = _currentMp
        set(newMp) = setCurrentMp(newMp, true)

    /**
     * Add the object to the list of Creature that must be informed of HP/MP updates of this Creature.
     * @param object : Creature to add to the listener.
     */
    fun addStatusListener(`object`: Creature) {
        if (`object` === activeChar)
            return

        _statusListener.add(`object`)
    }

    /**
     * Remove the object from the list of Creature that must be informed of HP/MP updates of this Creature.
     * @param object : Creature to remove to the listener.
     */
    fun removeStatusListener(`object`: Creature) {
        _statusListener.remove(`object`)
    }

    open fun reduceCp(value: Int) {}

    /**
     * Reduce the current HP of the Creature and launch the doDie Task if necessary.
     * @param value : The amount of removed HPs.
     * @param attacker : The Creature who attacks.
     */
    open fun reduceHp(value: Double, attacker: Creature) {
        reduceHp(value, attacker, true, false, false)
    }

    fun reduceHp(value: Double, attacker: Creature, isHpConsumption: Boolean) {
        reduceHp(value, attacker, true, false, isHpConsumption)
    }

    open fun reduceHp(value: Double, attacker: Creature?, awake: Boolean, isDOT: Boolean, isHPConsumption: Boolean) {
        if (activeChar.isDead)
            return

        // invul handling
        if (activeChar.isInvul) {
            // other chars can't damage
            if (attacker !== activeChar)
                return

            // only DOT and HP consumption allowed for damage self
            if (!isDOT && !isHPConsumption)
                return
        }

        if (attacker != null) {
            val attackerPlayer = attacker.actingPlayer
            if (attackerPlayer != null && attackerPlayer.isGM && !attackerPlayer.accessLevel.canGiveDamage)
                return
        }

        if (!isDOT && !isHPConsumption) {
            activeChar.stopEffectsOnDamage(awake)

            if (activeChar.isStunned && Rnd[10] == 0)
                activeChar.stopStunning(true)

            if (activeChar.isImmobileUntilAttacked)
                activeChar.stopImmobileUntilAttacked(null)
        }

        if (value > 0)
        // Reduce Hp if any
            currentHp = Math.max(currentHp - value, 0.0)

        // Die if character is mortal
        if (activeChar.currentHp < 0.5 && activeChar.isMortal) {
            activeChar.abortAttack()
            activeChar.abortCast()

            activeChar.doDie(attacker)
        }
    }

    fun reduceMp(value: Double) {
        currentMp = Math.max(currentMp - value, 0.0)
    }

    /**
     * Start the HP/MP/CP Regeneration task.
     */
    @Synchronized
    fun startHpMpRegeneration() {
        if (_regTask == null && !activeChar.isDead) {
            // Get the regeneration period.
            val period = Formulas.getRegeneratePeriod(activeChar)

            // Create the HP/MP/CP regeneration task.
            _regTask = ThreadPool.scheduleAtFixedRate(Runnable{ doRegeneration() }, period.toLong(), period.toLong())
        }
    }

    /**
     * Stop the HP/MP/CP Regeneration task.
     */
    @Synchronized
    fun stopHpMpRegeneration() {
        if (_regTask != null) {
            // Stop the HP/MP/CP regeneration task.
            _regTask!!.cancel(false)
            _regTask = null

            // Set the RegenActive flag to false.
            _flagsRegenActive = 0
        }
    }

    open fun setCurrentHp(newHp: Double, broadcastPacket: Boolean) {
        val maxHp = activeChar.maxHp.toDouble()

        synchronized(this) {
            if (activeChar.isDead)
                return

            if (newHp >= maxHp) {
                // Set the RegenActive flag to false
                _currentHp = maxHp
                _flagsRegenActive = _flagsRegenActive and REGEN_FLAG_HP.inv().toByte()

                // Stop the HP/MP/CP Regeneration task
                if (_flagsRegenActive.toInt() == 0)
                    stopHpMpRegeneration()
            } else {
                // Set the RegenActive flag to true
                _currentHp = newHp
                _flagsRegenActive = _flagsRegenActive or REGEN_FLAG_HP

                // Start the HP/MP/CP Regeneration task with Medium priority
                startHpMpRegeneration()
            }
        }

        if (broadcastPacket)
            activeChar.broadcastStatusUpdate()
    }

    fun setCurrentHpMp(newHp: Double, newMp: Double) {
        setCurrentHp(newHp, false)
        setCurrentMp(newMp, true)
    }

    fun setCurrentMp(newMp: Double, broadcastPacket: Boolean) {
        val maxMp = activeChar.stat.maxMp

        synchronized(this) {
            if (activeChar.isDead)
                return

            if (newMp >= maxMp) {
                // Set the RegenActive flag to false
                _currentMp = maxMp.toDouble()
                _flagsRegenActive = _flagsRegenActive and REGEN_FLAG_MP.inv()

                // Stop the HP/MP/CP Regeneration task
                if (_flagsRegenActive.toInt() == 0)
                    stopHpMpRegeneration()
            } else {
                // Set the RegenActive flag to true
                _currentMp = newMp
                _flagsRegenActive = _flagsRegenActive or REGEN_FLAG_MP

                // Start the HP/MP/CP Regeneration task with Medium priority
                startHpMpRegeneration()
            }
        }

        if (broadcastPacket)
            activeChar.broadcastStatusUpdate()
    }

    protected open fun doRegeneration() {
        val charstat = activeChar.stat

        // Modify the current HP of the Creature.
        if (currentHp < charstat.maxHp)
            setCurrentHp(currentHp + Formulas.calcHpRegen(activeChar), false)

        // Modify the current MP of the Creature.
        if (currentMp < charstat.maxMp)
            setCurrentMp(currentMp + Formulas.calcMpRegen(activeChar), false)

        // Send the StatusUpdate packet.
        activeChar.broadcastStatusUpdate()
    }

    companion object {

        const val REGEN_FLAG_CP: Byte = 4
        private const val REGEN_FLAG_HP: Byte = 1
        private const val REGEN_FLAG_MP: Byte = 2
    }
}