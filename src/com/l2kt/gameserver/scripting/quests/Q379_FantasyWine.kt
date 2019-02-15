package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q379_FantasyWine : Quest(379, "Fantasy Wine") {
    init {

        setItemsIds(LEAF, STONE)

        addStartNpc(HARLAN)
        addTalkId(HARLAN)

        addKillId(ENKU_CHAMPION, ENKU_SHAMAN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30074-3.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30074-6.htm", ignoreCase = true)) {
            st.takeItems(LEAF, 80)
            st.takeItems(STONE, 100)

            val rand = Rnd[10]
            if (rand < 3) {
                htmltext = "30074-6.htm"
                st.giveItems(5956, 1)
            } else if (rand < 9) {
                htmltext = "30074-7.htm"
                st.giveItems(5957, 1)
            } else {
                htmltext = "30074-8.htm"
                st.giveItems(5958, 1)
            }

            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30074-2a.htm", ignoreCase = true))
            st.exitQuest(true)

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "30074-0a.htm" else "30074-0.htm"

            Quest.STATE_STARTED -> {
                val leaf = st.getQuestItemsCount(LEAF)
                val stone = st.getQuestItemsCount(STONE)

                if (leaf == 80 && stone == 100)
                    htmltext = "30074-5.htm"
                else if (leaf == 80)
                    htmltext = "30074-4a.htm"
                else if (stone == 100)
                    htmltext = "30074-4b.htm"
                else
                    htmltext = "30074-4.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == ENKU_CHAMPION) {
            if (st.dropItemsAlways(LEAF, 1, 80) && st.getQuestItemsCount(STONE) >= 100)
                st["cond"] = "2"
        } else if (st.dropItemsAlways(STONE, 1, 100) && st.getQuestItemsCount(LEAF) >= 80)
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q379_FantasyWine"

        // NPCs
        private val HARLAN = 30074

        // Monsters
        private val ENKU_CHAMPION = 20291
        private val ENKU_SHAMAN = 20292

        // Items
        private val LEAF = 5893
        private val STONE = 5894
    }
}