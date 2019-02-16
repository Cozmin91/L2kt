package com.l2kt.gameserver.geoengine.geodata

import java.util.*

class BlockComplexDynamic : BlockComplex, IBlockDynamic {
    private val _bx: Int
    private val _by: Int
    private val _original: ByteArray
    private val _objects: MutableList<IGeoObject>

    /**
     * Creates [BlockComplexDynamic].
     * @param bx : Block X coordinate.
     * @param by : Block Y coordinate.
     * @param block : The original FlatBlock to create a dynamic version from.
     */
    constructor(bx: Int, by: Int, block: BlockFlat) {
        // load data
        val nswe = block._nswe
        val heightLow = (block._height.toInt() and 0x00FF).toByte()
        val heightHigh = (block._height.toInt() shr 8).toByte()

        // initialize buffer
        buffer = ByteArray(GeoStructure.BLOCK_CELLS * 3)

        // save data
        for (i in 0 until GeoStructure.BLOCK_CELLS) {
            // set nswe
            buffer!![i * 3] = nswe

            // set height
            buffer!![i * 3 + 1] = heightLow
            buffer!![i * 3 + 2] = heightHigh
        }

        // get block coordinates
        _bx = bx
        _by = by

        // create copy for dynamic implementation
        _original = ByteArray(GeoStructure.BLOCK_CELLS * 3)
        System.arraycopy(buffer, 0, _original, 0, GeoStructure.BLOCK_CELLS * 3)

        // create list for geo objects
        _objects = LinkedList()
    }

    /**
     * Creates [BlockComplexDynamic].
     * @param bx : Block X coordinate.
     * @param by : Block Y coordinate.
     * @param block : The original ComplexBlock to create a dynamic version from.
     */
    constructor(bx: Int, by: Int, block: BlockComplex) {
        // move buffer from BlockComplex object to this object
        buffer = block.buffer
        block.buffer = null

        // get block coordinates
        _bx = bx
        _by = by

        // create copy for dynamic implementation
        _original = ByteArray(GeoStructure.BLOCK_CELLS * 3)
        System.arraycopy(buffer, 0, _original, 0, GeoStructure.BLOCK_CELLS * 3)

        // create list for geo objects
        _objects = LinkedList()
    }

    override fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        return (_original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)).toShort()
    }

    override fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get nswe
        return _original[index]
    }

    override fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = _original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height > worldZ) index else -1
    }

    override fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int {
        // get cell index
        val index =
            (geoX % GeoStructure.BLOCK_CELLS_X * GeoStructure.BLOCK_CELLS_Y + geoY % GeoStructure.BLOCK_CELLS_Y) * 3

        // get height
        val height = _original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)

        // check height and return nswe
        return if (height < worldZ) index else -1
    }

    override fun getHeightOriginal(index: Int): Short {
        return (_original[index + 1].toInt() and 0x00FF or (_original[index + 2].toInt() shl 8)).toShort()
    }

    override fun getNsweOriginal(index: Int): Byte {
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
        System.arraycopy(_original, 0, buffer, 0, GeoStructure.BLOCK_CELLS * 3)

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
                    val ib = ((gx - minBX) * GeoStructure.BLOCK_CELLS_Y + (gy - minBY)) * 3

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

                        // set block Z to object height
                        buffer!![ib + 1] = (maxOZ and 0x00FF).toByte()
                        buffer!![ib + 2] = (maxOZ shr 8).toByte()
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