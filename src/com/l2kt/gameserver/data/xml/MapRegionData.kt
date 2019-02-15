package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.MapRegionData.forEach
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.ArenaZone
import com.l2kt.gameserver.model.zone.type.TownZone
import org.w3c.dom.Document
import java.nio.file.Path

/**
 * This class loads and stores map regions values under a 2D int array.<br></br>
 * <br></br>
 * It is notably used to find closest [Location] to teleport, closest [TownZone] zone or name, etc.
 */
object MapRegionData : IXmlReader {
    private const val REGIONS_X = 11
    private const val REGIONS_Y = 16
    private val _regions = Array(REGIONS_X) { IntArray(REGIONS_Y) }

    private val MDT_LOCATION = Location(12661, 181687, -3560)

    fun getMapRegionX(posX: Int): Int {
        // +4 to shift coords center
        return (posX shr 15) + 4
    }

    fun getMapRegionY(posY: Int): Int {
        // +8 to shift coords center
        return (posY shr 15) + 8
    }

    /**
     * @param townId : the townId to match.
     * @return a [TownZone] based on the overall list of existing towns, matching the townId.
     */
    fun getTown(townId: Int): TownZone? {
        return ZoneManager.getAllZones(TownZone::class.java).stream()
            .filter { t -> t.townId == townId }.findFirst().orElse(null)
    }

    /**
     * @param x : The X value (part of 3D point) to check.
     * @param y : The Y value (part of 3D point) to check.
     * @param z : The Z value (part of 3D point) to check.
     * @return a [TownZone] based on the overall list of existing towns, matching X/Y/Z points.
     */
    fun getTown(x: Int, y: Int, z: Int): TownZone? {
        return ZoneManager.getZone(x, y, z, TownZone::class.java)
    }

