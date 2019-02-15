package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q418_PathToAnArtisan : Quest(418, "Path to an Artisan") {
    init {

        setItemsIds(
            SILVERA_RING,
            FIRST_PASS_CERTIFICATE,
            SECOND_PASS_CERTIFICATE,
            BOOGLE_RATMAN_TOOTH,
            BOOGLE_RATMAN_LEADER_TOOTH,
            KLUTO_LETTER,
            FOOTPRINT_OF_THIEF,
            STOLEN_SECRET_BOX,
            SECRET_BOX
        )

        addStartNpc(SILVERA)
        addTalkId(SILVERA, KLUTO, PINTER, OBI, HITCHI, LOCKIRIN, RYDEL)

        addKillId(20389, 20390, 20017)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30527-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.DWARVEN_FIGHTER)
                htmltext = if (player.classId == ClassId.ARTISAN) "30527-02a.htm" else "30527-02.htm"
            else if (player.level < 19)
                htmltext = "30527-03.htm"
            else if (st.hasQuestItems(FINAL_PASS_CERTIFICATE))
                htmltext = "30527-04.htm"
        } else if (event.equals("30527-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(SILVERA_RING, 1)
        } else if (event.equals("30527-08a.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BOOGLE_RATMAN_LEADER_TOOTH, -1)
            st.takeItems(BOOGLE_RATMAN_TOOTH, -1)
            st.takeItems(SILVERA_RING, 1)
            st.giveItems(FIRST_PASS_CERTIFICATE, 1)
        } else if (event.equals("30527-08b.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BOOGLE_RATMAN_LEADER_TOOTH, -1)
            st.takeItems(BOOGLE_RATMAN_TOOTH, -1)
            st.takeItems(SILVERA_RING, 1)
        } else if (event.equals("30317-04.htm", ignoreCase = true) || event.equals("30317-07.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(KLUTO_LETTER, 1)
        } else if (event.equals("30317-10.htm", ignoreCase = true)) {
            st.takeItems(FIRST_PASS_CERTIFICATE, 1)
            st.takeItems(SECOND_PASS_CERTIFICATE, 1)
            st.takeItems(SECRET_BOX, 1)
            st.giveItems(FINAL_PASS_CERTIFICATE, 1)
            st.rewardExpAndSp(3200, 6980)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30317-12.htm", ignoreCase = true) || event.equals(
                "30531-05.htm",
                ignoreCase = true
            ) || event.equals("32052-11.htm", ignoreCase = true) || event.equals(
                "31963-10.htm",
                ignoreCase = true
            ) || event.equals("31956-04.htm", ignoreCase = true)
        ) {
            st.takeItems(FIRST_PASS_CERTIFICATE, 1)
            st.takeItems(SECOND_PASS_CERTIFICATE, 1)
            st.takeItems(SECRET_BOX, 1)
            st.giveItems(FINAL_PASS_CERTIFICATE, 1)
            st.rewardExpAndSp(3200, 3490)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30298-03.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(KLUTO_LETTER, -1)
            st.giveItems(FOOTPRINT_OF_THIEF, 1)
        } else if (event.equals("30298-06.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(FOOTPRINT_OF_THIEF, -1)
            st.takeItems(STOLEN_SECRET_BOX, -1)
            st.giveItems(SECOND_PASS_CERTIFICATE, 1)
            st.giveItems(SECRET_BOX, 1)
        } else if (event.equals("32052-06.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31963-04.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31963-05.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31963-07.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30527-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SILVERA -> if (cond == 1)
                        htmltext = "30527-07.htm"
                    else if (cond == 2)
                        htmltext = "30527-08.htm"
                    else if (cond == 3)
                        htmltext = "30527-09.htm"
                    else if (cond == 8)
                        htmltext = "30527-09a.htm"

                    KLUTO -> if (cond == 3)
                        htmltext = "30317-01.htm"
                    else if (cond == 4)
                        htmltext = "30317-08.htm"
                    else if (cond == 7)
                        htmltext = "30317-09.htm"

                    PINTER -> if (cond == 4)
                        htmltext = "30298-01.htm"
                    else if (cond == 5)
                        htmltext = "30298-04.htm"
                    else if (cond == 6)
                        htmltext = "30298-05.htm"
                    else if (cond == 7)
                        htmltext = "30298-07.htm"

                    OBI -> if (cond == 8)
                        htmltext = "32052-01.htm"
                    else if (cond == 9)
                        htmltext = "32052-06a.htm"
                    else if (cond == 11)
                        htmltext = "32052-07.htm"

                    HITCHI -> if (cond == 9)
                        htmltext = "31963-01.htm"
                    else if (cond == 10)
                        htmltext = "31963-04.htm"
                    else if (cond == 11)
                        htmltext = "31963-06a.htm"
                    else if (cond == 12)
                        htmltext = "31963-08.htm"

                    LOCKIRIN -> if (cond == 10)
                        htmltext = "30531-01.htm"

                    RYDEL -> if (cond == 12)
                        htmltext = "31956-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20389 -> if (st.getInt("cond") == 1 && st.dropItems(
                    BOOGLE_RATMAN_TOOTH,
                    1,
                    10,
                    700000
                ) && st.getQuestItemsCount(BOOGLE_RATMAN_LEADER_TOOTH) == 2
            )
                st["cond"] = "2"

            20390 -> if (st.getInt("cond") == 1 && st.dropItems(
                    BOOGLE_RATMAN_LEADER_TOOTH,
                    1,
                    2,
                    500000
                ) && st.getQuestItemsCount(BOOGLE_RATMAN_TOOTH) == 10
            )
                st["cond"] = "2"

            20017 -> if (st.getInt("cond") == 5 && st.dropItems(STOLEN_SECRET_BOX, 1, 1, 200000))
                st["cond"] = "6"
        }

        return null
    }

    companion object {
        private val qn = "Q418_PathToAnArtisan"

        // Items
        private val SILVERA_RING = 1632
        private val FIRST_PASS_CERTIFICATE = 1633
        private val SECOND_PASS_CERTIFICATE = 1634
        private val FINAL_PASS_CERTIFICATE = 1635
        private val BOOGLE_RATMAN_TOOTH = 1636
        private val BOOGLE_RATMAN_LEADER_TOOTH = 1637
        private val KLUTO_LETTER = 1638
        private val FOOTPRINT_OF_THIEF = 1639
        private val STOLEN_SECRET_BOX = 1640
        private val SECRET_BOX = 1641

        // NPCs
        private val SILVERA = 30527
        private val KLUTO = 30317
        private val PINTER = 30298
        private val OBI = 32052
        private val HITCHI = 31963
        private val LOCKIRIN = 30531
        private val RYDEL = 31956
    }
}