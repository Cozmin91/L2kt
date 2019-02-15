package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q013_ParcelDelivery : Quest(13, "Parcel Delivery") {
    init {

        setItemsIds(PACKAGE)

        addStartNpc(FUNDIN)
        addTalkId(FUNDIN, VULCAN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31274-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(PACKAGE, 1)
        } else if (event.equals("31539-1.htm", ignoreCase = true)) {
            st.takeItems(PACKAGE, 1)
            st.rewardItems(57, 82656)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 74) "31274-1.htm" else "31274-0.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                FUNDIN -> htmltext = "31274-2.htm"

                VULCAN -> htmltext = "31539-0.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q013_ParcelDelivery"

        // NPCs
        private const val FUNDIN = 31274
        private const val VULCAN = 31539

        // Item
        private const val PACKAGE = 7263
    }
}