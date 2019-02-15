package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q362_BardsMandolin : Quest(362, "Bard's Mandolin") {
    init {

        setItemsIds(SWAN_FLUTE, SWAN_LETTER)

        addStartNpc(SWAN)
        addTalkId(SWAN, NANARIN, GALION, WOODROW)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30957-3.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30957-7.htm", ignoreCase = true) || event.equals("30957-8.htm", ignoreCase = true)) {
            st.rewardItems(57, 10000)
            st.giveItems(4410, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30957-2.htm" else "30957-1.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SWAN -> if (cond == 1 || cond == 2)
                        htmltext = "30957-4.htm"
                    else if (cond == 3) {
                        htmltext = "30957-5.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(SWAN_LETTER, 1)
                    } else if (cond == 4)
                        htmltext = "30957-5a.htm"
                    else if (cond == 5)
                        htmltext = "30957-6.htm"

                    WOODROW -> if (cond == 1) {
                        htmltext = "30837-1.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 2)
                        htmltext = "30837-2.htm"
                    else if (cond > 2)
                        htmltext = "30837-3.htm"

                    GALION -> if (cond == 2) {
                        htmltext = "30958-1.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.giveItems(SWAN_FLUTE, 1)
                    } else if (cond > 2)
                        htmltext = "30958-2.htm"

                    NANARIN -> if (cond == 4) {
                        htmltext = "30956-1.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SWAN_FLUTE, 1)
                        st.takeItems(SWAN_LETTER, 1)
                    } else if (cond == 5)
                        htmltext = "30956-2.htm"
                }
            }
        }

        return htmltext
    }

    companion object {
        private val qn = "Q362_BardsMandolin"

        // Items
        private val SWAN_FLUTE = 4316
        private val SWAN_LETTER = 4317

        // NPCs
        private val SWAN = 30957
        private val NANARIN = 30956
        private val GALION = 30958
        private val WOODROW = 30837
    }
}