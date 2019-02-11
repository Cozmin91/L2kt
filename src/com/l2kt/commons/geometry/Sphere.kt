package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
class Sphere
/**
 * Sphere constructor.
 * @param x : Center X coordinate.
 * @param y : Center Y coordinate.
 * @param z : Center Z coordinate.
 * @param r : Sphere radius.
 */
    (
    x: Int, y: Int,
    private val _z: Int, r: Int
) : Circle(x, y, r) {

    override val area: Double
        get() = 4.0 * Math.PI * _r.toDouble() * _r.toDouble()

    override val volume: Double
        get() = 4.0 * Math.PI * _r.toDouble() * _r.toDouble() * _r.toDouble() / 3

    override val randomLocation: Location
        get() {
            val r = Math.cbrt(Rnd.nextDouble()) * _r
            val phi = Rnd.nextDouble() * 2.0 * Math.PI
            val theta = Math.acos(2 * Rnd.nextDouble() - 1)
            val x = (_x + r * Math.cos(phi) * Math.sin(theta)).toInt()
            val y = (_y + r * Math.sin(phi) * Math.sin(theta)).toInt()
            val z = (_z + r * Math.cos(theta)).toInt()
            return Location(x, y, z)
        }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        val dx = x - _x
        val dy = y - _y
        val dz = z - _z

        return dx * dx + dy * dy + dz * dz <= _r * _r
    }
}