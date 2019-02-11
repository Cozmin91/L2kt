package com.l2kt.commons.geometry

import java.util.ArrayList

import com.l2kt.commons.random.Rnd

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
class Polygon : AShape {

    protected val _shapes: List<AShape>

    override val size: Int

    override// not supported yet
    val area: Double
        get() = -1.0

    override// not supported yet
    val volume: Double
        get() = -1.0

    override val randomLocation: Location
        get() {
            var size = Rnd[this.size]

            for (shape in _shapes) {
                size -= shape.size
                if (size < 0)
                    return shape.randomLocation
            }
            throw Exception("Random location not found but this should never happen.")
        }

    /**
     * Constructor of the [Polygon].
     * @param shapes : List of [AShape].
     */
    constructor(shapes: List<AShape>) {
        _shapes = shapes

        var size = 0
        for (shape in shapes)
            size += shape.size
        this.size = size
    }

    /**
     * Constructor of the [Polygon]. Creates a polygon, which consists of triangles using Kong's algorithm.
     * @param id : Virtual ID of the polygon, used to separate constructor types.
     * @param points : List of `int[]` points, forming a polygon.
     */
    constructor(id: Int, points: MutableList<IntArray>) {
        var triangles: List<Triangle>? = null
        var size = 0
        try {
            // not a polygon, throw exception
            if (points.size < 3)
                throw IndexOutOfBoundsException("Can not create Polygon (id=$id) from less than 3 coordinates.")

            // get polygon orientation
            val isCw = getPolygonOrientation(points)

            // calculate non convex points
            val nonConvexPoints = calculateNonConvexPoints(points, isCw)

            // polygon triangulation of points based on orientation and non-convex points
            triangles = doTriangulationAlgorithm(points, isCw, nonConvexPoints)

            // calculate polygon size
            for (shape in triangles)
                size += shape.size
        } catch (e: Exception) {
            e.printStackTrace()
            triangles = ArrayList()
        }

        _shapes = triangles!!
        this.size = size
    }

    override fun isInside(x: Int, y: Int): Boolean {
        for (shape in _shapes)
            if (shape.isInside(x, y))
                return true

        return false
    }

    override fun isInside(x: Int, y: Int, z: Int): Boolean {
        for (shape in _shapes)
            if (shape.isInside(x, y, z))
                return true

        return false
    }

