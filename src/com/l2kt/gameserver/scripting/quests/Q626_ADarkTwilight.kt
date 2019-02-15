package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q626_ADarkTwilight : Quest(626, "A Dark Twilight") {
    init {
        CHANCES[21520] = 533000
        CHANCES[21523] = 566000
        CHANCES[21524] = 603000
        CHANCES[21525] = 603000
        CHANCES[21526] = 587000
        CHANCES[21529] = 606000
        CHANCES[21530] = 560000
        CHANCES[21531] = 669000
        CHANCES[21532] = 651000
        CHANCES[21535] = 672000
        CHANCES[21536] = 597000
        CHANCES[21539] = 739000
        CHANCES[21540] = 739000
        CHANCES[21658] = 669000
    }

    init {

        setItemsIds(BLOOD_OF_SAINT)

        addStartNpc(HIERARCH)
        addTalkId(HIERARCH)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31517-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("reward1", ignoreCase = true)) {
            if (st.getQuestItemsCount(BLOOD_OF_SAINT) == 300) {
                htmltext = "31517-07.htm"
                st.takeItems(BLOOD_OF_SAINT, 300)
                st.rewardExpAndSp(162773, 12500)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "31517-08.htm"
        } else if (event.equals("reward2", ignoreCase = true)) {
            if (st.getQuestItemsCount(BLOOD_OF_SAINT) == 300) {
                htmltext = "31517-07.htm"
                st.takeItems(BLOOD_OF_SAINT, 300)
                st.rewardItems(57, 100000)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "31517-08.htm"
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "31517-02.htm" else "31517-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31517-05.htm"
                else
                    htmltext = "31517-04.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(BLOOD_OF_SAINT, 1, 300, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q626_ADarkTwilight"

        // Items
        private val BLOOD_OF_SAINT = 7169

        // NPC
        private val HIERARCH = 31517

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}