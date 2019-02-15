package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q510_AClansReputation : Quest(510, "A Clan's Reputation") {
    init {

        setItemsIds(TYRANNOSAURUS_CLAW)

        addStartNpc(VALDIS)
        addTalkId(VALDIS)

        addKillId(22215, 22216, 22217)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31331-3.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31331-6.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (!player.isClanLeader || player.clan.level < 5) "31331-0.htm" else "31331-1.htm"

            Quest.STATE_STARTED -> {
                val count = 50 * st.getQuestItemsCount(TYRANNOSAURUS_CLAW)
                if (count > 0) {
                    val clan = player.clan

                    htmltext = "31331-7.htm"
                    st.takeItems(TYRANNOSAURUS_CLAW, -1)

                    clan.addReputationScore(count)
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.CLAN_QUEST_COMPLETED_AND_S1_POINTS_GAINED).addNumber(
                            count
                        )
                    )
                    clan.broadcastToOnlineMembers(PledgeShowInfoUpdate(clan))
                } else
                    htmltext = "31331-4.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        // Retrieve the qs of the clan leader.
        val st = getClanLeaderQuestState(player, npc)
        if (st == null || !st.isStarted)
            return null

        st.dropItemsAlways(TYRANNOSAURUS_CLAW, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q510_AClansReputation"

        // NPC
        private val VALDIS = 31331

        // Quest Item
        private val TYRANNOSAURUS_CLAW = 8767
    }
}