package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.extensions.toSelfAndKnownPlayers
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.FloodProtectors.Action
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.Dice
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class RollingDice : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (!FloodProtectors.performAction(playable.client, Action.ROLL_DICE)) {
            playable.sendPacket(SystemMessageId.YOU_MAY_NOT_THROW_THE_DICE_AT_THIS_TIME_TRY_AGAIN_LATER)
            return
        }

        val number = Rnd[1, 6]

        playable.toSelfAndKnownPlayers(
            Dice(
                playable.objectId,
                item.itemId,
                number,
                playable.x - 30,
                playable.y - 30,
                playable.z
            )
        )
        playable.toSelfAndKnownPlayers(
            SystemMessage.getSystemMessage(SystemMessageId.S1_ROLLED_S2).addCharName(playable).addNumber(
                number
            )
        )
    }
}