package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q642_APowerfulPrimevalCreature : Quest(642, "A Powerful Primeval Creature") {
    init {

        setItemsIds(DINOSAUR_TISSUE, DINOSAUR_EGG)

        addStartNpc(32105) // Dinn
        addTalkId(32105)

        // Dinosaurs + egg
        addKillId(
            22196,
            22197,
            22198,
            22199,
            22200,
            22201,
            22202,
            22203,
            22204,
            22205,
            22218,
            22219,
            22220,
            22223,
            22224,
            22225,
            ANCIENT_EGG
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("32105-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32105-08.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG))
                htmltext = "32105-06.htm"
        } else if (event.equals("32105-07.htm", ignoreCase = true)) {
            val tissues = st.getQuestItemsCount(DINOSAUR_TISSUE)
            if (tissues > 0) {
                st.takeItems(DINOSAUR_TISSUE, -1)
                st.rewardItems(57, tissues * 5000)
            } else
                htmltext = "32105-08.htm"
        } else if (event.contains("event_")) {
            if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG)) {
                htmltext = "32105-07.htm"

                st.takeItems(DINOSAUR_TISSUE, 150)
                st.takeItems(DINOSAUR_EGG, 1)
                st.rewardItems(57, 44000)
                st.giveItems(
                    REWARDS[Integer.parseInt(event.split("_").dropLastWhile { it.isEmpty() }.toTypedArray()[1])],
                    1
                )
            } else
                htmltext = "32105-08.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "32105-00.htm" else "32105-01.htm"

            Quest.STATE_STARTED -> htmltext = if (!st.hasQuestItems(DINOSAUR_TISSUE)) "32105-08.htm" else "32105-05.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == ANCIENT_EGG) {
            if (Rnd[100] < 1) {
                st.giveItems(DINOSAUR_EGG, 1)

                if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150)
                    st.playSound(QuestState.SOUND_MIDDLE)
                else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (Rnd[100] < 33) {
            st.rewardItems(DINOSAUR_TISSUE, 1)

            if (st.getQuestItemsCount(DINOSAUR_TISSUE) >= 150 && st.hasQuestItems(DINOSAUR_EGG))
                st.playSound(QuestState.SOUND_MIDDLE)
            else
                st.playSound(QuestState.SOUND_ITEMGET)
        }

        return null
    }

    companion object {
        private val qn = "Q642_APowerfulPrimevalCreature"

        // Items
        private val DINOSAUR_TISSUE = 8774
        private val DINOSAUR_EGG = 8775

        private val ANCIENT_EGG = 18344

        // Rewards
        private val REWARDS = intArrayOf(8690, 8692, 8694, 8696, 8698, 8700, 8702, 8704, 8706, 8708, 8710)
    }
}