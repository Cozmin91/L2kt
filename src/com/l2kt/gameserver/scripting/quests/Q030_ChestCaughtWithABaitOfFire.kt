package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q030_ChestCaughtWithABaitOfFire : Quest(30, "Chest caught with a bait of fire") {
    init {

        setItemsIds(MUSICAL_SCORE)

        addStartNpc(LINNAEUS)
        addTalkId(LINNAEUS, RUKAL)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31577-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31577-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(RED_TREASURE_BOX)) {
                st["cond"] = "2"
                st.takeItems(RED_TREASURE_BOX, 1)
                st.giveItems(MUSICAL_SCORE, 1)
            } else
                htmltext = "31577-08.htm"
        } else if (event.equals("30629-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(MUSICAL_SCORE)) {
                htmltext = "30629-02.htm"
                st.takeItems(MUSICAL_SCORE, 1)
                st.giveItems(NECKLACE_OF_PROTECTION, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30629-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 60)
                htmltext = "31577-02.htm"
            else {
                val st2 = player.getQuestState("Q053_LinnaeusSpecialBait")
                if (st2 != null && st2.isCompleted)
                    htmltext = "31577-01.htm"
                else
                    htmltext = "31577-03.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LINNAEUS -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(RED_TREASURE_BOX)) "31577-06.htm" else "31577-05.htm"
                    else if (cond == 2)
                        htmltext = "31577-09.htm"

                    RUKAL -> if (cond == 2)
                        htmltext = "30629-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q030_ChestCaughtWithABaitOfFire"

        // NPCs
        private const val LINNAEUS = 31577
        private const val RUKAL = 30629

        // Items
        private const val RED_TREASURE_BOX = 6511
        private const val MUSICAL_SCORE = 7628
        private const val NECKLACE_OF_PROTECTION = 916
    }
}