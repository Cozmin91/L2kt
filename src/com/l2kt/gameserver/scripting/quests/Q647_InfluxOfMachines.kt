package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q647_InfluxOfMachines : Quest(647, "Influx of Machines") {
    init {

        setItemsIds(DESTROYED_GOLEM_SHARD)

        addStartNpc(GUTENHAGEN)
        addTalkId(GUTENHAGEN)

        for (i in 22052..22078)
            addKillId(i)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("32069-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32069-06.htm", ignoreCase = true)) {
            st.takeItems(DESTROYED_GOLEM_SHARD, -1)
            st.giveItems(Rnd[4963, 4972], 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 46) "32069-03.htm" else "32069-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32069-04.htm"
                else if (cond == 2)
                    htmltext = "32069-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(DESTROYED_GOLEM_SHARD, 1, 500, 300000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q647_InfluxOfMachines"

        // Item
        private val DESTROYED_GOLEM_SHARD = 8100

        // NPC
        private val GUTENHAGEN = 32069
    }
}