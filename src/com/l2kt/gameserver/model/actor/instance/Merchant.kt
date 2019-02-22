package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.data.xml.MultisellData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.BuyList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SellList
import com.l2kt.gameserver.network.serverpackets.ShopPreviewList
import java.util.*

/**
 * An instance type extending [Folk], used for merchant (regular and multisell). It got buy/sell methods.<br></br>
 * <br></br>
 * It is used as mother class for few children, such as [Fisherman].
 */
open class Merchant(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""

        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/merchant/$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_SHOP && player.karma > 0 && showPkDenyChatWindow(player, "merchant"))
            return

        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        if (actualCommand.equals("Buy", ignoreCase = true)) {
            if (st.countTokens() < 1)
                return

            showBuyWindow(player, Integer.parseInt(st.nextToken()))
        } else if (actualCommand.equals("Sell", ignoreCase = true)) {
            // Retrieve sellable items.
            val items = player.inventory!!.sellableItems
            if (items.isEmpty()) {
                val content =
                    HtmCache.getHtm("data/html/" + (if (this is Fisherman) "fisherman" else "merchant") + "/" + npcId + "-empty.htm")
                if (content.isNotEmpty()) {
                    val html = NpcHtmlMessage(objectId)
                    html.setHtml(content)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                    return
                }
            }

            player.sendPacket(SellList(player.adena, items))
        } else if (actualCommand.equals("Wear", ignoreCase = true) && Config.ALLOW_WEAR) {
            if (st.countTokens() < 1)
                return

            showWearWindow(player, Integer.parseInt(st.nextToken()))
        } else if (actualCommand.equals("Multisell", ignoreCase = true)) {
            if (st.countTokens() < 1)
                return

            MultisellData.separateAndSend(st.nextToken(), player, this, false)
        } else if (actualCommand.equals("Multisell_Shadow", ignoreCase = true)) {
            val html = NpcHtmlMessage(objectId)

            if (player.level < 40)
                html.setFile("data/html/common/shadow_item-lowlevel.htm")
            else if (player.level < 46)
                html.setFile("data/html/common/shadow_item_mi_c.htm")
            else if (player.level < 52)
                html.setFile("data/html/common/shadow_item_hi_c.htm")
            else
                html.setFile("data/html/common/shadow_item_b.htm")

            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand.equals("Exc_Multisell", ignoreCase = true)) {
            if (st.countTokens() < 1)
                return

            MultisellData.separateAndSend(st.nextToken(), player, this, true)
        } else if (actualCommand.equals("Newbie_Exc_Multisell", ignoreCase = true)) {
            if (st.countTokens() < 1)
                return

            if (player.isNewbie)
                MultisellData.separateAndSend(st.nextToken(), player, this, true)
            else
                showChatWindow(player, "data/html/exchangelvlimit.htm")
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_SHOP && player.karma > 0 && showPkDenyChatWindow(player, "merchant"))
            return

        showChatWindow(player, getHtmlPath(npcId, `val`))
    }

    private fun showWearWindow(player: Player, `val`: Int) {
        val buyList = BuyListManager.getBuyList(`val`)
        if (buyList == null || !buyList.isNpcAllowed(npcId))
            return

        player.tempInventoryDisable()
        player.sendPacket(ShopPreviewList(buyList, player.adena, player.getSkillLevel(L2Skill.SKILL_EXPERTISE)))
    }

    protected fun showBuyWindow(player: Player, `val`: Int) {
        val buyList = BuyListManager.getBuyList(`val`)
        if (buyList == null || !buyList.isNpcAllowed(npcId))
            return

        player.tempInventoryDisable()
        player.sendPacket(BuyList(buyList, player.adena, if (castle != null) castle!!.taxRate else 0.0))
    }
}