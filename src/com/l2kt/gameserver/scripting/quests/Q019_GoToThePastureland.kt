package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q019_GoToThePastureland : Quest(19, "Go to the Pastureland!") {
    init {

        setItemsIds(YOUNG_WILD_BEAST_MEAT)

        addStartNpc(VLADIMIR)
        addTalkId(VLADIMIR, TUNATUN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31302-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(YOUNG_WILD_BEAST_MEAT, 1)
        } else if (event.equals("019_finish", ignoreCase = true)) {
            if (st.hasQuestItems(YOUNG_WILD_BEAST_MEAT)) {
                htmltext = "31537-01.htm"
                st.takeItems(YOUNG_WILD_BEAST_MEAT, 1)
                st.rewardItems(57, 30000)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "31537-02.htm"
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 63) "31302-03.htm" else "31302-00.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                VLADIMIR -> htmltext = "31302-02.htm"

                TUNATUN -> htmltext = "31537-00.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q019_GoToThePastureland"

        // Items
        private const val YOUNG_WILD_BEAST_MEAT = 7547

        // NPCs
        private const val VLADIMIR = 31302
        private const val TUNATUN = 31537
    }
}