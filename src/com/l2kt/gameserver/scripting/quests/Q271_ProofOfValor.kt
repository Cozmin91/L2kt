package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q271_ProofOfValor : Quest(271, "Proof of Valor") {
    init {

        setItemsIds(KASHA_WOLF_FANG)

        addStartNpc(30577) // Rukain
        addTalkId(30577)

        addKillId(20475) // Kasha Wolf
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30577-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            if (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR))
                htmltext = "30577-07.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30577-00.htm"
            else if (player.level < 4)
                htmltext = "30577-01.htm"
            else
                htmltext = if (st.hasAtLeastOneQuestItem(
                        NECKLACE_OF_COURAGE,
                        NECKLACE_OF_VALOR
                    )
                ) "30577-06.htm" else "30577-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = if (st.hasAtLeastOneQuestItem(
                        NECKLACE_OF_COURAGE,
                        NECKLACE_OF_VALOR
                    )
                ) "30577-07.htm" else "30577-04.htm"
            else {
                htmltext = "30577-05.htm"
                st.takeItems(KASHA_WOLF_FANG, -1)
                st.giveItems(if (Rnd[100] < 10) NECKLACE_OF_VALOR else NECKLACE_OF_COURAGE, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(KASHA_WOLF_FANG, if (Rnd[4] == 0) 2 else 1, 50))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q271_ProofOfValor"

        // Item
        private val KASHA_WOLF_FANG = 1473

        // Rewards
        private val NECKLACE_OF_VALOR = 1507
        private val NECKLACE_OF_COURAGE = 1506
    }
}