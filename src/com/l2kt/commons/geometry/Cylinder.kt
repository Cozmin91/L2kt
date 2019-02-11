package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
class Cylinder
/**
 * Cylinder constructor
 * @param x : Center X coordinate.
 * @param y : Center X coordinate.
 * @param r : Cylinder radius.
 * @param minZ : Minimum Z coordinate.
 * @param maxZ : Maximum Z coordinate.
 */
    (
    x: Int, y: Int, r: Int, // min and max Z coorinates
    private val _minZ: Int, private val _maxZ: Int
) : Circle(x, y, r) {

    override val area: Double
        get() = 2.0 * Math.PI * _r.toDouble() * (_r + _maxZ - _minZ).toDouble()

    override val volume: Double
        get() = Math.PI * _r.toDouble() * _r.toDouble() * (_maxZ - _minZ).toDouble()

    override// get uniform distance and angle
    // calculate coordinates and return
    val randomLocation: Location
        get() {
            val distance = Math.sqrt(Rnd.nextDouble()) * _r
            val angle = Rnd.nextDouble() * Math.PI * 2.0
            return Location(
                (distance * Math.cos(angle)).toInt(),
                (distance * Math.sin(angle)).toInt(),
                Rnd[_minZ, _maxZ]
            )
        }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        if (z < _minZ || z > _maxZ)
            return false

        val dx = x - _x
        val dy = y - _y

        return dx * dx + dy * dy <= _r * _r
    }
}