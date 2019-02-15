package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q008_AnAdventureBegins : Quest(8, "An Adventure Begins") {
    init {

        setItemsIds(ROSELYN_NOTE)

        addStartNpc(JASMINE)
        addTalkId(JASMINE, ROSELYN, HARNE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30134-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30355-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(ROSELYN_NOTE, 1)
        } else if (event.equals("30144-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROSELYN_NOTE, 1)
        } else if (event.equals("30134-06.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.level >= 3 && player.race == ClassRace.DARK_ELF)
                htmltext = "30134-02.htm"
            else
                htmltext = "30134-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    JASMINE -> if (cond == 1 || cond == 2)
                        htmltext = "30134-04.htm"
                    else if (cond == 3)
                        htmltext = "30134-05.htm"

                    ROSELYN -> if (cond == 1)
                        htmltext = "30355-01.htm"
                    else if (cond == 2)
                        htmltext = "30355-03.htm"

                    HARNE -> if (cond == 2)
                        htmltext = "30144-01.htm"
                    else if (cond == 3)
                        htmltext = "30144-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q008_AnAdventureBegins"

        // NPCs
        private const val JASMINE = 30134
        private const val ROSELYN = 30355
        private const val HARNE = 30144

        // Items
        private const val ROSELYN_NOTE = 7573

        // Rewards
        private const val SOE_GIRAN = 7559
        private const val MARK_TRAVELER = 7570
    }
}