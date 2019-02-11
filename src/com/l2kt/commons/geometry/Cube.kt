package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
class Cube
/**
 * Cube constructor.
 * @param x : Bottom left lower X coordinate.
 * @param y : Bottom left lower Y coordinate.
 * @param z : Bottom left lower Z coordinate.
 * @param a : Size of cube side.
 */
    (
    x: Int, y: Int, // cube origin coordinates
    private val _z: Int, a: Int
) : Square(x, y, a) {

    override val area: Double
        get() = (6 * _a * _a).toDouble()

    override val volume: Double
        get() = (_a * _a * _a).toDouble()

    override// calculate coordinates and return
    val randomLocation: Location
        get() = Location(_x + Rnd[_a], _y + Rnd[_a], _z + Rnd[_a])

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        var d = z - _z
        if (d < 0 || d > _a)
            return false

        d = x - _x
        if (d < 0 || d > _a)
            return false

        d = y - _y
        return !(d < 0 || d > _a)

    }
}