package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import java.util.*

class NewbieTravelToken : Quest(-1, "teleports") {
    init {
        data["30600"] = intArrayOf(12160, 16554, -4583) // DE
        data["30601"] = intArrayOf(115594, -177993, -912) // DW
        data["30599"] = intArrayOf(45470, 48328, -3059) // EV
        data["30602"] = intArrayOf(-45067, -113563, -199) // OV
        data["30598"] = intArrayOf(-84053, 243343, -3729) // TI
    }

    init {

        addStartNpc(30598, 30599, 30600, 30601, 30602)
        addTalkId(30598, 30599, 30600, 30601, 30602)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var st = player?.getQuestState(name)
        if (st == null)
            st = newQuestState(player!!)

        if (data.containsKey(event)) {
            val x = data[event]!![0]
            val y = data[event]!![1]
            val z = data[event]!![2]

            if (st!!.getQuestItemsCount(TOKEN) != 0) {
                st.takeItems(TOKEN, 1)
                st.player!!.teleToLocation(x, y, z, 0)
            } else
                return "notoken.htm"
        }
        st!!.exitQuest(true)
        return super.onAdvEvent(event, npc, player)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name)
        val npcId = npc.npcId

        if (player.level >= 20) {
            htmltext = "wronglevel.htm"
            st!!.exitQuest(true)
        } else
            htmltext = npcId.toString() + ".htm"

        return htmltext
    }

    companion object {
        private val data = HashMap<String, IntArray>()

        private const val TOKEN = 8542
    }
}