package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.BufferManager
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

class SchemeBuffer(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        val st = StringTokenizer(command, " ")
        val currentCommand = st.nextToken()

        if (currentCommand.startsWith("menu")) {
            val html = NpcHtmlMessage(0)
            html.setFile(getHtmlPath(npcId, 0))
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (currentCommand.startsWith("cleanup")) {
            player.stopAllEffectsExceptThoseThatLastThroughDeath()

            val summon = player.pet
            summon?.stopAllEffectsExceptThoseThatLastThroughDeath()

            val html = NpcHtmlMessage(0)
            html.setFile(getHtmlPath(npcId, 0))
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (currentCommand.startsWith("heal")) {
            player.setCurrentHpMp(player.maxHp.toDouble(), player.maxMp.toDouble())
            player.currentCp = player.maxCp.toDouble()

            val summon = player.pet
            summon?.setCurrentHpMp(summon.maxHp.toDouble(), summon.maxMp.toDouble())

            val html = NpcHtmlMessage(0)
            html.setFile(getHtmlPath(npcId, 0))
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (currentCommand.startsWith("support")) {
            showGiveBuffsWindow(player)
        } else if (currentCommand.startsWith("givebuffs")) {
            val schemeName = st.nextToken()
            val cost = Integer.parseInt(st.nextToken())

            var target: Creature? = null
            if (st.hasMoreTokens()) {
                val targetType = st.nextToken()
                if (targetType != null && targetType.equals("pet", ignoreCase = true))
                    target = player.pet
            } else
                target = player

            if (target == null)
                player.sendMessage("You don't have a pet.")
            else if (cost == 0 || player.reduceAdena("NPC Buffer", cost, this, true)) {
                for (skillId in BufferManager.getScheme(player.objectId, schemeName))
                    SkillTable.getInfo(skillId, SkillTable.getMaxLevel(skillId))!!.getEffects(this, target)
            }
        } else if (currentCommand.startsWith("editschemes")) {
            showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()))
        } else if (currentCommand.startsWith("skill")) {
            val groupType = st.nextToken()
            val schemeName = st.nextToken()

            val skillId = Integer.parseInt(st.nextToken())
            val page = Integer.parseInt(st.nextToken())

            val skills = BufferManager.getScheme(player.objectId, schemeName)

            if (currentCommand.startsWith("skillselect") && !schemeName.equals("none", ignoreCase = true)) {
                if (skills.size < player.maxBuffCount)
                    skills.add(skillId)
                else
                    player.sendMessage("This scheme has reached the maximum amount of buffs.")
            } else if (currentCommand.startsWith("skillunselect"))
                skills.remove(Integer.valueOf(skillId))

            showEditSchemeWindow(player, groupType, schemeName, page)
        } else if (currentCommand.startsWith("createscheme")) {
            try {
                val schemeName = st.nextToken()
                if (schemeName.length > 14) {
                    player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.")
                    return
                }

                val schemes = BufferManager.getPlayerSchemes(player.objectId)
                if (schemes != null) {
                    if (schemes.size == Config.BUFFER_MAX_SCHEMES) {
                        player.sendMessage("Maximum schemes amount is already reached.")
                        return
                    }

                    if (schemes.containsKey(schemeName)) {
                        player.sendMessage("The scheme name already exists.")
                        return
                    }
                }

                BufferManager.setScheme(player.objectId, schemeName.trim { it <= ' ' }, ArrayList())
                showGiveBuffsWindow(player)
            } catch (e: Exception) {
                player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.")
            }

        } else if (currentCommand.startsWith("deletescheme")) {
            try {
                val schemeName = st.nextToken()
                val schemes = BufferManager.getPlayerSchemes(player.objectId)

                if (schemes != null && schemes.containsKey(schemeName))
                    schemes.remove(schemeName)
            } catch (e: Exception) {
                player.sendMessage("This scheme name is invalid.")
            }

            showGiveBuffsWindow(player)
        }

        super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/mods/buffer/$filename.htm"
    }

    /**
     * Sends an html packet to player with Give Buffs menu info for player and pet, depending on targetType parameter {player, pet}
     * @param player : The player to make checks on.
     */
    private fun showGiveBuffsWindow(player: Player) {
        val sb = StringBuilder(200)

        val schemes = BufferManager.getPlayerSchemes(player.objectId)
        if (schemes == null || schemes.isEmpty())
            sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>")
        else {
            for ((key, value) in schemes) {
                val cost = getFee(value)
                StringUtil.append(
                    sb,
                    "<font color=\"LEVEL\">",
                    key,
                    " [",
                    value.size,
                    " / ",
                    player.maxBuffCount,
                    "]",
                    if (cost > 0) " - cost: " + StringUtil.formatNumber(cost.toLong()) else "",
                    "</font><br1>"
                )
                StringUtil.append(
                    sb,
                    "<a action=\"bypass npc_%objectId%_givebuffs ",
                    key,
                    " ",
                    cost,
                    "\">Use on Me</a>&nbsp;|&nbsp;"
                )
                StringUtil.append(
                    sb,
                    "<a action=\"bypass npc_%objectId%_givebuffs ",
                    key,
                    " ",
                    cost,
                    " pet\">Use on Pet</a>&nbsp;|&nbsp;"
                )
                StringUtil.append(
                    sb,
                    "<a action=\"bypass npc_%objectId%_editschemes Buffs ",
                    key,
                    " 1\">Edit</a>&nbsp;|&nbsp;"
                )
                StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_deletescheme ", key, "\">Delete</a><br>")
            }
        }

        val html = NpcHtmlMessage(0)
        html.setFile(getHtmlPath(npcId, 1))
        html.replace("%schemes%", sb.toString())
        html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    /**
     * This sends an html packet to player with Edit Scheme Menu info. This allows player to edit each created scheme (add/delete skills)
     * @param player : The player to make checks on.
     * @param groupType : The group of skills to select.
     * @param schemeName : The scheme to make check.
     * @param page The page.
     */
    private fun showEditSchemeWindow(player: Player, groupType: String, schemeName: String, page: Int) {
        val html = NpcHtmlMessage(0)
        val schemeSkills = BufferManager.getScheme(player.objectId, schemeName)

        html.setFile(getHtmlPath(npcId, 2))
        html.replace("%schemename%", schemeName)
        html.replace("%count%", schemeSkills.size.toString() + " / " + player.maxBuffCount)
        html.replace("%typesframe%", getTypesFrame(groupType, schemeName))
        html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page))
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    /**
     * @param player : The player to make checks on.
     * @param groupType : The group of skills to select.
     * @param schemeName : The scheme to make check.
     * @param page The page.
     * @return a String representing skills available to selection for a given groupType.
     */
    private fun getGroupSkillList(player: Player, groupType: String, schemeName: String, page: Int): String {
        var page = page
        // Retrieve the entire skills list based on group type.
        var skills = BufferManager.getSkillsIdsByType(groupType)
        if (skills.isEmpty())
            return "That group doesn't contain any skills."

        // Calculate page number.
        val max = MathUtil.countPagesNumber(skills.size, PAGE_LIMIT)
        if (page > max)
            page = max

        // Cut skills list up to page number.
        skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size))

        val schemeSkills = BufferManager.getScheme(player.objectId, schemeName)
        val sb = StringBuilder(skills.size * 150)

        var row = 0
        for (skillId in skills) {
            sb.append(if (row % 2 == 0) "<table width=\"280\" bgcolor=\"000000\"><tr>" else "<table width=\"280\"><tr>")

            if (skillId < 100) {
                if (schemeSkills.contains(skillId))
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill00",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>"
                    )
                else
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill00",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>"
                    )
            } else if (skillId < 1000) {
                if (schemeSkills.contains(skillId))
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill0",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>"
                    )
                else
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill0",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>"
                    )
            } else {
                if (schemeSkills.contains(skillId))
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>"
                    )
                else
                    StringUtil.append(
                        sb,
                        "<td height=40 width=40><img src=\"icon.skill",
                        skillId,
                        "\" width=32 height=32></td><td width=190>",
                        SkillTable.getInfo(skillId, 1)!!.name,
                        "<br1><font color=\"B09878\">",
                        BufferManager.getAvailableBuff(skillId)!!.description,
                        "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ",
                        groupType,
                        " ",
                        schemeName,
                        " ",
                        skillId,
                        " ",
                        page,
                        "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>"
                    )
            }

            sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>")
            row++
        }

        // Build page footer.
        sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>")

        if (page > 1)
            StringUtil.append(
                sb,
                "<td align=left width=70><a action=\"bypass npc_" + objectId + "_editschemes ",
                groupType,
                " ",
                schemeName,
                " ",
                page - 1,
                "\">Previous</a></td>"
            )
        else
            StringUtil.append(sb, "<td align=left width=70>Previous</td>")

        StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>")

        if (page < max)
            StringUtil.append(
                sb,
                "<td align=right width=70><a action=\"bypass npc_" + objectId + "_editschemes ",
                groupType,
                " ",
                schemeName,
                " ",
                page + 1,
                "\">Next</a></td>"
            )
        else
            StringUtil.append(sb, "<td align=right width=70>Next</td>")

        sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>")

        return sb.toString()
    }

    companion object {
        private val PAGE_LIMIT = 6

        /**
         * @param groupType : The group of skills to select.
         * @param schemeName : The scheme to make check.
         * @return a string representing all groupTypes available. The group currently on selection isn't linkable.
         */
        private fun getTypesFrame(groupType: String, schemeName: String): String {
            val sb = StringBuilder(500)
            sb.append("<table>")

            var count = 0
            for (type in BufferManager.skillTypes) {
                if (count == 0)
                    sb.append("<tr>")

                if (groupType.equals(type, ignoreCase = true))
                    StringUtil.append(sb, "<td width=65>", type, "</td>")
                else
                    StringUtil.append(
                        sb,
                        "<td width=65><a action=\"bypass npc_%objectId%_editschemes ",
                        type,
                        " ",
                        schemeName,
                        " 1\">",
                        type,
                        "</a></td>"
                    )

                count++
                if (count == 4) {
                    sb.append("</tr>")
                    count = 0
                }
            }

            if (!sb.toString().endsWith("</tr>"))
                sb.append("</tr>")

            sb.append("</table>")

            return sb.toString()
        }

        /**
         * @param list : A list of skill ids.
         * @return a global fee for all skills contained in list.
         */
        private fun getFee(list: ArrayList<Int>): Int {
            if (Config.BUFFER_STATIC_BUFF_COST > 0)
                return list.size * Config.BUFFER_STATIC_BUFF_COST

            var fee = 0
            for (sk in list)
                fee += BufferManager.getAvailableBuff(sk)!!.value

            return fee
        }
    }
}