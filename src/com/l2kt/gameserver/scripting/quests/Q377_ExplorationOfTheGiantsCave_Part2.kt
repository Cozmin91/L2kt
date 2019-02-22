package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q377_ExplorationOfTheGiantsCave_Part2 : Quest(377, "Exploration of the Giants' Cave, Part 2") {
    init {

        addStartNpc(31147) // Sobling
        addTalkId(31147)

        addKillId(20654, 20656, 20657, 20658)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31147-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31147-04.htm", ignoreCase = true)) {
            htmltext = checkItems(st)
        } else if (event.equals("31147-07.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level < 57 || !st.hasQuestItems(DICTIONARY_INTERMEDIATE)) "31147-01.htm" else "31147-02.htm"

            Quest.STATE_STARTED -> htmltext = checkItems(st)
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(ANCIENT_BOOK, 1, 0, 18000)

        return null
    }

    companion object {
        private val qn = "Q377_ExplorationOfTheGiantsCave_Part2"

        // Items
        private val ANCIENT_BOOK = 5955
        private val DICTIONARY_INTERMEDIATE = 5892

        private val BOOKS = arrayOf(
            // science & technology -> majestic leather, leather armor of nightmare
            intArrayOf(5945, 5946, 5947, 5948, 5949),
            // culture -> armor of nightmare, majestic plate
            intArrayOf(5950, 5951, 5952, 5953, 5954)
        )

        // Rewards
        private val RECIPES = arrayOf(
            // science & technology -> majestic leather, leather armor of nightmare
            intArrayOf(5338, 5336),
            // culture -> armor of nightmare, majestic plate
            intArrayOf(5420, 5422)
        )

        private fun checkItems(st: QuestState): String {
            for (type in BOOKS.indices) {
                var complete = true
                for (book in BOOKS[type]) {
                    if (!st.hasQuestItems(book))
                        complete = false
                }

                if (complete) {
                    for (book in BOOKS[type])
                        st.takeItems(book, 1)

                    st.giveItems(RECIPES[type][Rnd[RECIPES[type].size]], 1)
                    return "31147-04.htm"
                }
            }
            return "31147-05.htm"
        }
    }
}