    companion object {
        private val TRIANGULATION_MAX_LOOPS = 100

        /**
         * Returns clockwise (cw) or counter-clockwise (ccw) orientation of the polygon.
         * @param points : List of all points.
         * @return `boolean` : True, when the polygon is clockwise orientated.
         */
        private fun getPolygonOrientation(points: List<IntArray>): Boolean {
            // first find point with minimum x-coord - if there are several ones take the one with maximal y-coord

            // get point
            val size = points.size
            var index = 0
            var point = points[0]
            for (i in 1 until size) {
                val pt = points[i]

                // x lower, or x same and y higher
                if (pt[0] < point[0] || pt[0] == point[0] && pt[1] > point[1]) {
                    point = pt
                    index = i
                }
            }

            // get previous point
            val pointPrev = points[getPrevIndex(size, index)]

            // get next point
            val pointNext = points[getNextIndex(size, index)]

            // get orientation
            val vx = point[0] - pointPrev[0]
            val vy = point[1] - pointPrev[1]
            val res = pointNext[0] * vy - pointNext[1] * vx + vx * pointPrev[1] - vy * pointPrev[0]

            // return
            return res <= 0
        }

        /**
         * Returns next index to given index of data container.
         * @param size : Size of the data container.
         * @param index : Index to be compared.
         * @return `int` : Next index.
         */
        private fun getNextIndex(size: Int, index: Int): Int {
            var index = index
            // increase index and check for limit
            return if (++index >= size) 0 else index

        }

        /**
         * Returns previous index to given index of data container.
         * @param size : Size of the data container.
         * @param index : Index to be compared.
         * @return `int` : Previous index.
         */
        private fun getPrevIndex(size: Int, index: Int): Int {
            var index = index
            // decrease index and check for limit
            return if (--index < 0) size - 1 else index

        }

        /**
         * This determines all concave vertices of the polygon and separate convex ones.
         * @param points : List of all points.
         * @param isCw : Polygon orientation (clockwise/counterclockwise).
         * @return `List<int[]>` : List of non-convex points.
         */
        private fun calculateNonConvexPoints(points: List<IntArray>, isCw: Boolean): List<IntArray> {
            // list of non convex points
            val nonConvexPoints = ArrayList<IntArray>()

            // result value of test function
            val size = points.size
            for (i in 0 until size - 1) {
                // get 3 points
                val point = points[i]
                val pointNext = points[i + 1]
                val pointNextNext = points[getNextIndex(size, i + 2)]

                val vx = pointNext[0] - point[0]
                val vy = pointNext[1] - point[1]

                // note: cw means res/newres is <= 0
                val res = pointNextNext[0] * vy - pointNextNext[1] * vx + vx * point[1] - vy * point[0] > 0
                if (res == isCw)
                    nonConvexPoints.add(pointNext)
            }

            return nonConvexPoints
        }

        /**
         * Perform Kong's triangulation algorithm.
         * @param points : List of all points.
         * @param isCw : Polygon orientation (clockwise/counterclockwise).
         * @param nonConvexPoints : List of all non-convex points.
         * @return `List<Triangle>` : List of [Triangle].
         * @throws Exception : When coordinates are not aligned to form monotone polygon.
         */
        @Throws(Exception::class)
        private fun doTriangulationAlgorithm(
            points: MutableList<IntArray>,
            isCw: Boolean,
            nonConvexPoints: List<IntArray>
        ): List<Triangle> {
            // create the list
            val triangles = ArrayList<Triangle>()

            var size = points.size
            var loops = 0
            var index = 1
            while (size > 3) {
                // get next and previous indexes
                val indexPrev = getPrevIndex(size, index)
                val indexNext = getNextIndex(size, index)

                // get points
                val pointPrev = points[indexPrev]
                val point = points[index]
                val pointNext = points[indexNext]

                // check point to create polygon ear
                if (isEar(isCw, nonConvexPoints, pointPrev, point, pointNext)) {
                    // create triangle from polygon ear
                    triangles.add(Triangle(pointPrev, point, pointNext))

                    // remove middle point from list, update size
                    points.removeAt(index)
                    size--

                    // move index
                    index = getPrevIndex(size, index)
                } else {
                    // move index
                    index = indexNext
                }

                if (++loops == TRIANGULATION_MAX_LOOPS)
                    throw Exception("Coordinates are not aligned to form monotone polygon.")
            }

            // add last triangle
            triangles.add(Triangle(points[0], points[1], points[2]))

            // return triangles
            return triangles
        }

        /**
         * Returns true if the triangle formed by A, B, C points is an ear considering the polygon - thus if no other point is inside and it is convex.
         * @param isCw : Polygon orientation (clockwise/counterclockwise).
         * @param nonConvexPoints : List of all non-convex points.
         * @param A : ABC triangle
         * @param B : ABC triangle
         * @param C : ABC triangle
         * @return `boolean` : True, when ABC is ear of the polygon.
         */
        private fun isEar(
            isCw: Boolean,
            nonConvexPoints: List<IntArray>,
            A: IntArray,
            B: IntArray,
            C: IntArray
        ): Boolean {
            // ABC triangle
            if (!isConvex(isCw, A, B, C))
                return false

            // iterate over all concave points and check if one of them lies inside the given triangle
            for (i in nonConvexPoints.indices) {
                if (isInside(A, B, C, nonConvexPoints[i]))
                    return false
            }

            return true
        }

        /**
         * Returns true when the point B is convex considered the actual polygon. A, B and C are three consecutive points of the polygon.
         * @param isCw : Polygon orientation (clockwise/counterclockwise).
         * @param A : Point, previous to B.
         * @param B : Point, which convex information is being checked.
         * @param C : Point, next to B.
         * @return `boolean` : True, when B is convex point.
         */
        private fun isConvex(isCw: Boolean, A: IntArray, B: IntArray, C: IntArray): Boolean {
            // get vector coordinates
            val BAx = B[0] - A[0]
            val BAy = B[1] - A[1]

            // get virtual triangle orientation
            val cw = C[0] * BAy - C[1] * BAx + BAx * A[1] - BAy * A[0] > 0

            // compare with orientation of polygon
            return cw != isCw
        }

        /**
         * Returns true, when point P is inside triangle ABC.
         * @param A : ABC triangle
         * @param B : ABC triangle
         * @param C : ABC triangle
         * @param P : Point to be checked in ABC.
         * @return `boolean` : True, when P is inside ABC.
         */
        private fun isInside(A: IntArray, B: IntArray, C: IntArray, P: IntArray): Boolean {
            // get vector coordinates
            val BAx = B[0] - A[0]
            val BAy = B[1] - A[1]
            val CAx = C[0] - A[0]
            val CAy = C[1] - A[1]
            val PAx = P[0] - A[0]
            val PAy = P[1] - A[1]

            // get determinant
            val detXYZ = (BAx * CAy - CAx * BAy).toDouble()

            // calculate BA and CA coefficient to each P from A
            val ba = (BAx * PAy - PAx * BAy) / detXYZ
            val ca = (PAx * CAy - CAx * PAy) / detXYZ

            // check coefficients
            return ba > 0 && ca > 0 && ba + ca < 1
        }
    }
}