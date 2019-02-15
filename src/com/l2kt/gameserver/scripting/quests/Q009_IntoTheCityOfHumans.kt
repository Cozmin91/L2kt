package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q009_IntoTheCityOfHumans : Quest(9, "Into the City of Humans") {

    // NPCs
    val PETUKAI = 30583
    val TANAPI = 30571
    val TAMIL = 30576

    // Rewards
    val MARK_OF_TRAVELER = 7570
    val SOE_GIRAN = 7126

    init {

        addStartNpc(PETUKAI)
        addTalkId(PETUKAI, TANAPI, TAMIL)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30583-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30571-01.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30576-01.htm", ignoreCase = true)) {
            st.giveItems(MARK_OF_TRAVELER, 1)
            st.rewardItems(SOE_GIRAN, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 3 && player.race == ClassRace.ORC)
                htmltext = "30583-00.htm"
            else
                htmltext = "30583-00a.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    PETUKAI -> if (cond == 1)
                        htmltext = "30583-01a.htm"

                    TANAPI -> if (cond == 1)
                        htmltext = "30571-00.htm"
                    else if (cond == 2)
                        htmltext = "30571-01a.htm"

                    TAMIL -> if (cond == 2)
                        htmltext = "30576-00.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q009_IntoTheCityOfHumans"
    }
}