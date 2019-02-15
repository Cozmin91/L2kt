package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q357_WarehouseKeepersAmbition : Quest(357, "Warehouse Keeper's Ambition") {
    init {
        CHANCES[FOREST_RUNNER] = 400000
        CHANCES[FLINE_ELDER] = 410000
        CHANCES[LIELE_ELDER] = 440000
        CHANCES[VALLEY_TREANT_ELDER] = 650000
    }

    init {

        setItemsIds(JADE_CRYSTAL)

        addStartNpc(30686) // Silva
        addTalkId(30686)

        addKillId(FOREST_RUNNER, FLINE_ELDER, LIELE_ELDER, VALLEY_TREANT_ELDER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30686-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30686-7.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(JADE_CRYSTAL)
            if (count == 0)
                htmltext = "30686-4.htm"
            else {
                var reward = count * 425 + 3500
                if (count >= 100)
                    reward += 7400

                st.takeItems(JADE_CRYSTAL, -1)
                st.rewardItems(57, reward)
            }
        } else if (event.equals("30686-8.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 47) "30686-0a.htm" else "30686-0.htm"

            Quest.STATE_STARTED -> htmltext = if (!st.hasQuestItems(JADE_CRYSTAL)) "30686-4.htm" else "30686-6.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(JADE_CRYSTAL, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q357_WarehouseKeepersAmbition"

        // Item
        private val JADE_CRYSTAL = 5867

        // Monsters
        private val FOREST_RUNNER = 20594
        private val FLINE_ELDER = 20595
        private val LIELE_ELDER = 20596
        private val VALLEY_TREANT_ELDER = 20597

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}