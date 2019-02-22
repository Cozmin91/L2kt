package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q259_RanchersPlea : Quest(259, "Rancher's Plea") {
    init {

        setItemsIds(GIANT_SPIDER_SKIN)

        addStartNpc(EDMOND)
        addTalkId(EDMOND, MARIUS)

        addKillId(GIANT_SPIDER, TALON_SPIDER, BLADE_SPIDER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30497-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30497-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30405-04.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10) {
                st.takeItems(GIANT_SPIDER_SKIN, 10)
                st.rewardItems(HEALING_POTION, 1)
            } else
                htmltext = "<html><body>Incorrect item count</body></html>"
        } else if (event.equals("30405-05.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10) {
                st.takeItems(GIANT_SPIDER_SKIN, 10)
                st.rewardItems(WOODEN_ARROW, 50)
            } else
                htmltext = "<html><body>Incorrect item count</body></html>"
        } else if (event.equals("30405-07.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(GIANT_SPIDER_SKIN) >= 10)
                htmltext = "30405-06.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30497-01.htm" else "30497-02.htm"

            Quest.STATE_STARTED -> {
                val count = st.getQuestItemsCount(GIANT_SPIDER_SKIN)
                when (npc.npcId) {
                    EDMOND -> if (count == 0)
                        htmltext = "30497-04.htm"
                    else {
                        htmltext = "30497-05.htm"
                        st.takeItems(GIANT_SPIDER_SKIN, -1)
                        st.rewardItems(ADENA, (if (count >= 10) 250 else 0) + count * 25)
                    }

                    MARIUS -> htmltext = if (count < 10) "30405-01.htm" else "30405-02.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItemsAlways(GIANT_SPIDER_SKIN, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q259_RanchersPlea"

        // NPCs
        private val EDMOND = 30497
        private val MARIUS = 30405

        // Monsters
        private val GIANT_SPIDER = 20103
        private val TALON_SPIDER = 20106
        private val BLADE_SPIDER = 20108

        // Items
        private val GIANT_SPIDER_SKIN = 1495

        // Rewards
        private val ADENA = 57
        private val HEALING_POTION = 1061
        private val WOODEN_ARROW = 17
    }
}