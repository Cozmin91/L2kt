package com.l2kt.gameserver.model.holder

import com.l2kt.gameserver.model.L2Skill

/**
 * A class extending [IntIntHolder] containing all neccessary information to maintain valid timestamps and reuse for skills upon relog.
 */
class Timestamp : IntIntHolder {
    val reuse: Long
    val stamp: Long

    val remaining: Long
        get() = Math.max(stamp - System.currentTimeMillis(), 0)

    constructor(skill: L2Skill, reuse: Long) : super(skill.id, skill.level) {

        this.reuse = reuse
        stamp = System.currentTimeMillis() + reuse
    }

    constructor(skill: L2Skill, reuse: Long, systime: Long) : super(skill.id, skill.level) {

        this.reuse = reuse
        stamp = systime
    }

    fun hasNotPassed(): Boolean {
        return System.currentTimeMillis() < stamp
    }
}