package com.l2kt.gameserver.geoengine.geodata

import java.io.BufferedOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

open class BlockMultilayer : ABlock {

    var buffer: ByteArray? = null

    /**
     * Implicit constructor for children class.
     */
    protected constructor() {
        buffer = null
    }

    /**
     * Creates MultilayerBlock.
     * @param bb : Input byte buffer.
     * @param format : GeoFormat specifying format of loaded data.
     */
    constructor(bb: ByteBuffer, format: GeoFormat) {
        // move buffer pointer to end of MultilayerBlock
        for (cell in 0 until GeoStructure.BLOCK_CELLS) {
            // get layer count for this cell
            val layers = if (format !== GeoFormat.L2OFF) bb.get() else bb.short.toByte()

            if (layers <= 0 || layers > MAX_LAYERS)
                throw RuntimeException("Invalid layer count for MultilayerBlock")

            // add layers count
            _temp!!.put(layers)

            // loop over layers
            for (layer in 0 until layers) {
                if (format !== GeoFormat.L2D) {
                    // get data
                    val data = bb.short

                    // add nswe and height
                    _temp!!.put((data.toInt() and 0x000F).toByte())
                    _temp!!.putShort(((data.toInt() and 0xFFF0) shr 1).toShort())
                } else {
                    // add nswe
                    _temp!!.put(bb.get())

                    // add height
                    _temp!!.putShort(bb.short)
                }
            }
        }

        // initialize buffer
        buffer = Arrays.copyOf(_temp!!.array(), _temp!!.position())

        // clear temp buffer
        _temp!!.clear()
    }

    override fun hasGeoPos(): Boolean {
        return true
    }

    override fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index = getIndexNearest(geoX, geoY, worldZ)

        // get height
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        return getHeightNearest(geoX, geoY, worldZ)
    }

    override fun getHeightAbove(geoX: Int, geoY: Int, worldZ: Int): Short {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to last layer data (first from bottom)
        var layers = buffer!![index++]
        index += (layers - 1) * 3

        // loop though all layers, find first layer above worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is higher than worldZ, return layer height
            if (height > worldZ)
                return height.toShort()

            // move index to next layer
            index -= 3
        }

        // none layer found, return minimum value
        return java.lang.Short.MIN_VALUE
    }

    override fun getHeightBelow(geoX: Int, geoY: Int, worldZ: Int): Short {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from top)
        var layers = buffer!![index++]

        // loop though all layers, find first layer below worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is lower than worldZ, return layer height
            if (height < worldZ)
                return height.toShort()

            // move index to next layer
            index += 3
        }

        // none layer found, return maximum value
        return java.lang.Short.MAX_VALUE
    }

    override fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index = getIndexNearest(geoX, geoY, worldZ)

        // get nswe
        return buffer!![index]
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return getNsweNearest(geoX, geoY, worldZ)
    }

    override fun getNsweAbove(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to last layer data (first from bottom)
        var layers = buffer!![index++]
        index += (layers - 1) * 3

        // loop though all layers, find first layer above worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is higher than worldZ, return layer nswe
            if (height > worldZ)
                return buffer!![index]

            // move index to next layer
            index -= 3
        }

        // none layer found, block movement
        return 0
    }

    override fun getNsweBelow(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from top)
        var layers = buffer!![index++]

        // loop though all layers, find first layer below worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is lower than worldZ, return layer nswe
            if (height < worldZ)
                return buffer!![index]

            // move index to next layer
            index += 3
        }

        // none layer found, block movement
        return 0
    }

    override fun getIndexNearest(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from bottom)
        var layers = buffer!![index++]

        // loop though all cell layers, find closest layer
        var limit = Integer.MAX_VALUE
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // get Z distance and compare with limit
            // note: When 2 layers have same distance to worldZ (worldZ is in the middle of them):
            // > returns bottom layer
            // >= returns upper layer
            val distance = Math.abs(height - worldZ)
            if (distance > limit)
                break

            // update limit and move to next layer
            limit = distance
            index += 3
        }

        // return layer index
        return index - 3
    }

    override fun getIndexAbove(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to last layer data (first from bottom)
        var layers = buffer!![index++]
        index += (layers - 1) * 3

        // loop though all layers, find first layer above worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is higher than worldZ, return layer index
            if (height > worldZ)
                return index

            // move index to next layer
            index -= 3
        }

        // none layer found
        return -1
    }

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexAbove(geoX, geoY, worldZ)
    }

    override fun getIndexBelow(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += buffer!![index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from top)
        var layers = buffer!![index++]

        // loop though all layers, find first layer below worldZ
        while (layers-- > 0) {
            // get layer height
            val height = buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)

            // layer height is lower than worldZ, return layer index
            if (height < worldZ)
                return index

            // move index to next layer
            index += 3
        }

        // none layer found
        return -1
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        return getIndexBelow(geoX, geoY, worldZ)
    }

    override fun getHeight(index: Int): Short {
        // get height
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getHeightOriginal(index: Int): Short {
        // get height
        return (buffer!![index + 1].toInt() and 0x00FF or (buffer!![index + 2].toInt() shl 8)).toShort()
    }

    override fun getNswe(index: Int): Byte {
        // get nswe
        return buffer!![index]
    }

    override fun getNsweOriginal(index: Int): Byte {
        // get nswe
        return buffer!![index]
    }

    override fun setNswe(index: Int, nswe: Byte) {
        // set nswe
        buffer!![index] = nswe
    }

    @Throws(IOException::class)
    override fun saveBlock(stream: BufferedOutputStream) {
        // write block type
        stream.write(GeoStructure.TYPE_MULTILAYER_L2D.toInt())

        // for each cell
        var index = 0
        for (i in 0 until GeoStructure.BLOCK_CELLS) {
            // write layers count
            val layers = buffer!![index++]
            stream.write(layers.toInt())

            // write cell data
            stream.write(buffer!!, index, layers * 3)

            // move index to next cell
            index += layers * 3
        }
    }

    companion object {
        private val MAX_LAYERS = java.lang.Byte.MAX_VALUE.toInt()

        private var _temp: ByteBuffer? = null

        /**
         * Initializes the temporarily buffer.
         */
        fun initialize() {
            // initialize temporarily buffer and sorting mechanism
            _temp = ByteBuffer.allocate(GeoStructure.BLOCK_CELLS * MAX_LAYERS * 3)
            _temp!!.order(ByteOrder.LITTLE_ENDIAN)
        }

        /**
         * Releases temporarily buffer.
         */
        fun release() {
            _temp = null
        }
    }
}