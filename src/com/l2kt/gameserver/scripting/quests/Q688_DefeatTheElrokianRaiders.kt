package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q688_DefeatTheElrokianRaiders : Quest(688, "Defeat the Elrokian Raiders!") {
    init {

        setItemsIds(DINOSAUR_FANG_NECKLACE)

        addStartNpc(DINN)
        addTalkId(DINN)

        addKillId(ELROKI)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32105-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32105-08.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE)
            if (count > 0) {
                st.takeItems(DINOSAUR_FANG_NECKLACE, -1)
                st.rewardItems(57, count * 3000)
            }
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("32105-06.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE)

            st.takeItems(DINOSAUR_FANG_NECKLACE, -1)
            st.rewardItems(57, count * 3000)
        } else if (event.equals("32105-07.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(DINOSAUR_FANG_NECKLACE)
            if (count >= 100) {
                st.takeItems(DINOSAUR_FANG_NECKLACE, 100)
                st.rewardItems(57, 450000)
            } else
                htmltext = "32105-04.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "32105-00.htm" else "32105-01.htm"

            Quest.STATE_STARTED -> htmltext =
                    if (!st.hasQuestItems(DINOSAUR_FANG_NECKLACE)) "32105-04.htm" else "32105-05.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(DINOSAUR_FANG_NECKLACE, 1, 0, 500000)

        return null
    }

    companion object {
        private const val qn = "Q688_DefeatTheElrokianRaiders"

        // Item
        private const val DINOSAUR_FANG_NECKLACE = 8785

        // NPC
        private const val DINN = 32105

        // Monster
        private const val ELROKI = 22214
    }
}