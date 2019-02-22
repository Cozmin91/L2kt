package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q637_ThroughTheGateOnceMore : Quest(637, "Through the Gate Once More") {
    init {

        setItemsIds(NECROMANCER_HEART)

        addStartNpc(FLAURON)
        addTalkId(FLAURON)

        addKillId(21565, 21566, 21567)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("32010-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32010-10.htm", ignoreCase = true))
            st.exitQuest(true)

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 73 || !st.hasQuestItems(FADED_VISITOR_MARK))
                htmltext = "32010-01a.htm"
            else if (st.hasQuestItems(PAGAN_MARK))
                htmltext = "32010-00.htm"
            else
                htmltext = "32010-01.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 2) {
                if (st.getQuestItemsCount(NECROMANCER_HEART) == 10) {
                    htmltext = "32010-06.htm"
                    st.takeItems(FADED_VISITOR_MARK, 1)
                    st.takeItems(NECROMANCER_HEART, -1)
                    st.giveItems(PAGAN_MARK, 1)
                    st.giveItems(8273, 10)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                } else
                    st["cond"] = "1"
            } else
                htmltext = "32010-05.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(NECROMANCER_HEART, 1, 10, 400000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q637_ThroughTheGateOnceMore"

        // NPC
        private val FLAURON = 32010

        // Items
        private val FADED_VISITOR_MARK = 8065
        private val NECROMANCER_HEART = 8066

        // Reward
        private val PAGAN_MARK = 8067
    }
}