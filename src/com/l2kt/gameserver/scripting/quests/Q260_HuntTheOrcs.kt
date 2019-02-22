package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q260_HuntTheOrcs : Quest(260, "Hunt the Orcs") {
    init {

        setItemsIds(ORC_AMULET, ORC_NECKLACE)

        addStartNpc(RAYEN)
        addTalkId(RAYEN)

        addKillId(
            KABOO_ORC,
            KABOO_ORC_ARCHER,
            KABOO_ORC_GRUNT,
            KABOO_ORC_FIGHTER,
            KABOO_ORC_FIGHTER_LEADER,
            KABOO_ORC_FIGHTER_LIEUTENANT
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30221-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30221-06.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30221-00.htm"
            else if (player.level < 6)
                htmltext = "30221-01.htm"
            else
                htmltext = "30221-02.htm"

            Quest.STATE_STARTED -> {
                val amulet = st.getQuestItemsCount(ORC_AMULET)
                val necklace = st.getQuestItemsCount(ORC_NECKLACE)

                if (amulet == 0 && necklace == 0)
                    htmltext = "30221-04.htm"
                else {
                    htmltext = "30221-05.htm"
                    st.takeItems(ORC_AMULET, -1)
                    st.takeItems(ORC_NECKLACE, -1)
                    st.rewardItems(57, amulet * 5 + necklace * 15)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            KABOO_ORC, KABOO_ORC_GRUNT, KABOO_ORC_ARCHER -> st.dropItems(ORC_AMULET, 1, 0, 500000)

            KABOO_ORC_FIGHTER, KABOO_ORC_FIGHTER_LEADER, KABOO_ORC_FIGHTER_LIEUTENANT -> st.dropItems(
                ORC_NECKLACE,
                1,
                0,
                500000
            )
        }

        return null
    }

    companion object {
        private val qn = "Q260_HuntTheOrcs"

        // NPC
        private val RAYEN = 30221

        // Items
        private val ORC_AMULET = 1114
        private val ORC_NECKLACE = 1115

        // Monsters
        private val KABOO_ORC = 20468
        private val KABOO_ORC_ARCHER = 20469
        private val KABOO_ORC_GRUNT = 20470
        private val KABOO_ORC_FIGHTER = 20471
        private val KABOO_ORC_FIGHTER_LEADER = 20472
        private val KABOO_ORC_FIGHTER_LIEUTENANT = 20473
    }
}