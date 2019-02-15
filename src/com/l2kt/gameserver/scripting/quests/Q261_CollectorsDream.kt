package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q261_CollectorsDream : Quest(261, "Collector's Dream") {
    init {

        setItemsIds(GIANT_SPIDER_LEG)

        addStartNpc(30222) // Alshupes
        addTalkId(30222)

        addKillId(20308, 20460, 20466)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30222-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30222-01.htm" else "30222-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 2) {
                htmltext = "30222-05.htm"
                st.takeItems(GIANT_SPIDER_LEG, -1)
                st.rewardItems(57, 1000)
                st.rewardExpAndSp(2000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "30222-04.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(GIANT_SPIDER_LEG, 1, 8))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q261_CollectorsDream"

        // Items
        private val GIANT_SPIDER_LEG = 1087
    }
}