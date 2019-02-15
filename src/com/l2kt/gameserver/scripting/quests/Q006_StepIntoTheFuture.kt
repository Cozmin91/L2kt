package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q006_StepIntoTheFuture : Quest(6, "Step into the Future") {
    init {

        setItemsIds(BAULRO_LETTER)

        addStartNpc(ROXXY)
        addTalkId(ROXXY, BAULRO, SIR_COLLIN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30006-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30033-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(BAULRO_LETTER, 1)
        } else if (event.equals("30311-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BAULRO_LETTER)) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BAULRO_LETTER, 1)
            } else
                htmltext = "30311-03.htm"
        } else if (event.equals("30006-06.htm", ignoreCase = true)) {
            st.giveItems(MARK_TRAVELER, 1)
            st.rewardItems(SOE_GIRAN, 1)
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.HUMAN || player.level < 3)
                htmltext = "30006-01.htm"
            else
                htmltext = "30006-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ROXXY -> if (cond == 1 || cond == 2)
                        htmltext = "30006-04.htm"
                    else if (cond == 3)
                        htmltext = "30006-05.htm"

                    BAULRO -> if (cond == 1)
                        htmltext = "30033-01.htm"
                    else if (cond == 2)
                        htmltext = "30033-03.htm"
                    else
                        htmltext = "30033-04.htm"

                    SIR_COLLIN -> if (cond == 2)
                        htmltext = "30311-01.htm"
                    else if (cond == 3)
                        htmltext = "30311-03a.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q006_StepIntoTheFuture"

        // NPCs
        private const val ROXXY = 30006
        private const val BAULRO = 30033
        private const val SIR_COLLIN = 30311

        // Items
        private const val BAULRO_LETTER = 7571

        // Rewards
        private const val MARK_TRAVELER = 7570
        private const val SOE_GIRAN = 7559
    }
}