package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q051_OFullesSpecialBait : Quest(51, "O'Fulle's Special Bait") {
    init {

        setItemsIds(LOST_BAIT)

        addStartNpc(31572) // O'Fulle
        addTalkId(31572)

        addKillId(20552) // Fettered Soul
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31572-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31572-07.htm", ignoreCase = true)) {
            htmltext = "31572-06.htm"
            st.takeItems(LOST_BAIT, -1)
            st.rewardItems(ICY_AIR_LURE, 4)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 36) "31572-02.htm" else "31572-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(LOST_BAIT) == 100) "31572-04.htm" else "31572-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(LOST_BAIT, 1, 100))
            st["cond"] = "2"

        return null
    }

    companion object {
        private const val qn = "Q051_OFullesSpecialBait"

        // Item
        private const val LOST_BAIT = 7622

        // Reward
        private const val ICY_AIR_LURE = 7611
    }
}