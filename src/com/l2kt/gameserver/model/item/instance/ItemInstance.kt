package com.l2kt.gameserver.model.item.instance

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Augmentation
import com.l2kt.gameserver.model.ShotType
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.EtcItem
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.ItemType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.skills.basefuncs.Func
import com.l2kt.gameserver.taskmanager.ItemsOnGroundTaskManager
import java.sql.ResultSet
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.locks.ReentrantLock
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * This class manages items.
 */
class ItemInstance : WorldObject, Runnable, Comparable<ItemInstance> {

    private var _ownerId: Int = 0
    private var _dropperObjectId = 0

    private var _count: Int = 0
    var initCount: Int = 0

    var time: Long = 0
        private set
    var countDecrease = false

    /**
     * Returns the ID of the item
     * @return int
     */
    val itemId: Int
    /**
     * Returns the characteristics of the item
     * @return L2Item
     */
    val item: Item

    /** Location of the item : Inventory, PaperDoll, WareHouse  */
    private var _loc: ItemLocation? = null

    /** Slot where item is stored  */
    private var _locData: Int = 0

    private var _enchantLevel: Int = 0

    private var _augmentation: L2Augmentation? = null

    /** Shadow item  */
    private var _mana = -1

    /** Custom item types (used loto, race tickets)  */
    var customType1: Int = 0
    var customType2: Int = 0

    var isDestroyProtected: Boolean = false

    /**
     * @return the last change of the item.
     */
    /**
     * Sets the last change of the item
     * @param lastChange : int
     */
    var lastChange = ItemState.MODIFIED

    private var _existsInDb: Boolean = false // if a record exists in DB.
    private var _storedInDb: Boolean = false // if DB data is up-to-date.

    private val _dbLock = ReentrantLock()
    private var _dropProtection: ScheduledFuture<*>? = null

    private var _shotsMask = 0

    /**
     * Returns the ownerID of the item
     * @return int : ownerID of the item
     */
    /**
     * Sets the ownerID of the item
     * @param owner_id : int designating the ID of the owner
     */
    var ownerId: Int
        get() = _ownerId
        set(owner_id) {
            if (owner_id == _ownerId)
                return

            _ownerId = owner_id
            _storedInDb = false
        }

    /**
     * Sets the location of the item
     * @param loc : ItemLocation (enumeration)
     */
    var location: ItemLocation?
        get() = _loc
        set(loc) = setLocation(loc, 0)

    /**
     * Returns the quantity of item
     * @return int
     */
    /**
     * Sets the quantity of the item.<BR></BR>
     * <BR></BR>
     * @param count the new count to set
     */
    var count: Int
        get() = _count
        set(count) {
            if (this.count == count)
                return

            _count = if (count >= -1) count else 0
            _storedInDb = false
        }

    /**
     * Returns if item is equipable
     * @return boolean
     */
    val isEquipable: Boolean
        get() = !(item.bodyPart == 0 || item.itemType === EtcItemType.ARROW || item.itemType === EtcItemType.LURE)

    /**
     * Returns if item is equipped
     * @return boolean
     */
    val isEquipped: Boolean
        get() = _loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP

    /**
     * Returns the slot where the item is stored
     * @return int
     */
    val locationSlot: Int
        get() {
            assert(_loc == ItemLocation.PAPERDOLL || _loc == ItemLocation.PET_EQUIP || _loc == ItemLocation.FREIGHT)
            return _locData
        }

    val isOlyRestrictedItem: Boolean
        get() = item!!.isOlyRestrictedItem

    /**
     * Returns the type of item
     * @return Enum
     */
    val itemType: ItemType?
        get() = item!!.itemType

    /**
     * Returns true if item is an EtcItem
     * @return boolean
     */
    val isEtcItem: Boolean
        get() = item is EtcItem

    /**
     * Returns true if item is a Weapon/Shield
     * @return boolean
     */
    val isWeapon: Boolean
        get() = item is Weapon

