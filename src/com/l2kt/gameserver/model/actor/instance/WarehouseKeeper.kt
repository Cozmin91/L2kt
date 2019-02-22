package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*

/**
 * An instance type extending [Folk], used by warehouse keepers.<br></br>
 * <br></br>
 * A warehouse keeper stores [Player] items in a personal container.
 */
open class WarehouseKeeper(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override val isWarehouse: Boolean
        get() = true

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/warehouse/$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.karma > 0 && showPkDenyChatWindow(player, "warehouse"))
            return

        if (player.isProcessingTransaction) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING)
            return
        }

        if (player.activeEnchantItem != null) {
            player.activeEnchantItem = null
            player.sendPacket(EnchantResult.CANCELLED)
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED)
        }

        if (command.startsWith("WithdrawP")) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            player.activeWarehouse = player.warehouse

            if (player.activeWarehouse.size == 0) {
                player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH)
                return
            }

            player.sendPacket(WarehouseWithdrawList(player, WarehouseWithdrawList.PRIVATE))
        } else if (command == "DepositP") {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            player.activeWarehouse = player.warehouse
            player.tempInventoryDisable()

            player.sendPacket(WarehouseDepositList(player, WarehouseDepositList.PRIVATE))
        } else if (command == "WithdrawC") {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            if (player.clanPrivileges and Clan.CP_CL_VIEW_WAREHOUSE != Clan.CP_CL_VIEW_WAREHOUSE) {
                player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE)
                return
            }

            if (player.clan.level == 0)
                player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE)
            else {
                player.activeWarehouse = player.clan.warehouse
                player.sendPacket(WarehouseWithdrawList(player, WarehouseWithdrawList.CLAN))
            }
        } else if (command == "DepositC") {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            if (player.clan != null) {
                if (player.clan.level == 0)
                    player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE)
                else {
                    player.activeWarehouse = player.clan.warehouse
                    player.tempInventoryDisable()
                    player.sendPacket(WarehouseDepositList(player, WarehouseDepositList.CLAN))
                }
            }
        } else if (command.startsWith("WithdrawF")) {
            if (Config.ALLOW_FREIGHT) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                val freight = player.freight

                if (freight != null) {
                    if (freight.size > 0) {
                        if (Config.ALT_GAME_FREIGHTS)
                            freight.setActiveLocation(0)
                        else
                            freight.setActiveLocation(region!!.hashCode())

                        player.activeWarehouse = freight
                        player.sendPacket(WarehouseWithdrawList(player, WarehouseWithdrawList.FREIGHT))
                    } else
                        player.sendPacket(SystemMessageId.NO_ITEM_DEPOSITED_IN_WH)
                }
            }
        } else if (command.startsWith("DepositF")) {
            if (Config.ALLOW_FREIGHT) {
                // No other chars in the account of this player
                if (player.accountChars.isEmpty())
                    player.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST)
                else {
                    val chars = player.accountChars

                    if (chars.isEmpty()) {
                        player.sendPacket(ActionFailed.STATIC_PACKET)
                        return
                    }

                    player.sendPacket(PackageToList(chars))
                }// One or more chars other than this player for this account
            }
        } else if (command.startsWith("FreightChar")) {
            if (Config.ALLOW_FREIGHT) {
                val id = command.substring(command.lastIndexOf("_") + 1)

                player.sendPacket(ActionFailed.STATIC_PACKET)

                val freight = player.getDepositedFreight(Integer.parseInt(id))

                if (Config.ALT_GAME_FREIGHTS)
                    freight.setActiveLocation(0)
                else
                    freight.setActiveLocation(region!!.hashCode())

                player.activeWarehouse = freight
                player.tempInventoryDisable()
                player.sendPacket(WarehouseDepositList(player, WarehouseDepositList.FREIGHT))
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.karma > 0 && showPkDenyChatWindow(player, "warehouse"))
            return

        showChatWindow(player, getHtmlPath(npcId, `val`))
    }
}