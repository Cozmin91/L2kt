package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q371_ShriekOfGhosts : Quest(371, "Shriek of Ghosts") {
    init {
        CHANCES[20818] = intArrayOf(38, 43)
        CHANCES[20820] = intArrayOf(48, 56)
        CHANCES[20824] = intArrayOf(50, 58)
    }

    init {

        setItemsIds(URN, PORCELAIN)

        addStartNpc(REVA)
        addTalkId(REVA, PATRIN)

        addKillId(20818, 20820, 20824)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30867-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30867-07.htm", ignoreCase = true)) {
            var urns = st.getQuestItemsCount(URN)
            if (urns > 0) {
                st.takeItems(URN, urns)
                if (urns >= 100) {
                    urns += 13
                    htmltext = "30867-08.htm"
                } else
                    urns += 7
                st.rewardItems(57, urns * 1000)
            }
        } else if (event.equals("30867-10.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("APPR", ignoreCase = true)) {
            if (st.hasQuestItems(PORCELAIN)) {
                val chance = Rnd[100]

                st.takeItems(PORCELAIN, 1)

                if (chance < 2) {
                    st.giveItems(6003, 1)
                    htmltext = "30929-03.htm"
                } else if (chance < 32) {
                    st.giveItems(6004, 1)
                    htmltext = "30929-04.htm"
                } else if (chance < 62) {
                    st.giveItems(6005, 1)
                    htmltext = "30929-05.htm"
                } else if (chance < 77) {
                    st.giveItems(6006, 1)
                    htmltext = "30929-06.htm"
                } else
                    htmltext = "30929-07.htm"
            } else
                htmltext = "30929-02.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 59) "30867-01.htm" else "30867-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                REVA -> if (st.hasQuestItems(URN))
                    htmltext = if (st.hasQuestItems(PORCELAIN)) "30867-05.htm" else "30867-04.htm"
                else
                    htmltext = "30867-06.htm"

                PATRIN -> htmltext = "30929-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val chances = CHANCES[npc.npcId] ?: return null
        val random = Rnd[100]

        if (random < chances[1])
            st.dropItemsAlways(if (random < chances[0]) URN else PORCELAIN, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q371_ShriekOfGhosts"

        // NPCs
        private val REVA = 30867
        private val PATRIN = 30929

        // Item
        private val URN = 5903
        private val PORCELAIN = 6002

        // Drop chances
        private val CHANCES = HashMap<Int, IntArray>()
    }
}