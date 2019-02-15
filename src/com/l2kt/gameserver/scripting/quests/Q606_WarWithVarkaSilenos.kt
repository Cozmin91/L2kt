package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

/**
 * The onKill section of that quest is directly written on Q605.
 */
class Q606_WarWithVarkaSilenos : Quest(606, "War with Varka Silenos") {
    init {

        setItemsIds(VARKA_MANE)

        addStartNpc(31370) // Kadun Zu Ketra
        addTalkId(31370)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31370-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31370-07.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VARKA_MANE) >= 100) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(VARKA_MANE, 100)
                st.giveItems(HORN_OF_BUFFALO, 20)
            } else
                htmltext = "31370-08.htm"
        } else if (event.equals("31370-09.htm", ignoreCase = true)) {
            st.takeItems(VARKA_MANE, -1)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level >= 74 && player.isAlliedWithKetra) "31370-01.htm" else "31370-02.htm"

            Quest.STATE_STARTED -> htmltext = if (st.hasQuestItems(VARKA_MANE)) "31370-04.htm" else "31370-05.htm"
        }

        return htmltext
    }

    companion object {
        private val qn = "Q606_WarWithVarkaSilenos"

        // Items
        private val HORN_OF_BUFFALO = 7186
        private val VARKA_MANE = 7233
    }
}