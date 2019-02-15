package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q018_MeetingWithTheGoldenRam : Quest(18, "Meeting with the Golden Ram") {
    init {

        setItemsIds(SUPPLY_BOX)

        addStartNpc(DONAL)
        addTalkId(DONAL, DAISY, ABERCROMBIE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31314-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31315-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SUPPLY_BOX, 1)
        } else if (event.equals("31555-02.htm", ignoreCase = true)) {
            st.takeItems(SUPPLY_BOX, 1)
            st.rewardItems(57, 15000)
            st.rewardExpAndSp(50000, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 66) "31314-02.htm" else "31314-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    DONAL -> htmltext = "31314-04.htm"

                    DAISY -> if (cond == 1)
                        htmltext = "31315-01.htm"
                    else if (cond == 2)
                        htmltext = "31315-03.htm"

                    ABERCROMBIE -> if (cond == 2)
                        htmltext = "31555-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q018_MeetingWithTheGoldenRam"

        // Items
        private const val SUPPLY_BOX = 7245

        // NPCs
        private const val DONAL = 31314
        private const val DAISY = 31315
        private const val ABERCROMBIE = 31555
    }
}