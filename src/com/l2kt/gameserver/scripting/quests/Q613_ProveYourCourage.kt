package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q613_ProveYourCourage : Quest(613, "Prove your courage!") {
    init {

        setItemsIds(HEAD_OF_HEKATON)

        addStartNpc(31377) // Ashas Varka Durai
        addTalkId(31377)

        addKillId(25299) // Hekaton
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31377-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31377-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(HEAD_OF_HEKATON)) {
                st.takeItems(HEAD_OF_HEKATON, -1)
                st.giveItems(FEATHER_OF_VALOR, 1)
                st.rewardExpAndSp(10000, 0)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                htmltext = "31377-06.htm"
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 75)
                htmltext = "31377-03.htm"
            else if (player.allianceWithVarkaKetra <= -3 && st.hasQuestItems(VARKA_ALLIANCE_3) && !st.hasQuestItems(
                    FEATHER_OF_VALOR
                )
            )
                htmltext = "31377-01.htm"
            else
                htmltext = "31377-02.htm"

            Quest.STATE_STARTED -> htmltext = if (st.hasQuestItems(HEAD_OF_HEKATON)) "31377-05.htm" else "31377-06.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        if (player != null) {
            for (st in getPartyMembers(player, npc, "cond", "1")) {
                if (st.player.allianceWithVarkaKetra <= -3 && st.hasQuestItems(VARKA_ALLIANCE_3)) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(HEAD_OF_HEKATON, 1)
                }
            }
        }
        return null
    }

    companion object {
        private val qn = "Q613_ProveYourCourage"

        // Items
        private val HEAD_OF_HEKATON = 7240
        private val FEATHER_OF_VALOR = 7229
        private val VARKA_ALLIANCE_3 = 7223
    }
}