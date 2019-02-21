package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.xml.HennaData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.HennaEquipList
import com.l2kt.gameserver.network.serverpackets.HennaRemoveList

class SymbolMaker(objectID: Int, template: NpcTemplate) : Folk(objectID, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (command == "Draw")
            player.sendPacket(HennaEquipList(player, HennaData.getAvailableHennasFor(player)))
        else if (command == "RemoveList") {
            var hasHennas = false
            for (i in 1..3) {
                if (player.getHenna(i) != null)
                    hasHennas = true
            }

            if (hasHennas)
                player.sendPacket(HennaRemoveList(player))
            else
                player.sendPacket(SystemMessageId.SYMBOL_NOT_FOUND)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        return "data/html/symbolmaker/SymbolMaker.htm"
    }
}