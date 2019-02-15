package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q385_YokeOfThePast : Quest(385, "Yoke of the Past") {
    init {
        CHANCES[21208] = 70000
        CHANCES[21209] = 80000
        CHANCES[21210] = 110000
        CHANCES[21211] = 110000
        CHANCES[21213] = 140000
        CHANCES[21214] = 190000
        CHANCES[21215] = 190000
        CHANCES[21217] = 240000
        CHANCES[21218] = 300000
        CHANCES[21219] = 300000
        CHANCES[21221] = 370000
        CHANCES[21222] = 460000
        CHANCES[21223] = 450000
        CHANCES[21224] = 500000
        CHANCES[21225] = 540000
        CHANCES[21226] = 660000
        CHANCES[21227] = 640000
        CHANCES[21228] = 700000
        CHANCES[21229] = 750000
        CHANCES[21230] = 910000
        CHANCES[21231] = 860000
        CHANCES[21236] = 120000
        CHANCES[21237] = 140000
        CHANCES[21238] = 190000
        CHANCES[21239] = 190000
        CHANCES[21240] = 220000
        CHANCES[21241] = 240000
        CHANCES[21242] = 300000
        CHANCES[21243] = 300000
        CHANCES[21244] = 340000
        CHANCES[21245] = 370000
        CHANCES[21246] = 460000
        CHANCES[21247] = 450000
        CHANCES[21248] = 500000
        CHANCES[21249] = 540000
        CHANCES[21250] = 660000
        CHANCES[21251] = 640000
        CHANCES[21252] = 700000
        CHANCES[21253] = 750000
        CHANCES[21254] = 910000
        CHANCES[21255] = 860000
    }

    init {

        setItemsIds(ANCIENT_SCROLL)

        addStartNpc(*GATEKEEPER_ZIGGURAT)
        addTalkId(*GATEKEEPER_ZIGGURAT)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("10.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "02.htm" else "01.htm"

            Quest.STATE_STARTED -> if (!st.hasQuestItems(ANCIENT_SCROLL))
                htmltext = "08.htm"
            else {
                htmltext = "09.htm"
                val count = st.getQuestItemsCount(ANCIENT_SCROLL)
                st.takeItems(ANCIENT_SCROLL, -1)
                st.rewardItems(BLANK_SCROLL, count)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(ANCIENT_SCROLL, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q385_YokeOfThePast"

        // NPCs
        private val GATEKEEPER_ZIGGURAT = intArrayOf(
            31095,
            31096,
            31097,
            31098,
            31099,
            31100,
            31101,
            31102,
            31103,
            31104,
            31105,
            31106,
            31107,
            31108,
            31109,
            31110,
            31114,
            31115,
            31116,
            31117,
            31118,
            31119,
            31120,
            31121,
            31122,
            31123,
            31124,
            31125,
            31126
        )

        // Item
        private val ANCIENT_SCROLL = 5902

        // Reward
        private val BLANK_SCROLL = 5965

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}