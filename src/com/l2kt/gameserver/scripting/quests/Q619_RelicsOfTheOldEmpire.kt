package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q619_RelicsOfTheOldEmpire : Quest(619, "Relics of the Old Empire") {
    init {

        setItemsIds(RELICS)

        addStartNpc(GHOST_OF_ADVENTURER)
        addTalkId(GHOST_OF_ADVENTURER)

        for (id in 21396..21434)
        // IT monsters
            addKillId(id)

        // monsters at IT entrance
        addKillId(21798, 21799, 21800)

        for (id in 18120..18256)
        // Sepulchers monsters
            addKillId(id)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31538-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31538-09.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(RELICS) >= 1000) {
                htmltext = "31538-09.htm"
                st.takeItems(RELICS, 1000)
                st.giveItems(RCP_REWARDS[Rnd[RCP_REWARDS.size]], 1)
            } else
                htmltext = "31538-06.htm"
        } else if (event.equals("31538-10.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 74) "31538-02.htm" else "31538-01.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(RELICS) >= 1000)
                htmltext = "31538-04.htm"
            else if (st.hasQuestItems(ENTRANCE))
                htmltext = "31538-06.htm"
            else
                htmltext = "31538-07.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItemsAlways(RELICS, 1, 0)
        st.dropItems(ENTRANCE, 1, 0, 50000)

        return null
    }

    companion object {
        private val qn = "Q619_RelicsOfTheOldEmpire"

        // NPC
        private val GHOST_OF_ADVENTURER = 31538

        // Items
        private val RELICS = 7254
        private val ENTRANCE = 7075

        // Rewards ; all S grade weapons recipe (60%)
        private val RCP_REWARDS = intArrayOf(6881, 6883, 6885, 6887, 6891, 6893, 6895, 6897, 6899, 7580)
    }
}