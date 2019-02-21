package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.xml.TeleportLocationData
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

class DungeonGatekeeper(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        player.sendPacket(ActionFailed.STATIC_PACKET)

        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        val sealAvariceOwner = SevenSigns.getSealOwner(SealType.AVARICE)
        val sealGnosisOwner = SevenSigns.getSealOwner(SealType.GNOSIS)
        val playerCabal = SevenSigns.getPlayerCabal(player.objectId)
        val winningCabal = SevenSigns.cabalHighestScore

        if (actualCommand.startsWith("necro")) {
            var canPort = true
            if (SevenSigns.isSealValidationPeriod) {
                if (winningCabal === CabalType.DAWN && (playerCabal !== CabalType.DAWN || sealAvariceOwner !== CabalType.DAWN)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN)
                    canPort = false
                } else if (winningCabal === CabalType.DUSK && (playerCabal !== CabalType.DUSK || sealAvariceOwner !== CabalType.DUSK)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK)
                    canPort = false
                } else if (winningCabal === CabalType.NORMAL && playerCabal !== CabalType.NORMAL)
                    canPort = true
                else if (playerCabal === CabalType.NORMAL)
                    canPort = false
            } else {
                if (playerCabal === CabalType.NORMAL)
                    canPort = false
            }

            if (!canPort) {
                val html = NpcHtmlMessage(objectId)
                html.setFile(SevenSigns.SEVEN_SIGNS_HTML_PATH + "necro_no.htm")
                player.sendPacket(html)
            } else {
                doTeleport(player, Integer.parseInt(st.nextToken()))
                player.setIsIn7sDungeon(true)
            }
        } else if (actualCommand.startsWith("cata")) {
            var canPort = true
            if (SevenSigns.isSealValidationPeriod) {
                if (winningCabal === CabalType.DAWN && (playerCabal !== CabalType.DAWN || sealGnosisOwner !== CabalType.DAWN)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN)
                    canPort = false
                } else if (winningCabal === CabalType.DUSK && (playerCabal !== CabalType.DUSK || sealGnosisOwner !== CabalType.DUSK)) {
                    player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK)
                    canPort = false
                } else if (winningCabal === CabalType.NORMAL && playerCabal !== CabalType.NORMAL)
                    canPort = true
                else if (playerCabal === CabalType.NORMAL)
                    canPort = false
            } else {
                if (playerCabal === CabalType.NORMAL)
                    canPort = false
            }

            if (!canPort) {
                val html = NpcHtmlMessage(objectId)
                html.setFile(SevenSigns.SEVEN_SIGNS_HTML_PATH + "cata_no.htm")
                player.sendPacket(html)
            } else {
                doTeleport(player, Integer.parseInt(st.nextToken()))
                player.setIsIn7sDungeon(true)
            }
        } else if (actualCommand.startsWith("exit")) {
            doTeleport(player, Integer.parseInt(st.nextToken()))
            player.setIsIn7sDungeon(false)
        } else if (actualCommand.startsWith("goto")) {
            doTeleport(player, Integer.parseInt(st.nextToken()))
        } else
            super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/teleporter/$filename.htm"
    }

    private fun doTeleport(player: Player, `val`: Int) {
        val list = TeleportLocationData.getTeleportLocation(`val`)
        if (list != null && !player.isAlikeDead)
            player.teleToLocation(list, 20)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }
}