    enum class TeleportType {
        CASTLE,
        CLAN_HALL,
        SIEGE_FLAG,
        TOWN
    }

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/mapRegions.xml")
        IXmlReader.LOGGER.info("Loaded regions.")
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "map") { mapNode ->
                val attrs = mapNode.attributes
                val rY = parseInteger(attrs, "geoY")!! - 10
                for (rX in 0 until REGIONS_X)
                    _regions[rX][rY] = parseInteger(attrs, "geoX_" + (rX + 16))!!
            }
        }
    }

    fun getMapRegion(posX: Int, posY: Int): Int {
        return _regions[getMapRegionX(posX)][getMapRegionY(posY)]
    }

    /**
     * @param x : The X value (part of 2D point) to check.
     * @param y : The Y value (part of 2D point) to check.
     * @return the castleId associated to the townId, based on X/Y points.
     */
    fun getAreaCastle(x: Int, y: Int): Int {
        when (getMapRegion(x, y)) {
            0 // Talking Island Village
                , 5 // Town of Gludio
                , 6 // Gludin Village
            -> return 1

            7 // Town of Dion
            -> return 2

            8 // Town of Giran
                , 12 // Giran Harbor
            -> return 3

            1 // Elven Village
                , 2 // Dark Elven Village
                , 9 // Town of Oren
                , 17 // Floran Village
            -> return 4

            10 // Town of Aden
                , 11 // Hunters Village
            -> return 5

            13 // Heine
            -> return 6

            15 // Town of Goddard
            -> return 7

            14 // Rune Township
                , 18 // Primeval Isle Wharf
            -> return 8

            3 // Orc Village
                , 4 // Dwarven Village
                , 16 // Town of Schuttgart
            -> return 9
            else // Town of Aden
            -> return 5
        }
    }

    /**
     * @param x : The X value (part of 2D point) to check.
     * @param y : The Y value (part of 2D point) to check.
     * @return a String consisting of town name, based on X/Y points.
     */
    fun getClosestTownName(x: Int, y: Int): String {
        when (getMapRegion(x, y)) {
            0 -> return "Talking Island Village"
            1 -> return "Elven Village"
            2 -> return "Dark Elven Village"
            3 -> return "Orc Village"
            4 -> return "Dwarven Village"
            5 -> return "Town of Gludio"
            6 -> return "Gludin Village"
            7 -> return "Town of Dion"
            8 -> return "Town of Giran"
            9 -> return "Town of Oren"
            10 -> return "Town of Aden"
            11 -> return "Hunters Village"
            12 -> return "Giran Harbor"
            13 -> return "Heine"
            14 -> return "Rune Township"
            15 -> return "Town of Goddard"
            16 -> return "Town of Schuttgart"
            17 -> return "Floran Village"
            18 -> return "Primeval Isle"
            else -> return "Town of Aden"
        }
    }

    /**
     * @param creature : The Creature to check.
     * @param teleportType : The TeleportType to check.
     * @return a [Location] based on [Creature] and [TeleportType] parameters.
     */
    fun getLocationToTeleport(creature: Creature, teleportType: TeleportType): Location? {
        // The character isn't a player, bypass all checks and retrieve a random spawn location on closest town.
        if (creature !is Player)
            return getClosestTown(creature)!!.randomLoc

// The player is in MDT, move him out.
        if (creature.isInsideZone(ZoneId.MONSTER_TRACK))
            return MDT_LOCATION

        if (teleportType != TeleportType.TOWN && creature.clan != null) {
            if (teleportType == TeleportType.CLAN_HALL) {
                val ch = ClanHallManager.getInstance().getClanHallByOwner(creature.clan)
                if (ch != null) {
                    val zone = ch.zone
                    if (zone != null)
                        return zone.randomLoc
                }
            } else if (teleportType == TeleportType.CASTLE) {
                // Check if the player is part of a castle owning clan.
                var castle = CastleManager.getCastleByOwner(creature.clan)
                if (castle == null) {
                    // If not, check if he is in defending side.
                    castle = CastleManager.getCastle(creature)
                    if (!(castle != null && castle.siege.isInProgress && castle.siege.checkSides(
                            creature.clan,
                            SiegeSide.DEFENDER,
                            SiegeSide.OWNER
                        ))
                    )
                        castle = null
                }

                if (castle != null && castle.castleId > 0)
                    return castle.castleZone.randomLoc
            } else if (teleportType == TeleportType.SIEGE_FLAG) {
                val siege = CastleManager.getActiveSiege(creature)
                if (siege != null) {
                    val flag = siege.getFlag(creature.clan)
                    if (flag != null)
                        return flag.position
                }
            }
        }

        // Check if the player needs to be teleported in second closest town, during an active siege.
        val castle = CastleManager.getCastle(creature)
        if (castle != null && castle.siege.isInProgress)
            return if (creature.karma > 0) castle.siegeZone.randomChaoticLoc else castle.siegeZone.randomLoc

        // Karma player lands out of city.
        if (creature.karma > 0)
            return getClosestTown(creature)!!.randomChaoticLoc

        // Check if player is in arena.
        val arena = ZoneManager.getZone(creature, ArenaZone::class.java)
        return if (arena != null) arena.randomLoc else getClosestTown(creature)!!.randomLoc

        // Retrieve a random spawn location of the nearest town.
    }

    private fun getClosestTown(creature: Creature): TownZone? {
        when (getMapRegion(creature.x, creature.y)) {
            0 -> return getTown(2) // TI
            1// Elven
            -> return getTown(if (creature is Player && creature.template.race == ClassRace.DARK_ELF) 1 else 3)
            2// DE
            -> return getTown(if (creature is Player && creature.template.race == ClassRace.ELF) 3 else 1)
            3 -> return getTown(4) // Orc
            4// Dwarven
            -> return getTown(6)
            5// Gludio
            -> return getTown(7)
            6// Gludin
            -> return getTown(5)
            7 // Dion
            -> return getTown(8)
            8 // Giran
                , 12 // Giran Harbor
            -> return getTown(9)

            9 // Oren
            -> return getTown(10)

            10 // Aden
            -> return getTown(12)

            11 // HV
            -> return getTown(11)

            13 // Heine
            -> return getTown(15)

            14 // Rune
            -> return getTown(14)

            15 // Goddard
            -> return getTown(13)

            16 // Schuttgart
            -> return getTown(17)

            17// Floran
            -> return getTown(16)

            18// Primeval Isle
            -> return getTown(19)
        }
        return getTown(16) // Default to floran
    }

    /**
     * @param x : The X value (part of 2D point) to check.
     * @param y : The Y value (part of 2D point) to check.
     * @return the closest regionId based on X/Y points.
     */
    fun getClosestLocation(x: Int, y: Int): Int {
        when (getMapRegion(x, y)) {
            0 // TI
            -> return 1

            1 // Elven
            -> return 4

            2 // DE
            -> return 3

            3 // Orc
                , 4 // Dwarven
                , 16// Schuttgart
            -> return 9

            5 // Gludio
                , 6 // Gludin
            -> return 2

            7 // Dion
            -> return 5

            8 // Giran
                , 12 // Giran Harbor
            -> return 6

            9 // Oren
            -> return 10

            10 // Aden
            -> return 13

            11 // HV
            -> return 11

            13 // Heine
            -> return 12

            14 // Rune
            -> return 14

            15 // Goddard
            -> return 15
        }
        return 0
    }
}