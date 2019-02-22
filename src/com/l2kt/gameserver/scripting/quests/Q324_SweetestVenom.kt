package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q324_SweetestVenom : Quest(324, "Sweetest Venom") {
    init {
        CHANCES[20034] = 220000
        CHANCES[20038] = 230000
        CHANCES[20043] = 250000
    }

    init {

        setItemsIds(VENOM_SAC)

        addStartNpc(30351) // Astaron
        addTalkId(30351)

        addKillId(20034, 20038, 20043)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30351-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 18) "30351-02.htm" else "30351-03.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30351-05.htm"
            else {
                htmltext = "30351-06.htm"
                st.takeItems(VENOM_SAC, -1)
                st.rewardItems(57, 5810)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(VENOM_SAC, 1, 10, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q324_SweetestVenom"

        // Item
        private val VENOM_SAC = 1077

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}