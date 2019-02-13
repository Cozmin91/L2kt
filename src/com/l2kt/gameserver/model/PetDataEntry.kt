package com.l2kt.gameserver.model

import com.l2kt.gameserver.templates.StatsSet

class PetDataEntry(stats: StatsSet) {
    val maxExp: Long = stats.getLong("exp")
    val maxMeal: Int = stats.getInteger("maxMeal")
    val expType: Int = stats.getInteger("expType")
    val mealInBattle: Int = stats.getInteger("mealInBattle")
    val mealInNormal: Int = stats.getInteger("mealInNormal")
    val pAtk: Double = stats.getDouble("pAtk")
    val pDef: Double = stats.getDouble("pDef")
    val mAtk: Double = stats.getDouble("mAtk")
    val mDef: Double = stats.getDouble("mDef")
    val maxHp: Double = stats.getDouble("hp")
    val maxMp: Double = stats.getDouble("mp")

    val hpRegen: Float = stats.getFloat("hpRegen")
    val mpRegen: Float = stats.getFloat("mpRegen")

    val ssCount: Int = stats.getInteger("ssCount")
    val spsCount: Int = stats.getInteger("spsCount")

    val mountMealInBattle: Int = stats.getInteger("mealInBattleOnRide", 0)
    val mountMealInNormal: Int = stats.getInteger("mealInNormalOnRide", 0)
    val mountAtkSpd: Int = stats.getInteger("atkSpdOnRide", 0)
    val mountPAtk: Double = stats.getDouble("pAtkOnRide", 0.0)
    val mountMAtk: Double = stats.getDouble("mAtkOnRide", 0.0)
    val mountBaseSpeed: Int
    val mountSwimSpeed: Int
    val mountFlySpeed: Int

    init {

        val speed = stats.getString("speedOnRide", null)
        if (speed != null) {
            val speeds = speed.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
            mountBaseSpeed = Integer.parseInt(speeds[0])
            mountSwimSpeed = Integer.parseInt(speeds[2])
            mountFlySpeed = Integer.parseInt(speeds[4])
        } else {
            mountBaseSpeed = 0
            mountSwimSpeed = 0
            mountFlySpeed = 0
        }
    }
}