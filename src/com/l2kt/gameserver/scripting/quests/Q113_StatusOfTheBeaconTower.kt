package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q113_StatusOfTheBeaconTower : Quest(113, "Status of the Beacon Tower") {
    init {

        setItemsIds(BOX)

        addStartNpc(MOIRA)
        addTalkId(MOIRA, TORRANT)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31979-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BOX, 1)
        } else if (event.equals("32016-02.htm", ignoreCase = true)) {
            st.takeItems(BOX, 1)
            st.rewardItems(57, 21578)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 40) "31979-00.htm" else "31979-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                MOIRA -> htmltext = "31979-03.htm"

                TORRANT -> htmltext = "32016-01.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q113_StatusOfTheBeaconTower"

        // NPCs
        private const val MOIRA = 31979
        private const val TORRANT = 32016

        // Item
        private const val BOX = 8086
    }
}