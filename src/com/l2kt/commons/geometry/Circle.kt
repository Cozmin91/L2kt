package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
open class Circle
/**
 * Circle constructor
 * @param x : Center X coordinate.
 * @param y : Center Y coordinate.
 * @param r : Circle radius.
 */
    (// circle center coordinates
    protected val _x: Int, protected val _y: Int, // circle radius
    protected val _r: Int
) : AShape() {

    override val size: Int
        get() = Math.PI.toInt() * _r * _r

    override val area: Double
        get() = (Math.PI.toInt() * _r * _r).toDouble()

    override val volume: Double
        get() = 0.0

    override// get uniform distance and angle
    // calculate coordinates and return
    val randomLocation: Location
        get() {
            val distance = Math.sqrt(Rnd.nextDouble()) * _r
            val angle = Rnd.nextDouble() * Math.PI * 2.0
            return Location((distance * Math.cos(angle)).toInt(), (distance * Math.sin(angle)).toInt(), 0)
        }

    override fun isInside(x: Int, y: Int): Boolean {
        val dx = x - _x
        val dy = y - _y

        return dx * dx + dy * dy <= _r * _r
    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        val dx = x - _x
        val dy = y - _y

        return dx * dx + dy * dy <= _r * _r
    }
}