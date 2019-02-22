package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q632_NecromancersRequest : Quest(632, "Necromancer's Request") {
    init {

        setItemsIds(VAMPIRE_HEART, ZOMBIE_BRAIN)

        addStartNpc(31522) // Mysterious Wizard
        addTalkId(31522)

        addKillId(*VAMPIRES)
        addKillId(*UNDEADS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31522-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31522-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(VAMPIRE_HEART) >= 200) {
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(VAMPIRE_HEART, -1)
                st.rewardItems(57, 120000)
            } else
                htmltext = "31522-09.htm"
        } else if (event.equals("31522-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 63) "31522-01.htm" else "31522-02.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (st.getQuestItemsCount(VAMPIRE_HEART) >= 200) "31522-05.htm" else "31522-04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        for (undead in UNDEADS) {
            if (undead == npc.npcId) {
                st.dropItems(ZOMBIE_BRAIN, 1, 0, 330000)
                return null
            }
        }

        if (st.getInt("cond") == 1 && st.dropItems(VAMPIRE_HEART, 1, 200, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q632_NecromancersRequest"

        // Monsters
        private val VAMPIRES =
            intArrayOf(21568, 21573, 21582, 21585, 21586, 21587, 21588, 21589, 21590, 21591, 21592, 21593, 21594, 21595)

        private val UNDEADS =
            intArrayOf(21547, 21548, 21549, 21551, 21552, 21555, 21556, 21562, 21571, 21576, 21577, 21579)

        // Items
        private val VAMPIRE_HEART = 7542
        private val ZOMBIE_BRAIN = 7543
    }
}