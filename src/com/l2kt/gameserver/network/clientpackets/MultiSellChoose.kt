package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.L2Augmentation
import com.l2kt.gameserver.model.multisell.Ingredient
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ItemList
import com.l2kt.gameserver.network.serverpackets.StatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class MultiSellChoose : L2GameClientPacket() {
    // private static final int PC_BANG_POINTS = 65436;

    private var _listId: Int = 0
    private var _entryId: Int = 0
    private var _amount: Int = 0

    override fun readImpl() {
        _listId = readD()
        _entryId = readD()
        _amount = readD()
    }

    public override fun runImpl() {
        val player = client.activeChar ?: return

        if (!FloodProtectors.performAction(client, FloodProtectors.Action.MULTISELL)) {
            player.multiSell = null
            return
        }

        if (_amount < 1 || _amount > 9999) {
            player.multiSell = null
            return
        }

        val list = player.multiSell
        if (list == null || list.id != _listId) {
            player.multiSell = null
            return
        }

        val folk = player.currentFolk
        if (folk != null && !list.isNpcAllowed(folk.npcId) || folk == null && list.isNpcOnly) {
            player.multiSell = null
            return
        }

        if (folk != null && !folk.canInteract(player)) {
            player.multiSell = null
            return
        }

        val inv = player.inventory
        val entry = list.entries[_entryId - 1] // Entry Id begins from 1. We currently use entry IDs as index pointer.
        if (entry == null) {
            player.multiSell = null
            return
        }

        if (!entry.isStackable && _amount > 1) {
            player.multiSell = null
            return
        }

        var slots = 0
        var weight = 0
        for (e in entry.products) {
            if (e.itemId < 0)
                continue

            if (!e.isStackable)
                slots += e.itemCount * _amount
            else if (player.inventory!!.getItemByItemId(e.itemId) == null)
                slots++

            weight += e.itemCount * _amount * e.weight
        }

        if (!inv!!.validateWeight(weight)) {
            player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED)
            return
        }

        if (!inv.validateCapacity(slots)) {
            player.sendPacket(SystemMessageId.SLOTS_FULL)
            return
        }

        // Generate a list of distinct ingredients and counts in order to check if the correct item-counts are possessed by the player
        val ingredientsList = ArrayList<Ingredient>(entry.ingredients.size)
        var newIng: Boolean

        for (e in entry.ingredients) {
            newIng = true

            // at this point, the template has already been modified so that enchantments are properly included
            // whenever they need to be applied. Uniqueness of items is thus judged by item id AND enchantment level
            var i = ingredientsList.size
            while (--i >= 0) {
                val ex = ingredientsList[i]

                // if the item was already added in the list, merely increment the count
                // this happens if 1 list entry has the same ingredient twice (example 2 swords = 1 dual)
                if (ex.itemId == e.itemId && ex.enchantLevel == e.enchantLevel) {
                    if (ex.itemCount + e.itemCount > Integer.MAX_VALUE) {
                        player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED)
                        return
                    }

                    // two same ingredients, merge into one and replace old
                    val ing = ex.copy
                    ing.itemCount = ex.itemCount + e.itemCount
                    ingredientsList[i] = ing

                    newIng = false
                    break
                }
            }

            // if it's a new ingredient, just store its info directly (item id, count, enchantment)
            if (newIng)
                ingredientsList.add(e)
        }

        // now check if the player has sufficient items in the inventory to cover the ingredients' expences
        for (e in ingredientsList) {
            if (e.itemCount * _amount > Integer.MAX_VALUE) {
                player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED)
                return
            }

            if (e.itemId == CLAN_REPUTATION) {
                if (player.clan == null) {
                    player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER)
                    return
                }

                if (!player.isClanLeader) {
                    player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED)
                    return
                }

                if (player.clan!!.reputationScore < e.itemCount * _amount) {
                    player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW)
                    return
                }
            } else {
                // if this is not a list that maintains enchantment, check the count of all items that have the given id.
                // otherwise, check only the count of items with exactly the needed enchantment level
                if (inv.getInventoryItemCount(
                        e.itemId,
                        if (list.maintainEnchantment) e.enchantLevel else -1,
                        false
                    ) < (if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.maintainIngredient) e.itemCount * _amount else e.itemCount)
                ) {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
                    return
                }
            }
        }

        val augmentation = ArrayList<L2Augmentation>()

        for (e in entry.ingredients) {
            if (e.itemId == CLAN_REPUTATION) {
                val amount = e.itemCount * _amount

                player.clan?.takeReputationScore(amount)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(
                        amount
                    )
                )
            } else {
                var itemToTake = inv.getItemByItemId(e.itemId)
                if (itemToTake == null) {
                    player.multiSell = null
                    return
                }

                if (Config.ALT_BLACKSMITH_USE_RECIPES || !e.maintainIngredient) {
                    // if it's a stackable item, just reduce the amount from the first (only) instance that is found in the inventory
                    if (itemToTake.isStackable) {
                        if (!player.destroyItem(
                                "Multisell",
                                itemToTake.objectId,
                                e.itemCount * _amount,
                                player.target,
                                true
                            )
                        ) {
                            player.multiSell = null
                            return
                        }
                    } else {
                        // for non-stackable items, one of two scenaria are possible:
                        // a) list maintains enchantment: get the instances that exactly match the requested enchantment level
                        // b) list does not maintain enchantment: get the instances with the LOWEST enchantment level

                        // a) if enchantment is maintained, then get a list of items that exactly match this enchantment
                        if (list.maintainEnchantment) {
                            // loop through this list and remove (one by one) each item until the required amount is taken.
                            val inventoryContents = inv.getAllItemsByItemId(e.itemId, e.enchantLevel, false)
                            for (i in 0 until e.itemCount * _amount) {
                                if (inventoryContents[i].isAugmented)
                                    augmentation.add(inventoryContents[i].getAugmentation()!!)

                                if (!player.destroyItem(
                                        "Multisell",
                                        inventoryContents[i].objectId,
                                        1,
                                        player.target,
                                        true
                                    )
                                ) {
                                    player.multiSell = null
                                    return
                                }
                            }
                        } else
                        // b) enchantment is not maintained. Get the instances with the LOWEST enchantment level
                        {
                            for (i in 1..e.itemCount * _amount) {
                                val inventoryContents = inv.getAllItemsByItemId(e.itemId, false)

                                itemToTake = inventoryContents[0]
                                // get item with the LOWEST enchantment level from the inventory (0 is the lowest)
                                if (itemToTake!!.enchantLevel > 0) {
                                    for (item in inventoryContents) {
                                        if (item.enchantLevel < itemToTake!!.enchantLevel) {
                                            itemToTake = item

                                            // nothing will have enchantment less than 0. If a zero-enchanted item is found, just take it
                                            if (itemToTake!!.enchantLevel == 0)
                                                break
                                        }
                                    }
                                }

                                if (!player.destroyItem("Multisell", itemToTake!!.objectId, 1, player.target, true)) {
                                    player.multiSell = null
                                    return
                                }
                            }
                        }
                    }
                }
            }
        }

        // Generate the appropriate items
        for (e in entry.products) {
            if (e.itemId == CLAN_REPUTATION)
                player.clan?.addReputationScore(e.itemCount * _amount)
            else {
                if (e.isStackable)
                    inv.addItem("Multisell", e.itemId, e.itemCount * _amount, player, player.target)
                else {
                    for (i in 0 until e.itemCount * _amount) {
                        val product = inv.addItem("Multisell", e.itemId, 1, player, player.target)
                        if (product != null && list.maintainEnchantment) {
                            if (i < augmentation.size)
                                product.setAugmentation(L2Augmentation(augmentation[i].getAugmentationId(), augmentation[i].skill))

                            product.enchantLevel = e.enchantLevel
                            product.updateDatabase()
                        }
                    }
                }

                // msg part
                val sm: SystemMessage

                if (e.itemCount * _amount > 1)
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(e.itemId)
                        .addNumber(e.itemCount * _amount)
                else {
                    if (list.maintainEnchantment && e.enchantLevel > 0)
                        sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_S2).addNumber(e.enchantLevel)
                            .addItemName(e.itemId)
                    else
                        sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(e.itemId)
                }
                player.sendPacket(sm)
            }
        }
        player.sendPacket(ItemList(player, false))

        // All ok, send success message, remove items and add final product
        player.sendPacket(SystemMessageId.SUCCESSFULLY_TRADED_WITH_NPC)

        val su = StatusUpdate(player)
        su.addAttribute(StatusUpdate.CUR_LOAD, player.currentLoad)
        player.sendPacket(su)

        // finally, give the tax to the castle...
        if (folk != null && entry.taxAmount > 0)
            folk.castle?.addToTreasury(entry.taxAmount * _amount)
    }

    companion object {
        // Special IDs.
        private val CLAN_REPUTATION = 65336
    }
}