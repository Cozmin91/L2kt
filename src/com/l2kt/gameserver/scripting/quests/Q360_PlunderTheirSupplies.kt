package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q360_PlunderTheirSupplies : Quest(360, "Plunder Their Supplies") {
    init {

        setItemsIds(RECIPE_OF_SUPPLY, SUPPLY_ITEM, SUSPICIOUS_DOCUMENT)

        addStartNpc(30873) // Coleman
        addTalkId(30873)

        addKillId(20666, 20669)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30873-2.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30873-6.htm", ignoreCase = true)) {
            st.takeItems(SUPPLY_ITEM, -1)
            st.takeItems(SUSPICIOUS_DOCUMENT, -1)
            st.takeItems(RECIPE_OF_SUPPLY, -1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 52) "30873-0a.htm" else "30873-0.htm"

            Quest.STATE_STARTED -> {
                val supplyItems = st.getQuestItemsCount(SUPPLY_ITEM)
                if (supplyItems == 0)
                    htmltext = "30873-3.htm"
                else {
                    val reward = 6000 + supplyItems * 100 + st.getQuestItemsCount(RECIPE_OF_SUPPLY) * 6000

                    htmltext = "30873-5.htm"
                    st.takeItems(SUPPLY_ITEM, -1)
                    st.takeItems(RECIPE_OF_SUPPLY, -1)
                    st.rewardItems(57, reward)
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropMultipleItems(DROPLIST[if (npc.npcId == 20666) 0 else 1])

        if (st.getQuestItemsCount(SUSPICIOUS_DOCUMENT) == 5) {
            st.takeItems(SUSPICIOUS_DOCUMENT, 5)
            st.giveItems(RECIPE_OF_SUPPLY, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q360_PlunderTheirSupplies"

        // Items
        private val SUPPLY_ITEM = 5872
        private val SUSPICIOUS_DOCUMENT = 5871
        private val RECIPE_OF_SUPPLY = 5870

        private val DROPLIST = arrayOf(
            arrayOf(intArrayOf(SUSPICIOUS_DOCUMENT, 1, 0, 50000), intArrayOf(SUPPLY_ITEM, 1, 0, 500000)),
            arrayOf(intArrayOf(SUSPICIOUS_DOCUMENT, 1, 0, 50000), intArrayOf(SUPPLY_ITEM, 1, 0, 660000))
        )
    }
}