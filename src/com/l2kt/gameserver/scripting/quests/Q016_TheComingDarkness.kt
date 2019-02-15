package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q016_TheComingDarkness : Quest(16, "The Coming Darkness") {
    init {

        setItemsIds(CRYSTAL_OF_SEAL)

        addStartNpc(HIERARCH)
        addTalkId(HIERARCH, EVIL_ALTAR_1, EVIL_ALTAR_2, EVIL_ALTAR_3, EVIL_ALTAR_4, EVIL_ALTAR_5)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31517-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(CRYSTAL_OF_SEAL, 5)
        } else if (event.equals("31512-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRYSTAL_OF_SEAL, 1)
        } else if (event.equals("31513-1.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRYSTAL_OF_SEAL, 1)
        } else if (event.equals("31514-1.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRYSTAL_OF_SEAL, 1)
        } else if (event.equals("31515-1.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRYSTAL_OF_SEAL, 1)
        } else if (event.equals("31516-1.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRYSTAL_OF_SEAL, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 62) "31517-0a.htm" else "31517-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                val npcId = npc.npcId

                when (npcId) {
                    HIERARCH -> if (cond == 6) {
                        htmltext = "31517-4.htm"
                        st.rewardExpAndSp(221958, 0)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    } else {
                        if (st.hasQuestItems(CRYSTAL_OF_SEAL))
                            htmltext = "31517-3.htm"
                        else {
                            htmltext = "31517-3a.htm"
                            st.exitQuest(true)
                        }
                    }

                    EVIL_ALTAR_1, EVIL_ALTAR_2, EVIL_ALTAR_3, EVIL_ALTAR_4, EVIL_ALTAR_5 -> {
                        val condAltar = npcId - 31511

                        if (cond == condAltar) {
                            if (st.hasQuestItems(CRYSTAL_OF_SEAL))
                                htmltext = npcId.toString() + "-0.htm"
                            else
                                htmltext = "altar_nocrystal.htm"
                        } else if (cond > condAltar)
                            htmltext = npcId.toString() + "-2.htm"
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q016_TheComingDarkness"

        // NPCs
        private const val HIERARCH = 31517
        private const val EVIL_ALTAR_1 = 31512
        private const val EVIL_ALTAR_2 = 31513
        private const val EVIL_ALTAR_3 = 31514
        private const val EVIL_ALTAR_4 = 31515
        private const val EVIL_ALTAR_5 = 31516

        // Item
        private const val CRYSTAL_OF_SEAL = 7167
    }
}