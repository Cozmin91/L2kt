package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * @author l3x
 */
class CastleWarehouseKeeper(objectId: Int, template: NpcTemplate) : WarehouseKeeper(objectId, template) {

    override val isWarehouse: Boolean
        get() = true

    override fun showChatWindow(player: Player, `val`: Int) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
        var filename = "data/html/castlewarehouse/castlewarehouse-no.htm"

        val condition = validateCondition(player)
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
                filename = "data/html/castlewarehouse/castlewarehouse-busy.htm"
            else if (condition == COND_OWNER) {
                if (`val` == 0)
                    filename = "data/html/castlewarehouse/castlewarehouse.htm"
                else
                    filename = "data/html/castlewarehouse/castlewarehouse-$`val`.htm"
            }
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        html.replace("%npcname%", name)
        player.sendPacket(html)
    }

    protected fun validateCondition(player: Player): Int {
        if (castle != null && player.clan != null) {
            if (castle!!.siege.isInProgress)
                return COND_BUSY_BECAUSE_OF_SIEGE

            if (castle!!.ownerId == player.clanId)
                return COND_OWNER
        }
        return COND_ALL_FALSE
    }

    companion object {
        protected const val COND_ALL_FALSE = 0
        protected const val COND_BUSY_BECAUSE_OF_SIEGE = 1
        protected const val COND_OWNER = 2
    }
}