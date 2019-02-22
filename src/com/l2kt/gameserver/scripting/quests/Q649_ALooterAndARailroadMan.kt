package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q649_ALooterAndARailroadMan : Quest(649, "A Looter and a Railroad Man") {
    init {

        setItemsIds(THIEF_GUILD_MARK)

        addStartNpc(OBI)
        addTalkId(OBI)

        addKillId(22017, 22018, 22019, 22021, 22022, 22023, 22024, 22026)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32052-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32052-3.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(THIEF_GUILD_MARK) < 200)
                htmltext = "32052-3a.htm"
            else {
                st.takeItems(THIEF_GUILD_MARK, -1)
                st.rewardItems(57, 21698)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 30) "32052-0a.htm" else "32052-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32052-2a.htm"
                else if (cond == 2)
                    htmltext = "32052-2.htm"
            }
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(THIEF_GUILD_MARK, 1, 200, 800000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q649_ALooterAndARailroadMan"

        // Item
        private val THIEF_GUILD_MARK = 8099

        // NPC
        private val OBI = 32052
    }
}