package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q317_CatchTheWind : Quest(317, "Catch the Wind") {
    init {

        setItemsIds(WIND_SHARD)

        addStartNpc(30361) // Rizraell
        addTalkId(30361)

        addKillId(20036, 20044)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30361-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30361-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 18) "30361-02.htm" else "30361-03.htm"

            Quest.STATE_STARTED -> {
                val shards = st.getQuestItemsCount(WIND_SHARD)
                if (shards == 0)
                    htmltext = "30361-05.htm"
                else {
                    htmltext = "30361-07.htm"
                    st.takeItems(WIND_SHARD, -1)
                    st.rewardItems(57, 40 * shards + if (shards >= 10) 2988 else 0)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(WIND_SHARD, 1, 0, 500000)

        return null
    }

    companion object {
        private val qn = "Q317_CatchTheWind"

        // Item
        private val WIND_SHARD = 1078
    }
}