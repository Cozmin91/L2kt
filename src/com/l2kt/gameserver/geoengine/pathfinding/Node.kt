package com.l2kt.gameserver.geoengine.pathfinding

import com.l2kt.gameserver.geoengine.geodata.GeoLocation

class Node {
    // node coords and nswe flag
    var loc: GeoLocation? = null
        private set

    // node parent (for reverse path construction)
    var parent: Node? = null
    // node child (for moving over nodes during iteration)
    var child: Node? = null

    // node G cost (movement cost = parent movement cost + current movement cost)
    var cost = -1000.0

    fun setLoc(x: Int, y: Int, z: Int) {
        loc = GeoLocation(x, y, z)
    }

    fun free() {
        // reset node location
        loc = null

        // reset node parent, child and cost
        parent = null
        child = null
        cost = -1000.0
    }
}