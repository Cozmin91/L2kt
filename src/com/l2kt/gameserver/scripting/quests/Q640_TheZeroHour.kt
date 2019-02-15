package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q640_TheZeroHour : Quest(640, "The Zero Hour") {
    init {

        setItemsIds(FANG_OF_STAKATO)

        addStartNpc(KAHMAN)
        addTalkId(KAHMAN)

        // All "spiked" stakatos types, except babies and cannibalistic followers.
        addKillId(
            22105,
            22106,
            22107,
            22108,
            22109,
            22110,
            22111,
            22113,
            22114,
            22115,
            22116,
            22117,
            22118,
            22119,
            22121
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31554-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31554-05.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(FANG_OF_STAKATO))
                htmltext = "31554-06.htm"
        } else if (event.equals("31554-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (StringUtil.isDigit(event)) {
            val reward = REWARDS[Integer.parseInt(event)]

            if (st.getQuestItemsCount(FANG_OF_STAKATO) >= reward[0]) {
                htmltext = "31554-09.htm"
                st.takeItems(FANG_OF_STAKATO, reward[0])
                st.rewardItems(reward[1], reward[2])
            } else
                htmltext = "31554-06.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 66)
                htmltext = "31554-00.htm"
            else {
                val st2 = player.getQuestState("Q109_InSearchOfTheNest")
                htmltext = if (st2 != null && st2.isCompleted) "31554-01.htm" else "31554-10.htm"
            }

            Quest.STATE_STARTED -> htmltext = if (st.hasQuestItems(FANG_OF_STAKATO)) "31554-04.htm" else "31554-03.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItemsAlways(FANG_OF_STAKATO, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q640_TheZeroHour"

        // NPC
        private val KAHMAN = 31554

        // Item
        private val FANG_OF_STAKATO = 8085

        private val REWARDS = arrayOf(
            intArrayOf(12, 4042, 1),
            intArrayOf(6, 4043, 1),
            intArrayOf(6, 4044, 1),
            intArrayOf(81, 1887, 10),
            intArrayOf(33, 1888, 5),
            intArrayOf(30, 1889, 10),
            intArrayOf(150, 5550, 10),
            intArrayOf(131, 1890, 10),
            intArrayOf(123, 1893, 5)
        )
    }
}