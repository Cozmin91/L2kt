package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q382_KailsMagicCoin : Quest(382, "Kail's Magic Coin") {
    init {

        setItemsIds(SILVER_BASILISK, GOLD_GOLEM, BLOOD_DRAGON)

        addStartNpc(30687) // Vergara
        addTalkId(30687)

        addKillId(FALLEN_ORC, FALLEN_ORC_ARCHER, FALLEN_ORC_SHAMAN, FALLEN_ORC_CAPTAIN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30687-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext =
                    if (player.level < 55 || !st.hasQuestItems(ROYAL_MEMBERSHIP)) "30687-01.htm" else "30687-02.htm"

            Quest.STATE_STARTED -> htmltext = "30687-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            FALLEN_ORC -> st.dropItems(SILVER_BASILISK, 1, 0, 100000)

            FALLEN_ORC_ARCHER -> st.dropItems(GOLD_GOLEM, 1, 0, 100000)

            FALLEN_ORC_SHAMAN -> st.dropItems(BLOOD_DRAGON, 1, 0, 100000)

            FALLEN_ORC_CAPTAIN -> st.dropItems(5961 + Rnd[3], 1, 0, 100000)
        }

        return null
    }

    companion object {
        private val qn = "Q382_KailsMagicCoin"

        // Monsters
        private val FALLEN_ORC = 21017
        private val FALLEN_ORC_ARCHER = 21019
        private val FALLEN_ORC_SHAMAN = 21020
        private val FALLEN_ORC_CAPTAIN = 21022

        // Items
        private val ROYAL_MEMBERSHIP = 5898
        private val SILVER_BASILISK = 5961
        private val GOLD_GOLEM = 5962
        private val BLOOD_DRAGON = 5963
    }
}