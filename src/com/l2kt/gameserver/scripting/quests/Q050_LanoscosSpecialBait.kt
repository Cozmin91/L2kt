package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q050_LanoscosSpecialBait : Quest(50, "Lanosco's Special Bait") {
    init {

        setItemsIds(ESSENCE_OF_WIND)

        addStartNpc(31570) // Lanosco
        addTalkId(31570)

        addKillId(21026) // Singing wind
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31570-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31570-07.htm", ignoreCase = true)) {
            htmltext = "31570-06.htm"
            st.takeItems(ESSENCE_OF_WIND, -1)
            st.rewardItems(WIND_FISHING_LURE, 4)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 27) "31570-02.htm" else "31570-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(ESSENCE_OF_WIND) == 100) "31570-04.htm" else "31570-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(ESSENCE_OF_WIND, 1, 100, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private const val qn = "Q050_LanoscosSpecialBait"

        // Item
        private const val ESSENCE_OF_WIND = 7621

        // Reward
        private const val WIND_FISHING_LURE = 7610
    }
}