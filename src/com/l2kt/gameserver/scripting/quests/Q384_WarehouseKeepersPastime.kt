package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q384_WarehouseKeepersPastime : Quest(384, "Warehouse Keeper's Pastime") {
    init {
        CHANCES[20947] = 160000 // Connabi
        CHANCES[20948] = 180000 // Bartal
        CHANCES[20945] = 120000 // Cadeine
        CHANCES[20946] = 150000 // Sanhidro
        CHANCES[20635] = 150000 // Carinkain
        CHANCES[20773] = 610000 // Conjurer Bat Lord
        CHANCES[20774] = 600000 // Conjurer Bat
        CHANCES[20760] = 240000 // Dragon Bearer Archer
        CHANCES[20758] = 240000 // Dragon Bearer Chief
        CHANCES[20759] = 230000 // Dragon Bearer Warrior
        CHANCES[20242] = 220000 // Dustwind Gargoyle
        CHANCES[20281] = 220000 // Dustwind Gargoyle (2)
        CHANCES[20556] = 140000 // Giant Monstereye
        CHANCES[20668] = 210000 // Grave Guard
        CHANCES[20241] = 220000 // Hunter Gargoyle
        CHANCES[20286] = 220000 // Hunter Gargoyle (2)
        CHANCES[20949] = 190000 // Luminun
        CHANCES[20950] = 200000 // Innersen
        CHANCES[20942] = 90000 // Nightmare Guide
        CHANCES[20943] = 120000 // Nightmare Keeper
        CHANCES[20944] = 110000 // Nightmare Lord
        CHANCES[20559] = 140000 // Rotting Golem
        CHANCES[20243] = 210000 // Thunder Wyrm
        CHANCES[20282] = 210000 // Thunder Wyrm (2)
        CHANCES[20677] = 340000 // Tulben
        CHANCES[20605] = 150000 // Weird Drake
    }

    init {

        setItemsIds(MEDAL)

        addStartNpc(CLIFF)
        addTalkId(CLIFF, BAXT)

        for (npcId in CHANCES.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        val npcId = npc!!.npcId
        if (event.equals("30182-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals(npcId.toString() + "-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals(npcId.toString() + "-11.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(MEDAL) < 10)
                htmltext = npcId.toString() + "-12.htm"
            else {
                st["bet"] = "10"
                st["board"] = StringUtil.scrambleString("123456789")
                st.takeItems(MEDAL, 10)
            }
        } else if (event.equals(npcId.toString() + "-13.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(MEDAL) < 100)
                htmltext = npcId.toString() + "-12.htm"
            else {
                st["bet"] = "100"
                st["board"] = StringUtil.scrambleString("123456789")
                st.takeItems(MEDAL, 100)
            }
        } else if (event.startsWith("select_1-"))
        // first pick
        {
            // Register the first char.
            st["playerArray"] = event.substring(9)

            // Send back the finalized HTM with dynamic content.
            htmltext = fillBoard(st, getHtmlText(npcId.toString() + "-14.htm"))
        } else if (event.startsWith("select_2-"))
        // pick #2-5
        {
            // Stores the current event for future use.
            val number = event.substring(9)

            // Restore the player array.
            val playerArray = st["playerArray"]

            // Verify if the given number is already on the player array, if yes, it's invalid, otherwise register it.
            if (ArraysUtil.contains(
                    playerArray!!.split("").dropLastWhile { it.isEmpty() }.toTypedArray(),
                    number
                )
            )
                htmltext = fillBoard(
                    st,
                    getHtmlText(npcId.toString() + "-" + (14 + 2 * playerArray.length).toString() + ".htm")
                )
            else {
                // Stores the final String.
                st["playerArray"] = playerArray + number

                htmltext = fillBoard(
                    st,
                    getHtmlText(npcId.toString() + "-" + (11 + 2 * (playerArray.length + 1)).toString() + ".htm")
                )
            }
        } else if (event.startsWith("select_3-"))
        // pick #6
        {
            // Stores the current event for future use.
            val number = event.substring(9)

            // Restore the player array.
            val playerArray = st["playerArray"]

            // Verify if the given number is already on the player array, if yes, it's invalid, otherwise calculate reward.
            if (ArraysUtil.contains(
                    playerArray!!.split("").dropLastWhile { it.isEmpty() }.toTypedArray(),
                    number
                )
            )
                htmltext = fillBoard(st, getHtmlText(npcId.toString() + "-26.htm"))
            else {
                // No need to store the String on player db, but still need to update it.
                val playerChoice =
                    (playerArray + number).split("").dropLastWhile { it.isEmpty() }.toTypedArray()

                // Transform the generated board (9 string length) into a 2d matrice (3x3 int).
                val board = st["board"]!!.split("").dropLastWhile { it.isEmpty() }.toTypedArray()

                // test for all line combination
                var winningLines = 0

                for (map in INDEX_MAP) {
                    // test line combination
                    var won = true
                    for (index in map)
                        won = won and ArraysUtil.contains(playerChoice, board[index])

                    // cut the loop, when you won
                    if (won)
                        winningLines++
                }

                if (winningLines == 3) {
                    htmltext = getHtmlText(npcId.toString() + "-23.htm")

                    val chance = Rnd[100]
                    for (reward in if (st["bet"] === "10") _rewards_10_win else _rewards_100_win) {
                        if (chance < reward[0]) {
                            st.giveItems(reward[1], 1)
                            if (reward[1] == 2437)
                                st.giveItems(2463, 1)

                            break
                        }
                    }
                } else if (winningLines == 0) {
                    htmltext = getHtmlText(npcId.toString() + "-25.htm")

                    val chance = Rnd[100]
                    for (reward in if (st["bet"] === "10") _rewards_10_lose else _rewards_100_lose) {
                        if (chance < reward[0]) {
                            st.giveItems(reward[1], 1)
                            break
                        }
                    }
                } else
                    htmltext = getHtmlText(npcId.toString() + "-24.htm")

                for (i in 1..9) {
                    htmltext = htmltext.replace("<?Cell$i?>", board[i])
                    htmltext = htmltext.replace(
                        "<?FontColor$i?>",
                        if (ArraysUtil.contains(playerChoice, board[i])) "ff0000" else "ffffff"
                    )
                }
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 40) "30182-04.htm" else "30182-01.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                CLIFF -> htmltext = if (st.getQuestItemsCount(MEDAL) < 10) "30182-06.htm" else "30182-07.htm"

                BAXT -> htmltext = if (st.getQuestItemsCount(MEDAL) < 10) "30685-01.htm" else "30685-02.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(MEDAL, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q384_WarehouseKeepersPastime"

        // NPCs
        private val CLIFF = 30182
        private val BAXT = 30685

        // Items
        private val MEDAL = 5964

        private val CHANCES = HashMap<Int, Int>()

        private val INDEX_MAP = arrayOf(
            intArrayOf(1, 2, 3), // line 1
            intArrayOf(4, 5, 6), // line 2
            intArrayOf(7, 8, 9), // line 3
            intArrayOf(1, 4, 7), // column 1
            intArrayOf(2, 5, 8), // column 2
            intArrayOf(3, 6, 9), // column 3
            intArrayOf(1, 5, 9), // diagonal 1
            intArrayOf(3, 5, 7)
        )// diagonal 2

        private val _rewards_10_win = arrayOf(
            intArrayOf(16, 1888), // Synthetic Cokes
            intArrayOf(32, 1887), // Varnish of Purity
            intArrayOf(50, 1894), // Crafted Leather
            intArrayOf(80, 952), // Scroll: Enchant Armor (C)
            intArrayOf(89, 1890), // Mithril Alloy
            intArrayOf(98, 1893), // Oriharukon
            intArrayOf(100, 951)
        )// Scroll: Enchant Weapon (C)

        private val _rewards_10_lose = arrayOf(
            intArrayOf(50, 4041), // Mold Hardener
            intArrayOf(80, 952), // Scroll: Enchant Armor (C)
            intArrayOf(98, 1892), // Blacksmith's Frame
            intArrayOf(100, 917)
        )// Necklace of Mermaid

        private val _rewards_100_win = arrayOf(
            intArrayOf(50, 883), // Aquastone Ring
            intArrayOf(80, 951), // Scroll: Enchant Weapon (C)
            intArrayOf(98, 852), // Moonstone Earring
            intArrayOf(100, 401)
        )// Drake Leather Armor

        private val _rewards_100_lose = arrayOf(
            intArrayOf(50, 951), // Scroll: Enchant Weapon (C)
            intArrayOf(80, 500), // Great Helmet
            intArrayOf(98, 2437), // Drake Leather Boots
            intArrayOf(100, 135)
        )// Samurai Longsword

        private fun fillBoard(st: QuestState, htmltext: String): String {
            var htmltext = htmltext
            val playerArray = st["playerArray"]!!.split("").dropLastWhile { it.isEmpty() }.toTypedArray()
            val board = st["board"]!!.split("").dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in 1..9)
                htmltext = htmltext.replace(
                    "<?Cell$i?>",
                    if (ArraysUtil.contains(playerArray, board[i])) board[i] else "?"
                )

            return htmltext
        }
    }
}