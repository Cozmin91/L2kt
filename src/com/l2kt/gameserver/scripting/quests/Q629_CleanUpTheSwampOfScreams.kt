package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q629_CleanUpTheSwampOfScreams : Quest(629, "Clean up the Swamp of Screams") {
    init {
        CHANCES[21508] = 500000
        CHANCES[21509] = 431000
        CHANCES[21510] = 521000
        CHANCES[21511] = 576000
        CHANCES[21512] = 746000
        CHANCES[21513] = 530000
        CHANCES[21514] = 538000
        CHANCES[21515] = 545000
        CHANCES[21516] = 553000
        CHANCES[21517] = 560000
    }

    init {

        setItemsIds(TALON_OF_STAKATO, GOLDEN_RAM_COIN)

        addStartNpc(PIERCE)
        addTalkId(PIERCE)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31553-1.htm", ignoreCase = true)) {
            if (player.level >= 66) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
            } else {
                htmltext = "31553-0a.htm"
                st.exitQuest(true)
            }
        } else if (event.equals("31553-3.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TALON_OF_STAKATO) >= 100) {
                st.takeItems(TALON_OF_STAKATO, 100)
                st.giveItems(GOLDEN_RAM_COIN, 20)
            } else
                htmltext = "31553-3a.htm"
        } else if (event.equals("31553-5.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        if (!st.hasAtLeastOneQuestItem(7246, 7247))
            return "31553-6.htm"

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 66) "31553-0a.htm" else "31553-0.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(TALON_OF_STAKATO) >= 100) "31553-2.htm" else "31553-1a.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(TALON_OF_STAKATO, 1, 100, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q629_CleanUpTheSwampOfScreams"

        // NPC
        private val PIERCE = 31553

        // ITEMS
        private val TALON_OF_STAKATO = 7250
        private val GOLDEN_RAM_COIN = 7251

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}