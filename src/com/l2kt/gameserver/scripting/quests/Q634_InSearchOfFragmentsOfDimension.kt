package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q634_InSearchOfFragmentsOfDimension : Quest(634, "In Search of Fragments of Dimension") {
    init {

        // Dimensional Gate Keepers.
        for (i in 31494..31507) {
            addStartNpc(i)
            addTalkId(i)
        }

        // All mobs.
        for (i in 21208..21255)
            addKillId(i)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("05.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "01a.htm" else "01.htm"

            Quest.STATE_STARTED -> htmltext = "03.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(DIMENSION_FRAGMENT, (npc.level * 0.15 + 2.6).toInt(), -1, 80000)

        return null
    }

    companion object {
        private val qn = "Q634_InSearchOfFragmentsOfDimension"

        // Items
        private val DIMENSION_FRAGMENT = 7079
    }
}