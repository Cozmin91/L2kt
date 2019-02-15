package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q122_OminousNews : Quest(122, "Ominous News") {
    init {

        addStartNpc(MOIRA)
        addTalkId(MOIRA, KARUDA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31979-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32017-02.htm", ignoreCase = true)) {
            st.rewardItems(57, 1695)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "31979-01.htm" else "31979-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                MOIRA -> htmltext = "31979-03.htm"

                KARUDA -> htmltext = "32017-01.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q122_OminousNews"

        // NPCs
        private const val MOIRA = 31979
        private const val KARUDA = 32017
    }
}