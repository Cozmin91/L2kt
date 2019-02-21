package com.l2kt.gameserver.geoengine

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.geoengine.geodata.*
import com.l2kt.gameserver.geoengine.pathfinding.Node
import com.l2kt.gameserver.geoengine.pathfinding.NodeBuffer
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.Location
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.RandomAccessFile
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object GeoEngine {

    private val _blocks: Array<Array<ABlock?>> = Array<Array<ABlock?>>(GeoStructure.GEO_BLOCKS_X) { arrayOfNulls(GeoStructure.GEO_BLOCKS_Y) }
    private val _nullBlock: BlockNull = BlockNull()


    private val _geoBugReports: PrintWriter?
    private val _debugItems = ConcurrentHashMap.newKeySet<ItemInstance>()

    // pre-allocated buffers
    private val _buffers: Array<BufferHolder?>

    // pathfinding statistics
    private var _findSuccess = 0
    private var _findFails = 0
    private var _postFilterPlayableUses = 0
    private var _postFilterUses = 0
    private var _postFilterElapsed: Long = 0

    val LOGGER = CLogger(GeoEngine::class.java.name)

    private val GEO_BUG = "%d;%d;%d;%d;%d;%d;%d;%s\r\n"

    /**
     * Return pathfinding statistics, useful for getting information about pathfinding status.
     * @return `List<String>` : stats
     */
    val stat: List<String>
        get() {
            val list = ArrayList<String>()

            for (buffer in _buffers)
                list.add(buffer.toString())

            list.add("Use: playable=" + _postFilterPlayableUses.toString() + " non-playable=" + (_postFilterUses - _postFilterPlayableUses).toString())

            if (_postFilterUses > 0)
                list.add(
                    "Time (ms): total=" + _postFilterElapsed.toString() + " avg=" + String.format(
                        "%1.2f",
                        _postFilterElapsed.toDouble() / _postFilterUses
                    )
                )

            list.add("Pathfind: success=" + _findSuccess.toString() + ", fail=" + _findFails.toString())

            return list
        }

    /**
     * GeoEngine contructor. Loads all geodata files of chosen geodata format.
     */
    init {
        // initialize block container

        // load null block

        // initialize multilayer temporarily buffer
        BlockMultilayer.initialize()

        // load geo files according to geoengine config setup
        val props = Config.initProperties(Config.GEOENGINE_FILE)
        var loaded = 0
        var failed = 0
        for (rx in World.TILE_X_MIN..World.TILE_X_MAX) {
            for (ry in World.TILE_Y_MIN..World.TILE_Y_MAX) {
                if (props.containsKey(rx.toString() + "_" + ry.toString())) {
                    // region file is load-able, try to load it
                    if (loadGeoBlocks(rx, ry))
                        loaded++
                    else
                        failed++
                } else {
                    // region file is not load-able, load null blocks
                    loadNullBlocks(rx, ry)
                }
            }
        }
        LOGGER.info("Loaded {} L2D region files.", loaded)

        // release multilayer block temporarily buffer
        BlockMultilayer.release()

        if (failed > 0) {
            LOGGER.warn(
                "Failed to load {} L2D region files. Please consider to check your \"geodata.properties\" settings and location of your geodata files.",
                failed
            )
            System.exit(1)
        }

        // initialize bug reports
        var writer: PrintWriter? = null
        try {
            writer = PrintWriter(FileOutputStream(File(Config.GEODATA_PATH + "geo_bugs.txt"), true), true)
        } catch (e: Exception) {
            LOGGER.error("Couldn't load \"geo_bugs.txt\" file.", e)
        }

        _geoBugReports = writer

        val array = Config.PATHFIND_BUFFERS.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
        _buffers = arrayOfNulls(array.size)

        var count = 0
        for (i in array.indices) {
            val buf = array[i]
            val args = buf.split("x").dropLastWhile { it.isEmpty() }.toTypedArray()

            try {
                val size = Integer.parseInt(args[1])
                count += size
                _buffers[i] = BufferHolder(Integer.parseInt(args[0]), size)
            } catch (e: Exception) {
                LOGGER.error("Couldn't load buffer setting: {}.", e, buf)
            }

        }

        LOGGER.info("Loaded {} node buffers.", count)
    }

    /**
     * Create list of node locations as result of calculated buffer node tree.
     * @param target : the entry point
     * @return List<NodeLoc> : list of node location
    </NodeLoc> */
    private fun constructPath(target: Node): MutableList<Location> {
        var target = target
        // create empty list
        val list = LinkedList<Location>()

        // set direction X/Y
        var dx = 0
        var dy = 0

        // get target parent
        var parent = target.parent

        // while parent exists
        while (parent != null) {
            // get parent <> target direction X/Y
            val nx = parent.loc!!.geoX - target.loc!!.geoX
            val ny = parent.loc!!.geoY - target.loc!!.geoY

            // direction has changed?
            if (dx != nx || dy != ny) {
                // add node to the beginning of the list
                list.addFirst(target.loc)

                // update direction X/Y
                dx = nx
                dy = ny
            }

            // move to next node, set target and get its parent
            target = parent
            parent = target.parent
        }

        // return list
        return list
    }

    // GEODATA - GENERAL

    /**
     * Converts world X to geodata X.
     * @param worldX
     * @return int : Geo X
     */
    fun getGeoX(worldX: Int): Int {
        return MathUtil.limit(worldX, World.WORLD_X_MIN, World.WORLD_X_MAX) - World.WORLD_X_MIN shr 4
    }

    /**
     * Converts world Y to geodata Y.
     * @param worldY
     * @return int : Geo Y
     */
    fun getGeoY(worldY: Int): Int {
        return MathUtil.limit(worldY, World.WORLD_Y_MIN, World.WORLD_Y_MAX) - World.WORLD_Y_MIN shr 4
    }

    /**
     * Converts geodata X to world X.
     * @param geoX
     * @return int : World X
     */
    fun getWorldX(geoX: Int): Int {
        return (MathUtil.limit(geoX, 0, GeoStructure.GEO_CELLS_X) shl 4) + World.WORLD_X_MIN + 8
    }

    /**
     * Converts geodata Y to world Y.
     * @param geoY
     * @return int : World Y
     */
    fun getWorldY(geoY: Int): Int {
        return (MathUtil.limit(geoY, 0, GeoStructure.GEO_CELLS_Y) shl 4) + World.WORLD_Y_MIN + 8
    }

    // GEODATA - DYNAMIC

    /**
     * Returns calculated NSWE flag byte as a description of [IGeoObject].<br></br>
     * The [IGeoObject] is defined by boolean 2D array, saying if the object is present on given cell or not.
     * @param inside : 2D description of [IGeoObject]
     * @return byte[][] : Returns NSWE flags of [IGeoObject].
     */
    fun calculateGeoObject(inside: Array<BooleanArray>): Array<ByteArray> {
        // get dimensions
        val width = inside.size
        val height = inside[0].size

        // create object flags for geodata, according to the geo object 2D description
        val result = Array(width) { ByteArray(height) }

        // loop over each cell of the geo object
        for (ix in 0 until width)
            for (iy in 0 until height)
                if (inside[ix][iy]) {
                    // cell is inside geo object, block whole movement (nswe = 0)
                    result[ix][iy] = 0
                } else {
                    // cell is outside of geo object, block only movement leading inside geo object

                    // set initial value -> no geodata change
                    var nswe = 0xFF.toByte()

                    // perform axial and diagonal checks
                    if (iy < height - 1)
                        if (inside[ix][iy + 1])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_S.toInt()).inv().toByte()
                    if (iy > 0)
                        if (inside[ix][iy - 1])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_N.toInt()).inv().toByte()
                    if (ix < width - 1)
                        if (inside[ix + 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_E.toInt()).inv().toByte()
                    if (ix > 0)
                        if (inside[ix - 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_W.toInt()).inv().toByte()
                    if (ix < width - 1 && iy < height - 1)
                        if (inside[ix + 1][iy + 1] || inside[ix][iy + 1] || inside[ix + 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_SE.toInt()).inv().toByte()
                    if (ix < width - 1 && iy > 0)
                        if (inside[ix + 1][iy - 1] || inside[ix][iy - 1] || inside[ix + 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_NE.toInt()).inv().toByte()
                    if (ix > 0 && iy < height - 1)
                        if (inside[ix - 1][iy + 1] || inside[ix][iy + 1] || inside[ix - 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_SW.toInt()).inv().toByte()
                    if (ix > 0 && iy > 0)
                        if (inside[ix - 1][iy - 1] || inside[ix][iy - 1] || inside[ix - 1][iy])
                            nswe = (nswe.toInt() and GeoStructure.CELL_FLAG_NW.toInt()).inv().toByte()

                    result[ix][iy] = nswe
                }

        return result
    }

    /**
     * Returns diagonal NSWE flag format of combined two NSWE flags.
     * @param dirX : X direction NSWE flag
     * @param dirY : Y direction NSWE flag
     * @return byte : NSWE flag of combined direction
     */
    private fun getDirXY(dirX: Byte, dirY: Byte): Byte {
        // check axis directions
        if (dirY == GeoStructure.CELL_FLAG_N) {
            return if (dirX == GeoStructure.CELL_FLAG_W) GeoStructure.CELL_FLAG_NW else GeoStructure.CELL_FLAG_NE

        }

        return if (dirX == GeoStructure.CELL_FLAG_W) GeoStructure.CELL_FLAG_SW else GeoStructure.CELL_FLAG_SE

    }

    /**
     * Provides optimize selection of the buffer. When all pre-initialized buffer are locked, creates new buffer and log this situation.
     * @param size : pre-calculated minimal required size
     * @param playable : moving object is playable?
     * @return NodeBuffer : buffer
     */
    private fun getBuffer(size: Int, playable: Boolean): NodeBuffer? {
        var current: NodeBuffer? = null
        for (holder in _buffers) {
            if(holder == null)
                break

            // Find proper size of buffer
            if (holder._size < size)
                continue

            // Find unlocked NodeBuffer
            for (buffer in holder._buffer) {
                if (!buffer.isLocked)
                    continue

                holder._uses++
                if (playable)
                    holder._playableUses++

                holder._elapsed += buffer.elapsedTime
                return buffer
            }

            // NodeBuffer not found, allocate temporary buffer
            current = NodeBuffer(holder._size)
            current.isLocked

            holder._overflows++
            if (playable)
                holder._playableOverflows++
        }

        return current
    }

    /**
     * Loads geodata from a file. When file does not exist, is corrupted or not consistent, loads none geodata.
     * @param regionX : Geodata file region X coordinate.
     * @param regionY : Geodata file region Y coordinate.
     * @return boolean : True, when geodata file was loaded without problem.
     */
    private fun loadGeoBlocks(regionX: Int, regionY: Int): Boolean {
        val filename = String.format(GeoFormat.L2D.filename, regionX, regionY)
        val filepath = Config.GEODATA_PATH + filename

        // standard load
        try {
            RandomAccessFile(filepath, "r").use { raf ->
                raf.channel.use { fc ->
                    // initialize file buffer
                    val buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load()
                    buffer.order(ByteOrder.LITTLE_ENDIAN)

                    // get block indexes
                    val blockX = (regionX - World.TILE_X_MIN) * GeoStructure.REGION_BLOCKS_X
                    val blockY = (regionY - World.TILE_Y_MIN) * GeoStructure.REGION_BLOCKS_Y

                    // loop over region blocks
                    for (ix in 0 until GeoStructure.REGION_BLOCKS_X) {
                        for (iy in 0 until GeoStructure.REGION_BLOCKS_Y) {
                            // get block type
                            val type = buffer.get()

                            // load block according to block type
                            when (type) {
                                GeoStructure.TYPE_FLAT_L2D -> _blocks[blockX + ix][blockY + iy] =
                                        BlockFlat(buffer, GeoFormat.L2D)

                                GeoStructure.TYPE_COMPLEX_L2D -> _blocks[blockX + ix][blockY + iy] =
                                        BlockComplex(buffer, GeoFormat.L2D)

                                GeoStructure.TYPE_MULTILAYER_L2D -> _blocks[blockX + ix][blockY + iy] =
                                        BlockMultilayer(buffer, GeoFormat.L2D)

                                else -> throw IllegalArgumentException("Unknown block type: $type")
                            }
                        }
                    }

                    // check data consistency
                    if (buffer.remaining() > 0)
                        LOGGER.warn(
                            "Region file {} can be corrupted, remaining {} bytes to read.",
                            filename,
                            buffer.remaining()
                        )

                    // loading was successful
                    return true
                }
            }
        } catch (e: Exception) {
            // an error occured while loading, load null blocks
            LOGGER.error("Error loading {} region file.", e, filename)

            // replace whole region file with null blocks
            loadNullBlocks(regionX, regionY)

            // loading was not successful
            return false
        }

    }

    /**
     * Loads null blocks. Used when no region file is detected or an error occurs during loading.
     * @param regionX : Geodata file region X coordinate.
     * @param regionY : Geodata file region Y coordinate.
     */
    private fun loadNullBlocks(regionX: Int, regionY: Int) {
        // get block indexes
        val blockX = (regionX - World.TILE_X_MIN) * GeoStructure.REGION_BLOCKS_X
        val blockY = (regionY - World.TILE_Y_MIN) * GeoStructure.REGION_BLOCKS_Y

        // load all null blocks
        for (ix in 0 until GeoStructure.REGION_BLOCKS_X)
            for (iy in 0 until GeoStructure.REGION_BLOCKS_Y)
                _blocks[blockX + ix][blockY + iy] = _nullBlock
    }

    /**
     * Returns the height of cell, which is closest to given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, closest to given coordinates.
     */
    private fun getHeightNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Short {
        return getBlock(geoX, geoY).getHeightNearestOriginal(geoX, geoY, worldZ)
    }

    /**
     * Returns the NSWE flag byte of cell, which is closes to given coordinates.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte coordinate, closest to given coordinates.
     */
    private fun getNsweNearestOriginal(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return getBlock(geoX, geoY).getNsweNearestOriginal(geoX, geoY, worldZ)
    }

    /**
     * Returns block of geodata on given coordinates.
     * @param geoX : Geodata X
     * @param geoY : Geodata Y
     * @return [ABlock] : Bloack of geodata.
     */
    fun getBlock(geoX: Int, geoY: Int): ABlock {
        return _blocks[geoX / GeoStructure.BLOCK_CELLS_X][geoY / GeoStructure.BLOCK_CELLS_Y]!!
    }

    /**
     * Check if geo coordinates has geo.
     * @param geoX : Geodata X
     * @param geoY : Geodata Y
     * @return boolean : True, if given geo coordinates have geodata
     */
    fun hasGeoPos(geoX: Int, geoY: Int): Boolean {
        return getBlock(geoX, geoY).hasGeoPos()
    }

    /**
     * Returns the height of cell, which is closest to given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell geodata Z coordinate, closest to given coordinates.
     */
    fun getHeightNearest(geoX: Int, geoY: Int, worldZ: Int): Short {
        return getBlock(geoX, geoY).getHeightNearest(geoX, geoY, worldZ)
    }

    /**
     * Returns the NSWE flag byte of cell, which is closes to given coordinates.
     * @param geoX : Cell geodata X coordinate.
     * @param geoY : Cell geodata Y coordinate.
     * @param worldZ : Cell world Z coordinate.
     * @return short : Cell NSWE flag byte coordinate, closest to given coordinates.
     */
    fun getNsweNearest(geoX: Int, geoY: Int, worldZ: Int): Byte {
        return getBlock(geoX, geoY).getNsweNearest(geoX, geoY, worldZ)
    }

    /**
     * Check if world coordinates has geo.
     * @param worldX : World X
     * @param worldY : World Y
     * @return boolean : True, if given world coordinates have geodata
     */
    fun hasGeo(worldX: Int, worldY: Int): Boolean {
        return hasGeoPos(getGeoX(worldX), getGeoY(worldY))
    }

    /**
     * Returns closest Z coordinate according to geodata.
     * @param worldX : world x
     * @param worldY : world y
     * @param worldZ : world z
     * @return short : nearest Z coordinates according to geodata
     */
    fun getHeight(worldX: Int, worldY: Int, worldZ: Int): Short {
        return getHeightNearest(getGeoX(worldX), getGeoY(worldY), worldZ)
    }

    /**
     * Add [IGeoObject] to the geodata.
     * @param object : An object using [IGeoObject] interface.
     */
    fun addGeoObject(`object`: IGeoObject) {
        toggleGeoObject(`object`, true)
    }

    /**
     * Remove [IGeoObject] from the geodata.
     * @param object : An object using [IGeoObject] interface.
     */
    fun removeGeoObject(`object`: IGeoObject) {
        toggleGeoObject(`object`, false)
    }

    /**
     * Toggles an [IGeoObject] in the geodata.
     * @param object : An object using [IGeoObject] interface.
     * @param add : Add/remove object.
     */
    private fun toggleGeoObject(`object`: IGeoObject, add: Boolean) {
        // get object geo coordinates and data
        val minGX = `object`.geoX
        val minGY = `object`.geoY
        val geoData = `object`.objectGeoData

        // get min/max block coordinates
        val minBX = minGX / GeoStructure.BLOCK_CELLS_X
        val maxBX = (minGX + geoData.size - 1) / GeoStructure.BLOCK_CELLS_X
        val minBY = minGY / GeoStructure.BLOCK_CELLS_Y
        val maxBY = (minGY + geoData[0].size - 1) / GeoStructure.BLOCK_CELLS_Y

        // loop over affected blocks in X direction
        for (bx in minBX..maxBX) {
            // loop over affected blocks in Y direction
            for (by in minBY..maxBY) {
                var block: ABlock? = null

                // conversion to dynamic block must be synchronized to prevent 2 independent threads converting same block
                synchronized(_blocks) {
                    // get related block
                    block = _blocks[bx][by]!!

                    // check for dynamic block
                    if (block !is IBlockDynamic) {
                        // null block means no geodata (particular region file is not loaded), no geodata means no geobjects
                        if (block is BlockNull)
                            return@synchronized

                        // not a dynamic block, convert it
                        if (block is BlockFlat) {
                            // convert flat block to the dynamic complex block
                            block = BlockComplexDynamic(bx, by, block as BlockFlat)
                            _blocks[bx][by] = block
                        } else if (block is BlockComplex) {
                            // convert complex block to the dynamic complex block
                            block = BlockComplexDynamic(bx, by, block as BlockComplex)
                            _blocks[bx][by] = block
                        } else if (block is BlockMultilayer) {
                            // convert multilayer block to the dynamic multilayer block
                            block = BlockMultilayerDynamic(bx, by, block as BlockMultilayer)
                            _blocks[bx][by] = block
                        }
                    }
                }

                if (block !is BlockNull){
                    // add/remove geo object to/from dynamic block
                    if (add)
                        (block as IBlockDynamic).addGeoObject(`object`)
                    else
                        (block as IBlockDynamic).removeGeoObject(`object`)
                }
            }
        }
    }

    // PATHFINDING

    /**
     * Check line of sight from [WorldObject] to [WorldObject].
     * @param origin : The origin object.
     * @param target : The target object.
     * @return `boolean` : True if origin can see target
     */
    fun canSeeTarget(origin: WorldObject, target: WorldObject): Boolean {
        // get origin and target world coordinates
        val ox = origin.x
        val oy = origin.y
        val oz = origin.z
        val tx = target.x
        val ty = target.y
        val tz = target.z

        // get origin and check existing geo coordinates
        val gox = getGeoX(ox)
        val goy = getGeoY(oy)
        if (!hasGeoPos(gox, goy))
            return true

        val goz = getHeightNearest(gox, goy, oz)

        // get target and check existing geo coordinates
        val gtx = getGeoX(tx)
        val gty = getGeoY(ty)
        if (!hasGeoPos(gtx, gty))
            return true

        val door = target is Door
        val gtz = if (door) getHeightNearestOriginal(gtx, gty, tz) else getHeightNearest(gtx, gty, tz)

        // origin and target coordinates are same
        if (gox == gtx && goy == gty)
            return goz == gtz

        // get origin and target height, real height = collision height * 2
        var oheight = 0.0
        if (origin is Creature)
            oheight = origin.collisionHeight * 2

        var theight = 0.0
        if (target is Creature)
            theight = target.collisionHeight * 2

        // perform geodata check
        return if (door) checkSeeOriginal(gox, goy, goz.toInt(), oheight, gtx, gty, gtz.toInt(), theight) else checkSee(
            gox,
            goy,
            goz.toInt(),
            oheight,
            gtx,
            gty,
            gtz.toInt(),
            theight
        )
    }

    /**
     * Check line of sight from [WorldObject] to [Location].
     * @param origin : The origin object.
     * @param position : The target position.
     * @return `boolean` : True if object can see position
     */
    fun canSeeTarget(origin: WorldObject, position: Location): Boolean {
        // get origin and target world coordinates
        val ox = origin.x
        val oy = origin.y
        val oz = origin.z
        val tx = position.x
        val ty = position.y
        val tz = position.z

        // get origin and check existing geo coordinates
        val gox = getGeoX(ox)
        val goy = getGeoY(oy)
        if (!hasGeoPos(gox, goy))
            return true

        val goz = getHeightNearest(gox, goy, oz)

        // get target and check existing geo coordinates
        val gtx = getGeoX(tx)
        val gty = getGeoY(ty)
        if (!hasGeoPos(gtx, gty))
            return true

        val gtz = getHeightNearest(gtx, gty, tz)

        // origin and target coordinates are same
        if (gox == gtx && goy == gty)
            return goz == gtz

        // get origin and target height, real height = collision height * 2
        var oheight = 0.0
        if (origin is Creature)
            oheight = origin.template.collisionHeight

        // perform geodata check
        return checkSee(gox, goy, goz.toInt(), oheight, gtx, gty, gtz.toInt(), 0.0)
    }

    /**
     * Simple check for origin to target visibility.
     * @param gox : origin X geodata coordinate
     * @param goy : origin Y geodata coordinate
     * @param goz : origin Z geodata coordinate
     * @param oheight : origin height (if instance of [Creature])
     * @param gtx : target X geodata coordinate
     * @param gty : target Y geodata coordinate
     * @param gtz : target Z geodata coordinate
     * @param theight : target height (if instance of [Creature])
     * @return `boolean` : True, when target can be seen.
     */
    fun checkSee(
        gox: Int,
        goy: Int,
        goz: Int,
        oheight: Double,
        gtx: Int,
        gty: Int,
        gtz: Int,
        theight: Double
    ): Boolean {
        var gox = gox
        var goy = goy
        var goz = goz
        var gtx = gtx
        var gty = gty
        var gtz = gtz
        // get line of sight Z coordinates
        var losoz = goz + oheight * Config.PART_OF_CHARACTER_HEIGHT / 100
        var lostz = gtz + theight * Config.PART_OF_CHARACTER_HEIGHT / 100

        // get X delta and signum
        val dx = Math.abs(gtx - gox)
        val sx = if (gox < gtx) 1 else -1
        val dirox = if (sx > 0) GeoStructure.CELL_FLAG_E else GeoStructure.CELL_FLAG_W
        val dirtx = if (sx > 0) GeoStructure.CELL_FLAG_W else GeoStructure.CELL_FLAG_E

        // get Y delta and signum
        val dy = Math.abs(gty - goy)
        val sy = if (goy < gty) 1 else -1
        val diroy = if (sy > 0) GeoStructure.CELL_FLAG_S else GeoStructure.CELL_FLAG_N
        val dirty = if (sy > 0) GeoStructure.CELL_FLAG_N else GeoStructure.CELL_FLAG_S

        // get Z delta
        val dm = Math.max(dx, dy)
        val dz = (lostz - losoz) / dm

        // get direction flag for diagonal movement
        val diroxy = getDirXY(dirox, diroy)
        val dirtxy = getDirXY(dirtx, dirty)

        // delta, determines axis to move on (+..X axis, -..Y axis)
        var d = dx - dy

        // NSWE direction of movement
        var diro: Byte
        var dirt: Byte

        // clearDebugItems();
        // dropDebugItem(728, 0, new GeoLocation(gox, goy, goz)); // blue potion
        // dropDebugItem(728, 0, new GeoLocation(gtx, gty, gtz)); // blue potion

        // initialize node values
        var nox = gox
        var noy = goy
        var ntx = gtx
        var nty = gty
        var nsweo = getNsweNearest(gox, goy, goz)
        var nswet = getNsweNearest(gtx, gty, gtz)

        // loop
        var block: ABlock
        var index: Int
        for (i in 0 until (dm + 1) / 2) {
            // dropDebugItem(57, 0, new GeoLocation(gox, goy, goz)); // antidote
            // dropDebugItem(1831, 0, new GeoLocation(gtx, gty, gtz)); // adena

            // reset direction flag
            diro = 0
            dirt = 0

            // calculate next point coordinates
            val e2 = 2 * d
            if (e2 > -dy && e2 < dx) {
                // calculate next point XY coordinates
                d -= dy
                d += dx
                nox += sx
                ntx -= sx
                noy += sy
                nty -= sy
                diro = (diro.toInt() or diroxy.toInt()).toByte()
                dirt = (dirt.toInt() or dirtxy.toInt()).toByte()
            } else if (e2 > -dy) {
                // calculate next point X coordinate
                d -= dy
                nox += sx
                ntx -= sx
                diro = (diro.toInt() or dirox.toInt()).toByte()
                dirt = (dirt.toInt() or dirtx.toInt()).toByte()
            } else if (e2 < dx) {
                // calculate next point Y coordinate
                d += dx
                noy += sy
                nty -= sy
                diro = (diro.toInt() or diroy.toInt()).toByte()
                dirt = (dirt.toInt() or dirty.toInt()).toByte()
            }

            run {
                // get block of the next cell
                block = getBlock(nox, noy)

                // get index of particular layer, based on movement conditions
                if ((nsweo.toInt() and diro.toInt()) == 0)
                    index = block.getIndexAbove(nox, noy, goz - GeoStructure.CELL_IGNORE_HEIGHT)
                else
                    index = block.getIndexBelow(nox, noy, goz + GeoStructure.CELL_IGNORE_HEIGHT)

                // layer does not exist, return
                if (index == -1)
                    return false

                // get layer and next line of sight Z coordinate
                goz = block.getHeight(index).toInt()
                losoz += dz

                // perform line of sight check, return when fails
                if (goz - losoz > Config.MAX_OBSTACLE_HEIGHT)
                    return false

                // get layer nswe
                nsweo = block.getNswe(index)
            }
            run {
                // get block of the next cell
                block = getBlock(ntx, nty)

                // get index of particular layer, based on movement conditions
                if ((nswet.toInt() and dirt.toInt()) == 0)
                    index = block.getIndexAbove(ntx, nty, gtz - GeoStructure.CELL_IGNORE_HEIGHT)
                else
                    index = block.getIndexBelow(ntx, nty, gtz + GeoStructure.CELL_IGNORE_HEIGHT)

                // layer does not exist, return
                if (index == -1)
                    return false

                // get layer and next line of sight Z coordinate
                gtz = block.getHeight(index).toInt()
                lostz -= dz

                // perform line of sight check, return when fails
                if (gtz - lostz > Config.MAX_OBSTACLE_HEIGHT)
                    return false

                // get layer nswe
                nswet = block.getNswe(index)
            }

            // update coords
            gox = nox
            goy = noy
            gtx = ntx
            gty = nty
        }

        // when iteration is completed, compare final Z coordinates
        return Math.abs(goz - gtz) < GeoStructure.CELL_HEIGHT * 4
    }

    /**
     * Simple check for origin to target visibility.<br></br>
     * Geodata without [IGeoObject] are taken in consideration.<br></br>
     * NOTE: When two doors close between each other and the LoS check of one doors is performed through another door, result will not be accurate (the other door are skipped).
     * @param gox : origin X geodata coordinate
     * @param goy : origin Y geodata coordinate
     * @param goz : origin Z geodata coordinate
     * @param oheight : origin height (if instance of [Creature])
     * @param gtx : target X geodata coordinate
     * @param gty : target Y geodata coordinate
     * @param gtz : target Z geodata coordinate
     * @param theight : target height (if instance of [Creature] or [Door])
     * @return `boolean` : True, when target can be seen.
     */
    fun checkSeeOriginal(
        gox: Int,
        goy: Int,
        goz: Int,
        oheight: Double,
        gtx: Int,
        gty: Int,
        gtz: Int,
        theight: Double
    ): Boolean {
        var gox = gox
        var goy = goy
        var goz = goz
        var gtx = gtx
        var gty = gty
        var gtz = gtz
        // get line of sight Z coordinates
        var losoz = goz + oheight * Config.PART_OF_CHARACTER_HEIGHT / 100
        var lostz = gtz + theight * Config.PART_OF_CHARACTER_HEIGHT / 100

        // get X delta and signum
        val dx = Math.abs(gtx - gox)
        val sx = if (gox < gtx) 1 else -1
        val dirox = if (sx > 0) GeoStructure.CELL_FLAG_E else GeoStructure.CELL_FLAG_W
        val dirtx = if (sx > 0) GeoStructure.CELL_FLAG_W else GeoStructure.CELL_FLAG_E

        // get Y delta and signum
        val dy = Math.abs(gty - goy)
        val sy = if (goy < gty) 1 else -1
        val diroy = if (sy > 0) GeoStructure.CELL_FLAG_S else GeoStructure.CELL_FLAG_N
        val dirty = if (sy > 0) GeoStructure.CELL_FLAG_N else GeoStructure.CELL_FLAG_S

        // get Z delta
        val dm = Math.max(dx, dy)
        val dz = (lostz - losoz) / dm

        // get direction flag for diagonal movement
        val diroxy = getDirXY(dirox, diroy)
        val dirtxy = getDirXY(dirtx, dirty)

        // delta, determines axis to move on (+..X axis, -..Y axis)
        var d = dx - dy

        // NSWE direction of movement
        var diro: Byte
        var dirt: Byte

        // clearDebugItems();
        // dropDebugItem(728, 0, new GeoLocation(gox, goy, goz)); // blue potion
        // dropDebugItem(728, 0, new GeoLocation(gtx, gty, gtz)); // blue potion

        // initialize node values
        var nox = gox
        var noy = goy
        var ntx = gtx
        var nty = gty
        var nsweo = getNsweNearestOriginal(gox, goy, goz)
        var nswet = getNsweNearestOriginal(gtx, gty, gtz)

        // loop
        var block: ABlock
        var index: Int
        for (i in 0 until (dm + 1) / 2) {
            // dropDebugItem(57, 0, new GeoLocation(gox, goy, goz)); // antidote
            // dropDebugItem(1831, 0, new GeoLocation(gtx, gty, gtz)); // adena

            // reset direction flag
            diro = 0
            dirt = 0

            // calculate next point coordinates
            val e2 = 2 * d
            if (e2 > -dy && e2 < dx) {
                // calculate next point XY coordinates
                d -= dy
                d += dx
                nox += sx
                ntx -= sx
                noy += sy
                nty -= sy
                diro = (diro.toInt() or diroxy.toInt()).toByte()
                dirt = (dirt.toInt() or dirtxy.toInt()).toByte()
            } else if (e2 > -dy) {
                // calculate next point X coordinate
                d -= dy
                nox += sx
                ntx -= sx
                diro = (diro.toInt() or dirox.toInt()).toByte()
                dirt = (dirt.toInt() or dirtx.toInt()).toByte()
            } else if (e2 < dx) {
                // calculate next point Y coordinate
                d += dx
                noy += sy
                nty -= sy
                diro = (diro.toInt() or diroy.toInt()).toByte()
                dirt = (dirt.toInt() or dirty.toInt()).toByte()
            }

            run {
                // get block of the next cell
                block = getBlock(nox, noy)

                // get index of particular layer, based on movement conditions
                if ((nsweo.toInt() and diro.toInt()) == 0)
                    index = block.getIndexAboveOriginal(nox, noy, goz - GeoStructure.CELL_IGNORE_HEIGHT)
                else
                    index = block.getIndexBelowOriginal(nox, noy, goz + GeoStructure.CELL_IGNORE_HEIGHT)

                // layer does not exist, return
                if (index == -1)
                    return false

                // get layer and next line of sight Z coordinate
                goz = block.getHeightOriginal(index).toInt()
                losoz += dz

                // perform line of sight check, return when fails
                if (goz - losoz > Config.MAX_OBSTACLE_HEIGHT)
                    return false

                // get layer nswe
                nsweo = block.getNsweOriginal(index)
            }
            run {
                // get block of the next cell
                block = getBlock(ntx, nty)

                // get index of particular layer, based on movement conditions
                if ((nswet.toInt() and dirt.toInt()) == 0)
                    index = block.getIndexAboveOriginal(ntx, nty, gtz - GeoStructure.CELL_IGNORE_HEIGHT)
                else
                    index = block.getIndexBelowOriginal(ntx, nty, gtz + GeoStructure.CELL_IGNORE_HEIGHT)

                // layer does not exist, return
                if (index == -1)
                    return false

                // get layer and next line of sight Z coordinate
                gtz = block.getHeightOriginal(index).toInt()
                lostz -= dz

                // perform line of sight check, return when fails
                if (gtz - lostz > Config.MAX_OBSTACLE_HEIGHT)
                    return false

                // get layer nswe
                nswet = block.getNsweOriginal(index)
            }

            // update coords
            gox = nox
            goy = noy
            gtx = ntx
            gty = nty
        }

        // when iteration is completed, compare final Z coordinates
        return Math.abs(goz - gtz) < GeoStructure.CELL_HEIGHT * 4
    }

    /**
     * Check movement from coordinates to coordinates.
     * @param ox : origin X coordinate
     * @param oy : origin Y coordinate
     * @param oz : origin Z coordinate
     * @param tx : target X coordinate
     * @param ty : target Y coordinate
     * @param tz : target Z coordinate
     * @return {code boolean} : True if target coordinates are reachable from origin coordinates
     */
    fun canMoveToTarget(ox: Int, oy: Int, oz: Int, tx: Int, ty: Int, tz: Int): Boolean {
        // get origin and check existing geo coordinates
        val gox = getGeoX(ox)
        val goy = getGeoY(oy)
        if (!hasGeoPos(gox, goy))
            return true

        val goz = getHeightNearest(gox, goy, oz)

        // get target and check existing geo coordinates
        val gtx = getGeoX(tx)
        val gty = getGeoY(ty)
        if (!hasGeoPos(gtx, gty))
            return true

        val gtz = getHeightNearest(gtx, gty, tz)

        // target coordinates reached
        if (gox == gtx && goy == gty && goz == gtz)
            return true

        // perform geodata check
        val loc = checkMove(gox, goy, goz.toInt(), gtx, gty, gtz.toInt())
        return loc.geoX == gtx && loc.geoY == gty
    }

    /**
     * Check movement from origin to target. Returns last available point in the checked path.
     * @param ox : origin X coordinate
     * @param oy : origin Y coordinate
     * @param oz : origin Z coordinate
     * @param tx : target X coordinate
     * @param ty : target Y coordinate
     * @param tz : target Z coordinate
     * @return [Location] : Last point where object can walk (just before wall)
     */
    fun canMoveToTargetLoc(ox: Int, oy: Int, oz: Int, tx: Int, ty: Int, tz: Int): Location {
        // get origin and check existing geo coordinates
        val gox = getGeoX(ox)
        val goy = getGeoY(oy)
        if (!hasGeoPos(gox, goy))
            return Location(tx, ty, tz)

        val goz = getHeightNearest(gox, goy, oz)

        // get target and check existing geo coordinates
        val gtx = getGeoX(tx)
        val gty = getGeoY(ty)
        if (!hasGeoPos(gtx, gty))
            return Location(tx, ty, tz)

        val gtz = getHeightNearest(gtx, gty, tz)

        // target coordinates reached
        return if (gox == gtx && goy == gty && goz == gtz) Location(tx, ty, tz) else checkMove(
            gox,
            goy,
            goz.toInt(),
            gtx,
            gty,
            gtz.toInt()
        )

        // perform geodata check
    }

    /**
     * With this method you can check if a position is visible or can be reached by beeline movement.<br></br>
     * Target X and Y reachable and Z is on same floor:
     *
     *  * Location of the target with corrected Z value from geodata.
     *
     * Target X and Y reachable but Z is on another floor:
     *
     *  * Location of the origin with corrected Z value from geodata.
     *
     * Target X and Y not reachable:
     *
     *  * Last accessible location in destination to target.
     *
     * @param gox : origin X geodata coordinate
     * @param goy : origin Y geodata coordinate
     * @param goz : origin Z geodata coordinate
     * @param gtx : target X geodata coordinate
     * @param gty : target Y geodata coordinate
     * @param gtz : target Z geodata coordinate
     * @return [GeoLocation] : The last allowed point of movement.
     */
    fun checkMove(gox: Int, goy: Int, goz: Int, gtx: Int, gty: Int, gtz: Int): GeoLocation {
        // get X delta, signum and direction flag
        val dx = Math.abs(gtx - gox)
        val sx = if (gox < gtx) 1 else -1
        val dirX = if (sx > 0) GeoStructure.CELL_FLAG_E else GeoStructure.CELL_FLAG_W

        // get Y delta, signum and direction flag
        val dy = Math.abs(gty - goy)
        val sy = if (goy < gty) 1 else -1
        val dirY = if (sy > 0) GeoStructure.CELL_FLAG_S else GeoStructure.CELL_FLAG_N

        // get direction flag for diagonal movement
        val dirXY = getDirXY(dirX, dirY)

        // delta, determines axis to move on (+..X axis, -..Y axis)
        var d = dx - dy

        // NSWE direction of movement
        var direction: Byte

        // load pointer coordinates
        var gpx = gox
        var gpy = goy
        var gpz = goz

        // load next pointer
        var nx = gpx
        var ny = gpy

        // loop
        do {
            direction = 0

            // calculate next point coordinates
            val e2 = 2 * d
            if (e2 > -dy && e2 < dx) {
                d -= dy
                d += dx
                nx += sx
                ny += sy
                direction = (direction.toInt() or dirXY.toInt()).toByte()
            } else if (e2 > -dy) {
                d -= dy
                nx += sx
                direction = (direction.toInt() or dirX.toInt()).toByte()
            } else if (e2 < dx) {
                d += dx
                ny += sy
                direction = (direction.toInt() or dirY.toInt()).toByte()
            }

            // obstacle found, return
            if ((getNsweNearest(gpx, gpy, gpz).toInt() and direction.toInt()) == 0)
                return GeoLocation(gpx, gpy, gpz)

            // update pointer coordinates
            gpx = nx
            gpy = ny
            gpz = getHeightNearest(nx, ny, gpz).toInt()

            // target coordinates reached
            if (gpx == gtx && gpy == gty) {
                return if (gpz == gtz) {
                    // path found, Z coordinates are okay, return target point
                    GeoLocation(gtx, gty, gtz)
                } else GeoLocation(gox, goy, goz)

                // path found, Z coordinates are not okay, return origin point
            }
        } while (true)
    }

    /**
     * Returns the list of location objects as a result of complete path calculation.
     * @param ox : origin x
     * @param oy : origin y
     * @param oz : origin z
     * @param tx : target x
     * @param ty : target y
     * @param tz : target z
     * @param playable : moving object is playable?
     * @return `List<Location>` : complete path from nodes
     */
    fun findPath(ox: Int, oy: Int, oz: Int, tx: Int, ty: Int, tz: Int, playable: Boolean): List<Location>? {
        // get origin and check existing geo coords
        val gox = getGeoX(ox)
        val goy = getGeoY(oy)
        if (!hasGeoPos(gox, goy))
            return null

        val goz = getHeightNearest(gox, goy, oz)

        // get target and check existing geo coords
        val gtx = getGeoX(tx)
        val gty = getGeoY(ty)
        if (!hasGeoPos(gtx, gty))
            return null

        val gtz = getHeightNearest(gtx, gty, tz)

        // Prepare buffer for pathfinding calculations
        val buffer = getBuffer(64 + 2 * Math.max(Math.abs(gox - gtx), Math.abs(goy - gty)), playable) ?: return null

        // clean debug path
        val debug = playable && Config.DEBUG_PATH
        if (debug)
            clearDebugItems()

        // find path
        var path: MutableList<Location>? = null
        try {
            val result = buffer.findPath(gox, goy, goz, gtx, gty, gtz)

            if (result == null) {
                _findFails++
                return null
            }

            if (debug) {
                // path origin
                dropDebugItem(728, 0, GeoLocation(gox, goy, goz.toInt())) // blue potion

                // path
                for (n in buffer.debugPath()) {
                    if (n.cost < 0)
                        dropDebugItem(1831, (-n.cost * 10).toInt(), n.loc) // antidote
                    else
                        dropDebugItem(57, (n.cost * 10).toInt(), n.loc) // adena
                }
            }

            path = constructPath(result)
        } catch (e: Exception) {
            LOGGER.error("Failed to generate a path.", e)

            _findFails++
            return null
        } finally {
            buffer.free()
            _findSuccess++
        }

        // check path
        if (path!!.size < 3)
            return path

        // log data
        val timeStamp = System.currentTimeMillis()
        _postFilterUses++
        if (playable)
            _postFilterPlayableUses++

        // get path list iterator
        val point = path.listIterator()

        // get node A (origin)
        var nodeAx = gox
        var nodeAy = goy
        var nodeAz = goz

        // get node B
        var nodeB = point.next() as GeoLocation

        // iterate thought the path to optimize it
        while (point.hasNext()) {
            // get node C
            val nodeC = path[point.nextIndex()] as GeoLocation

            // check movement from node A to node C
            val loc = checkMove(nodeAx, nodeAy, nodeAz.toInt(), nodeC.geoX, nodeC.geoY, nodeC.z)
            if (loc.geoX == nodeC.geoX && loc.geoY == nodeC.geoY) {
                // can move from node A to node C

                // remove node B
                point.remove()

                // show skipped nodes
                if (debug)
                    dropDebugItem(735, 0, nodeB) // green potion
            } else {
                // can not move from node A to node C

                // set node A (node B is part of path, update A coordinates)
                nodeAx = nodeB.geoX
                nodeAy = nodeB.geoY
                nodeAz = nodeB.z.toShort()
            }

            // set node B
            nodeB = point.next() as GeoLocation
        }

        // show final path
        if (debug) {
            for (node in path)
                dropDebugItem(65, 0, node) // red potion
        }

        // log data
        _postFilterElapsed += System.currentTimeMillis() - timeStamp

        return path
    }

    // MISC

    /**
     * Record a geodata bug.
     * @param loc : Location of the geodata bug.
     * @param comment : Short commentary.
     * @return boolean : True, when bug was successfully recorded.
     */
    fun addGeoBug(loc: Location, comment: String): Boolean {
        val gox = getGeoX(loc.x)
        val goy = getGeoY(loc.y)
        val goz = loc.z
        val rx = gox / GeoStructure.REGION_CELLS_X + World.TILE_X_MIN
        val ry = goy / GeoStructure.REGION_CELLS_Y + World.TILE_Y_MIN
        val bx = gox / GeoStructure.BLOCK_CELLS_X % GeoStructure.REGION_BLOCKS_X
        val by = goy / GeoStructure.BLOCK_CELLS_Y % GeoStructure.REGION_BLOCKS_Y
        val cx = gox % GeoStructure.BLOCK_CELLS_X
        val cy = goy % GeoStructure.BLOCK_CELLS_Y

        return try {
            _geoBugReports!!.printf(GEO_BUG, rx, ry, bx, by, cx, cy, goz, comment.replace(";", ":"))
            true
        } catch (e: Exception) {
            LOGGER.error("Couldn't save new entry to \"geo_bugs.txt\" file.", e)
            false
        }

    }

    /**
     * Add new item to drop list for debug purpose.
     * @param id : Item id
     * @param count : Item count
     * @param loc : Item location
     */
    fun dropDebugItem(id: Int, count: Int, loc: Location?) {
        val item = ItemInstance(IdFactory.getInstance().nextId, id)
        item.count = count
        item.spawnMe(loc!!)

        _debugItems.add(item)
    }

    /**
     * Clear item drop list for debugging paths.
     */
    fun clearDebugItems() {
        for (item in _debugItems)
            item.decayMe()

        _debugItems.clear()
    }

    /**
     * NodeBuffer container with specified size and count of separate buffers.
     */
    private class BufferHolder(internal val _size: Int, internal val _count: Int) {
        internal var _buffer: ArrayList<NodeBuffer>

        // statistics
        internal var _playableUses = 0
        internal var _uses = 0
        internal var _playableOverflows = 0
        internal var _overflows = 0
        internal var _elapsed: Long = 0

        init {
            _buffer = ArrayList(_count)

            for (i in 0 until _count)
                _buffer.add(NodeBuffer(_size))
        }

        override fun toString(): String {
            val sb = StringBuilder(100)

            StringUtil.append(
                sb,
                "Buffer ",
                _size.toString(),
                "x",
                _size.toString(),
                ": count=",
                _count.toString(),
                " uses=",
                _playableUses.toString(),
                "/",
                _uses.toString()
            )

            if (_uses > 0)
                StringUtil.append(
                    sb,
                    " total/avg(ms)=",
                    _elapsed.toString(),
                    "/",
                    String.format("%1.2f", _elapsed.toDouble() / _uses)
                )

            StringUtil.append(sb, " ovf=", _playableOverflows.toString(), "/", _overflows.toString())

            return sb.toString()
        }
    }
}