package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.manager.DimensionalRiftManager.forEach
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.rift.DimensionalRift
import com.l2kt.gameserver.model.rift.DimensionalRiftRoom
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * Loads and stores available [DimensionalRiftRoom]s for the [DimensionalRift] system.
 */
object DimensionalRiftManager : IXmlReader {

    private val _rooms = HashMap<Byte, MutableMap<Byte, DimensionalRiftRoom>>(7)
    private const val DIMENSIONAL_FRAGMENT = 7079

    /**
     * @param type : The type of rift to check.
     * @return the amount of needed Dimensional Fragments for the given type of rift.
     */
    private fun getNeededItems(type: Byte): Int {
        when (type.toInt()) {
            1 -> return Config.RIFT_ENTER_COST_RECRUIT
            2 -> return Config.RIFT_ENTER_COST_SOLDIER
            3 -> return Config.RIFT_ENTER_COST_OFFICER
            4 -> return Config.RIFT_ENTER_COST_CAPTAIN
            5 -> return Config.RIFT_ENTER_COST_COMMANDER
            6 -> return Config.RIFT_ENTER_COST_HERO
            else -> throw IndexOutOfBoundsException()
        }
    }

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/dimensionalRift.xml")
        IXmlReader.LOGGER.info("Loaded Dimensional Rift rooms.")
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "area") { areaNode ->
                val areaAttrs = areaNode.attributes

                val type = java.lang.Byte.parseByte(areaAttrs.getNamedItem("type").nodeValue)

                // Generate new layer of rooms if that type doesn't exist yet.
                if (!_rooms.containsKey(type))
                    _rooms[type] = HashMap(9)

                forEach(areaNode, "room") { roomNode ->
                    // Generate the room using StatsSet content.
                    val riftRoom = DimensionalRiftRoom(type, parseAttributes(roomNode))

                    // Store it.
                    _rooms[type]?.set(riftRoom.id, riftRoom)

                    forEach(roomNode, "spawn") innerLoop@{ spawnNode ->
                        val spawnAttrs = spawnNode.attributes

                        val mobId = Integer.parseInt(spawnAttrs.getNamedItem("mobId").nodeValue)
                        val delay = Integer.parseInt(spawnAttrs.getNamedItem("delay").nodeValue)
                        val count = Integer.parseInt(spawnAttrs.getNamedItem("count").nodeValue)

                        val template = NpcData.getTemplate(mobId)
                        if (template == null) {
                            IXmlReader.LOGGER.warn("Template $mobId not found!")
                            return@innerLoop
                        }

                        try {
                            for (i in 0 until count) {
                                val spawnDat = L2Spawn(template)
                                spawnDat.setLoc(riftRoom.randomX, riftRoom.randomY, DimensionalRiftRoom.Z_VALUE, -1)
                                spawnDat.respawnDelay = delay
                                SpawnTable.addNewSpawn(spawnDat, false)

                                riftRoom.spawns.add(spawnDat)
                            }
                        } catch (e: Exception) {
                            IXmlReader.LOGGER.error("Failed to initialize a spawn.", e)
                        }
                    }
                }
            }
        }
    }

    fun reload() {
        // For every room of every area, clean the spawns, area data and then _rooms data.
        for (area in _rooms.values) {
            for (room in area.values)
                room.spawns.clear()

            area.clear()
        }
        _rooms.clear()

        // Reload the static data.
        load()
    }

    fun getRoom(type: Byte, room: Byte): DimensionalRiftRoom? {
        val area = _rooms[type]
        return if (area == null) null else area[room]
    }

    fun checkIfInRiftZone(x: Int, y: Int, z: Int, ignorePeaceZone: Boolean): Boolean {
        val area = _rooms[0.toByte()] ?: return false

        return if (ignorePeaceZone) area[1.toByte()]!!.checkIfInZone(x, y, z) else area[1.toByte()]!!.checkIfInZone(x, y, z) && !area[0.toByte()]!!.checkIfInZone(x, y, z)
    }

    fun checkIfInPeaceZone(x: Int, y: Int, z: Int): Boolean {
        val room = getRoom(0.toByte(), 0.toByte()) ?: return false

        return room.checkIfInZone(x, y, z)
    }

    /**
     * Teleport the [Player] into the waiting room.
     * @param player : The Player to teleport.
     */
    fun teleportToWaitingRoom(player: Player) {
        val room = getRoom(0.toByte(), 0.toByte()) ?: return

        player.teleToLocation(room.teleportLoc)
    }

    /**
     * Start the [DimensionalRift] process for a [Player] [Party]. Numerous checks are processed (party leader, already rifting, not enough mats, no free cells,...).
     * @param player : The Player to test.
     * @param type : The type of rift to start.
     * @param npc : The given [Npc].
     */
    @Synchronized
    fun start(player: Player, type: Byte, npc: Npc) {
        val party = player.party

        // No party.
        if (party == null) {
            showHtmlFile(player, "data/html/seven_signs/rift/NoParty.htm", npc)
            return
        }

        // Player isn't the party leader.
        if (!party.isLeader(player)) {
            showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc)
            return
        }

        // Party is already in rift.
        if (party.isInDimensionalRift)
            return

        // Party members' count is lower than config.
        if (party.membersCount < Config.RIFT_MIN_PARTY_SIZE) {
            val html = NpcHtmlMessage(npc.objectId)
            html.setFile("data/html/seven_signs/rift/SmallParty.htm")
            html.replace("%npc_name%", npc.name)
            html.replace("%count%", Integer.toString(Config.RIFT_MIN_PARTY_SIZE))
            player.sendPacket(html)
            return
        }

        // Rift is full.
        val availableRooms = getFreeRooms(type, false)
        if (availableRooms.isEmpty()) {
            val html = NpcHtmlMessage(npc.objectId)
            html.setFile("data/html/seven_signs/rift/Full.htm")
            html.replace("%npc_name%", npc.name)
            player.sendPacket(html)
            return
        }

        // One of teammates isn't on peace zone or hasn't required amount of items.
        for (member in party.members) {
            if (!checkIfInPeaceZone(member.x, member.y, member.z)) {
                showHtmlFile(player, "data/html/seven_signs/rift/NotInWaitingRoom.htm", npc)
                return
            }
        }

        val count = getNeededItems(type)

        // Check if every party member got a Dimensional Fragment.
        for (member in party.members) {
            val item = member.inventory!!.getItemByItemId(DIMENSIONAL_FRAGMENT)
            if (item == null || item.count < getNeededItems(type)) {
                val html = NpcHtmlMessage(npc.objectId)
                html.setFile("data/html/seven_signs/rift/NoFragments.htm")
                html.replace("%npc_name%", npc.name)
                html.replace("%count%", Integer.toString(count))
                player.sendPacket(html)
                return
            }
        }

        // Delete the Dimensional Fragment for every member.
        for (member in party.members)
            member.destroyItemByItemId("RiftEntrance", DIMENSIONAL_FRAGMENT, count, null, true)

        // Creates an instance of the rift.
        DimensionalRift(party, Rnd[availableRooms]!!)
    }

    fun showHtmlFile(player: Player, file: String, npc: Npc) {
        val html = NpcHtmlMessage(npc.objectId)
        html.setFile(file)
        html.replace("%npc_name%", npc.name)
        player.sendPacket(html)
    }

    /**
     * @param type : The type of area to test.
     * @param canUseBossRoom : If true, the boss zone is considered as valid zone.
     * @return a [List] of all available [DimensionalRiftRoom]s. A free room is considered without party inside. Boss room is a valid room according the boolean flag.
     */
    fun getFreeRooms(type: Byte, canUseBossRoom: Boolean): List<DimensionalRiftRoom> {
        return _rooms[type]?.values?.filter { r -> !r.isPartyInside && (canUseBossRoom || !r.isBossRoom) }.orEmpty()
    }

    fun onPartyEdit(party: Party?) {
        if (party == null)
            return

        val rift = party.dimensionalRift
        if (rift != null) {
            for (member in party.members)
                teleportToWaitingRoom(member)

            rift.killRift()
        }
    }
}