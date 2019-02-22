package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q303_CollectArrowheads : Quest(303, "Collect Arrowheads") {
    init {

        setItemsIds(ORCISH_ARROWHEAD)

        addStartNpc(30029) // Minia
        addTalkId(30029)

        addKillId(20361)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30029-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 10) "30029-01.htm" else "30029-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30029-04.htm"
            else {
                htmltext = "30029-05.htm"
                st.takeItems(ORCISH_ARROWHEAD, -1)
                st.rewardItems(57, 1000)
                st.rewardExpAndSp(2000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(ORCISH_ARROWHEAD, 1, 10, 400000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q303_CollectArrowheads"

        // Item
        private val ORCISH_ARROWHEAD = 963
    }
}