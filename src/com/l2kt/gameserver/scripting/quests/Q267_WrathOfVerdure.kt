package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q267_WrathOfVerdure : Quest(267, "Wrath of Verdure") {
    init {

        setItemsIds(GOBLIN_CLUB)

        addStartNpc(31853) // Bremec
        addTalkId(31853)

        addKillId(20325) // Goblin
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31853-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31853-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "31853-00.htm"
            else if (player.level < 4)
                htmltext = "31853-01.htm"
            else
                htmltext = "31853-02.htm"

            Quest.STATE_STARTED -> {
                val count = st.getQuestItemsCount(GOBLIN_CLUB)
                if (count > 0) {
                    htmltext = "31853-05.htm"
                    st.takeItems(GOBLIN_CLUB, -1)
                    st.rewardItems(SILVERY_LEAF, count)
                } else
                    htmltext = "31853-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(GOBLIN_CLUB, 1, 0, 500000)

        return null
    }

    companion object {
        private val qn = "Q267_WrathOfVerdure"

        // Items
        private val GOBLIN_CLUB = 1335

        // Reward
        private val SILVERY_LEAF = 1340
    }
}