package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q112_WalkOfFate : Quest(112, "Walk of Fate") {
    init {

        addStartNpc(LIVINA)
        addTalkId(LIVINA, KARUDA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30572-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32017-02.htm", ignoreCase = true)) {
            st.giveItems(ENCHANT_D, 1)
            st.rewardItems(57, 4665)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "30572-00.htm" else "30572-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                LIVINA -> htmltext = "30572-03.htm"

                KARUDA -> htmltext = "32017-01.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q112_WalkOfFate"

        // NPCs
        private const val LIVINA = 30572
        private const val KARUDA = 32017

        // Rewards
        private const val ENCHANT_D = 956
    }
}