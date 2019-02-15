package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q258_BringWolfPelts : Quest(258, "Bring Wolf Pelts") {
    init {

        setItemsIds(WOLF_PELT)

        addStartNpc(30001) // Lector
        addTalkId(30001)

        addKillId(20120, 20442) // Wolf, Elder Wolf
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30001-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 3) "30001-01.htm" else "30001-02.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(WOLF_PELT) < 40)
                htmltext = "30001-05.htm"
            else {
                st.takeItems(WOLF_PELT, -1)
                val randomNumber = Rnd[16]

                // Reward is based on a random number (1D16).
                if (randomNumber == 0)
                    st.giveItems(COTTON_SHIRT, 1)
                else if (randomNumber < 6)
                    st.giveItems(LEATHER_PANTS, 1)
                else if (randomNumber < 9)
                    st.giveItems(LEATHER_SHIRT, 1)
                else if (randomNumber < 13)
                    st.giveItems(SHORT_LEATHER_GLOVES, 1)
                else
                    st.giveItems(TUNIC, 1)

                htmltext = "30001-06.htm"

                if (randomNumber == 0)
                    st.playSound(QuestState.SOUND_JACKPOT)
                else
                    st.playSound(QuestState.SOUND_FINISH)

                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(WOLF_PELT, 1, 40))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q258_BringWolfPelts"

        // Item
        private val WOLF_PELT = 702

        // Rewards
        private val COTTON_SHIRT = 390
        private val LEATHER_PANTS = 29
        private val LEATHER_SHIRT = 22
        private val SHORT_LEATHER_GLOVES = 1119
        private val TUNIC = 426
    }
}