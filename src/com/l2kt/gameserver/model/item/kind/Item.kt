package com.l2kt.gameserver.model.item.kind

import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.type.*
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.basefuncs.Func
import com.l2kt.gameserver.skills.basefuncs.FuncTemplate
import com.l2kt.gameserver.skills.conditions.Condition
import com.l2kt.gameserver.templates.StatsSet
import java.util.*
import java.util.logging.Logger

/**
 * This class contains all informations concerning the item (weapon, armor, etc). Mother class of :
 *
 *  * L2Armor
 *  * L2EtcItem
 *  * Weapon
 *
 */
abstract class Item
/**
 * Constructor of the L2Item that fill class variables.
 * @param set : StatsSet corresponding to a set of couples (key,value) for description of the item
 */
protected constructor(set: StatsSet) {

    protected val _log = Logger.getLogger(Item::class.java.name)

    /**
     * @return int the ID of the item
     */
    val itemId: Int
    /**
     * @return String the name of the item
     */
    val name: String
    /**
     * @return int the type 1 of the item
     */
    var type1: Int = 0
        protected set // needed for item list (inventory)
    /**
     * @return int the type 2 of the item
     */
    var type2: Int = 0
        protected set // different lists for armor, weapon, etc
    /**
     * @return int the weight of the item
     */
    val weight: Int
    /**
     * @return boolean if the item is stackable
     */
    val isStackable: Boolean
    /**
     * @return int the type of material of the item
     */
    val materialType: MaterialType?
    /**
     * @return CrystalType the type of crystal if item is crystallizable
     */
    val crystalType: CrystalType?
    /**
     * @return int the duration of the item
     */
    val duration: Int
    /**
     * @return int the part of the body used with the item.
     */
    val bodyPart: Int
    /**
     * @return int the price of reference of the item
     */
    val referencePrice: Int
    /**
     * @return int the quantity of crystals for crystallization
     */
    val crystalCount: Int

    /**
     * Returns if the item can be sold
     * @return boolean
     */
    val isSellable: Boolean
    /**
     * Returns if the item can dropped
     * @return boolean
     */
    val isDropable: Boolean
    /**
     * Returns if the item can destroy
     * @return boolean
     */
    val isDestroyable: Boolean
    /**
     * Returns if the item can add to trade
     * @return boolean
     */
    val isTradable: Boolean
    /**
     * Returns if the item can be put into warehouse
     * @return boolean
     */
    val isDepositable: Boolean

    val isHeroItem: Boolean
    val isOlyRestrictedItem: Boolean

    val defaultAction: ActionType?

    protected var _funcTemplates: MutableList<FuncTemplate> = mutableListOf()

    protected var _preConditions: MutableList<Condition> = mutableListOf()
    /**
     * Method to retrieve skills linked to this item
     * @return Skills linked to this item as SkillHolder[]
     */
    var skills: MutableList<IntIntHolder> = mutableListOf()

    private var _questEvents: MutableList<Quest> = mutableListOf()

    /**
     * @return Enum the itemType.
     */
    abstract val itemType: ItemType?

    abstract val itemMask: Int

    /**
     * @return boolean if the item is crystallizable
     */
    val isCrystallizable: Boolean
        get() = crystalType !== CrystalType.NONE && crystalCount > 0

    /**
     * @return int the type of crystal if item is crystallizable
     */
    val crystalItemId: Int
        get() = crystalType!!.crystalId

    /**
     * @return boolean if the item is consumable
     */
    open val isConsumable: Boolean
        get() = false

    val isEquipable: Boolean
        get() = bodyPart != 0 && itemType !is EtcItemType

    val isConditionAttached: Boolean
        get() = !_preConditions.isEmpty()

    val isQuestItem: Boolean
        get() = itemType === EtcItemType.QUEST

    val isPetItem: Boolean
        get() = itemType === ArmorType.PET || itemType === WeaponType.PET

    val isPotion: Boolean
        get() = itemType === EtcItemType.POTION

    val isElixir: Boolean
        get() = itemType === EtcItemType.ELIXIR

    val questEvents: List<Quest>
        get() = _questEvents

    init {
        itemId = set.getInteger("item_id")
        name = set.getString("name")
        weight = set.getInteger("weight", 0)
        materialType = set.getEnum("material", MaterialType::class.java, MaterialType.STEEL)
        duration = set.getInteger("duration", -1)
        bodyPart = ItemTable.slots.get(set.getString("bodypart", "none"))!!
        referencePrice = set.getInteger("price", 0)
        crystalType = set.getEnum("crystal_type", CrystalType::class.java, CrystalType.NONE)
        crystalCount = set.getInteger("crystal_count", 0)

        isStackable = set.getBool("is_stackable", false)
        isSellable = set.getBool("is_sellable", true)
        isDropable = set.getBool("is_dropable", true)
        isDestroyable = set.getBool("is_destroyable", true)
        isTradable = set.getBool("is_tradable", true)
        isDepositable = set.getBool("is_depositable", true)

        isHeroItem = itemId >= 6611 && itemId <= 6621 || itemId == 6842
        isOlyRestrictedItem = set.getBool("is_oly_restricted", false)

        defaultAction = set.getEnum("default_action", ActionType::class.java, ActionType.none)

        val skills = set.getString("item_skill", null)
        if (skills != null) {
            val skillsSplit = skills.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
            this.skills = mutableListOf()
            var used = 0

            for (element in skillsSplit) {
                try {
                    val skillSplit = element.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
                    val id = Integer.parseInt(skillSplit[0])
                    val level = Integer.parseInt(skillSplit[1])

                    if (id == 0) {
                        _log.info("Ignoring item_skill(" + element + ") for item " + toString() + ". Skill id is 0.")
                        continue
                    }

                    if (level == 0) {
                        _log.info("Ignoring item_skill(" + element + ") for item " + toString() + ". Skill level is 0.")
                        continue
                    }

                    this.skills.add(used, IntIntHolder(id, level))
                    ++used
                } catch (e: Exception) {
                    _log.warning("Failed to parse item_skill(" + element + ") for item " + toString() + ". The used format is wrong.")
                }

            }

            // this is only loading? just don't leave a null or use a collection?
            if (used != this.skills.size) {
                val skillHolder = arrayOfNulls<IntIntHolder>(used)
                System.arraycopy(this.skills, 0, skillHolder, 0, used)
                this.skills = skillHolder.filterNotNull().toMutableList()
            }
        }
    }

    /**
     * @param enchantLevel
     * @return int the quantity of crystals for crystallization on specific enchant level
     */
    fun getCrystalCount(enchantLevel: Int): Int {
        return if (enchantLevel > 3) {
            when (type2) {
                TYPE2_SHIELD_ARMOR, TYPE2_ACCESSORY -> crystalCount + crystalType!!.crystalEnchantBonusArmor * (3 * enchantLevel - 6)

                TYPE2_WEAPON -> crystalCount + crystalType!!.crystalEnchantBonusWeapon * (2 * enchantLevel - 3)

                else -> crystalCount
            }
        } else if (enchantLevel > 0) {
            when (type2) {
                TYPE2_SHIELD_ARMOR, TYPE2_ACCESSORY -> crystalCount + crystalType!!.crystalEnchantBonusArmor * enchantLevel
                TYPE2_WEAPON -> crystalCount + crystalType!!.crystalEnchantBonusWeapon * enchantLevel
                else -> crystalCount
            }
        } else
            crystalCount
    }

    /**
     * Get the functions used by this item.
     * @param item : ItemInstance pointing out the item
     * @param player : Creature pointing out the player
     * @return the list of functions
     */
    fun getStatFuncs(item: ItemInstance, player: Creature): List<Func> {
        if (_funcTemplates.isEmpty())
            return emptyList()

        val funcs = ArrayList<Func>(_funcTemplates.size)

        val env = Env()
        env.character = player
        env.target = player
        env.item = item

        for (t in _funcTemplates!!) {
            val f = t.getFunc(env, item)
            if (f != null)
                funcs.add(f)
        }
        return funcs
    }

    /**
     * Add the FuncTemplate f to the list of functions used with the item
     * @param f : FuncTemplate to add
     */
    fun attach(f: FuncTemplate) {
        if (_funcTemplates == null)
            _funcTemplates = ArrayList(1)

        _funcTemplates!!.add(f)
    }

    fun attach(c: Condition) {
        if (_preConditions == null)
            _preConditions = ArrayList()

        if (!_preConditions!!.contains(c))
            _preConditions!!.add(c)
    }

    fun checkCondition(activeChar: Creature, target: WorldObject, sendMessage: Boolean): Boolean {
        // Don't allow hero equipment and restricted items during Olympiad
        if ((isOlyRestrictedItem || isHeroItem) && activeChar is Player && activeChar.actingPlayer!!.isInOlympiadMode) {
            if (isEquipable)
                activeChar.actingPlayer!!.sendPacket(SystemMessageId.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT)
            else
                activeChar.actingPlayer!!.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT)

            return false
        }

        if (_preConditions == null)
            return true

        val env = Env()
        env.character = activeChar
        if (target is Creature)
            env.target = target

        for (preCondition in _preConditions) {
            if (preCondition == null)
                continue

            if (!preCondition.test(env)) {
                if (activeChar is Summon) {
                    activeChar.actingPlayer!!.sendPacket(SystemMessageId.PET_CANNOT_USE_ITEM)
                    return false
                }

                if (sendMessage) {
                    val msg = preCondition.message
                    val msgId = preCondition.messageId
                    if (msg != null) {
                        activeChar.sendMessage(msg)
                    } else if (msgId != 0) {
                        val sm = SystemMessage.getSystemMessage(msgId)
                        if (preCondition.isAddName)
                            sm.addItemName(itemId)
                        activeChar.sendPacket(sm)
                    }
                }
                return false
            }
        }
        return true
    }

    /**
     * Returns the name of the item
     * @return String
     */
    override fun toString(): String {
        return "$name ($itemId)"
    }

    fun addQuestEvent(quest: Quest) {
        if (_questEvents.isEmpty())
            _questEvents = ArrayList(3)

        _questEvents.add(quest)
    }

    companion object {
        const val TYPE1_WEAPON_RING_EARRING_NECKLACE = 0
        const val TYPE1_SHIELD_ARMOR = 1
        const val TYPE1_ITEM_QUESTITEM_ADENA = 4

        const val TYPE2_WEAPON = 0
        const val TYPE2_SHIELD_ARMOR = 1
        const val TYPE2_ACCESSORY = 2
        const val TYPE2_QUEST = 3
        const val TYPE2_MONEY = 4
        const val TYPE2_OTHER = 5

        const val SLOT_NONE = 0x0000
        const val SLOT_UNDERWEAR = 0x0001
        const val SLOT_R_EAR = 0x0002
        const val SLOT_L_EAR = 0x0004
        const val SLOT_LR_EAR = 0x00006
        const val SLOT_NECK = 0x0008
        const val SLOT_R_FINGER = 0x0010
        const val SLOT_L_FINGER = 0x0020
        const val SLOT_LR_FINGER = 0x0030
        const val SLOT_HEAD = 0x0040
        const val SLOT_R_HAND = 0x0080
        const val SLOT_L_HAND = 0x0100
        const val SLOT_GLOVES = 0x0200
        const val SLOT_CHEST = 0x0400
        const val SLOT_LEGS = 0x0800
        const val SLOT_FEET = 0x1000
        const val SLOT_BACK = 0x2000
        const val SLOT_LR_HAND = 0x4000
        const val SLOT_FULL_ARMOR = 0x8000
        const val SLOT_FACE = 0x010000
        const val SLOT_ALLDRESS = 0x020000
        const val SLOT_HAIR = 0x040000
        const val SLOT_HAIRALL = 0x080000

        const val SLOT_WOLF = -100
        const val SLOT_HATCHLING = -101
        const val SLOT_STRIDER = -102
        const val SLOT_BABYPET = -103

        const val SLOT_ALLWEAPON = SLOT_LR_HAND or SLOT_R_HAND
    }
}