package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q627_HeartInSearchOfPower : Quest(627, "Heart in Search of Power") {
    init {
        CHANCES[21520] = 550000
        CHANCES[21523] = 584000
        CHANCES[21524] = 621000
        CHANCES[21525] = 621000
        CHANCES[21526] = 606000
        CHANCES[21529] = 625000
        CHANCES[21530] = 578000
        CHANCES[21531] = 690000
        CHANCES[21532] = 671000
        CHANCES[21535] = 693000
        CHANCES[21536] = 615000
        CHANCES[21539] = 762000
        CHANCES[21540] = 762000
        CHANCES[21658] = 690000
    }

    init {
        REWARDS["adena"] = intArrayOf(0, 0, 100000)
        REWARDS["asofe"] = intArrayOf(4043, 13, 6400)
        REWARDS["thon"] = intArrayOf(4044, 13, 6400)
        REWARDS["enria"] = intArrayOf(4042, 6, 13600)
        REWARDS["mold"] = intArrayOf(4041, 3, 17200)
    }

    init {

        setItemsIds(BEAD_OF_OBEDIENCE)

        addStartNpc(NECROMANCER)
        addTalkId(NECROMANCER, ENFEUX)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31518-01.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31518-03.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BEAD_OF_OBEDIENCE) == 300) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(BEAD_OF_OBEDIENCE, -1)
                st.giveItems(SEAL_OF_LIGHT, 1)
            } else {
                htmltext = "31518-03a.htm"
                st["cond"] = "1"
                st.takeItems(BEAD_OF_OBEDIENCE, -1)
            }
        } else if (event.equals("31519-01.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SEAL_OF_LIGHT) == 1) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(SEAL_OF_LIGHT, 1)
                st.giveItems(GEM_OF_SAINTS, 1)
            }
        } else if (REWARDS.containsKey(event)) {
            if (st.getQuestItemsCount(GEM_OF_SAINTS) == 1) {
                htmltext = "31518-07.htm"
                st.takeItems(GEM_OF_SAINTS, 1)

                if (REWARDS[event]!![0] > 0)
                    st.giveItems(REWARDS[event]!![0], REWARDS[event]!![1])
                st.rewardItems(57, REWARDS[event]!![2])

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else
                htmltext = "31518-7.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 60) "31518-00a.htm" else "31518-00.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    NECROMANCER -> if (cond == 1)
                        htmltext = "31518-01a.htm"
                    else if (cond == 2)
                        htmltext = "31518-02.htm"
                    else if (cond == 3)
                        htmltext = "31518-04.htm"
                    else if (cond == 4)
                        htmltext = "31518-05.htm"

                    ENFEUX -> if (cond == 3)
                        htmltext = "31519-00.htm"
                    else if (cond == 4)
                        htmltext = "31519-02.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(BEAD_OF_OBEDIENCE, 1, 300, CHANCES[npc.npcId] ?: 0))
            st["cond"] = "2"

        return null
    }

    companion object {
        private val qn = "Q627_HeartInSearchOfPower"

        // NPCs
        private val NECROMANCER = 31518
        private val ENFEUX = 31519

        // Items
        private val SEAL_OF_LIGHT = 7170
        private val BEAD_OF_OBEDIENCE = 7171
        private val GEM_OF_SAINTS = 7172

        // Drop chances
        private val CHANCES = HashMap<Int, Int>()

        // Rewards
        private val REWARDS = HashMap<String, IntArray>()
    }
}