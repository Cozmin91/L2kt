package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed

class EffectPoint(objectId: Int, template: NpcTemplate, owner: Creature?) : Npc(objectId, template) {
    override val actingPlayer: Player? = owner?.actingPlayer

    override fun onAction(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun onActionShift(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }
}