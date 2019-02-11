package com.l2kt.gameserver.taskmanager

import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.itemcontainer.listeners.OnEquipListener
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Updates the timer and removes the [ItemInstance] as a shadow item.
 */
object ShadowItemTaskManager : Runnable, OnEquipListener {

    private val shadowItems = ConcurrentHashMap<ItemInstance, Player>()
    private const val DELAY_SECONDS = 1

    init {
        ThreadPool.scheduleAtFixedRate(this, 1000, 1000)
    }

    override fun run() {
        if (shadowItems.isEmpty())
            return

        for ((item, player) in shadowItems) {

            val mana = item.decreaseMana(DELAY_SECONDS)

            if (mana == -1) {
                player.inventory!!.unEquipItemInSlotAndRecord(item.locationSlot)
                val iu = InventoryUpdate()
                iu.addModifiedItem(item)
                player.sendPacket(iu)

                player.destroyItem("ShadowItem", item, player, false)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_0).addItemName(
                        item.itemId
                    )
                )
                shadowItems.remove(item)

                continue
            }

            when (mana) {
                60 - DELAY_SECONDS -> player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_1).addItemName(
                        item.itemId
                    )
                )
                300 - DELAY_SECONDS -> player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_5).addItemName(
                        item.itemId
                    )
                )
                600 - DELAY_SECONDS -> player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1S_REMAINING_MANA_IS_NOW_10).addItemName(
                        item.itemId
                    )
                )
            }

            if (mana % 60 == 60 - DELAY_SECONDS) {
                val iu = InventoryUpdate()
                iu.addModifiedItem(item)
                player.sendPacket(iu)
            }
        }
    }

    override fun onEquip(slot: Int, item: ItemInstance, playable: Playable) {
        if (!item.isShadowItem)
            return

        if (playable !is Player)
            return

        shadowItems[item] = playable
    }

    override fun onUnequip(slot: Int, item: ItemInstance, actor: Playable) {
        if (!item.isShadowItem)
            return

        shadowItems.remove(item)
    }

    fun remove(player: Player) {
        if (shadowItems.isEmpty())
            return

        shadowItems.values.removeAll(setOf(player))
    }
}