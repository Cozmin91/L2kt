package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q331_ArrowOfVengeance : Quest(331, "Arrow of Vengeance") {
    init {

        setItemsIds(HARPY_FEATHER, MEDUSA_VENOM, WYRM_TOOTH)

        addStartNpc(30125) // Belton
        addTalkId(30125)

        addKillId(20145, 20158, 20176)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30125-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30125-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 32) "30125-01.htm" else "30125-02.htm"

            Quest.STATE_STARTED -> {
                val harpyFeather = st.getQuestItemsCount(HARPY_FEATHER)
                val medusaVenom = st.getQuestItemsCount(MEDUSA_VENOM)
                val wyrmTooth = st.getQuestItemsCount(WYRM_TOOTH)

                if (harpyFeather + medusaVenom + wyrmTooth > 0) {
                    htmltext = "30125-05.htm"
                    st.takeItems(HARPY_FEATHER, -1)
                    st.takeItems(MEDUSA_VENOM, -1)
                    st.takeItems(WYRM_TOOTH, -1)

                    var reward = harpyFeather * 78 + medusaVenom * 88 + wyrmTooth * 92
                    if (harpyFeather + medusaVenom + wyrmTooth > 10)
                        reward += 3100

                    st.rewardItems(57, reward)
                } else
                    htmltext = "30125-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20145 -> st.dropItems(HARPY_FEATHER, 1, 0, 500000)

            20158 -> st.dropItems(MEDUSA_VENOM, 1, 0, 500000)

            20176 -> st.dropItems(WYRM_TOOTH, 1, 0, 500000)
        }

        return null
    }

    companion object {
        private val qn = "Q331_ArrowOfVengeance"

        // Items
        private val HARPY_FEATHER = 1452
        private val MEDUSA_VENOM = 1453
        private val WYRM_TOOTH = 1454
    }
}