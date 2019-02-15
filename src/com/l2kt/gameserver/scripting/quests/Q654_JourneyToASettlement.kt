package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q654_JourneyToASettlement : Quest(654, "Journey to a Settlement") {
    init {

        setItemsIds(ANTELOPE_SKIN)

        addStartNpc(31453) // Nameless Spirit
        addTalkId(31453)

        addKillId(21294, 21295) // Canyon Antelope, Canyon Antelope Slave
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31453-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31453-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31453-06.htm", ignoreCase = true)) {
            st.takeItems(ANTELOPE_SKIN, -1)
            st.giveItems(FORCE_FIELD_REMOVAL_SCROLL, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val prevSt = player.getQuestState("Q119_LastImperialPrince")
                htmltext =
                        if (prevSt == null || !prevSt.isCompleted || player.level < 74) "31453-00.htm" else "31453-01.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31453-02.htm"
                else if (cond == 2)
                    htmltext = "31453-04.htm"
                else if (cond == 3)
                    htmltext = "31453-05.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItems(ANTELOPE_SKIN, 1, 1, 50000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q654_JourneyToASettlement"

        // Item
        private const val ANTELOPE_SKIN = 8072

        // Reward
        private const val FORCE_FIELD_REMOVAL_SCROLL = 8073
    }
}