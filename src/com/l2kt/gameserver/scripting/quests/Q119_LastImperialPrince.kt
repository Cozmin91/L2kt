package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q119_LastImperialPrince : Quest(119, "Last Imperial Prince") {
    init {

        setItemsIds(ANTIQUE_BROOCH)

        addStartNpc(NAMELESS_SPIRIT)
        addTalkId(NAMELESS_SPIRIT, DEVORIN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31453-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(ANTIQUE_BROOCH)) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else {
                htmltext = "31453-04b.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("32009-02.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ANTIQUE_BROOCH)) {
                htmltext = "31453-02a.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("32009-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31453-07.htm", ignoreCase = true)) {
            st.rewardItems(57, 68787)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (!st.hasQuestItems(ANTIQUE_BROOCH) || player.level < 74) "31453-00a.htm" else "31453-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    NAMELESS_SPIRIT -> if (cond == 1)
                        htmltext = "31453-04a.htm"
                    else if (cond == 2)
                        htmltext = "31453-05.htm"

                    DEVORIN -> if (cond == 1)
                        htmltext = "32009-01.htm"
                    else if (cond == 2)
                        htmltext = "32009-04.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = "31453-00b.htm"
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q119_LastImperialPrince"

        // NPCs
        private const val NAMELESS_SPIRIT = 31453
        private const val DEVORIN = 32009

        // Item
        private const val ANTIQUE_BROOCH = 7262
    }
}