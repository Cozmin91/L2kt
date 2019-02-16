package com.l2kt.gameserver.geoengine.geodata

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.location.Location

class GeoLocation(x: Int, y: Int, z: Int) : Location(x, y, GeoEngine.getHeightNearest(x, y, z).toInt()) {
    var nswe: Byte = 0
        private set

    val geoX: Int
        get() = _x

    val geoY: Int
        get() = _y

    init {
        nswe = GeoEngine.getNsweNearest(x, y, z)
    }

    operator fun set(x: Int, y: Int, z: Short) {
        super.set(x, y, GeoEngine.getHeightNearest(x, y, z.toInt()).toInt())
        nswe = GeoEngine.getNsweNearest(x, y, z.toInt())
    }

    override fun getX(): Int {
        return GeoEngine.getWorldX(_x)
    }

    override fun getY(): Int {
        return GeoEngine.getWorldY(_y)
    }
}