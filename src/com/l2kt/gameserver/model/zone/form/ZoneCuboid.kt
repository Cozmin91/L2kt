package com.l2kt.gameserver.model.zone.form

import com.l2kt.gameserver.model.zone.ZoneForm

class ZoneCuboid(x1: Int, x2: Int, y1: Int, y2: Int, z1: Int, z2: Int) : ZoneForm() {
    private var _x1: Int = 0
    private var _x2: Int = 0
    private var _y1: Int = 0
    private var _y2: Int = 0
    override var lowZ: Int = 0
        private set
    override var highZ: Int = 0
        private set

    init {
        _x1 = x1
        _x2 = x2

        // switch them if alignment is wrong
        if (_x1 > _x2) {
            _x1 = x2
            _x2 = x1
        }

        _y1 = y1
        _y2 = y2

        // switch them if alignment is wrong
        if (_y1 > _y2) {
            _y1 = y2
            _y2 = y1
        }

        lowZ = z1
        highZ = z2

        // switch them if alignment is wrong
        if (lowZ > highZ) {
            lowZ = z2
            highZ = z1
        }
    }

    override fun isInsideZone(x: Int, y: Int, z: Int): Boolean {
        return !(x < _x1 || x > _x2 || y < _y1 || y > _y2 || z < lowZ || z > highZ)

    }

    override fun intersectsRectangle(ax1: Int, ax2: Int, ay1: Int, ay2: Int): Boolean {
        // Check if any point inside this rectangle
        if (isInsideZone(ax1, ay1, highZ - 1))
            return true

        if (isInsideZone(ax1, ay2, highZ - 1))
            return true

        if (isInsideZone(ax2, ay1, highZ - 1))
            return true

        if (isInsideZone(ax2, ay2, highZ - 1))
            return true

        // Check if any point from this rectangle is inside the other one
        if (_x1 in (ax1 + 1)..(ax2 - 1) && _y1 > ay1 && _y1 < ay2)
            return true

        if (_x1 in (ax1 + 1)..(ax2 - 1) && _y2 > ay1 && _y2 < ay2)
            return true

        if (_x2 in (ax1 + 1)..(ax2 - 1) && _y1 > ay1 && _y1 < ay2)
            return true

        if (_x2 in (ax1 + 1)..(ax2 - 1) && _y2 > ay1 && _y2 < ay2)
            return true

        // Horizontal lines may intersect vertical lines
        if (lineSegmentsIntersect(_x1, _y1, _x2, _y1, ax1, ay1, ax1, ay2))
            return true

        if (lineSegmentsIntersect(_x1, _y1, _x2, _y1, ax2, ay1, ax2, ay2))
            return true

        if (lineSegmentsIntersect(_x1, _y2, _x2, _y2, ax1, ay1, ax1, ay2))
            return true

        if (lineSegmentsIntersect(_x1, _y2, _x2, _y2, ax2, ay1, ax2, ay2))
            return true

        // Vertical lines may intersect horizontal lines
        if (lineSegmentsIntersect(_x1, _y1, _x1, _y2, ax1, ay1, ax2, ay1))
            return true

        if (lineSegmentsIntersect(_x1, _y1, _x1, _y2, ax1, ay2, ax2, ay2))
            return true

        if (lineSegmentsIntersect(_x2, _y1, _x2, _y2, ax1, ay1, ax2, ay1))
            return true

        return lineSegmentsIntersect(_x2, _y1, _x2, _y2, ax1, ay2, ax2, ay2)

    }

    override fun getDistanceToZone(x: Int, y: Int): Double {
        var test: Double = Math.pow((_x1 - x).toDouble(), 2.0) + Math.pow((_y2 - y).toDouble(), 2.0)
        var shortestDist = Math.pow((_x1 - x).toDouble(), 2.0) + Math.pow((_y1 - y).toDouble(), 2.0)

        if (test < shortestDist)
            shortestDist = test

        test = Math.pow((_x2 - x).toDouble(), 2.0) + Math.pow((_y1 - y).toDouble(), 2.0)
        if (test < shortestDist)
            shortestDist = test

        test = Math.pow((_x2 - x).toDouble(), 2.0) + Math.pow((_y2 - y).toDouble(), 2.0)
        if (test < shortestDist)
            shortestDist = test

        return Math.sqrt(shortestDist)
    }

    override fun visualizeZone(id: Int, z: Int) {
        // x1->x2
        var x = _x1
        while (x < _x2) {
            ZoneForm.dropDebugItem(id, x, _y1, z)
            ZoneForm.dropDebugItem(id, x, _y2, z)
            x += ZoneForm.STEP
        }

        // y1->y2
        var y = _y1
        while (y < _y2) {
            ZoneForm.dropDebugItem(id, _x1, y, z)
            ZoneForm.dropDebugItem(id, _x2, y, z)
            y += ZoneForm.STEP
        }
    }
}