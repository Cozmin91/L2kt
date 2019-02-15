package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q155_FindSirWindawood : Quest(155, "Find Sir Windawood") {
    init {

        setItemsIds(OFFICIAL_LETTER)

        addStartNpc(ABELLOS)
        addTalkId(WINDAWOOD, ABELLOS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30042-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(OFFICIAL_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 3) "30042-01a.htm" else "30042-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                ABELLOS -> htmltext = "30042-03.htm"

                WINDAWOOD -> if (st.hasQuestItems(OFFICIAL_LETTER)) {
                    htmltext = "30311-01.htm"
                    st.takeItems(OFFICIAL_LETTER, 1)
                    st.rewardItems(HASTE_POTION, 1)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q155_FindSirWindawood"

        // Items
        private val OFFICIAL_LETTER = 1019
        private val HASTE_POTION = 734

        // NPCs
        private val ABELLOS = 30042
        private val WINDAWOOD = 30311
    }
}