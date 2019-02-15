package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q168_DeliverSupplies : Quest(168, "Deliver Supplies") {
    init {

        setItemsIds(JENNA_LETTER, SENTRY_BLADE_1, SENTRY_BLADE_2, SENTRY_BLADE_3, OLD_BRONZE_SWORD)

        addStartNpc(JENNA)
        addTalkId(JENNA, ROSELYN, KRISTIN, HARANT)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30349-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(JENNA_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30349-00.htm"
            else if (player.level < 3)
                htmltext = "30349-01.htm"
            else
                htmltext = "30349-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    JENNA -> if (cond == 1)
                        htmltext = "30349-04.htm"
                    else if (cond == 2) {
                        htmltext = "30349-05.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SENTRY_BLADE_1, 1)
                    } else if (cond == 3)
                        htmltext = "30349-07.htm"
                    else if (cond == 4) {
                        htmltext = "30349-06.htm"
                        st.takeItems(OLD_BRONZE_SWORD, 2)
                        st.rewardItems(57, 820)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    HARANT -> if (cond == 1) {
                        htmltext = "30360-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(JENNA_LETTER, 1)
                        st.giveItems(SENTRY_BLADE_1, 1)
                        st.giveItems(SENTRY_BLADE_2, 1)
                        st.giveItems(SENTRY_BLADE_3, 1)
                    } else if (cond == 2)
                        htmltext = "30360-02.htm"

                    ROSELYN -> if (cond == 3) {
                        if (st.hasQuestItems(SENTRY_BLADE_2)) {
                            htmltext = "30355-01.htm"
                            st.takeItems(SENTRY_BLADE_2, 1)
                            st.giveItems(OLD_BRONZE_SWORD, 1)
                            if (st.getQuestItemsCount(OLD_BRONZE_SWORD) == 2) {
                                st["cond"] = "4"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            }
                        } else
                            htmltext = "30355-02.htm"
                    } else if (cond == 4)
                        htmltext = "30355-02.htm"

                    KRISTIN -> if (cond == 3) {
                        if (st.hasQuestItems(SENTRY_BLADE_3)) {
                            htmltext = "30357-01.htm"
                            st.takeItems(SENTRY_BLADE_3, 1)
                            st.giveItems(OLD_BRONZE_SWORD, 1)
                            if (st.getQuestItemsCount(OLD_BRONZE_SWORD) == 2) {
                                st["cond"] = "4"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            }
                        } else
                            htmltext = "30357-02.htm"
                    } else if (cond == 4)
                        htmltext = "30357-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q168_DeliverSupplies"

        // Items
        private val JENNA_LETTER = 1153
        private val SENTRY_BLADE_1 = 1154
        private val SENTRY_BLADE_2 = 1155
        private val SENTRY_BLADE_3 = 1156
        private val OLD_BRONZE_SWORD = 1157

        // NPCs
        private val JENNA = 30349
        private val ROSELYN = 30355
        private val KRISTIN = 30357
        private val HARANT = 30360
    }
}