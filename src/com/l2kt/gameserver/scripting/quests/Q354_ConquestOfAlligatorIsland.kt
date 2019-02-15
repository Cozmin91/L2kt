package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q354_ConquestOfAlligatorIsland : Quest(354, "Conquest of Alligator Island") {
    init {
        DROPLIST[20804] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 490000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Crokian Lad
        DROPLIST[20805] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 560000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Dailaon Lad
        DROPLIST[20806] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 500000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Crokian Lad Warrior
        DROPLIST[20807] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 600000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Farhite Lad
        DROPLIST[20808] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 690000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Nos Lad
        DROPLIST[20991] = arrayOf(
            intArrayOf(ALLIGATOR_TOOTH, 1, 0, 600000),
            intArrayOf(TORN_MAP_FRAGMENT, 1, 0, 100000)
        ) // Swamp Tribe
    }

    init {

        setItemsIds(ALLIGATOR_TOOTH, TORN_MAP_FRAGMENT)

        addStartNpc(30895) // Kluck
        addTalkId(30895)

        addKillId(20804, 20805, 20806, 20807, 20808, 20991)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30895-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30895-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(TORN_MAP_FRAGMENT))
                htmltext = "30895-03a.htm"
        } else if (event.equals("30895-05.htm", ignoreCase = true)) {
            val amount = st.getQuestItemsCount(ALLIGATOR_TOOTH)
            if (amount > 0) {
                var reward = amount * 220 + 3100
                if (amount >= 100) {
                    reward += 7600
                    htmltext = "30895-05b.htm"
                } else
                    htmltext = "30895-05a.htm"

                st.takeItems(ALLIGATOR_TOOTH, -1)
                st.rewardItems(57, reward)
            }
        } else if (event.equals("30895-07.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TORN_MAP_FRAGMENT) >= 10) {
                htmltext = "30895-08.htm"
                st.takeItems(TORN_MAP_FRAGMENT, 10)
                st.giveItems(PIRATE_TREASURE_MAP, 1)
                st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("30895-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 38) "30895-00.htm" else "30895-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.hasQuestItems(TORN_MAP_FRAGMENT)) "30895-03a.htm" else "30895-03.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropMultipleItems(DROPLIST[npc.npcId] ?: return null)

        return null
    }

    companion object {
        private val qn = "Q354_ConquestOfAlligatorIsland"

        // Items
        private val ALLIGATOR_TOOTH = 5863
        private val TORN_MAP_FRAGMENT = 5864
        private val PIRATE_TREASURE_MAP = 5915

        private val DROPLIST = HashMap<Int, Array<IntArray>>()
    }
}