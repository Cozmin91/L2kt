package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q644_GraveRobberAnnihilation : Quest(644, "Grave Robber Annihilation") {
    init {

        setItemsIds(ORC_GRAVE_GOODS)

        addStartNpc(KARUDA)
        addTalkId(KARUDA)

        addKillId(22003, 22004, 22005, 22006, 22008)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("32017-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32017-04.htm"
            st.takeItems(ORC_GRAVE_GOODS, -1)

            val reward = REWARDS[Integer.parseInt(event)]
            st.rewardItems(reward[0], reward[1])

            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "32017-06.htm" else "32017-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32017-05.htm"
                else if (cond == 2)
                    htmltext = "32017-07.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(ORC_GRAVE_GOODS, 1, 120, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q644_GraveRobberAnnihilation"

        // Item
        private val ORC_GRAVE_GOODS = 8088

        // Rewards
        private val REWARDS = arrayOf(
            intArrayOf(1865, 30),
            intArrayOf(1867, 40),
            intArrayOf(1872, 40),
            intArrayOf(1871, 30),
            intArrayOf(1870, 30),
            intArrayOf(1869, 30)
        )

        // NPC
        private val KARUDA = 32017
    }
}