    /**
     * Returns true if item is an Armor
     * @return boolean
     */
    val isArmor: Boolean
        get() = item is Armor

    /**
     * Returns the characteristics of the L2EtcItem
     * @return EtcItem
     */
    val etcItem: EtcItem?
        get() = item as? EtcItem

    /**
     * Returns the characteristics of the Weapon
     * @return Weapon
     */
    val weaponItem: Weapon?
        get() = item as? Weapon

    /**
     * Returns the characteristics of the L2Armor
     * @return Armor
     */
    val armorItem: Armor?
        get() = item as? Armor

    /**
     * Returns the quantity of crystals for crystallization
     * @return int
     */
    val crystalCount: Int
        get() = item!!.getCrystalCount(_enchantLevel)

    /**
     * @return the reference price of the item.
     */
    val referencePrice: Int
        get() = item!!.referencePrice

    /**
     * @return the name of the item.
     */
    val itemName: String
        get() = item!!.name

    /**
     * @return if item is stackable.
     */
    val isStackable: Boolean
        get() = item!!.isStackable

    /**
     * @return if item is dropable.
     */
    val isDropable: Boolean
        get() = if (isAugmented) false else item!!.isDropable

    /**
     * @return if item is destroyable.
     */
    val isDestroyable: Boolean
        get() = if (isQuestItem) false else item!!.isDestroyable

    /**
     * @return if item is tradable
     */
    val isTradable: Boolean
        get() = if (isAugmented) false else item!!.isTradable

    /**
     * @return if item is sellable.
     */
    val isSellable: Boolean
        get() = if (isAugmented) false else item!!.isSellable

    /**
     * @return if item is consumable.
     */
    val isConsumable: Boolean
        get() = item!!.isConsumable

    /**
     * @return the level of enchantment of the item.
     */
    /**
     * Sets the level of enchantment of the item
     * @param enchantLevel : number to apply.
     */
    var enchantLevel: Int
        get() = _enchantLevel
        set(enchantLevel) {
            if (_enchantLevel == enchantLevel)
                return

            _enchantLevel = enchantLevel
            _storedInDb = false
        }

    /**
     * @return whether this item is augmented or not ; true if augmented.
     */
    val isAugmented: Boolean
        get() = _augmentation != null

    /**
     * @return true if this item is a shadow item. Shadow items have a limited life-time.
     */
    val isShadowItem: Boolean
        get() = _mana >= 0

    /**
     * @return the remaining mana of this shadow item (left life-time).
     */
    val mana: Int
        get() = _mana / 60

    val isNightLure: Boolean
        get() = itemId >= 8505 && itemId <= 8513 || itemId == 8485

    val isPetItem: Boolean
        get() = item!!.isPetItem

    val isPotion: Boolean
        get() = item!!.isPotion

    val isElixir: Boolean
        get() = item!!.isElixir

    val isHerb: Boolean
        get() = item!!.itemType === EtcItemType.HERB

    val isHeroItem: Boolean
        get() = item!!.isHeroItem

    val isQuestItem: Boolean
        get() = item!!.isQuestItem

    val questEvents: List<Quest>
        get() = item!!.questEvents

    enum class ItemState {
        UNCHANGED,
        ADDED,
        MODIFIED,
        REMOVED
    }

    enum class ItemLocation {
        VOID,
        INVENTORY,
        PAPERDOLL,
        WAREHOUSE,
        CLANWH,
        PET,
        PET_EQUIP,
        LEASE,
        FREIGHT
    }

    /**
     * Constructor of the ItemInstance from the objectId and the itemId.
     * @param objectId : int designating the ID of the object in the world
     * @param itemId : int designating the ID of the item
     */
    constructor(objectId: Int, itemId: Int) : super(objectId) {
        this.itemId = itemId
        this.item = ItemTable.getTemplate(itemId) ?: throw IllegalArgumentException()

        if (this.itemId == 0)
            throw IllegalArgumentException()

        super.name = item.name
        count = 1
        _loc = ItemLocation.VOID
        customType1 = 0
        customType2 = 0
        _mana = item.duration * 60
    }

