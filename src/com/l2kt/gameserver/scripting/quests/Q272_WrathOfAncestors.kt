package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q272_WrathOfAncestors : Quest(272, "Wrath of Ancestors") {
    init {

        setItemsIds(GRAVE_ROBBERS_HEAD)

        addStartNpc(30572) // Livina
        addTalkId(30572)

        addKillId(20319, 20320) // Goblin Grave Robber, Goblin Tomb Raider Leader
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30572-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30572-00.htm"
            else if (player.level < 5)
                htmltext = "30572-01.htm"
            else
                htmltext = "30572-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30572-04.htm"
            else {
                htmltext = "30572-05.htm"
                st.takeItems(GRAVE_ROBBERS_HEAD, -1)
                st.rewardItems(57, 1500)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(GRAVE_ROBBERS_HEAD, 1, 50))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q272_WrathOfAncestors"

        // Item
        private val GRAVE_ROBBERS_HEAD = 1474
    }
}