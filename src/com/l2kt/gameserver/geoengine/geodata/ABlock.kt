package com.l2kt.gameserver.geoengine.geodata

import java.io.BufferedOutputStream
import java.io.IOException

abstract class ABlock {
    /**
     * Checks the block for having geodata.
     * @return boolean : True, when block has geodata (Flat, Complex, Multilayer).
     */
    abstract fun hasGeoPos(): Boolean

    /**
     * Returns the height of cell, which is closest to given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, nearest to given coordinates.
     */
    abstract fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short

    /**
     * Returns the height of cell, which is closest to given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, nearest to given coordinates.
     */
    abstract fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short

    /**
     * Returns the height of cell, which is first above given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, above given coordinates.
     */
    abstract fun getHeightAbove(geoX: Int, geoY: Int, worldZ: Int): Short

    /**
     * Returns the height of cell, which is first below given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, below given coordinates.
     */
    abstract fun getHeightBelow(geoX: Int, geoY: Int, worldZ: Int): Short

    /**
     * Returns the NSWE flag byte of cell, which is closest to given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte, nearest to given coordinates.
     */
    abstract fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte

    /**
     * Returns the NSWE flag byte of cell, which is closest to given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte, nearest to given coordinates.
     */
    abstract fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte

    /**
     * Returns the NSWE flag byte of cell, which is first above given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte, nearest to given coordinates.
     */
    abstract fun getNsweAbove(geoX: Int, geoY: Int, worldZ: Int): Byte

    /**
     * Returns the NSWE flag byte of cell, which is first below given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte, nearest to given coordinates.
     */
    abstract fun getNsweBelow(geoX: Int, geoY: Int, worldZ: Int): Byte

    /**
     * Returns index to data of the cell, which is closes layer to given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return `int` : Cell index.
     */
    abstract fun getIndexNearest(geoX: Int, geoY: Int, worldZ: Int): Int

    /**
     * Returns index to data of the cell, which is first layer above given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return `int` : Cell index. -1..when no layer available below given Z coordinate.
     */
    abstract fun getIndexAbove(geoX: Int, geoY: Int, worldZ: Int): Int

    /**
     * Returns index to data of the cell, which is first layer above given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return `int` : Cell index. -1..when no layer available below given Z coordinate.
     */
    abstract fun getIndexAboveOriginal(geoX: Int, geoY: Int, worldZ: Int): Int

    /**
     * Returns index to data of the cell, which is first layer below given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return `int` : Cell index. -1..when no layer available below given Z coordinate.
     */
    abstract fun getIndexBelow(geoX: Int, geoY: Int, worldZ: Int): Int

    /**
     * Returns index to data of the cell, which is first layer below given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return `int` : Cell index. -1..when no layer available below given Z coordinate.
     */
    abstract fun getIndexBelowOriginal(geoX: Int, geoY: Int, worldZ: Int): Int

    /**
     * Returns the height of cell given by cell index.
     * @param index : Index of the cell.
     * @return short : Cell geodata Z coordinate, below given coordinates.
     */
    abstract fun getHeight(index: Int): Short

    /**
     * Returns the height of cell given by cell index.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param index : Index of the cell.
     * @return short : Cell geodata Z coordinate, below given coordinates.
     */
    abstract fun getHeightOriginal(index: Int): Short

    /**
     * Returns the NSWE flag byte of cell given by cell index.
     * @param index : Index of the cell.
     * @return short : Cell geodata Z coordinate, below given coordinates.
     */
    abstract fun getNswe(index: Int): Byte

    /**
     * Returns the NSWE flag byte of cell given by cell index.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param index : Index of the cell.
     * @return short : Cell geodata Z coordinate, below given coordinates.
     */
    abstract fun getNsweOriginal(index: Int): Byte

    /**
     * Sets the NSWE flag byte of cell given by cell index.
     * @param index : Index of the cell.
     * @param nswe : New NSWE flag byte.
     */
    abstract fun setNswe(index: Int, nswe: Byte)

    /**
     * Saves the block in L2D format to [BufferedOutputStream]. Used only for L2D geodata conversion.
     * @param stream : The stream.
     * @throws IOException : Can't save the block to steam.
     */
    @Throws(IOException::class)
    abstract fun saveBlock(stream: BufferedOutputStream)
}