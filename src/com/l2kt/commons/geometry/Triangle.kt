package com.l2kt.commons.geometry

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
open class Triangle
/**
 * Triangle constructor.
 * @param A : Point A of the triangle.
 * @param B : Point B of the triangle.
 * @param C : Point C of the triangle.
 */
    (A: IntArray, B: IntArray, C: IntArray) : AShape() {
    protected val _Ax: Int
    protected val _Ay: Int

    protected val _BAx: Int
    protected val _BAy: Int

    protected val _CAx: Int
    protected val _CAy: Int

    override val size: Int

    override val area: Double
        get() = size.toDouble()

    override val volume: Double
        get() = 0.0

    override val randomLocation: Location
        get() {
            var ba = Rnd.nextDouble()
            var ca = Rnd.nextDouble()
            if (ba + ca > 1) {
                ba = 1 - ba
                ca = 1 - ca
            }
            val x = _Ax + (ba * _BAx + ca * _CAx).toInt()
            val y = _Ay + (ba * _BAy + ca * _CAy).toInt()
            return Location(x, y, 0)
        }

    init {
        _Ax = A[0]
        _Ay = A[1]

        _BAx = B[0] - A[0]
        _BAy = B[1] - A[1]

        _CAx = C[0] - A[0]
        _CAy = C[1] - A[1]

        size = Math.abs(_BAx * _CAy - _CAx * _BAy) / 2
    }

    override fun isInside(x: Int, y: Int): Boolean {
        // method parameters must be LONG, since whole calculations must be done in LONG...we are doing really big numbers
        val dx = (x - _Ax).toLong()
        val dy = (y - _Ay).toLong()

        val a = (0 - dx) * (_BAy - 0) - (_BAx - 0) * (0 - dy) >= 0
        val b = (_BAx - dx) * (_CAy - _BAy) - (_CAx - _BAx) * (_BAy - dy) >= 0
        val c = (_CAx - dx) * (0 - _CAy) - (0 - _CAx) * (_CAy - dy) >= 0

        return a == b && b == c
    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        // method parameters must be LONG, since whole calculations must be done in LONG...we are doing really big numbers
        val dx = (x - _Ax).toLong()
        val dy = (y - _Ay).toLong()

        val a = (0 - dx) * (_BAy - 0) - (_BAx - 0) * (0 - dy) >= 0
        val b = (_BAx - dx) * (_CAy - _BAy) - (_CAx - _BAx) * (_BAy - dy) >= 0
        val c = (_CAx - dx) * (0 - _CAy) - (0 - _CAx) * (_CAy - dy) >= 0

        return a == b && b == c
    }
}