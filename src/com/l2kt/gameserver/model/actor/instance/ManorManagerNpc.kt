package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*

class ManorManagerNpc(objectId: Int, template: NpcTemplate) : Merchant(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("manor_menu_select")) {
            if (CastleManorManager.isUnderMaintenance) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE)
                return
            }

            val st = StringTokenizer(command, "&")

            val ask =
                Integer.parseInt(st.nextToken().split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val state =
                Integer.parseInt(st.nextToken().split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val time = st.nextToken().split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1] == "1"

            val castleId = if (state < 0) castle.castleId else state

            when (ask) {
                1 // Seed purchase
                -> if (castleId != castle.castleId)
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.HERE_YOU_CAN_BUY_ONLY_SEEDS_OF_S1_MANOR).addString(
                            castle.name
                        )
                    )
                else
                    player.sendPacket(BuyListSeed(player.adena, castleId))

                2 // Crop sales
                -> player.sendPacket(ExShowSellCropList(player.inventory!!, castleId))

                3 // Current seeds (Manor info)
                -> player.sendPacket(ExShowSeedInfo(castleId, time, false))

                4 // Current crops (Manor info)
                -> player.sendPacket(ExShowCropInfo(castleId, time, false))

                5 // Basic info (Manor info)
                -> player.sendPacket(ExShowManorDefaultInfo(false))

                6 // Buy harvester
                -> showBuyWindow(player, 300000 + npcId)

                9 // Edit sales (Crop sales)
                -> player.sendPacket(ExShowProcureCropDetail(state))
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        return "data/html/manormanager/manager.htm"
    }

    override fun showChatWindow(player: Player) {
        if (!Config.ALLOW_MANOR) {
            showChatWindow(player, "data/html/npcdefault.htm")
            return
        }

        if (castle != null && player.clan != null && castle.ownerId == player.clanId && player.isClanLeader)
            showChatWindow(player, "data/html/manormanager/manager-lord.htm")
        else
            showChatWindow(player, "data/html/manormanager/manager.htm")
    }
}
