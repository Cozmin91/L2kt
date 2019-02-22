package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q375_WhisperOfDreams_Part2 : Quest(375, "Whisper of Dreams, Part 2") {
    init {

        setItemsIds(KARIK_HORN, CAVE_HOWLER_SKULL)

        addStartNpc(MANAKIA)
        addTalkId(MANAKIA)

        addKillId(KARIK, CAVE_HOWLER)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        // Manakia
        if (event.equals("30515-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.takeItems(MYSTERIOUS_STONE, 1)
        } else if (event.equals("30515-07.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (!st.hasQuestItems(MYSTERIOUS_STONE) || player.level < 60) "30515-01.htm" else "30515-02.htm"

            Quest.STATE_STARTED -> if (st.getQuestItemsCount(KARIK_HORN) >= 100 && st.getQuestItemsCount(
                    CAVE_HOWLER_SKULL
                ) >= 100
            ) {
                htmltext = "30515-05.htm"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(KARIK_HORN, 100)
                st.takeItems(CAVE_HOWLER_SKULL, 100)
                st.giveItems(REWARDS[Rnd[REWARDS.size]], 1)
            } else
                htmltext = "30515-04.htm"
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        // Drop horn or skull to anyone.
        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            KARIK -> st.dropItemsAlways(KARIK_HORN, 1, 100)

            CAVE_HOWLER -> st.dropItems(CAVE_HOWLER_SKULL, 1, 100, 900000)
        }

        return null
    }

    companion object {
        private val qn = "Q375_WhisperOfDreams_Part2"

        // NPCs
        private val MANAKIA = 30515

        // Monsters
        private val KARIK = 20629
        private val CAVE_HOWLER = 20624

        // Items
        private val MYSTERIOUS_STONE = 5887
        private val KARIK_HORN = 5888
        private val CAVE_HOWLER_SKULL = 5889

        // Rewards : A grade robe recipes
        private val REWARDS = intArrayOf(5348, 5350, 5352)
    }
}