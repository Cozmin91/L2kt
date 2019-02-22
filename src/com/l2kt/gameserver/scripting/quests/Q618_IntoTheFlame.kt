package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q618_IntoTheFlame : Quest(618, "Into The Flame") {
    init {

        setItemsIds(VACUALITE_ORE, VACUALITE)

        addStartNpc(KLEIN)
        addTalkId(KLEIN, HILDA)

        // Kookaburras, Bandersnatches, Grendels
        addKillId(21274, 21275, 21276, 21277, 21282, 21283, 21284, 21285, 21290, 21291, 21292, 21293)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31540-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31540-05.htm", ignoreCase = true)) {
            st.takeItems(VACUALITE, 1)
            st.giveItems(FLOATING_STONE, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("31271-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31271-05.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(VACUALITE_ORE, -1)
            st.giveItems(VACUALITE, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "31540-01.htm" else "31540-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    KLEIN -> htmltext = if (cond == 4) "31540-04.htm" else "31540-03.htm"

                    HILDA -> if (cond == 1)
                        htmltext = "31271-01.htm"
                    else if (cond == 2)
                        htmltext = "31271-03.htm"
                    else if (cond == 3)
                        htmltext = "31271-04.htm"
                    else if (cond == 4)
                        htmltext = "31271-06.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player ?: return null, npc, "2") ?: return null

        if (st.dropItems(VACUALITE_ORE, 1, 50, 500000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private val qn = "Q618_IntoTheFlame"

        // NPCs
        private val KLEIN = 31540
        private val HILDA = 31271

        // Items
        private val VACUALITE_ORE = 7265
        private val VACUALITE = 7266

        // Reward
        private val FLOATING_STONE = 7267
    }
}