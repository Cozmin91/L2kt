package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q650_ABrokenDream : Quest(650, "A Broken Dream") {
    init {

        setItemsIds(DREAM_FRAGMENT)

        addStartNpc(GHOST)
        addTalkId(GHOST)
        addKillId(CREWMAN, VAGABOND)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32054-01a.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32054-03.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(DREAM_FRAGMENT))
                htmltext = "32054-04.htm"
        } else if (event.equals("32054-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val st2 = player.getQuestState("Q117_TheOceanOfDistantStars")
                if (st2 != null && st2.isCompleted && player.level >= 39)
                    htmltext = "32054-01.htm"
                else {
                    htmltext = "32054-00.htm"
                    st.exitQuest(true)
                }
            }

            Quest.STATE_STARTED -> htmltext = "32054-02.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(DREAM_FRAGMENT, 1, 0, 250000)

        return null
    }

    companion object {
        private val qn = "Q650_ABrokenDream"

        // NPC
        private val GHOST = 32054

        // Item
        private val DREAM_FRAGMENT = 8514

        // Monsters
        private val CREWMAN = 22027
        private val VAGABOND = 22028
    }
}