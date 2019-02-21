package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.xml.MultisellData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.olympiad.CompetitionType
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager
import com.l2kt.gameserver.model.olympiad.OlympiadManager
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.ExHeroList
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.concurrent.CopyOnWriteArrayList

class OlympiadManagerNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        // Only used by Olympiad managers. Monument of Heroes don't use "Chat" bypass.
        var filename = "noble"

        if (`val` > 0)
            filename = "noble_$`val`"

        return "$filename.htm"
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        val npcId = template.npcId
        var filename = getHtmlPath(npcId, `val`)

        when (npcId) {
            31688 // Olympiad managers
            -> if (player.isNoble && `val` == 0)
                filename = "noble_main.htm"

            31690 // Monuments of Heroes
                , 31769, 31770, 31771, 31772 -> if (player.isHero || Hero.isInactiveHero(player.objectId))
                filename = "hero_main.htm"
            else
                filename = "hero_main2.htm"
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/olympiad/$filename")

        // Hidden option for players who are in inactive mode.
        if (filename === "hero_main.htm") {
            var hiddenText = ""
            if (Hero.isInactiveHero(player.objectId))
                hiddenText = "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>"

            html.replace("%hero%", hiddenText)
        }
        html.replace("%objectId%", objectId)
        player.sendPacket(html)

        // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("OlympiadNoble")) {
            val html = NpcHtmlMessage(objectId)
            if (player.isCursedWeaponEquipped) {
                html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_cw.htm")
                player.sendPacket(html)
                return
            }

            if (player.classIndex != 0) {
                html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_sub.htm")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            }

            if (!player.isNoble || player.classId.level() < 3) {
                html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_cant_thirdclass.htm")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            }

            val `val` = Integer.parseInt(command.substring(14))
            when (`val`) {
                1 // Unregister
                -> OlympiadManager.unRegisterNoble(player)

                2 // Show waiting list
                -> {
                    val nonClassed = OlympiadManager.registeredNonClassBased.size
                    val classed = OlympiadManager.registeredClassBased.size

                    html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_registered.htm")
                    html.replace("%listClassed%", classed)
                    html.replace("%listNonClassed%", nonClassed)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                3 // There are %points% Grand Olympiad points granted for this event.
                -> {
                    val points = Olympiad.getNoblePoints(player.objectId)
                    html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_points1.htm")
                    html.replace("%points%", points)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                4 // register non classed based
                -> OlympiadManager.registerNoble(player, CompetitionType.NON_CLASSED)

                5 // register classed based
                -> OlympiadManager.registerNoble(player, CompetitionType.CLASSED)

                6 // request tokens reward
                -> {
                    html.setFile(
                        Olympiad.OLYMPIAD_HTML_PATH + if (Olympiad.getNoblessePasses(
                                player,
                                false
                            ) > 0
                        ) "noble_settle.htm" else "noble_nopoints2.htm"
                    )
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                7 // Rewards
                -> MultisellData.separateAndSend("102", player, this, false)

                10 // Give tokens to player
                -> player.addItem("Olympiad", GATE_PASS, Olympiad.getNoblessePasses(player, true), player, true)

                else -> {
                }
            }
        } else if (command.startsWith("Olympiad")) {
            val `val` = Integer.parseInt(command.substring(9, 10))

            val html = NpcHtmlMessage(objectId)
            when (`val`) {
                2 // Show rank for a specific class, example >> Olympiad 1_88
                -> {
                    val classId = Integer.parseInt(command.substring(11))
                    if (classId >= 88 && classId <= 118) {
                        val names = Olympiad.getClassLeaderBoard(classId)
                        html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "noble_ranking.htm")

                        var index = 1
                        for (name in names) {
                            html.replace("%place$index%", index)
                            html.replace("%rank$index%", name)

                            index++
                            if (index > 10)
                                break
                        }

                        while (index <= 10) {
                            html.replace("%place$index%", "")
                            html.replace("%rank$index%", "")
                            index++
                        }

                        html.replace("%objectId%", objectId)
                        player.sendPacket(html)
                    }
                }

                3 // Spectator overview
                -> {
                    html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "olympiad_observe_list.htm")

                    var i = 0

                    val sb = StringBuilder(2000)
                    for (task in OlympiadGameManager.olympiadTasks.filterNotNull()) {
                        StringUtil.append(sb, "<a action=\"bypass arenachange ", i, "\">Arena ", ++i, "&nbsp;")

                        if (task.isGameStarted) {
                            if (task.isInTimerTime)
                                StringUtil.append(sb, "(&$907;)") // Counting In Progress
                            else if (task.isBattleStarted)
                                StringUtil.append(sb, "(&$829;)") // In Progress
                            else
                                StringUtil.append(sb, "(&$908;)") // Terminate

                            StringUtil.append(
                                sb,
                                "&nbsp;",
                                task.game!!.playerNames[0],
                                "&nbsp; : &nbsp;",
                                task.game!!.playerNames[1]
                            )
                        } else
                            StringUtil.append(sb, "(&$906;)</td><td>&nbsp;") // Initial State

                        StringUtil.append(sb, "</a><br>")
                    }
                    html.replace("%list%", sb.toString())
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                4 // Send heroes list.
                -> player.sendPacket(ExHeroList())

                5 // Hero pending state.
                -> if (Hero.isInactiveHero(player.objectId)) {
                    html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "hero_confirm.htm")
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                6 // Hero confirm action.
                -> if (Hero.isInactiveHero(player.objectId)) {
                    if (player.isSubClassActive || player.level < 76) {
                        player.sendMessage("You may only become an hero on a main class whose level is 75 or more.")
                        return
                    }

                    Hero.activateHero(player)
                }

                7 // Main panel
                -> {
                    html.setFile(Olympiad.OLYMPIAD_HTML_PATH + "hero_main.htm")

                    var hiddenText = ""
                    if (Hero.isInactiveHero(player.objectId))
                        hiddenText =
                            "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>"

                    html.replace("%hero%", hiddenText)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                else -> {
                }
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun onSpawn() {
        super.onSpawn()

        if (npcId == 31688)
            _managers.add(this)
    }

    override fun onDecay() {
        _managers.remove(this)
        super.onDecay()
    }

    companion object {
        private val _managers = CopyOnWriteArrayList<OlympiadManagerNpc>()

        private const val GATE_PASS = 6651

        val instances: List<OlympiadManagerNpc>
            get() = _managers
    }
}