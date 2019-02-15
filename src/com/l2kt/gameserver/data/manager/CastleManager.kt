package com.l2kt.gameserver.data.manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.manager.CastleManager.forEach
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.item.MercenaryTicket
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.location.TowerSpawnLocation
import com.l2kt.gameserver.model.pledge.Clan
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * Loads and stores [Castle]s informations, using database and XML informations.
 */
object CastleManager : IXmlReader {

    private val _castles = HashMap<Int, Castle>()
    private const val LOAD_CASTLES = "SELECT * FROM castle ORDER BY id"
    private const val LOAD_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle=?"
    private const val RESET_CERTIFICATES = "UPDATE castle SET certificates=300"

    val castles: Collection<Castle>
        get() = _castles.values

    init {
        // Generate Castle objects with dynamic data.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_CASTLES).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val id = rs.getInt("id")
                            val castle = Castle(id, rs.getString("name"))

                            castle.siegeDate = Calendar.getInstance()
                            castle.siegeDate.timeInMillis = rs.getLong("siegeDate")
                            castle.isTimeRegistrationOver = rs.getBoolean("regTimeOver")
                            castle.setTaxPercent(rs.getInt("taxPercent"), false)
                            castle.treasury = rs.getLong("treasury")
                            castle.setLeftCertificates(rs.getInt("certificates"), false)

                            con.prepareStatement(LOAD_OWNER).use { ps1 ->
                                ps1.setInt(1, id)
                                ps1.executeQuery().use { rs1 ->
                                    while (rs1.next()) {
                                        val ownerId = rs1.getInt("clan_id")
                                        if (ownerId > 0) {
                                            val clan = ClanTable.getClan(ownerId)
                                            if (clan != null)
                                                castle.ownerId = ownerId
                                        }
                                    }
                                }
                            }

                            _castles[id] = castle
                        }
                    }
                }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Failed to load castles.", e)
        }

        // Feed Castle objects with static data.
        load()

        // Load traps informations. Generate siege entities for every castle (if not handled, it's only processed during player login).
        for (castle in _castles.values) {
            castle.loadTrapUpgrade()
            castle.siege = Siege(castle)
        }
    }

    override fun load() {
        parseFile("./data/xml/castles.xml")
        IXmlReader.LOGGER.info("Loaded {} castles.", _castles.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "castle") { castleNode ->
                val attrs = castleNode.attributes
                val castle = _castles[parseInteger(attrs, "id")]
                if (castle != null) {
                    castle.circletId = parseInteger(attrs, "circletId")!!
                    forEach(
                        castleNode,
                        "artifact"
                    ) { artifactNode -> castle.setArtifacts(parseString(artifactNode.attributes, "val")) }
                    forEach(castleNode, "controlTowers") { controlTowersNode ->
                        forEach(controlTowersNode, "tower") { towerNode ->
                            val location =
                                parseString(towerNode.attributes, "loc").split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            castle.controlTowers.add(
                                TowerSpawnLocation(
                                    13002,
                                    SpawnLocation(
                                        Integer.parseInt(location[0]),
                                        Integer.parseInt(location[1]),
                                        Integer.parseInt(location[2]),
                                        -1
                                    )
                                )
                            )
                        }
                    }
                    forEach(castleNode, "flameTowers") { flameTowersNode ->
                        forEach(flameTowersNode, "tower") { towerNode ->
                            val towerAttrs = towerNode.attributes
                            val location =
                                parseString(towerAttrs, "loc").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            castle.flameTowers.add(
                                TowerSpawnLocation(
                                    13004,
                                    SpawnLocation(
                                        Integer.parseInt(location[0]),
                                        Integer.parseInt(location[1]),
                                        Integer.parseInt(location[2]),
                                        -1
                                    ),
                                    parseString(
                                        towerAttrs,
                                        "zones"
                                    ).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                )
                            )
                        }
                    }
                    forEach(castleNode, "relatedNpcIds") { relatedNpcIdsNode ->
                        castle.setRelatedNpcIds(
                            parseString(
                                relatedNpcIdsNode.attributes,
                                "val"
                            )
                        )
                    }
                    forEach(castleNode, "tickets") { ticketsNode ->
                        forEach(ticketsNode, "ticket") { ticketNode ->
                            castle.tickets.add(MercenaryTicket(parseAttributes(ticketNode)))
                        }
                    }
                }
            }
        }
    }

    fun getCastleById(castleId: Int): Castle? {
        return _castles[castleId]
    }

    fun getCastleByOwner(clan: Clan): Castle? {
        return _castles.values.firstOrNull { c -> c.ownerId == clan.clanId }
    }

    fun getCastleByName(name: String): Castle? {
        return _castles.values.firstOrNull { c -> c.name.equals(name, ignoreCase = true) }
    }

    fun getCastle(x: Int, y: Int, z: Int): Castle? {
        return _castles.values.firstOrNull { c -> c.checkIfInZone(x, y, z) }
    }

    fun getCastle(`object`: WorldObject): Castle? {
        return getCastle(`object`.x, `object`.y, `object`.z)
    }

    fun validateTaxes(sealStrifeOwner: CabalType) {
        val maxTax: Int = when (sealStrifeOwner) {
            SevenSigns.CabalType.DAWN -> 25

            SevenSigns.CabalType.DUSK -> 5

            else -> 15
        }

        _castles.values.filter { c -> c.taxPercent > maxTax }.forEach { c -> c.setTaxPercent(maxTax, true) }
    }

    fun getActiveSiege(`object`: WorldObject): Siege? {
        return getActiveSiege(`object`.x, `object`.y, `object`.z)
    }

    fun getActiveSiege(x: Int, y: Int, z: Int): Siege? {
        for (castle in _castles.values)
            if (castle.siege.checkIfInZone(x, y, z))
                return castle.siege

        return null
    }

    /**
     * Reset all castles certificates. Reset the memory value, and run a unique query.
     */
    fun resetCertificates() {
        // Reset memory. Don't use the inner save.
        for (castle in _castles.values)
            castle.setLeftCertificates(300, false)

        // Update all castles with a single query.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESET_CERTIFICATES).use { ps -> ps.executeUpdate() }
            }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Failed to reset certificates.", e)
        }

    }
}