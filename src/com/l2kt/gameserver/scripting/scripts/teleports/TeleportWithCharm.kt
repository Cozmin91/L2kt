package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class TeleportWithCharm : Quest(-1, "teleports") {
    init {

        addStartNpc(WHIRPY, TAMIL)
        addTalkId(WHIRPY, TAMIL)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(name)
        var htmltext = ""

        val npcId = npc.npcId
        if (npcId == WHIRPY) {
            if (st!!.getQuestItemsCount(DWARF_GATEKEEPER_TOKEN) >= 1) {
                st.takeItems(DWARF_GATEKEEPER_TOKEN, 1)
                player.teleToLocation(-80826, 149775, -3043, 0)
            } else
                htmltext = "30540-01.htm"
        } else if (npcId == TAMIL) {
            if (st!!.getQuestItemsCount(ORC_GATEKEEPER_CHARM) >= 1) {
                st.takeItems(ORC_GATEKEEPER_CHARM, 1)
                player.teleToLocation(-80826, 149775, -3043, 0)
            } else
                htmltext = "30576-01.htm"
        }

        st!!.exitQuest(true)
        return htmltext
    }

    companion object {
        private const val WHIRPY = 30540
        private const val TAMIL = 30576

        private const val ORC_GATEKEEPER_CHARM = 1658
        private const val DWARF_GATEKEEPER_TOKEN = 1659
    }
}