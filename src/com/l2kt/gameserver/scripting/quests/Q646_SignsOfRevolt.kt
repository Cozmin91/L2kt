package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q646_SignsOfRevolt : Quest(646, "Signs Of Revolt") {
    init {

        setItemsIds(CURSED_DOLL)

        addStartNpc(TORRANT)
        addTalkId(TORRANT)

        addKillId(
            22029,
            22030,
            22031,
            22032,
            22033,
            22034,
            22035,
            22036,
            22037,
            22038,
            22039,
            22040,
            22041,
            22042,
            22043,
            22044,
            22045,
            22047,
            22049
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("32016-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (StringUtil.isDigit(event)) {
            htmltext = "32016-07.htm"
            st.takeItems(CURSED_DOLL, -1)

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
            Quest.STATE_CREATED -> htmltext = if (player.level < 40) "32016-02.htm" else "32016-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32016-04.htm"
                else if (cond == 2)
                    htmltext = "32016-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(CURSED_DOLL, 1, 180, 750000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q646_SignsOfRevolt"

        // NPC
        private val TORRANT = 32016

        // Item
        private val CURSED_DOLL = 8087

        // Rewards
        private val REWARDS =
            arrayOf(intArrayOf(1880, 9), intArrayOf(1881, 12), intArrayOf(1882, 20), intArrayOf(57, 21600))
    }
}