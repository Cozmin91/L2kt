package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q319_ScentOfDeath : Quest(319, "Scent of Death") {
    init {

        setItemsIds(ZOMBIE_SKIN)

        addStartNpc(30138) // Minaless
        addTalkId(30138)

        addKillId(20015, 20020)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30138-04.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 11) "30138-02.htm" else "30138-03.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30138-05.htm"
            else {
                htmltext = "30138-06.htm"
                st.takeItems(ZOMBIE_SKIN, -1)
                st.rewardItems(57, 3350)
                st.rewardItems(1060, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(ZOMBIE_SKIN, 1, 5, 200000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q319_ScentOfDeath"

        // Item
        private val ZOMBIE_SKIN = 1045
    }
}