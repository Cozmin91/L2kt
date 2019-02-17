package com.l2kt.gameserver.model.entity

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.HolyThing
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.MercenaryTicket
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.TowerSpawnLocation
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.ClanMember
import com.l2kt.gameserver.model.zone.type.CastleTeleportZone
import com.l2kt.gameserver.model.zone.type.CastleZone
import com.l2kt.gameserver.model.zone.type.SiegeZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

class Castle(val castleId: Int, val name: String) {

    var circletId: Int = 0
    var ownerId: Int = 0

    val doors: MutableList<Door> = ArrayList()
    val tickets: MutableList<MercenaryTicket> = ArrayList(60)
    private val _artifacts = ArrayList<Int>(1)
    private val _relatedNpcIds = ArrayList<Int>()

    private val _droppedTickets = ConcurrentSkipListSet<ItemInstance>()
    private val _siegeGuards = ArrayList<Npc>()

    val controlTowers: MutableList<TowerSpawnLocation> = ArrayList()
    val flameTowers: MutableList<TowerSpawnLocation> = ArrayList()

    lateinit var siege: Siege
    var siegeDate: Calendar? = null
    var isTimeRegistrationOver = true

    var taxPercent: Int = 0
        private set
    var taxRate: Double = 0.toDouble()
        private set
    var treasury: Long = 0

    var siegeZone: SiegeZone? = null
        private set
    var castleZone: CastleZone? = null
        private set
    var teleZone: CastleTeleportZone? = null
        private set

    var leftCertificates: Int = 0
        private set

    val droppedTickets: Set<ItemInstance>
        get() = _droppedTickets

    val relatedNpcIds: List<Int>
        get() = _relatedNpcIds

    val artifacts: List<Int>
        get() = _artifacts

    init {

        // Feed _siegeZone.
        for (zone in ZoneManager.getAllZones(SiegeZone::class.java)) {
            if (zone.siegeObjectId == castleId) {
                siegeZone = zone
                break
            }
        }

        // Feed _castleZone.
        for (zone in ZoneManager.getAllZones(CastleZone::class.java)) {
            if (zone.castleId == castleId) {
                castleZone = zone
                break
            }
        }

        // Feed _teleZone.
        for (zone in ZoneManager.getAllZones(CastleTeleportZone::class.java)) {
            if (zone.castleId == castleId) {
                teleZone = zone
                break
            }
        }
    }

