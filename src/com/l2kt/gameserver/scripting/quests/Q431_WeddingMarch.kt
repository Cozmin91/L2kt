package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q431_WeddingMarch : Quest(431, "Wedding March") {
    init {

        setItemsIds(SILVER_CRYSTAL)

        addStartNpc(KANTABILON)
        addTalkId(KANTABILON)

        addKillId(20786, 20787)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31042-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31042-05.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SILVER_CRYSTAL) < 50)
                htmltext = "31042-03.htm"
            else {
                st.takeItems(SILVER_CRYSTAL, -1)
                st.giveItems(WEDDING_ECHO_CRYSTAL, 25)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 38) "31042-00.htm" else "31042-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31042-02.htm"
                else if (cond == 2)
                    htmltext = if (st.getQuestItemsCount(SILVER_CRYSTAL) < 50) "31042-03.htm" else "31042-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        if (st.dropItems(SILVER_CRYSTAL, 1, 50, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q431_WeddingMarch"

        // NPC
        private val KANTABILON = 31042

        // Item
        private val SILVER_CRYSTAL = 7540

        // Reward
        private val WEDDING_ECHO_CRYSTAL = 7062
    }
}