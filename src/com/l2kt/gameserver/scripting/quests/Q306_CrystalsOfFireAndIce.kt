package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q306_CrystalsOfFireAndIce : Quest(306, "Crystals of Fire and Ice") {
    init {

        setItemsIds(FLAME_SHARD, ICE_SHARD)

        addStartNpc(30004) // Katerina
        addTalkId(30004)

        addKillId(20109, 20110, 20112, 20113, 20114, 20115)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30004-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30004-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 17) "30004-01.htm" else "30004-02.htm"

            Quest.STATE_STARTED -> {
                val totalItems = st.getQuestItemsCount(FLAME_SHARD) + st.getQuestItemsCount(ICE_SHARD)
                if (totalItems == 0)
                    htmltext = "30004-04.htm"
                else {
                    htmltext = "30004-05.htm"
                    st.takeItems(FLAME_SHARD, -1)
                    st.takeItems(ICE_SHARD, -1)
                    st.rewardItems(57, 30 * totalItems + if (totalItems > 10) 5000 else 0)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        for (drop in DROPLIST) {
            if (npc.npcId == drop[0]) {
                st.dropItems(drop[1], 1, 0, drop[2])
                break
            }
        }

        return null
    }

    companion object {
        private val qn = "Q306_CrystalsOfFireAndIce"

        // Items
        private val FLAME_SHARD = 1020
        private val ICE_SHARD = 1021

        // Droplist (npcId, itemId, chance)
        private val DROPLIST = arrayOf(
            intArrayOf(20109, FLAME_SHARD, 300000),
            intArrayOf(20110, ICE_SHARD, 300000),
            intArrayOf(20112, FLAME_SHARD, 400000),
            intArrayOf(20113, ICE_SHARD, 400000),
            intArrayOf(20114, FLAME_SHARD, 500000),
            intArrayOf(20115, ICE_SHARD, 500000)
        )
    }
}