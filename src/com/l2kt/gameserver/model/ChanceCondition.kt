package com.l2kt.gameserver.model

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.templates.StatsSet
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author kombat
 */
class ChanceCondition private constructor(private val _triggerType: TriggerType, private val _chance: Int) {

    enum class TriggerType constructor(private val _mask: Int) {
        // You hit an enemy
        ON_HIT(1),
        // You hit an enemy - was crit
        ON_CRIT(2),
        // You cast a skill
        ON_CAST(4),
        // You cast a skill - it was a physical one
        ON_PHYSICAL(8),
        // You cast a skill - it was a magic one
        ON_MAGIC(16),
        // You cast a skill - it was a magic one - good magic
        ON_MAGIC_GOOD(32),
        // You cast a skill - it was a magic one - offensive magic
        ON_MAGIC_OFFENSIVE(64),
        // You are attacked by enemy
        ON_ATTACKED(128),
        // You are attacked by enemy - by hit
        ON_ATTACKED_HIT(256),
        // You are attacked by enemy - by hit - was crit
        ON_ATTACKED_CRIT(512),
        // A skill was casted on you
        ON_HIT_BY_SKILL(1024),
        // An evil skill was casted on you
        ON_HIT_BY_OFFENSIVE_SKILL(2048),
        // A good skill was casted on you
        ON_HIT_BY_GOOD_MAGIC(4096),
        // Evading melee attack
        ON_EVADED_HIT(8192),
        // Effect only - on start
        ON_START(16384),
        // Effect only - each second
        ON_ACTION_TIME(32768),
        // Effect only - on exit
        ON_EXIT(65536);

        fun check(event: Int): Boolean {
            return _mask and event != 0 // Trigger (sub-)type contains event (sub-)type
        }
    }

    fun trigger(event: Int): Boolean {
        return _triggerType.check(event) && (_chance < 0 || Rnd[100] < _chance)
    }

    override fun toString(): String {
        return "Trigger[$_chance;$_triggerType]"
    }

    companion object {
        protected val _log = Logger.getLogger(ChanceCondition::class.java.name)
        const val EVT_HIT = 1
        const val EVT_CRIT = 2
        const val EVT_CAST = 4
        const val EVT_PHYSICAL = 8
        const val EVT_MAGIC = 16
        const val EVT_MAGIC_GOOD = 32
        const val EVT_MAGIC_OFFENSIVE = 64
        const val EVT_ATTACKED = 128
        const val EVT_ATTACKED_HIT = 256
        const val EVT_ATTACKED_CRIT = 512
        const val EVT_HIT_BY_SKILL = 1024
        const val EVT_HIT_BY_OFFENSIVE_SKILL = 2048
        const val EVT_HIT_BY_GOOD_MAGIC = 4096
        const val EVT_EVADED_HIT = 8192
        const val EVT_ON_START = 16384
        const val EVT_ON_ACTION_TIME = 32768
        const val EVT_ON_EXIT = 65536

        fun parse(set: StatsSet): ChanceCondition? {
            try {
                val trigger = set.getEnum("chanceType", TriggerType::class.java, null)
                val chance = set.getInteger("activationChance", -1)

                if (trigger != null)
                    return ChanceCondition(trigger, chance)
            } catch (e: Exception) {
                _log.log(Level.WARNING, "", e)
            }

            return null
        }

        fun parse(chanceType: String?, chance: Int): ChanceCondition? {
            if (chanceType == null)
                return null

            val trigger = TriggerType.valueOf(chanceType)

            return ChanceCondition(trigger, chance)
        }
    }
}