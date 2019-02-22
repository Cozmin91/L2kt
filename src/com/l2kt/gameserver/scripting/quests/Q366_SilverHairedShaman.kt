package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q366_SilverHairedShaman : Quest(366, "Silver Haired Shaman") {
    init {
        CHANCES[20986] = 560000
        CHANCES[20987] = 660000
        CHANCES[20988] = 620000
    }

    init {

        setItemsIds(HAIR)

        addStartNpc(DIETER)
        addTalkId(DIETER)

        addKillId(20986, 20987, 20988)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30111-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30111-6.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 48) "30111-0.htm" else "30111-1.htm"

            Quest.STATE_STARTED -> {
                val count = st.getQuestItemsCount(HAIR)
                if (count == 0)
                    htmltext = "30111-3.htm"
                else {
                    htmltext = "30111-4.htm"
                    st.takeItems(HAIR, -1)
                    st.rewardItems(57, 12070 + 500 * count)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(HAIR, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q366_SilverHairedShaman"

        // NPC
        private val DIETER = 30111

        // Item
        private val HAIR = 5874

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}