package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q409_PathToAnElvenOracle : Quest(409, "Path to an Elven Oracle") {
    init {

        setItemsIds(
            CRYSTAL_MEDALLION,
            SWINDLER_MONEY,
            ALLANA_DIARY,
            LIZARD_CAPTAIN_ORDER,
            HALF_OF_DIARY,
            TAMIL_NECKLACE
        )

        addStartNpc(MANUEL)
        addTalkId(MANUEL, ALLANA, PERRIN)

        addKillId(27032, 27033, 27034, 27035)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30293-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(CRYSTAL_MEDALLION, 1)
        } else if (event.equals("spawn_lizards", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            addSpawn(27032, -92319, 154235, -3284, 2000, false, 0, false)
            addSpawn(27033, -92361, 154190, -3284, 2000, false, 0, false)
            addSpawn(27034, -92375, 154278, -3278, 2000, false, 0, false)
            return null
        } else if (event.equals("30428-06.htm", ignoreCase = true))
            addSpawn(27035, -93194, 147587, -2672, 2000, false, 0, true)

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.ELVEN_MYSTIC)
                htmltext = if (player.classId == ClassId.ELVEN_ORACLE) "30293-02a.htm" else "30293-02.htm"
            else if (player.level < 19)
                htmltext = "30293-03.htm"
            else if (st.hasQuestItems(LEAF_OF_ORACLE))
                htmltext = "30293-04.htm"
            else
                htmltext = "30293-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MANUEL -> if (cond == 1)
                        htmltext = "30293-06.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30293-09.htm"
                    else if (cond > 3 && cond < 7)
                        htmltext = "30293-07.htm"
                    else if (cond == 7) {
                        htmltext = "30293-08.htm"
                        st.takeItems(ALLANA_DIARY, 1)
                        st.takeItems(CRYSTAL_MEDALLION, 1)
                        st.takeItems(LIZARD_CAPTAIN_ORDER, 1)
                        st.takeItems(SWINDLER_MONEY, 1)
                        st.giveItems(LEAF_OF_ORACLE, 1)
                        st.rewardExpAndSp(3200, 1130)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    ALLANA -> if (cond == 1)
                        htmltext = "30424-01.htm"
                    else if (cond == 3) {
                        htmltext = "30424-02.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(HALF_OF_DIARY, 1)
                    } else if (cond == 4)
                        htmltext = "30424-03.htm"
                    else if (cond == 5)
                        htmltext = "30424-06.htm"
                    else if (cond == 6) {
                        htmltext = "30424-04.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HALF_OF_DIARY, -1)
                        st.giveItems(ALLANA_DIARY, 1)
                    } else if (cond == 7)
                        htmltext = "30424-05.htm"

                    PERRIN -> if (cond == 4)
                        htmltext = "30428-01.htm"
                    else if (cond == 5) {
                        htmltext = "30428-04.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TAMIL_NECKLACE, -1)
                        st.giveItems(SWINDLER_MONEY, 1)
                    } else if (cond > 5)
                        htmltext = "30428-05.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId == 27035) {
            if (st.getInt("cond") == 4) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(TAMIL_NECKLACE, 1)
            }
        } else if (st.getInt("cond") == 2) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LIZARD_CAPTAIN_ORDER, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q409_PathToAnElvenOracle"

        // Items
        private val CRYSTAL_MEDALLION = 1231
        private val SWINDLER_MONEY = 1232
        private val ALLANA_DIARY = 1233
        private val LIZARD_CAPTAIN_ORDER = 1234
        private val LEAF_OF_ORACLE = 1235
        private val HALF_OF_DIARY = 1236
        private val TAMIL_NECKLACE = 1275

        // NPCs
        private val MANUEL = 30293
        private val ALLANA = 30424
        private val PERRIN = 30428
    }
}