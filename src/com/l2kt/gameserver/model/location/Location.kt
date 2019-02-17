package com.l2kt.gameserver.model.location

import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype used to retain a 3D (x/y/z) point. It got the capability to be set and cleaned.
 */
open class Location {

    @Volatile
    open var x: Int = 0
        protected set
    @Volatile
    open var y: Int = 0
        protected set
    @Volatile
    var z: Int = 0
        protected set

    constructor(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    constructor(loc: Location) {
        this.x = loc.x
        this.y = loc.y
        this.z = loc.z
    }

    constructor(loc: StatsSet) {
        this.x = loc.getInteger("x")
        this.y = loc.getInteger("y")
        this.z = loc.getInteger("z")
    }

    override fun toString(): String {
        return x.toString() + ", " + y + ", " + z
    }

    override fun hashCode(): Int {
        return x xor y xor z
    }

    override fun equals(o: Any?): Boolean {
        if (o is Location) {
            val loc = o as Location?
            return loc!!.x == x && loc.y == y && loc.z == z
        }

        return false
    }

    operator fun set(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun set(loc: Location) {
        x = loc.x
        y = loc.y
        z = loc.z
    }

    open fun clean() {
        x = 0
        y = 0
        z = 0
    }

    companion object {
        val DUMMY_LOC = Location(0, 0, 0)
    }
}