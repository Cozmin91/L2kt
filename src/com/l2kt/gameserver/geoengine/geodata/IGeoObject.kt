package com.l2kt.gameserver.geoengine.geodata

interface IGeoObject {
    /**
     * Returns geodata X coordinate of the [IGeoObject].
     * @return int : Geodata X coordinate.
     */
    val geoX: Int

    /**
     * Returns geodata Y coordinate of the [IGeoObject].
     * @return int : Geodata Y coordinate.
     */
    val geoY: Int

    /**
     * Returns geodata Z coordinate of the [IGeoObject].
     * @return int : Geodata Z coordinate.
     */
    val geoZ: Int

    /**
     * Returns height of the [IGeoObject].
     * @return int : Height.
     */
    val height: Int

    /**
     * Returns [IGeoObject] data.
     * @return byte[][] : [IGeoObject] data.
     */
    val objectGeoData: Array<ByteArray>
}
