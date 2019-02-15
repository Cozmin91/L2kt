package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q641_AttackSailren : Quest(641, "Attack Sailren!") {
    init {

        setItemsIds(GAZKH_FRAGMENT)

        addStartNpc(STATUE)
        addTalkId(STATUE)

        addKillId(22196, 22197, 22198, 22199, 22218, 22223)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return null

        if (event.equals("32109-5.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32109-8.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(GAZKH_FRAGMENT) >= 30) {
                npc!!.broadcastPacket(MagicSkillUse(npc, player, 5089, 1, 3000, 0))
                st.takeItems(GAZKH_FRAGMENT, -1)
                st.giveItems(GAZKH, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                htmltext = "32109-6.htm"
                st["cond"] = "1"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level < 77)
                htmltext = "32109-3.htm"
            else {
                val st2 = player.getQuestState(Q126_TheNameOfEvil_2.qn)
                htmltext = if (st2 != null && st2.isCompleted) "32109-1.htm" else "32109-2.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "32109-5.htm"
                else if (cond == 2)
                    htmltext = "32109-7.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMember(player, npc, "cond", "1") ?: return null

        if (st.dropItems(GAZKH_FRAGMENT, 1, 30, 50000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q641_AttackSailren"

        // NPCs
        private val STATUE = 32109

        // Quest Item
        private val GAZKH_FRAGMENT = 8782
        private val GAZKH = 8784
    }
}