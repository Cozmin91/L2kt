package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q222_TestOfTheDuelist : Quest(222, "Test of the Duelist") {
    init {

        setItemsIds(
            ORDER_GLUDIO,
            ORDER_DION,
            ORDER_GIRAN,
            ORDER_OREN,
            ORDER_ADEN,
            FINAL_ORDER,
            PUNCHER_SHARD,
            NOBLE_ANT_FEELER,
            DRONE_CHITIN,
            DEAD_SEEKER_FANG,
            OVERLORD_NECKLACE,
            FETTERED_SOUL_CHAIN,
            CHIEF_AMULET,
            ENCHANTED_EYE_MEAT,
            TAMRIN_ORC_RING,
            TAMRIN_ORC_ARROW,
            EXCURO_SKIN,
            KRATOR_SHARD,
            GRANDIS_SKIN,
            TIMAK_ORC_BELT,
            LAKIN_MACE
        )

        addStartNpc(KAIEN)
        addTalkId(KAIEN)

        addKillId(
            PUNCHER,
            NOBLE_ANT_LEADER,
            MARSH_STAKATO_DRONE,
            DEAD_SEEKER,
            BREKA_ORC_OVERLORD,
            FETTERED_SOUL,
            LETO_LIZARDMAN_OVERLORD,
            ENCHANTED_MONSTEREYE,
            TAMLIN_ORC,
            TAMLIN_ORC_ARCHER,
            EXCURO,
            KRATOR,
            GRANDIS,
            TIMAK_ORC_OVERLORD,
            LAKIN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30623-04.htm", ignoreCase = true)) {
            if (player.race == ClassRace.ORC)
                htmltext = "30623-05.htm"
        } else if (event.equals("30623-07.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ORDER_GLUDIO, 1)
            st.giveItems(ORDER_DION, 1)
            st.giveItems(ORDER_GIRAN, 1)
            st.giveItems(ORDER_OREN, 1)
            st.giveItems(ORDER_ADEN, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30623-07a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30623-16.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 3) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)

                st.takeItems(ORDER_GLUDIO, 1)
                st.takeItems(ORDER_DION, 1)
                st.takeItems(ORDER_GIRAN, 1)
                st.takeItems(ORDER_OREN, 1)
                st.takeItems(ORDER_ADEN, 1)

                st.takeItems(PUNCHER_SHARD, -1)
                st.takeItems(NOBLE_ANT_FEELER, -1)
                st.takeItems(DRONE_CHITIN, -1)
                st.takeItems(DEAD_SEEKER_FANG, -1)
                st.takeItems(OVERLORD_NECKLACE, -1)
                st.takeItems(FETTERED_SOUL_CHAIN, -1)
                st.takeItems(CHIEF_AMULET, -1)
                st.takeItems(ENCHANTED_EYE_MEAT, -1)
                st.takeItems(TAMRIN_ORC_RING, -1)
                st.takeItems(TAMRIN_ORC_ARROW, -1)

                st.giveItems(FINAL_ORDER, 1)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val classId = player.classId.id
                if (classId != 0x01 && classId != 0x2f && classId != 0x13 && classId != 0x20)
                    htmltext = "30623-02.htm"
                else if (player.level < 39)
                    htmltext = "30623-01.htm"
                else
                    htmltext = "30623-03.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 2)
                    htmltext = "30623-07a.htm"
                else if (cond == 3)
                    htmltext = "30623-13.htm"
                else if (cond == 4)
                    htmltext = "30623-17.htm"
                else if (cond == 5) {
                    htmltext = "30623-18.htm"
                    st.takeItems(FINAL_ORDER, 1)
                    st.takeItems(EXCURO_SKIN, -1)
                    st.takeItems(KRATOR_SHARD, -1)
                    st.takeItems(GRANDIS_SKIN, -1)
                    st.takeItems(TIMAK_ORC_BELT, -1)
                    st.takeItems(LAKIN_MACE, -1)
                    st.giveItems(MARK_OF_DUELIST, 1)
                    st.rewardExpAndSp(47015, 20000)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (st.getInt("cond") == 2) {
            when (npc.npcId) {
                PUNCHER -> if (st.dropItemsAlways(PUNCHER_SHARD, 1, 10))
                    if (st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(
                            DEAD_SEEKER_FANG
                        ) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(
                            FETTERED_SOUL_CHAIN
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                NOBLE_ANT_LEADER -> if (st.dropItemsAlways(NOBLE_ANT_FEELER, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(DRONE_CHITIN) >= 10 && st.getQuestItemsCount(
                            DEAD_SEEKER_FANG
                        ) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(
                            FETTERED_SOUL_CHAIN
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                MARSH_STAKATO_DRONE -> if (st.dropItemsAlways(DRONE_CHITIN, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DEAD_SEEKER_FANG
                        ) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(
                            FETTERED_SOUL_CHAIN
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                DEAD_SEEKER -> if (st.dropItemsAlways(DEAD_SEEKER_FANG, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(OVERLORD_NECKLACE) >= 10 && st.getQuestItemsCount(
                            FETTERED_SOUL_CHAIN
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                BREKA_ORC_OVERLORD -> if (st.dropItemsAlways(OVERLORD_NECKLACE, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            FETTERED_SOUL_CHAIN
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                FETTERED_SOUL -> if (st.dropItemsAlways(FETTERED_SOUL_CHAIN, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            OVERLORD_NECKLACE
                        ) >= 10 && st.getQuestItemsCount(CHIEF_AMULET) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_ARROW) >= 10
                    )
                        st["cond"] = "3"

                LETO_LIZARDMAN_OVERLORD -> if (st.dropItemsAlways(CHIEF_AMULET, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            OVERLORD_NECKLACE
                        ) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(
                            ENCHANTED_EYE_MEAT
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_ARROW
                        ) >= 10
                    )
                        st["cond"] = "3"

                ENCHANTED_MONSTEREYE -> if (st.dropItemsAlways(ENCHANTED_EYE_MEAT, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            OVERLORD_NECKLACE
                        ) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(
                            CHIEF_AMULET
                        ) >= 10 && st.getQuestItemsCount(TAMRIN_ORC_RING) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_ARROW
                        ) >= 10
                    )
                        st["cond"] = "3"

                TAMLIN_ORC -> if (st.dropItemsAlways(TAMRIN_ORC_RING, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            OVERLORD_NECKLACE
                        ) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(
                            CHIEF_AMULET
                        ) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_ARROW
                        ) >= 10
                    )
                        st["cond"] = "3"

                TAMLIN_ORC_ARCHER -> if (st.dropItemsAlways(TAMRIN_ORC_ARROW, 1, 10))
                    if (st.getQuestItemsCount(PUNCHER_SHARD) >= 10 && st.getQuestItemsCount(NOBLE_ANT_FEELER) >= 10 && st.getQuestItemsCount(
                            DRONE_CHITIN
                        ) >= 10 && st.getQuestItemsCount(DEAD_SEEKER_FANG) >= 10 && st.getQuestItemsCount(
                            OVERLORD_NECKLACE
                        ) >= 10 && st.getQuestItemsCount(FETTERED_SOUL_CHAIN) >= 10 && st.getQuestItemsCount(
                            CHIEF_AMULET
                        ) >= 10 && st.getQuestItemsCount(ENCHANTED_EYE_MEAT) >= 10 && st.getQuestItemsCount(
                            TAMRIN_ORC_RING
                        ) >= 10
                    )
                        st["cond"] = "3"
            }
        } else if (st.getInt("cond") == 4) {
            when (npc.npcId) {
                EXCURO -> if (st.dropItemsAlways(EXCURO_SKIN, 1, 3))
                    if (st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(
                            GRANDIS_SKIN
                        ) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3
                    )
                        st["cond"] = "5"

                KRATOR -> if (st.dropItemsAlways(KRATOR_SHARD, 1, 3))
                    if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(LAKIN_MACE) >= 3 && st.getQuestItemsCount(
                            GRANDIS_SKIN
                        ) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3
                    )
                        st["cond"] = "5"

                LAKIN -> if (st.dropItemsAlways(LAKIN_MACE, 1, 3))
                    if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(
                            GRANDIS_SKIN
                        ) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3
                    )
                        st["cond"] = "5"

                GRANDIS -> if (st.dropItemsAlways(GRANDIS_SKIN, 1, 3))
                    if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(
                            LAKIN_MACE
                        ) >= 3 && st.getQuestItemsCount(TIMAK_ORC_BELT) >= 3
                    )
                        st["cond"] = "5"

                TIMAK_ORC_OVERLORD -> if (st.dropItemsAlways(TIMAK_ORC_BELT, 1, 3))
                    if (st.getQuestItemsCount(EXCURO_SKIN) >= 3 && st.getQuestItemsCount(KRATOR_SHARD) >= 3 && st.getQuestItemsCount(
                            LAKIN_MACE
                        ) >= 3 && st.getQuestItemsCount(GRANDIS_SKIN) >= 3
                    )
                        st["cond"] = "5"
            }
        }

        return null
    }

    companion object {
        private val qn = "Q222_TestOfTheDuelist"

        private val KAIEN = 30623

        // Items
        private val ORDER_GLUDIO = 2763
        private val ORDER_DION = 2764
        private val ORDER_GIRAN = 2765
        private val ORDER_OREN = 2766
        private val ORDER_ADEN = 2767
        private val PUNCHER_SHARD = 2768
        private val NOBLE_ANT_FEELER = 2769
        private val DRONE_CHITIN = 2770
        private val DEAD_SEEKER_FANG = 2771
        private val OVERLORD_NECKLACE = 2772
        private val FETTERED_SOUL_CHAIN = 2773
        private val CHIEF_AMULET = 2774
        private val ENCHANTED_EYE_MEAT = 2775
        private val TAMRIN_ORC_RING = 2776
        private val TAMRIN_ORC_ARROW = 2777
        private val FINAL_ORDER = 2778
        private val EXCURO_SKIN = 2779
        private val KRATOR_SHARD = 2780
        private val GRANDIS_SKIN = 2781
        private val TIMAK_ORC_BELT = 2782
        private val LAKIN_MACE = 2783

        // Rewards
        private val MARK_OF_DUELIST = 2762
        private val DIMENSIONAL_DIAMOND = 7562

        // Monsters
        private val PUNCHER = 20085
        private val NOBLE_ANT_LEADER = 20090
        private val MARSH_STAKATO_DRONE = 20234
        private val DEAD_SEEKER = 20202
        private val BREKA_ORC_OVERLORD = 20270
        private val FETTERED_SOUL = 20552
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val ENCHANTED_MONSTEREYE = 20564
        private val TAMLIN_ORC = 20601
        private val TAMLIN_ORC_ARCHER = 20602
        private val EXCURO = 20214
        private val KRATOR = 20217
        private val GRANDIS = 20554
        private val TIMAK_ORC_OVERLORD = 20588
        private val LAKIN = 20604
    }
}