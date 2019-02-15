package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q033_MakeAPairOfDressShoes : Quest(33, "Make a Pair of Dress Shoes") {
    init {

        addStartNpc(WOODLEY)
        addTalkId(WOODLEY, IAN, LEIKAR)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30838-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31520-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30838-3.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30838-5.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(LEATHER) >= 200 && st.getQuestItemsCount(THREAD) >= 600 && st.getQuestItemsCount(
                    ADENA
                ) >= 200000
            ) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(ADENA, 200000)
                st.takeItems(LEATHER, 200)
                st.takeItems(THREAD, 600)
            } else
                htmltext = "30838-4a.htm"
        } else if (event.equals("30164-1.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(ADENA) >= 300000) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(ADENA, 300000)
            } else
                htmltext = "30164-1a.htm"
        } else if (event.equals("30838-7.htm", ignoreCase = true)) {
            st.giveItems(DRESS_SHOES_BOX, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 60) {
                val fwear = player.getQuestState("Q037_MakeFormalWear")
                if (fwear != null && fwear.getInt("cond") == 7)
                    htmltext = "30838-0.htm"
                else
                    htmltext = "30838-0a.htm"
            } else
                htmltext = "30838-0b.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    WOODLEY -> if (cond == 1)
                        htmltext = "30838-1.htm"
                    else if (cond == 2)
                        htmltext = "30838-2.htm"
                    else if (cond == 3) {
                        if (st.getQuestItemsCount(LEATHER) >= 200 && st.getQuestItemsCount(THREAD) >= 600 && st.getQuestItemsCount(
                                ADENA
                            ) >= 200000
                        )
                            htmltext = "30838-4.htm"
                        else
                            htmltext = "30838-4a.htm"
                    } else if (cond == 4)
                        htmltext = "30838-5a.htm"
                    else if (cond == 5)
                        htmltext = "30838-6.htm"

                    LEIKAR -> if (cond == 1)
                        htmltext = "31520-0.htm"
                    else if (cond > 1)
                        htmltext = "31520-1a.htm"

                    IAN -> if (cond == 4)
                        htmltext = "30164-0.htm"
                    else if (cond == 5)
                        htmltext = "30164-2.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q033_MakeAPairOfDressShoes"

        // NPCs
        private const val WOODLEY = 30838
        private const val IAN = 30164
        private const val LEIKAR = 31520

        // Items
        private const val LEATHER = 1882
        private const val THREAD = 1868
        private const val ADENA = 57

        // Rewards
        var DRESS_SHOES_BOX = 7113
    }
}