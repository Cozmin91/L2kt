package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
class Cuboid
/**
 * Cuboid constructor.
 * @param x : Bottom left lower X coordinate.
 * @param y : Bottom left lower Y coordinate.
 * @param minZ : Minimum Z coordinate.
 * @param maxZ : Maximum Z coordinate.
 * @param w : Cuboid width.
 * @param h : Cuboid height.
 */
    (
    x: Int, y: Int, // min and max Z coorinates
    private val _minZ: Int, private val _maxZ: Int, w: Int, h: Int
) : Rectangle(x, y, w, h) {

    override val area: Double
        get() = (2 * (_w * _h + (_w + _h) * (_maxZ - _minZ))).toDouble()

    override val volume: Double
        get() = (_w * _h * (_maxZ - _minZ)).toDouble()

    override// calculate coordinates and return
    val randomLocation: Location
        get() = Location(_x + Rnd[_w], _y + Rnd[_h], Rnd[_minZ, _maxZ])

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        if (z < _minZ || z > _maxZ)
            return false

        var d = x - _x
        if (d < 0 || d > _w)
            return false

        d = y - _y
        return !(d < 0 || d > _h)

    }
}