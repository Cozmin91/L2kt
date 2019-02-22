package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q327_RecoverTheFarmland : Quest(327, "Recover the Farmland") {
    init {
        EXP_REWARD[ANCIENT_CLAY_URN] = 2766
        EXP_REWARD[ANCIENT_BRASS_TIARA] = 3227
        EXP_REWARD[ANCIENT_BRONZE_MIRROR] = 3227
        EXP_REWARD[ANCIENT_JADE_NECKLACE] = 3919
    }

    init {

        setItemsIds(LEIKAN_LETTER)

        addStartNpc(LEIKAN, PIOTUR)
        addTalkId(LEIKAN, PIOTUR, IRIS, ASHA, NESTLE)

        addKillId(
            TUREK_ORC_WARLORD,
            TUREK_ORC_ARCHER,
            TUREK_ORC_SKIRMISHER,
            TUREK_ORC_SUPPLIER,
            TUREK_ORC_FOOTMAN,
            TUREK_ORC_SENTINEL,
            TUREK_ORC_SHAMAN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Piotur
        if (event.equals("30597-03.htm", ignoreCase = true) && st.getInt("cond") < 1) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30597-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30382-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LEIKAN_LETTER, 1)
        } else if (event.equals("30313-02.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(CLAY_URN_FRAGMENT) >= 5) {
                st.takeItems(CLAY_URN_FRAGMENT, 5)
                if (Rnd[6] < 5) {
                    htmltext = "30313-03.htm"
                    st.rewardItems(ANCIENT_CLAY_URN, 1)
                } else
                    htmltext = "30313-10.htm"
            }
        } else if (event.equals("30313-04.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRASS_TRINKET_PIECE) >= 5) {
                st.takeItems(BRASS_TRINKET_PIECE, 5)
                if (Rnd[7] < 6) {
                    htmltext = "30313-05.htm"
                    st.rewardItems(ANCIENT_BRASS_TIARA, 1)
                } else
                    htmltext = "30313-10.htm"
            }
        } else if (event.equals("30313-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(BRONZE_MIRROR_PIECE) >= 5) {
                st.takeItems(BRONZE_MIRROR_PIECE, 5)
                if (Rnd[7] < 6) {
                    htmltext = "30313-07.htm"
                    st.rewardItems(ANCIENT_BRONZE_MIRROR, 1)
                } else
                    htmltext = "30313-10.htm"
            }
        } else if (event.equals("30313-08.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(JADE_NECKLACE_BEAD) >= 5) {
                st.takeItems(JADE_NECKLACE_BEAD, 5)
                if (Rnd[8] < 7) {
                    htmltext = "30313-09.htm"
                    st.rewardItems(ANCIENT_JADE_NECKLACE, 1)
                } else
                    htmltext = "30313-10.htm"
            }
        } else if (event.equals("30034-03.htm", ignoreCase = true)) {
            val n = st.getQuestItemsCount(CLAY_URN_FRAGMENT)
            if (n == 0)
                htmltext = "30034-02.htm"
            else {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(CLAY_URN_FRAGMENT, n)
                st.rewardExpAndSp((n * 307).toLong(), 0)
            }
        } else if (event.equals("30034-04.htm", ignoreCase = true)) {
            val n = st.getQuestItemsCount(BRASS_TRINKET_PIECE)
            if (n == 0)
                htmltext = "30034-02.htm"
            else {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(BRASS_TRINKET_PIECE, n)
                st.rewardExpAndSp((n * 368).toLong(), 0)
            }
        } else if (event.equals("30034-05.htm", ignoreCase = true)) {
            val n = st.getQuestItemsCount(BRONZE_MIRROR_PIECE)
            if (n == 0)
                htmltext = "30034-02.htm"
            else {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(BRONZE_MIRROR_PIECE, n)
                st.rewardExpAndSp((n * 368).toLong(), 0)
            }
        } else if (event.equals("30034-06.htm", ignoreCase = true)) {
            val n = st.getQuestItemsCount(JADE_NECKLACE_BEAD)
            if (n == 0)
                htmltext = "30034-02.htm"
            else {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(JADE_NECKLACE_BEAD, n)
                st.rewardExpAndSp((n * 430).toLong(), 0)
            }
        } else if (event.equals("30034-07.htm", ignoreCase = true)) {
            var isRewarded = false

            for (i in 1852..1855) {
                val n = st.getQuestItemsCount(i)
                if (n > 0) {
                    st.takeItems(i, n)
                    st.rewardExpAndSp((n * (EXP_REWARD[i] ?: 0)).toLong(), 0)
                    isRewarded = true
                }
            }
            if (!isRewarded)
                htmltext = "30034-02.htm"
            else
                st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30314-03.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ANCIENT_CLAY_URN))
                htmltext = "30314-07.htm"
            else {
                st.takeItems(ANCIENT_CLAY_URN, 1)
                st.rewardItems(SOULSHOT_D, 70 + Rnd[41])
            }
        } else if (event.equals("30314-04.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ANCIENT_BRASS_TIARA))
                htmltext = "30314-07.htm"
            else {
                st.takeItems(ANCIENT_BRASS_TIARA, 1)
                val rnd = Rnd[100]
                if (rnd < 40)
                    st.rewardItems(HEALING_POTION, 1)
                else if (rnd < 84)
                    st.rewardItems(HASTE_POTION, 1)
                else
                    st.rewardItems(POTION_OF_ALACRITY, 1)
            }
        } else if (event.equals("30314-05.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ANCIENT_BRONZE_MIRROR))
                htmltext = "30314-07.htm"
            else {
                st.takeItems(ANCIENT_BRONZE_MIRROR, 1)
                st.rewardItems(if (Rnd[100] < 59) SCROLL_OF_ESCAPE else SCROLL_OF_RESURRECTION, 1)
            }
        } else if (event.equals("30314-06.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(ANCIENT_JADE_NECKLACE))
                htmltext = "30314-07.htm"
            else {
                st.takeItems(ANCIENT_JADE_NECKLACE, 1)
                st.rewardItems(SPIRITSHOT_D, 50 + Rnd[41])
            }
        }// Nestle
        // Iris
        // Asha
        // Leikan

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = npc.npcId.toString() + (if (player.level < 25) "-01.htm" else "-02.htm")

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    PIOTUR -> if (!st.hasQuestItems(LEIKAN_LETTER)) {
                        if (st.hasAtLeastOneQuestItem(TUREK_DOGTAG, TUREK_MEDALLION)) {
                            htmltext = "30597-05.htm"

                            if (cond < 4) {
                                st["cond"] = "4"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            }

                            val dogtag = st.getQuestItemsCount(TUREK_DOGTAG)
                            val medallion = st.getQuestItemsCount(TUREK_MEDALLION)

                            st.takeItems(TUREK_DOGTAG, -1)
                            st.takeItems(TUREK_MEDALLION, -1)
                            st.rewardItems(
                                ADENA,
                                dogtag * 40 + medallion * 50 + if (dogtag + medallion >= 10) 619 else 0
                            )
                        } else
                            htmltext = "30597-04.htm"
                    } else {
                        htmltext = "30597-03a.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LEIKAN_LETTER, 1)
                    }

                    LEIKAN -> if (cond == 2)
                        htmltext = "30382-04.htm"
                    else if (cond == 3 || cond == 4) {
                        htmltext = "30382-05.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 5)
                        htmltext = "30382-05.htm"

                    else -> htmltext = npc.npcId.toString() + "-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        for (npcData in DROPLIST) {
            if (npcData[0] == npc.npcId) {
                st.dropItemsAlways(npcData[2], 1, -1)
                st.dropItems(Rnd[1848, 1851], 1, 0, npcData[1])
                break
            }
        }

        return null
    }

    companion object {
        private val qn = "Q327_RecoverTheFarmland"

        // Items
        private val LEIKAN_LETTER = 5012
        private val TUREK_DOGTAG = 1846
        private val TUREK_MEDALLION = 1847
        private val CLAY_URN_FRAGMENT = 1848
        private val BRASS_TRINKET_PIECE = 1849
        private val BRONZE_MIRROR_PIECE = 1850
        private val JADE_NECKLACE_BEAD = 1851
        private val ANCIENT_CLAY_URN = 1852
        private val ANCIENT_BRASS_TIARA = 1853
        private val ANCIENT_BRONZE_MIRROR = 1854
        private val ANCIENT_JADE_NECKLACE = 1855

        // Rewards
        private val ADENA = 57
        private val SOULSHOT_D = 1463
        private val SPIRITSHOT_D = 2510
        private val HEALING_POTION = 1061
        private val HASTE_POTION = 734
        private val POTION_OF_ALACRITY = 735
        private val SCROLL_OF_ESCAPE = 736
        private val SCROLL_OF_RESURRECTION = 737

        // NPCs
        private val LEIKAN = 30382
        private val PIOTUR = 30597
        private val IRIS = 30034
        private val ASHA = 30313
        private val NESTLE = 30314

        // Monsters
        private val TUREK_ORC_WARLORD = 20495
        private val TUREK_ORC_ARCHER = 20496
        private val TUREK_ORC_SKIRMISHER = 20497
        private val TUREK_ORC_SUPPLIER = 20498
        private val TUREK_ORC_FOOTMAN = 20499
        private val TUREK_ORC_SENTINEL = 20500
        private val TUREK_ORC_SHAMAN = 20501

        // Chances
        private val DROPLIST = arrayOf(
            intArrayOf(TUREK_ORC_ARCHER, 140000, TUREK_DOGTAG),
            intArrayOf(TUREK_ORC_SKIRMISHER, 70000, TUREK_DOGTAG),
            intArrayOf(TUREK_ORC_SUPPLIER, 120000, TUREK_DOGTAG),
            intArrayOf(TUREK_ORC_FOOTMAN, 100000, TUREK_DOGTAG),
            intArrayOf(TUREK_ORC_SENTINEL, 80000, TUREK_DOGTAG),
            intArrayOf(TUREK_ORC_SHAMAN, 90000, TUREK_MEDALLION),
            intArrayOf(TUREK_ORC_WARLORD, 180000, TUREK_MEDALLION)
        )

        // Exp
        private val EXP_REWARD = HashMap<Int, Int>()
    }
}