package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q621_EggDelivery : Quest(621, "Egg Delivery") {
    init {

        setItemsIds(BOILED_EGGS, FEE_OF_BOILED_EGG)

        addStartNpc(JEREMY)
        addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, VALENTINE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31521-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BOILED_EGGS, 5)
        } else if (event.equals("31543-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BOILED_EGGS, 1)
            st.giveItems(FEE_OF_BOILED_EGG, 1)
        } else if (event.equals("31544-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BOILED_EGGS, 1)
            st.giveItems(FEE_OF_BOILED_EGG, 1)
        } else if (event.equals("31545-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BOILED_EGGS, 1)
            st.giveItems(FEE_OF_BOILED_EGG, 1)
        } else if (event.equals("31546-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BOILED_EGGS, 1)
            st.giveItems(FEE_OF_BOILED_EGG, 1)
        } else if (event.equals("31547-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BOILED_EGGS, 1)
            st.giveItems(FEE_OF_BOILED_EGG, 1)
        } else if (event.equals("31521-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(FEE_OF_BOILED_EGG) < 5) {
                htmltext = "31521-08.htm"
                st.playSound(QuestState.SOUND_GIVEUP)
                st.exitQuest(true)
            } else {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(FEE_OF_BOILED_EGG, 5)
            }
        } else if (event.equals("31584-02.htm", ignoreCase = true)) {
            if (Rnd[5] < 1) {
                st.rewardItems(RECIPES[Rnd[3]], 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                st.rewardItems(57, 18800)
                st.rewardItems(HASTE_POTION, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 68) "31521-03.htm" else "31521-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    JEREMY -> if (cond == 1)
                        htmltext = "31521-04.htm"
                    else if (cond == 6)
                        htmltext = "31521-05.htm"
                    else if (cond == 7)
                        htmltext = "31521-07.htm"

                    PULIN -> if (cond == 1 && st.getQuestItemsCount(BOILED_EGGS) == 5)
                        htmltext = "31543-01.htm"
                    else if (cond > 1)
                        htmltext = "31543-03.htm"

                    NAFF -> if (cond == 2 && st.getQuestItemsCount(BOILED_EGGS) == 4)
                        htmltext = "31544-01.htm"
                    else if (cond > 2)
                        htmltext = "31544-03.htm"

                    CROCUS -> if (cond == 3 && st.getQuestItemsCount(BOILED_EGGS) == 3)
                        htmltext = "31545-01.htm"
                    else if (cond > 3)
                        htmltext = "31545-03.htm"

                    KUBER -> if (cond == 4 && st.getQuestItemsCount(BOILED_EGGS) == 2)
                        htmltext = "31546-01.htm"
                    else if (cond > 4)
                        htmltext = "31546-03.htm"

                    BEOLIN -> if (cond == 5 && st.getQuestItemsCount(BOILED_EGGS) == 1)
                        htmltext = "31547-01.htm"
                    else if (cond > 5)
                        htmltext = "31547-03.htm"

                    VALENTINE -> if (cond == 7)
                        htmltext = "31584-01.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q621_EggDelivery"

        // Items
        private val BOILED_EGGS = 7195
        private val FEE_OF_BOILED_EGG = 7196

        // NPCs
        private val JEREMY = 31521
        private val PULIN = 31543
        private val NAFF = 31544
        private val CROCUS = 31545
        private val KUBER = 31546
        private val BEOLIN = 31547
        private val VALENTINE = 31584

        // Rewards
        private val HASTE_POTION = 1062
        private val RECIPES = intArrayOf(6847, 6849, 6851)
    }
}