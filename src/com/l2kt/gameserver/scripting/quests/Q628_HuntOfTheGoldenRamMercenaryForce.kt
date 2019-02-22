package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q628_HuntOfTheGoldenRamMercenaryForce : Quest(628, "Hunt of the Golden Ram Mercenary Force") {
    init {
        CHANCES[21508] = 500000
        CHANCES[21509] = 430000
        CHANCES[21510] = 521000
        CHANCES[21511] = 575000
        CHANCES[21512] = 746000
        CHANCES[21513] = 500000
        CHANCES[21514] = 430000
        CHANCES[21515] = 520000
        CHANCES[21516] = 531000
        CHANCES[21517] = 744000
    }

    init {

        setItemsIds(SPLINTER_STAKATO_CHITIN, NEEDLE_STAKATO_CHITIN, GOLDEN_RAM_BADGE_RECRUIT, GOLDEN_RAM_BADGE_SOLDIER)

        addStartNpc(KAHMAN)
        addTalkId(KAHMAN)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31554-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31554-03a.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100 && st.getInt("cond") == 1)
            // Giving GOLDEN_RAM_BADGE_RECRUIT Medals
            {
                htmltext = "31554-04.htm"
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(SPLINTER_STAKATO_CHITIN, -1)
                st.giveItems(GOLDEN_RAM_BADGE_RECRUIT, 1)
            }
        } else if (event.equals("31554-07.htm", ignoreCase = true))
        // Cancel Quest
        {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 66) "31554-01a.htm" else "31554-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100)
                        htmltext = "31554-03.htm"
                    else
                        htmltext = "31554-03a.htm"
                } else if (cond == 2) {
                    if (st.getQuestItemsCount(SPLINTER_STAKATO_CHITIN) >= 100 && st.getQuestItemsCount(
                            NEEDLE_STAKATO_CHITIN
                        ) >= 100
                    ) {
                        htmltext = "31554-05.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_FINISH)
                        st.takeItems(SPLINTER_STAKATO_CHITIN, -1)
                        st.takeItems(NEEDLE_STAKATO_CHITIN, -1)
                        st.takeItems(GOLDEN_RAM_BADGE_RECRUIT, 1)
                        st.giveItems(GOLDEN_RAM_BADGE_SOLDIER, 1)
                    } else if (!st.hasQuestItems(SPLINTER_STAKATO_CHITIN) && !st.hasQuestItems(NEEDLE_STAKATO_CHITIN))
                        htmltext = "31554-04b.htm"
                    else
                        htmltext = "31554-04a.htm"
                } else if (cond == 3)
                    htmltext = "31554-05a.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        val npcId = npc.npcId

        when (npcId) {
            21508, 21509, 21510, 21511, 21512 -> if (cond == 1 || cond == 2)
                st.dropItems(SPLINTER_STAKATO_CHITIN, 1, 100, CHANCES[npcId]!!)

            21513, 21514, 21515, 21516, 21517 -> if (cond == 2)
                st.dropItems(NEEDLE_STAKATO_CHITIN, 1, 100, CHANCES[npcId]!!)
        }

        return null
    }

    companion object {
        private val qn = "Q628_HuntOfTheGoldenRamMercenaryForce"

        // NPCs
        private val KAHMAN = 31554

        // Items
        private val SPLINTER_STAKATO_CHITIN = 7248
        private val NEEDLE_STAKATO_CHITIN = 7249
        private val GOLDEN_RAM_BADGE_RECRUIT = 7246
        private val GOLDEN_RAM_BADGE_SOLDIER = 7247

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}