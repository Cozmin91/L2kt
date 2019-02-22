package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.NpcSay
import java.util.*

class CastleGatekeeper(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {
    protected var _currentTask: Boolean = false
    private var _delay: Int = 0

    private val delayInSeconds: Int
        get() = if (_delay > 0) _delay / 1000 else 0

    override fun onBypassFeedback(player: Player, command: String) {
        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        if (actualCommand.equals("tele", ignoreCase = true)) {
            if (!_currentTask) {
                if (castle!!.siege.isInProgress) {
                    if (castle!!.siege.controlTowerCount == 0)
                        _delay = 480000
                    else
                        _delay = 30000
                } else
                    _delay = 0

                _currentTask = true
                ThreadPool.schedule(oustAllPlayers(), _delay.toLong())
            }

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/castleteleporter/MassGK-1.htm")
            html.replace("%delay%", delayInSeconds)
            player.sendPacket(html)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        val filename: String
        if (!_currentTask) {
            if (castle!!.siege.isInProgress && castle!!.siege.controlTowerCount == 0)
                filename = "data/html/castleteleporter/MassGK-2.htm"
            else
                filename = "data/html/castleteleporter/MassGK.htm"
        } else
            filename = "data/html/castleteleporter/MassGK-1.htm"

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        html.replace("%delay%", delayInSeconds)
        player.sendPacket(html)
    }

    protected inner class oustAllPlayers : Runnable {
        override fun run() {
            // Make the region talk only during a siege
            if (castle!!.siege.isInProgress) {
                val cs = NpcSay(
                    objectId,
                    1,
                    npcId,
                    "The defenders of " + castle!!.name + " castle have been teleported to the inner castle."
                )
                val region = MapRegionData.getMapRegion(x, y)

                for (player in World.players) {
                    if (region == MapRegionData.getMapRegion(player.x, player.y))
                        player.sendPacket(cs)
                }
            }
            castle!!.oustAllPlayers()
            _currentTask = false
        }
    }
}