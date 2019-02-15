package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q048_ToTheImmortalPlateau : Quest(48, "To the Immortal Plateau") {
    init {

        setItemsIds(
            ORDER_DOCUMENT_1,
            ORDER_DOCUMENT_2,
            ORDER_DOCUMENT_3,
            MAGIC_SWORD_HILT,
            GEMSTONE_POWDER,
            PURIFIED_MAGIC_NECKLACE
        )

        addStartNpc(GALLADUCCI)
        addTalkId(GALLADUCCI, SANDRA, DUSTIN, GENTLER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30097-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ORDER_DOCUMENT_1, 1)
        } else if (event.equals("30094-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ORDER_DOCUMENT_1, 1)
            st.giveItems(MAGIC_SWORD_HILT, 1)
        } else if (event.equals("30097-06.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MAGIC_SWORD_HILT, 1)
            st.giveItems(ORDER_DOCUMENT_2, 1)
        } else if (event.equals("30090-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ORDER_DOCUMENT_2, 1)
            st.giveItems(GEMSTONE_POWDER, 1)
        } else if (event.equals("30097-09.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GEMSTONE_POWDER, 1)
            st.giveItems(ORDER_DOCUMENT_3, 1)
        } else if (event.equals("30116-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ORDER_DOCUMENT_3, 1)
            st.giveItems(PURIFIED_MAGIC_NECKLACE, 1)
        } else if (event.equals("30097-12.htm", ignoreCase = true)) {
            st.takeItems(MARK_OF_TRAVELER, -1)
            st.takeItems(PURIFIED_MAGIC_NECKLACE, 1)
            st.rewardItems(SCROLL_OF_ESCAPE_SPECIAL, 1)
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
            Quest.STATE_CREATED -> if (player.race == ClassRace.ORC && player.level >= 3) {
                if (st.hasQuestItems(MARK_OF_TRAVELER))
                    htmltext = "30097-02.htm"
                else
                    htmltext = "30097-01.htm"
            } else
                htmltext = "30097-01a.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GALLADUCCI -> if (cond == 1)
                        htmltext = "30097-04.htm"
                    else if (cond == 2)
                        htmltext = "30097-05.htm"
                    else if (cond == 3)
                        htmltext = "30097-07.htm"
                    else if (cond == 4)
                        htmltext = "30097-08.htm"
                    else if (cond == 5)
                        htmltext = "30097-10.htm"
                    else if (cond == 6)
                        htmltext = "30097-11.htm"

                    GENTLER -> if (cond == 1)
                        htmltext = "30094-01.htm"
                    else if (cond > 1)
                        htmltext = "30094-03.htm"

                    SANDRA -> if (cond == 3)
                        htmltext = "30090-01.htm"
                    else if (cond > 3)
                        htmltext = "30090-03.htm"

                    DUSTIN -> if (cond == 5)
                        htmltext = "30116-01.htm"
                    else if (cond == 6)
                        htmltext = "30116-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q048_ToTheImmortalPlateau"

        // NPCs
        private const val GALLADUCCI = 30097
        private const val GENTLER = 30094
        private const val SANDRA = 30090
        private const val DUSTIN = 30116

        // Items
        private const val ORDER_DOCUMENT_1 = 7563
        private const val ORDER_DOCUMENT_2 = 7564
        private const val ORDER_DOCUMENT_3 = 7565
        private const val MAGIC_SWORD_HILT = 7568
        private const val GEMSTONE_POWDER = 7567
        private const val PURIFIED_MAGIC_NECKLACE = 7566
        private const val MARK_OF_TRAVELER = 7570
        private const val SCROLL_OF_ESCAPE_SPECIAL = 7557
    }
}