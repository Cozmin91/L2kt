package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q158_SeedOfEvil : Quest(158, "Seed of Evil") {
    init {

        setItemsIds(CLAY_TABLET)

        addStartNpc(30031) // Biotin
        addTalkId(30031)

        addKillId(27016) // Nerkas
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30031-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 21) "30031-02.htm" else "30031-03.htm"

            Quest.STATE_STARTED -> if (!st.hasQuestItems(CLAY_TABLET))
                htmltext = "30031-05.htm"
            else {
                htmltext = "30031-06.htm"
                st.takeItems(CLAY_TABLET, 1)
                st.giveItems(ENCHANT_ARMOR_D, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        st["cond"] = "2"
        st.playSound(QuestState.SOUND_MIDDLE)
        st.giveItems(CLAY_TABLET, 1)

        return null
    }

    companion object {
        private val qn = "Q158_SeedOfEvil"

        // Item
        private val CLAY_TABLET = 1025

        // Reward
        private val ENCHANT_ARMOR_D = 956
    }
}