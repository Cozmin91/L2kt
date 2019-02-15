package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q029_ChestCaughtWithABaitOfEarth : Quest(29, "Chest caught with a bait of earth") {
    init {

        setItemsIds(SMALL_GLASS_BOX)

        addStartNpc(WILLIE)
        addTalkId(WILLIE, ANABEL)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31574-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31574-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SMALL_PURPLE_TREASURE_CHEST)) {
                st["cond"] = "2"
                st.takeItems(SMALL_PURPLE_TREASURE_CHEST, 1)
                st.giveItems(SMALL_GLASS_BOX, 1)
            } else
                htmltext = "31574-08.htm"
        } else if (event.equals("30909-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SMALL_GLASS_BOX)) {
                htmltext = "30909-02.htm"
                st.takeItems(SMALL_GLASS_BOX, 1)
                st.giveItems(PLATED_LEATHER_GLOVES, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30909-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 48)
                htmltext = "31574-02.htm"
            else {
                val st2 = player.getQuestState("Q052_WilliesSpecialBait")
                if (st2 != null && st2.isCompleted)
                    htmltext = "31574-01.htm"
                else
                    htmltext = "31574-03.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    WILLIE -> if (cond == 1)
                        htmltext =
                                if (!st.hasQuestItems(SMALL_PURPLE_TREASURE_CHEST)) "31574-06.htm" else "31574-05.htm"
                    else if (cond == 2)
                        htmltext = "31574-09.htm"

                    ANABEL -> if (cond == 2)
                        htmltext = "30909-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q029_ChestCaughtWithABaitOfEarth"

        // NPCs
        private const val WILLIE = 31574
        private const val ANABEL = 30909

        // Items
        private const val SMALL_PURPLE_TREASURE_CHEST = 6507
        private const val SMALL_GLASS_BOX = 7627
        private const val PLATED_LEATHER_GLOVES = 2455
    }
}