    @Synchronized
    fun engrave(clan: Clan, target: WorldObject) {
        if (!isGoodArtifact(target))
            return

        setOwner(clan)

        // "Clan X engraved the ruler" message.
        siege!!.announceToPlayers(
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_ENGRAVED_RULER).addString(clan.name),
            true
        )
    }

    /**
     * Add amount to castle's treasury (warehouse).
     * @param amount The amount to add.
     */
    fun addToTreasury(amount: Int) {
        var amount = amount
        if (ownerId <= 0)
            return

        if (name.equals("Schuttgart", ignoreCase = true) || name.equals("Goddard", ignoreCase = true)) {
            val rune = CastleManager.getCastleByName("rune")
            if (rune != null) {
                val runeTax = (amount * rune.taxRate).toInt()
                if (rune.ownerId > 0)
                    rune.addToTreasury(runeTax)
                amount -= runeTax
            }
        }

        if (!name.equals("aden", ignoreCase = true) && !name.equals(
                "Rune",
                ignoreCase = true
            ) && !name.equals("Schuttgart", ignoreCase = true) && !name.equals("Goddard", ignoreCase = true)
        )
        // If current castle instance is not Aden, Rune, Goddard or Schuttgart.
        {
            val aden = CastleManager.getCastleByName("aden")
            if (aden != null) {
                val adenTax =
                    (amount * aden.taxRate).toInt() // Find out what Aden gets from the current castle instance's income
                if (aden.ownerId > 0)
                    aden.addToTreasury(adenTax) // Only bother to really add the tax to the treasury if not npc owned

                amount -= adenTax // Subtract Aden's income from current castle instance's income
            }
        }

        addToTreasuryNoTax(amount.toLong())
    }

    /**
     * Add amount to castle instance's treasury (warehouse), no tax paying.
     * @param amount The amount of adenas to add to treasury.
     * @return true if successful.
     */
    fun addToTreasuryNoTax(amount: Long): Boolean {
        var amount = amount
        if (ownerId <= 0)
            return false

        if (amount < 0) {
            amount *= -1
            if (treasury < amount)
                return false
            treasury -= amount
        } else {
            if (treasury + amount > Integer.MAX_VALUE)
                treasury = Integer.MAX_VALUE.toLong()
            else
                treasury += amount
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_TREASURY).use { ps ->
                    ps.setLong(1, treasury)
                    ps.setInt(2, castleId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update treasury.", e)
        }

        return true
    }

    /**
     * Move non clan members off castle area and to nearest town.
     */
    fun banishForeigners() {
        castleZone!!.banishForeigners(ownerId)
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return true if object is inside the zone
     */
    fun checkIfInZone(x: Int, y: Int, z: Int): Boolean {
        return siegeZone!!.isInsideZone(x, y, z)
    }

    fun oustAllPlayers() {
        teleZone!!.oustAllPlayers()
    }

    /**
     * Set (and optionally save on database) left certificates count.
     * @param leftCertificates : the count to save.
     * @param save : true means we store it on database. Basically setted to false on server startup.
     */
    fun setLeftCertificates(leftCertificates: Int, save: Boolean) {
        this.leftCertificates = leftCertificates

        if (save) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_CERTIFICATES).use { ps ->
                        ps.setInt(1, leftCertificates)
                        ps.setInt(2, castleId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't update certificates amount.", e)
            }

        }
    }

    /**
     * Get the object distance to this castle zone.
     * @param obj The WorldObject to make tests on.
     * @return the distance between the WorldObject and the zone.
     */
    fun getDistance(obj: WorldObject): Double {
        return siegeZone!!.getDistanceToZone(obj)
    }

    fun closeDoor(activeChar: Player, doorId: Int) {
        openCloseDoor(activeChar, doorId, false)
    }

    fun openDoor(activeChar: Player, doorId: Int) {
        openCloseDoor(activeChar, doorId, true)
    }

    fun openCloseDoor(activeChar: Player, doorId: Int, open: Boolean) {
        if (activeChar.clanId != ownerId)
            return

        val door = getDoor(doorId)
        if (door != null) {
            if (open)
                door.openMe()
            else
                door.closeMe()
        }
    }

    /**
     * This method setup the castle owner.
     * @param clan The clan who will own the castle.
     */
    fun setOwner(clan: Clan?) {
        // Act only if castle owner is different of NPC, or if old owner is different of new owner.
        if (ownerId > 0 && (clan == null || clan.clanId != ownerId)) {
            // Try to find clan instance of the old owner.
            val oldOwner = ClanTable.getClan(ownerId)
            if (oldOwner != null) {
                // Dismount the old leader if he was riding a wyvern.
                val oldLeader = oldOwner.leader!!.playerInstance
                if (oldLeader != null) {
                    if (oldLeader.mountType == 2)
                        oldLeader.dismount()
                }

                // Unset castle flag for old owner clan.
                oldOwner.setCastle(0)
            }
        }

        // Update database.
        updateOwnerInDB(clan)

        // If siege is in progress, mid victory phase of siege.
        if (siege!!.isInProgress) {
            siege!!.midVictory()

            // "There is a new castle Lord" message when the castle change of hands. Message sent for both sides.
            siege!!.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.NEW_CASTLE_LORD), true)
        }
    }

    /**
     * Remove the castle owner. This method is only used by admin command.
     */
    fun removeOwner() {
        if (ownerId <= 0)
            return

        val clan = ClanTable.getClan(ownerId) ?: return

        clan.setCastle(0)
        clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))

        // Remove clan from siege registered clans (as owners are automatically added).
        siege!!.registeredClans.remove(clan)

        updateOwnerInDB(null)

        if (siege!!.isInProgress)
            siege!!.midVictory()
        else
            checkItemsForClan(clan)
    }

    /**
     * This method updates the castle tax rate.
     * @param activeChar Sends informative messages to that character (success or fail).
     * @param taxPercent The new tax rate to apply.
     */
    fun setTaxPercent(activeChar: Player, taxPercent: Int) {
        val maxTax: Int
        when (SevenSigns.getSealOwner(SealType.STRIFE)) {
            SevenSigns.CabalType.DAWN -> maxTax = 25

            SevenSigns.CabalType.DUSK -> maxTax = 5

            else -> maxTax = 15
        }

        if (taxPercent < 0 || taxPercent > maxTax) {
            activeChar.sendMessage("Tax value must be between 0 and $maxTax.")
            return
        }

        setTaxPercent(taxPercent, true)
        activeChar.sendMessage("$name castle tax changed to $taxPercent%.")
    }

    fun setTaxPercent(taxPercent: Int, save: Boolean) {
        this.taxPercent = taxPercent
        taxRate = this.taxPercent / 100.0

        if (save) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement = con.prepareStatement("UPDATE castle SET taxPercent = ? WHERE id = ?")
                    statement.setInt(1, taxPercent)
                    statement.setInt(2, castleId)
                    statement.execute()
                    statement.close()
                }
            } catch (e: Exception) {
            }

        }
    }

    /**
     * Respawn doors associated to that castle.
     * @param isDoorWeak if true, spawn doors with 50% max HPs.
     */
    fun spawnDoors(isDoorWeak: Boolean) {
        for (door in doors) {
            if (door.isDead)
                door.doRevive()

            door.closeMe()
            door.currentHp = (if (isDoorWeak) door.maxHp / 2 else door.maxHp).toDouble()
            door.broadcastStatusUpdate()
        }
    }

    /**
     * Close doors associated to that castle.
     */
    fun closeDoors() {
        for (door in doors)
            door.closeMe()
    }

    /**
     * Upgrade door.
     * @param doorId The doorId to affect.
     * @param hp The hp ratio.
     * @param db If set to true, save changes on database.
     */
    fun upgradeDoor(doorId: Int, hp: Int, db: Boolean) {
        val door = getDoor(doorId) ?: return

        door.stat.upgradeHpRatio = hp
        door.currentHp = door.maxHp.toDouble()

        if (db) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_DOORS).use { ps ->
                        ps.setInt(1, doorId)
                        ps.setInt(2, hp)
                        ps.setInt(3, castleId)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't upgrade castle doors.", e)
            }

        }
    }

    /**
     * This method loads castle door upgrade data from database.
     */
    fun loadDoorUpgrade() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_DOORS).use { ps ->
                    ps.setInt(1, castleId)

                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            upgradeDoor(rs.getInt("doorId"), rs.getInt("hp"), false)
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load door upgrades.", e)
        }

    }

    /**
     * This method is only used on siege midVictory.
     */
    fun removeDoorUpgrade() {
        for (door in doors)
            door.stat.upgradeHpRatio = 1

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_DOOR).use { ps ->
                    ps.setInt(1, castleId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete door upgrade.", e)
        }

    }

    private fun updateOwnerInDB(clan: Clan?) {
        if (clan != null)
            ownerId = clan.clanId // Update owner id property
        else {
            ownerId = 0 // Remove owner
            CastleManorManager.resetManorData(castleId)
        }

        if (clan != null) {
            // Set castle for new owner.
            clan.setCastle(castleId)

            // Announce to clan members.
            clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan), PlaySound(1, "Siege_Victory"))
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_OWNER).use { ps ->
                    con.prepareStatement(UPDATE_OWNER).use { ps2 ->
                        ps.setInt(1, castleId)
                        ps.execute()

                        ps2.setInt(1, castleId)
                        ps2.setInt(2, ownerId)
                        ps2.execute()
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update castle owner.", e)
        }

    }

    fun getDoor(doorId: Int): Door? {
        for (door in doors) {
            if (door.doorId == doorId)
                return door
        }
        return null
    }

    fun getTicket(itemId: Int): MercenaryTicket? {
        return tickets.stream().filter { t -> t.itemId == itemId }.findFirst().orElse(null)
    }

    fun addDroppedTicket(item: ItemInstance) {
        _droppedTickets.add(item)
    }

    fun removeDroppedTicket(item: ItemInstance) {
        _droppedTickets.remove(item)
    }

    fun getDroppedTicketsCount(itemId: Int): Int {
        return _droppedTickets.stream().filter { t -> t.itemId == itemId }.count().toInt()
    }

    fun isTooCloseFromDroppedTicket(x: Int, y: Int, z: Int): Boolean {
        for (item in _droppedTickets) {
            val dx = (x - item.x).toDouble()
            val dy = (y - item.y).toDouble()
            val dz = (z - item.z).toDouble()

            if (dx * dx + dy * dy + dz * dz < 25 * 25)
                return true
        }
        return false
    }

    /**
     * That method is used to spawn NPCs, being neutral guards or player-based mercenaries.
     *
     *  * If castle got an owner, it spawns mercenaries following tickets. Otherwise it uses SpawnManager territory.
     *  * It feeds the nearest Control Tower with the spawn. If tower is broken, associated spawns are removed.
     *
     */
    fun spawnSiegeGuardsOrMercenaries() {
        if (ownerId > 0) {
            for (item in _droppedTickets) {
                // Retrieve MercenaryTicket information.
                val ticket = getTicket(item.itemId) ?: continue

                // Generate templates, feed them with ticket information.
                val template = NpcData.getTemplate(ticket.npcId) ?: continue

                try {
                    val spawn = L2Spawn(template)
                    spawn.loc = item.position
                    spawn.setRespawnState(false)

                    _siegeGuards.add(spawn.doSpawn(false)!!)
                } catch (e: Exception) {
                    LOGGER.error("Couldn't spawn npc ticket {}. ", e, ticket.npcId)
                }

                // Delete the ticket item.
                item.decayMe()
            }

            _droppedTickets.clear()
        } else {
            // TODO Territory based spawn
        }
    }

    /**
     * Despawn neutral guards or player-based mercenaries.
     */
    fun despawnSiegeGuardsOrMercenaries() {
        if (ownerId > 0) {
            for (npc in _siegeGuards)
                npc.doDie(npc)

            _siegeGuards.clear()
        } else {
            // TODO territory based despawn
        }
    }

    fun loadTrapUpgrade() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_TRAPS).use { ps ->
                    ps.setInt(1, castleId)

                    ps.executeQuery().use { rs ->
                        while (rs.next())
                            flameTowers[rs.getInt("towerIndex")].upgradeLevel = rs.getInt("level")
                    }
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't load traps.", e)
        }

    }

    fun setRelatedNpcIds(idsToSplit: String) {
        for (splittedId in idsToSplit.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            _relatedNpcIds.add(Integer.parseInt(splittedId))
    }

    fun setArtifacts(idsToSplit: String) {
        for (idToSplit in idsToSplit.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            _artifacts.add(Integer.parseInt(idToSplit))
    }

    fun isGoodArtifact(`object`: WorldObject): Boolean {
        return `object` is HolyThing && _artifacts.contains(`object`.npcId)
    }

    /**
     * @param towerIndex : The index to check on.
     * @return the trap upgrade level for a dedicated tower index.
     */
    fun getTrapUpgradeLevel(towerIndex: Int): Int {
        val spawn = flameTowers.getOrNull(towerIndex)
        return spawn?.upgradeLevel ?: 0
    }

    /**
     * Save properties of a Flame Tower.
     * @param towerIndex : The tower to affect.
     * @param level : The new level of update.
     * @param save : Should it be saved on database or not.
     */
    fun setTrapUpgrade(towerIndex: Int, level: Int, save: Boolean) {
        if (save) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_TRAP).use { ps ->
                        ps.setInt(1, castleId)
                        ps.setInt(2, towerIndex)
                        ps.setInt(3, level)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't replace trap upgrade.", e)
            }

        }

        val spawn = flameTowers.getOrNull(towerIndex)
        if (spawn != null)
            spawn.upgradeLevel = level
    }

    /**
     * Delete all traps informations for a single castle.
     */
    fun removeTrapUpgrade() {
        for (ts in flameTowers)
            ts.upgradeLevel = 0

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_TRAP).use { ps ->
                    ps.setInt(1, castleId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete trap upgrade.", e)
        }

    }

    fun checkItemsForMember(member: ClanMember) {
        val player = member.playerInstance
        player?.checkItemRestriction() ?: try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_ITEMS_LOC).use { ps ->
                    ps.setInt(1, circletId)
                    ps.setInt(2, member.objectId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update items for member.", e)
        }
    }

    fun checkItemsForClan(clan: Clan) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_ITEMS_LOC).use { ps ->
                    ps.setInt(1, circletId)

                    for (member in clan.members) {
                        val player = member.playerInstance
                        if (player != null)
                            player.checkItemRestriction()
                        else {
                            ps.setInt(2, member.objectId)
                            ps.addBatch()
                        }
                    }
                    ps.executeBatch()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't update items for clan.", e)
        }

    }

    companion object {
        protected val LOGGER = CLogger(Castle::class.java.name)

        private val UPDATE_TREASURY = "UPDATE castle SET treasury = ? WHERE id = ?"
        private val UPDATE_CERTIFICATES = "UPDATE castle SET certificates=? WHERE id=?"

        private val UPDATE_DOORS = "REPLACE INTO castle_doorupgrade (doorId, hp, castleId) VALUES (?,?,?)"
        private val LOAD_DOORS = "SELECT * FROM castle_doorupgrade WHERE castleId=?"
        private val DELETE_DOOR = "DELETE FROM castle_doorupgrade WHERE castleId=?"

        private val DELETE_OWNER = "UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?"
        private val UPDATE_OWNER = "UPDATE clan_data SET hasCastle=? WHERE clan_id=?"

        private val LOAD_TRAPS = "SELECT * FROM castle_trapupgrade WHERE castleId=?"
        private val UPDATE_TRAP = "REPLACE INTO castle_trapupgrade (castleId, towerIndex, level) values (?,?,?)"
        private val DELETE_TRAP = "DELETE FROM castle_trapupgrade WHERE castleId=?"

        private val UPDATE_ITEMS_LOC =
            "UPDATE items SET loc='INVENTORY' WHERE item_id IN (?, 6841) AND owner_id=? AND loc='PAPERDOLL'"
    }
}