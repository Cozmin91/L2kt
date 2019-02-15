package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q020_BringUpWithLove : Quest(20, "Bring Up With Love") {
    init {

        setItemsIds(JEWEL_OF_INNOCENCE)

        addStartNpc(31537) // Tunatun
        addTalkId(31537)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31537-09.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31537-12.htm", ignoreCase = true)) {
            st.takeItems(JEWEL_OF_INNOCENCE, -1)
            st.rewardItems(57, 68500)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 65) "31537-02.htm" else "31537-01.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 2)
                htmltext = "31537-11.htm"
            else
                htmltext = "31537-10.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        const val qn = "Q020_BringUpWithLove"

        // Item
        private const val JEWEL_OF_INNOCENCE = 7185
    }
}