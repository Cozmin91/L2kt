package com.l2kt.gameserver.model.location

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype extending [Location], used to retain a single Gatekeeper teleport location.
 */
class TeleportLocation(set: StatsSet) : Location(set.getInteger("x"), set.getInteger("y"), set.getInteger("z")) {

    val price: Int = set.getInteger("price")
    val isNoble: Boolean = set.getBool("isNoble")

}