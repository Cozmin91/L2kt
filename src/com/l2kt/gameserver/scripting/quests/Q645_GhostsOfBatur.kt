package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q645_GhostsOfBatur : Quest(645, "Ghosts Of Batur") {
    init {

        addStartNpc(KARUDA)
        addTalkId(KARUDA)

        addKillId(22007, 22009, 22010, 22011, 22012, 22013, 22014, 22015, 22016)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("32017-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32017-07.htm"
            st.takeItems(CURSED_GRAVE_GOODS, -1)

            val reward = REWARDS[Integer.parseInt(event)]
            st.giveItems(reward[0], reward[1])

            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 23) "32017-02.htm" else "32017-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32017-04.htm"
                else if (cond == 2)
                    htmltext = "32017-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(CURSED_GRAVE_GOODS, 1, 180, 750000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q645_GhostsOfBatur"

        // NPC
        private val KARUDA = 32017

        // Item
        private val CURSED_GRAVE_GOODS = 8089

        // Rewards
        private val REWARDS = arrayOf(
            intArrayOf(1878, 18),
            intArrayOf(1879, 7),
            intArrayOf(1880, 4),
            intArrayOf(1881, 6),
            intArrayOf(1882, 10),
            intArrayOf(1883, 2)
        )
    }
}