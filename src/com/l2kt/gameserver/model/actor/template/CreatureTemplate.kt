package com.l2kt.gameserver.model.actor.template

import com.l2kt.gameserver.templates.StatsSet

/**
 * The generic datatype used by any character template. It holds basic informations, such as base stats (STR, CON, DEX,...) and extended stats (power attack, magic attack, hp/mp regen, collision values).
 */
open class CreatureTemplate(set: StatsSet) {
    val baseSTR: Int = set.getInteger("str", 40)
    val baseCON: Int = set.getInteger("con", 21)
    val baseDEX: Int = set.getInteger("dex", 30)
    val baseINT: Int = set.getInteger("int", 20)
    val baseWIT: Int = set.getInteger("wit", 43)
    val baseMEN: Int = set.getInteger("men", 20)

    private val _baseHpMax: Double = set.getDouble("hp", 0.0)
    private val _baseMpMax: Double = set.getDouble("mp", 0.0)

    val baseHpReg: Double = set.getDouble("hpRegen", 1.5)
    val baseMpReg: Double = set.getDouble("mpRegen", 0.9)

    val basePAtk: Double = set.getDouble("pAtk")
    val baseMAtk: Double = set.getDouble("mAtk")
    val basePDef: Double = set.getDouble("pDef")
    val baseMDef: Double = set.getDouble("mDef")

    val basePAtkSpd: Int = set.getInteger("atkSpd", 300)

    val baseCritRate: Int = set.getInteger("crit", 4)

    val baseWalkSpeed: Int = set.getInteger("walkSpd", 0)
    val baseRunSpeed: Int = set.getInteger("runSpd", 1)

    val collisionRadius: Double = set.getDouble("radius")
    val collisionHeight: Double = set.getDouble("height")

    open fun getBaseHpMax(level: Int): Double {
        return _baseHpMax
    }

    open fun getBaseMpMax(level: Int): Double {
        return _baseMpMax
    }
}