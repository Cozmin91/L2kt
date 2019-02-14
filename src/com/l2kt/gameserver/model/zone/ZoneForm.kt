package com.l2kt.gameserver.model.zone

import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.item.instance.ItemInstance

/**
 * Abstract base class for any zone form.
 */
abstract class ZoneForm {

    abstract val lowZ: Int // Support for the ability to extract the z coordinates of zones.

    abstract val highZ: Int // New fishing patch makes use of that to get the Z for the hook

    abstract fun isInsideZone(x: Int, y: Int, z: Int): Boolean

    abstract fun intersectsRectangle(x1: Int, x2: Int, y1: Int, y2: Int): Boolean

    abstract fun getDistanceToZone(x: Int, y: Int): Double

    abstract fun visualizeZone(id: Int, z: Int)

    protected fun lineSegmentsIntersect(ax1: Int, ay1: Int, ax2: Int, ay2: Int, bx1: Int, by1: Int, bx2: Int, by2: Int): Boolean {
        return java.awt.geom.Line2D.linesIntersect(ax1.toDouble(), ay1.toDouble(), ax2.toDouble(), ay2.toDouble(), bx1.toDouble(), by1.toDouble(), bx2.toDouble(), by2.toDouble())
    }

    companion object {
        const val STEP = 50

        fun dropDebugItem(id: Int, x: Int, y: Int, z: Int) {
            val item = ItemInstance(IdFactory.getInstance().nextId, 57)
            item.count = id
            item.spawnMe(x, y, z + 5)

            ZoneManager.getInstance().addDebugItem(item)
        }
    }
}