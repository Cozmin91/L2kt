package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q622_SpecialtyLiquorDelivery : Quest(622, "Specialty Liquor Delivery") {
    init {

        setItemsIds(SPECIAL_DRINK, FEE_OF_SPECIAL_DRINK)

        addStartNpc(JEREMY)
        addTalkId(JEREMY, PULIN, NAFF, CROCUS, KUBER, BEOLIN, LIETTA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31521-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(SPECIAL_DRINK, 5)
        } else if (event.equals("31547-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPECIAL_DRINK, 1)
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1)
        } else if (event.equals("31546-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPECIAL_DRINK, 1)
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1)
        } else if (event.equals("31545-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPECIAL_DRINK, 1)
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1)
        } else if (event.equals("31544-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPECIAL_DRINK, 1)
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1)
        } else if (event.equals("31543-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SPECIAL_DRINK, 1)
            st.giveItems(FEE_OF_SPECIAL_DRINK, 1)
        } else if (event.equals("31521-06.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(FEE_OF_SPECIAL_DRINK, 5)
        } else if (event.equals("31267-02.htm", ignoreCase = true)) {
            if (Rnd[5] < 1)
                st.giveItems(RECIPES[Rnd[RECIPES.size]], 1)
            else {
                st.rewardItems(ADENA, 18800)
                st.rewardItems(HASTE_POTION, 1)
            }
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 68) "31521-03.htm" else "31521-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    JEREMY -> if (cond < 6)
                        htmltext = "31521-04.htm"
                    else if (cond == 6)
                        htmltext = "31521-05.htm"
                    else if (cond == 7)
                        htmltext = "31521-06.htm"

                    BEOLIN -> if (cond == 1 && st.getQuestItemsCount(SPECIAL_DRINK) == 5)
                        htmltext = "31547-01.htm"
                    else if (cond > 1)
                        htmltext = "31547-03.htm"

                    KUBER -> if (cond == 2 && st.getQuestItemsCount(SPECIAL_DRINK) == 4)
                        htmltext = "31546-01.htm"
                    else if (cond > 2)
                        htmltext = "31546-03.htm"

                    CROCUS -> if (cond == 3 && st.getQuestItemsCount(SPECIAL_DRINK) == 3)
                        htmltext = "31545-01.htm"
                    else if (cond > 3)
                        htmltext = "31545-03.htm"

                    NAFF -> if (cond == 4 && st.getQuestItemsCount(SPECIAL_DRINK) == 2)
                        htmltext = "31544-01.htm"
                    else if (cond > 4)
                        htmltext = "31544-03.htm"

                    PULIN -> if (cond == 5 && st.getQuestItemsCount(SPECIAL_DRINK) == 1)
                        htmltext = "31543-01.htm"
                    else if (cond > 5)
                        htmltext = "31543-03.htm"

                    LIETTA -> if (cond == 7)
                        htmltext = "31267-01.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q622_SpecialtyLiquorDelivery"

        // Items
        private val SPECIAL_DRINK = 7197
        private val FEE_OF_SPECIAL_DRINK = 7198

        // NPCs
        private val JEREMY = 31521
        private val PULIN = 31543
        private val NAFF = 31544
        private val CROCUS = 31545
        private val KUBER = 31546
        private val BEOLIN = 31547
        private val LIETTA = 31267

        // Rewards
        private val ADENA = 57
        private val HASTE_POTION = 1062
        private val RECIPES = intArrayOf(6847, 6849, 6851)
    }
}