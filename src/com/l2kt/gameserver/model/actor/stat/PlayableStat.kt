package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.base.Experience
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.SwampZone
import com.l2kt.gameserver.skills.Stats

open class PlayableStat(activeChar: Playable) : CreatureStat(activeChar) {

    override// get base value
    // apply zone modifier before final calculation
    // calculate speed
    val moveSpeed: Float
        get() {
            var baseValue = baseMoveSpeed.toFloat()
            if (activeChar.isInsideZone(ZoneId.SWAMP)) {
                val zone = ZoneManager.getZone(activeChar, SwampZone::class.java)
                if (zone != null)
                    baseValue *= ((100 + zone.moveBonus) / 100.0).toFloat()
            }
            return calcStat(Stats.RUN_SPEED, baseValue.toDouble(), null, null).toFloat()
        }

    val maxLevel: Int
        get() = Experience.MAX_LEVEL.toInt()

    open fun addExp(value: Long): Boolean {
        var value = value
        if (exp + value < 0)
            return true

        if (exp + value >= getExpForLevel(Experience.MAX_LEVEL.toInt()))
            value = getExpForLevel(Experience.MAX_LEVEL.toInt()) - 1 - exp

        exp += value

        var level: Byte = 0
        level = 1
        while (level <= Experience.MAX_LEVEL) {
            if (exp >= getExpForLevel(level.toInt())) {
                level++
                continue
            }

            level--
            break
        }

        if (level != this.level)
            addLevel((level - this.level).toByte())

        return true
    }

    fun removeExp(value: Long): Boolean {
        var value = value
        if (exp - value < 0)
            value = exp - 1

        exp -= value

        var level: Byte = 1
        while (level <= Experience.MAX_LEVEL) {
            if (exp >= getExpForLevel(level.toInt())) {
                level++
                continue
            }

            level--
            break
        }

        if (level != this.level)
            addLevel((level - this.level).toByte())

        return true
    }

    open fun addExpAndSp(addToExp: Long, addToSp: Int): Boolean {
        var expAdded = false
        var spAdded = false

        if (addToExp >= 0)
            expAdded = addExp(addToExp)

        if (addToSp >= 0)
            spAdded = addSp(addToSp)

        return expAdded || spAdded
    }

    open fun removeExpAndSp(removeExp: Long, removeSp: Int): Boolean {
        var expRemoved = false
        var spRemoved = false

        if (removeExp > 0)
            expRemoved = removeExp(removeExp)

        if (removeSp > 0)
            spRemoved = removeSp(removeSp)

        return expRemoved || spRemoved
    }

    open fun addLevel(value: Byte): Boolean {
        var value = value.toInt()
        if (level + value > Experience.MAX_LEVEL - 1) {
            if (level < Experience.MAX_LEVEL - 1)
                value = Experience.MAX_LEVEL.toInt() - 1 - level.toInt()
            else
                return false
        }

        val levelIncreased = level + value > level
        value += level
        level = value.toByte()

        // Sync up exp with current level
        if (exp >= getExpForLevel(level + 1) || getExpForLevel(level.toInt()) > exp)
            exp = getExpForLevel(level.toInt())

        if (!levelIncreased)
            return false

        activeChar.status.setCurrentHpMp(maxHp.toDouble(), maxMp.toDouble())

        return true
    }

    fun addSp(value: Int): Boolean {
        var value = value
        if (value < 0)
            return false

        val currentSp = sp
        if (currentSp == Integer.MAX_VALUE)
            return false

        if (currentSp > Integer.MAX_VALUE - value)
            value = Integer.MAX_VALUE - currentSp

        sp = currentSp + value
        return true
    }

    fun removeSp(value: Int): Boolean {
        var value = value
        val currentSp = sp
        if (currentSp < value)
            value = currentSp

        sp -= value
        return true
    }

    open fun getExpForLevel(level: Int): Long {
        return level.toLong()
    }

    override val activeChar: Playable
        get() = super.activeChar as Playable
}