package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.ZoneManager.forEach
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.zone.SpawnZoneType
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.model.zone.form.ZoneCuboid
import com.l2kt.gameserver.model.zone.form.ZoneCylinder
import com.l2kt.gameserver.model.zone.form.ZoneNPoly
import com.l2kt.gameserver.model.zone.type.BossZone
import org.w3c.dom.Document
import java.lang.reflect.Constructor
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Loads and stores zones, based on their [ZoneType].
 */
object ZoneManager : IXmlReader {

    private val _zones = HashMap<Class<out ZoneType>, Map<Int, ZoneType>>()
    private val _debugItems = ConcurrentHashMap<Int, ItemInstance>()
    private val DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list"
    private val INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)"
    private var _lastDynamicId = 0

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/zones")
        IXmlReader.LOGGER.info(
            "Loaded {} zones classes and total {} zones.",
            _zones.size,
            _zones.values.stream().mapToInt { it.size }.sum()
        )
    }

    override fun parseDocument(doc: Document, path: Path) {
        _lastDynamicId = _lastDynamicId / 1000 * 1000 + 1000

        val zoneType = StringUtil.getNameWithoutExtension(path.toFile().name)

        // Create the Constructor, based on file name. It is reused by every zone.
        val zoneConstructor: Constructor<*>
        try {
            zoneConstructor = Class.forName("com.l2kt.gameserver.model.zone.type.$zoneType")
                .getConstructor(Int::class.javaPrimitiveType)
        } catch (e: Exception) {
            IXmlReader.LOGGER.error(
                "The zone type {} doesn't exist. Abort zones loading for {}.",
                e,
                zoneType,
                path.toFile().name
            )
            return
        }

        forEach(doc, "list") { listNode ->
            forEach(listNode, "zone") innerLoop@ { zoneNode ->
                val attrs = zoneNode.attributes
                val attribute = attrs.getNamedItem("id")
                val zoneId = if (attribute == null) _lastDynamicId++ else Integer.parseInt(attribute.nodeValue)
                val temp: ZoneType
                try {
                    temp = zoneConstructor.newInstance(zoneId) as ZoneType
                } catch (e: Exception) {
                    IXmlReader.LOGGER.error("The zone id {} couldn't be instantiated.", e, zoneId)
                    return@innerLoop
                }

                val zoneShape = parseString(attrs, "shape")
                val minZ = parseInteger(attrs, "minZ")!!
                val maxZ = parseInteger(attrs, "maxZ")!!

                val nodes = ArrayList<IntIntHolder>()
                forEach(zoneNode, "node") { nodeNode ->
                    val nodeAttrs = nodeNode.attributes
                    nodes.add(IntIntHolder(parseInteger(nodeAttrs, "x")!!, parseInteger(nodeAttrs, "y")!!))
                }
                if (nodes.isEmpty()) {
                    IXmlReader.LOGGER.warn("Missing nodes for zone {} in file {}.", zoneId, zoneType)
                    return@innerLoop
                }

                forEach(zoneNode, "stat") { statNode ->
                    val statAttrs = statNode.attributes
                    temp.setParameter(parseString(statAttrs, "name"), parseString(statAttrs, "val"))
                }

                if (temp is SpawnZoneType) {
                    forEach(zoneNode, "spawn") { spawnNode ->
                        val spawnAttrs = spawnNode.attributes
                        temp.addLoc(
                            parseLocation(spawnNode),
                            parseBoolean(spawnAttrs, "isChaotic", false)!!
                        )
                    }
                }

                val coords = nodes.toTypedArray()
                when (zoneShape) {
                    "Cuboid" -> if (coords.size == 2)
                        temp.zone = ZoneCuboid(
                            coords[0].id,
                            coords[1].id,
                            coords[0].value,
                            coords[1].value,
                            minZ,
                            maxZ
                        )
                    else {
                        IXmlReader.LOGGER.warn("Missing cuboid nodes for zone {} in file {}.", zoneId, zoneType)
                        return@innerLoop
                    }
                    "NPoly" -> if (coords.size > 2) {
                        val aX = IntArray(coords.size)
                        val aY = IntArray(coords.size)
                        for (i in coords.indices) {
                            aX[i] = coords[i].id
                            aY[i] = coords[i].value
                        }
                        temp.zone = ZoneNPoly(aX, aY, minZ, maxZ)
                    } else {
                        IXmlReader.LOGGER.warn("Missing NPoly nodes for zone {} in file {}.", zoneId, zoneType)
                        return@innerLoop
                    }
                    "Cylinder" -> {
                        val zoneRad = parseInteger(attrs, "rad")!!
                        if (coords.size == 1 && zoneRad > 0)
                            temp.zone = ZoneCylinder(coords[0].id, coords[0].value, minZ, maxZ, zoneRad)
                        else {
                            IXmlReader.LOGGER.warn("Missing Cylinder nodes for zone {} in file {}.", zoneId, zoneType)
                            return@innerLoop
                        }
                    }
                    else -> {
                        IXmlReader.LOGGER.warn("Unknown {} shape in file {}.", zoneShape, zoneType)
                        return@innerLoop
                    }
                }

                addZone(zoneId, temp)

                val regions = World.getInstance().worldRegions
                for (x in regions.indices) {
                    val xLoc = World.getRegionX(x)
                    val xLoc2 = World.getRegionX(x + 1)
                    for (y in 0 until regions[x].size)
                        if (temp.zone!!.intersectsRectangle(xLoc, xLoc2, World.getRegionY(y), World.getRegionY(y + 1)))
                            regions[x][y].addZone(temp)
                }
            }
        }
    }

    /**
     * Reload zones using following steps :
     *
     *  * Save boss zones data.
     *  * Clean zones from all regions.
     *  * Clear containers.
     *  * Use the regular load process.
     *  * Revalidate zones for all existing creatures.
     *
     */
    fun reload() {
        // Save boss zones data.
        save()

        // Remove zones from world.
        for (regions in World.getInstance().worldRegions) {
            for (region in regions)
                region.zones.clear()
        }

        // Clear _zones and _debugItems Maps.
        _zones.clear()
        clearDebugItems()

        // Reset dynamic id.
        _lastDynamicId = 0

        // Load all zones.
        load()

        // Revalidate creatures in zones.
        for (`object` in World.getInstance().objects) {
            if (`object` is Creature)
                `object`.revalidateZone(true)
        }
    }

    /**
     * Save boss zone data.<br></br>
     * <br></br>
     * We first clear existing entries, than we save each zone data on database.
     */
    fun save() {
        try {
            L2DatabaseFactory.connection.use { con ->
                // clear table first
                var ps = con.prepareStatement(DELETE_GRAND_BOSS_LIST)
                ps.executeUpdate()
                ps.close()

                // store actual data
                ps = con.prepareStatement(INSERT_GRAND_BOSS_LIST)
                for (zone in _zones[BossZone::class.java]?.values.orEmpty()) {
                    for (player in (zone as BossZone).allowedPlayers) {
                        ps.setInt(1, player)
                        ps.setInt(2, zone.id)
                        ps.addBatch()
                    }
                }
                ps.executeBatch()
                ps.close()
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Error storing boss zones.", e)
        }

        IXmlReader.LOGGER.info("Saved boss zones data.")
    }

    /**
     * Add a new zone into _zones [Map]. If the zone type doesn't exist, generate the entry first.
     * @param id : The zone id to add.
     * @param <T> : The [ZoneType] children class.
     * @param zone : The zone to add.
    </T> */
    fun <T : ZoneType> addZone(id: Int?, zone: T) {
        if(id == null)
            return

        var map: MutableMap<Int, T>? = _zones[zone.javaClass] as MutableMap<Int, T>?
        if (map == null) {
            map = HashMap()
            map[id] = zone
            _zones[zone.javaClass] = map
        } else
            map[id] = zone
    }

    /**
     * @param <T> : The [ZoneType] children class.
     * @param type : The Class type to refer.
     * @return all zones by [Class] type.
    </T> */
    fun <T : ZoneType> getAllZones(type: Class<T>): Collection<T> {
        return (_zones[type]?.values as Collection<T>)
    }

    /**
     * @param id : The zone id to retrieve.
     * @return the first zone matching id.
     */
    fun getZoneById(id: Int): ZoneType? {
        for (map in _zones.values) {
            if (map.containsKey(id))
                return map[id]
        }
        return null
    }

    /**
     * @param <T> : The [ZoneType] children class.
     * @param id : The zone id to retrieve.
     * @param type : The Class type to refer.
     * @return a zone by id and [Class].
    </T> */
    fun <T : ZoneType> getZoneById(id: Int, type: Class<T>): T {
        return _zones[type]?.get(id) as T
    }

    /**
     * @param object : The object position to refer.
     * @return all zones based on object position.
     */
    fun getZones(`object`: WorldObject): List<ZoneType> {
        return getZones(`object`.x, `object`.y, `object`.z)
    }

    /**
     * @param <T> : The [ZoneType] children class.
     * @param object : The object position to refer.
     * @param type : The Class type to refer.
     * @return a zone based on object position and zone [Class].
    </T> */
    fun <T : ZoneType> getZone(`object`: WorldObject?, type: Class<T>): T? {
        return if (`object` == null) null else getZone(`object`.x, `object`.y, `object`.z, type)

    }

    /**
     * @param x : The X location to check.
     * @param y : The Y location to check.
     * @return all zones on a 2D plane from given coordinates (no matter their [Class]).
     */
    fun getZones(x: Int, y: Int): List<ZoneType> {
        val temp = ArrayList<ZoneType>()
        for (zone in World.getInstance().getRegion(x, y).zones) {
            if (zone.isInsideZone(x, y))
                temp.add(zone)
        }
        return temp
    }

    /**
     * @param x : The X location to check.
     * @param y : The Y location to check.
     * @param z : The Z location to check.
     * @return all zones on a 3D plane from given coordinates (no matter their [Class]).
     */
    fun getZones(x: Int, y: Int, z: Int): List<ZoneType> {
        val temp = ArrayList<ZoneType>()
        for (zone in World.getInstance().getRegion(x, y).zones) {
            if (zone.isInsideZone(x, y, z))
                temp.add(zone)
        }
        return temp
    }

    /**
     * @param <T> : The [ZoneType] children class.
     * @param x : The X location to check.
     * @param y : The Y location to check.
     * @param type : The Class type to refer.
     * @return a zone based on given coordinates and its [Class].
    </T> */
    fun <T : ZoneType> getZone(x: Int, y: Int, type: Class<T>): T? {
        for (zone in World.getInstance().getRegion(x, y).zones) {
            if (zone.isInsideZone(x, y) && type.isInstance(zone))
                return zone as T
        }
        return null
    }

    /**
     * @param <T> : The [ZoneType] children class.
     * @param x : The X location to check.
     * @param y : The Y location to check.
     * @param z : The Z location to check.
     * @param type : The Class type to refer.
     * @return a zone based on given coordinates and its [Class].
    </T> */
    fun <T : ZoneType> getZone(x: Int, y: Int, z: Int, type: Class<T>): T? {
        for (zone in World.getInstance().getRegion(x, y).zones) {
            if (zone.isInsideZone(x, y, z) && type.isInstance(zone))
                return zone as T
        }
        return null
    }

    /**
     * Add an [ItemInstance] on debug list. Used to visualize zones.
     * @param item : The item to add.
     */
    fun addDebugItem(item: ItemInstance) {
        _debugItems[item.objectId] = item
    }

    /**
     * Remove all [ItemInstance] debug items from the world and clear _debugItems [Map].
     */
    fun clearDebugItems() {
        for (item in _debugItems.values)
            item.decayMe()

        _debugItems.clear()
    }
}