package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SkillCoolTime
import java.util.*

class AdminBuffs : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_getbuffs")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()

            if (st.hasMoreTokens()) {
                val playername = st.nextToken()
                val player = World.getPlayer(playername)
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
                    return false
                }

                var page = 1
                if (st.hasMoreTokens())
                    page = Integer.parseInt(st.nextToken())

                showBuffs(activeChar, player, page)
                return true
            } else if (activeChar.target != null && activeChar.target is Creature) {
                showBuffs(activeChar, activeChar.target as Creature, 1)
                return true
            } else {
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                return false
            }
        } else if (command.startsWith("admin_stopbuff")) {
            try {
                val st = StringTokenizer(command, " ")

                st.nextToken()
                val objectId = Integer.parseInt(st.nextToken())
                val skillId = Integer.parseInt(st.nextToken())

                removeBuff(activeChar, objectId, skillId)
                return true
            } catch (e: Exception) {
                activeChar.sendMessage("Failed removing effect: " + e.message)
                activeChar.sendMessage("Usage: //stopbuff <objectId> <skillId>")
                return false
            }

        } else if (command.startsWith("admin_stopallbuffs")) {
            try {
                val st = StringTokenizer(command, " ")
                st.nextToken()
                val objectId = Integer.parseInt(st.nextToken())
                removeAllBuffs(activeChar, objectId)
                return true
            } catch (e: Exception) {
                activeChar.sendMessage("Failed removing all effects: " + e.message)
                activeChar.sendMessage("Usage: //stopallbuffs <objectId>")
                return false
            }

        } else if (command.startsWith("admin_areacancel")) {
            try {
                val st = StringTokenizer(command, " ")
                st.nextToken()
                val `val` = st.nextToken()
                val radius = Integer.parseInt(`val`)

                for (knownChar in activeChar.getKnownTypeInRadius(Player::class.java, radius))
                    knownChar.stopAllEffects()

                activeChar.sendMessage("All effects canceled within radius $radius.")
                return true
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //areacancel <radius>")
                return false
            }

        } else if (command.startsWith("admin_removereuse")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()

            var player: Player? = null
            if (st.hasMoreTokens()) {
                val name = st.nextToken()

                player = World.getPlayer(name)
                if (player == null) {
                    activeChar.sendMessage("The player $name is not online.")
                    return false
                }
            } else if (activeChar.target is Player)
                player = activeChar.target as Player

            if (player == null) {
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT)
                return false
            }

            player.reuseTimeStamp.clear()
            player.disabledSkills.clear()
            player.sendPacket(SkillCoolTime(player))
            activeChar.sendMessage(player.name + "'s skills reuse time is now cleaned.")
            return true
        } else
            return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 20

        private val ADMIN_COMMANDS =
            arrayOf("admin_getbuffs", "admin_stopbuff", "admin_stopallbuffs", "admin_areacancel", "admin_removereuse")

        fun showBuffs(activeChar: Player, target: Creature, page: Int) {
            val effects = target.allEffects

            if (page > effects.size / PAGE_LIMIT + 1 || page < 1)
                return

            var max = effects.size / PAGE_LIMIT
            if (effects.size > PAGE_LIMIT * max)
                max++

            val sb =
                StringBuilder("<html><table width=\"100%\"><tr><td width=45><button value=\"Main\" action=\"bypass -h admin_admin\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=180><center><font color=\"LEVEL\">Effects of " + target.name + "</font></td><td width=45><button value=\"Back\" action=\"bypass -h admin_current_player\" width=45 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table><br><table width=\"100%\"><tr><td width=160>Skill</td><td width=60>Time Left</td><td width=60>Action</td></tr>")

            val start = (page - 1) * PAGE_LIMIT
            val end = Math.min((page - 1) * PAGE_LIMIT + PAGE_LIMIT, effects.size)

            for (i in start until end) {
                val e = effects[i]
                if (e != null)
                    StringUtil.append(
                        sb,
                        "<tr><td>",
                        e.skill.name,
                        "</td><td>",
                        if (e.skill.isToggle) "toggle" else (e.period - e.time).toString() + "s",
                        "</td><td><a action=\"bypass -h admin_stopbuff ",
                        target.objectId,
                        " ",
                        e.skill.id,
                        "\">Remove</a></td></tr>"
                    )
            }

            sb.append("</table><br><table width=\"100%\" bgcolor=444444><tr>")
            for (x in 0 until max) {
                val pagenr = x + 1
                if (page == pagenr)
                    StringUtil.append(sb, "<td>Page ", pagenr, "</td>")
                else
                    StringUtil.append(
                        sb,
                        "<td><a action=\"bypass -h admin_getbuffs ",
                        target.name,
                        " ",
                        x + 1,
                        "\"> Page ",
                        pagenr,
                        "</a></td>"
                    )
            }

            StringUtil.append(
                sb,
                "</tr></table><br><center><button value=\"Remove All\" action=\"bypass -h admin_stopallbuffs ",
                target.objectId,
                "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></html>"
            )

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun removeBuff(activeChar: Player, objId: Int, skillId: Int) {
            if (skillId < 1)
                return

            val obj = World.getObject(objId)
            if (obj is Creature) {
                val target = obj as Creature?

                for (e in target!!.allEffects) {
                    if (e != null && e.skill.id == skillId) {
                        e.exit()
                        activeChar.sendMessage("Removed " + e.skill.name + " level " + e.skill.level + " from " + target.name + " (" + objId + ")")
                    }
                }
                showBuffs(activeChar, target, 1)
            }
        }

        private fun removeAllBuffs(activeChar: Player, objId: Int) {
            val target = World.getObject(objId)
            if (target is Creature) {
                target.stopAllEffects()
                activeChar.sendMessage("Removed all effects from " + target.name + " (" + objId + ")")
                showBuffs(activeChar, target, 1)
            }
        }
    }
}