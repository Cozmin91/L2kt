package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q417_PathToBecomeAScavenger : Quest(417, "Path To Become A Scavenger") {
    init {

        setItemsIds(
            PIPPI_LETTER,
            RAUT_TELEPORT_SCROLL,
            SUCCUBUS_UNDIES,
            MION_LETTER,
            BRONK_INGOT,
            SHARI_AXE,
            ZIMENF_POTION,
            BRONK_PAY,
            SHARI_PAY,
            ZIMENF_PAY,
            BEAR_PICTURE,
            TARANTULA_PICTURE,
            HONEY_JAR,
            BEAD,
            BEAD_PARCEL_1,
            BEAD_PARCEL_2
        )

        addStartNpc(PIPPI)
        addTalkId(RAUT, SHARI, MION, PIPPI, BRONK, ZIMENF, TOMA, TORAI, YASHENI)

        addKillId(HUNTER_TARANTULA, PLUNDER_TARANTULA, HUNTER_BEAR, HONEY_BEAR)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // PIPPI
        if (event.equals("30524-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DWARVEN_FIGHTER)
                htmltext = if (player.classId == ClassId.SCAVENGER) "30524-02a.htm" else "30524-08.htm"
            else if (player.level < 19)
                htmltext = "30524-02.htm"
            else if (st.hasQuestItems(RING_OF_RAVEN))
                htmltext = "30524-04.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(PIPPI_LETTER, 1)
            }
        } else if (event.equals("30519_1", ignoreCase = true)) {
            val random = Rnd[3]

            htmltext = "30519-0" + (random + 2) + ".htm"
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PIPPI_LETTER, -1)
            st.giveItems(ZIMENF_POTION - random, 1)
        } else if (event.equals("30519_2", ignoreCase = true)) {
            val random = Rnd[3]

            htmltext = "30519-0" + (random + 2) + ".htm"
            st.takeItems(BRONK_PAY, -1)
            st.takeItems(SHARI_PAY, -1)
            st.takeItems(ZIMENF_PAY, -1)
            st.giveItems(ZIMENF_POTION - random, 1)
        } else if (event.equals("30519-07.htm", ignoreCase = true))
            st["id"] = (st.getInt("id") + 1).toString()
        else if (event.equals("30519-09.htm", ignoreCase = true)) {
            val id = st.getInt("id")
            if (id / 10 < 2) {
                htmltext = "30519-07.htm"
                st["id"] = (id + 1).toString()
            } else if (id / 10 == 2)
                st["id"] = (id + 1).toString()
            else if (id / 10 >= 3) {
                htmltext = "30519-10.htm"
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(SHARI_AXE, -1)
                st.takeItems(ZIMENF_POTION, -1)
                st.takeItems(BRONK_INGOT, -1)
                st.giveItems(MION_LETTER, 1)
            }
        } else if (event.equals("30519-11.htm", ignoreCase = true) && Rnd.nextBoolean())
            htmltext = "30519-06.htm"
        else if (event.equals("30556-05b.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BEAD, -1)
            st.takeItems(TARANTULA_PICTURE, 1)
            st.giveItems(BEAD_PARCEL_1, 1)
        } else if (event.equals("30556-06b.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BEAD, -1)
            st.takeItems(TARANTULA_PICTURE, 1)
            st.giveItems(BEAD_PARCEL_2, 1)
        } else if (event.equals("30316-02.htm", ignoreCase = true) || event.equals("30316-03.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BEAD_PARCEL_1, 1)
            st.giveItems(RAUT_TELEPORT_SCROLL, 1)
        } else if (event.equals("30557-03.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RAUT_TELEPORT_SCROLL, 1)
            st.giveItems(SUCCUBUS_UNDIES, 1)
        } else if (event.equals("31958-02.htm", ignoreCase = true)) {
            st.takeItems(BEAD_PARCEL_2, 1)
            st.giveItems(RING_OF_RAVEN, 1)
            st.rewardExpAndSp(3200, 7080)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }// YASHENI
        // TORAI
        // RAUT
        // TOMA
        // MION

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30524-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    PIPPI -> if (cond == 1)
                        htmltext = "30524-06.htm"
                    else if (cond > 1)
                        htmltext = "30524-07.htm"

                    MION -> if (st.hasQuestItems(PIPPI_LETTER))
                        htmltext = "30519-01.htm"
                    else if (st.hasAtLeastOneQuestItem(BRONK_INGOT, SHARI_AXE, ZIMENF_POTION)) {
                        val id = st.getInt("id")
                        if (id / 10 == 0)
                            htmltext = "30519-05.htm"
                        else
                            htmltext = "30519-08.htm"
                    } else if (st.hasAtLeastOneQuestItem(BRONK_PAY, SHARI_PAY, ZIMENF_PAY)) {
                        val id = st.getInt("id")
                        if (id < 50)
                            htmltext = "30519-12.htm"
                        else {
                            htmltext = "30519-15.htm"
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(BRONK_PAY, -1)
                            st.takeItems(SHARI_PAY, -1)
                            st.takeItems(ZIMENF_PAY, -1)
                            st.giveItems(MION_LETTER, 1)
                        }
                    } else if (cond == 4)
                        htmltext = "30519-13.htm"
                    else if (cond > 4)
                        htmltext = "30519-14.htm"

                    SHARI -> if (st.hasQuestItems(SHARI_AXE)) {
                        val id = st.getInt("id")
                        if (id < 20)
                            htmltext = "30517-01.htm"
                        else {
                            htmltext = "30517-02.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                        st["id"] = (id + 10).toString()
                        st.takeItems(SHARI_AXE, 1)
                        st.giveItems(SHARI_PAY, 1)
                    } else if (st.hasQuestItems(SHARI_PAY))
                        htmltext = "30517-03.htm"

                    BRONK -> if (st.hasQuestItems(BRONK_INGOT)) {
                        val id = st.getInt("id")
                        if (id < 20)
                            htmltext = "30525-01.htm"
                        else {
                            htmltext = "30525-02.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                        st["id"] = (id + 10).toString()
                        st.takeItems(BRONK_INGOT, 1)
                        st.giveItems(BRONK_PAY, 1)
                    } else if (st.hasQuestItems(BRONK_PAY))
                        htmltext = "30525-03.htm"

                    ZIMENF -> if (st.hasQuestItems(ZIMENF_POTION)) {
                        val id = st.getInt("id")
                        if (id < 20)
                            htmltext = "30538-01.htm"
                        else {
                            htmltext = "30538-02.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                        st["id"] = (id + 10).toString()
                        st.takeItems(ZIMENF_POTION, 1)
                        st.giveItems(ZIMENF_PAY, 1)
                    } else if (st.hasQuestItems(ZIMENF_PAY))
                        htmltext = "30538-03.htm"

                    TOMA -> if (cond == 4) {
                        htmltext = "30556-01.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MION_LETTER, 1)
                        st.giveItems(BEAR_PICTURE, 1)
                    } else if (cond == 5)
                        htmltext = "30556-02.htm"
                    else if (cond == 6) {
                        htmltext = "30556-03.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HONEY_JAR, -1)
                        st.takeItems(BEAR_PICTURE, 1)
                        st.giveItems(TARANTULA_PICTURE, 1)
                    } else if (cond == 7)
                        htmltext = "30556-04.htm"
                    else if (cond == 8)
                        htmltext = "30556-05a.htm"
                    else if (cond == 9)
                        htmltext = "30556-06a.htm"
                    else if (cond == 10 || cond == 11)
                        htmltext = "30556-07.htm"
                    else if (cond == 12)
                        htmltext = "30556-06c.htm"

                    RAUT -> if (cond == 9)
                        htmltext = "30316-01.htm"
                    else if (cond == 10)
                        htmltext = "30316-04.htm"
                    else if (cond == 11) {
                        htmltext = "30316-05.htm"
                        st.takeItems(SUCCUBUS_UNDIES, 1)
                        st.giveItems(RING_OF_RAVEN, 1)
                        st.rewardExpAndSp(3200, 7080)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    TORAI -> if (cond == 10)
                        htmltext = "30557-01.htm"

                    YASHENI -> if (cond == 12)
                        htmltext = "31958-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            HUNTER_BEAR -> if (st.getInt("cond") == 5) {
                val step = st.getInt("step")
                if (step > 20) {
                    if ((step - 20) * 10 >= Rnd[100]) {
                        addSpawn(HONEY_BEAR, npc, false, 300000, true)
                        st.unset("step")
                    } else
                        st["step"] = (step + 1).toString()
                } else
                    st["step"] = (step + 1).toString()
            }

            HONEY_BEAR -> if (st.getInt("cond") == 5 && npc.spoilerId == player!!.objectId && st.dropItemsAlways(
                    HONEY_JAR,
                    1,
                    5
                )
            )
                st["cond"] = "6"

            HUNTER_TARANTULA, PLUNDER_TARANTULA -> if (st.getInt("cond") == 7 && npc.spoilerId == player!!.objectId && st.dropItems(
                    BEAD,
                    1,
                    20,
                    if (npc.npcId == HUNTER_TARANTULA) 333333 else 600000
                )
            )
                st["cond"] = "8"
        }

        return null
    }

    companion object {
        private val qn = "Q417_PathToBecomeAScavenger"

        // Items
        private val RING_OF_RAVEN = 1642
        private val PIPPI_LETTER = 1643
        private val RAUT_TELEPORT_SCROLL = 1644
        private val SUCCUBUS_UNDIES = 1645
        private val MION_LETTER = 1646
        private val BRONK_INGOT = 1647
        private val SHARI_AXE = 1648
        private val ZIMENF_POTION = 1649
        private val BRONK_PAY = 1650
        private val SHARI_PAY = 1651
        private val ZIMENF_PAY = 1652
        private val BEAR_PICTURE = 1653
        private val TARANTULA_PICTURE = 1654
        private val HONEY_JAR = 1655
        private val BEAD = 1656
        private val BEAD_PARCEL_1 = 1657
        private val BEAD_PARCEL_2 = 8543

        // NPCs
        private val RAUT = 30316
        private val SHARI = 30517
        private val MION = 30519
        private val PIPPI = 30524
        private val BRONK = 30525
        private val ZIMENF = 30538
        private val TOMA = 30556
        private val TORAI = 30557
        private val YASHENI = 31958

        // Monsters
        private val HUNTER_TARANTULA = 20403
        private val PLUNDER_TARANTULA = 20508
        private val HUNTER_BEAR = 20777
        private val HONEY_BEAR = 27058
    }
}