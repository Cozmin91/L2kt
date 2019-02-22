package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q264_KeenClaws : Quest(264, "Keen Claws") {
    init {

        setItemsIds(WOLF_CLAW)

        addStartNpc(30136) // Payne
        addTalkId(30136)

        addKillId(20003, 20456) // Goblin, Wolf
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30136-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 3) "30136-01.htm" else "30136-02.htm"

            Quest.STATE_STARTED -> {
                val count = st.getQuestItemsCount(WOLF_CLAW)
                if (count < 50)
                    htmltext = "30136-04.htm"
                else {
                    htmltext = "30136-05.htm"
                    st.takeItems(WOLF_CLAW, -1)

                    val n = Rnd[17]
                    if (n == 0) {
                        st.giveItems(WOODEN_HELMET, 1)
                        st.playSound(QuestState.SOUND_JACKPOT)
                    } else if (n < 2)
                        st.giveItems(57, 1000)
                    else if (n < 5)
                        st.giveItems(LEATHER_SANDALS, 1)
                    else if (n < 8) {
                        st.giveItems(STOCKINGS, 1)
                        st.giveItems(57, 50)
                    } else if (n < 11)
                        st.giveItems(HEALING_POTION, 1)
                    else if (n < 14)
                        st.giveItems(SHORT_GLOVES, 1)
                    else
                        st.giveItems(CLOTH_SHOES, 1)

                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (npc.npcId == 20003) {
            if (st.dropItems(WOLF_CLAW, if (Rnd.nextBoolean()) 2 else 4, 50, 500000))
                st["cond"] = "2"
        } else if (st.dropItemsAlways(WOLF_CLAW, if (Rnd[5] < 4) 1 else 2, 50))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q264_KeenClaws"

        // Item
        private val WOLF_CLAW = 1367

        // Rewards
        private val LEATHER_SANDALS = 36
        private val WOODEN_HELMET = 43
        private val STOCKINGS = 462
        private val HEALING_POTION = 1061
        private val SHORT_GLOVES = 48
        private val CLOTH_SHOES = 35
    }
}