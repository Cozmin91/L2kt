package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q297_GatekeepersFavor : Quest(297, "Gatekeeper's Favor") {
    init {

        setItemsIds(STARSTONE)

        addStartNpc(30540) // Wirphy
        addTalkId(30540)

        addKillId(20521) // Whinstone Golem
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30540-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30540-01.htm" else "30540-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30540-04.htm"
            else {
                htmltext = "30540-05.htm"
                st.takeItems(STARSTONE, -1)
                st.rewardItems(GATEKEEPER_TOKEN, 2)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(STARSTONE, 1, 20, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q297_GatekeepersFavor"

        // Item
        private val STARSTONE = 1573

        // Reward
        private val GATEKEEPER_TOKEN = 1659
    }
}