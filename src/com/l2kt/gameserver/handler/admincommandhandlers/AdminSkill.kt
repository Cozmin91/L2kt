package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class AdminSkill : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command == "admin_show_skills")
            showMainPage(activeChar)
        else if (command.startsWith("admin_remove_skills")) {
            try {
                removeSkillsPage(activeChar, Integer.parseInt(command.substring(20)))
            } catch (e: Exception) {
                removeSkillsPage(activeChar, 1)
            }

        } else if (command.startsWith("admin_skill_list")) {
            AdminHelpPage.showHelpPage(activeChar, "skills.htm")
        } else if (command.startsWith("admin_skill_index")) {
            try {
                val `val` = command.substring(18)
                AdminHelpPage.showHelpPage(activeChar, "skills/$`val`.htm")
            } catch (e: Exception) {
            }

        } else if (command.startsWith("admin_add_skill")) {
            try {
                val `val` = command.substring(15)
                adminAddSkill(activeChar, `val`)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //add_skill <skill_id> <level>")
            }

        } else if (command.startsWith("admin_remove_skill")) {
            try {
                val id = command.substring(19)
                val idval = Integer.parseInt(id)
                adminRemoveSkill(activeChar, idval)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //remove_skill <skill_id>")
            }

        } else if (command == "admin_get_skills") {
            adminGetSkills(activeChar)
        } else if (command == "admin_reset_skills")
            adminResetSkills(activeChar)
        else if (command == "admin_give_all_skills")
            adminGiveAllSkills(activeChar)
        else if (command == "admin_remove_all_skills") {
            if (activeChar.target is Player) {
                val player = activeChar.target as Player

                for (skill in player.skills.values)
                    player.removeSkill(skill.id, true)

                activeChar.sendMessage("You removed all skills from " + player.name + ".")
                if (player != activeChar)
                    player.sendMessage("Admin removed all skills from you.")

                player.sendSkillList()
            }
        } else if (command.startsWith("admin_add_clan_skill")) {
            try {
                val `val` = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                adminAddClanSkill(activeChar, Integer.parseInt(`val`[1]), Integer.parseInt(`val`[2]))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>")
            }

        } else if (command.startsWith("admin_st")) {
            try {
                val st = StringTokenizer(command)
                st.nextToken()

                val id = Integer.parseInt(st.nextToken())
                adminTestSkill(activeChar, id)
            } catch (e: Exception) {
                activeChar.sendMessage("Used to test skills' visual effect, format : //st <ID>")
            }

        }

        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 10

        private val ADMIN_COMMANDS = arrayOf(
            "admin_show_skills",
            "admin_remove_skills",
            "admin_skill_list",
            "admin_skill_index",
            "admin_add_skill",
            "admin_remove_skill",
            "admin_get_skills",
            "admin_reset_skills",
            "admin_give_all_skills",
            "admin_remove_all_skills",
            "admin_add_clan_skill",
            "admin_st"
        )

        private val ADMIN_SKILLS = ArrayList<L2Skill>()

        private fun adminTestSkill(activeChar: Player, id: Int) {
            val player: Creature
            val target = activeChar.target

            if (target == null || target !is Creature)
                player = activeChar
            else
                player = target

            player.broadcastPacket(MagicSkillUse(activeChar, player, id, 1, 1, 1))
        }

        /**
         * This function will give all the skills that the target can learn at his/her level
         * @param activeChar The GM char.
         */
        private fun adminGiveAllSkills(activeChar: Player) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            player.rewardSkills()
            activeChar.sendMessage("You gave all available skills to " + player.name + ".")
        }

        private fun removeSkillsPage(activeChar: Player, page: Int) {
            val target = activeChar.target
            var player: Player? = null
            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                return
            }

            var skills: List<L2Skill> = ArrayList(player.skills.values)

            val max = MathUtil.countPagesNumber(skills.size, PAGE_LIMIT)

            skills = skills.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, skills.size))

            val sb = StringBuilder(3000)
            StringUtil.append(
                sb,
                "<html><body><table width=270><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=180><center>Delete Skills Menu</center></td><td width=45><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br><br><center>Editing <font color=\"LEVEL\">",
                player.name,
                "</font>, ",
                player.template.className,
                " lvl ",
                player.level,
                ".<br><center><table width=270><tr>"
            )

            for (i in 0 until max) {
                val pagenr = i + 1
                if (page == pagenr)
                    StringUtil.append(sb, "<td>", pagenr, "</td>")
                else
                    StringUtil.append(
                        sb,
                        "<td><a action=\"bypass -h admin_remove_skills ",
                        pagenr,
                        "\">",
                        pagenr,
                        "</a></td>"
                    )
            }

            sb.append("</tr></table></center><br><table width=270><tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>")

            for (skill in skills)
                StringUtil.append(
                    sb,
                    "<tr><td width=80><a action=\"bypass -h admin_remove_skill ",
                    skill.id,
                    "\">",
                    skill.name,
                    "</a></td><td width=60>",
                    skill.level,
                    "</td><td width=40>",
                    skill.id,
                    "</td></tr>"
                )

            sb.append("</table><br><center><table width=200><tr><td width=50 align=right>Id: </td><td><edit var=\"id_to_remove\" width=55></td><td width=100><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill \$id_to_remove\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"></td></tr><tr><td></td><td></td><td><button value=\"Back to stats\" action=\"bypass -h admin_current_player\" width=95 height=21 back=\"bigbutton_over\" fore=\"bigbutton\"></td></tr></table></center></body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showMainPage(activeChar: Player) {
            val target = activeChar.target
            var player: Player? = null
            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/charskills.htm")
            html.replace("%name%", player.name)
            html.replace("%level%", player.level)
            html.replace("%class%", player.template.className)
            activeChar.sendPacket(html)
        }

        private fun adminGetSkills(activeChar: Player) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            if (player == activeChar)
                player.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF)
            else {
                ADMIN_SKILLS.addAll(activeChar.skills.values)

                for (skill in ADMIN_SKILLS)
                    activeChar.removeSkill(skill.id, false)

                for (skill in player.skills.values)
                    activeChar.addSkill(skill, false)

                activeChar.sendMessage("You ninjaed " + player.name + "'s skills list.")
                activeChar.sendSkillList()
            }
        }

        private fun adminResetSkills(activeChar: Player) {
            if (ADMIN_SKILLS.isEmpty())
                activeChar.sendMessage("Ninja first skills of someone to use that command.")
            else {
                for (skill in activeChar.skills.values)
                    activeChar.removeSkill(skill.id, false)

                for (skill in ADMIN_SKILLS)
                    activeChar.addSkill(skill, false)

                activeChar.sendMessage("All your skills have been returned back.")
                activeChar.sendSkillList()

                ADMIN_SKILLS.clear()
            }
        }

        private fun adminAddSkill(activeChar: Player, `val`: String) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else {
                showMainPage(activeChar)
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            val st = StringTokenizer(`val`)
            if (st.countTokens() != 2)
                showMainPage(activeChar)
            else {
                var skill: L2Skill? = null
                try {
                    val id = st.nextToken()
                    val level = st.nextToken()
                    val idval = Integer.parseInt(id)
                    val levelval = Integer.parseInt(level)
                    skill = SkillTable.getInfo(idval, levelval)
                } catch (e: Exception) {
                }

                if (skill != null) {
                    val name = skill.name

                    player.addSkill(skill, true)
                    player.sendMessage("Admin gave you the skill $name.")
                    if (player != activeChar)
                        activeChar.sendMessage("You gave the skill " + name + " to " + player.name + ".")

                    player.sendSkillList()
                } else
                    activeChar.sendMessage("Error: there is no such skill.")

                showMainPage(activeChar) // Back to start
            }
        }

        private fun adminRemoveSkill(activeChar: Player, idval: Int) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return
            }

            val skill = player.removeSkill(idval, true)
            if (skill == null)
                activeChar.sendMessage("Error: there is no such skill.")
            else {
                activeChar.sendMessage("You removed the skill " + skill.name + " from " + player.name + ".")
                if (player != activeChar)
                    player.sendMessage("Admin removed the skill " + skill.name + " from your skills list.")

                player.sendSkillList()
            }

            removeSkillsPage(activeChar, 1)
        }

        private fun adminAddClanSkill(activeChar: Player, id: Int, level: Int) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                showMainPage(activeChar)
                return
            }

            if (!player.isClanLeader) {
                activeChar.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addCharName(
                        player
                    )
                )
                showMainPage(activeChar)
                return
            }

            if (id < 370 || id > 391 || level < 1 || level > 3) {
                activeChar.sendMessage("Usage: //add_clan_skill <skill_id> <level>")
                showMainPage(activeChar)
                return
            }

            val skill = SkillTable.getInfo(id, level)
            if (skill == null) {
                activeChar.sendMessage("Error: there is no such skill.")
                return
            }

            player.clan.addNewSkill(skill)

            activeChar.sendMessage("You gave " + skill.name + " Clan Skill to " + player.clan.name + " clan.")

            showMainPage(activeChar)
            return
        }
    }
}