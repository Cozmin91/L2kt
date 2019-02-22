package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q313_CollectSpores : Quest(313, "Collect Spores") {
    init {

        setItemsIds(SPORE_SAC)

        addStartNpc(30150) // Herbiel
        addTalkId(30150)

        addKillId(20509) // Spore Fungus
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30150-05.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 8) "30150-02.htm" else "30150-03.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30150-06.htm"
            else {
                htmltext = "30150-07.htm"
                st.takeItems(SPORE_SAC, -1)
                st.rewardItems(57, 3500)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(SPORE_SAC, 1, 10, 400000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q313_CollectSpores"

        // Item
        private val SPORE_SAC = 1118
    }
}