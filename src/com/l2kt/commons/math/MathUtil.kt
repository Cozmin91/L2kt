package com.l2kt.commons.math

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature

object MathUtil {
    /**
     * @param objectsSize : The overall elements size.
     * @param pageSize : The number of elements per page.
     * @return The number of pages, based on the number of elements and the number of elements we want per page.
     */
    fun countPagesNumber(objectsSize: Int, pageSize: Int): Int {
        return objectsSize / pageSize + if (objectsSize % pageSize == 0) 0 else 1
    }

    /**
     * @param numToTest : The number to test.
     * @param min : The minimum limit.
     * @param max : The maximum limit.
     * @return the number or one of the limit (mininum / maximum).
     */
    fun limit(numToTest: Int, min: Int, max: Int): Int {
        return if (numToTest > max) max else if (numToTest < min) min else numToTest
    }

    fun calculateAngleFrom(obj1: WorldObject, obj2: WorldObject): Double {
        return calculateAngleFrom(obj1.x, obj1.y, obj2.x, obj2.y)
    }

    fun calculateAngleFrom(obj1X: Int, obj1Y: Int, obj2X: Int, obj2Y: Int): Double {
        var angleTarget = Math.toDegrees(Math.atan2((obj2Y - obj1Y).toDouble(), (obj2X - obj1X).toDouble()))
        if (angleTarget < 0)
            angleTarget = 360 + angleTarget

        return angleTarget
    }

    fun convertHeadingToDegree(clientHeading: Int): Double {
        return clientHeading / 182.04444444444444444444444444444
    }

    fun convertDegreeToClientHeading(degree: Double): Int {
        var degree = degree
        if (degree < 0)
            degree = 360 + degree

        return (degree * 182.04444444444444444444444444444).toInt()
    }

    fun calculateHeadingFrom(obj1: WorldObject, obj2: WorldObject): Int {
        return calculateHeadingFrom(obj1.x, obj1.y, obj2.x, obj2.y)
    }

    fun calculateHeadingFrom(obj1X: Int, obj1Y: Int, obj2X: Int, obj2Y: Int): Int {
        var angleTarget = Math.toDegrees(Math.atan2((obj2Y - obj1Y).toDouble(), (obj2X - obj1X).toDouble()))
        if (angleTarget < 0)
            angleTarget = 360 + angleTarget

        return (angleTarget * 182.04444444444444444444444444444).toInt()
    }

    fun calculateHeadingFrom(dx: Double, dy: Double): Int {
        var angleTarget = Math.toDegrees(Math.atan2(dy, dx))
        if (angleTarget < 0)
            angleTarget = 360 + angleTarget

        return (angleTarget * 182.04444444444444444444444444444).toInt()
    }

    fun calculateDistance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
        return calculateDistance(x1, y1, 0, x2, y2, 0, false)
    }

    fun calculateDistance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int, includeZAxis: Boolean): Double {
        val dx = x1.toDouble() - x2
        val dy = y1.toDouble() - y2

        if (includeZAxis) {
            val dz = (z1 - z2).toDouble()
            return Math.sqrt(dx * dx + dy * dy + dz * dz)
        }

        return Math.sqrt(dx * dx + dy * dy)
    }

    fun calculateDistance(obj1: WorldObject?, obj2: WorldObject?, includeZAxis: Boolean): Double {
        return if (obj1 == null || obj2 == null) 1000000.0 else calculateDistance(
            obj1.position.x,
            obj1.position.y,
            obj1.position.z,
            obj2.position.x,
            obj2.position.y,
            obj2.position.z,
            includeZAxis
        )

    }

    /**
     * Faster calculation than checkIfInRange if distance is short and collisionRadius isn't needed. Not for long distance checks (potential teleports, far away castles, etc)
     * @param radius The radius to use as check.
     * @param obj1 The position 1 to make check on.
     * @param obj2 The postion 2 to make check on.
     * @param includeZAxis Include Z check or not.
     * @return true if both objects are in the given radius.
     */
    fun checkIfInShortRadius(radius: Int, obj1: WorldObject?, obj2: WorldObject?, includeZAxis: Boolean): Boolean {
        if (obj1 == null || obj2 == null)
            return false

        if (radius == -1)
            return true // not limited

        val dx = obj1.x - obj2.x
        val dy = obj1.y - obj2.y

        if (includeZAxis) {
            val dz = obj1.z - obj2.z
            return dx * dx + dy * dy + dz * dz <= radius * radius
        }

        return dx * dx + dy * dy <= radius * radius
    }

    /**
     * This check includes collision radius of both characters.<br></br>
     * Used for accurate checks (skill casts, knownlist, etc).
     * @param range The range to use as check.
     * @param obj1 The position 1 to make check on.
     * @param obj2 The postion 2 to make check on.
     * @param includeZAxis Include Z check or not.
     * @return true if both objects are in the given radius.
     */
    fun checkIfInRange(range: Int, obj1: WorldObject?, obj2: WorldObject?, includeZAxis: Boolean): Boolean {
        if (obj1 == null || obj2 == null)
            return false

        if (range == -1)
            return true // not limited

        var rad = 0.0
        if (obj1 is Creature)
            rad += obj1.collisionRadius

        if (obj2 is Creature)
            rad += obj2.collisionRadius

        val dx = (obj1.x - obj2.x).toDouble()
        val dy = (obj1.y - obj2.y).toDouble()

        if (includeZAxis) {
            val dz = (obj1.z - obj2.z).toDouble()
            val d = dx * dx + dy * dy + dz * dz

            return d <= (range * range).toDouble() + 2.0 * range.toDouble() * rad + rad * rad
        }

        val d = dx * dx + dy * dy
        return d <= (range * range).toDouble() + 2.0 * range.toDouble() * rad + rad * rad
    }

    /**
     * Returns the rounded value of val to specified number of digits after the decimal point.<BR></BR>
     * (Based on round() in PHP)
     * @param val
     * @param numPlaces
     * @return float roundedVal
     */
    fun roundTo(`val`: Float, numPlaces: Int): Float {
        if (numPlaces <= 1)
            return Math.round(`val`).toFloat()

        val exponent = Math.pow(10.0, numPlaces.toDouble()).toFloat()

        return Math.round(`val` * exponent) / exponent
    }
}