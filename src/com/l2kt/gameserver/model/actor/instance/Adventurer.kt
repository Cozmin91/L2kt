package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ExQuestInfo

/**
 * @author LBaldi
 */
class Adventurer(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("raidInfo")) {
            val bossLevel = Integer.parseInt(command.substring(9).trim { it <= ' ' })
            var filename = "data/html/adventurer_guildsman/raid_info/info.htm"
            if (bossLevel != 0)
                filename = "data/html/adventurer_guildsman/raid_info/level$bossLevel.htm"

            showChatWindow(player, filename)
        } else if (command.equals("questlist", ignoreCase = true))
            player.sendPacket(ExQuestInfo.STATIC_PACKET)
        else
            super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""

        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/adventurer_guildsman/$filename.htm"
    }
}