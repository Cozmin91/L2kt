package com.l2kt.gameserver.handler.admincommandhandlers

import java.util.StringTokenizer

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.CursedWeapon

import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * This class handles following admin commands:
 *
 *  * cw_info : displays cursed weapons status.
 *  * cw_remove : removes a cursed weapon from the world - item id or name must be provided.
 *  * cw_add : adds a cursed weapon into the world - item id or name must be provided. Current target will be the owner.
 *  * cw_goto : teleports GM to the specified cursed weapon (item or player position).
 *
 * >
 */
class AdminCursedWeapons : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        val st = StringTokenizer(command)
        st.nextToken()

        if (command.startsWith("admin_cw_info")) {
            if (!command.contains("menu")) {
                activeChar.sendMessage("====== Cursed Weapons: ======")
                for (cw in CursedWeaponManager.cursedWeapons) {
                    activeChar.sendMessage(cw.name + " (" + cw.itemId + ")")
                    if (cw.isActive) {
                        val milliToStart = cw.timeLeft

                        val numSecs = (milliToStart / 1000 % 60).toDouble()
                        var countDown = (milliToStart / 1000 - numSecs) / 60
                        val numMins = Math.floor(countDown % 60).toInt()
                        countDown = (countDown - numMins) / 60
                        val numHours = Math.floor(countDown % 24).toInt()
                        val numDays = Math.floor((countDown - numHours) / 24).toInt()

                        if (cw.isActivated) {
                            val pl = cw.player
                            activeChar.sendMessage("  Owner: " + (pl?.name ?: "null"))
                            activeChar.sendMessage("  Stored values: karma=" + cw.playerKarma + " PKs=" + cw.playerPkKills)
                            activeChar.sendMessage("  Current stage:" + cw.currentStage)
                            activeChar.sendMessage("  Overall time: $numDays days $numHours hours $numMins min.")
                            activeChar.sendMessage("  Hungry time: " + cw.hungryTime + " min.")
                            activeChar.sendMessage("  Current kills : " + cw.nbKills + " / " + cw.numberBeforeNextStage)
                        } else if (cw.isDropped) {
                            activeChar.sendMessage("  Lying on the ground.")
                            activeChar.sendMessage("  Time remaining: $numDays days $numHours hours $numMins min.")
                        }
                    } else
                        activeChar.sendMessage("  Doesn't exist in the world.")

                    activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER)
                }
            } else {
                val sb = StringBuilder(2000)
                for (cw in CursedWeaponManager.cursedWeapons) {
                    StringUtil.append(sb, "<table width=280><tr><td>Name:</td><td>", cw.name, "</td></tr>")

                    if (cw.isActive) {
                        val milliToStart = cw.timeLeft

                        val numSecs = (milliToStart / 1000 % 60).toDouble()
                        var countDown = (milliToStart / 1000 - numSecs) / 60
                        val numMins = Math.floor(countDown % 60).toInt()
                        countDown = (countDown - numMins) / 60
                        val numHours = Math.floor(countDown % 24).toInt()
                        val numDays = Math.floor((countDown - numHours) / 24).toInt()

                        if (cw.isActivated) {
                            val pl = cw.player
                            StringUtil.append(
                                sb,
                                "<tr><td>Owner:</td><td>",
                                pl?.name ?: "null",
                                "</td></tr><tr><td>Stored values:</td><td>Karma=",
                                cw.playerKarma,
                                " PKs=",
                                cw.playerPkKills,
                                "</td></tr><tr><td>Current stage:</td><td>",
                                cw.currentStage,
                                "</td></tr><tr><td>Overall time:</td><td>",
                                numDays,
                                "d. ",
                                numHours,
                                "h. ",
                                numMins,
                                "m.</td></tr><tr><td>Hungry time:</td><td>",
                                cw.hungryTime,
                                "m.</td></tr><tr><td>Current kills:</td><td>",
                                cw.nbKills,
                                " / ",
                                cw.numberBeforeNextStage,
                                "</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ",
                                cw.itemId,
                                "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ",
                                cw.itemId,
                                "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>"
                            )
                        } else if (cw.isDropped)
                            StringUtil.append(
                                sb,
                                "<tr><td>Position:</td><td>Lying on the ground</td></tr><tr><td>Overall time:</td><td>",
                                numDays,
                                "d. ",
                                numHours,
                                "h. ",
                                numMins,
                                "m.</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ",
                                cw.itemId,
                                "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ",
                                cw.itemId,
                                "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>"
                            )
                    } else
                        StringUtil.append(
                            sb,
                            "<tr><td>Position:</td><td>Doesn't exist.</td></tr><tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add ",
                            cw.itemId,
                            "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td></td></tr>"
                        )

                    sb.append("</table><br>")
                }

                val html = NpcHtmlMessage(0)
                html.setFile("data/html/admin/cwinfo.htm")
                html.replace("%cwinfo%", sb.toString())
                activeChar.sendPacket(html)
            }
        } else {
            try {
                var id = 0

                var parameter = st.nextToken()
                if (parameter.matches("[0-9]*".toRegex()))
                    id = Integer.parseInt(parameter)
                else {
                    parameter = parameter.replace('_', ' ')
                    for (cwp in CursedWeaponManager.cursedWeapons) {
                        if (cwp.name.toLowerCase().contains(parameter.toLowerCase())) {
                            id = cwp.itemId
                            break
                        }
                    }
                }

                val cw = CursedWeaponManager.getCursedWeapon(id)
                if (cw == null) {
                    activeChar.sendMessage("Unknown cursed weapon ID.")
                    return false
                }

                if (command.startsWith("admin_cw_remove "))
                    cw.endOfLife()
                else if (command.startsWith("admin_cw_goto "))
                    cw.goTo(activeChar)
                else if (command.startsWith("admin_cw_add")) {
                    if (cw.isActive)
                        activeChar.sendMessage("This cursed weapon is already active.")
                    else {
                        val target = activeChar.target
                        if (target is Player)
                            target.addItem("AdminCursedWeaponAdd", id, 1, target, true)
                        else
                            activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true)

                        // Start task
                        cw.reActivate(true)
                    }
                } else
                    activeChar.sendMessage("Unknown command.")
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>")
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS =
            arrayOf("admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_add", "admin_cw_info_menu")
    }
}