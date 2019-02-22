package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q648_AnIceMerchantsDream : Quest(648, "An Ice Merchant's Dream") {
    init {
        REWARDS["a"] = intArrayOf(
            SILVER_ICE_CRYSTAL, 23, 1894 // Crafted Leather
        )
        REWARDS["b"] = intArrayOf(
            SILVER_ICE_CRYSTAL, 6, 1881 // Coarse Bone Powder
        )
        REWARDS["c"] = intArrayOf(
            SILVER_ICE_CRYSTAL, 8, 1880 // Steel
        )
        REWARDS["d"] = intArrayOf(
            BLACK_ICE_CRYSTAL, 1800, 729 // Scroll: Enchant Weapon (A-Grade)
        )
        REWARDS["e"] = intArrayOf(
            BLACK_ICE_CRYSTAL, 240, 730 // Scroll: Enchant Armor (A-Grade)
        )
        REWARDS["f"] = intArrayOf(
            BLACK_ICE_CRYSTAL, 500, 947 // Scroll: Enchant Weapon (B-Grade)
        )
        REWARDS["g"] = intArrayOf(
            BLACK_ICE_CRYSTAL, 80, 948 // Scroll: Enchant Armor (B-Grade)
        )
    }

    init {
        CHANCES[22080] = intArrayOf(285000, 48000) // Massive Maze Bandersnatch
        CHANCES[22081] = intArrayOf(443000, 0) // Lost Watcher
        CHANCES[22082] = intArrayOf(510000, 0) // Elder Lost Watcher
        CHANCES[22084] = intArrayOf(477000, 49000) // Panthera
        CHANCES[22085] = intArrayOf(420000, 43000) // Lost Gargoyle
        CHANCES[22086] = intArrayOf(490000, 50000) // Lost Gargoyle Youngling
        CHANCES[22087] = intArrayOf(787000, 81000) // Pronghorn Spirit
        CHANCES[22088] = intArrayOf(480000, 49000) // Pronghorn
        CHANCES[22089] = intArrayOf(550000, 56000) // Ice Tarantula
        CHANCES[22090] = intArrayOf(570000, 58000) // Frost Tarantula
        CHANCES[22092] = intArrayOf(623000, 0) // Frost Iron Golem
        CHANCES[22093] = intArrayOf(910000, 93000) // Lost Buffalo
        CHANCES[22094] = intArrayOf(553000, 57000) // Frost Buffalo
        CHANCES[22096] = intArrayOf(593000, 61000) // Ursus
        CHANCES[22097] = intArrayOf(693000, 71000) // Lost Yeti
        CHANCES[22098] = intArrayOf(717000, 74000) // Frost Yeti
    }

    init {

        setItemsIds(SILVER_HEMOCYTE, SILVER_ICE_CRYSTAL, BLACK_ICE_CRYSTAL)

        addStartNpc(RAFFORTY, ICE_SHELF)
        addTalkId(RAFFORTY, ICE_SHELF)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // RAFFORTY
        if (event.equals("32020-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32020-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32020-14.htm", ignoreCase = true) || event.equals("32020-15.htm", ignoreCase = true)) {
            val black = st.getQuestItemsCount(BLACK_ICE_CRYSTAL)
            val silver = st.getQuestItemsCount(SILVER_ICE_CRYSTAL)
            if (silver + black > 0) {
                st.takeItems(BLACK_ICE_CRYSTAL, -1)
                st.takeItems(SILVER_ICE_CRYSTAL, -1)
                st.rewardItems(57, silver * 300 + black * 1200)
            } else
                htmltext = "32020-16a.htm"
        } else if (event.startsWith("32020-17")) {
            val reward = REWARDS[event.substring(8, 9)] ?: return null
            if (st.getQuestItemsCount(reward[0]) >= reward[1]) {
                st.takeItems(reward[0], reward[1])
                st.rewardItems(reward[2], 1)
            } else
                htmltext = "32020-15a.htm"
        } else if (event.equals("32020-20.htm", ignoreCase = true) || event.equals("32020-22.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("32023-05.htm", ignoreCase = true)) {
            if (st.getInt("exCond") == 0)
                st["exCond"] = ((Rnd[4] + 1) * 10).toString()
        } else if (event.startsWith("32023-06-")) {
            val exCond = st.getInt("exCond")
            if (exCond > 0) {
                htmltext = "32023-06.htm"
                st["exCond"] = (exCond + if (event.endsWith("chisel")) 1 else 2).toString()
                st.playSound("ItemSound2.broken_key")
                st.takeItems(SILVER_ICE_CRYSTAL, 1)
            }
        } else if (event.startsWith("32023-07-")) {
            val exCond = st.getInt("exCond")
            if (exCond > 0) {
                val `val` = exCond / 10
                if (`val` == exCond - `val` * 10 + if (event.endsWith("knife")) 0 else 2) {
                    htmltext = "32023-07.htm"
                    st.playSound("ItemSound3.sys_enchant_success")
                    st.rewardItems(BLACK_ICE_CRYSTAL, 1)
                } else {
                    htmltext = "32023-08.htm"
                    st.playSound("ItemSound3.sys_enchant_failed")
                }
                st["exCond"] = "0"
            }
        }// ICE SHELF

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (npc.npcId == RAFFORTY) {
                if (player.level < 53)
                    htmltext = "32020-01.htm"
                else {
                    val st2 = player.getQuestState(qn2)
                    htmltext = if (st2 != null && st2.isCompleted) "32020-02.htm" else "32020-03.htm"
                }
            } else
                htmltext = "32023-01.htm"

            Quest.STATE_STARTED -> if (npc.npcId == RAFFORTY) {
                val hasItem = st.hasAtLeastOneQuestItem(SILVER_ICE_CRYSTAL, BLACK_ICE_CRYSTAL)
                val st2 = player.getQuestState(qn2)
                if (st2 != null && st2.isCompleted) {
                    htmltext = if (hasItem) "32020-11.htm" else "32020-09.htm"
                    if (st.getInt("cond") == 1) {
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    }
                } else
                    htmltext = if (hasItem) "32020-10.htm" else "32020-08.htm"
            } else {
                if (!st.hasQuestItems(SILVER_ICE_CRYSTAL))
                    htmltext = "32023-02.htm"
                else {
                    if (st.getInt("exCond") % 10 == 0) {
                        htmltext = "32023-03.htm"
                        st["exCond"] = "0"
                    } else
                        htmltext = "32023-04.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val chance = CHANCES[npc.npcId] ?: intArrayOf(0, 0)

        st.dropItems(SILVER_ICE_CRYSTAL, 1, 0, chance[0])

        if (st.getInt("cond") == 2 && chance[1] > 0)
            st.dropItems(SILVER_HEMOCYTE, 1, 0, chance[1])

        return null
    }

    companion object {
        private val qn = "Q648_AnIceMerchantsDream"
        private val qn2 = "Q115_TheOtherSideOfTruth"

        // Items
        private val SILVER_HEMOCYTE = 8057
        private val SILVER_ICE_CRYSTAL = 8077
        private val BLACK_ICE_CRYSTAL = 8078

        // Rewards
        private val REWARDS = HashMap<String, IntArray>()

        // NPCs
        private val RAFFORTY = 32020
        private val ICE_SHELF = 32023

        // Drop chances
        private val CHANCES = HashMap<Int, IntArray>()
    }
}