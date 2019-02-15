package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q121_PavelTheGiant : Quest(121, "Pavel the Giant") {
    init {

        addStartNpc(NEWYEAR)
        addTalkId(NEWYEAR, YUMI)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31961-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32041-2.htm", ignoreCase = true)) {
            st.rewardExpAndSp(10000, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 46) "31961-1a.htm" else "31961-1.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                NEWYEAR -> htmltext = "31961-2a.htm"

                YUMI -> htmltext = "32041-1.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q121_PavelTheGiant"

        // NPCs
        private const val NEWYEAR = 31961
        private const val YUMI = 32041
    }
}