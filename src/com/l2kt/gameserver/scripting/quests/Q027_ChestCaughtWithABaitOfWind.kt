package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q027_ChestCaughtWithABaitOfWind : Quest(27, "Chest caught with a bait of wind") {
    init {

        setItemsIds(STRANGE_BLUEPRINT)

        addStartNpc(LANOSCO)
        addTalkId(LANOSCO, SHALING)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31570-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31570-07.htm", ignoreCase = true)) {
            if (st.hasQuestItems(LARGE_BLUE_TREASURE_CHEST)) {
                st["cond"] = "2"
                st.takeItems(LARGE_BLUE_TREASURE_CHEST, 1)
                st.giveItems(STRANGE_BLUEPRINT, 1)
            } else
                htmltext = "31570-08.htm"
        } else if (event.equals("31434-02.htm", ignoreCase = true)) {
            if (st.hasQuestItems(STRANGE_BLUEPRINT)) {
                htmltext = "31434-02.htm"
                st.takeItems(STRANGE_BLUEPRINT, 1)
                st.giveItems(BLACK_PEARL_RING, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "31434-03.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 27)
                htmltext = "31570-02.htm"
            else {
                val st2 = player.getQuestState("Q050_LanoscosSpecialBait")
                if (st2 != null && st2.isCompleted)
                    htmltext = "31570-01.htm"
                else
                    htmltext = "31570-03.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LANOSCO -> if (cond == 1)
                        htmltext = if (!st.hasQuestItems(LARGE_BLUE_TREASURE_CHEST)) "31570-06.htm" else "31570-05.htm"
                    else if (cond == 2)
                        htmltext = "31570-09.htm"

                    SHALING -> if (cond == 2)
                        htmltext = "31434-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q027_ChestCaughtWithABaitOfWind"

        // NPCs
        private const val LANOSCO = 31570
        private const val SHALING = 31434

        // Items
        private const val LARGE_BLUE_TREASURE_CHEST = 6500
        private const val STRANGE_BLUEPRINT = 7625
        private const val BLACK_PEARL_RING = 880
    }
}