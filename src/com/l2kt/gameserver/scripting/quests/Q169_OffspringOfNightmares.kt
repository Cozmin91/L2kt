package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q169_OffspringOfNightmares : Quest(169, "Offspring of Nightmares") {
    init {

        setItemsIds(CRACKED_SKULL, PERFECT_SKULL)

        addStartNpc(30145) // Vlasty
        addTalkId(30145)

        addKillId(20105, 20025)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30145-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30145-08.htm", ignoreCase = true)) {
            val reward = 17000 + st.getQuestItemsCount(CRACKED_SKULL) * 20
            st.takeItems(PERFECT_SKULL, -1)
            st.takeItems(CRACKED_SKULL, -1)
            st.giveItems(BONE_GAITERS, 1)
            st.rewardItems(57, reward)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30145-00.htm"
            else if (player.level < 15)
                htmltext = "30145-02.htm"
            else
                htmltext = "30145-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1) {
                    if (st.hasQuestItems(CRACKED_SKULL))
                        htmltext = "30145-06.htm"
                    else
                        htmltext = "30145-05.htm"
                } else if (cond == 2)
                    htmltext = "30145-07.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (st.getInt("cond") == 1 && st.dropItems(PERFECT_SKULL, 1, 1, 200000))
            st["cond"] = "2"
        else
            st.dropItems(CRACKED_SKULL, 1, 0, 500000)

        return null
    }

    companion object {
        private val qn = "Q169_OffspringOfNightmares"

        // Items
        private val CRACKED_SKULL = 1030
        private val PERFECT_SKULL = 1031
        private val BONE_GAITERS = 31
    }
}