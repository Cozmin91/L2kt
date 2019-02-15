package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q039_RedEyedInvaders : Quest(39, "Red-Eyed Invaders") {
    init {
        FIRST_DP[MAILLE_LIZARDMAN_GUARD] = intArrayOf(RED_BONE_NECKLACE, BLACK_BONE_NECKLACE)
        FIRST_DP[MAILLE_LIZARDMAN] = intArrayOf(BLACK_BONE_NECKLACE, RED_BONE_NECKLACE)
        FIRST_DP[MAILLE_LIZARDMAN_SCOUT] = intArrayOf(BLACK_BONE_NECKLACE, RED_BONE_NECKLACE)
    }

    init {
        SECOND_DP[ARANEID] = intArrayOf(GEM_OF_MAILLE, INCENSE_POUCH, 500000)
        SECOND_DP[MAILLE_LIZARDMAN_GUARD] = intArrayOf(INCENSE_POUCH, GEM_OF_MAILLE, 300000)
        SECOND_DP[MAILLE_LIZARDMAN_SCOUT] = intArrayOf(INCENSE_POUCH, GEM_OF_MAILLE, 250000)
    }

    init {

        setItemsIds(BLACK_BONE_NECKLACE, RED_BONE_NECKLACE, INCENSE_POUCH, GEM_OF_MAILLE)

        addStartNpc(BABENCO)
        addTalkId(BABENCO, BATHIS)

        addKillId(MAILLE_LIZARDMAN, MAILLE_LIZARDMAN_SCOUT, MAILLE_LIZARDMAN_GUARD, ARANEID)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30334-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30332-1.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30332-3.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.takeItems(BLACK_BONE_NECKLACE, -1)
            st.takeItems(RED_BONE_NECKLACE, -1)
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30332-5.htm", ignoreCase = true)) {
            st.takeItems(INCENSE_POUCH, -1)
            st.takeItems(GEM_OF_MAILLE, -1)
            st.giveItems(GREEN_COLORED_LURE_HG, 60)
            st.giveItems(BABY_DUCK_RODE, 1)
            st.giveItems(FISHING_SHOT_NG, 500)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 20) "30334-2.htm" else "30334-0.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BABENCO -> htmltext = "30334-3.htm"

                    BATHIS -> if (cond == 1)
                        htmltext = "30332-0.htm"
                    else if (cond == 2)
                        htmltext = "30332-2a.htm"
                    else if (cond == 3)
                        htmltext = "30332-2.htm"
                    else if (cond == 4)
                        htmltext = "30332-3a.htm"
                    else if (cond == 5)
                        htmltext = "30332-4.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        val npcId = npc.npcId

        var st = getRandomPartyMember(player!!, npc, "2")
        if (st != null && npcId != ARANEID) {
            val list = FIRST_DP[npcId] ?: return null

            if (st.dropItems(list[0], 1, 100, 500000) && st.getQuestItemsCount(list[1]) == 100)
                st["cond"] = "3"
        } else {
            st = getRandomPartyMember(player, npc, "4")
            if (st != null && npcId != MAILLE_LIZARDMAN) {
                val list = SECOND_DP[npcId] ?: return null

                if (st.dropItems(list[0], 1, 30, list[2]) && st.getQuestItemsCount(list[1]) == 30)
                    st["cond"] = "5"
            }
        }

        return null
    }

    companion object {
        private const val qn = "Q039_RedEyedInvaders"

        // NPCs
        private const val BABENCO = 30334
        private const val BATHIS = 30332

        // Mobs
        private const val MAILLE_LIZARDMAN = 20919
        private const val MAILLE_LIZARDMAN_SCOUT = 20920
        private const val MAILLE_LIZARDMAN_GUARD = 20921
        private const val ARANEID = 20925

        // Items
        private const val BLACK_BONE_NECKLACE = 7178
        private const val RED_BONE_NECKLACE = 7179
        private const val INCENSE_POUCH = 7180
        private const val GEM_OF_MAILLE = 7181

        // First droplist
        private val FIRST_DP = HashMap<Int, IntArray>()

        // Second droplist
        private val SECOND_DP = HashMap<Int, IntArray>()

        // Rewards
        private const val GREEN_COLORED_LURE_HG = 6521
        private const val BABY_DUCK_RODE = 6529
        private const val FISHING_SHOT_NG = 6535
    }
}