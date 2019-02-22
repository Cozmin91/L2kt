package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.serverpackets.BuyList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

class MercenaryManagerNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        val condition = validateCondition(player)
        if (condition < COND_OWNER)
            return

        if (command.startsWith("back"))
            showChatWindow(player)
        else if (command.startsWith("how_to")) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/mercmanager/mseller005.htm")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (command.startsWith("hire")) {
            // Can't buy new mercenaries if seal validation period isn't reached.
            if (!SevenSigns.isSealValidationPeriod) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/mercmanager/msellerdenial.htm")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            }

            val st = StringTokenizer(command, " ")
            st.nextToken()

            val buyList = BuyListManager.getBuyList(Integer.parseInt(npcId.toString() + st.nextToken()))
            if (buyList == null || !buyList.isNpcAllowed(npcId))
                return

            player.tempInventoryDisable()
            player.sendPacket(BuyList(buyList, player.adena, 0.0))

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/mercmanager/mseller004.htm")
            player.sendPacket(html)
        } else if (command.startsWith("merc_limit")) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/mercmanager/" + if (castle!!.castleId == 5) "aden_msellerLimit.htm" else "msellerLimit.htm")
            html.replace("%castleName%", castle!!.name)
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        val html = NpcHtmlMessage(objectId)

        val condition = validateCondition(player)
        if (condition == COND_ALL_FALSE)
            html.setFile("data/html/mercmanager/mseller002.htm")
        else if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
            html.setFile("data/html/mercmanager/mseller003.htm")
        else if (condition == COND_OWNER) {
            // Different output depending about who is currently owning the Seal of Strife.
            when (SevenSigns.getSealOwner(SealType.STRIFE)) {
                SevenSigns.CabalType.DAWN -> html.setFile("data/html/mercmanager/mseller001_dawn.htm")

                SevenSigns.CabalType.DUSK -> html.setFile("data/html/mercmanager/mseller001_dusk.htm")

                else -> html.setFile("data/html/mercmanager/mseller001.htm")
            }
        }

        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    private fun validateCondition(player: Player): Int {
        if (castle != null && player.clan != null) {
            if (castle!!.siege.isInProgress)
                return COND_BUSY_BECAUSE_OF_SIEGE

            if (castle!!.ownerId == player.clanId && player.clanPrivileges and Clan.CP_CS_MERCENARIES == Clan.CP_CS_MERCENARIES)
                return COND_OWNER
        }
        return COND_ALL_FALSE
    }

    companion object {
        private const val COND_ALL_FALSE = 0
        private const val COND_BUSY_BECAUSE_OF_SIEGE = 1
        private const val COND_OWNER = 2
    }
}