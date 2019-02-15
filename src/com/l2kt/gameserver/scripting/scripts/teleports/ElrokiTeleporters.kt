package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class ElrokiTeleporters : Quest(-1, "teleports") {
    init {

        addStartNpc(32111, 32112)
        addTalkId(32111, 32112)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        if (npc.npcId == 32111) {
            if (player.isInCombat)
                return "32111-no.htm"

            player.teleToLocation(4990, -1879, -3178, 0)
        } else
            player.teleToLocation(7557, -5513, -3221, 0)

        return null
    }
}