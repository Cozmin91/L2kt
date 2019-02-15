package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class ToIVortex : Quest(-1, "teleports") {
    init {

        addStartNpc(30952, 30953, 30954)
        addTalkId(30952, 30953, 30954)
        addFirstTalkId(30952, 30953, 30954)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = ""
        val st = player!!.getQuestState(name)

        if (event.equals("blue", ignoreCase = true)) {
            if (st!!.hasQuestItems(BLUE_STONE)) {
                st.takeItems(BLUE_STONE, 1)
                player.teleToLocation(114097, 19935, 935, 0)
            } else
                htmltext = "no-items.htm"
        } else if (event.equals("green", ignoreCase = true)) {
            if (st!!.hasQuestItems(GREEN_STONE)) {
                st.takeItems(GREEN_STONE, 1)
                player.teleToLocation(110930, 15963, -4378, 0)
            } else
                htmltext = "no-items.htm"
        } else if (event.equals("red", ignoreCase = true)) {
            if (st!!.hasQuestItems(RED_STONE)) {
                st.takeItems(RED_STONE, 1)
                player.teleToLocation(118558, 16659, 5987, 0)
            } else
                htmltext = "no-items.htm"
        }
        st!!.exitQuest(true)
        return htmltext
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var st = player.getQuestState(name)
        if (st == null)
            st = newQuestState(player)

        return npc.npcId.toString() + ".htm"
    }

    companion object {
        private const val GREEN_STONE = 4401
        private const val BLUE_STONE = 4402
        private const val RED_STONE = 4403
    }
}