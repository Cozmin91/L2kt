package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q347_GoGetTheCalculator : Quest(347, "Go Get the Calculator") {
    init {

        setItemsIds(GEMSTONE_BEAST_CRYSTAL, CALCULATOR_QUEST)

        addStartNpc(BRUNON)
        addTalkId(BRUNON, SILVERA, SPIRON, BALANKI)

        addKillId(20540)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30526-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30533-03.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(57) >= 100) {
                htmltext = "30533-02.htm"
                st.takeItems(57, 100)

                if (st.getInt("cond") == 3)
                    st["cond"] = "4"
                else
                    st["cond"] = "2"

                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("30532-02.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 2)
                st["cond"] = "4"
            else
                st["cond"] = "3"

            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30526-08.htm", ignoreCase = true)) {
            st.takeItems(CALCULATOR_QUEST, -1)
            st.giveItems(CALCULATOR_REAL, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30526-09.htm", ignoreCase = true)) {
            st.takeItems(CALCULATOR_QUEST, -1)
            st.rewardItems(57, 1000)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 12) "30526-00.htm" else "30526-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BRUNON -> htmltext = if (!st.hasQuestItems(CALCULATOR_QUEST)) "30526-06.htm" else "30526-07.htm"

                    SPIRON -> htmltext = if (cond < 4) "30532-01.htm" else "30532-05.htm"

                    BALANKI -> htmltext = if (cond < 4) "30533-01.htm" else "30533-04.htm"

                    SILVERA -> if (cond < 4)
                        htmltext = "30527-00.htm"
                    else if (cond == 4) {
                        htmltext = "30527-01.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 5) {
                        if (st.getQuestItemsCount(GEMSTONE_BEAST_CRYSTAL) < 10)
                            htmltext = "30527-02.htm"
                        else {
                            htmltext = "30527-03.htm"
                            st["cond"] = "6"
                            st.takeItems(GEMSTONE_BEAST_CRYSTAL, -1)
                            st.giveItems(CALCULATOR_QUEST, 1)
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 6)
                        htmltext = "30527-04.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "5") ?: return null

        st.dropItems(GEMSTONE_BEAST_CRYSTAL, 1, 10, 500000)

        return null
    }

    companion object {
        private val qn = "Q347_GoGetTheCalculator"

        // NPCs
        private val BRUNON = 30526
        private val SILVERA = 30527
        private val SPIRON = 30532
        private val BALANKI = 30533

        // Items
        private val GEMSTONE_BEAST_CRYSTAL = 4286
        private val CALCULATOR_QUEST = 4285
        private val CALCULATOR_REAL = 4393
    }
}