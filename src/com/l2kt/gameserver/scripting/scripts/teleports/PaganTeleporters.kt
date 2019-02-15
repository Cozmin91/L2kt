package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest

class PaganTeleporters : Quest(-1, "teleports") {
    init {

        addStartNpc(32034, 32035, 32036, 32037, 32039, 32040)
        addTalkId(32034, 32035, 32036, 32037, 32039, 32040)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("Close_Door1", ignoreCase = true))
            DoorData.getDoor(19160001)!!.closeMe()
        else if (event.equals("Close_Door2", ignoreCase = true)) {
            DoorData.getDoor(19160010)!!.closeMe()
            DoorData.getDoor(19160011)!!.closeMe()
        }
        return null
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name) ?: return htmltext

        when (npc.npcId) {
            32034 -> if (st.hasQuestItems(VISITOR_MARK) || st.hasQuestItems(PAGAN_MARK)) {
                DoorData.getDoor(19160001)!!.openMe()
                startQuestTimer("Close_Door1", 10000, npc, player, false)
                htmltext = "FadedMark.htm"
            } else {
                htmltext = "32034-1.htm"
                st.exitQuest(true)
            }

            32035 -> {
                DoorData.getDoor(19160001)!!.openMe()
                startQuestTimer("Close_Door1", 10000, npc, player, false)
                htmltext = "FadedMark.htm"
            }

            32036 -> if (!st.hasQuestItems(PAGAN_MARK))
                htmltext = "32036-1.htm"
            else {
                DoorData.getDoor(19160010)!!.openMe()
                DoorData.getDoor(19160011)!!.openMe()
                startQuestTimer("Close_Door2", 10000, npc, player, false)
                htmltext = "32036-2.htm"
            }

            32037 -> {
                DoorData.getDoor(19160010)!!.openMe()
                DoorData.getDoor(19160011)!!.openMe()
                startQuestTimer("Close_Door2", 10000, npc, player, false)
                htmltext = "FadedMark.htm"
            }

            32039 -> player.teleToLocation(-12766, -35840, -10856, 0)

            32040 -> player.teleToLocation(34962, -49758, -763, 0)
        }
        return htmltext
    }

    companion object {
        // Items
        private const val VISITOR_MARK = 8064
        private const val PAGAN_MARK = 8067
    }
}