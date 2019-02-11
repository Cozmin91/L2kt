package com.l2kt.commons.geometry

import com.l2kt.gameserver.model.location.Location

/**
 * @author Hasha
 */
abstract class AShape {
    /**
     * Returns size of the AShape floor projection.
     * @return int : Size.
     */
    abstract val size: Int

    /**
     * Returns surface area of the AShape.
     * @return double : Surface area.
     */
    abstract val area: Double

    /**
     * Returns enclosed volume of the AShape.
     * @return double : Enclosed volume.
     */
    abstract val volume: Double

    /**
     * Returns [Location] of random point inside AShape.<br></br>
     * In case AShape is only in 2D space, Z is set as 0.
     * @return [Location] : Random location inside AShape.
     */
    abstract val randomLocation: Location

    /**
     * Checks if given X, Y coordinates are laying inside the AShape.
     * @param x : World X coordinates.
     * @param y : World Y coordinates.
     * @return boolean : True, when if coordinates are inside this AShape.
     */
    abstract fun isInside(x: Int, y: Int): Boolean

    /**
     * Checks if given X, Y, Z coordinates are laying inside the AShape.
     * @param x : World X coordinates.
     * @param y : World Y coordinates.
     * @param z : World Z coordinates.
     * @return boolean : True, when if coordinates are inside this AShape.
     */
    abstract fun isInside(x: Int, y: Int, z: Int): Boolean
}