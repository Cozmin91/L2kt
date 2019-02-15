package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q368_TrespassingIntoTheSacredArea : Quest(368, "Trespassing into the Sacred Area") {
    init {
        CHANCES[20794] = 500000
        CHANCES[20795] = 770000
        CHANCES[20796] = 500000
        CHANCES[20797] = 480000
    }

    init {

        setItemsIds(FANG)

        addStartNpc(RESTINA)
        addTalkId(RESTINA)

        addKillId(20794, 20795, 20796, 20797)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30926-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30926-05.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 36) "30926-01a.htm" else "30926-01.htm"

            Quest.STATE_STARTED -> {
                val fangs = st.getQuestItemsCount(FANG)
                if (fangs == 0)
                    htmltext = "30926-03.htm"
                else {
                    val reward = 250 * fangs + if (fangs > 10) 5730 else 2000

                    htmltext = "30926-04.htm"
                    st.takeItems(5881, -1)
                    st.rewardItems(57, reward)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(FANG, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q368_TrespassingIntoTheSacredArea"

        // NPC
        private val RESTINA = 30926

        // Item
        private val FANG = 5881

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}