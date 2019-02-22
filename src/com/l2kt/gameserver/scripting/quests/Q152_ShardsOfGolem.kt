package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q152_ShardsOfGolem : Quest(152, "Shards of Golem") {
    init {

        setItemsIds(HARRIS_RECEIPT_1, HARRIS_RECEIPT_2, GOLEM_SHARD, TOOL_BOX)

        addStartNpc(HARRIS)
        addTalkId(HARRIS, ALTRAN)

        addKillId(STONE_GOLEM)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30035-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(HARRIS_RECEIPT_1, 1)
        } else if (event.equals("30283-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HARRIS_RECEIPT_1, 1)
            st.giveItems(HARRIS_RECEIPT_2, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 10) "30035-01a.htm" else "30035-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HARRIS -> if (cond < 4)
                        htmltext = "30035-03.htm"
                    else if (cond == 4) {
                        htmltext = "30035-04.htm"
                        st.takeItems(HARRIS_RECEIPT_2, 1)
                        st.takeItems(TOOL_BOX, 1)
                        st.giveItems(WOODEN_BREASTPLATE, 1)
                        st.rewardExpAndSp(5000, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ALTRAN -> if (cond == 1)
                        htmltext = "30283-01.htm"
                    else if (cond == 2)
                        htmltext = "30283-03.htm"
                    else if (cond == 3) {
                        htmltext = "30283-04.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GOLEM_SHARD, -1)
                        st.giveItems(TOOL_BOX, 1)
                    } else if (cond == 4)
                        htmltext = "30283-05.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (st.dropItems(GOLEM_SHARD, 1, 5, 300000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private val qn = "Q152_ShardsOfGolem"

        // Items
        private val HARRIS_RECEIPT_1 = 1008
        private val HARRIS_RECEIPT_2 = 1009
        private val GOLEM_SHARD = 1010
        private val TOOL_BOX = 1011

        // Reward
        private val WOODEN_BREASTPLATE = 23

        // NPCs
        private val HARRIS = 30035
        private val ALTRAN = 30283

        // Mob
        private val STONE_GOLEM = 20016
    }
}