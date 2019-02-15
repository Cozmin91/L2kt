package com.l2kt.gameserver.scripting.quests

import java.util.HashMap

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q341_HuntingForWildBeasts : Quest(341, "Hunting for Wild Beasts") {
    init {
        CHANCES[20021] = 500000 // Red Bear
        CHANCES[20203] = 900000 // Dion Grizzly
        CHANCES[20310] = 500000 // Brown Bear
        CHANCES[20335] = 700000 // Grizzly Bear
    }

    init {

        setItemsIds(BEAR_SKIN)

        addStartNpc(30078) // Pano
        addTalkId(30078)

        addKillId(20021, 20203, 20310, 20335)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30078-02.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "30078-00.htm" else "30078-01.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(BEAR_SKIN) < 20)
                htmltext = "30078-03.htm"
            else {
                htmltext = "30078-04.htm"
                st.takeItems(BEAR_SKIN, -1)
                st.rewardItems(57, 3710)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(BEAR_SKIN, 1, 20, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q341_HuntingForWildBeasts"

        // Item
        private val BEAR_SKIN = 4259

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}