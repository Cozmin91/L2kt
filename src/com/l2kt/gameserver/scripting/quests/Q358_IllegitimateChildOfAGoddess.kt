package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q358_IllegitimateChildOfAGoddess : Quest(358, "Illegitimate Child of a Goddess") {
    init {

        setItemsIds(SCALE)

        addStartNpc(30862) // Oltlin
        addTalkId(30862)

        addKillId(20672, 20673) // Trives, Falibati
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30862-05.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 63) "30862-01.htm" else "30862-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30862-06.htm"
            else {
                htmltext = "30862-07.htm"
                st.takeItems(SCALE, -1)
                st.giveItems(REWARD[Rnd[REWARD.size]], 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(SCALE, 1, 108, if (npc.npcId == 20672) 680000 else 660000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q358_IllegitimateChildOfAGoddess"

        // Item
        private val SCALE = 5868

        // Reward
        private val REWARD = intArrayOf(6329, 6331, 6333, 6335, 6337, 6339, 5364, 5366)
    }
}