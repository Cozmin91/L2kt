package com.l2kt.gameserver.model.rift

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.templates.StatsSet
import java.awt.Polygon
import java.awt.Shape
import java.util.*

/**
 * One cell of Dimensional Rift system.<br></br>
 * <br></br>
 * Each DimensionalRiftRoom holds specific [L2Spawn]s, a [Shape], and a teleport [Location].
 */
class DimensionalRiftRoom(val type: Byte, set: StatsSet) {

    val spawns: List<L2Spawn> = ArrayList()
    val id: Byte

    private val _xMin: Int
    private val _xMax: Int
    private val _yMin: Int
    private val _yMax: Int

    val teleportLoc: Location

    private val _shape: Shape

    val isBossRoom: Boolean

    var isPartyInside: Boolean = false

    val randomX: Int
        get() = Rnd[_xMin, _xMax]

    val randomY: Int
        get() = Rnd[_yMin, _yMax]

    init {
        val xMin = set.getInteger("xMin")
        val xMax = set.getInteger("xMax")
        val yMin = set.getInteger("yMin")
        val yMax = set.getInteger("yMax")
        id = set.getByte("id")
        _xMin = xMin + 128
        _xMax = xMax - 128
        _yMin = yMin + 128
        _yMax = yMax - 128

        teleportLoc = Location(set.getInteger("xT"), set.getInteger("yT"), Z_VALUE)

        isBossRoom = id.toInt() == 9

        _shape = Polygon(intArrayOf(xMin, xMax, xMax, xMin), intArrayOf(yMin, yMin, yMax, yMax), 4)
    }

    override fun toString(): String {
        return "RiftRoom #" + type + "_" + id + ", full: " + isPartyInside + ", tel: " + teleportLoc.toString() + ", spawns: " + spawns.size

    }

    fun checkIfInZone(x: Int, y: Int, z: Int): Boolean {
        return _shape.contains(x.toDouble(), y.toDouble()) && z >= -6816 && z <= -6240
    }

    fun spawn() {
        for (spawn in spawns) {
            spawn.doSpawn(false)
            spawn.setRespawnState(true)
        }
    }

    fun unspawn() {
        for (spawn in spawns) {
            spawn.setRespawnState(false)
            if (spawn.npc != null)
                spawn.npc.deleteMe()
        }
        isPartyInside = false
    }

    companion object {
        const val Z_VALUE = -6752
    }
}