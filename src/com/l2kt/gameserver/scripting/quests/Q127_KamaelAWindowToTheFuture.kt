package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ExShowSlideshowKamael
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q127_KamaelAWindowToTheFuture : Quest(127, "Kamael: A Window to the Future") {
    init {

        setItemsIds(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ORC, MARK_DELF, MARK_ELF)

        addStartNpc(DOMINIC)
        addTalkId(DOMINIC, KLAUS, ALDER, AKLAN, OLTLIN, JURIS, RODEMAI)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31350-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(MARK_DOMINIC, 1)
        } else if (event.equals("31350-06.htm", ignoreCase = true)) {
            st.takeItems(MARK_HUMAN, -1)
            st.takeItems(MARK_DWARF, -1)
            st.takeItems(MARK_ELF, -1)
            st.takeItems(MARK_DELF, -1)
            st.takeItems(MARK_ORC, -1)
            st.takeItems(MARK_DOMINIC, -1)
            st.rewardItems(57, 159100)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30187-06.htm", ignoreCase = true))
            st["cond"] = "2"
        else if (event.equals("30187-08.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MARK_HUMAN, 1)
        } else if (event.equals("32092-05.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MARK_DWARF, 1)
        } else if (event.equals("31288-04.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MARK_ORC, 1)
        } else if (event.equals("30862-04.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MARK_DELF, 1)
        } else if (event.equals("30113-04.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(MARK_ELF, 1)
        } else if (event.equals("kamaelstory", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            player.sendPacket(ExShowSlideshowKamael.STATIC_PACKET)
            return null
        } else if (event.equals("30756-05.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        npc.npcId
        val cond = st.getInt("cond")

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "31350-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                KLAUS -> if (cond == 1)
                    htmltext = "30187-01.htm"
                else if (cond == 2)
                    htmltext = "30187-06.htm"

                ALDER -> if (cond == 3)
                    htmltext = "32092-01.htm"

                AKLAN -> if (cond == 4)
                    htmltext = "31288-01.htm"

                OLTLIN -> if (cond == 5)
                    htmltext = "30862-01.htm"

                JURIS -> if (cond == 6)
                    htmltext = "30113-01.htm"

                RODEMAI -> if (cond == 7)
                    htmltext = "30756-01.htm"
                else if (cond == 8)
                    htmltext = "30756-04.htm"

                DOMINIC -> if (cond == 9)
                    htmltext = "31350-05.htm"
            }

            Quest.STATE_COMPLETED -> {
                htmltext = Quest.alreadyCompletedMsg
                return htmltext
            }
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q127_KamaelAWindowToTheFuture"

        // NPCs
        private const val DOMINIC = 31350
        private const val KLAUS = 30187
        private const val ALDER = 32092
        private const val AKLAN = 31288
        private const val OLTLIN = 30862
        private const val JURIS = 30113
        private const val RODEMAI = 30756

        // Items
        private const val MARK_DOMINIC = 8939
        private const val MARK_HUMAN = 8940
        private const val MARK_DWARF = 8941
        private const val MARK_ORC = 8944
        private const val MARK_DELF = 8943
        private const val MARK_ELF = 8942
    }
}