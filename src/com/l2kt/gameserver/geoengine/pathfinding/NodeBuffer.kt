package com.l2kt.gameserver.geoengine.pathfinding

import com.l2kt.Config
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.geoengine.geodata.GeoStructure
import java.util.*
import java.util.concurrent.locks.ReentrantLock

class NodeBuffer
/**
 * Constructor of NodeBuffer.
 * @param size : one dimension size of buffer
 */
    (private val _size: Int) {
    private val _lock = ReentrantLock()
    private val _buffer: Array<Array<Node?>> = Array<Array<Node?>>(_size) { arrayOfNulls(_size) }

    // center coordinates
    private var _cx = 0
    private var _cy = 0

    // target coordinates
    private var _gtx = 0
    private var _gty = 0
    private var _gtz: Short = 0

    // pathfinding statistics
    private var _timeStamp: Long = 0
    var elapsedTime: Long = 0
        private set

    private var _current: Node? = null

    val isLocked: Boolean
        get() = _lock.tryLock()

    init {

        // initialize buffer
        for (x in 0 until _size)
            for (y in 0 until _size)
                _buffer[x][y] = Node()
    }// set size

    /**
     * Find path consisting of Nodes. Starts at origin coordinates, ends in target coordinates.
     * @param gox : origin point x
     * @param goy : origin point y
     * @param goz : origin point z
     * @param gtx : target point x
     * @param gty : target point y
     * @param gtz : target point z
     * @return Node : first node of path
     */
    fun findPath(gox: Int, goy: Int, goz: Short, gtx: Int, gty: Int, gtz: Short): Node? {
        // load timestamp
        _timeStamp = System.currentTimeMillis()

        // set coordinates (middle of the line (gox,goy) - (gtx,gty), will be in the center of the buffer)
        _cx = gox + (gtx - gox - _size) / 2
        _cy = goy + (gty - goy - _size) / 2

        _gtx = gtx
        _gty = gty
        _gtz = gtz

        _current = getNode(gox, goy, goz)
        _current!!.cost = getCostH(gox, goy, goz.toInt())

        var count = 0
        do {
            // reached target?
            if (_current!!.loc!!.geoX == _gtx && _current!!.loc!!.geoY == _gty && Math.abs(_current!!.loc!!.z - _gtz) < 8)
                return _current

            // expand current node
            expand()

            // move pointer
            _current = _current!!.child
        } while (_current != null && ++count < Config.MAX_ITERATIONS)

        return null
    }

    /**
     * Creates list of Nodes to show debug path.
     * @return List<Node> : nodes
    </Node> */
    fun debugPath(): List<Node> {
        val result = ArrayList<Node>()

        var n = _current
        while (n!!.parent != null) {
            result.add(n)
            n.cost = -n.cost
            n = n.parent
        }

        for (nodes in _buffer) {
            for (node in nodes) {
                if (node?.loc == null || node.cost <= 0)
                    continue

                result.add(node)
            }
        }

        return result
    }

    fun free() {
        _current = null

        for (nodes in _buffer)
            for (node in nodes)
                if (node?.loc != null)
                    node.free()

        _lock.unlock()
        elapsedTime = System.currentTimeMillis() - _timeStamp
    }

    /**
     * Check _current Node and add its neighbors to the buffer.
     */
    private fun expand() {
        // can't move anywhere, don't expand
        val nswe = _current!!.loc!!.nswe
        if (nswe.toInt() == 0)
            return

        // get geo coords of the node to be expanded
        val x = _current!!.loc!!.geoX
        val y = _current!!.loc!!.geoY
        val z = _current!!.loc!!.z.toShort()

        // can move north, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_N.toInt()) != 0)
            addNode(x, y - 1, z, Config.BASE_WEIGHT)

        // can move south, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_S.toInt()) != 0)
            addNode(x, y + 1, z, Config.BASE_WEIGHT)

        // can move west, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_W.toInt()) != 0)
            addNode(x - 1, y, z, Config.BASE_WEIGHT)

        // can move east, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_E.toInt()) != 0)
            addNode(x + 1, y, z, Config.BASE_WEIGHT)

        // can move north-west, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_NW.toInt()) != 0)
            addNode(x - 1, y - 1, z, Config.DIAGONAL_WEIGHT)

        // can move north-east, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_NE.toInt()) != 0)
            addNode(x + 1, y - 1, z, Config.DIAGONAL_WEIGHT)

        // can move south-west, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_SW.toInt()) != 0)
            addNode(x - 1, y + 1, z, Config.DIAGONAL_WEIGHT)

        // can move south-east, expand
        if ((nswe.toInt() and GeoStructure.CELL_FLAG_SE.toInt()) != 0)
            addNode(x + 1, y + 1, z, Config.DIAGONAL_WEIGHT)
    }

    /**
     * Returns node, if it exists in buffer.
     * @param x : node X coord
     * @param y : node Y coord
     * @param z : node Z coord
     * @return Node : node, if exits in buffer
     */
    private fun getNode(x: Int, y: Int, z: Short): Node? {
        // check node X out of coordinates
        val ix = x - _cx
        if (ix < 0 || ix >= _size)
            return null

        // check node Y out of coordinates
        val iy = y - _cy
        if (iy < 0 || iy >= _size)
            return null

        // get node
        val result = _buffer[ix][iy]

        // check and update
        if (result?.loc == null)
            result?.setLoc(x, y, z.toInt())

        // return node
        return result
    }

    /**
     * Add node given by coordinates to the buffer.
     * @param x : geo X coord
     * @param y : geo Y coord
     * @param z : geo Z coord
     * @param weight : weight of movement to new node
     */
    private fun addNode(x: Int, y: Int, z: Short, weight: Int) {
        // get node to be expanded
        val node = getNode(x, y, z) ?: return

        // Z distance between nearby cells is higher than cell size, record as geodata bug
        if (node.loc!!.z > z + 2 * GeoStructure.CELL_HEIGHT) {
            if (Config.DEBUG_GEO_NODE)
                GeoEngine.addGeoBug(node.loc!!, "NodeBufferDiag: Check Z coords.")

            return
        }

        // node was already expanded, return
        if (node.cost >= 0)
            return

        node.parent = _current
        if (node.loc!!.nswe != 0xFF.toByte())
            node.cost = getCostH(x, y, node.loc!!.z) + weight * Config.OBSTACLE_MULTIPLIER
        else
            node.cost = getCostH(x, y, node.loc!!.z) + weight

        var current = _current
        var count = 0
        while (current!!.child != null && count < Config.MAX_ITERATIONS * 4) {
            count++
            if (current.child!!.cost > node.cost) {
                node.child = current.child
                break
            }
            current = current.child
        }

        if (count >= Config.MAX_ITERATIONS * 4)
            System.err.println("Pathfinding: too long loop detected, cost:" + node.cost)

        current.child = node
    }

    /**
     * @param x : node X coord
     * @param y : node Y coord
     * @param i : node Z coord
     * @return double : node cost
     */
    private fun getCostH(x: Int, y: Int, i: Int): Double {
        val dX = x - _gtx
        val dY = y - _gty
        val dZ = (i - _gtz) / GeoStructure.CELL_HEIGHT

        // return (Math.abs(dX) + Math.abs(dY) + Math.abs(dZ)) * Config.HEURISTIC_WEIGHT; // Manhattan distance
        return Math.sqrt((dX * dX + dY * dY + dZ * dZ).toDouble()) * Config.HEURISTIC_WEIGHT // Direct distance
    }
}