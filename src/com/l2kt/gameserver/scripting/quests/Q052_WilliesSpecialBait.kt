package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q052_WilliesSpecialBait : Quest(52, "Willie's Special Bait") {
    init {

        setItemsIds(TARLK_EYE)

        addStartNpc(31574) // Willie
        addTalkId(31574)

        addKillId(20573) // Tarlk Basilik
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31574-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31574-07.htm", ignoreCase = true)) {
            htmltext = "31574-06.htm"
            st.takeItems(TARLK_EYE, -1)
            st.rewardItems(EARTH_FISHING_LURE, 4)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 48) "31574-02.htm" else "31574-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(TARLK_EYE) == 100) "31574-04.htm" else "31574-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(TARLK_EYE, 1, 100, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private const val qn = "Q052_WilliesSpecialBait"

        // Item
        private const val TARLK_EYE = 7623

        // Reward
        private const val EARTH_FISHING_LURE = 7612
    }
}