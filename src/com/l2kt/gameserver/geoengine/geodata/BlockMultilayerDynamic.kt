package com.l2kt.gameserver.geoengine.geodata

import java.util.*

class BlockMultilayerDynamic
/**
 * Creates [BlockMultilayerDynamic].
 * @param bx : Block X coordinate.
 * @param by : Block Y coordinate.
 * @param block : The original MultilayerBlock to create a dynamic version from.
 */
    (private val _bx: Int, private val _by: Int, block: BlockMultilayer) : BlockMultilayer(), IBlockDynamic {
    private val _original: ByteArray
    private val _objects: MutableList<IGeoObject>

    init {
        // move buffer from ComplexBlock object to this object
        buffer = block.buffer
        block.buffer = null

        // create copy for dynamic implementation
        _original = ByteArray(buffer?.size ?: 0)
        System.arraycopy(buffer, 0, _original, 0, buffer?.size ?: 0)

        // create list for geo objects
        _objects = LinkedList()
    }// get block coordinates

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index = getIndexNearestOriginal(geoX, geoY, worldZ)

        // get height
        return (_original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)).toShort()
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index = getIndexNearestOriginal(geoX, geoY, worldZ)

        // get nswe
        return _original[index]
    }

    private fun getIndexNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += _original[index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from bottom)
        var layers = _original[index++]

        // loop though all cell layers, find closest layer
        var limit = Integer.MAX_VALUE
        while (layers-- > 0) {
            // get layer height
            val height = _original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)

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

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += _original[index] * 3 + 1
        }

        // get layers count and shift to last layer data (first from bottom)
        var layers = _original[index++]
        index += (layers - 1) * 3

        // loop though all layers, find first layer above worldZ
        while (layers-- > 0) {
            // get layer height
            val height = _original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)

            // layer height is higher than worldZ, return layer index
            if (height > worldZ)
                return index

            // move index to next layer
            index -= 3
        }

        // none layer found
        return -1
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        // move index to the cell given by coordinates
        var index = 0
        for (i in 0 until geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) {
            // move index by amount of layers for this cell
            index += _original[index] * 3 + 1
        }

        // get layers count and shift to first layer data (first from top)
        var layers = _original[index++]

        // loop though all layers, find first layer below worldZ
        while (layers-- > 0) {
            // get layer height
            val height = _original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)

            // layer height is lower than worldZ, return layer index
            if (height < worldZ)
                return index

            // move index to next layer
            index += 3
        }

        // none layer found
        return -1
    }

    override fun getHeightOriginal(index: Int): Short {
        // get height
        return (_original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)).toShort()
    }

    override fun getNsweOriginal(index: Int): Byte {
        // get nswe
        return _original[index]
    }

    @Synchronized
    override fun addGeoObject(`object`: IGeoObject) {
        // add geo object, update block geodata when added
        if (_objects.add(`object`))
            update()
    }

    @Synchronized
    override fun removeGeoObject(`object`: IGeoObject) {
        // remove geo object, update block geodata when removed
        if (_objects.remove(`object`))
            update()
    }

    private fun update() {
        // copy original geodata, than apply changes
        System.arraycopy(_original, 0, buffer, 0, _original.size)

        // get block geo coordinates
        val minBX = _bx * GeoStructure.BLOCK_CELLS_X
        val minBY = _by * GeoStructure.BLOCK_CELLS_Y
        val maxBX = minBX + GeoStructure.BLOCK_CELLS_X
        val maxBY = minBY + GeoStructure.BLOCK_CELLS_Y

        // for all objects
        for (`object` in _objects) {
            // get object geo coordinates and other object variables
            val minOX = `object`.geoX
            val minOY = `object`.geoY
            val minOZ = `object`.geoZ
            val maxOZ = minOZ + `object`.height
            val geoData = `object`.objectGeoData

            // calculate min/max geo coordinates for iteration (intersection of block and object)
            val minGX = Math.max(minBX, minOX)
            val minGY = Math.max(minBY, minOY)
            val maxGX = Math.min(maxBX, minOX + geoData.size)
            val maxGY = Math.min(maxBY, minOY + geoData[0].size)

            // iterate over intersection of block and object
            for (gx in minGX until maxGX) {
                for (gy in minGY until maxGY) {
                    // get object nswe
                    val objNswe = geoData[gx - minOX][gy - minOY]

                    // object contains no change of data in this cell, continue to next cell
                    if (objNswe.toInt() == 0xFF)
                        continue

                    // get block index of this cell
                    val ib = getIndexNearest(gx, gy, minOZ)

                    // compare block data and original data, when height differs -> height was affected by other geo object
                    // -> cell is inside an object -> no need to check/change it anymore (Z is lifted, nswe is 0)
                    // compare is done in raw format (2 bytes) instead of conversion to short
                    if (buffer!![ib + 1] != _original[ib + 1] || buffer!![ib + 2] != _original[ib + 2])
                        continue

                    // so far cell is not inside of any object
                    if (objNswe.toInt() == 0) {
                        // cell is inside of this object -> set nswe to 0 and lift Z up

                        // set block nswe
                        buffer!![ib] = 0

                        // calculate object height, limit to next layer
                        var z = maxOZ
                        val i = getIndexAbove(gx, gy, minOZ)
                        if (i != -1) {
                            val az = getHeight(i).toInt()
                            if (az <= maxOZ)
                                z = az - GeoStructure.CELL_IGNORE_HEIGHT
                        }

                        // set block Z to object height
                        buffer!![ib + 1] = (z and 0x00FF).toByte()
                        buffer!![ib + 2] = (z shr 8).toByte()
                    } else {
                        // cell is outside of this object -> update nswe

                        // height different is too high (trying to update another layer), skip
                        val z = getHeight(ib)
                        if (Math.abs(z - minOZ) > GeoStructure.CELL_IGNORE_HEIGHT)
                            continue

                        // adjust block nswe according to the object nswe
                        buffer!![ib] = (buffer!![ib].toInt() and objNswe.toInt()).toByte()
                    }
                }
            }
        }
    }
}