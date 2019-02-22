package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q508_AClansReputation : Quest(508, "A Clan's Reputation") {
    init {

        setItemsIds(
            THEMIS_SCALE,
            NUCLEUS_OF_HEKATON_PRIME,
            TIPHON_SHARD,
            GLAKI_NUCLEUS,
            RAHHA_FANG,
            NUCLEUS_OF_FLAMESTONE_GIANT
        )

        addStartNpc(SIR_ERIC_RODEMAI)
        addTalkId(SIR_ERIC_RODEMAI)

        addKillId(
            FLAMESTONE_GIANT,
            PALIBATI_QUEEN_THEMIS,
            HEKATON_PRIME,
            GARGOYLE_LORD_TIPHON,
            LAST_LESSER_GIANT_GLAKI,
            RAHHA
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (StringUtil.isDigit(event)) {
            htmltext = "30868-$event.htm"
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["raid"] = event
            st.playSound(QuestState.SOUND_ACCEPT)

            val evt = Integer.parseInt(event)

            val x = radar[evt - 1][0]
            val y = radar[evt - 1][1]
            val z = radar[evt - 1][2]

            if (x + y + z > 0)
                st.addRadar(x, y, z)
        } else if (event.equals("30868-7.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        val clan = player.clan

        when (st.state) {
            Quest.STATE_CREATED -> if (!player.isClanLeader)
                htmltext = "30868-0a.htm"
            else if (clan.level < 5)
                htmltext = "30868-0b.htm"
            else
                htmltext = "30868-0c.htm"

            Quest.STATE_STARTED -> {
                val raid = st.getInt("raid")
                val item = reward_list[raid - 1][1]

                if (!st.hasQuestItems(item))
                    htmltext = "30868-" + raid + "a.htm"
                else {
                    val reward = Rnd[reward_list[raid - 1][2], reward_list[raid - 1][3]]

                    htmltext = "30868-" + raid + "b.htm"
                    st.takeItems(item, 1)
                    clan.addReputationScore(reward)
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(
                            reward
                        )
                    )
                    clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        // Retrieve the qS of the clan leader.
        val st = getClanLeaderQuestState(player, npc)
        if (st == null || !st.isStarted)
            return null

        // Reward only if quest is setup on good index.
        val raid = st.getInt("raid")
        if (reward_list[raid - 1][0] == npc.npcId)
            st.dropItemsAlways(reward_list[raid - 1][1], 1, 1)

        return null
    }

    companion object {
        private val qn = "Q508_AClansReputation"

        // NPC
        private val SIR_ERIC_RODEMAI = 30868

        // Items
        private val NUCLEUS_OF_FLAMESTONE_GIANT = 8494
        private val THEMIS_SCALE = 8277
        private val NUCLEUS_OF_HEKATON_PRIME = 8279
        private val TIPHON_SHARD = 8280
        private val GLAKI_NUCLEUS = 8281
        private val RAHHA_FANG = 8282

        // Raidbosses
        private val FLAMESTONE_GIANT = 25524
        private val PALIBATI_QUEEN_THEMIS = 25252
        private val HEKATON_PRIME = 25140
        private val GARGOYLE_LORD_TIPHON = 25255
        private val LAST_LESSER_GIANT_GLAKI = 25245
        private val RAHHA = 25051

        // Reward list (itemId, minClanPoints, maxClanPoints)
        private val reward_list = arrayOf(
            intArrayOf(PALIBATI_QUEEN_THEMIS, THEMIS_SCALE, 65, 100),
            intArrayOf(HEKATON_PRIME, NUCLEUS_OF_HEKATON_PRIME, 40, 75),
            intArrayOf(GARGOYLE_LORD_TIPHON, TIPHON_SHARD, 30, 65),
            intArrayOf(LAST_LESSER_GIANT_GLAKI, GLAKI_NUCLEUS, 105, 140),
            intArrayOf(RAHHA, RAHHA_FANG, 40, 75),
            intArrayOf(FLAMESTONE_GIANT, NUCLEUS_OF_FLAMESTONE_GIANT, 60, 95)
        )

        // Radar
        private val radar = arrayOf(
            intArrayOf(192346, 21528, -3648),
            intArrayOf(191979, 54902, -7658),
            intArrayOf(170038, -26236, -3824),
            intArrayOf(171762, 55028, -5992),
            intArrayOf(117232, -9476, -3320),
            intArrayOf(144218, -5816, -4722)
        )
    }
}