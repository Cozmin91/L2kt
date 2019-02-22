package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q414_PathToAnOrcRaider : Quest(414, "Path To An Orc Raider") {
    init {

        setItemsIds(
            GREEN_BLOOD,
            GOBLIN_DWELLING_MAP,
            KURUKA_RATMAN_TOOTH,
            BETRAYER_REPORT_1,
            BETRAYER_REPORT_2,
            HEAD_OF_BETRAYER,
            TIMORA_ORC_HEAD
        )

        addStartNpc(KARUKIA)
        addTalkId(KARUKIA, KASMAN, TAZEER)

        addKillId(GOBLIN_TOMB_RAIDER_LEADER, KURUKA_RATMAN_LEADER, UMBAR_ORC, TIMORA_ORC)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // KARUKIA
        if (event.equals("30570-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ORC_FIGHTER)
                htmltext = if (player.classId == ClassId.ORC_RAIDER) "30570-02a.htm" else "30570-03.htm"
            else if (player.level < 19)
                htmltext = "30570-02.htm"
            else if (st.hasQuestItems(MARK_OF_RAIDER))
                htmltext = "30570-04.htm"
            else {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(GOBLIN_DWELLING_MAP, 1)
            }
        } else if (event.equals("30570-07a.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GOBLIN_DWELLING_MAP, 1)
            st.takeItems(KURUKA_RATMAN_TOOTH, -1)
            st.giveItems(BETRAYER_REPORT_1, 1)
            st.giveItems(BETRAYER_REPORT_2, 1)
        } else if (event.equals("30570-07b.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GOBLIN_DWELLING_MAP, 1)
            st.takeItems(KURUKA_RATMAN_TOOTH, -1)
        } else if (event.equals("31978-03.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        }// TAZEER

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30570-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    KARUKIA -> if (cond == 1)
                        htmltext = "30570-06.htm"
                    else if (cond == 2)
                        htmltext = "30570-07.htm"
                    else if (cond == 3 || cond == 4)
                        htmltext = "30570-08.htm"
                    else if (cond == 5)
                        htmltext = "30570-07b.htm"

                    KASMAN -> if (cond == 3)
                        htmltext = "30501-01.htm"
                    else if (cond == 4) {
                        if (st.getQuestItemsCount(HEAD_OF_BETRAYER) == 1)
                            htmltext = "30501-02.htm"
                        else {
                            htmltext = "30501-03.htm"
                            st.takeItems(BETRAYER_REPORT_1, 1)
                            st.takeItems(BETRAYER_REPORT_2, 1)
                            st.takeItems(HEAD_OF_BETRAYER, -1)
                            st.giveItems(MARK_OF_RAIDER, 1)
                            st.rewardExpAndSp(3200, 2360)
                            player.broadcastPacket(SocialAction(player, 3))
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(true)
                        }
                    }

                    TAZEER -> if (cond == 5)
                        htmltext = "31978-01.htm"
                    else if (cond == 6)
                        htmltext = "31978-04.htm"
                    else if (cond == 7) {
                        htmltext = "31978-05.htm"
                        st.takeItems(TIMORA_ORC_HEAD, 1)
                        st.giveItems(MARK_OF_RAIDER, 1)
                        st.rewardExpAndSp(3200, 2360)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            GOBLIN_TOMB_RAIDER_LEADER -> if (cond == 1) {
                if (st.getQuestItemsCount(GREEN_BLOOD) <= Rnd[20]) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(GREEN_BLOOD, 1)
                } else {
                    st.takeItems(GREEN_BLOOD, -1)
                    addSpawn(KURUKA_RATMAN_LEADER, npc, false, 300000, true)
                }
            }

            KURUKA_RATMAN_LEADER -> if (cond == 1 && st.dropItemsAlways(KURUKA_RATMAN_TOOTH, 1, 10))
                st["cond"] = "2"

            UMBAR_ORC -> if ((cond == 3 || cond == 4) && st.getQuestItemsCount(HEAD_OF_BETRAYER) < 2 && Rnd[10] < 2) {
                if (cond == 3)
                    st["cond"] = "4"

                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(HEAD_OF_BETRAYER, 1)
            }

            TIMORA_ORC -> if (cond == 6 && st.dropItems(TIMORA_ORC_HEAD, 1, 1, 600000))
                st["cond"] = "7"
        }

        return null
    }

    companion object {
        private val qn = "Q414_PathToAnOrcRaider"

        // Items
        private val GREEN_BLOOD = 1578
        private val GOBLIN_DWELLING_MAP = 1579
        private val KURUKA_RATMAN_TOOTH = 1580
        private val BETRAYER_REPORT_1 = 1589
        private val BETRAYER_REPORT_2 = 1590
        private val HEAD_OF_BETRAYER = 1591
        private val MARK_OF_RAIDER = 1592
        private val TIMORA_ORC_HEAD = 8544

        // NPCs
        private val KARUKIA = 30570
        private val KASMAN = 30501
        private val TAZEER = 31978

        // Monsters
        private val GOBLIN_TOMB_RAIDER_LEADER = 20320
        private val KURUKA_RATMAN_LEADER = 27045
        private val UMBAR_ORC = 27054
        private val TIMORA_ORC = 27320
    }
}