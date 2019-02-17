package com.l2kt.gameserver.model.item.instance

import com.l2kt.gameserver.model.item.kind.Item

/**
 * Get all information from ItemInstance to generate ItemInfo.
 */
class ItemInfo {
    /** Identifier of the ItemInstance  */
    var objectId: Int = 0
        private set

    /** The L2Item template of the ItemInstance  */
    var item: Item? = null
        private set

    /** The level of enchant on the ItemInstance  */
    var enchant: Int = 0
        private set

    /** The augmentation of the item  */
    var augmentationBoni: Int = 0
        private set

    /** The quantity of ItemInstance  */
    var count: Int = 0
        private set

    /** The price of the ItemInstance  */
    val price: Int = 0

    /** The custom ItemInstance types (used loto, race tickets)  */
    var customType1: Int = 0
        private set
    var customType2: Int = 0
        private set

    /** If True the ItemInstance is equipped  */
    var equipped: Int = 0
        private set

    /** The action to do clientside (1=ADD, 2=MODIFY, 3=REMOVE)  */
    var change: ItemInstance.ItemState? = null
        private set

    /** The mana of this item  */
    var mana: Int = 0
        private set

    /**
     * Get all information from ItemInstance to generate ItemInfo.
     * @param item The item instance.
     */
    constructor(item: ItemInstance?) {
        if (item == null)
            return

        // Get the Identifier of the ItemInstance
        objectId = item.objectId

        // Get the L2Item of the ItemInstance
        this.item = item.item

        // Get the enchant level of the ItemInstance
        enchant = item.enchantLevel

        // Get the augmentation boni
        if (item.isAugmented)
            augmentationBoni = item.getAugmentation()!!.getAugmentationId()
        else
            augmentationBoni = 0

        // Get the quantity of the ItemInstance
        count = item.count

        // Get custom item types (used loto, race tickets)
        customType1 = item.customType1
        customType2 = item.customType2

        // Verify if the ItemInstance is equipped
        equipped = if (item.isEquipped) 1 else 0

        // Get the action to do clientside
        change = item.lastChange

        // Get shadow item mana
        mana = item.mana
    }

    constructor(item: ItemInstance?, change: ItemInstance.ItemState) {
        if (item == null)
            return

        // Get the Identifier of the ItemInstance
        objectId = item.objectId

        // Get the L2Item of the ItemInstance
        this.item = item.item

        // Get the enchant level of the ItemInstance
        enchant = item.enchantLevel

        // Get the augmentation boni
        if (item.isAugmented)
            augmentationBoni = item.getAugmentation()!!.getAugmentationId()
        else
            augmentationBoni = 0

        // Get the quantity of the ItemInstance
        count = item.count

        // Get custom item types (used loto, race tickets)
        customType1 = item.customType1
        customType2 = item.customType2

        // Verify if the ItemInstance is equipped
        equipped = if (item.isEquipped) 1 else 0

        // Get the action to do clientside
        this.change = change

        // Get shadow item mana
        mana = item.mana
    }
}