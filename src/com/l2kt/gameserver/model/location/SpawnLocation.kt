package com.l2kt.gameserver.model.location

/**
 * A datatype extending [Location], wildly used as character position, since it also stores heading of the character.
 */
open class SpawnLocation : Location {

    @Volatile
    var heading: Int = 0
        protected set

    constructor(x: Int, y: Int, z: Int, heading: Int) : super(x, y, z) {

        this.heading = heading
    }

    constructor(loc: SpawnLocation) : super(loc.x, loc.y, loc.z) {

        heading = loc.heading
    }

    override fun toString(): String {
        return x.toString() + ", " + y + ", " + z + ", " + heading
    }

    override fun hashCode(): Int {
        return x xor y xor z xor heading
    }

    override fun equals(o: Any?): Boolean {
        if (o is SpawnLocation) {
            val loc = o as SpawnLocation?
            return loc!!.x == x && loc.y == y && loc.z == z && loc.heading == heading
        }

        return false
    }

    operator fun set(x: Int, y: Int, z: Int, heading: Int) {
        super.set(x, y, z)

        this.heading = heading
    }

    fun set(loc: SpawnLocation) {
        super.set(loc.x, loc.y, loc.z)

        heading = loc.heading
    }

    override fun clean() {
        super.set(0, 0, 0)

        heading = 0
    }

    companion object {
        val DUMMY_SPAWNLOC = SpawnLocation(0, 0, 0, 0)
    }
}