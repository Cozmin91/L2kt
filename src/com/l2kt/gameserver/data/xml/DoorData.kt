package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.geometry.Polygon
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.xml.DoorData.forEach
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.geoengine.geodata.GeoStructure
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.template.DoorTemplate
import com.l2kt.gameserver.model.actor.template.DoorTemplate.DoorType
import com.l2kt.gameserver.model.entity.Castle
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [Door]s.<br></br>
 * <br></br>
 * The different informations help to generate a [DoorTemplate] and a GeoObject, then we create the Door instance itself. The spawn is made just after the initialization of this class to avoid NPEs.
 */
object DoorData : IXmlReader {
    private val _doors = HashMap<Int, Door>()

    val doors: Collection<Door>
        get() = _doors.values

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/doors.xml")
        IXmlReader.LOGGER.info("Loaded {} doors templates.", _doors.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "door") { doorNode ->
                val set = parseAttributes(doorNode)
                val id = set.getInteger("id")
                forEach(doorNode, "castle") { castleNode ->
                    set.set(
                        "castle",
                        parseString(castleNode.attributes, "id")
                    )
                }
                forEach(doorNode, "position") { positionNode ->
                    val attrs = positionNode.attributes
                    set.set("posX", parseInteger(attrs, "x"))
                    set.set("posY", parseInteger(attrs, "y"))
                    set.set("posZ", parseInteger(attrs, "z"))
                }

                val coords = ArrayList<IntArray>()
                forEach(doorNode, "coordinates") { coordinatesNode ->
                    forEach(coordinatesNode, "loc") { locNode ->
                        val attrs = locNode.attributes
                        coords.add(intArrayOf(parseInteger(attrs, "x")!!, parseInteger(attrs, "y")!!))
                    }
                }

                var minX = Integer.MAX_VALUE
                var maxX = Integer.MIN_VALUE
                var minY = Integer.MAX_VALUE
                var maxY = Integer.MIN_VALUE
                for (coord in coords) {
                    minX = Math.min(minX, coord[0])
                    maxX = Math.max(maxX, coord[0])
                    minY = Math.min(minY, coord[1])
                    maxY = Math.max(maxY, coord[1])
                }

                forEach(doorNode, "stats|function") { node -> set.putAll(parseAttributes(node)) }

                val posX = set.getInteger("posX")
                val posY = set.getInteger("posY")
                val posZ = set.getInteger("posZ")
                val x = GeoEngine.getGeoX(minX) - 1
                val y = GeoEngine.getGeoY(minY) - 1
                val sizeX = GeoEngine.getGeoX(maxX) + 1 - x + 1
                val sizeY = GeoEngine.getGeoY(maxY) + 1 - y + 1
                val geoX = GeoEngine.getGeoX(posX)
                val geoY = GeoEngine.getGeoY(posY)
                val geoZ = GeoEngine.getHeightNearest(geoX, geoY, posZ).toInt()
                val block = GeoEngine.getBlock(geoX, geoY)
                val i = block.getIndexAbove(geoX, geoY, geoZ)
                if (i != -1) {
                    val layerDiff = block.getHeight(i) - geoZ
                    if (set.getInteger("height") > layerDiff)
                        set.set("height", layerDiff - GeoStructure.CELL_IGNORE_HEIGHT)
                }
                val limit = if (set.getEnum(
                        "type",
                        DoorType::class.java
                    ) == DoorType.WALL
                ) GeoStructure.CELL_IGNORE_HEIGHT * 4 else GeoStructure.CELL_IGNORE_HEIGHT
                val inside = Array(sizeX) { BooleanArray(sizeY) }
                val polygon = Polygon(id, coords)
                for (ix in 0 until sizeX) {
                    for (iy in 0 until sizeY) {
                        val gx = x + ix
                        val gy = y + iy
                        val z = GeoEngine.getHeightNearest(gx, gy, posZ).toInt()
                        if (Math.abs(z - posZ) > limit)
                            continue

                        val worldX = GeoEngine.getWorldX(gx)
                        val worldY = GeoEngine.getWorldY(gy)

                        var wix = worldX - 6
                        cell@ while (wix <= worldX + 6) {
                            var wiy = worldY - 6
                            while (wiy <= worldY + 6) {
                                if (polygon.isInside(wix, wiy)) {
                                    inside[ix][iy] = true
                                    break@cell
                                }
                                wiy += 2
                            }
                            wix += 2
                        }
                    }
                }

                set.set("geoX", x)
                set.set("geoY", y)
                set.set("geoZ", geoZ)
                set.set("geoData", GeoEngine.calculateGeoObject(inside))
                set.set("pAtk", 0)
                set.set("mAtk", 0)
                set.set("runSpd", 0)
                set.set("radius", 16)

                val template = DoorTemplate(set)
                val door = Door(IdFactory.getInstance().nextId, template)
                door.setCurrentHpMp(door.maxHp.toDouble(), door.maxMp.toDouble())
                door.position.set(posX, posY, posZ)
                _doors[door.doorId] = door
            }
        }
    }

    fun reload() {
        for (door in _doors.values)
            door.openMe()

        _doors.clear()

        for (castle in CastleManager.castles)
            castle.doors.clear()

        load()
        spawn()
    }

    /**
     * Spawns [Door]s into the world. If this door is associated to a [Castle], we load door upgrade aswell.<br></br>
     * <br></br>
     * Note: keep as side-method, do not join to the load(). On initial load, the DoorTable.getInstance() is not initialized, yet Door is calling it during spawn process...causing NPE.
     */
    fun spawn() {
        // spawn doors
        for (door in _doors.values)
            door.spawnMe()

        // load doors upgrades
        for (castle in CastleManager.castles)
            castle.loadDoorUpgrade()
    }

    fun getDoor(id: Int): Door? {
        return _doors[id]
    }
}