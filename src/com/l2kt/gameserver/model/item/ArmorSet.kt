package com.l2kt.gameserver.model.item

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.templates.StatsSet

class ArmorSet(set: StatsSet) {
    private val _name: String

    val setItemsId = IntArray(5)

    val skillId: Int
    val shield: Int
    val shieldSkillId: Int
    val enchant6skillId: Int

    init {
        _name = set.getString("name")

        setItemsId[0] = set.getInteger("chest")
        setItemsId[1] = set.getInteger("legs")
        setItemsId[2] = set.getInteger("head")
        setItemsId[3] = set.getInteger("gloves")
        setItemsId[4] = set.getInteger("feet")

        skillId = set.getInteger("skillId")
        shield = set.getInteger("shield")
        shieldSkillId = set.getInteger("shieldSkillId")
        enchant6skillId = set.getInteger("enchant6Skill")
    }

    override fun toString(): String {
        return _name

    }

    /**
     * Checks if player have equipped all items from set (not checking shield)
     * @param player whose inventory is being checked
     * @return True if player equips whole set
     */
    fun containAll(player: Player): Boolean {
        val inv = player.inventory

        var legs = 0
        var head = 0
        var gloves = 0
        var feet = 0

        val legsItem = inv!!.getPaperdollItem(Inventory.PAPERDOLL_LEGS)
        if (legsItem != null)
            legs = legsItem.itemId

        if (setItemsId[1] != 0 && setItemsId[1] != legs)
            return false

        val headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD)
        if (headItem != null)
            head = headItem.itemId

        if (setItemsId[2] != 0 && setItemsId[2] != head)
            return false

        val glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES)
        if (glovesItem != null)
            gloves = glovesItem.itemId

        if (setItemsId[3] != 0 && setItemsId[3] != gloves)
            return false

        val feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET)
        if (feetItem != null)
            feet = feetItem.itemId

        return if (setItemsId[4] != 0 && setItemsId[4] != feet) false else true

    }

    fun containItem(slot: Int, itemId: Int): Boolean {
        when (slot) {
            Inventory.PAPERDOLL_CHEST -> return setItemsId[0] == itemId

            Inventory.PAPERDOLL_LEGS -> return setItemsId[1] == itemId

            Inventory.PAPERDOLL_HEAD -> return setItemsId[2] == itemId

            Inventory.PAPERDOLL_GLOVES -> return setItemsId[3] == itemId

            Inventory.PAPERDOLL_FEET -> return setItemsId[4] == itemId

            else -> return false
        }
    }

    fun containShield(player: Player): Boolean {
        val shieldItem = player.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
        return if (shieldItem != null && shieldItem.itemId == shield) true else false

    }

    fun containShield(shieldId: Int): Boolean {
        return if (shield == 0) false else shield == shieldId

    }

    /**
     * Checks if all parts of set are enchanted to +6 or more
     * @param player
     * @return
     */
    fun isEnchanted6(player: Player): Boolean {
        val inv = player.inventory

        val chestItem = inv!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
        if (chestItem != null && chestItem.enchantLevel < 6)
            return false

        var legs = 0
        var head = 0
        var gloves = 0
        var feet = 0

        val legsItem = inv.getPaperdollItem(Inventory.PAPERDOLL_LEGS)
        if (legsItem != null && legsItem.enchantLevel > 5)
            legs = legsItem.itemId

        if (setItemsId[1] != 0 && setItemsId[1] != legs)
            return false

        val headItem = inv.getPaperdollItem(Inventory.PAPERDOLL_HEAD)
        if (headItem != null && headItem.enchantLevel > 5)
            head = headItem.itemId

        if (setItemsId[2] != 0 && setItemsId[2] != head)
            return false

        val glovesItem = inv.getPaperdollItem(Inventory.PAPERDOLL_GLOVES)
        if (glovesItem != null && glovesItem.enchantLevel > 5)
            gloves = glovesItem.itemId

        if (setItemsId[3] != 0 && setItemsId[3] != gloves)
            return false

        val feetItem = inv.getPaperdollItem(Inventory.PAPERDOLL_FEET)
        if (feetItem != null && feetItem.enchantLevel > 5)
            feet = feetItem.itemId

        return !(setItemsId[4] != 0 && setItemsId[4] != feet)

    }
}