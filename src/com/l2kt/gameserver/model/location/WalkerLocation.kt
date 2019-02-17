package com.l2kt.gameserver.model.location

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype extending [Location], used as a unique node of a pre-programmed route for Walker NPCs.<br></br>
 * <br></br>
 * Added to the x/y/z informations, you can also find delay (the time the Walker NPC will stand on the point without moving), the String to broadcast (null if none) and the running behavior.
 */
class WalkerLocation(set: StatsSet, private val _run: Boolean) :
    Location(set.getInteger("X"), set.getInteger("Y"), set.getInteger("Z")) {
    val chat: String? = set.getString("chat", null)
    val delay: Int = set.getInteger("delay", 0) * 1000

    fun doesNpcMustRun(): Boolean {
        return _run
    }
}