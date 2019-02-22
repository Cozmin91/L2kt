package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q643_RiseAndFallOfTheElrokiTribe : Quest(643, "Rise and Fall of the Elroki Tribe") {
    init {

        setItemsIds(BONES)

        addStartNpc(SINGSING)
        addTalkId(SINGSING, KARAKAWEI)

        addKillId(22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32106-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32106-07.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(BONES)

            st.takeItems(BONES, count)
            st.rewardItems(57, count * 1374)
        } else if (event.equals("32106-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("32117-03.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(BONES)
            if (count >= 300) {
                st.takeItems(BONES, 300)
                st.rewardItems(Rnd[8712, 8722], 5)
            } else
                htmltext = "32117-04.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "32106-00.htm" else "32106-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                SINGSING -> htmltext = if (st.hasQuestItems(BONES)) "32106-06.htm" else "32106-05.htm"

                KARAKAWEI -> htmltext = "32117-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(BONES, 1, 0, 750000)

        return null
    }

    companion object {
        private val qn = "Q643_RiseAndFallOfTheElrokiTribe"

        // NPCs
        private val SINGSING = 32106
        private val KARAKAWEI = 32117

        // Items
        private val BONES = 8776
    }
}