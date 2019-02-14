package com.l2kt.gameserver.model

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.FishingChampionshipManager
import com.l2kt.gameserver.data.xml.FishData
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.instance.PenaltyMonster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.concurrent.Future
import java.util.concurrent.ScheduledFuture

/**
 * This class handles all fishing aspects and variables.<br></br>
 * <br></br>
 * The fishing stance starts with a task named _lookingForFish. This task handles the waiting process, up to the caught time.<br></br>
 * <br></br>
 * Once a [Fish] is found, a new task occurs, named _fishCombat. The [Player] will play a minigame to reduce the resilience of the Fish. If he succeeds, he caught the Fish.
 */
class FishingStance(private val _fisher: Player?) {

    /**
     * @return the bait [Location].
     */
    val loc = Location(0, 0, 0)

    private var _time: Int = 0
    private var _stop: Int = 0
    private var _goodUse: Int = 0
    private var _anim: Int = 0
    private var _mode: Int = 0
    private var _deceptiveMode: Int = 0

    private var _lookingForFish: Future<*>? = null
    private var _fishCombat: Future<*>? = null

    private var _thinking: Boolean = false

    private var _fish: Fish? = null
    private var _fishCurHp: Int = 0

    private var _isUpperGrade: Boolean = false

    private var _lure: ItemInstance? = null
    private var _lureType: Int = 0

    /**
     * @return a fish level, based on multiple factors. It never goes out 1-27, and is slightly impacted by randomness.
     */
    private// Fisherman potion overrides skill level.
    // Also check the presence of Fishing Expertise skill.
    // Random aspect : 35% to get -1 level, 15% to get +1 level.
    // Return a number from 1 to 27, no matter what.
    val randomFishLvl: Int
        get() {
            val effect = _fisher!!.getFirstEffect(2274)
            var level = effect?.skill?.power?.toInt() ?: _fisher.getSkillLevel(1315)
            if (level <= 0)
                return 1
            val check = Rnd[100]
            if (check < 35)
                level--
            else if (check < 50)
                level++
            return MathUtil.limit(level, 1, 27)
        }

    /**
     * @return true if currently looking for fish (waiting stance), false otherwise.
     */
    val isLookingForFish: Boolean
        get() = _lookingForFish != null

    /**
     * @return true if currently fighting fish (minigame), false otherwise.
     */
    val isUnderFishCombat: Boolean
        get() = _fishCombat != null

    /**
     * @param group : The group based on lure type (beginner, normal, upper grade).
     * @return a random [Fish] type, based on randomness and lure's group.
     */
    private fun getRandomFishType(group: Int): Int {
        val check = Rnd[100]
        var type = 1
        when (group) {
            0 // fish for novices
            -> when (_lure!!.itemId) {
                7807 // green lure, preferred by fast-moving (nimble) fish (type 5)
                -> type = when {
                    check <= 54 -> 5
                    check <= 77 -> 4
                    else -> 6
                }

                7808 // purple lure, preferred by fat fish (type 4)
                -> type = when {
                    check <= 54 -> 4
                    check <= 77 -> 6
                    else -> 5
                }

                7809 // yellow lure, preferred by ugly fish (type 6)
                -> type = when {
                    check <= 54 -> 6
                    check <= 77 -> 5
                    else -> 4
                }

                8486 // prize-winning fishing lure for beginners
                -> type = when {
                    check <= 33 -> 4
                    check <= 66 -> 5
                    else -> 6
                }
            }

            1 // normal fish
            -> when (_lure!!.itemId) {
                7610, 7611, 7612, 7613 -> type = 3

                6519 // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
                    , 8505, 6520, 6521, 8507 -> type = when {
                    check <= 54 -> 1
                    check <= 74 -> 0
                    check <= 94 -> 2
                    else -> 3
                }

                6522 // all theese lures (purple) are prefered by fat fish (type 0)
                    , 8508, 6523, 6524, 8510 -> type = when {
                    check <= 54 -> 0
                    check <= 74 -> 1
                    check <= 94 -> 2
                    else -> 3
                }

                6525 // all theese lures (yellow) are prefered by ugly fish (type 2)
                    , 8511, 6526, 6527, 8513 -> type = when {
                        check <= 55 -> 2
                        check <= 74 -> 1
                        check <= 94 -> 0
                        else -> 3
                    }
                8484 // prize-winning fishing lure
                -> type = when {
                    check <= 33 -> 0
                    check <= 66 -> 1
                    else -> 2
                }
            }

            2 // upper grade fish, luminous lure
            -> when (_lure!!.itemId) {
                8506 // green lure, preferred by fast-moving (nimble) fish (type 8)
                -> type = when {
                    check <= 54 -> 8
                    check <= 77 -> 7
                    else -> 9
                }

                8509 // purple lure, preferred by fat fish (type 7)
                -> type = when {
                    check <= 54 -> 7
                    check <= 77 -> 9
                    else -> 8
                }

                8512 // yellow lure, preferred by ugly fish (type 9)
                -> type = when {
                    check <= 54 -> 9
                    check <= 77 -> 8
                    else -> 7
                }

                8485 // prize-winning fishing lure
                -> type = when {
                    check <= 33 -> 7
                    check <= 66 -> 8
                    else -> 9
                }
            }
        }
        return type
    }

