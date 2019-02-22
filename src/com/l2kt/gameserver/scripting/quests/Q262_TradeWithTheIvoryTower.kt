package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q262_TradeWithTheIvoryTower : Quest(262, "Trade with the Ivory Tower") {
    init {

        setItemsIds(FUNGUS_SAC)

        addStartNpc(30137) // Vollodos
        addTalkId(30137)

        addKillId(20400, 20007)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30137-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 8) "30137-01.htm" else "30137-02.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(FUNGUS_SAC) < 10)
                htmltext = "30137-04.htm"
            else {
                htmltext = "30137-05.htm"
                st.takeItems(FUNGUS_SAC, -1)
                st.rewardItems(57, 3000)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(FUNGUS_SAC, 1, 10, if (npc.npcId == 20400) 400000 else 300000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q262_TradeWithTheIvoryTower"

        // Item
        private val FUNGUS_SAC = 707
    }
}