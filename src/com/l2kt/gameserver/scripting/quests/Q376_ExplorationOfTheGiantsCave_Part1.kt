package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q376_ExplorationOfTheGiantsCave_Part1 : Quest(376, "Exploration of the Giants' Cave, Part 1") {
    init {

        setItemsIds(DICTIONARY_BASIC, MYSTERIOUS_BOOK)

        addStartNpc(SOBLING)
        addTalkId(SOBLING, CLIFF)

        addKillId(20647, 20648, 20649, 20650)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Sobling
        if (event.equals("31147-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["condBook"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(DICTIONARY_BASIC, 1)
        } else if (event.equals("31147-04.htm", ignoreCase = true)) {
            htmltext = checkItems(st)
        } else if (event.equals("31147-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30182-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MYSTERIOUS_BOOK, -1)
            st.giveItems(DICTIONARY_INTERMEDIATE, 1)
        }// Cliff

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 51) "31147-01.htm" else "31147-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SOBLING -> htmltext = checkItems(st)

                    CLIFF -> if (cond == 2 && st.hasQuestItems(MYSTERIOUS_BOOK))
                        htmltext = "30182-01.htm"
                    else if (cond == 3)
                        htmltext = "30182-03.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        // Drop parchment to anyone
        var st: QuestState? = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st!!.dropItems(PARCHMENT, 1, 0, 20000)

        // Drop mysterious book to person who still need it
        st = getRandomPartyMember(player, npc, "condBook", "1")
        if (st == null)
            return null

        if (st.dropItems(MYSTERIOUS_BOOK, 1, 1, 1000))
            st.unset("condBook")

        return null
    }

    companion object {
        private val qn = "Q376_ExplorationOfTheGiantsCave_Part1"

        // NPCs
        private val SOBLING = 31147
        private val CLIFF = 30182

        // Items
        private val PARCHMENT = 5944
        private val DICTIONARY_BASIC = 5891
        private val MYSTERIOUS_BOOK = 5890
        private val DICTIONARY_INTERMEDIATE = 5892
        private val BOOKS = arrayOf(
            // medical theory -> tallum tunic, tallum stockings
            intArrayOf(5937, 5938, 5939, 5940, 5941),
            // architecture -> dark crystal leather, tallum leather
            intArrayOf(5932, 5933, 5934, 5935, 5936),
            // golem plans -> dark crystal breastplate, tallum plate
            intArrayOf(5922, 5923, 5924, 5925, 5926),
            // basics of magic -> dark crystal gaiters, dark crystal leggings
            intArrayOf(5927, 5928, 5929, 5930, 5931)
        )

        // Rewards
        private val RECIPES = arrayOf(
            // medical theory -> tallum tunic, tallum stockings
            intArrayOf(5346, 5354),
            // architecture -> dark crystal leather, tallum leather
            intArrayOf(5332, 5334),
            // golem plans -> dark crystal breastplate, tallum plate
            intArrayOf(5416, 5418),
            // basics of magic -> dark crystal gaiters, dark crystal leggings
            intArrayOf(5424, 5340)
        )

        private fun checkItems(st: QuestState): String {
            if (st.hasQuestItems(MYSTERIOUS_BOOK)) {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    return "31147-07.htm"
                }
                return "31147-08.htm"
            }

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