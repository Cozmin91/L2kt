package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.Config
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * @author Kerberos
 */
class PetFood : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        val itemId = item.itemId
        when (itemId) {
            2515 // Wolf's food
            -> useFood(playable, 2048, item)
            4038 // Hatchling's food
            -> useFood(playable, 2063, item)
            5168 // Strider's food
            -> useFood(playable, 2101, item)
            5169 // ClanHall / Castle Strider's food
            -> useFood(playable, 2102, item)
            6316 // Wyvern's food
            -> useFood(playable, 2180, item)
            7582 // Baby Pet's food
            -> useFood(playable, 2048, item)
        }
    }

    fun useFood(activeChar: Playable, magicId: Int, item: ItemInstance): Boolean {
        val skill = SkillTable.getInfo(magicId, 1)
        if (skill != null) {
            if (activeChar is Pet) {
                if (activeChar.destroyItem("Consume", item.objectId, 1, null, false)) {
                    // Send visual effect.
                    activeChar.broadcastPacket(MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0))

                    // Put current value.
                    activeChar.currentFed = activeChar.currentFed + skill.feed * Config.PET_FOOD_RATE

                    // If pet is still hungry, send an alert.
                    if (activeChar.checkAutoFeedState())
                        activeChar.owner.sendPacket(SystemMessageId.YOUR_PET_ATE_A_LITTLE_BUT_IS_STILL_HUNGRY)

                    return true
                }
            } else if (activeChar is Player) {
                val itemId = item.itemId

                if (activeChar.isMounted && activeChar.petTemplate.canEatFood(itemId)) {
                    if (activeChar.destroyItem("Consume", item.objectId, 1, null, false)) {
                        activeChar.broadcastPacket(MagicSkillUse(activeChar, activeChar, magicId, 1, 0, 0))
                        activeChar.currentFeed = activeChar.currentFeed + skill.feed * Config.PET_FOOD_RATE
                    }
                    return true
                }

                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(
                        itemId
                    )
                )
                return false
            }
        }
        return false
    }
}