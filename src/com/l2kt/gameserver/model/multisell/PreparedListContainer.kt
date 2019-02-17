package com.l2kt.gameserver.model.multisell

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.kind.Weapon
import java.util.*

/**
 * A dynamic layer of [ListContainer], which holds the current [Npc] objectId for security reasons.<br></br>
 * <br></br>
 * It can also allow to check inventory content.
 */
class PreparedListContainer(template: ListContainer, inventoryOnly: Boolean, player: Player?, npc: Npc?) :
    ListContainer(template.id) {
    private var _npcObjectId = 0

    init {
        run{
            maintainEnchantment = template.maintainEnchantment
            applyTaxes = false

            _npcsAllowed = template._npcsAllowed

            var taxRate = 0.0
            if (npc != null) {
                _npcObjectId = npc.objectId
                if (template.applyTaxes && npc.castle != null && npc.castle.ownerId > 0) {
                    applyTaxes = true
                    taxRate = npc.castle.taxRate
                }
            }

            if (inventoryOnly) {
                if (player == null)
                    return@run

                val items: Array<ItemInstance>
                if (maintainEnchantment)
                    items = player.inventory!!.getUniqueItemsByEnchantLevel(false, false, false)
                else
                    items = player.inventory!!.getUniqueItems(false, false, false)

                entries = LinkedList()
                for (item in items) {
                    // only do the match up on equippable items that are not currently equipped
                    // so for each appropriate item, produce a set of entries for the multisell list.
                    if (!item.isEquipped && (item.item is Armor || item.item is Weapon)) {
                        // loop through the entries to see which ones we wish to include
                        for (ent in template.entries) {
                            // check ingredients of this entry to see if it's an entry we'd like to include.
                            for (ing in ent.ingredients) {
                                if (item.itemId == ing.itemId) {
                                    entries.add(PreparedEntry(ent, item, applyTaxes, maintainEnchantment, taxRate))
                                    break // next entry
                                }
                            }
                        }
                    }
                }
            } else {
                entries = ArrayList(template.entries.size)

                for (ent in template.entries)
                    entries.add(PreparedEntry(ent, null, applyTaxes, false, taxRate))
            }
        }
    }

    fun checkNpcObjectId(npcObjectId: Int): Boolean {
        return if (_npcObjectId != 0) _npcObjectId == npcObjectId else true
    }
}