    /**
     * Modify [Fish] current HP during the minigame. We also handle the different events (HP reaching 0, or HP going past maximum HP).
     * @param hp : The HP amount to add or remove.
     * @param penalty : The penalty amount.
     */
    fun changeHp(hp: Int, penalty: Int) {
        _fishCurHp -= hp
        if (_fishCurHp < 0)
            _fishCurHp = 0

        _fisher!!.broadcastPacket(
            ExFishingHpRegen(
                _fisher,
                _time,
                _fishCurHp,
                _mode,
                _goodUse,
                _anim,
                penalty,
                _deceptiveMode
            )
        )
        _anim = 0

        if (_fishCurHp > _fish!!.hp * 2) {
            _fishCurHp = _fish!!.hp * 2
            end(false)
            return
        } else if (_fishCurHp == 0)
            end(true)
    }

    protected fun aiTask() {
        if (_thinking)
            return

        _thinking = true
        _time--

        try {
            if (_mode == 1) {
                if (_deceptiveMode == 0)
                    _fishCurHp += _fish!!.hpRegen
            } else {
                if (_deceptiveMode == 1)
                    _fishCurHp += _fish!!.hpRegen
            }

            if (_stop == 0) {
                _stop = 1
                var check = Rnd[100]
                if (check >= 70)
                    _mode = if (_mode == 0) 1 else 0

                if (_isUpperGrade) {
                    check = Rnd[100]
                    if (check >= 90)
                        _deceptiveMode = if (_deceptiveMode == 0) 1 else 0
                }
            } else
                _stop--
        } finally {
            _thinking = false
            if (_anim != 0)
                _fisher!!.broadcastPacket(
                    ExFishingHpRegen(
                        _fisher,
                        _time,
                        _fishCurHp,
                        _mode,
                        0,
                        _anim,
                        0,
                        _deceptiveMode
                    )
                )
            else
                _fisher!!.sendPacket(ExFishingHpRegen(_fisher, _time, _fishCurHp, _mode, 0, _anim, 0, _deceptiveMode))
        }
    }

    fun useRealing(dmg: Int, penalty: Int) {
        _anim = 2
        if (Rnd[100] > 90) {
            _fisher!!.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN)
            _goodUse = 0
            changeHp(0, penalty)
            return
        }

        if (_fisher == null)
            return

