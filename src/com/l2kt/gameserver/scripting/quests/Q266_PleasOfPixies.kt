package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q266_PleasOfPixies : Quest(266, "Pleas of Pixies") {
    init {

        setItemsIds(PREDATOR_FANG)

        addStartNpc(31852) // Murika
        addTalkId(31852)

        addKillId(20525, 20530, 20534, 20537)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("31852-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "31852-00.htm"
            else if (player.level < 3)
                htmltext = "31852-01.htm"
            else
                htmltext = "31852-02.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(PREDATOR_FANG) < 100)
                htmltext = "31852-04.htm"
            else {
                htmltext = "31852-05.htm"
                st.takeItems(PREDATOR_FANG, -1)

                val n = Rnd[100]
                if (n < 10) {
                    st.playSound(QuestState.SOUND_JACKPOT)
                    st.rewardItems(EMERALD, 1)
                } else if (n < 30)
                    st.rewardItems(BLUE_ONYX, 1)
                else if (n < 60)
                    st.rewardItems(ONYX, 1)
                else
                    st.rewardItems(GLASS_SHARD, 1)

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            20525 -> if (st.dropItemsAlways(PREDATOR_FANG, Rnd[2, 3], 100))
                st["cond"] = "2"

            20530 -> if (st.dropItems(PREDATOR_FANG, 1, 100, 800000))
                st["cond"] = "2"

            20534 -> if (st.dropItems(PREDATOR_FANG, if (Rnd[3] == 0) 1 else 2, 100, 600000))
                st["cond"] = "2"

            20537 -> if (st.dropItemsAlways(PREDATOR_FANG, 2, 100))
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q266_PleasOfPixies"

        // Items
        private val PREDATOR_FANG = 1334

        // Rewards
        private val GLASS_SHARD = 1336
        private val EMERALD = 1337
        private val BLUE_ONYX = 1338
        private val ONYX = 1339
    }
}