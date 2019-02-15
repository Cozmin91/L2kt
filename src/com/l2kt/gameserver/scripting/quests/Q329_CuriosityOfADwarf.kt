package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q329_CuriosityOfADwarf : Quest(329, "Curiosity of a Dwarf") {
    init {

        addStartNpc(30437) // Rolento
        addTalkId(30437)

        addKillId(20083, 20085) // Granite golem, Puncher
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30437-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30437-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 33) "30437-01.htm" else "30437-02.htm"

            Quest.STATE_STARTED -> {
                val golem = st.getQuestItemsCount(GOLEM_HEARTSTONE)
                val broken = st.getQuestItemsCount(BROKEN_HEARTSTONE)

                if (golem + broken == 0)
                    htmltext = "30437-04.htm"
                else {
                    htmltext = "30437-05.htm"
                    st.takeItems(GOLEM_HEARTSTONE, -1)
                    st.takeItems(BROKEN_HEARTSTONE, -1)
                    st.rewardItems(57, broken * 50 + golem * 1000 + if (golem + broken > 10) 1183 else 0)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = Rnd[100]
        if (chance < 2)
            st.dropItemsAlways(GOLEM_HEARTSTONE, 1, 0)
        else if (chance < (if (npc.npcId == 20083) 44 else 50))
            st.dropItemsAlways(BROKEN_HEARTSTONE, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q329_CuriosityOfADwarf"

        // Items
        private val GOLEM_HEARTSTONE = 1346
        private val BROKEN_HEARTSTONE = 1365
    }
}