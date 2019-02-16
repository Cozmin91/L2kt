package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * Handler to use mercenary tickets.<br></br>
 * <br></br>
 * Check constraints:
 *
 *  * Only specific tickets may be used in each castle (different tickets for each castle)
 *  * Only the owner of that castle may use them
 *  * tickets cannot be used during siege
 *  * Check if max number of tickets from this ticket's TYPE has been reached
 *
 * If allowed, spawn the item in the world and remove it from the player's inventory.
 */
class MercTicket : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        val activeChar = playable as Player ?: return

        val castle = CastleManager.getCastle(activeChar) ?: return

        val castleId = castle.castleId

        // Castle lord check.
        if (!activeChar.isCastleLord(castleId)) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES)
            return
        }

        val itemId = item.itemId
        val ticket = castle.getTicket(itemId)

        // Valid ticket for castle check.
        if (ticket == null) {
            activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE)
            return
        }

        // Siege in progress check.
        if (castle.siege.isInProgress) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE)
            return
        }

        // Seal validation check.
        if (!SevenSigns.getInstance().isSealValidationPeriod) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE)
            return
        }

        // Seal of Strife owner check.
        if (!ticket.isSsqType(SevenSigns.getInstance().getSealOwner(SealType.STRIFE))) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE)
            return
        }

        // Max amount check.
        if (castle.getDroppedTicketsCount(itemId) >= ticket.maxAmount) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE)
            return
        }

        // Distance check.
        if (castle.isTooCloseFromDroppedTicket(activeChar.x, activeChar.y, activeChar.z)) {
            activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT)
            return
        }

        val droppedTicket =
            activeChar.dropItem("Consume", item.objectId, 1, activeChar.x, activeChar.y, activeChar.z, null, false)
                ?: return

        castle.addDroppedTicket(droppedTicket)

        activeChar.sendPacket(
            SystemMessage.getSystemMessage(SystemMessageId.PLACE_S1_IN_CURRENT_LOCATION_AND_DIRECTION).addItemName(
                itemId
            )
        )
    }
}