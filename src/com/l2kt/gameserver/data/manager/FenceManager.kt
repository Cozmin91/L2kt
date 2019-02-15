package com.l2kt.gameserver.data.manager

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.instance.Fence
import java.util.*

/**
 * Fence table to manage [Fence] spawn/despawn.
 */
object FenceManager {

    private val _fences = ArrayList<Fence>()

    /**
     * Returns list of all fences spawned in the world.
     * @return List<Fence> : List of all fences.
    </Fence> */
    val fences: List<Fence>
        get() = _fences

    private val LOGGER = CLogger(FenceManager::class.java.name)

    /**
     * Returns the size template of [Fence] based on given size value.
     * @param size : Requested size.
     * @return [FenceSize] : Size of [Fence] in particular dimension.
     */
    private fun getFenceSize(size: Int): FenceSize? {
        if (size < 199)
            return FenceSize.SIZE_100

        if (size < 299)
            return FenceSize.SIZE_200

        if (size < 399)
            return FenceSize.SIZE_300

        if (size < 499)
            return FenceSize.SIZE_400

        if (size < 599)
            return FenceSize.SIZE_500

        if (size < 699)
            return FenceSize.SIZE_600

        if (size < 799)
            return FenceSize.SIZE_700

        if (size < 899)
            return FenceSize.SIZE_800

        if (size < 999)
            return FenceSize.SIZE_900

        return if (size < 1099) FenceSize.SIZE_1000 else null
    }

    /**
     * Description of each fence dimension parameters.
     *
     *  * _offset parameter says, what is the fence position compared to geodata grid.
     *  * _geoDataSize parameter says, what is the raw fence size in geodata coordinates (cells).
     *
     */
    private enum class FenceSize private constructor(internal val _offset: Int, internal val _geoDataSize: Int) {
        // FIXME: find better way of setting correct size to the fence, tried calculation, but didn't find any 100% valid equation

        SIZE_100(8, 11),
        SIZE_200(0, 18),
        SIZE_300(0, 24),
        SIZE_400(0, 30),
        SIZE_500(0, 36),
        SIZE_600(0, 42),
        SIZE_700(8, 49),
        SIZE_800(8, 55),
        SIZE_900(8, 61),
        SIZE_1000(0, 68)
    }

    /**
     * Adds [Fence] to the world.
     * @param x : Spawn X world coordinate.
     * @param y : Spawn Y world coordinate.
     * @param z : Spawn Z world coordinate.
     * @param type : Type of the fence. 1..corner stones only, 2..fence + corner stones
     * @param sizeX : Size of the [Fence] in X direction.
     * @param sizeY : Size of the [Fence] in Y direction.
     * @param height : The height of [Fence].
     * @return The newly created Fence object.
     */
    fun addFence(x: Int, y: Int, z: Int, type: Int, sizeX: Int, sizeY: Int, height: Int): Fence? {
        var x = x
        var y = y
        val fsx = getFenceSize(sizeX)
        val fsy = getFenceSize(sizeY)

        if (fsx == null || fsy == null) {
            LOGGER.warn("Unknown dimensions for fence, x={} y={}.", sizeX, sizeY)
            return null
        }

        // adjust coordinates to align fence symmetrically to geodata
        x = x and -0x10 + fsx._offset
        y = y and -0x10 + fsy._offset

        val sx = fsx._geoDataSize
        val sy = fsy._geoDataSize

        val geoX = GeoEngine.getGeoX(x) - sx / 2
        val geoY = GeoEngine.getGeoY(y) - sy / 2
        val geoZ = GeoEngine.getInstance().getHeight(x, y, z).toInt()

        // create inner description
        val inside = Array(sx) { BooleanArray(sy) }
        for (ix in 1 until sx - 1)
            for (iy in 1 until sy - 1)
                if (type == 2)
                    inside[ix][iy] = ix < 3 || ix >= sx - 3 || iy < 3 || iy >= sy - 3
                else
                    inside[ix][iy] = (ix < 3 || ix >= sx - 3) && (iy < 3 || iy >= sy - 3)
        val geoData = GeoEngine.calculateGeoObject(inside)

        // create new fence
        val fence = Fence(type, sizeX, sizeY, height, geoX, geoY, geoZ, geoData)

        // spawn fence to world
        fence.spawnMe(x, y, z)

        // add fence to geoengine and list
        GeoEngine.getInstance().addGeoObject(fence)
        _fences.add(fence)

        return fence
    }

    /**
     * Remove given [Fence] from the world.
     * @param fence : [Fence] to be removed.
     */
    fun removeFence(fence: Fence) {
        // remove fence from world
        fence.decayMe()

        // remove fence from geoengine and list
        GeoEngine.getInstance().removeGeoObject(fence)
        _fences.remove(fence)
    }
}