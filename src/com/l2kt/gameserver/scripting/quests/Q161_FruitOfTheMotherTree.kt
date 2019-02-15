package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q161_FruitOfTheMotherTree : Quest(161, "Fruit of the Mothertree") {
    init {

        setItemsIds(ANDELLIA_LETTER, MOTHERTREE_FRUIT)

        addStartNpc(ANDELLIA)
        addTalkId(ANDELLIA, THALIA)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30362-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ANDELLIA_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30362-00.htm"
            else if (player.level < 3)
                htmltext = "30362-02.htm"
            else
                htmltext = "30362-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ANDELLIA -> if (cond == 1)
                        htmltext = "30362-05.htm"
                    else if (cond == 2) {
                        htmltext = "30362-06.htm"
                        st.takeItems(MOTHERTREE_FRUIT, 1)
                        st.rewardItems(57, 1000)
                        st.rewardExpAndSp(1000, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    THALIA -> if (cond == 1) {
                        htmltext = "30371-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ANDELLIA_LETTER, 1)
                        st.giveItems(MOTHERTREE_FRUIT, 1)
                    } else if (cond == 2)
                        htmltext = "30371-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q161_FruitOfTheMotherTree"

        // NPCs
        private val ANDELLIA = 30362
        private val THALIA = 30371

        // Items
        private val ANDELLIA_LETTER = 1036
        private val MOTHERTREE_FRUIT = 1037
    }
}