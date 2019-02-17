package com.l2kt.gameserver.model.entity

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.model.pledge.Clan
import java.util.*

interface Siegable {

    val attackerClans: List<Clan>

    val defenderClans: List<Clan>

    val siegeDate: Calendar?
    fun startSiege()

    fun endSiege()

    fun checkSide(clan: Clan?, type: SiegeSide): Boolean

    fun checkSides(clan: Clan?, vararg types: SiegeSide): Boolean

    fun checkSides(clan: Clan?): Boolean

    fun getFlag(clan: Clan?): Npc?
}