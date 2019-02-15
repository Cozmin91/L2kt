package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q661_MakingTheHarvestGroundsSafe : Quest(661, "Making the Harvest Grounds Safe") {
    init {

        setItemsIds(STING_OF_GIANT_POISON_BEE, CLOUDY_GEM, TALON_OF_YOUNG_ARANEID)

        addStartNpc(NORMAN)
        addTalkId(NORMAN)

        addKillId(GIANT_POISON_BEE, CLOUDY_BEAST, YOUNG_ARANEID)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30210-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30210-04.htm", ignoreCase = true)) {
            val item1 = st.getQuestItemsCount(STING_OF_GIANT_POISON_BEE)
            val item2 = st.getQuestItemsCount(CLOUDY_GEM)
            val item3 = st.getQuestItemsCount(TALON_OF_YOUNG_ARANEID)
            var sum = 0

            sum = item1 * 57 + item2 * 56 + item3 * 60

            if (item1 + item2 + item3 >= 10)
                sum += 2871

            st.takeItems(STING_OF_GIANT_POISON_BEE, item1)
            st.takeItems(CLOUDY_GEM, item2)
            st.takeItems(TALON_OF_YOUNG_ARANEID, item3)
            st.rewardItems(ADENA, sum)
        } else if (event.equals("30210-06.htm", ignoreCase = true))
            st.exitQuest(true)

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 21) "30210-01a.htm" else "30210-01.htm"

            Quest.STATE_STARTED -> htmltext = if (st.hasAtLeastOneQuestItem(
                    STING_OF_GIANT_POISON_BEE,
                    CLOUDY_GEM,
                    TALON_OF_YOUNG_ARANEID
                )
            ) "30210-03.htm" else "30210-05.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(npc.npcId - 12812, 1, 0, 500000)

        return null
    }

    companion object {
        private const val qn = "Q661_MakingTheHarvestGroundsSafe"

        // NPC
        private const val NORMAN = 30210

        // Items
        private const val STING_OF_GIANT_POISON_BEE = 8283
        private const val CLOUDY_GEM = 8284
        private const val TALON_OF_YOUNG_ARANEID = 8285

        // Reward
        private const val ADENA = 57

        // Monsters
        private const val GIANT_POISON_BEE = 21095
        private const val CLOUDY_BEAST = 21096
        private const val YOUNG_ARANEID = 21097
    }
}