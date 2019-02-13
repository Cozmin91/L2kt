package com.l2kt.gameserver.model.zone.type

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.zone.ZoneType

/**
 * A zone extending [ZoneType], used for fish points.
 */
class FishingZone(id: Int) : ZoneType(id) {

    val waterZ: Int
        get() = zone!!.highZ

    override fun onEnter(character: Creature) {}

    override fun onExit(character: Creature) {}
}