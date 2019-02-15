package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q156_MillenniumLove : Quest(156, "Millennium Love") {
    init {

        setItemsIds(LILITH_LETTER, THEON_DIARY)

        addStartNpc(LILITH)
        addTalkId(LILITH, BAENEDES)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30368-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LILITH_LETTER, 1)
        } else if (event.equals("30369-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LILITH_LETTER, 1)
            st.giveItems(THEON_DIARY, 1)
        } else if (event.equals("30369-03.htm", ignoreCase = true)) {
            st.takeItems(LILITH_LETTER, 1)
            st.rewardExpAndSp(3000, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30368-00.htm" else "30368-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                LILITH -> if (st.hasQuestItems(LILITH_LETTER))
                    htmltext = "30368-05.htm"
                else if (st.hasQuestItems(THEON_DIARY)) {
                    htmltext = "30368-06.htm"
                    st.takeItems(THEON_DIARY, 1)
                    st.giveItems(5250, 1)
                    st.rewardExpAndSp(3000, 0)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }

                BAENEDES -> if (st.hasQuestItems(LILITH_LETTER))
                    htmltext = "30369-01.htm"
                else if (st.hasQuestItems(THEON_DIARY))
                    htmltext = "30369-04.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q156_MillenniumLove"

        // Items
        private val LILITH_LETTER = 1022
        private val THEON_DIARY = 1023

        // NPCs
        private val LILITH = 30368
        private val BAENEDES = 30369
    }
}