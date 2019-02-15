package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q053_LinnaeusSpecialBait : Quest(53, "Linnaues' Special Bait") {
    init {

        setItemsIds(CRIMSON_DRAKE_HEART)

        addStartNpc(31577) // Linnaeus
        addTalkId(31577)

        addKillId(20670) // Crimson Drake
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31577-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31577-07.htm", ignoreCase = true)) {
            htmltext = "31577-06.htm"
            st.takeItems(CRIMSON_DRAKE_HEART, -1)
            st.rewardItems(FLAMING_FISHING_LURE, 4)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "31577-02.htm" else "31577-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(CRIMSON_DRAKE_HEART) == 100) "31577-04.htm" else "31577-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(CRIMSON_DRAKE_HEART, 1, 100, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private const val qn = "Q053_LinnaeusSpecialBait"

        // Item
        private const val CRIMSON_DRAKE_HEART = 7624

        // Reward
        private const val FLAMING_FISHING_LURE = 7613
    }
}