package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q359_ForSleeplessDeadmen : Quest(359, "For Sleepless Deadmen") {
    init {
        CHANCES[DOOM_SERVANT] = 320000
        CHANCES[DOOM_GUARD] = 340000
        CHANCES[DOOM_ARCHER] = 420000
    }

    init {

        setItemsIds(REMAINS)

        addStartNpc(30857) // Orven
        addTalkId(30857)

        addKillId(DOOM_SERVANT, DOOM_GUARD, DOOM_ARCHER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30857-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30857-10.htm", ignoreCase = true)) {
            st.giveItems(REWARD[Rnd[REWARD.size]], 4)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "30857-01.htm" else "30857-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30857-07.htm"
                else if (cond == 2) {
                    htmltext = "30857-08.htm"
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(REMAINS, -1)
                } else if (cond == 3)
                    htmltext = "30857-09.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(REMAINS, 1, 60, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q359_ForSleeplessDeadmen"

        // Item
        private val REMAINS = 5869

        // Monsters
        private val DOOM_SERVANT = 21006
        private val DOOM_GUARD = 21007
        private val DOOM_ARCHER = 21008

        // Reward
        private val REWARD = intArrayOf(6341, 6342, 6343, 6344, 6345, 6346, 5494, 5495)

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}