package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q601_WatchingEyes : Quest(601, "Watching Eyes") {
    init {

        setItemsIds(PROOF_OF_AVENGER)

        addStartNpc(31683) // Eye of Argos
        addTalkId(31683)

        addKillId(21306, 21308, 21309, 21310, 21311)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31683-03.htm", ignoreCase = true)) {
            if (player.level < 71)
                htmltext = "31683-02.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            }
        } else if (event.equals("31683-07.htm", ignoreCase = true)) {
            st.takeItems(PROOF_OF_AVENGER, -1)

            val random = Rnd[100]
            for (element in REWARDS) {
                if (random < element[2]) {
                    st.rewardItems(57, element[1])

                    if (element[0] != 0) {
                        st.giveItems(element[0], 5)
                        st.rewardExpAndSp(120000, 10000)
                    }
                    break
                }
            }
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "31683-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = if (st.hasQuestItems(PROOF_OF_AVENGER)) "31683-05.htm" else "31683-04.htm"
                else if (cond == 2)
                    htmltext = "31683-06.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player, npc, "cond", "1") ?: return null

        if (st.dropItems(PROOF_OF_AVENGER, 1, 100, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q601_WatchingEyes"

        // Items
        private val PROOF_OF_AVENGER = 7188

        // Rewards
        private val REWARDS = arrayOf(
            intArrayOf(6699, 90000, 20),
            intArrayOf(6698, 80000, 40),
            intArrayOf(6700, 40000, 50),
            intArrayOf(0, 230000, 100)
        )
    }
}