package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q014_WhereaboutsOfTheArchaeologist : Quest(14, "Whereabouts of the Archaeologist") {
    init {

        setItemsIds(LETTER)

        addStartNpc(LIESEL)
        addTalkId(LIESEL, GHOST_OF_ADVENTURER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31263-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LETTER, 1)
        } else if (event.equals("31538-1.htm", ignoreCase = true)) {
            st.takeItems(LETTER, 1)
            st.rewardItems(57, 113228)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 74) "31263-1.htm" else "31263-0.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                LIESEL -> htmltext = "31263-2.htm"

                GHOST_OF_ADVENTURER -> htmltext = "31538-0.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q014_WhereaboutsOfTheArchaeologist"

        // NPCs
        private const val LIESEL = 31263
        private const val GHOST_OF_ADVENTURER = 31538

        // Items
        private const val LETTER = 7253
    }
}