    /**
     * Constructor of the ItemInstance from the objetId and the description of the item given by the L2Item.
     * @param objectId : int designating the ID of the object in the world
     * @param item : L2Item containing informations of the item
     */
    constructor(objectId: Int, item: Item) : super(objectId) {
        itemId = item.itemId
        this.item = item

        name = this.item!!.name
        count = 1

        _loc = ItemLocation.VOID
        _mana = this.item.duration * 60
    }

    @Synchronized
    override fun run() {
        _ownerId = 0
        _dropProtection = null
    }

    /**
     * Sets the ownerID of the item
     * @param process : String Identifier of process triggering this action
     * @param owner_id : int designating the ID of the owner
     * @param creator : Player Player requesting the item creation
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     */
    fun setOwnerId(process: String, owner_id: Int, creator: Player?, reference: WorldObject?) {
        ownerId = owner_id

        if (Config.LOG_ITEMS) {
            val record = LogRecord(Level.INFO, "CHANGE:$process")
            record.loggerName = "item"
            record.parameters = arrayOf(creator, this, reference)
            ITEM_LOG.log(record)
        }
    }

    /**
     * Sets the location of the item.<BR></BR>
     * <BR></BR>
     * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
     * @param loc : ItemLocation (enumeration)
     * @param loc_data : int designating the slot where the item is stored or the village for freights
     */
    fun setLocation(loc: ItemLocation?, loc_data: Int) {
        if (loc == _loc && loc_data == _locData)
            return

        _loc = loc
        _locData = loc_data
        _storedInDb = false
    }

    /**
     * Sets the quantity of the item.<BR></BR>
     * <BR></BR>
     * <U><I>Remark :</I></U> If loc and loc_data different from database, say datas not up-to-date
     * @param process : String Identifier of process triggering this action
     * @param count : int
     * @param creator : Player Player requesting the item creation
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     */
    fun changeCount(process: String?, count: Int, creator: Player?, reference: WorldObject?) {
        if (count == 0)
            return

        if (count > 0 && this.count > Integer.MAX_VALUE - count)
            this.count = Integer.MAX_VALUE
        else
            this.count = this.count + count

        if (this.count < 0)
            this.count = 0

        _storedInDb = false

        if (Config.LOG_ITEMS && process != null) {
            val record = LogRecord(Level.INFO, "CHANGE:$process")
            record.loggerName = "item"
            record.parameters = arrayOf(creator, this, reference)
            ITEM_LOG.log(record)
        }
    }

    /**
     * @param isPrivateWareHouse : make additionals checks on tradable / shadow items.
     * @return if item can be deposited in warehouse or freight.
     */
    fun isDepositable(isPrivateWareHouse: Boolean): Boolean {
        // equipped, hero and quest items
        if (isEquipped || !item!!.isDepositable)
            return false

        if (!isPrivateWareHouse) {
            // augmented not tradable
            if (!isTradable || isShadowItem)
                return false
        }
        return true
    }

    /**
     * @param player : the player to check.
     * @param allowAdena : if true, count adenas.
     * @param allowNonTradable : if true, count non tradable items.
     * @return if item is available for manipulation.
     */
    fun isAvailable(player: Player, allowAdena: Boolean, allowNonTradable: Boolean): Boolean {
        return (!isEquipped // Not equipped

                && item!!.type2 != Item.TYPE2_QUEST // Not Quest Item

                && (item.type2 != Item.TYPE2_MONEY || item.type1 != Item.TYPE1_SHIELD_ARMOR) // not money, not shield

                && (player.pet == null || objectId != player.pet!!.controlItemId) // Not Control item of currently summoned pet

                && player.activeEnchantItem != this // Not momentarily used enchant scroll

                && (allowAdena || itemId != 57) // Not adena

                && (player.currentSkill.skill == null || player.currentSkill.skill.itemConsumeId != itemId) && (!player.isCastingSimultaneouslyNow || player.lastSimultaneousSkillCast == null || player.lastSimultaneousSkillCast.itemConsumeId != itemId) && (allowNonTradable || isTradable))
    }

