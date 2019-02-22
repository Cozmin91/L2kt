package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q380_BringOutTheFlavorOfIngredients : Quest(380, "Bring Out the Flavor of Ingredients!") {
    init {

        setItemsIds(RITRON_FRUIT, MOON_FACE_FLOWER, LEECH_FLUIDS)

        addStartNpc(30069) // Rollant
        addTalkId(30069)

        addKillId(DIRE_WOLF, KADIF_WEREWOLF, GIANT_MIST_LEECH)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30069-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30069-12.htm", ignoreCase = true)) {
            st.giveItems(JELLY_RECIPE, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 24) "30069-00.htm" else "30069-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30069-06.htm"
                else if (cond == 2) {
                    if (st.getQuestItemsCount(ANTIDOTE) >= 2) {
                        htmltext = "30069-07.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(RITRON_FRUIT, -1)
                        st.takeItems(MOON_FACE_FLOWER, -1)
                        st.takeItems(LEECH_FLUIDS, -1)
                        st.takeItems(ANTIDOTE, 2)
                    } else
                        htmltext = "30069-06.htm"
                } else if (cond == 3) {
                    htmltext = "30069-08.htm"
                    st["cond"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else if (cond == 4) {
                    htmltext = "30069-09.htm"
                    st["cond"] = "5"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else if (cond == 5) {
                    htmltext = "30069-10.htm"
                    st["cond"] = "6"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else if (cond == 6) {
                    st.giveItems(RITRON_JELLY, 1)
                    if (Rnd[100] < 55)
                        htmltext = "30069-11.htm"
                    else {
                        htmltext = "30069-13.htm"
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            DIRE_WOLF -> if (st.dropItems(RITRON_FRUIT, 1, 4, 100000))
                if (st.getQuestItemsCount(MOON_FACE_FLOWER) == 20 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
                    st["cond"] = "2"

            KADIF_WEREWOLF -> if (st.dropItems(MOON_FACE_FLOWER, 1, 20, 500000))
                if (st.getQuestItemsCount(RITRON_FRUIT) == 4 && st.getQuestItemsCount(LEECH_FLUIDS) == 10)
                    st["cond"] = "2"

            GIANT_MIST_LEECH -> if (st.dropItems(LEECH_FLUIDS, 1, 10, 500000))
                if (st.getQuestItemsCount(RITRON_FRUIT) == 4 && st.getQuestItemsCount(MOON_FACE_FLOWER) == 20)
                    st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q380_BringOutTheFlavorOfIngredients"

        // Monsters
        private val DIRE_WOLF = 20205
        private val KADIF_WEREWOLF = 20206
        private val GIANT_MIST_LEECH = 20225

        // Items
        private val RITRON_FRUIT = 5895
        private val MOON_FACE_FLOWER = 5896
        private val LEECH_FLUIDS = 5897
        private val ANTIDOTE = 1831

        // Rewards
        private val RITRON_JELLY = 5960
        private val JELLY_RECIPE = 5959
    }
}