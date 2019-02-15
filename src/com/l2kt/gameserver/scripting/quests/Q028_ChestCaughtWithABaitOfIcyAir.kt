package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q028_ChestCaughtWithABaitOfIcyAir : Quest(28, "Chest caught with a bait of icy air") {
    init {

        setItemsIds(KIKI_LETTER)

        addStartNpc(OFULLE)
        addTalkId(OFULLE, KIKI)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31572-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31572-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BIG_YELLOW_TREASURE_CHEST)) {
                st["cond"] = "2"
                st.takeItems(BIG_YELLOW_TREASURE_CHEST, 1)
                st.giveItems(KIKI_LETTER, 1)
            } else
                htmltext = "31572-08.htm"
        } else if (event.equals("31442-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(KIKI_LETTER)) {
                htmltext = "31442-02.htm"
                st.takeItems(KIKI_LETTER, 1)
                st.giveItems(ELVEN_RING, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "31442-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 36)
                htmltext = "31572-02.htm"
            else {
                val st2 = player.getQuestState("Q051_OFullesSpecialBait")
                if (st2 != null && st2.isCompleted)
                    htmltext = "31572-01.htm"
                else
                    htmltext = "31572-03.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    OFULLE -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(BIG_YELLOW_TREASURE_CHEST)) "31572-06.htm" else "31572-05.htm"
                    else if (cond == 2)
                        htmltext = "31572-09.htm"

                    KIKI -> if (cond == 2)
                        htmltext = "31442-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q028_ChestCaughtWithABaitOfIcyAir"

        // NPCs
        private const val OFULLE = 31572
        private const val KIKI = 31442

        // Items
        private const val BIG_YELLOW_TREASURE_CHEST = 6503
        private const val KIKI_LETTER = 7626
        private const val ELVEN_RING = 881
    }
}