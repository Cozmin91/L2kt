package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.HennaInfo
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.UserInfo

/**
 * Custom class allowing you to choose your class.<br></br>
 * <br></br>
 * You can customize class rewards as needed items. Check npc.properties for more informations.<br></br>
 * This NPC type got 2 differents ways to level:
 *
 *  * the normal one, where you have to be at least of the good level.<br></br>
 * NOTE : you have to take 1st class then 2nd, if you try to take 2nd directly it won't work.
 *  * the "allow_entire_tree" version, where you can take class depending of your current path.<br></br>
 * NOTE : you don't need to be of the good level.
 *
 * Added to the "change class" function, this NPC can noblesse and give available skills (related to your current class and level).
 */
class ClassMaster(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun showChatWindow(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
        var filename = "data/html/classmaster/disabled.htm"

        if (Config.ALLOW_CLASS_MASTERS)
            filename = "data/html/classmaster/$npcId.htm"

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (!Config.ALLOW_CLASS_MASTERS)
            return

        if (command.startsWith("1stClass"))
            showHtmlMenu(player, objectId, 1)
        else if (command.startsWith("2ndClass"))
            showHtmlMenu(player, objectId, 2)
        else if (command.startsWith("3rdClass"))
            showHtmlMenu(player, objectId, 3)
        else if (command.startsWith("change_class")) {
            val `val` = Integer.parseInt(command.substring(13))

            if (checkAndChangeClass(player, `val`)) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/classmaster/ok.htm")
                html.replace("%name%", PlayerData.getClassNameById(`val`))
                player.sendPacket(html)
            }
        } else if (command.startsWith("become_noble")) {
            val html = NpcHtmlMessage(objectId)

            if (!player.isNoble) {
                player.setNoble(true, true)
                player.sendPacket(UserInfo(player))
                html.setFile("data/html/classmaster/nobleok.htm")
                player.sendPacket(html)
            } else {
                html.setFile("data/html/classmaster/alreadynoble.htm")
                player.sendPacket(html)
            }
        } else if (command.startsWith("learn_skills"))
            player.rewardSkills()
        else
            super.onBypassFeedback(player, command)
    }

    private fun showHtmlMenu(player: Player, objectId: Int, level: Int) {
        val html = NpcHtmlMessage(objectId)

        if (!Config.CLASS_MASTER_SETTINGS.isAllowed(level)) {
            val sb = StringBuilder(100)
            sb.append("<html><body>")

            when (player.classId.level()) {
                0 -> if (Config.CLASS_MASTER_SETTINGS.isAllowed(1))
                    sb.append("Come back here when you reached level 20 to change your class.<br>")
                else if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
                    sb.append("Come back after your first occupation change.<br>")
                else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                    sb.append("Come back after your second occupation change.<br>")
                else
                    sb.append("I can't change your occupation.<br>")

                1 -> if (Config.CLASS_MASTER_SETTINGS.isAllowed(2))
                    sb.append("Come back here when you reached level 40 to change your class.<br>")
                else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                    sb.append("Come back after your second occupation change.<br>")
                else
                    sb.append("I can't change your occupation.<br>")

                2 -> if (Config.CLASS_MASTER_SETTINGS.isAllowed(3))
                    sb.append("Come back here when you reached level 76 to change your class.<br>")
                else
                    sb.append("I can't change your occupation.<br>")

                3 -> sb.append("There is no class change available for you anymore.<br>")
            }
            sb.append("</body></html>")
            html.setHtml(sb.toString())
        } else {
            val currentClassId = player.classId
            if (currentClassId.level() >= level)
                html.setFile("data/html/classmaster/nomore.htm")
            else {
                val minLevel = getMinLevel(currentClassId.level())
                if (player.level >= minLevel || Config.ALLOW_ENTIRE_TREE) {
                    val menu = StringBuilder(100)
                    for (cid in ClassId.VALUES) {
                        if (cid.level() != level)
                            continue

                        if (validateClassId(currentClassId, cid))
                            StringUtil.append(
                                menu,
                                "<a action=\"bypass -h npc_%objectId%_change_class ",
                                cid.id,
                                "\">",
                                PlayerData.getClassNameById(cid.id),
                                "</a><br>"
                            )
                    }

                    if (menu.length > 0) {
                        html.setFile("data/html/classmaster/template.htm")
                        html.replace("%name%", PlayerData.getClassNameById(currentClassId.id))
                        html.replace("%menu%", menu.toString())
                    } else {
                        html.setFile("data/html/classmaster/comebacklater.htm")
                        html.replace("%level%", getMinLevel(level - 1))
                    }
                } else {
                    if (minLevel < Integer.MAX_VALUE) {
                        html.setFile("data/html/classmaster/comebacklater.htm")
                        html.replace("%level%", minLevel)
                    } else
                        html.setFile("data/html/classmaster/nomore.htm")
                }
            }
        }

        html.replace("%objectId%", objectId)
        html.replace("%req_items%", getRequiredItems(level))
        player.sendPacket(html)
    }

    private fun checkAndChangeClass(player: Player, `val`: Int): Boolean {
        val currentClassId = player.classId
        if (getMinLevel(currentClassId.level()) > player.level && !Config.ALLOW_ENTIRE_TREE)
            return false

        if (!validateClassId(currentClassId, `val`))
            return false

        val newJobLevel = currentClassId.level() + 1

        // Weight/Inventory check
        if (!Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).isEmpty()) {
            if (player.weightPenalty > 2) {
                player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT)
                return false
            }
        }

        val neededItems = Config.CLASS_MASTER_SETTINGS.getRequiredItems(newJobLevel)

        // check if player have all required items for class transfer
        for (item in neededItems) {
            if (player.inventory!!.getInventoryItemCount(item.id, -1) < item.value) {
                player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)
                return false
            }
        }

        // get all required items for class transfer
        for (item in neededItems) {
            if (!player.destroyItemByItemId("ClassMaster", item.id, item.value, player, true))
                return false
        }

        // reward player with items
        for (item in Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel))
            player.addItem("ClassMaster", item.id, item.value, player, true)

        player.setClassId(`val`)

        if (player.isSubClassActive)
            player.subClasses[player.classIndex]!!.classId = player.activeClass
        else
            player.baseClass = player.activeClass

        player.sendPacket(HennaInfo(player))
        player.broadcastUserInfo()
        return true
    }

    /**
     * @param level - current skillId level (0 - start, 1 - first, etc)
     * @return minimum player level required for next class transfer
     */
    private fun getMinLevel(level: Int): Int {
        when (level) {
            0 -> return 20
            1 -> return 40
            2 -> return 76
            else -> return Integer.MAX_VALUE
        }
    }

    /**
     * Returns true if class change is possible
     * @param oldCID current player ClassId
     * @param val new class index
     * @return
     */
    private fun validateClassId(oldCID: ClassId, `val`: Int): Boolean {
        try {
            return validateClassId(oldCID, ClassId.VALUES[`val`])
        } catch (e: Exception) {
            // possible ArrayOutOfBoundsException
        }

        return false
    }

    /**
     * Returns true if class change is possible
     * @param oldCID current player ClassId
     * @param newCID new ClassId
     * @return true if class change is possible
     */
    private fun validateClassId(oldCID: ClassId, newCID: ClassId?): Boolean {
        if (newCID == null)
            return false

        if (oldCID === newCID.parent)
            return true

        return if (Config.ALLOW_ENTIRE_TREE && newCID.childOf(oldCID)) true else false

    }

    private fun getRequiredItems(level: Int): String {
        val neededItems = Config.CLASS_MASTER_SETTINGS.getRequiredItems(level)
        if (neededItems == null || neededItems.isEmpty())
            return "<tr><td>none</td></r>"

        val sb = StringBuilder()
        for (item in neededItems)
            StringUtil.append(
                sb,
                "<tr><td><font color=\"LEVEL\">",
                item.value,
                "</font></td><td>",
                ItemTable.getTemplate(item.id)!!.name,
                "</td></tr>"
            )

        return sb.toString()
    }
}