package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q017_LightAndDarkness : Quest(17, "Light and Darkness") {
    init {

        setItemsIds(BLOOD_OF_SAINT)

        addStartNpc(HIERARCH)
        addTalkId(HIERARCH, SAINT_ALTAR_1, SAINT_ALTAR_2, SAINT_ALTAR_3, SAINT_ALTAR_4)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31517-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BLOOD_OF_SAINT, 4)
        } else if (event.equals("31508-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BLOOD_OF_SAINT)) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_SAINT, 1)
            } else
                htmltext = "31508-03.htm"
        } else if (event.equals("31509-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BLOOD_OF_SAINT)) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_SAINT, 1)
            } else
                htmltext = "31509-03.htm"
        } else if (event.equals("31510-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BLOOD_OF_SAINT)) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_SAINT, 1)
            } else
                htmltext = "31510-03.htm"
        } else if (event.equals("31511-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BLOOD_OF_SAINT)) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BLOOD_OF_SAINT, 1)
            } else
                htmltext = "31511-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 61) "31517-03.htm" else "31517-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HIERARCH -> if (cond == 5) {
                        htmltext = "31517-07.htm"
                        st.rewardExpAndSp(105527, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    } else {
                        if (st.hasQuestItems(BLOOD_OF_SAINT))
                            htmltext = "31517-05.htm"
                        else {
                            htmltext = "31517-06.htm"
                            st.exitQuest(true)
                        }
                    }

                    SAINT_ALTAR_1 -> if (cond == 1)
                        htmltext = "31508-01.htm"
                    else if (cond > 1)
                        htmltext = "31508-04.htm"

                    SAINT_ALTAR_2 -> if (cond == 2)
                        htmltext = "31509-01.htm"
                    else if (cond > 2)
                        htmltext = "31509-04.htm"

                    SAINT_ALTAR_3 -> if (cond == 3)
                        htmltext = "31510-01.htm"
                    else if (cond > 3)
                        htmltext = "31510-04.htm"

                    SAINT_ALTAR_4 -> if (cond == 4)
                        htmltext = "31511-01.htm"
                    else if (cond > 4)
                        htmltext = "31511-04.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q017_LightAndDarkness"

        // Items
        private const val BLOOD_OF_SAINT = 7168

        // NPCs
        private const val HIERARCH = 31517
        private const val SAINT_ALTAR_1 = 31508
        private const val SAINT_ALTAR_2 = 31509
        private const val SAINT_ALTAR_3 = 31510
        private const val SAINT_ALTAR_4 = 31511
    }
}