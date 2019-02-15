package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

/**
 * The onKill section of that quest is directly written on Q611.
 */
class Q612_WarWithKetraOrcs : Quest(612, "War with Ketra Orcs") {
    init {

        setItemsIds(MOLAR_OF_KETRA_ORC)

        addStartNpc(31377) // Ashas Varka Durai
        addTalkId(31377)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31377-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31377-07.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(MOLAR_OF_KETRA_ORC) >= 100) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(MOLAR_OF_KETRA_ORC, 100)
                st.giveItems(NEPENTHES_SEED, 20)
            } else
                htmltext = "31377-08.htm"
        } else if (event.equals("31377-09.htm", ignoreCase = true)) {
            st.takeItems(MOLAR_OF_KETRA_ORC, -1)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level >= 74 && player.isAlliedWithVarka) "31377-01.htm" else "31377-02.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.hasQuestItems(MOLAR_OF_KETRA_ORC)) "31377-04.htm" else "31377-05.htm"
        }

        return htmltext
    }

    companion object {
        private val qn = "Q612_WarWithKetraOrcs"

        // Items
        private val NEPENTHES_SEED = 7187
        private val MOLAR_OF_KETRA_ORC = 7234
    }
}