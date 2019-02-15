package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q157_RecoverSmuggledGoods : Quest(157, "Recover Smuggled Goods") {
    init {

        setItemsIds(ADAMANTITE_ORE)

        addStartNpc(30005) // Wilford
        addTalkId(30005)

        addKillId(20121) // Toad
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30005-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 5) "30005-02.htm" else "30005-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30005-06.htm"
                else if (cond == 2) {
                    htmltext = "30005-07.htm"
                    st.takeItems(ADAMANTITE_ORE, -1)
                    st.giveItems(BUCKLER, 1)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(ADAMANTITE_ORE, 1, 20, 400000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q157_RecoverSmuggledGoods"

        // Item
        private val ADAMANTITE_ORE = 1024

        // Reward
        private val BUCKLER = 20
    }
}