package com.l2kt.gameserver.geoengine.geodata

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.location.Location

class GeoLocation(x: Int, y: Int, z: Int) : Location(x, y, GeoEngine.getHeightNearest(x, y, z).toInt()) {
    var nswe: Byte = GeoEngine.getNsweNearest(x, y, z)

    val geoX: Int
        get() = super.x

    val geoY: Int
        get() = super.y

    override var x: Int
        get() = GeoEngine.getWorldX(super.x)
        set(value) {
            super.x = value
        }

    override var y: Int
        get() = GeoEngine.getWorldY(super.y)
        set(value) {
            super.y = value
        }

    operator fun set(x: Int, y: Int, z: Short) {
        super.set(x, y, GeoEngine.getHeightNearest(x, y, z.toInt()).toInt())
        nswe = GeoEngine.getNsweNearest(x, y, z.toInt())
    }
}