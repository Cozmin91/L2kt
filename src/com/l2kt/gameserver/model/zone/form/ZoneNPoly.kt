package com.l2kt.gameserver.model.zone.form

import com.l2kt.gameserver.model.zone.ZoneForm

class ZoneNPoly(private val _x: IntArray, private val _y: IntArray, override val lowZ: Int, override val highZ: Int) :
    ZoneForm() {

    override fun isInsideZone(x: Int, y: Int, z: Int): Boolean {
        if (z < lowZ || z > highZ)
            return false

        var inside = false
        var i = 0
        var j = _x.size - 1
        while (i < _x.size) {
            if ((_y[i] <= y && y < _y[j] || _y[j] <= y && y < _y[i]) && x < (_x[j] - _x[i]) * (y - _y[i]) / (_y[j] - _y[i]) + _x[i])
                inside = !inside
            j = i++
        }
        return inside
    }

    override fun intersectsRectangle(ax1: Int, ax2: Int, ay1: Int, ay2: Int): Boolean {
        var tX: Int
        var tY: Int
        var uX: Int
        var uY: Int

        // First check if a point of the polygon lies inside the rectangle
        if (_x[0] in (ax1 + 1)..(ax2 - 1) && _y[0] > ay1 && _y[0] < ay2)
            return true

        // Or a point of the rectangle inside the polygon
        if (isInsideZone(ax1, ay1, highZ - 1))
            return true

        // Check every possible line of the polygon for a collision with any of the rectangles side
        for (i in _y.indices) {
            tX = _x[i]
            tY = _y[i]
            uX = _x[(i + 1) % _x.size]
            uY = _y[(i + 1) % _x.size]

            // Check if this line intersects any of the four sites of the rectangle
            if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2))
                return true

            if (lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1))
                return true

            if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2))
                return true

            if (lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1))
                return true
        }

        return false
    }

    override fun getDistanceToZone(x: Int, y: Int): Double {
        var test: Double
        var shortestDist = Math.pow((_x[0] - x).toDouble(), 2.0) + Math.pow((_y[0] - y).toDouble(), 2.0)

        for (i in 1 until _y.size) {
            test = Math.pow((_x[i] - x).toDouble(), 2.0) + Math.pow((_y[i] - y).toDouble(), 2.0)
            if (test < shortestDist)
                shortestDist = test
        }

        return Math.sqrt(shortestDist)
    }

    override fun visualizeZone(id: Int, z: Int) {
        for (i in _x.indices) {
            var nextIndex = i + 1

            // ending point to first one
            if (nextIndex == _x.size)
                nextIndex = 0

            val vx = _x[nextIndex] - _x[i]
            val vy = _y[nextIndex] - _y[i]
            var lenght = Math.sqrt((vx * vx + vy * vy).toDouble()).toFloat()
            lenght /= ZoneForm.STEP.toFloat()

            var o = 1
            while (o <= lenght) {
                val k = o / lenght

                ZoneForm.dropDebugItem(id, (_x[i] + k * vx).toInt(), (_y[i] + k * vy).toInt(), z)
                o++
            }
        }
    }
}