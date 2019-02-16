package com.l2kt.gameserver.model.olympiad

import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.templates.StatsSet

/**
 * @author DS
 */
class Participant {
    val objectId: Int
    var player: Player? = null
    val name: String
    val side: Int
    val baseClass: Int
    var disconnected = false
    var defaulted = false
    val stats: StatsSet?

    constructor(plr: Player, olympiadSide: Int) {
        objectId = plr.objectId
        player = plr
        name = plr.name
        side = olympiadSide
        baseClass = plr.baseClass
        stats = Olympiad.getNobleStats(objectId)
    }

    constructor(objId: Int, olympiadSide: Int) {
        objectId = objId
        player = null
        name = "-"
        side = olympiadSide
        baseClass = 0
        stats = null
    }

    fun updatePlayer() {
        if (player == null || !player!!.isOnline)
            player = World.getPlayer(objectId)
    }

    fun updateStat(statName: String, increment: Int) {
        stats!![statName] = Math.max(stats.getInteger(statName) + increment, 0).toDouble()
    }
}