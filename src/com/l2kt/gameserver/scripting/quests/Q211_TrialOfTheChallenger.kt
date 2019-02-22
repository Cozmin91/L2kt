package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q211_TrialOfTheChallenger : Quest(211, "Trial of the Challenger") {
    init {

        setItemsIds(LETTER_OF_KASH, WATCHER_EYE_1, WATCHER_EYE_2, SCROLL_OF_SHYSLASSYS, BROKEN_KEY)

        addStartNpc(KASH)
        addTalkId(FILAUR, KASH, MARTIEN, RALDO, CHEST_OF_SHYSLASSYS)

        addKillId(SHYSLASSYS, GORR, BARAHAM, SUCCUBUS_QUEEN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // KASH
        if (event.equals("30644-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            if (!player.memos.getBool("secondClassChange35", false)) {
                htmltext = "30644-05a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                player.memos.set("secondClassChange35", true)
            }
        } else if (event.equals("30645-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_OF_KASH, 1)
        } else if (event.equals("30646-04.htm", ignoreCase = true) || event.equals("30646-06.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(WATCHER_EYE_2, 1)
        } else if (event.equals("30647-04.htm", ignoreCase = true)) {
            if (st.hasQuestItems(BROKEN_KEY)) {
                if (Rnd[10] < 2) {
                    htmltext = "30647-03.htm"
                    st.playSound(QuestState.SOUND_JACKPOT)
                    st.takeItems(BROKEN_KEY, 1)
                    val chance = Rnd[100]
                    if (chance > 90) {
                        st.rewardItems(BRIGANDINE_GAUNTLETS_PATTERN, 1)
                        st.rewardItems(IRON_BOOTS_DESIGN, 1)
                        st.rewardItems(MANTICOR_SKIN_GAITERS_PATTERN, 1)
                        st.rewardItems(MITHRIL_SCALE_GAITERS_MATERIAL, 1)
                        st.rewardItems(RIP_GAUNTLETS_PATTERN, 1)
                    } else if (chance > 70) {
                        st.rewardItems(ELVEN_NECKLACE_BEADS, 1)
                        st.rewardItems(TOME_OF_BLOOD_PAGE, 1)
                    } else if (chance > 40)
                        st.rewardItems(WHITE_TUNIC_PATTERN, 1)
                    else
                        st.rewardItems(IRON_BOOTS_DESIGN, 1)
                } else {
                    htmltext = "30647-02.htm"
                    st.takeItems(BROKEN_KEY, 1)
                    st.rewardItems(ADENA, Rnd[1, 1000])
                }
            }
        }// CHEST_OF_SHYSLASSYS
        // RALDO
        // MARTIEN

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.WARRIOR && player.classId != ClassId.ELVEN_KNIGHT && player.classId != ClassId.PALUS_KNIGHT && player.classId != ClassId.ORC_RAIDER && player.classId != ClassId.MONK)
                htmltext = "30644-02.htm"
            else if (player.level < 35)
                htmltext = "30644-01.htm"
            else
                htmltext = "30644-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    KASH -> if (cond == 1)
                        htmltext = "30644-06.htm"
                    else if (cond == 2) {
                        htmltext = "30644-07.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SCROLL_OF_SHYSLASSYS, 1)
                        st.giveItems(LETTER_OF_KASH, 1)
                    } else if (cond == 3)
                        htmltext = "30644-08.htm"
                    else if (cond > 3)
                        htmltext = "30644-09.htm"

                    CHEST_OF_SHYSLASSYS -> htmltext = "30647-01.htm"

                    MARTIEN -> if (cond == 3)
                        htmltext = "30645-01.htm"
                    else if (cond == 4)
                        htmltext = "30645-03.htm"
                    else if (cond == 5) {
                        htmltext = "30645-04.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(WATCHER_EYE_1, 1)
                    } else if (cond == 6)
                        htmltext = "30645-05.htm"
                    else if (cond == 7)
                        htmltext = "30645-07.htm"
                    else if (cond > 7)
                        htmltext = "30645-06.htm"

                    RALDO -> if (cond == 7)
                        htmltext = "30646-01.htm"
                    else if (cond == 8)
                        htmltext = "30646-06a.htm"
                    else if (cond == 10) {
                        htmltext = "30646-07.htm"
                        st.takeItems(BROKEN_KEY, 1)
                        st.giveItems(MARK_OF_CHALLENGER, 1)
                        st.rewardExpAndSp(72394, 11250)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    FILAUR -> if (cond == 8) {
                        if (player.level >= 36) {
                            htmltext = "30535-01.htm"
                            st["cond"] = "9"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "30535-03.htm"
                    } else if (cond == 9) {
                        htmltext = "30535-02.htm"
                        st.addRadar(176560, -184969, -3729)
                    } else if (cond == 10)
                        htmltext = "30535-04.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            SHYSLASSYS -> if (st.getInt("cond") == 1) {
                st["cond"] = "2"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(BROKEN_KEY, 1)
                st.giveItems(SCROLL_OF_SHYSLASSYS, 1)
                addSpawn(CHEST_OF_SHYSLASSYS, npc, false, 200000, true)
            }

            GORR -> if (st.getInt("cond") == 4 && st.dropItemsAlways(WATCHER_EYE_1, 1, 1))
                st["cond"] = "5"

            BARAHAM -> {
                if (st.getInt("cond") == 6 && st.dropItemsAlways(WATCHER_EYE_2, 1, 1))
                    st["cond"] = "7"
                addSpawn(RALDO, npc, false, 100000, true)
            }

            SUCCUBUS_QUEEN -> {
                if (st.getInt("cond") == 9) {
                    st["cond"] = "10"
                    st.playSound(QuestState.SOUND_MIDDLE)
                }
                addSpawn(RALDO, npc, false, 100000, true)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q211_TrialOfTheChallenger"

        // Items
        private val LETTER_OF_KASH = 2628
        private val WATCHER_EYE_1 = 2629
        private val WATCHER_EYE_2 = 2630
        private val SCROLL_OF_SHYSLASSYS = 2631
        private val BROKEN_KEY = 2632

        // Rewards
        private val ADENA = 57
        private val ELVEN_NECKLACE_BEADS = 1904
        private val WHITE_TUNIC_PATTERN = 1936
        private val IRON_BOOTS_DESIGN = 1940
        private val MANTICOR_SKIN_GAITERS_PATTERN = 1943
        private val RIP_GAUNTLETS_PATTERN = 1946
        private val TOME_OF_BLOOD_PAGE = 2030
        private val MITHRIL_SCALE_GAITERS_MATERIAL = 2918
        private val BRIGANDINE_GAUNTLETS_PATTERN = 2927
        private val MARK_OF_CHALLENGER = 2627
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val FILAUR = 30535
        private val KASH = 30644
        private val MARTIEN = 30645
        private val RALDO = 30646
        private val CHEST_OF_SHYSLASSYS = 30647

        // Monsters
        private val SHYSLASSYS = 27110
        private val GORR = 27112
        private val BARAHAM = 27113
        private val SUCCUBUS_QUEEN = 27114
    }
}