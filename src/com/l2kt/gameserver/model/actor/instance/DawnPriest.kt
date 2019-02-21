package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

class DawnPriest(objectId: Int, template: NpcTemplate) : SignsPriest(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("Chat"))
            showChatWindow(player)
        else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)

        var filename = SevenSigns.SEVEN_SIGNS_HTML_PATH

        val winningCabal = SevenSigns.cabalHighestScore

        when (SevenSigns.getPlayerCabal(player.objectId)) {
            SevenSigns.CabalType.DAWN -> if (SevenSigns.isCompResultsPeriod)
                filename += "dawn_priest_5.htm"
            else if (SevenSigns.isRecruitingPeriod)
                filename += "dawn_priest_6.htm"
            else if (SevenSigns.isSealValidationPeriod) {
                if (winningCabal === CabalType.DAWN) {
                    if (winningCabal !== SevenSigns.getSealOwner(SealType.GNOSIS))
                        filename += "dawn_priest_2c.htm"
                    else
                        filename += "dawn_priest_2a.htm"
                } else if (winningCabal === CabalType.NORMAL)
                    filename += "dawn_priest_2d.htm"
                else
                    filename += "dawn_priest_2b.htm"
            } else
                filename += "dawn_priest_1b.htm"

            SevenSigns.CabalType.DUSK -> if (SevenSigns.isSealValidationPeriod)
                filename += "dawn_priest_3a.htm"
            else
                filename += "dawn_priest_3b.htm"

            else -> if (SevenSigns.isCompResultsPeriod)
                filename += "dawn_priest_5.htm"
            else if (SevenSigns.isRecruitingPeriod)
                filename += "dawn_priest_6.htm"
            else if (SevenSigns.isSealValidationPeriod) {
                if (winningCabal === CabalType.DAWN)
                    filename += "dawn_priest_4.htm"
                else if (winningCabal === CabalType.NORMAL)
                    filename += "dawn_priest_2d.htm"
                else
                    filename += "dawn_priest_2b.htm"
            } else
                filename += "dawn_priest_1a.htm"
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }
}