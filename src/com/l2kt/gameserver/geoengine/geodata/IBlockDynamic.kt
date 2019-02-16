package com.l2kt.gameserver.geoengine.geodata

interface IBlockDynamic {
    /**
     * Adds [IGeoObject] to the [ABlock]. The block will update geodata according the object.
     * @param object : [IGeoObject] to be added.
     */
    fun addGeoObject(`object`: IGeoObject)

    /**
     * Removes [IGeoObject] from the [ABlock]. The block will update geodata according the object.
     * @param object : [IGeoObject] to be removed.
     */
    fun removeGeoObject(`object`: IGeoObject)
}