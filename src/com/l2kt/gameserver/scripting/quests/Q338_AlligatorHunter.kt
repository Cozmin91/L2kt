package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q338_AlligatorHunter : Quest(338, "Alligator Hunter") {
    init {

        setItemsIds(ALLIGATOR_PELT)

        addStartNpc(30892) // Enverun
        addTalkId(30892)

        addKillId(20135) // Alligator
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30892-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30892-05.htm", ignoreCase = true)) {
            val pelts = st.getQuestItemsCount(ALLIGATOR_PELT)

            var reward = pelts * 60
            if (pelts > 10)
                reward += 3430

            st.takeItems(ALLIGATOR_PELT, -1)
            st.rewardItems(57, reward)
        } else if (event.equals("30892-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 40) "30892-00.htm" else "30892-01.htm"

            Quest.STATE_STARTED -> htmltext = if (st.hasQuestItems(ALLIGATOR_PELT)) "30892-03.htm" else "30892-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItemsAlways(ALLIGATOR_PELT, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q338_AlligatorHunter"

        // Item
        private val ALLIGATOR_PELT = 4337
    }
}