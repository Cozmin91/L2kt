package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q659_IdRatherBeCollectingFairyBreath : Quest(659, "I'd Rather Be Collecting Fairy Breath") {
    init {

        setItemsIds(FAIRY_BREATH)

        addStartNpc(GALATEA)
        addTalkId(GALATEA)

        addKillId(GIGGLING_WIND, BABBLING_WIND, SOBBING_WIND)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30634-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30634-06.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(FAIRY_BREATH)
            if (count > 0) {
                st.takeItems(FAIRY_BREATH, count)
                if (count < 10)
                    st.rewardItems(57, count * 50)
                else
                    st.rewardItems(57, count * 50 + 5365)
            }
        } else if (event.equals("30634-08.htm", ignoreCase = true))
            st.exitQuest(true)

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 26) "30634-01.htm" else "30634-02.htm"

            Quest.STATE_STARTED -> htmltext = if (!st.hasQuestItems(FAIRY_BREATH)) "30634-04.htm" else "30634-05.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(FAIRY_BREATH, 1, 0, 900000)

        return null
    }

    companion object {
        private const val qn = "Q659_IdRatherBeCollectingFairyBreath"

        // NPCs
        private const val GALATEA = 30634

        // Item
        private const val FAIRY_BREATH = 8286

        // Monsters
        private const val SOBBING_WIND = 21023
        private const val BABBLING_WIND = 21024
        private const val GIGGLING_WIND = 21025
    }
}