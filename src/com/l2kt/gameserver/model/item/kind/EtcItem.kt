package com.l2kt.gameserver.model.item.kind

import com.l2kt.gameserver.model.item.type.ActionType
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.ItemType
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.templates.StatsSet

/**
 * This class is dedicated to the management of EtcItem.
 */
class EtcItem
/**
 * Constructor for EtcItem.
 * @see Item constructor
 *
 * @param set : StatsSet designating the set of couples (key,value) for description of the Etc
 */
    (set: StatsSet) : Item(set) {

    /**
     * Return handler name. null if no handler for item
     * @return String
     */
    val handlerName: String?
    val sharedReuseGroup: Int
    private var _type: EtcItemType
    val reuseDelay: Int

    init {
        _type = EtcItemType.valueOf(set.getString("etcitem_type", "none")!!.toUpperCase())

        // l2j custom - L2EtcItemType.SHOT
        when (defaultAction) {
            ActionType.soulshot, ActionType.summon_soulshot, ActionType.summon_spiritshot, ActionType.spiritshot -> {
                _type = EtcItemType.SHOT
            }
        }

        type1 = Item.TYPE1_ITEM_QUESTITEM_ADENA
        type2 = Item.TYPE2_OTHER // default is other

        if (isQuestItem)
            type2 = Item.TYPE2_QUEST
        else if (itemId == PcInventory.ADENA_ID || itemId == PcInventory.ANCIENT_ADENA_ID)
            type2 = Item.TYPE2_MONEY

        handlerName = set.getString("handler", null) // ! null !
        sharedReuseGroup = set.getInteger("shared_reuse_group", -1)
        reuseDelay = set.getInteger("reuse_delay", 0)
    }

    override val isConsumable: Boolean
        get() = itemType === EtcItemType.SHOT || itemType === EtcItemType.POTION

    override val itemMask: Int
        get() = itemType!!.mask()

    override val itemType: ItemType?
        get() = _type
}