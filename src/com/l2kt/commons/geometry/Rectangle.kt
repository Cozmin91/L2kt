package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
open class Rectangle
/**
 * Rectangle constructor.
 * @param x : Bottom left X coordinate.
 * @param y : Bottom left Y coordinate.
 * @param w : Rectangle width.
 * @param h : Rectangle height.
 */
    (// rectangle origin coordinates
    protected val _x: Int, protected val _y: Int, // rectangle width and height
    protected val _w: Int, protected val _h: Int
) : AShape() {

    override val size: Int
        get() = _w * _h

    override val area: Double
        get() = (_w * _h).toDouble()

    override val volume: Double
        get() = 0.0

    override// calculate coordinates and return
    val randomLocation: Location
        get() = Location(_x + Rnd[_w], _y + Rnd[_h], 0)

    override fun isInside(x: Int, y: Int): Boolean {
        var d = x - _x
        if (d < 0 || d > _w)
            return false

        d = y - _y
        return !(d < 0 || d > _h)

    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        var d = x - _x
        if (d < 0 || d > _w)
            return false

        d = y - _y
        return !(d < 0 || d > _h)

    }
}