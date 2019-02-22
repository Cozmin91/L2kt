package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q372_LegacyOfInsolence : Quest(372, "Legacy of Insolence") {
    init {

        addStartNpc(WALDERAL)
        addTalkId(WALDERAL, PATRIN, HOLLY, CLAUDIA, DESMOND)

        addKillId(*MONSTERS_DROPS[0])
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30844-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30844-05b.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 1) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("30844-07.htm", ignoreCase = true)) {
            for (blueprint in 5989..6001) {
                if (!st.hasQuestItems(blueprint)) {
                    htmltext = "30844-06.htm"
                    break
                }
            }
        } else if (event.startsWith("30844-07-")) {
            checkAndRewardItems(st, 0, Integer.parseInt(event.substring(9, 10)), WALDERAL)
        } else if (event.equals("30844-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 59) "30844-01.htm" else "30844-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                WALDERAL -> htmltext = "30844-05.htm"

                HOLLY -> htmltext = checkAndRewardItems(st, 1, 4, HOLLY)

                PATRIN -> htmltext = checkAndRewardItems(st, 2, 5, PATRIN)

                CLAUDIA -> htmltext = checkAndRewardItems(st, 3, 6, CLAUDIA)

                DESMOND -> htmltext = checkAndRewardItems(st, 4, 7, DESMOND)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        for (i in 0 until MONSTERS_DROPS[0].size) {
            if (MONSTERS_DROPS[0][i] == npcId) {
                st.dropItems(MONSTERS_DROPS[1][i], 1, 0, MONSTERS_DROPS[2][i])
                break
            }
        }
        return null
    }

    companion object {
        private val qn = "Q372_LegacyOfInsolence"

        // NPCs
        private val WALDERAL = 30844
        private val PATRIN = 30929
        private val HOLLY = 30839
        private val CLAUDIA = 31001
        private val DESMOND = 30855

        // Monsters
        private val MONSTERS_DROPS = arrayOf(
            // npcId
            intArrayOf(20817, 20821, 20825, 20829, 21069, 21063),
            // parchment (red, blue, black, white)
            intArrayOf(5966, 5966, 5966, 5967, 5968, 5969),
            // rate
            intArrayOf(300000, 400000, 460000, 400000, 250000, 250000)
        )

        // Items
        private val SCROLLS = arrayOf(
            // Walderal => 13 blueprints => parts, recipes.
            intArrayOf(5989, 6001),
            // Holly -> 5x Imperial Genealogy -> Dark Crystal parts/Adena
            intArrayOf(5984, 5988),
            // Patrin -> 5x Ancient Epic -> Tallum parts/Adena
            intArrayOf(5979, 5983),
            // Claudia -> 7x Revelation of the Seals -> Nightmare parts/Adena
            intArrayOf(5972, 5978),
            // Desmond -> 7x Revelation of the Seals -> Majestic parts/Adena
            intArrayOf(5972, 5978)
        )

        // Rewards matrice.
        private val REWARDS_MATRICE = arrayOf(
            // Walderal DC choice
            arrayOf(
                intArrayOf(13, 5496),
                intArrayOf(26, 5508),
                intArrayOf(40, 5525),
                intArrayOf(58, 5368),
                intArrayOf(76, 5392),
                intArrayOf(100, 5426)
            ),
            // Walderal Tallum choice
            arrayOf(
                intArrayOf(13, 5497),
                intArrayOf(26, 5509),
                intArrayOf(40, 5526),
                intArrayOf(58, 5370),
                intArrayOf(76, 5394),
                intArrayOf(100, 5428)
            ),
            // Walderal NM choice
            arrayOf(
                intArrayOf(20, 5502),
                intArrayOf(40, 5514),
                intArrayOf(58, 5527),
                intArrayOf(73, 5380),
                intArrayOf(87, 5404),
                intArrayOf(100, 5430)
            ),
            // Walderal Maja choice
            arrayOf(
                intArrayOf(20, 5503),
                intArrayOf(40, 5515),
                intArrayOf(58, 5528),
                intArrayOf(73, 5382),
                intArrayOf(87, 5406),
                intArrayOf(100, 5432)
            ),
            // Holly DC parts + adenas.
            arrayOf(intArrayOf(33, 5496), intArrayOf(66, 5508), intArrayOf(89, 5525), intArrayOf(100, 57)),
            // Patrin Tallum parts + adenas.
            arrayOf(intArrayOf(33, 5497), intArrayOf(66, 5509), intArrayOf(89, 5526), intArrayOf(100, 57)),
            // Claudia NM parts + adenas.
            arrayOf(intArrayOf(35, 5502), intArrayOf(70, 5514), intArrayOf(87, 5527), intArrayOf(100, 57)),
            // Desmond Maja choice
            arrayOf(intArrayOf(35, 5503), intArrayOf(70, 5515), intArrayOf(87, 5528), intArrayOf(100, 57))
        )

        private fun checkAndRewardItems(st: QuestState, itemType: Int, rewardType: Int, npcId: Int): String {
            // Retrieve array with items to check.
            val itemsToCheck = SCROLLS[itemType]

            // Check set of items.
            for (item in itemsToCheck[0]..itemsToCheck[1])
                if (!st.hasQuestItems(item))
                    return npcId.toString() + if (npcId == WALDERAL) "-07a.htm" else "-01.htm"

            // Remove set of items.
            for (item in itemsToCheck[0]..itemsToCheck[1])
                st.takeItems(item, 1)

            // Retrieve array with rewards.
            val rewards = REWARDS_MATRICE[rewardType]
            val chance = Rnd[100]

            for (reward in rewards) {
                if (chance < reward[0]) {
                    st.rewardItems(reward[1], 1)
                    return npcId.toString() + "-02.htm"
                }
            }

            return npcId.toString() + if (npcId == WALDERAL) "-07a.htm" else "-01.htm"
        }
    }
}