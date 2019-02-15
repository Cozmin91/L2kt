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

class Q509_TheClansPrestige : Quest(509, "The Clan's Prestige") {
    init {

        setItemsIds(DAIMON_EYES, HESTIA_FAIRY_STONE, NUCLEUS_OF_LESSER_GOLEM, FALSTON_FANG, SHAID_TALON)

        addStartNpc(VALDIS)
        addTalkId(VALDIS)

        addKillId(DAIMON_THE_WHITE_EYED, HESTIA_GUARDIAN_DEITY, PLAGUE_GOLEM, DEMON_AGENT_FALSTON, QUEEN_SHYEED)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (StringUtil.isDigit(event)) {
            htmltext = "31331-$event.htm"
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
        } else if (event.equals("31331-6.htm", ignoreCase = true)) {
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
                htmltext = "31331-0a.htm"
            else if (clan.level < 6)
                htmltext = "31331-0b.htm"
            else
                htmltext = "31331-0c.htm"

            Quest.STATE_STARTED -> {
                val raid = st.getInt("raid")
                val item = reward_list[raid - 1][1]

                if (!st.hasQuestItems(item))
                    htmltext = "31331-" + raid + "a.htm"
                else {
                    val reward = Rnd[reward_list[raid - 1][2], reward_list[raid - 1][3]]

                    htmltext = "31331-" + raid + "b.htm"
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

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

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
        private val qn = "Q509_TheClansPrestige"

        // NPCs
        private val VALDIS = 31331

        // Items
        private val DAIMON_EYES = 8489
        private val HESTIA_FAIRY_STONE = 8490
        private val NUCLEUS_OF_LESSER_GOLEM = 8491
        private val FALSTON_FANG = 8492
        private val SHAID_TALON = 8493

        // Raid Bosses
        private val DAIMON_THE_WHITE_EYED = 25290
        private val HESTIA_GUARDIAN_DEITY = 25293
        private val PLAGUE_GOLEM = 25523
        private val DEMON_AGENT_FALSTON = 25322
        private val QUEEN_SHYEED = 25514

        // Reward list (itemId, minClanPoints, maxClanPoints)
        private val reward_list = arrayOf(
            intArrayOf(DAIMON_THE_WHITE_EYED, DAIMON_EYES, 180, 215),
            intArrayOf(HESTIA_GUARDIAN_DEITY, HESTIA_FAIRY_STONE, 430, 465),
            intArrayOf(PLAGUE_GOLEM, NUCLEUS_OF_LESSER_GOLEM, 380, 415),
            intArrayOf(DEMON_AGENT_FALSTON, FALSTON_FANG, 220, 255),
            intArrayOf(QUEEN_SHYEED, SHAID_TALON, 130, 165)
        )

        // Radar
        private val radar = arrayOf(
            intArrayOf(186320, -43904, -3175),
            intArrayOf(134672, -115600, -1216),
            intArrayOf(170000, -59900, -3848),
            intArrayOf(93296, -75104, -1824),
            intArrayOf(79635, -55612, -5980)
        )
    }
}