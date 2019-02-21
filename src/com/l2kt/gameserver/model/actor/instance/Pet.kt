package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.handler.ItemHandler
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.PetDataEntry
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.stat.PetStat
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.PetTemplate
import com.l2kt.gameserver.model.group.Party.LootRule
import com.l2kt.gameserver.model.holder.Timestamp
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.item.type.ArmorType
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.model.itemcontainer.PetInventory
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.taskmanager.DecayTaskManager
import com.l2kt.gameserver.taskmanager.ItemsOnGroundTaskManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

/**
 * A pet is a instance extending [Summon], linked to a [Player]. A pet is different than a Servitor in multiple ways:
 *
 *  * It got its own inventory
 *  * It can earn xp and levels
 *  * Their lifetime isn't limited (but they got a food gauge)
 *
 * It can be mountable, like Wyverns or Striders. A children class of Pet, [BabyPet] can also buff their owner. Finally a last type of pet is the Sin Eater, a creature used to remove PK kills.
 */
open class Pet(objectId: Int, template: NpcTemplate, owner: Player, control: ItemInstance) :
    Summon(objectId, template, owner) {

    private val _reuseTimeStamps = ConcurrentHashMap<Int, Timestamp>()

    private val _inventory: PetInventory
    private val _controlItemId: Int
    private val _isMountable: Boolean

    var currentFed: Int = 0
        set(num) {
            field = Math.min(num, petData!!.maxMeal)
        }
    private var _curWeightPenalty = 0

    private var _expBeforeDeath: Long = 0

    private var _feedTask: Future<*>? = null

    var petData: PetDataEntry? = null
        private set

    override// Name isn't setted yet.
    var name: String
        get() = super.name
        set(name) {
            val controlItem = controlItem
            if (controlItem!!.customType2 == (if (name == null) 1 else 0)) {
                controlItem.customType2 = if (name != null) 1 else 0
                controlItem.updateDatabase()

                val iu = InventoryUpdate()
                iu.addModifiedItem(controlItem)
                owner.sendPacket(iu)
            }
            super.name = name
        }

    val reuseTimeStamps: Collection<Timestamp>
        get() = _reuseTimeStamps.values

    val reuseTimeStamp: Map<Int, Timestamp>
        get() = _reuseTimeStamps

    val controlItem: ItemInstance?
        get() = owner.inventory!!.getItemByObjectId(_controlItemId)

    val currentLoad: Int
        get() = _inventory.totalWeight

    val inventoryLimit: Int
        get() = Config.INVENTORY_MAXIMUM_PET

    init {

        position[owner.x + 50, owner.y + 100] = owner.z

        _inventory = PetInventory(this)
        _controlItemId = control.objectId
        _isMountable =
            template.npcId == 12526 || template.npcId == 12527 || template.npcId == 12528 || template.npcId == 12621
    }

    override fun initCharStat() {
        stat = PetStat(this)
    }

    override fun getStat(): PetStat {
        return super.getStat() as PetStat
    }

    override fun getTemplate(): PetTemplate {
        return super.getTemplate() as PetTemplate
    }

    override fun getInventory(): PetInventory? {
        return _inventory
    }

    override fun getControlItemId(): Int {
        return _controlItemId
    }

    override fun isMountable(): Boolean {
        return _isMountable
    }

    override fun getSummonType(): Int {
        return 2
    }

    override fun onAction(player: Player) {
        // Refresh the Player owner reference if objectId is matching, but object isn't.
        if (player.objectId == owner.objectId && player != owner)
            owner = player

        super.onAction(player)
    }

    override fun getActiveWeaponInstance(): ItemInstance? {
        return _inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND)
    }

    override fun getActiveWeaponItem(): Weapon? {
        val weapon = activeWeaponInstance ?: return null

        return weapon.item as Weapon
    }

    override fun destroyItem(
        process: String,
        objectId: Int,
        count: Int,
        reference: WorldObject,
        sendMessage: Boolean
    ): Boolean {
        val item = _inventory.destroyItem(process, objectId, count, owner, reference)
        if (item == null) {
            if (sendMessage)
                owner.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        // Send Pet inventory update packet
        val petIU = PetInventoryUpdate()
        petIU.addItem(item)
        owner.sendPacket(petIU)

        if (sendMessage) {
            if (count > 1)
                owner.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.itemId).addItemNumber(
                        count
                    )
                )
            else
                owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.itemId))
        }
        return true
    }

    override fun destroyItemByItemId(
        process: String,
        itemId: Int,
        count: Int,
        reference: WorldObject,
        sendMessage: Boolean
    ): Boolean {
        val item = _inventory.destroyItemByItemId(process, itemId, count, owner, reference)
        if (item == null) {
            if (sendMessage)
                owner.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        // Send Pet inventory update packet
        val petIU = PetInventoryUpdate()
        petIU.addItem(item)
        owner.sendPacket(petIU)

        if (sendMessage) {
            if (count > 1)
                owner.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.itemId).addItemNumber(
                        count
                    )
                )
            else
                owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.itemId))
        }
        return true
    }

    override fun doPickupItem(`object`: WorldObject) {
        if (isDead)
            return

        ai.setIntention(CtrlIntention.IDLE)

        // The object must be an item.
        if (`object` !is ItemInstance)
            return

        broadcastPacket(StopMove(objectId, x, y, z, heading))

// Can't pickup cursed weapons.
        if (CursedWeaponManager.isCursed(`object`.itemId)) {
            owner.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(`object`.itemId))
            return
        }

        // Can't pickup shots and arrows.
        if (`object`.item.itemType === EtcItemType.ARROW || `object`.item.itemType === EtcItemType.SHOT) {
            owner.sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS)
            return
        }

        synchronized(`object`) {
            if (!`object`.isVisible)
                return

            if (!_inventory.validateCapacity(`object`)) {
                owner.sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS)
                return
            }

            if (!_inventory.validateWeight(`object`, `object`.count)) {
                owner.sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED)
                return
            }

            if (`object`.ownerId != 0 && !owner.isLooterOrInLooterParty(`object`.ownerId)) {
                if (`object`.itemId == 57)
                    owner.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(
                            `object`.count
                        )
                    )
                else if (`object`.count > 1)
                    owner.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(
                            `object`.itemId
                        ).addNumber(`object`.count)
                    )
                else
                    owner.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(
                            `object`.itemId
                        )
                    )

                return
            }

            if (`object`.hasDropProtection())
                `object`.removeDropProtection()

            // If owner is in party and it isnt finders keepers, distribute the item instead of stealing it -.-
            val party = owner.party
            if (party != null && party.lootRule !== LootRule.ITEM_LOOTER)
                party.distributeItem(owner, `object`)
            else
                `object`.pickupMe(this)

            // Item must be removed from ItemsOnGroundManager if it is active.
            ItemsOnGroundTaskManager.remove(`object`)
        }

        // Auto use herbs - pick up
        if (`object`.itemType === EtcItemType.HERB) {
            val handler = ItemHandler.getHandler(`object`.etcItem)
            handler?.useItem(this, `object`, false)

            `object`.destroyMe("Consume", owner, null)
            broadcastStatusUpdate()
        } else {
            // if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
            if (`object`.itemType is ArmorType || `object`.itemType is WeaponType) {
                val msg: SystemMessage
                if (`object`.enchantLevel > 0)
                    msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2_S3)
                        .addCharName(owner).addNumber(`object`.enchantLevel).addItemName(`object`.itemId)
                else
                    msg =
                        SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2).addCharName(owner)
                            .addItemName(`object`.itemId)

                owner.broadcastPacket(msg, 1400)
            }

            val sm2: SystemMessage
            if (`object`.itemId == 57)
                sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addItemNumber(`object`.count)
            else if (`object`.enchantLevel > 0)
                sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(`object`.enchantLevel)
                    .addItemName(`object`.itemId)
            else if (`object`.count > 1)
                sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addItemName(`object`.itemId)
                    .addItemNumber(`object`.count)
            else
                sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addItemName(`object`.itemId)

            owner.sendPacket(sm2)
            inventory!!.addItem("Pickup", `object`, owner, this)
            owner.sendPacket(PetItemList(this))
        }

        if (followStatus)
            followOwner()
    }

    override fun deleteMe(owner: Player) {
        inventory!!.deleteMe()
        super.deleteMe(owner)
        destroyControlItem(owner) // this should also delete the pet from the db
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        stopFeed()
        owner.sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_20_MINUTES)
        DecayTaskManager.add(this, 1200)

        // Dont decrease exp if killed in duel or arena
        val owner = owner
        if (owner != null && !owner.isInDuel && (!isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE)))
            deathPenalty()

        return true
    }

    override fun doRevive() {
        owner.removeReviving()

        super.doRevive()

        // stopDecay
        DecayTaskManager.cancel(this)
        startFeed()

        if (!checkHungryState())
            setRunning()

        ai.setIntention(CtrlIntention.ACTIVE, null)
    }

    override fun doRevive(revivePower: Double) {
        // Restore the pet's lost experience depending on the % return of the skill used
        restoreExp(revivePower)
        doRevive()
    }

    override fun getWeapon(): Int {
        val weapon = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_RHAND)
        return weapon?.itemId ?: 0

    }

    override fun getArmor(): Int {
        val weapon = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
        return weapon?.itemId ?: 0

    }

    override fun store() {
        if (_controlItemId == 0)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(STORE_PET).use { ps ->
                    ps.setString(1, name)
                    ps.setInt(2, stat.level.toInt())
                    ps.setDouble(3, status.currentHp)
                    ps.setDouble(4, status.currentMp)
                    ps.setLong(5, stat.exp)
                    ps.setInt(6, stat.sp)
                    ps.setInt(7, currentFed)
                    ps.setInt(8, _controlItemId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store pet data for {}.", e, objectId)
        }

        val itemInst = controlItem
        if (itemInst != null && itemInst.enchantLevel != stat.level.toInt()) {
            itemInst.enchantLevel = stat.level.toInt()
            itemInst.updateDatabase()
        }
    }

    @Synchronized
    override fun unSummon(owner: Player) {
        // First, stop feed task.
        stopFeed()

        // Then drop inventory.
        if (!isDead && inventory != null)
            inventory!!.deleteMe()

        // Finally drop pet itself.
        super.unSummon(owner)

        // Drop pet from world's pet list.
        if (!isDead)
            World.removePet(owner.objectId)
    }

    override fun addExpAndSp(addToExp: Long, addToSp: Int) {
        stat.addExpAndSp(
            Math.round(addToExp * if (npcId == 12564) Config.SINEATER_XP_RATE else Config.PET_XP_RATE),
            addToSp
        )
    }

    override fun getExpForThisLevel(): Long {
        return stat.getExpForLevel(level)
    }

    override fun getExpForNextLevel(): Long {
        return stat.getExpForLevel(level + 1)
    }

    override fun getLevel(): Int {
        return stat.level.toInt()
    }

    override fun getSkillLevel(skillId: Int): Int {
        // Unknown skill. Return 0.
        return if (getSkill(skillId) == null) 0 else Math.max(
            1,
            Math.min((level - 8) / 6, SkillTable.getMaxLevel(skillId))
        )

        // Max level for pet is 80, max level for pet skills is 12 => ((80 - 8) / 6) = 12.
    }

    override fun getMaxLoad(): Int {
        return PetTemplate.MAX_LOAD
    }

    override fun getSoulShotsPerHit(): Int {
        return petData!!.ssCount
    }

    override fun getSpiritShotsPerHit(): Int {
        return petData!!.spsCount
    }

    override fun updateAndBroadcastStatus(`val`: Int) {
        refreshOverloaded()
        super.updateAndBroadcastStatus(`val`)
    }

    override fun addTimeStamp(skill: L2Skill, reuse: Long) {
        _reuseTimeStamps[skill.reuseHashCode] = Timestamp(skill, reuse)
    }

    fun setPetData(level: Int) {
        petData = template.getPetDataEntry(level)
    }

    /**
     * Transfers item to another inventory
     * @param process : String Identifier of process triggering this action
     * @param objectId : ObjectId of the item to be transfered
     * @param count : int Quantity of items to be transfered
     * @param target : The Inventory to target
     * @param actor : Player Player requesting the item transfer
     * @param reference : WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    fun transferItem(
        process: String,
        objectId: Int,
        count: Int,
        target: Inventory,
        actor: Player,
        reference: WorldObject
    ): ItemInstance? {
        val oldItem = checkItemManipulation(objectId, count) ?: return null

        val wasWorn = oldItem.isPetItem && oldItem.isEquipped

        val newItem = inventory!!.transferItem(process, objectId, count, target, actor, reference) ?: return null

        // Send pet inventory update packet
        val petIU = PetInventoryUpdate()
        if (oldItem.count > 0 && oldItem != newItem)
            petIU.addModifiedItem(oldItem)
        else
            petIU.addRemovedItem(oldItem)
        sendPacket(petIU)

        // Send player inventory update packet
        val playerIU = InventoryUpdate()
        if (newItem.count > count)
            playerIU.addModifiedItem(newItem)
        else
            playerIU.addNewItem(newItem)
        sendPacket(playerIU)

        // Update player current load aswell
        val playerSU = StatusUpdate(owner)
        playerSU.addAttribute(StatusUpdate.CUR_LOAD, owner.currentLoad)
        sendPacket(playerSU)

        if (wasWorn)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(newItem))

        return newItem
    }

    fun checkItemManipulation(objectId: Int, count: Int): ItemInstance? {
        val item = inventory!!.getItemByObjectId(objectId) ?: return null

        if (count < 1 || count > 1 && !item.isStackable)
            return null

        return if (count > item.count) null else item

    }

    /**
     * Remove the [Pet] reference from [World], then the control item from the [Player] owner inventory. Finally, delete the pet from database.
     * @param owner : The owner from whose inventory we should delete the item.
     */
    fun destroyControlItem(owner: Player) {
        // Remove the pet instance from world.
        World.removePet(owner.objectId)

        // Delete the item from owner inventory.
        owner.destroyItem("PetDestroy", _controlItemId, 1, getOwner(), false)

        // Delete the pet from the database.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_PET).use { ps ->
                    ps.setInt(1, _controlItemId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't delete pet data for {}.", e, objectId)
        }

    }

    @Synchronized
    fun stopFeed() {
        if (_feedTask != null) {
            _feedTask!!.cancel(false)
            _feedTask = null
        }
    }

    @Synchronized
    fun startFeed() {
        // stop feeding task if its active
        stopFeed()

        if (!isDead && owner.pet === this)
            _feedTask = ThreadPool.scheduleAtFixedRate(FeedTask(), 10000, 10000)
    }

    /**
     * Restore the specified % of experience this [Pet] has lost.
     * @param restorePercent : The percent of experience to restore.
     */
    fun restoreExp(restorePercent: Double) {
        if (_expBeforeDeath > 0) {
            stat.addExp(Math.round((_expBeforeDeath - stat.exp) * restorePercent / 100))

            _expBeforeDeath = 0
        }
    }

    private fun deathPenalty() {
        val lvl = stat.level.toInt()
        val percentLost = -0.07 * lvl + 6.5

        // Calculate the Experience loss
        val lostExp = Math.round((stat.getExpForLevel(lvl + 1) - stat.getExpForLevel(lvl)) * percentLost / 100)

        // Get the Experience before applying penalty
        _expBeforeDeath = stat.exp

        // Set the new Experience value of the L2PetInstance
        stat.addExp(-lostExp)
    }

    fun refreshOverloaded() // TODO find a way to apply effect without adding skill. For now it's desactivated.
    {
        val maxLoad = maxLoad
        if (maxLoad > 0) {
            val weightproc = currentLoad * 1000 / maxLoad
            val newWeightPenalty: Int

            if (weightproc < 500)
                newWeightPenalty = 0
            else if (weightproc < 666)
                newWeightPenalty = 1
            else if (weightproc < 800)
                newWeightPenalty = 2
            else if (weightproc < 1000)
                newWeightPenalty = 3
            else
                newWeightPenalty = 4

            if (_curWeightPenalty != newWeightPenalty) {
                _curWeightPenalty = newWeightPenalty
                if (newWeightPenalty > 0) {
                    // addSkill(SkillTable.INSTANCE.getInfo(4270, newWeightPenalty), false);
                    setIsOverloaded(currentLoad >= maxLoad)
                } else {
                    // removeSkill(4270, false);
                    setIsOverloaded(false)
                }
            }
        }
    }

    /**
     * @return true if the auto feed limit is reached, false otherwise or if there is no need to feed.
     */
    fun checkAutoFeedState(): Boolean {
        return currentFed < petData!!.maxMeal * template.autoFeedLimit
    }

    /**
     * @return true if the hungry limit is reached, false otherwise or if there is no need to feed.
     */
    fun checkHungryState(): Boolean {
        return currentFed < petData!!.maxMeal * template.hungryLimit
    }

    /**
     * @return true if the unsummon limit is reached, false otherwise or if there is no need to feed.
     */
    fun checkUnsummonState(): Boolean {
        return currentFed < petData!!.maxMeal * template.unsummonLimit
    }

    fun canWear(item: Item): Boolean {
        val npcId = template.npcId

        if (npcId > 12310 && npcId < 12314 && item.bodyPart == Item.SLOT_HATCHLING)
            return true

        if (npcId == 12077 && item.bodyPart == Item.SLOT_WOLF)
            return true

        if (npcId > 12525 && npcId < 12529 && item.bodyPart == Item.SLOT_STRIDER)
            return true

        return if (npcId > 12779 && npcId < 12783 && item.bodyPart == Item.SLOT_BABYPET) true else false

    }

    /**
     * Manage [Pet] feeding task.
     *
     *  * Feed or kill the pet depending on hunger level.
     *  * If pet has food in inventory and feed level drops below 55% then consume food from inventory.
     *  * Send a broadcastStatusUpdate packet for this pet.
     *
     */
    protected inner class FeedTask : Runnable {

        private val feedConsume: Int
            get() = if (isAttackingNow) petData!!.mealInBattle else petData!!.mealInNormal

        override fun run() {
            if (owner == null || owner.pet == null || owner.pet!!.objectId != objectId) {
                stopFeed()
                return
            }

            currentFed = if (currentFed > feedConsume) currentFed - feedConsume else 0

            var food = inventory!!.getItemByItemId(template.food1)
            if (food == null)
                food = inventory!!.getItemByItemId(template.food2)

            if (food != null && checkAutoFeedState()) {
                val handler = ItemHandler.getHandler(food.etcItem)
                if (handler != null) {
                    owner.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(
                            food
                        )
                    )
                    handler.useItem(this@Pet, food, false)
                }
            } else if (currentFed == 0) {
                owner.sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY)
                if (Rnd[100] < 30) {
                    stopFeed()
                    owner.sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT)
                    deleteMe(owner)
                    return
                }
            } else if (currentFed < 0.10 * petData!!.maxMeal) {
                owner.sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL)
                if (Rnd[100] < 3) {
                    stopFeed()
                    owner.sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT)
                    deleteMe(owner)
                    return
                }
            }

            if (checkHungryState())
                setWalking()
            else
                setRunning()

            broadcastStatusUpdate()
        }
    }

    companion object {
        private const val LOAD_PET =
            "SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?"
        private const val STORE_PET =
            "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),level=VALUES(level),curHp=VALUES(curHp),curMp=VALUES(curMp),exp=VALUES(exp),sp=VALUES(sp),fed=VALUES(fed)"
        private const val DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?"

        fun restore(control: ItemInstance, template: NpcTemplate, owner: Player): Pet? {
            try {
                L2DatabaseFactory.connection.use { con ->
                    val pet: Pet
                    if (template.isType("BabyPet"))
                        pet = BabyPet(IdFactory.getInstance().nextId, template, owner, control)
                    else
                        pet = Pet(IdFactory.getInstance().nextId, template, owner, control)

                    val ps = con.prepareStatement(LOAD_PET)
                    ps.setInt(1, control.objectId)

                    val rset = ps.executeQuery()
                    if (!rset.next()) {
                        rset.close()
                        ps.close()

                        pet.stat.level = if (template.npcId == 12564) pet.owner.level.toByte() else template.level
                        pet.stat.exp = pet.expForThisLevel
                        pet.status.currentHp = pet.maxHp.toDouble()
                        pet.status.currentMp = pet.maxMp.toDouble()
                        pet.currentFed = pet.petData!!.maxMeal
                        pet.store()

                        return pet
                    }

                    pet.name = rset.getString("name")

                    pet.stat.level = rset.getByte("level")
                    pet.stat.exp = rset.getLong("exp")
                    pet.stat.sp = rset.getInt("sp")

                    pet.status.currentHp = rset.getDouble("curHp")
                    pet.status.currentMp = rset.getDouble("curMp")
                    if (rset.getDouble("curHp") < 0.5) {
                        pet.setIsDead(true)
                        pet.stopHpMpRegeneration()
                    }

                    pet.currentFed = rset.getInt("fed")

                    rset.close()
                    ps.close()
                    return pet
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't restore pet data for {}.", e, owner.name)
                return null
            }

        }
    }
}