        if (_mode == 1) {
            if (_deceptiveMode == 0) {
                // Reeling is successful, Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(
                        dmg
                    )
                )
                if (penalty == 50)
                    _fisher.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(
                            penalty
                        )
                    )

                _goodUse = 1
                changeHp(dmg, penalty)
            } else {
                // Reeling failed, Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(
                        dmg
                    )
                )
                _goodUse = 2
                changeHp(-dmg, penalty)
            }
        } else {
            if (_deceptiveMode == 0) {
                // Reeling failed, Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_REELING_S1_HP_REGAINED).addNumber(
                        dmg
                    )
                )
                _goodUse = 2
                changeHp(-dmg, penalty)
            } else {
                // Reeling is successful, Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESFUL_S1_DAMAGE).addNumber(
                        dmg
                    )
                )
                if (penalty == 50)
                    _fisher.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.REELING_SUCCESSFUL_PENALTY_S1).addNumber(
                            penalty
                        )
                    )

                _goodUse = 1
                changeHp(dmg, penalty)
            }
        }
    }

    fun usePomping(dmg: Int, penalty: Int) {
        _anim = 1
        if (Rnd[100] > 90) {
            _fisher!!.sendPacket(SystemMessageId.FISH_RESISTED_ATTEMPT_TO_BRING_IT_IN)
            _goodUse = 0
            changeHp(0, penalty)
            return
        }

        if (_fisher == null)
            return

        if (_mode == 0) {
            if (_deceptiveMode == 0) {
                // Pumping is successful. Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(
                        dmg
                    )
                )
                if (penalty == 50)
                    _fisher.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(
                            penalty
                        )
                    )

                _goodUse = 1
                changeHp(dmg, penalty)
            } else {
                // Pumping failed, Regained: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(
                        dmg
                    )
                )
                _goodUse = 2
                changeHp(-dmg, penalty)
            }
        } else {
            if (_deceptiveMode == 0) {
                // Pumping failed, Regained: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.FISH_RESISTED_PUMPING_S1_HP_REGAINED).addNumber(
                        dmg
                    )
                )
                _goodUse = 2
                changeHp(-dmg, penalty)
            } else {
                // Pumping is successful. Damage: $s1
                _fisher.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESFUL_S1_DAMAGE).addNumber(
                        dmg
                    )
                )
                if (penalty == 50)
                    _fisher.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.PUMPING_SUCCESSFUL_PENALTY_S1).addNumber(
                            penalty
                        )
                    )

                _goodUse = 1
                changeHp(dmg, penalty)
            }
        }
    }

    /**
     * Start the fishing process.
     *
     *  * The [Player] is immobilized, if he was moving.
     *  * The bait [Location] and the lure are set.
     *  * The [Fish] to caught is calculated.
     *  * The _lookingForFish task is processed.
     *
     * @param x : The X bait location.
     * @param y : The Y bait location.
     * @param z : The Z bait location.
     * @param lure : The used lure.
     */
    fun start(x: Int, y: Int, z: Int, lure: ItemInstance) {
        if (_fisher!!.isDead)
            return

        // Stop the player.
        _fisher.stopMove(null)
        _fisher.setIsImmobilized(true)

        // Set variables.
        loc.set(x, y, z)
        _lure = lure

        var group: Int

        when (_lure!!.itemId) {
            7807 // green for beginners
                , 7808 // purple for beginners
                , 7809 // yellow for beginners
                , 8486 // prize-winning for beginners
            -> {
                group = 0
                group = 2
                group = 1
            }

            8485 // prize-winning luminous
                , 8506 // green luminous
                , 8509 // purple luminous
                , 8512 // yellow luminous
            -> {
                group = 2
                group = 1
            }

            else -> group = 1
        }

        // Define which fish we gonna catch.
        _fish = FishData.getFish(randomFishLvl, getRandomFishType(group), group)
        if (_fish == null) {
            end(false)
            return
        }

        _fisher.sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING)

        _fisher.broadcastPacket(ExFishingStart(_fisher, _fish!!.getType(_lure!!.isNightLure), loc, _lure!!.isNightLure))
        _fisher.sendPacket(PlaySound(1, "SF_P_01"))

        // "Looking for fish" task is now processed.
        if (_lookingForFish == null) {
            val lureid = _lure!!.itemId
            val isNoob = _fish!!.group == 0
            val isUpperGrade = _fish!!.group == 2

            var checkDelay = 0
            if (lureid == 6519 || lureid == 6522 || lureid == 6525 || lureid == 8505 || lureid == 8508 || lureid == 8511)
            // low grade
                checkDelay = Math.round((_fish!!.gutsCheckTime * 1.33).toFloat())
            else if (lureid == 6520 || lureid == 6523 || lureid == 6526 || lureid >= 8505 && lureid <= 8513 || lureid >= 7610 && lureid <= 7613 || lureid >= 7807 && lureid <= 7809 || lureid >= 8484 && lureid <= 8486)
            // medium grade, beginner, prize-winning & quest special bait
                checkDelay = Math.round((_fish!!.gutsCheckTime * 1.00).toFloat())
            else if (lureid == 6521 || lureid == 6524 || lureid == 6527 || lureid == 8507 || lureid == 8510 || lureid == 8513)
            // high grade
                checkDelay = Math.round((_fish!!.gutsCheckTime * 0.66).toFloat())

            val timer = System.currentTimeMillis() + _fish!!.waitTime.toLong() + 10000

            _lookingForFish = ThreadPool.scheduleAtFixedRate({
                if (System.currentTimeMillis() >= timer) {
                    end(false)
                    return@scheduleAtFixedRate
                }

                if (_fish!!.getType(_lure!!.isNightLure) == -1)
                    return@scheduleAtFixedRate

                if (_fish!!.guts > Rnd[1000]) {
                    // Stop task.
                    if (_lookingForFish != null) {
                        _lookingForFish!!.cancel(false)
                        _lookingForFish = null
                    }

                    _fishCurHp = _fish!!.hp
                    _time = _fish!!.combatTime / 1000
                    _isUpperGrade = isUpperGrade
                    _lure = lure

                    if (isUpperGrade) {
                        _deceptiveMode = if (Rnd[100] >= 90) 1 else 0
                        _lureType = 2
                    } else {
                        _deceptiveMode = 0
                        _lureType = if (isNoob) 0 else 1
                    }
                    _mode = if (Rnd[100] >= 80) 1 else 0

                    _fisher.broadcastPacket(
                        ExFishingStartCombat(
                            _fisher,
                            _time,
                            _fish!!.hp,
                            _mode,
                            _lureType,
                            _deceptiveMode
                        )
                    )
                    _fisher.sendPacket(PlaySound(1, "SF_S_01"))

                    // Succeeded in getting a bite
                    _fisher.sendPacket(SystemMessageId.GOT_A_BITE)

                    if (_fishCombat == null)
                        _fishCombat = fishCombatTask(_fisher)
                }
            }, 10000, checkDelay.toLong())
        }
    }

    private fun fishCombatTask(_fisher: Player): ScheduledFuture<*>? {
        return ThreadPool.scheduleAtFixedRate({
            if (_fish == null)
                return@scheduleAtFixedRate

            // The fish got away.
            if (_fishCurHp >= _fish!!.hp * 2) {
                _fisher.sendPacket(SystemMessageId.BAIT_STOLEN_BY_FISH)
                end(false)
            } else if (_time <= 0) {
                _fisher.sendPacket(SystemMessageId.FISH_SPIT_THE_HOOK)
                end(false)
            } else
                aiTask()// Time is up, the fish spit the hook.
        }, 1000, 1000)
    }

    /**
     * Ends the fishing process.
     *
     *  * Process the reward ([Fish] or 5% [PenaltyMonster]), if "win" is set as True.
     *  * Cleanup all variables.
     *  * Give back the movement ability to the [Player].
     *  * End all running tasks.
     *
     * @param win : If true, a Fish or a PenaltyMonster has been caught.
     */
    fun end(win: Boolean) {
        if (win) {
            if (Rnd[100] < 5) {
                val npcId = 18319 + Math.min(_fisher!!.level / 11, 7) // 18319-18326

                val npc = PenaltyMonster(IdFactory.getInstance().nextId, NpcData.getTemplate(npcId))
                npc.setXYZ(_fisher.x, _fisher.y, _fisher.z + 20)
                npc.setCurrentHpMp(npc.maxHp.toDouble(), npc.maxMp.toDouble())
                npc.heading = _fisher.heading
                npc.spawnMe()
                npc.setPlayerToKill(_fisher)

                _fisher.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING_SMELLY_THROW_IT_BACK)
            } else {
                _fisher!!.sendPacket(SystemMessageId.YOU_CAUGHT_SOMETHING)
                _fisher.addItem("Fishing", _fish!!.id, 1, null, true)

                FishingChampionshipManager.getInstance().newFish(_fisher, _lure!!.itemId)
            }
        }

        if (_fish == null)
            _fisher!!.sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY)

        // Cleanup variables.
        _time = 0
        _stop = 0
        _goodUse = 0
        _anim = 0
        _mode = 0
        _deceptiveMode = 0

        _thinking = false

        _fish = null
        _fishCurHp = 0

        _isUpperGrade = false

        _lure = null
        _lureType = 0

        loc.clean()

        // Ends fishing
        _fisher!!.broadcastPacket(ExFishingEnd(win, _fisher.objectId))
        _fisher.sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING)
        _fisher.setIsImmobilized(false)

        // Stop tasks.
        if (_lookingForFish != null) {
            _lookingForFish!!.cancel(false)
            _lookingForFish = null
        }

        if (_fishCombat != null) {
            _fishCombat!!.cancel(false)
            _fishCombat = null
        }
    }
}