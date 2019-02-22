package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q295_DreamingOfTheSkies : Quest(295, "Dreaming of the Skies") {
    init {

        setItemsIds(FLOATING_STONE)

        addStartNpc(30536) // Arin
        addTalkId(30536)

        addKillId(20153) // Magical Weaver
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30536-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> htmltext = if (player.level < 11) "30536-01.htm" else "30536-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30536-04.htm"
            else {
                st.takeItems(FLOATING_STONE, -1)

                if (!st.hasQuestItems(RING_OF_FIREFLY)) {
                    htmltext = "30536-05.htm"
                    st.giveItems(RING_OF_FIREFLY, 1)
                } else {
                    htmltext = "30536-06.htm"
                    st.rewardItems(57, 2400)
                }

                st.rewardExpAndSp(0, 500)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(FLOATING_STONE, if (Rnd[100] > 25) 1 else 2, 50))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q295_DreamingOfTheSkies"

        // Item
        private val FLOATING_STONE = 1492

        // Reward
        private val RING_OF_FIREFLY = 1509
    }
}