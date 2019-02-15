package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q007_ATripBegins : Quest(7, "A Trip Begins") {
    init {

        setItemsIds(ARIEL_RECO)

        addStartNpc(MIRABEL)
        addTalkId(MIRABEL, ARIEL, ASTERIOS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30146-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30148-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(ARIEL_RECO, 1)
        } else if (event.equals("30154-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARIEL_RECO, 1)
        } else if (event.equals("30146-06.htm", ignoreCase = true)) {
            st.giveItems(MARK_TRAVELER, 1)
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30146-01.htm"
            else if (player.level < 3)
                htmltext = "30146-01a.htm"
            else
                htmltext = "30146-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MIRABEL -> if (cond == 1 || cond == 2)
                        htmltext = "30146-04.htm"
                    else if (cond == 3)
                        htmltext = "30146-05.htm"

                    ARIEL -> if (cond == 1)
                        htmltext = "30148-01.htm"
                    else if (cond == 2)
                        htmltext = "30148-03.htm"

                    ASTERIOS -> if (cond == 2)
                        htmltext = "30154-01.htm"
                    else if (cond == 3)
                        htmltext = "30154-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q007_ATripBegins"

        // NPCs
        private const val MIRABEL = 30146
        private const val ARIEL = 30148
        private const val ASTERIOS = 30154

        // Items
        private const val ARIEL_RECO = 7572

        // Rewards
        private const val MARK_TRAVELER = 7570
        private const val SOE_GIRAN = 7559
    }
}