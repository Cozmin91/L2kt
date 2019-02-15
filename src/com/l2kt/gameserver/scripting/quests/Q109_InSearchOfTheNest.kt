package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q109_InSearchOfTheNest : Quest(109, "In Search of the Nest") {
    init {

        setItemsIds(SCOUT_MEMO)

        addStartNpc(PIERCE)
        addTalkId(PIERCE, SCOUT_CORPSE, KAHMAN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31553-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32015-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SCOUT_MEMO, 1)
        } else if (event.equals("31553-03.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SCOUT_MEMO, 1)
        } else if (event.equals("31554-02.htm", ignoreCase = true)) {
            st.rewardItems(57, 5168)
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
            Quest.STATE_CREATED ->
                // Must worn one or other Golden Ram Badge in order to be accepted.
                if (player.level >= 66 && st.hasAtLeastOneQuestItem(RECRUIT_BADGE, SOLDIER_BADGE))
                    htmltext = "31553-00.htm"
                else
                    htmltext = "31553-00a.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    PIERCE -> if (cond == 1)
                        htmltext = "31553-01a.htm"
                    else if (cond == 2)
                        htmltext = "31553-02.htm"
                    else if (cond == 3)
                        htmltext = "31553-03.htm"

                    SCOUT_CORPSE -> if (cond == 1)
                        htmltext = "32015-01.htm"
                    else if (cond == 2)
                        htmltext = "32015-02.htm"

                    KAHMAN -> if (cond == 3)
                        htmltext = "31554-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q109_InSearchOfTheNest"

        // NPCs
        private const val PIERCE = 31553
        private const val KAHMAN = 31554
        private const val SCOUT_CORPSE = 32015

        // Items
        private const val SCOUT_MEMO = 8083
        private const val RECRUIT_BADGE = 7246
        private const val SOLDIER_BADGE = 7247
    }
}