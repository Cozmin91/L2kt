package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q110_ToThePrimevalIsle : Quest(110, "To the Primeval Isle") {
    init {

        setItemsIds(ANCIENT_BOOK)

        addStartNpc(ANTON)
        addTalkId(ANTON, MARQUEZ)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31338-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ANCIENT_BOOK, 1)
        } else if (event.equals("32113-03.htm", ignoreCase = true) && st.hasQuestItems(ANCIENT_BOOK)) {
            st.takeItems(ANCIENT_BOOK, 1)
            st.rewardItems(57, 169380)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "31338-00.htm" else "31338-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                ANTON -> htmltext = "31338-01c.htm"

                MARQUEZ -> htmltext = "32113-01.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q110_ToThePrimevalIsle"

        // NPCs
        private const val ANTON = 31338
        private const val MARQUEZ = 32113

        // Item
        private const val ANCIENT_BOOK = 8777
    }
}