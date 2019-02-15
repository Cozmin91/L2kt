package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q223_TestOfTheChampion : Quest(223, "Test of the Champion") {
    init {

        setItemsIds(
            MASON_LETTER,
            MEDUSA_VENOM,
            WINDSUS_BILE,
            WHITE_ROSE_INSIGNIA,
            HARPY_EGG,
            GROOT_LETTER,
            MOUEN_LETTER,
            ASCALON_LETTER_1,
            IRON_ROSE_RING,
            BLOODY_AXE_HEAD,
            ASCALON_LETTER_2,
            ASCALON_LETTER_3,
            MOUEN_ORDER_1,
            ROAD_RATMAN_HEAD,
            MOUEN_ORDER_2,
            LETO_LIZARDMAN_FANG
        )

        addStartNpc(ASCALON)
        addTalkId(ASCALON, GROOT, MOUEN, MASON)

        addAttackId(HARPY, ROAD_SCAVENGER)
        addKillId(
            HARPY,
            MEDUSA,
            HARPY_MATRIARCH,
            ROAD_COLLECTOR,
            ROAD_SCAVENGER,
            WINDSUS,
            LETO_LIZARDMAN,
            LETO_LIZARDMAN_ARCHER,
            LETO_LIZARDMAN_SOLDIER,
            LETO_LIZARDMAN_WARRIOR,
            LETO_LIZARDMAN_SHAMAN,
            LETO_LIZARDMAN_OVERLORD,
            BLOODY_AXE_ELITE
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event == "30624-06.htm") {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ASCALON_LETTER_1, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30624-06a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event == "30624-10.htm") {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MASON_LETTER, 1)
            st.giveItems(ASCALON_LETTER_2, 1)
        } else if (event == "30624-14.htm") {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GROOT_LETTER, 1)
            st.giveItems(ASCALON_LETTER_3, 1)
        } else if (event == "30625-03.htm") {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ASCALON_LETTER_1, 1)
            st.giveItems(IRON_ROSE_RING, 1)
        } else if (event == "30093-02.htm") {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ASCALON_LETTER_2, 1)
            st.giveItems(WHITE_ROSE_INSIGNIA, 1)
        } else if (event == "30196-03.htm") {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ASCALON_LETTER_3, 1)
            st.giveItems(MOUEN_ORDER_1, 1)
        } else if (event == "30196-06.htm") {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MOUEN_ORDER_1, 1)
            st.takeItems(ROAD_RATMAN_HEAD, 1)
            st.giveItems(MOUEN_ORDER_2, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val classId = player.classId
                if (classId != ClassId.WARRIOR && classId != ClassId.ORC_RAIDER)
                    htmltext = "30624-01.htm"
                else if (player.level < 39)
                    htmltext = "30624-02.htm"
                else
                    htmltext = if (classId == ClassId.WARRIOR) "30624-03.htm" else "30624-04.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ASCALON -> if (cond == 1)
                        htmltext = "30624-07.htm"
                    else if (cond < 4)
                        htmltext = "30624-08.htm"
                    else if (cond == 4)
                        htmltext = "30624-09.htm"
                    else if (cond == 5)
                        htmltext = "30624-11.htm"
                    else if (cond > 5 && cond < 8)
                        htmltext = "30624-12.htm"
                    else if (cond == 8)
                        htmltext = "30624-13.htm"
                    else if (cond == 9)
                        htmltext = "30624-15.htm"
                    else if (cond > 9 && cond < 14)
                        htmltext = "30624-16.htm"
                    else if (cond == 14) {
                        htmltext = "30624-17.htm"
                        st.takeItems(MOUEN_LETTER, 1)
                        st.giveItems(MARK_OF_CHAMPION, 1)
                        st.rewardExpAndSp(117454, 25000)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    MASON -> if (cond == 1)
                        htmltext = "30625-01.htm"
                    else if (cond == 2)
                        htmltext = "30625-04.htm"
                    else if (cond == 3) {
                        htmltext = "30625-05.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BLOODY_AXE_HEAD, -1)
                        st.takeItems(IRON_ROSE_RING, 1)
                        st.giveItems(MASON_LETTER, 1)
                    } else if (cond == 4)
                        htmltext = "30625-06.htm"
                    else if (cond > 4)
                        htmltext = "30625-07.htm"

                    GROOT -> if (cond == 5)
                        htmltext = "30093-01.htm"
                    else if (cond == 6)
                        htmltext = "30093-03.htm"
                    else if (cond == 7) {
                        htmltext = "30093-04.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(WHITE_ROSE_INSIGNIA, 1)
                        st.takeItems(HARPY_EGG, -1)
                        st.takeItems(MEDUSA_VENOM, -1)
                        st.takeItems(WINDSUS_BILE, -1)
                        st.giveItems(GROOT_LETTER, 1)
                    } else if (cond == 8)
                        htmltext = "30093-05.htm"
                    else if (cond > 8)
                        htmltext = "30093-06.htm"

                    MOUEN -> if (cond == 9)
                        htmltext = "30196-01.htm"
                    else if (cond == 10)
                        htmltext = "30196-04.htm"
                    else if (cond == 11)
                        htmltext = "30196-05.htm"
                    else if (cond == 12)
                        htmltext = "30196-07.htm"
                    else if (cond == 13) {
                        htmltext = "30196-08.htm"
                        st["cond"] = "14"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LETO_LIZARDMAN_FANG, -1)
                        st.takeItems(MOUEN_ORDER_2, 1)
                        st.giveItems(MOUEN_LETTER, 1)
                    } else if (cond > 13)
                        htmltext = "30196-09.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            HARPY // Possibility to spawn an HARPY _MATRIARCH.
            -> if (st.getInt("cond") == 6 && Rnd.nextBoolean() && !npc.isScriptValue(1)) {
                // Spawn one or two matriarchs.
                for (i in 1 until if (Rnd[10] < 7) 2 else 3) {
                    val collector = addSpawn(HARPY_MATRIARCH, npc, true, 0, false) as Attackable?

                    collector!!.setRunning()
                    collector.addDamageHate(attacker, 0, 999)
                    collector.ai.setIntention(CtrlIntention.ATTACK, attacker)
                }
                npc.scriptValue = 1
            }

            ROAD_SCAVENGER // Possibility to spawn a Road Collector.
            -> if (st.getInt("cond") == 10 && Rnd.nextBoolean() && !npc.isScriptValue(1)) {
                // Spawn one or two collectors.
                for (i in 1 until if (Rnd[10] < 7) 2 else 3) {
                    val collector = addSpawn(ROAD_COLLECTOR, npc, true, 0, false) as Attackable?

                    collector!!.setRunning()
                    collector.addDamageHate(attacker, 0, 999)
                    collector.ai.setIntention(CtrlIntention.ATTACK, attacker)
                }
                npc.scriptValue = 1
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        when (npcId) {
            BLOODY_AXE_ELITE -> if (st.getInt("cond") == 2 && st.dropItemsAlways(BLOODY_AXE_HEAD, 1, 100))
                st["cond"] = "3"

            HARPY, HARPY_MATRIARCH -> if (st.getInt("cond") == 6 && st.dropItems(HARPY_EGG, 1, 30, 500000))
                if (st.getQuestItemsCount(MEDUSA_VENOM) == 30 && st.getQuestItemsCount(WINDSUS_BILE) == 30)
                    st["cond"] = "7"

            MEDUSA -> if (st.getInt("cond") == 6 && st.dropItems(MEDUSA_VENOM, 1, 30, 500000))
                if (st.getQuestItemsCount(HARPY_EGG) == 30 && st.getQuestItemsCount(WINDSUS_BILE) == 30)
                    st["cond"] = "7"

            WINDSUS -> if (st.getInt("cond") == 6 && st.dropItems(WINDSUS_BILE, 1, 30, 500000))
                if (st.getQuestItemsCount(HARPY_EGG) == 30 && st.getQuestItemsCount(MEDUSA_VENOM) == 30)
                    st["cond"] = "7"

            ROAD_COLLECTOR, ROAD_SCAVENGER -> if (st.getInt("cond") == 10 && st.dropItemsAlways(
                    ROAD_RATMAN_HEAD,
                    1,
                    100
                )
            )
                st["cond"] = "11"

            LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD -> if (st.getInt(
                    "cond"
                ) == 12 && st.dropItems(LETO_LIZARDMAN_FANG, 1, 100, 500000 + 100000 * (npcId - 20577))
            )
                st["cond"] = "13"
        }

        return null
    }

    companion object {
        private val qn = "Q223_TestOfTheChampion"

        // Items
        private val ASCALON_LETTER_1 = 3277
        private val MASON_LETTER = 3278
        private val IRON_ROSE_RING = 3279
        private val ASCALON_LETTER_2 = 3280
        private val WHITE_ROSE_INSIGNIA = 3281
        private val GROOT_LETTER = 3282
        private val ASCALON_LETTER_3 = 3283
        private val MOUEN_ORDER_1 = 3284
        private val MOUEN_ORDER_2 = 3285
        private val MOUEN_LETTER = 3286
        private val HARPY_EGG = 3287
        private val MEDUSA_VENOM = 3288
        private val WINDSUS_BILE = 3289
        private val BLOODY_AXE_HEAD = 3290
        private val ROAD_RATMAN_HEAD = 3291
        private val LETO_LIZARDMAN_FANG = 3292

        // Rewards
        private val MARK_OF_CHAMPION = 3276
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val ASCALON = 30624
        private val GROOT = 30093
        private val MOUEN = 30196
        private val MASON = 30625

        // Monsters
        private val HARPY = 20145
        private val HARPY_MATRIARCH = 27088
        private val MEDUSA = 20158
        private val WINDSUS = 20553
        private val ROAD_COLLECTOR = 27089
        private val ROAD_SCAVENGER = 20551
        private val LETO_LIZARDMAN = 20577
        private val LETO_LIZARDMAN_ARCHER = 20578
        private val LETO_LIZARDMAN_SOLDIER = 20579
        private val LETO_LIZARDMAN_WARRIOR = 20580
        private val LETO_LIZARDMAN_SHAMAN = 20581
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val BLOODY_AXE_ELITE = 20780
    }
}