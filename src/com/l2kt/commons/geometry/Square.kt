package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
open class Square
/**
 * Square constructor.
 * @param x : Bottom left X coordinate.
 * @param y : Bottom left Y coordinate.
 * @param a : Size of square side.
 */
    (// square origin coordinates
    protected val _x: Int, protected val _y: Int, // square side
    protected val _a: Int
) : AShape() {

    override val size: Int
        get() = _a * _a

    override val area: Double
        get() = (_a * _a).toDouble()

    override val volume: Double
        get() = 0.0

    override// calculate coordinates and return
    val randomLocation: Location
        get() = Location(_x + Rnd[_a], _y + Rnd[_a], 0)

    override fun isInside(x: Int, y: Int): Boolean {
        var d = x - _x
        if (d < 0 || d > _a)
            return false

        d = y - _y
        return !(d < 0 || d > _a)

    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        var d = x - _x
        if (d < 0 || d > _a)
            return false

        d = y - _y
        return !(d < 0 || d > _a)

    }
}