package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q404_PathToAHumanWizard : Quest(404, "Path to a Human Wizard") {
    init {

        setItemsIds(
            MAP_OF_LUSTER,
            KEY_OF_FLAME,
            FLAME_EARING,
            BROKEN_BRONZE_MIRROR,
            WIND_FEATHER,
            WIND_BANGEL,
            RAMA_DIARY,
            SPARKLE_PEBBLE,
            WATER_NECKLACE,
            RUST_GOLD_COIN,
            RED_SOIL,
            EARTH_RING
        )

        addStartNpc(PARINA)
        addTalkId(PARINA, EARTH_SNAKE, WASTELAND_LIZARDMAN, FLAME_SALAMANDER, WIND_SYLPH, WATER_UNDINE)

        addKillId(20021, 20359, 27030)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30391-08.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30410-03.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BROKEN_BRONZE_MIRROR, 1)
            st.giveItems(WIND_FEATHER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        val cond = st.getInt("cond")
        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.HUMAN_MYSTIC)
                htmltext = if (player.classId == ClassId.HUMAN_WIZARD) "30391-02a.htm" else "30391-01.htm"
            else if (player.level < 19)
                htmltext = "30391-02.htm"
            else if (st.hasQuestItems(BEAD_OF_SEASON))
                htmltext = "30391-03.htm"
            else
                htmltext = "30391-04.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                PARINA -> if (cond < 13)
                    htmltext = "30391-05.htm"
                else if (cond == 13) {
                    htmltext = "30391-06.htm"
                    st.takeItems(EARTH_RING, 1)
                    st.takeItems(FLAME_EARING, 1)
                    st.takeItems(WATER_NECKLACE, 1)
                    st.takeItems(WIND_BANGEL, 1)
                    st.giveItems(BEAD_OF_SEASON, 1)
                    st.rewardExpAndSp(3200, 2020)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(true)
                }

                FLAME_SALAMANDER -> if (cond == 1) {
                    htmltext = "30411-01.htm"
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(MAP_OF_LUSTER, 1)
                } else if (cond == 2)
                    htmltext = "30411-02.htm"
                else if (cond == 3) {
                    htmltext = "30411-03.htm"
                    st["cond"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(KEY_OF_FLAME, 1)
                    st.takeItems(MAP_OF_LUSTER, 1)
                    st.giveItems(FLAME_EARING, 1)
                } else if (cond > 3)
                    htmltext = "30411-04.htm"

                WIND_SYLPH -> if (cond == 4) {
                    htmltext = "30412-01.htm"
                    st["cond"] = "5"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(BROKEN_BRONZE_MIRROR, 1)
                } else if (cond == 5)
                    htmltext = "30412-02.htm"
                else if (cond == 6) {
                    htmltext = "30412-03.htm"
                    st["cond"] = "7"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(WIND_FEATHER, 1)
                    st.giveItems(WIND_BANGEL, 1)
                } else if (cond > 6)
                    htmltext = "30412-04.htm"

                WASTELAND_LIZARDMAN -> if (cond == 5)
                    htmltext = "30410-01.htm"
                else if (cond > 5)
                    htmltext = "30410-04.htm"

                WATER_UNDINE -> if (cond == 7) {
                    htmltext = "30413-01.htm"
                    st["cond"] = "8"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RAMA_DIARY, 1)
                } else if (cond == 8)
                    htmltext = "30413-02.htm"
                else if (cond == 9) {
                    htmltext = "30413-03.htm"
                    st["cond"] = "10"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(RAMA_DIARY, 1)
                    st.takeItems(SPARKLE_PEBBLE, -1)
                    st.giveItems(WATER_NECKLACE, 1)
                } else if (cond > 9)
                    htmltext = "30413-04.htm"

                EARTH_SNAKE -> if (cond == 10) {
                    htmltext = "30409-01.htm"
                    st["cond"] = "11"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RUST_GOLD_COIN, 1)
                } else if (cond == 11)
                    htmltext = "30409-02.htm"
                else if (cond == 12) {
                    htmltext = "30409-03.htm"
                    st["cond"] = "13"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(RED_SOIL, 1)
                    st.takeItems(RUST_GOLD_COIN, 1)
                    st.giveItems(EARTH_RING, 1)
                } else if (cond > 12)
                    htmltext = "30409-04.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20359 // Ratman Warrior
            -> if (st.getInt("cond") == 2 && st.dropItems(KEY_OF_FLAME, 1, 1, 800000))
                st["cond"] = "3"

            27030 // Water Seer
            -> if (st.getInt("cond") == 8 && st.dropItems(SPARKLE_PEBBLE, 1, 2, 800000))
                st["cond"] = "9"

            20021 // Red Bear
            -> if (st.getInt("cond") == 11 && st.dropItems(RED_SOIL, 1, 1, 200000))
                st["cond"] = "12"
        }

        return null
    }

    companion object {
        private val qn = "Q404_PathToAHumanWizard"

        // Items
        private val MAP_OF_LUSTER = 1280
        private val KEY_OF_FLAME = 1281
        private val FLAME_EARING = 1282
        private val BROKEN_BRONZE_MIRROR = 1283
        private val WIND_FEATHER = 1284
        private val WIND_BANGEL = 1285
        private val RAMA_DIARY = 1286
        private val SPARKLE_PEBBLE = 1287
        private val WATER_NECKLACE = 1288
        private val RUST_GOLD_COIN = 1289
        private val RED_SOIL = 1290
        private val EARTH_RING = 1291
        private val BEAD_OF_SEASON = 1292

        // NPCs
        private val PARINA = 30391
        private val EARTH_SNAKE = 30409
        private val WASTELAND_LIZARDMAN = 30410
        private val FLAME_SALAMANDER = 30411
        private val WIND_SYLPH = 30412
        private val WATER_UNDINE = 30413
    }
}