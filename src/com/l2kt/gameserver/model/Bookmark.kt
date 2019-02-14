package com.l2kt.gameserver.model

/**
 * A datatype used as teleportation point reminder. Used by GM admincommand //bk.
 */
data class Bookmark(val name: String, val id: Int, val x: Int, val y: Int, val z: Int)