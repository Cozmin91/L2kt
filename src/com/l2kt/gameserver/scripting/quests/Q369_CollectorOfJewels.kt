package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q369_CollectorOfJewels : Quest(369, "Collector of Jewels") {
    init {
        DROPLIST[20609] = intArrayOf(FLARE_SHARD, 630000)
        DROPLIST[20612] = intArrayOf(FLARE_SHARD, 770000)
        DROPLIST[20749] = intArrayOf(FLARE_SHARD, 850000)
        DROPLIST[20616] = intArrayOf(FREEZING_SHARD, 600000)
        DROPLIST[20619] = intArrayOf(FREEZING_SHARD, 730000)
        DROPLIST[20747] = intArrayOf(FREEZING_SHARD, 850000)
    }

    init {

        setItemsIds(FLARE_SHARD, FREEZING_SHARD)

        addStartNpc(NELL)
        addTalkId(NELL)

        for (mob in DROPLIST.keys)
            addKillId(mob)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30376-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30376-07.htm", ignoreCase = true))
            st.playSound(QuestState.SOUND_ITEMGET)
        else if (event.equals("30376-08.htm", ignoreCase = true)) {
            st.exitQuest(true)
            st.playSound(QuestState.SOUND_FINISH)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 25) "30376-01.htm" else "30376-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val flare = st.getQuestItemsCount(FLARE_SHARD)
                val freezing = st.getQuestItemsCount(FREEZING_SHARD)

                if (cond == 1)
                    htmltext = "30376-04.htm"
                else if (cond == 2 && flare >= 50 && freezing >= 50) {
                    htmltext = "30376-05.htm"
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(FLARE_SHARD, -1)
                    st.takeItems(FREEZING_SHARD, -1)
                    st.rewardItems(ADENA, 12500)
                } else if (cond == 3)
                    htmltext = "30376-09.htm"
                else if (cond == 4 && flare >= 200 && freezing >= 200) {
                    htmltext = "30376-10.htm"
                    st.takeItems(FLARE_SHARD, -1)
                    st.takeItems(FREEZING_SHARD, -1)
                    st.rewardItems(ADENA, 63500)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        val drop = DROPLIST[npc.npcId]!!

        if (cond == 1) {
            if (st.dropItems(
                    drop[0],
                    1,
                    50,
                    drop[1]
                ) && st.getQuestItemsCount(if (drop[0] == FLARE_SHARD) FREEZING_SHARD else FLARE_SHARD) >= 50
            )
                st["cond"] = "2"
        } else if (cond == 3 && st.dropItems(
                drop[0],
                1,
                200,
                drop[1]
            ) && st.getQuestItemsCount(if (drop[0] == FLARE_SHARD) FREEZING_SHARD else FLARE_SHARD) >= 200
        )
            st["cond"] = "4"

        return null
    }

    companion object {
        private val qn = "Q369_CollectorOfJewels"

        // NPC
        private val NELL = 30376

        // Items
        private val FLARE_SHARD = 5882
        private val FREEZING_SHARD = 5883

        // Reward
        private val ADENA = 57

        // Droplist
        private val DROPLIST = HashMap<Int, IntArray>()
    }
}