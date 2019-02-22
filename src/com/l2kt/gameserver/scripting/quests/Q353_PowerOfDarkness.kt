package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q353_PowerOfDarkness : Quest(353, "Power of Darkness") {
    init {

        setItemsIds(STONE)

        addStartNpc(31044) // Galman
        addTalkId(31044)

        addKillId(20244, 20245, 20283, 20284)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31044-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31044-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 55) "31044-01.htm" else "31044-02.htm"

            Quest.STATE_STARTED -> {
                val stones = st.getQuestItemsCount(STONE)
                if (stones == 0)
                    htmltext = "31044-05.htm"
                else {
                    htmltext = "31044-06.htm"
                    st.takeItems(STONE, -1)
                    st.rewardItems(57, 2500 + 230 * stones)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(STONE, 1, 0, if (npc.npcId == 20244 || npc.npcId == 20283) 480000 else 500000)

        return null
    }

    companion object {
        private val qn = "Q353_PowerOfDarkness"

        // Item
        private val STONE = 5862
    }
}