package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.manager.BuyListManager
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Merchant
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*

class AdminEditNpc : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        val st = StringTokenizer(command, " ")
        st.nextToken()

        if (command.startsWith("admin_show_minion")) {
            // You need to target a Monster.
            val target = activeChar.target
            if (target !is Monster) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return false
            }

            // Load static Htm.
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/minion.htm")
            html.replace("%target%", target.name)

            val sb = StringBuilder()

            // Monster is a minion, deliver boss state.
            val master = target.getMaster()
            if (master != null) {
                html.replace("%type%", "minion")
                StringUtil.append(
                    sb,
                    "<tr><td>",
                    master.npcId,
                    "</td><td>",
                    master.name,
                    " (",
                    if (master.isDead) "Dead" else "Alive",
                    ")</td></tr>"
                )
            } else if (target.hasMinions()) {
                html.replace("%type%", "master")

                for ((key, value) in target.minionList.minions)
                    StringUtil.append(
                        sb,
                        "<tr><td>",
                        key.npcId,
                        "</td><td>",
                        key.toString(),
                        " (",
                        if (value) "Alive" else "Dead",
                        ")</td></tr>"
                    )
            } else
                html.replace("%type%", "regular monster")// Monster isn't anything.
            // Monster is a master, find back minions informations.

            html.replace("%minion%", sb.toString())
            activeChar.sendPacket(html)
        } else if (command.startsWith("admin_show_shoplist")) {
            try {
                showShopList(activeChar, Integer.parseInt(st.nextToken()))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_shoplist <list_id>")
            }

        } else if (command.startsWith("admin_show_shop")) {
            try {
                showShop(activeChar, Integer.parseInt(st.nextToken()))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_shop <npc_id>")
            }

        } else if (command.startsWith("admin_show_droplist")) {
            try {
                val npcId = Integer.parseInt(st.nextToken())
                val page = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 1

                showNpcDropList(activeChar, npcId, page)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_droplist <npc_id> [<page>]")
            }

        } else if (command.startsWith("admin_show_skilllist")) {
            try {
                showNpcSkillList(activeChar, Integer.parseInt(st.nextToken()))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_skilllist <npc_id>")
            }

        } else if (command.startsWith("admin_show_scripts")) {
            try {
                showScriptsList(activeChar, Integer.parseInt(st.nextToken()))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_scripts <npc_id>")
            }

        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 20

        private val ADMIN_COMMANDS = arrayOf(
            "admin_show_droplist",
            "admin_show_minion",
            "admin_show_scripts",
            "admin_show_shop",
            "admin_show_shoplist",
            "admin_show_skilllist"
        )

        private fun showShopList(activeChar: Player, listId: Int) {
            val buyList = BuyListManager.getBuyList(listId)
            if (buyList == null) {
                activeChar.sendMessage("BuyList template is unknown for id: $listId.")
                return
            }

            val sb = StringBuilder(500)
            StringUtil.append(
                sb,
                "<html><body><center><font color=\"LEVEL\">",
                NpcData.getTemplate(buyList.npcId)!!.name,
                " (",
                buyList.npcId,
                ") buylist id: ",
                buyList.listId,
                "</font></center><br><table width=\"100%\"><tr><td width=200>Item</td><td width=80>Price</td></tr>"
            )

            for (product in buyList.products)
                StringUtil.append(sb, "<tr><td>", product.item.name, "</td><td>", product.price, "</td></tr>")

            sb.append("</table></body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showShop(activeChar: Player, npcId: Int) {
            val buyLists = BuyListManager.getBuyListsByNpcId(npcId)
            if (buyLists.isEmpty()) {
                activeChar.sendMessage("No buyLists found for id: $npcId.")
                return
            }

            val sb = StringBuilder(500)
            StringUtil.append(sb, "<html><title>Merchant Shop Lists</title><body>")

            if (activeChar.target is Merchant) {
                val merchant = activeChar.target as Npc
                val taxRate = merchant.castle.taxPercent

                StringUtil.append(
                    sb,
                    "<center><font color=\"LEVEL\">",
                    merchant.name,
                    " (",
                    npcId,
                    ")</font></center><br>Tax rate: ",
                    taxRate,
                    "%"
                )
            }

            StringUtil.append(sb, "<table width=\"100%\">")

            for (buyList in buyLists)
                StringUtil.append(
                    sb,
                    "<tr><td><a action=\"bypass -h admin_show_shoplist ",
                    buyList.listId,
                    " 1\">Buylist id: ",
                    buyList.listId,
                    "</a></td></tr>"
                )

            StringUtil.append(sb, "</table></body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showNpcDropList(activeChar: Player, npcId: Int, page: Int) {
            val npcData = NpcData.getTemplate(npcId)
            if (npcData == null) {
                activeChar.sendMessage("Npc template is unknown for id: $npcId.")
                return
            }

            val sb = StringBuilder(2000)
            StringUtil.append(
                sb,
                "<html><title>Show droplist page ",
                page,
                "</title><body><center><font color=\"LEVEL\">",
                npcData.name,
                " (",
                npcId,
                ")</font></center><br>"
            )

            if (!npcData.dropData.isEmpty()) {
                sb.append("Drop type legend: <font color=\"3BB9FF\">Drop</font> | <font color=\"00ff00\">Sweep</font><br><table><tr><td width=25>cat.</td><td width=255>item</td></tr>")

                var myPage = 1
                var i = 0
                var shown = 0
                var hasMore = false

                for (cat in npcData.dropData) {
                    if (shown == PAGE_LIMIT) {
                        hasMore = true
                        break
                    }

                    for (drop in cat.allDrops) {
                        if (myPage != page) {
                            i++
                            if (i == PAGE_LIMIT) {
                                myPage++
                                i = 0
                            }
                            continue
                        }

                        if (shown == PAGE_LIMIT) {
                            hasMore = true
                            break
                        }

                        StringUtil.append(
                            sb,
                            "<tr><td><font color=\"",
                            if (cat.isSweep) "00FF00" else "3BB9FF",
                            "\">",
                            cat.categoryType,
                            "</td><td>",
                            ItemTable.getTemplate(drop.itemId)!!.name,
                            " (",
                            drop.itemId,
                            ")</td></tr>"
                        )
                        shown++
                    }
                }

                sb.append("</table><table width=\"100%\" bgcolor=666666><tr>")

                if (page > 1) {
                    StringUtil.append(
                        sb,
                        "<td width=120><a action=\"bypass -h admin_show_droplist ",
                        npcId,
                        " ",
                        page - 1,
                        "\">Prev Page</a></td>"
                    )
                    if (!hasMore)
                        StringUtil.append(sb, "<td width=100>Page ", page, "</td><td width=70></td></tr>")
                }

                if (hasMore) {
                    if (page <= 1)
                        sb.append("<td width=120></td>")

                    StringUtil.append(
                        sb,
                        "<td width=100>Page ",
                        page,
                        "</td><td width=70><a action=\"bypass -h admin_show_droplist ",
                        npcId,
                        " ",
                        page + 1,
                        "\">Next Page</a></td></tr>"
                    )
                }
                sb.append("</table>")
            } else
                sb.append("This NPC has no drops.")

            sb.append("</body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showNpcSkillList(activeChar: Player, npcId: Int) {
            val npcData = NpcData.getTemplate(npcId)
            if (npcData == null) {
                activeChar.sendMessage("Npc template is unknown for id: $npcId.")
                return
            }

            val sb = StringBuilder(500)
            StringUtil.append(
                sb,
                "<html><body><center><font color=\"LEVEL\">",
                npcData.name,
                " (",
                npcId,
                ") skills</font></center><br>"
            )

            if (!npcData.skills.isEmpty()) {
                var type: NpcTemplate.SkillType? = null // Used to see if we moved of type.

                // For any type of SkillType
                for ((key, value) in npcData.skills) {
                    if (type != key) {
                        type = key
                        StringUtil.append(sb, "<br><font color=\"LEVEL\">", type!!.name, "</font><br1>")
                    }

                    for (skill in value)
                        StringUtil.append(
                            sb,
                            if (skill.skillType === L2SkillType.NOTDONE) "<font color=\"777777\">" + skill.name + "</font>" else skill.name,
                            " [",
                            skill.id,
                            "-",
                            skill.level,
                            "]<br1>"
                        )
                }
            } else
                sb.append("This NPC doesn't hold any skill.")

            sb.append("</body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showScriptsList(activeChar: Player, npcId: Int) {
            val npcData = NpcData.getTemplate(npcId)
            if (npcData == null) {
                activeChar.sendMessage("Npc template is unknown for id: $npcId.")
                return
            }

            val sb = StringBuilder(500)
            StringUtil.append(
                sb,
                "<html><body><center><font color=\"LEVEL\">",
                npcData.name,
                " (",
                npcId,
                ")</font></center><br>"
            )

            if (!npcData.eventQuests.isEmpty()) {
                var type: EventType? = null // Used to see if we moved of type.

                // For any type of EventType
                for ((key, value) in npcData.eventQuests) {
                    if (type !== key) {
                        type = key
                        StringUtil.append(sb, "<br><font color=\"LEVEL\">", type!!.name, "</font><br1>")
                    }

                    for (quest in value)
                        StringUtil.append(sb, quest.name, "<br1>")
                }
            } else
                sb.append("This NPC isn't affected by scripts.")

            sb.append("</body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }
    }
}