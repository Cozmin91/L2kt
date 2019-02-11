package com.l2kt.gameserver.skills

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Cubic
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance

class Env {
    var character: Creature? = null

    var cubic: Cubic? = null

    var target: Creature? = null

    var item: ItemInstance? = null

    var skill: L2Skill? = null

    var value: Double = 0.toDouble()

    var baseValue: Double = 0.toDouble()

    var isSkillMastery = false

    var shield: Byte = 0

    var isSoulShot = false

    var isSpiritShot = false

    var isBlessedSpiritShot = false

    val player: Player?
        get() = if (character == null) null else character!!.actingPlayer

    constructor()

    constructor(shield: Byte, soulShot: Boolean, spiritShot: Boolean, blessedSpiritShot: Boolean) {
        this.shield = shield
        isSoulShot = soulShot
        isSpiritShot = spiritShot
        isBlessedSpiritShot = blessedSpiritShot
    }

    fun addValue(value: Double) {
        this.value += value
    }

    fun subValue(value: Double) {
        this.value -= value
    }

    fun mulValue(value: Double) {
        this.value *= value
    }

    fun divValue(value: Double) {
        this.value /= value
    }
}