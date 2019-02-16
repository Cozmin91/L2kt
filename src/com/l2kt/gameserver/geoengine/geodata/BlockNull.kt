package com.l2kt.gameserver.geoengine.geodata

import java.io.BufferedOutputStream

class BlockNull : ABlock() {
    private val _nswe: Byte

    init {
        _nswe = 0xFF.toByte()
    }

    override fun hasGeoPos(): Boolean {
        return false
    }

    override fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short {
        return worldZ.toShort()
    }

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        return worldZ.toShort()
    }

    override fun getHeightAbove(geoX: Int, geoY: Int, worldZ: Int): Short {
        return worldZ.toShort()
    }

    override fun getHeightBelow(geoX: Int, geoY: Int, worldZ: Int): Short {
        return worldZ.toShort()
    }

    override fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getNsweAbove(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getNsweBelow(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getIndexNearest(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getIndexAbove(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getIndexBelow(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getHeight(index: Int): Short {
        return 0
    }

    override fun getHeightOriginal(index: Int): Short {
        return 0
    }

    override fun getNswe(index: Int): Byte {
        return _nswe
    }

    override fun getNsweOriginal(index: Int): Byte {
        return _nswe
    }

    override fun setNswe(index: Int, nswe: Byte) {}

    override fun saveBlock(stream: BufferedOutputStream) {}
}