package com.l2kt.gameserver.model.actor.status

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.entity.Duel.DuelState
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

class PlayerStatus(activeChar: Player) : PlayableStatus(activeChar) {
    private var _currentCp = 0.0

    override var currentCp: Double
        get() = _currentCp
        set(newCp) = setCurrentCp(newCp, true)

    override fun reduceCp(value: Int) {
        if (currentCp > value)
            currentCp -= value
        else
            currentCp = 0.0
    }

    override fun reduceHp(value: Double, attacker: Creature) {
        reduceHp(value, attacker, true, false, false, false)
    }

    override fun reduceHp(
        value: Double,
        attacker: Creature?,
        awake: Boolean,
        isDOT: Boolean,
        isHPConsumption: Boolean
    ) {
        reduceHp(value, attacker, awake, isDOT, isHPConsumption, false)
    }

    fun reduceHp(
        value: Double,
        attacker: Creature?,
        awake: Boolean,
        isDOT: Boolean,
        isHPConsumption: Boolean,
        ignoreCP: Boolean
    ) {
        var value = value
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

        if (!isHPConsumption) {
            activeChar.stopEffectsOnDamage(awake)
            activeChar.forceStandUp()

            if (!isDOT) {
                if (activeChar.isStunned && Rnd[10] == 0)
                    activeChar.stopStunning(true)
            }
        }

        if (attacker != null && attacker !== activeChar) {
            val attackerPlayer = attacker.actingPlayer
            if (attackerPlayer != null) {
                if (attackerPlayer.isGM && !attackerPlayer.accessLevel.canGiveDamage)
                    return
            }

            if (activeChar.isInDuel) {
                val playerState = activeChar.duelState
                if (playerState === DuelState.DEAD || playerState === DuelState.WINNER)
                    return

                // Cancel duel if player got hit by another player that is not part of the duel or if player isn't in duel state.
                if (attackerPlayer == null || attackerPlayer.duelId != activeChar.duelId || playerState !== DuelState.DUELLING)
                    activeChar.duelState = DuelState.INTERRUPTED
            }

            var fullValue = value.toInt()
            var tDmg = 0

            // Check and calculate transfered damage
            val summon = activeChar.pet
            if (summon != null && summon is Servitor && MathUtil.checkIfInRange(900, activeChar, summon, true)) {
                tDmg = value.toInt() * activeChar.stat.calcStat(
                    Stats.TRANSFER_DAMAGE_PERCENT,
                    0.0,
                    null,
                    null
                ).toInt() / 100

                // Only transfer dmg up to current HP, it should not be killed
                tDmg = Math.min(summon.currentHp.toInt() - 1, tDmg)
                if (tDmg > 0) {
                    summon.reduceCurrentHp(tDmg.toDouble(), attacker, null)
                    value -= tDmg.toDouble()
                    fullValue =
                        value.toInt() // reduce the announced value here as player will get a message about summon damage
                }
            }

            if (!ignoreCP && attacker is Playable) {
                if (currentCp >= value) {
                    currentCp -= value // Set Cp to diff of Cp vs value
                    value = 0.0 // No need to subtract anything from Hp
                } else {
                    value -= currentCp // Get diff from value vs Cp; will apply diff to Hp
                    setCurrentCp(0.0, false) // Set Cp to 0
                }
            }

            if (fullValue > 0 && !isDOT) {
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(
                        attacker
                    ).addNumber(fullValue)
                )

                if (tDmg > 0 && attackerPlayer != null)
                    attackerPlayer.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.GIVEN_S1_DAMAGE_TO_YOUR_TARGET_AND_S2_DAMAGE_TO_SERVITOR).addNumber(
                            fullValue
                        ).addNumber(tDmg)
                    )
            }
        }

        if (value > 0) {
            value = currentHp - value
            if (value <= 0) {
                if (activeChar.isInDuel) {
                    if (activeChar.duelState === DuelState.DUELLING) {
                        activeChar.disableAllSkills()
                        stopHpMpRegeneration()

                        if (attacker != null) {
                            attacker.ai.setIntention(CtrlIntention.ACTIVE)
                            attacker.sendPacket(ActionFailed.STATIC_PACKET)
                        }

                        // let the DuelManager know of his defeat
                        DuelManager.onPlayerDefeat(activeChar)
                    }
                    value = 1.0
                } else
                    value = 0.0
            }
            currentHp = value
        }

        if (activeChar.currentHp < 0.5 && activeChar.isMortal) {
            activeChar.abortAttack()
            activeChar.abortCast()

            if (activeChar.isInOlympiadMode) {
                stopHpMpRegeneration()
                activeChar.setIsDead(true)

                if (activeChar.pet != null)
                    activeChar.pet!!.ai.setIntention(CtrlIntention.IDLE, null)

                return
            }

            activeChar.doDie(attacker)

            if (!Config.DISABLE_TUTORIAL) {
                val qs = activeChar.getQuestState("Tutorial")
                qs?.quest?.notifyEvent("CE30", null, activeChar)
            }
        }
    }

    override fun setCurrentHp(newHp: Double, broadcastPacket: Boolean) {
        super.setCurrentHp(newHp, broadcastPacket)

        if (!Config.DISABLE_TUTORIAL && currentHp <= activeChar.stat.maxHp * .3) {
            val qs = activeChar.getQuestState("Tutorial")
            qs?.quest?.notifyEvent("CE45", null, activeChar)
        }
    }

    fun setCurrentCp(newCp: Double, broadcastPacket: Boolean) {
        var newCp = newCp
        val maxCp = activeChar.stat.maxCp

        synchronized(this) {
            if (activeChar.isDead)
                return

            if (newCp < 0)
                newCp = 0.0

            if (newCp >= maxCp) {
                // Set the RegenActive flag to false
                _currentCp = maxCp.toDouble()
                _flagsRegenActive = _flagsRegenActive and CreatureStatus.REGEN_FLAG_CP.inv()

                // Stop the HP/MP/CP Regeneration task
                if (_flagsRegenActive.toInt() == 0)
                    stopHpMpRegeneration()
            } else {
                // Set the RegenActive flag to true
                _currentCp = newCp
                _flagsRegenActive = _flagsRegenActive or CreatureStatus.REGEN_FLAG_CP

                // Start the HP/MP/CP Regeneration task with Medium priority
                startHpMpRegeneration()
            }
        }

        if (broadcastPacket)
            activeChar.broadcastStatusUpdate()
    }

    override fun doRegeneration() {
        val pcStat = activeChar.stat

        // Modify the current CP of the Creature.
        if (currentCp < pcStat.maxCp)
            setCurrentCp(currentCp + Formulas.calcCpRegen(activeChar), false)

        // Modify the current HP of the Creature.
        if (currentHp < pcStat.maxHp)
            setCurrentHp(currentHp + Formulas.calcHpRegen(activeChar), false)

        // Modify the current MP of the Creature.
        if (currentMp < pcStat.maxMp)
            setCurrentMp(currentMp + Formulas.calcMpRegen(activeChar), false)

        // Send the StatusUpdate packet.
        activeChar.broadcastStatusUpdate()
    }

    override val activeChar: Player
        get() = super.activeChar as Player
}