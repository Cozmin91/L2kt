package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q291_RevengeOfTheRedbonnet : Quest(291, "Revenge of the Redbonnet") {
    init {

        setItemsIds(BLACK_WOLF_PELT)

        addStartNpc(30553) // Maryse Redbonnet
        addTalkId(30553)

        addKillId(20317)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30553-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 4) "30553-01.htm" else "30553-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30553-04.htm"
                else if (cond == 2) {
                    htmltext = "30553-05.htm"
                    st.takeItems(BLACK_WOLF_PELT, -1)

                    val random = Rnd[100]
                    if (random < 3)
                        st.rewardItems(GRANDMA_PEARL, 1)
                    else if (random < 21)
                        st.rewardItems(GRANDMA_MIRROR, 1)
                    else if (random < 46)
                        st.rewardItems(GRANDMA_NECKLACE, 1)
                    else {
                        st.rewardItems(SCROLL_OF_ESCAPE, 1)
                        st.rewardItems(GRANDMA_HAIRPIN, 1)
                    }

                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(BLACK_WOLF_PELT, 1, 40))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q291_RevengeOfTheRedbonnet"

        // Quest items
        private val BLACK_WOLF_PELT = 1482

        // Rewards
        private val SCROLL_OF_ESCAPE = 736
        private val GRANDMA_PEARL = 1502
        private val GRANDMA_MIRROR = 1503
        private val GRANDMA_NECKLACE = 1504
        private val GRANDMA_HAIRPIN = 1505
    }
}