    override fun onAction(player: Player) {
        if (player.isFlying) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        // Mercenaries tickets case.
        if (item!!.itemType === EtcItemType.CASTLE_GUARD) {
            if (player.isInParty) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val castle = CastleManager.getCastle(player)
            if (castle == null) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val ticket = castle.getTicket(itemId)
            if (ticket == null) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            if (!player.isCastleLord(castle.castleId)) {
                player.sendPacket(SystemMessageId.THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING)
                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }
        }

        player.ai.setIntention(CtrlIntention.PICK_UP, this)
    }

    override fun onActionShift(player: Player) {
        if (player.isGM) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/admin/iteminfo.htm")
            html.replace("%objid%", objectId)
            html.replace("%itemid%", itemId)
            html.replace("%ownerid%", ownerId)
            html.replace("%loc%", location!!.toString())
            html.replace("%class%", javaClass.simpleName)
            player.sendPacket(html)
        }
        super.onActionShift(player)
    }

    /**
     * @return the augmentation object for this item.
     */
    fun getAugmentation(): L2Augmentation? {
        return _augmentation
    }

    /**
     * Sets a new augmentation.
     * @param augmentation : the augmentation object to apply.
     * @return return true if successfull.
     */
    fun setAugmentation(augmentation: L2Augmentation): Boolean {
        // there shall be no previous augmentation..
        if (_augmentation != null)
            return false

        _augmentation = augmentation
        updateItemAttributes()
        return true
    }

    /**
     * Remove the augmentation.
     */
    fun removeAugmentation() {
        if (_augmentation == null)
            return

        _augmentation = null

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_AUGMENTATION).use { ps ->
                    ps.setInt(1, objectId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't remove augmentation for {}.", e, toString())
        }

    }

    private fun restoreAttributes() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_AUGMENTATION).use { ps ->
                    ps.setInt(1, objectId)

                    ps.executeQuery().use { rs ->
                        if (rs.next())
                            _augmentation =
                                    L2Augmentation(
                                        rs.getInt("attributes"),
                                        rs.getInt("skill_id"),
                                        rs.getInt("skill_level")
                                    )
                    }
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore augmentation for {}.", e, toString())
        }

    }

    private fun updateItemAttributes() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_AUGMENTATION).use { ps ->
                    ps.setInt(1, objectId)

                    if (_augmentation == null) {
                        ps.setInt(2, -1)
                        ps.setInt(3, -1)
                        ps.setInt(4, -1)
                    } else {
                        ps.setInt(2, _augmentation!!.attributes)

                        if (_augmentation!!.skill == null) {
                            ps.setInt(3, 0)
                            ps.setInt(4, 0)
                        } else {
                            ps.setInt(3, _augmentation!!.skill!!.id)
                            ps.setInt(4, _augmentation!!.skill!!.level)
                        }
                    }
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't update attributes for {}.", e, toString())
        }

    }

    /**
     * Sets the mana for this shadow item.
     * @param period
     * @return return remaining mana of this shadow item
     */
    fun decreaseMana(period: Int): Int {
        _storedInDb = false

        _mana -= period
        return _mana
    }

    /**
     * Returns false cause item can't be attacked
     * @return boolean false
     */
    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    /**
     * This function basically returns a set of functions from L2Item/L2Armor/Weapon, but may add additional functions, if this particular item instance is enhanched for a particular player.
     * @param player : Creature designating the player
     * @return Func[]
     */
    fun getStatFuncs(player: Creature): List<Func> {
        return item!!.getStatFuncs(this, player)
    }

    /**
     * Updates database.<BR></BR>
     * <BR></BR>
     * <U><I>Concept : </I></U><BR></BR>
     * <B>IF</B> the item exists in database :
     * <UL>
     * <LI><B>IF</B> the item has no owner, or has no location, or has a null quantity : remove item from database</LI>
     * <LI><B>ELSE</B> : update item in database</LI>
    </UL> *
     * <B> Otherwise</B> :
     * <UL>
     * <LI><B>IF</B> the item hasn't a null quantity, and has a correct location, and has a correct owner : insert item in database</LI>
    </UL> *
     */
    fun updateDatabase() {
        _dbLock.lock()

        try {
            if (_existsInDb) {
                if (_ownerId == 0 || _loc == ItemLocation.VOID || count == 0 && _loc != ItemLocation.LEASE)
                    removeFromDb()
                else
                    updateInDb()
            } else {
                if (_ownerId == 0 || _loc == ItemLocation.VOID || count == 0 && _loc != ItemLocation.LEASE)
                    return

                insertIntoDb()
            }
        } finally {
            _dbLock.unlock()
        }
    }

    /**
     * Init a dropped ItemInstance and add it in the world as a visible object.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T ADD the object to _objects of World </B></FONT><BR></BR>
     * <BR></BR>
     * @param dropper : the character who dropped the item.
     * @param x : X location of the item.
     * @param y : Y location of the item.
     * @param z : Z location of the item.
     */
    fun dropMe(dropper: Creature, x: Int, y: Int, z: Int) {
        ThreadPool.execute(ItemDropTask(this, dropper, x, y, z))
    }

    inner class ItemDropTask(
        private val _itm: ItemInstance,
        private val _dropper: Creature?,
        private var _x: Int,
        private var _y: Int,
        private var _z: Int
    ) : Runnable {

        override fun run() {
            assert(_itm.region == null)

            if (_dropper != null) {
                val dropDest = GeoEngine.canMoveToTargetLoc(_dropper.x, _dropper.y, _dropper.z, _x, _y, _z)
                _x = dropDest.x
                _y = dropDest.y
                _z = dropDest.z
            }

            _itm.setDropperObjectId(_dropper?.objectId ?: 0) // Set the dropper Id for the knownlist packets in sendInfo
            _itm.spawnMe(_x, _y, _z)

            ItemsOnGroundTaskManager.add(_itm, _dropper!!)

            _itm.setDropperObjectId(0) // Set the dropper Id back to 0 so it no longer shows the drop packet
        }
    }

    /**
     * Remove a ItemInstance from the visible world and send server->client GetItem packets.<BR></BR>
     * <BR></BR>
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T REMOVE the object from _objects of World.</B></FONT><BR></BR>
     * <BR></BR>
     * @param player Player that pick up the item
     */
    fun pickupMe(player: Creature) {
        player.broadcastPacket(GetItem(this, player.objectId))

        // Unregister dropped ticket from castle, if that item is on a castle area and is a valid ticket.
        val castle = CastleManager.getCastle(player)
        if (castle != null && castle.getTicket(itemId) != null)
            castle.removeDroppedTicket(this)

        if (!Config.DISABLE_TUTORIAL && (itemId == 57 || itemId == 6353)) {
            val actor = player.actingPlayer
            if (actor != null) {
                val qs = actor.getQuestState("Tutorial")
                qs?.quest?.notifyEvent("CE$itemId", null, actor)
            }
        }

        // Calls directly setRegion(null), we don't have to care about.
        isVisible = false
    }

    /**
     * Update the database with values of the item
     */
    private fun updateInDb() {
        assert(_existsInDb)

        if (_storedInDb)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_ITEM).use { ps ->
                    ps.setInt(1, _ownerId)
                    ps.setInt(2, count)
                    ps.setString(3, _loc!!.name)
                    ps.setInt(4, _locData)
                    ps.setInt(5, enchantLevel)
                    ps.setInt(6, customType1)
                    ps.setInt(7, customType2)
                    ps.setInt(8, _mana)
                    ps.setLong(9, time)
                    ps.setInt(10, objectId)
                    ps.executeUpdate()

                    _existsInDb = true
                    _storedInDb = true
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't update {}. ", e, toString())
        }

    }

    /**
     * Insert the item in database
     */
    private fun insertIntoDb() {
        assert(!_existsInDb && objectId != 0)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(INSERT_ITEM).use { ps ->
                    ps.setInt(1, _ownerId)
                    ps.setInt(2, itemId)
                    ps.setInt(3, count)
                    ps.setString(4, _loc!!.name)
                    ps.setInt(5, _locData)
                    ps.setInt(6, enchantLevel)
                    ps.setInt(7, objectId)
                    ps.setInt(8, customType1)
                    ps.setInt(9, customType2)
                    ps.setInt(10, _mana)
                    ps.setLong(11, time)
                    ps.executeUpdate()

                    _existsInDb = true
                    _storedInDb = true

                    if (_augmentation != null)
                        updateItemAttributes()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't insert {}.", e, toString())
        }

    }

    /**
     * Delete item from database
     */
    private fun removeFromDb() {
        assert(_existsInDb)

        try {
            L2DatabaseFactory.connection.use { con ->
                var ps = con.prepareStatement(DELETE_ITEM)
                ps.setInt(1, objectId)
                ps.executeUpdate()
                ps.close()

                ps = con.prepareStatement(DELETE_AUGMENTATION)
                ps.setInt(1, objectId)
                ps.executeUpdate()
                ps.close()

                _existsInDb = false
                _storedInDb = false
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't delete {}.", e, toString())
        }

    }

    /**
     * @return the item in String format.
     */
    override fun toString(): String {
        return "($objectId) $name"
    }

    @Synchronized
    fun hasDropProtection(): Boolean {
        return _dropProtection != null
    }

    @Synchronized
    fun setDropProtection(ownerId: Int, isRaidParty: Boolean) {
        _ownerId = ownerId
        _dropProtection =
                ThreadPool.schedule(this, if (isRaidParty) RAID_LOOT_PROTECTION_TIME else REGULAR_LOOT_PROTECTION_TIME)
    }

    @Synchronized
    fun removeDropProtection() {
        if (_dropProtection != null) {
            _dropProtection!!.cancel(true)
            _dropProtection = null
        }

        _ownerId = 0
    }

    fun restoreInitCount() {
        if (countDecrease)
            _count = initCount
    }

    fun actualizeTime() {
        time = System.currentTimeMillis()
    }

    override fun decayMe() {
        ItemsOnGroundTaskManager.remove(this)

        super.decayMe()
    }

    /**
     * Destroys this [ItemInstance] from server, and release its objectId.
     * @param process : The identifier of process triggering this action (used by logs).
     * @param actor : The [Player] requesting the item destruction.
     * @param reference : The [WorldObject] referencing current action like NPC selling item or previous item in transformation.
     */
    fun destroyMe(process: String?, actor: Player?, reference: WorldObject?) {
        count = 0
        ownerId = 0
        location = ItemLocation.VOID
        lastChange = ItemState.REMOVED

        World.removeObject(this)
        IdFactory.getInstance().releaseId(objectId)

        if (Config.LOG_ITEMS) {
            val record = LogRecord(Level.INFO, "DELETE:${process ?: "Anonymous"}")
            record.loggerName = "item"
            record.parameters = arrayOf(actor, this, reference)
            ITEM_LOG.log(record)
        }

        // if it's a pet control item, delete the pet as well
        if (itemType === EtcItemType.PET_COLLAR) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(DELETE_PET_ITEM).use { ps ->
                        ps.setInt(1, objectId)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't delete {}.", e, toString())
            }

        }
    }

    fun setDropperObjectId(id: Int) {
        _dropperObjectId = id
    }

    override fun sendInfo(activeChar: Player) {
        if (_dropperObjectId != 0)
            activeChar.sendPacket(DropItem(this, _dropperObjectId))
        else
            activeChar.sendPacket(SpawnItem(this))
    }

    override fun isChargedShot(type: ShotType): Boolean {
        return _shotsMask and type.mask == type.mask
    }

    override fun setChargedShot(type: ShotType, charged: Boolean) {
        if (charged)
            _shotsMask = _shotsMask or type.mask
        else
            _shotsMask = _shotsMask and type.mask.inv()
    }

    fun unChargeAllShots() {
        _shotsMask = 0
    }

    override fun compareTo(item: ItemInstance): Int {
        val time = java.lang.Long.compare(item.time, this.time)
        return if (time != 0) time else Integer.compare(item.objectId, objectId)

    }

    companion object {
        private val ITEM_LOG = Logger.getLogger("item")

        private val DELETE_AUGMENTATION = "DELETE FROM augmentations WHERE item_id = ?"
        private val RESTORE_AUGMENTATION = "SELECT attributes,skill_id,skill_level FROM augmentations WHERE item_id=?"
        private val UPDATE_AUGMENTATION = "REPLACE INTO augmentations VALUES(?,?,?,?)"

        private val UPDATE_ITEM =
            "UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?"
        private val INSERT_ITEM =
            "INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)"
        private val DELETE_ITEM = "DELETE FROM items WHERE object_id=?"

        private val DELETE_PET_ITEM = "DELETE FROM pets WHERE item_obj_id=?"

        private val REGULAR_LOOT_PROTECTION_TIME: Long = 15000
        private val RAID_LOOT_PROTECTION_TIME: Long = 300000

        /**
         * @param ownerId : objectID of the owner.
         * @param rs : the ResultSet of the item.
         * @return a ItemInstance stored in database from its objectID
         */
        fun restoreFromDb(ownerId: Int, rs: ResultSet): ItemInstance? {
            var inst: ItemInstance? = null
            val objectId: Int
            val itemId: Int
            val slot: Int
            val enchant: Int
            val type1: Int
            val type2: Int
            val manaLeft: Int
            val count: Int
            val time: Long
            val loc: ItemLocation

            try {
                objectId = rs.getInt(1)
                itemId = rs.getInt("item_id")
                count = rs.getInt("count")
                loc = ItemLocation.valueOf(rs.getString("loc"))
                slot = rs.getInt("loc_data")
                enchant = rs.getInt("enchant_level")
                type1 = rs.getInt("custom_type1")
                type2 = rs.getInt("custom_type2")
                manaLeft = rs.getInt("mana_left")
                time = rs.getLong("time")
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't restore an item owned by {}.", e, ownerId)
                return null
            }

            val item = ItemTable.getTemplate(itemId) ?: return null

            inst = ItemInstance(objectId, item)
            inst._ownerId = ownerId
            inst.count = count
            inst._enchantLevel = enchant
            inst.customType1 = type1
            inst.customType2 = type2
            inst._loc = loc
            inst._locData = slot
            inst._existsInDb = true
            inst._storedInDb = true

            // Setup life time for shadow weapons
            inst._mana = manaLeft
            inst.time = time

            // load augmentation
            if (inst.isEquipable)
                inst.restoreAttributes()

            return inst
        }

        /**
         * Create an [ItemInstance] corresponding to the itemId and count, add it to the server and logs the activity.
         * @param itemId : The itemId of the item to be created.
         * @param count : The quantity of items to be created for stackable items.
         * @param actor : The [Player] requesting the item creation.
         * @param reference : The [WorldObject] referencing current action like NPC selling item or previous item in transformation.
         * @return a new ItemInstance corresponding to the itemId and count.
         */
        fun create(itemId: Int, count: Int, actor: Player?, reference: WorldObject?): ItemInstance {
            // Create and Init the ItemInstance corresponding to the Item Identifier
            val item = ItemInstance(IdFactory.getInstance().nextId, itemId)

            // Add the ItemInstance object to _objects of World.
            World.addObject(item)

            // Set Item parameters
            if (item.isStackable && count > 1)
                item.count = count

            if (Config.LOG_ITEMS) {
                val record = LogRecord(Level.INFO, "CREATE")
                record.loggerName = "item"
                record.parameters = arrayOf(actor, item, reference)
                ITEM_LOG.log(record)
            }

            return item
        }
    }
}