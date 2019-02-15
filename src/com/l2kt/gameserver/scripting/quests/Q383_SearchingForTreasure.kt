package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q383_SearchingForTreasure : Quest(383, "Searching for Treasure") {
    init {

        addStartNpc(ESPEN)
        addTalkId(ESPEN, PIRATE_CHEST)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30890-04.htm", ignoreCase = true)) {
            // Sell the map.
            if (st.hasQuestItems(PIRATE_TREASURE_MAP)) {
                st.takeItems(PIRATE_TREASURE_MAP, 1)
                st.rewardItems(57, 1000)
            } else
                htmltext = "30890-06.htm"
        } else if (event.equals("30890-07.htm", ignoreCase = true)) {
            // Listen the story.
            if (st.hasQuestItems(PIRATE_TREASURE_MAP)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else
                htmltext = "30890-06.htm"
        } else if (event.equals("30890-11.htm", ignoreCase = true)) {
            // Decipher the map.
            if (st.hasQuestItems(PIRATE_TREASURE_MAP)) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(PIRATE_TREASURE_MAP, 1)
            } else
                htmltext = "30890-06.htm"
        } else if (event.equals("31148-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(THIEF_KEY)) {
                st.takeItems(THIEF_KEY, 1)

                // Adena reward.
                var i1 = 0

                var i0 = Rnd[100]
                if (i0 < 5)
                    st.giveItems(2450, 1)
                else if (i0 < 6)
                    st.giveItems(2451, 1)
                else if (i0 < 18)
                    st.giveItems(956, 1)
                else if (i0 < 28)
                    st.giveItems(952, 1)
                else
                    i1 += 500

                i0 = Rnd[1000]
                if (i0 < 25)
                    st.giveItems(4481, 1)
                else if (i0 < 50)
                    st.giveItems(4482, 1)
                else if (i0 < 75)
                    st.giveItems(4483, 1)
                else if (i0 < 100)
                    st.giveItems(4484, 1)
                else if (i0 < 125)
                    st.giveItems(4485, 1)
                else if (i0 < 150)
                    st.giveItems(4486, 1)
                else if (i0 < 175)
                    st.giveItems(4487, 1)
                else if (i0 < 200)
                    st.giveItems(4488, 1)
                else if (i0 < 225)
                    st.giveItems(4489, 1)
                else if (i0 < 250)
                    st.giveItems(4490, 1)
                else if (i0 < 275)
                    st.giveItems(4491, 1)
                else if (i0 < 300)
                    st.giveItems(4492, 1)
                else
                    i1 += 300

                i0 = Rnd[100]
                if (i0 < 4)
                    st.giveItems(1337, 1)
                else if (i0 < 8)
                    st.giveItems(1338, 2)
                else if (i0 < 12)
                    st.giveItems(1339, 2)
                else if (i0 < 16)
                    st.giveItems(3447, 2)
                else if (i0 < 20)
                    st.giveItems(3450, 1)
                else if (i0 < 25)
                    st.giveItems(3453, 1)
                else if (i0 < 27)
                    st.giveItems(3456, 1)
                else
                    i1 += 500

                i0 = Rnd[100]
                if (i0 < 20)
                    st.giveItems(4408, 1)
                else if (i0 < 40)
                    st.giveItems(4409, 1)
                else if (i0 < 60)
                    st.giveItems(4418, 1)
                else if (i0 < 80)
                    st.giveItems(4419, 1)
                else
                    i1 += 500

                st.rewardItems(57, i1)

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31148-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level < 42 || !st.hasQuestItems(PIRATE_TREASURE_MAP)) "30890-01.htm" else "30890-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ESPEN -> if (cond == 1)
                        htmltext = "30890-07a.htm"
                    else
                        htmltext = "30890-12.htm"

                    PIRATE_CHEST -> if (cond == 2)
                        htmltext = "31148-01.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q383_SearchingForTreasure"

        // NPCs
        private val ESPEN = 30890
        private val PIRATE_CHEST = 31148

        // Items
        private val PIRATE_TREASURE_MAP = 5915
        private val THIEF_KEY = 1661
    }
}