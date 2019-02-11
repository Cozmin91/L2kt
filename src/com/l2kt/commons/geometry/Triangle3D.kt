package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * Tri-sided polygon in 3D, while having bottom and top area flat (in Z coordinate).<br></br>
 * It is **not** 3D oriented triangle.
 * @author Hasha
 */
class Triangle3D
/**
 * Triangle constructor.
 * @param A : Point A of the triangle.
 * @param B : Point B of the triangle.
 * @param C : Point C of the triangle.
 */
    (A: IntArray, B: IntArray, C: IntArray) : Triangle(A, B, C) {
    // min and max Z coorinates
    private val _minZ: Int = Math.min(A[2], Math.min(B[2], C[2]))
    private val _maxZ: Int = Math.max(A[2], Math.max(B[2], C[2]))

    // total length of all sides
    private val _length: Double

    override val area: Double
        get() = size * 2 + _length * (_maxZ - _minZ)

    override val volume: Double
        get() = (size * (_maxZ - _minZ)).toDouble()

    override// get relative length of AB and AC vectors
    // adjust length if too long
    // calc coords (take A, add AB and AC)
    // return
    val randomLocation: Location
        get() {
            var ba = Rnd.nextDouble()
            var ca = Rnd.nextDouble()
            if (ba + ca > 1) {
                ba = 1 - ba
                ca = 1 - ca
            }
            val x = _Ax + (ba * _BAx + ca * _CAx).toInt()
            val y = _Ay + (ba * _BAy + ca * _CAy).toInt()
            return Location(x, y, Rnd[_minZ, _maxZ])
        }

    init {

        val CBx = _CAx - _BAx
        val CBy = _CAy - _BAy
        _length =
            Math.sqrt((_BAx * _BAx + _BAy * _BAy).toDouble()) + Math.sqrt((_CAx * _CAx + _CAy * _CAy).toDouble()) + Math.sqrt(
                (CBx * CBx + CBy * CBy).toDouble()
            )
    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        return if (z < _minZ || z > _maxZ) false else super.isInside(x, y, z)

    }
}