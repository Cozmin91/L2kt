package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q015_SweetWhispers : Quest(15, "Sweet Whispers") {
    init {

        addStartNpc(VLADIMIR)
        addTalkId(VLADIMIR, HIERARCH, MYSTERIOUS_NECRO)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31302-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31518-01.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31517-01.htm", ignoreCase = true)) {
            st.rewardExpAndSp(60217, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "31302-00a.htm" else "31302-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VLADIMIR -> htmltext = "31302-01a.htm"

                    MYSTERIOUS_NECRO -> if (cond == 1)
                        htmltext = "31518-00.htm"
                    else if (cond == 2)
                        htmltext = "31518-01a.htm"

                    HIERARCH -> if (cond == 2)
                        htmltext = "31517-00.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q015_SweetWhispers"

        // NPCs
        private const val VLADIMIR = 31302
        private const val HIERARCH = 31517
        private const val MYSTERIOUS_NECRO = 31518
    }
}