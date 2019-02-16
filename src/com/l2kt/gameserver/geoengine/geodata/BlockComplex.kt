package com.l2kt.gameserver.geoengine.geodata

import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.ByteBuffer

open class BlockComplex : ABlock {
    var buffer: ByteArray? = null

    /**
     * Implicit constructor for children class.
     */
    protected constructor() {
        // buffer is initialized in children class
        buffer = null
    }

    /**
     * Creates ComplexBlock.
     * @param bb : Input byte buffer.
     * @param format : GeoFormat specifying format of loaded data.
     */
    constructor(bb: ByteBuffer, format: GeoFormat) {
        // initialize buffer
        buffer = ByteArray(GeoStructure.BLOCK_CELLS * 3)

        // load data
        for (i in 0 until GeoStructure.BLOCK_CELLS) {
            if (format !== GeoFormat.L2D) {
                // get data
                var data = bb.short

                // get nswe
                buffer!![i * 3] = (data.toInt() and 0x000F).toByte()

                // get height
                data = ((data.toInt() and 0xFFF0) shr 1).toShort()
                buffer!![i * 3 + 1] = (data.toInt() and 0x00FF).toByte()
                buffer!![i * 3 + 2] = (data.toInt() shr 8).toByte()
            } else {
                // get nswe
                val nswe = bb.get()
                buffer!![i * 3] = nswe

                // get height
                val height = bb.short
                buffer!![i * 3 + 1] = (height.toInt() and 0x00FF).toByte()
                buffer!![i * 3 + 2] = (height.toInt() shr 8).toByte()
            }
        }
    }

    override fun hasGeoPos(): Boolean {
        return true
    }

    override fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        return getHeightNearest(geoX, geoY, worldZ)
    }

    override fun getHeightAbove(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()

        // check and return height
        return if (height > worldZ) height else java.lang.Short.MIN_VALUE
    }

    override fun getHeightBelow(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()

        // check and return height
        return if (height < worldZ) height else java.lang.Short.MAX_VALUE
    }

    override fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get nswe
        return buffer!![index]
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return getNsweNearest(geoX, geoY, worldZ)
    }

    override fun getNsweAbove(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height > worldZ) buffer!![index] else 0
    }

    override fun getNsweBelow(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height < worldZ) buffer!![index] else 0
    }

    override fun getIndexNearest(geoX: Int, geoY: Int, worldZ: Int): Int {
        return (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3
    }

    override fun getIndexAbove(geoX: Int, geoY: Int, worldZ: Int): Int {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height > worldZ) index else -1
    }

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexAbove(geoX, geoY, worldZ)
    }

    override fun getIndexBelow(geoX: Int, geoY: Int, worldZ: Int): Int {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height < worldZ) index else -1
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexBelow(geoX, geoY, worldZ)
    }

    override fun getHeight(index: Int): Short {
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getHeightOriginal(index: Int): Short {
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getNswe(index: Int): Byte {
        return buffer!![index]
    }

    override fun getNsweOriginal(index: Int): Byte {
        return buffer!![index]
    }

    override fun setNswe(index: Int, nswe: Byte) {
        buffer!![index] = nswe
    }

    @Throws(IOException::class)
    override fun saveBlock(stream: BufferedOutputStream) {
        // write block type
        stream.write(GeoStructure.TYPE_COMPLEX_L2D.toInt())

        // write block data
        stream.write(buffer!!, 0, GeoStructure.BLOCK_CELLS * 3)
    }
}