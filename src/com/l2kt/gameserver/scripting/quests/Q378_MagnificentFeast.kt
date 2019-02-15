package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q378_MagnificentFeast : Quest(378, "Magnificent Feast") {
    init {
        REWARDS["9"] = intArrayOf(847, 1, 5700)
        REWARDS["10"] = intArrayOf(846, 2, 0)
        REWARDS["12"] = intArrayOf(909, 1, 25400)
        REWARDS["17"] = intArrayOf(846, 2, 1200)
        REWARDS["18"] = intArrayOf(879, 1, 6900)
        REWARDS["20"] = intArrayOf(890, 2, 8500)
        REWARDS["33"] = intArrayOf(879, 1, 8100)
        REWARDS["34"] = intArrayOf(910, 1, 0)
        REWARDS["36"] = intArrayOf(848, 1, 2200)
    }

    init {

        addStartNpc(RANSPO)
        addTalkId(RANSPO)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30594-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30594-4a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(WINE_15)) {
                st["cond"] = "2"
                st["score"] = "1"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(WINE_15, 1)
            } else
                htmltext = "30594-4.htm"
        } else if (event.equals("30594-4b.htm", ignoreCase = true)) {
            if (st.hasQuestItems(WINE_30)) {
                st["cond"] = "2"
                st["score"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(WINE_30, 1)
            } else
                htmltext = "30594-4.htm"
        } else if (event.equals("30594-4c.htm", ignoreCase = true)) {
            if (st.hasQuestItems(WINE_60)) {
                st["cond"] = "2"
                st["score"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(WINE_60, 1)
            } else
                htmltext = "30594-4.htm"
        } else if (event.equals("30594-6.htm", ignoreCase = true)) {
            if (st.hasQuestItems(MUSICAL_SCORE)) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(MUSICAL_SCORE, 1)
            } else
                htmltext = "30594-5.htm"
        } else {
            val score = st.getInt("score")
            if (event.equals("30594-8a.htm", ignoreCase = true)) {
                if (st.hasQuestItems(SALAD_RECIPE)) {
                    st["cond"] = "4"
                    st["score"] = (score + 8).toString()
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(SALAD_RECIPE, 1)
                } else
                    htmltext = "30594-8.htm"
            } else if (event.equals("30594-8b.htm", ignoreCase = true)) {
                if (st.hasQuestItems(SAUCE_RECIPE)) {
                    st["cond"] = "4"
                    st["score"] = (score + 16).toString()
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(SAUCE_RECIPE, 1)
                } else
                    htmltext = "30594-8.htm"
            } else if (event.equals("30594-8c.htm", ignoreCase = true)) {
                if (st.hasQuestItems(STEAK_RECIPE)) {
                    st["cond"] = "4"
                    st["score"] = (score + 32).toString()
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(STEAK_RECIPE, 1)
                } else
                    htmltext = "30594-8.htm"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "30594-0.htm" else "30594-1.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30594-3.htm"
                else if (cond == 2)
                    htmltext = if (!st.hasQuestItems(MUSICAL_SCORE)) "30594-5.htm" else "30594-5a.htm"
                else if (cond == 3)
                    htmltext = "30594-7.htm"
                else if (cond == 4) {
                    val score = st["score"]
                    if (REWARDS.containsKey(score) && st.hasQuestItems(RITRON_DESSERT)) {
                        htmltext = "30594-10.htm"

                        st.takeItems(RITRON_DESSERT, 1)
                        st.giveItems(REWARDS[score]!![0], REWARDS[score]!![1])

                        val adena = REWARDS[score]!![2]
                        if (adena > 0)
                            st.rewardItems(57, adena)

                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    } else
                        htmltext = "30594-9.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q378_MagnificentFeast"

        // NPC
        private val RANSPO = 30594

        // Items
        private val WINE_15 = 5956
        private val WINE_30 = 5957
        private val WINE_60 = 5958
        private val MUSICAL_SCORE = 4421
        private val SALAD_RECIPE = 1455
        private val SAUCE_RECIPE = 1456
        private val STEAK_RECIPE = 1457
        private val RITRON_DESSERT = 5959

        // Rewards
        private val REWARDS = HashMap<String, IntArray>()
    }
}