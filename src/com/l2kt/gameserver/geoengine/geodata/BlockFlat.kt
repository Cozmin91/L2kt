package com.l2kt.gameserver.geoengine.geodata

import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.ByteBuffer

class BlockFlat
/**
 * Creates FlatBlock.
 * @param bb : Input byte buffer.
 * @param format : GeoFormat specifying format of loaded data.
 */
    (bb: ByteBuffer, format: GeoFormat) : ABlock() {
    val _height: Short
    var _nswe: Byte = 0

    init {
        _height = bb.short
        _nswe = if (format !== GeoFormat.L2D) 0x0F else 0xFF.toByte()

        if (format === GeoFormat.L2OFF)
            bb.short
    }

    override fun hasGeoPos(): Boolean {
        return true
    }

    override fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short {
        return _height
    }

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        return _height
    }

    override fun getHeightAbove(geoX: Int, geoY: Int, worldZ: Int): Short {
        // check and return height
        return if (_height > worldZ) _height else java.lang.Short.MIN_VALUE
    }

    override fun getHeightBelow(geoX: Int, geoY: Int, worldZ: Int): Short {
        // check and return height
        return if (_height < worldZ) _height else java.lang.Short.MAX_VALUE
    }

    override fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return _nswe
    }

    override fun getNsweAbove(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // check height and return nswe
        return if (_height > worldZ) _nswe else 0
    }

    override fun getNsweBelow(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // check height and return nswe
        return if (_height < worldZ) _nswe else 0
    }

    override fun getIndexNearest(geoX: Int, geoY: Int, worldZ: Int): Int {
        return 0
    }

    override fun getIndexAbove(geoX: Int, geoY: Int, worldZ: Int): Int {
        // check height and return index
        return if (_height > worldZ) 0 else -1
    }

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexAbove(geoX, geoY, worldZ)
    }

    override fun getIndexBelow(geoX: Int, geoY: Int, worldZ: Int): Int {
        // check height and return index
        return if (_height < worldZ) 0 else -1
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexBelow(geoX, geoY, worldZ)
    }

    override fun getHeight(index: Int): Short {
        return _height
    }

    override fun getHeightOriginal(index: Int): Short {
        return _height
    }

    override fun getNswe(index: Int): Byte {
        return _nswe
    }

    override fun getNsweOriginal(index: Int): Byte {
        return _nswe
    }

    override fun setNswe(index: Int, nswe: Byte) {
        _nswe = nswe
    }

    @Throws(IOException::class)
    override fun saveBlock(stream: BufferedOutputStream) {
        // write block type
        stream.write(GeoStructure.TYPE_FLAT_L2D.toInt())

        // write height
        stream.write((_height.toInt() and 0x00FF).toByte().toInt())
        stream.write((_height.toInt() shr 8).toByte().toInt())
    }
}