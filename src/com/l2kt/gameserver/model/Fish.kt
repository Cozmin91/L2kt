package com.l2kt.gameserver.model

import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used to retain a fish information.
 */
class Fish(set: StatsSet) {
    val id: Int = set.getInteger("id")
    val level: Int = set.getInteger("level")
    val hp: Int = set.getInteger("hp")
    val hpRegen: Int = set.getInteger("hpRegen")
    val type: Int = set.getInteger("type")
    val group: Int = set.getInteger("group")
    val guts: Int = set.getInteger("guts")
    val gutsCheckTime: Int = set.getInteger("gutsCheckTime")
    val waitTime: Int = set.getInteger("waitTime")
    val combatTime: Int = set.getInteger("combatTime")

    fun getType(isLureNight: Boolean): Int {
        return if (!GameTimeTaskManager.isNight && isLureNight) -1 else type
    }
}