package com.l2kt.gameserver.model.zone.form

import com.l2kt.gameserver.model.zone.ZoneForm

class ZoneCylinder(
    private val _x: Int,
    private val _y: Int,
    override val lowZ: Int,
    override val highZ: Int,
    private val _rad: Int
) : ZoneForm() {
    private val _radS: Int = _rad * _rad

    override fun isInsideZone(x: Int, y: Int, z: Int): Boolean {
        return !(Math.pow((_x - x).toDouble(), 2.0) + Math.pow(
            (_y - y).toDouble(),
            2.0
        ) > _radS || z < lowZ || z > highZ)

    }

    override fun intersectsRectangle(ax1: Int, ax2: Int, ay1: Int, ay2: Int): Boolean {
        // Circles point inside the rectangle?
        if (_x in (ax1 + 1)..(ax2 - 1) && _y > ay1 && _y < ay2)
            return true

        // Any point of the rectangle intersecting the Circle?
        if (Math.pow((ax1 - _x).toDouble(), 2.0) + Math.pow((ay1 - _y).toDouble(), 2.0) < _radS)
            return true

        if (Math.pow((ax1 - _x).toDouble(), 2.0) + Math.pow((ay2 - _y).toDouble(), 2.0) < _radS)
            return true

        if (Math.pow((ax2 - _x).toDouble(), 2.0) + Math.pow((ay1 - _y).toDouble(), 2.0) < _radS)
            return true

        if (Math.pow((ax2 - _x).toDouble(), 2.0) + Math.pow((ay2 - _y).toDouble(), 2.0) < _radS)
            return true

        // Collision on any side of the rectangle?
        if (_x in (ax1 + 1)..(ax2 - 1)) {
            if (Math.abs(_y - ay2) < _rad)
                return true

            if (Math.abs(_y - ay1) < _rad)
                return true
        }

        if (_y in (ay1 + 1)..(ay2 - 1)) {
            if (Math.abs(_x - ax2) < _rad)
                return true

            if (Math.abs(_x - ax1) < _rad)
                return true
        }

        return false
    }

    override fun getDistanceToZone(x: Int, y: Int): Double {
        return Math.sqrt(Math.pow((_x - x).toDouble(), 2.0) + Math.pow((_y - y).toDouble(), 2.0)) - _rad
    }

    override fun visualizeZone(id: Int, z: Int) {
        val count = (2.0 * Math.PI * _rad.toDouble() / ZoneForm.STEP).toInt()
        val angle = 2 * Math.PI / count

        for (i in 0 until count) {
            val x = (Math.cos(angle * i) * _rad).toInt()
            val y = (Math.sin(angle * i) * _rad).toInt()

            ZoneForm.dropDebugItem(id, _x + x, _y + y, z)
        }
    }
}