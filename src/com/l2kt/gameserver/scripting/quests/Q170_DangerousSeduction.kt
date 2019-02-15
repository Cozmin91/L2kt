package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q170_DangerousSeduction : Quest(170, "Dangerous Seduction") {
    init {

        setItemsIds(NIGHTMARE_CRYSTAL)

        addStartNpc(30305) // Vellior
        addTalkId(30305)

        addKillId(27022) // Merkenis
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30305-04.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30305-00.htm"
            else if (player.level < 21)
                htmltext = "30305-02.htm"
            else
                htmltext = "30305-03.htm"

            Quest.STATE_STARTED -> if (st.hasQuestItems(NIGHTMARE_CRYSTAL)) {
                htmltext = "30305-06.htm"
                st.takeItems(NIGHTMARE_CRYSTAL, -1)
                st.rewardItems(57, 102680)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30305-05.htm"

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        st["cond"] = "2"
        st.playSound(QuestState.SOUND_MIDDLE)
        st.giveItems(NIGHTMARE_CRYSTAL, 1)

        return null
    }

    companion object {
        private val qn = "Q170_DangerousSeduction"

        // Item
        private val NIGHTMARE_CRYSTAL = 1046
    }
}