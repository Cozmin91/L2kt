package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q226_TestOfTheHealer : Quest(226, "Test of the Healer") {

    private var _tatoma: Npc? = null
    private var _letoLeader: Npc? = null

    init {

        setItemsIds(
            REPORT_OF_PERRIN,
            KRISTINA_LETTER,
            PICTURE_OF_WINDY,
            GOLDEN_STATUE,
            WINDY_PEBBLES,
            ORDER_OF_SORIUS,
            SECRET_LETTER_1,
            SECRET_LETTER_2,
            SECRET_LETTER_3,
            SECRET_LETTER_4
        )

        addStartNpc(BANDELLOS)
        addTalkId(
            BANDELLOS,
            SORIUS,
            ALLANA,
            PERRIN,
            GUPU,
            ORPHAN_GIRL,
            WINDY_SHAORING,
            MYSTERIOUS_DARKELF,
            PIPER_LONGBOW,
            SLEIN_SHINING_BLADE,
            KAIN_FLYING_KNIFE,
            KRISTINA,
            DAURIN_HAMMERCRUSH
        )

        addKillId(
            LETO_LIZARDMAN_LEADER,
            LETO_LIZARDMAN_ASSASSIN,
            LETO_LIZARDMAN_SNIPER,
            LETO_LIZARDMAN_WIZARD,
            LETO_LIZARDMAN_LORD,
            TATOMA
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // BANDELLOS
        if (event.equals("30473-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(REPORT_OF_PERRIN, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30473-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30473-09.htm", ignoreCase = true)) {
            st.takeItems(GOLDEN_STATUE, 1)
            st.giveItems(MARK_OF_HEALER, 1)
            st.rewardExpAndSp(134839, 50000)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30428-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)

            if (_tatoma == null) {
                _tatoma = addSpawn(TATOMA, -93254, 147559, -2679, 0, false, 0, false)
                startQuestTimer("tatoma_despawn", 200000, null, player, false)
            }
        } else if (event.equals("30658-02.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(57) >= 100000) {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(57, 100000)
                st.giveItems(PICTURE_OF_WINDY, 1)
            } else
                htmltext = "30658-05.htm"
        } else if (event.equals("30658-03.htm", ignoreCase = true))
            st["gupu"] = "1"
        else if (event.equals("30658-07.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30660-03.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PICTURE_OF_WINDY, 1)
            st.giveItems(WINDY_PEBBLES, 1)
        } else if (event.equals("30674-02.htm", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_BEFORE_BATTLE)
            st.takeItems(ORDER_OF_SORIUS, 1)

            if (_letoLeader == null) {
                _letoLeader = addSpawn(LETO_LIZARDMAN_LEADER, -97441, 106585, -3405, 0, false, 0, false)
                startQuestTimer("leto_leader_despawn", 200000, null, player, false)
            }
        } else if (event.equals("30665-02.htm", ignoreCase = true)) {
            st["cond"] = "22"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SECRET_LETTER_1, 1)
            st.takeItems(SECRET_LETTER_2, 1)
            st.takeItems(SECRET_LETTER_3, 1)
            st.takeItems(SECRET_LETTER_4, 1)
            st.giveItems(KRISTINA_LETTER, 1)
        } else if (event.equals("tatoma_despawn", ignoreCase = true)) {
            _tatoma!!.deleteMe()
            _tatoma = null
            return null
        } else if (event.equals("leto_leader_despawn", ignoreCase = true)) {
            _letoLeader!!.deleteMe()
            _letoLeader = null
            return null
        }// DESPAWNS
        // KRISTINA
        // DAURIN HAMMERCRUSH
        // WINDY SHAORING
        // GUPU
        // PERRIN

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.KNIGHT && player.classId != ClassId.ELVEN_KNIGHT && player.classId != ClassId.CLERIC && player.classId != ClassId.ELVEN_ORACLE)
                htmltext = "30473-01.htm"
            else if (player.level < 39)
                htmltext = "30473-02.htm"
            else
                htmltext = "30473-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BANDELLOS -> if (cond < 23)
                        htmltext = "30473-05.htm"
                    else {
                        if (!st.hasQuestItems(GOLDEN_STATUE)) {
                            htmltext = "30473-06.htm"
                            st.giveItems(MARK_OF_HEALER, 1)
                            st.rewardExpAndSp(118304, 26250)
                            player.broadcastPacket(SocialAction(player, 3))
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(false)
                        } else
                            htmltext = "30473-07.htm"
                    }

                    PERRIN -> if (cond < 3)
                        htmltext = "30428-01.htm"
                    else if (cond == 3) {
                        htmltext = "30428-03.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(REPORT_OF_PERRIN, 1)
                    } else
                        htmltext = "30428-04.htm"

                    ORPHAN_GIRL -> htmltext = "30659-0" + Rnd[1, 5] + ".htm"

                    ALLANA -> if (cond == 4) {
                        htmltext = "30424-01.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 4)
                        htmltext = "30424-02.htm"

                    GUPU -> if (st.getInt("gupu") == 1 && cond != 9) {
                        htmltext = "30658-07.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 5) {
                        htmltext = "30658-01.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 6)
                        htmltext = "30658-01.htm"
                    else if (cond == 7)
                        htmltext = "30658-04.htm"
                    else if (cond == 8) {
                        htmltext = "30658-06.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.takeItems(WINDY_PEBBLES, 1)
                        st.giveItems(GOLDEN_STATUE, 1)
                    } else if (cond > 8)
                        htmltext = "30658-07.htm"

                    WINDY_SHAORING -> if (cond == 7)
                        htmltext = "30660-01.htm"
                    else if (st.hasQuestItems(WINDY_PEBBLES))
                        htmltext = "30660-04.htm"

                    SORIUS -> if (cond == 9) {
                        htmltext = "30327-01.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(ORDER_OF_SORIUS, 1)
                    } else if (cond > 9 && cond < 22)
                        htmltext = "30327-02.htm"
                    else if (cond == 22) {
                        htmltext = "30327-03.htm"
                        st["cond"] = "23"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KRISTINA_LETTER, 1)
                    } else if (cond == 23)
                        htmltext = "30327-04.htm"

                    DAURIN_HAMMERCRUSH -> if (cond == 10)
                        htmltext = "30674-01.htm"
                    else if (cond == 11) {
                        htmltext = "30674-02a.htm"
                        if (_letoLeader == null) {
                            _letoLeader = addSpawn(LETO_LIZARDMAN_LEADER, -97441, 106585, -3405, 0, false, 0, false)
                            startQuestTimer("leto_leader_despawn", 200000, null, player, false)
                        }
                    } else if (cond == 12) {
                        htmltext = "30674-03.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 12)
                        htmltext = "30674-04.htm"

                    PIPER_LONGBOW, SLEIN_SHINING_BLADE, KAIN_FLYING_KNIFE -> if (cond == 13 || cond == 14)
                        htmltext = npc.npcId.toString() + "-01.htm"
                    else if (cond > 14 && cond < 19)
                        htmltext = npc.npcId.toString() + "-02.htm"
                    else if (cond > 18 && cond < 22) {
                        htmltext = npc.npcId.toString() + "-03.htm"
                        st["cond"] = "21"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    }

                    MYSTERIOUS_DARKELF -> if (cond == 13) {
                        htmltext = "30661-01.htm"
                        st["cond"] = "14"
                        st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                        addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_ASSASSIN, player, true, 0, false)
                    } else if (cond == 14)
                        htmltext = "30661-01.htm"
                    else if (cond == 15) {
                        htmltext = "30661-02.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                        addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_SNIPER, player, true, 0, false)
                    } else if (cond == 16)
                        htmltext = "30661-02.htm"
                    else if (cond == 17) {
                        htmltext = "30661-03.htm"
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                        addSpawn(LETO_LIZARDMAN_WIZARD, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_WIZARD, player, true, 0, false)
                        addSpawn(LETO_LIZARDMAN_LORD, player, true, 0, false)
                    } else if (cond == 18)
                        htmltext = "30661-03.htm"
                    else if (cond == 19) {
                        htmltext = "30661-04.htm"
                        st["cond"] = "20"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 20 || cond == 21)
                        htmltext = "30661-04.htm"

                    KRISTINA -> if (cond > 18 && cond < 22)
                        htmltext = "30665-01.htm"
                    else if (cond > 21)
                        htmltext = "30665-04.htm"
                    else
                        htmltext = "30665-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        when (npc.npcId) {
            TATOMA -> {
                if (cond == 1 || cond == 2) {
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                }
                _tatoma = null
                cancelQuestTimer("tatoma_despawn", null, player)
            }

            LETO_LIZARDMAN_LEADER -> {
                if (cond == 10 || cond == 11) {
                    st["cond"] = "12"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(SECRET_LETTER_1, 1)
                }
                _letoLeader = null
                cancelQuestTimer("leto_leader_despawn", null, player)
            }

            LETO_LIZARDMAN_ASSASSIN -> if (cond == 13 || cond == 14) {
                st["cond"] = "15"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(SECRET_LETTER_2, 1)
            }

            LETO_LIZARDMAN_SNIPER -> if (cond == 15 || cond == 16) {
                st["cond"] = "17"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(SECRET_LETTER_3, 1)
            }

            LETO_LIZARDMAN_LORD -> if (cond == 17 || cond == 18) {
                st["cond"] = "19"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(SECRET_LETTER_4, 1)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q226_TestOfTheHealer"

        // Items
        private val REPORT_OF_PERRIN = 2810
        private val KRISTINA_LETTER = 2811
        private val PICTURE_OF_WINDY = 2812
        private val GOLDEN_STATUE = 2813
        private val WINDY_PEBBLES = 2814
        private val ORDER_OF_SORIUS = 2815
        private val SECRET_LETTER_1 = 2816
        private val SECRET_LETTER_2 = 2817
        private val SECRET_LETTER_3 = 2818
        private val SECRET_LETTER_4 = 2819

        // Rewards
        private val MARK_OF_HEALER = 2820
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val BANDELLOS = 30473
        private val SORIUS = 30327
        private val ALLANA = 30424
        private val PERRIN = 30428
        private val GUPU = 30658
        private val ORPHAN_GIRL = 30659
        private val WINDY_SHAORING = 30660
        private val MYSTERIOUS_DARKELF = 30661
        private val PIPER_LONGBOW = 30662
        private val SLEIN_SHINING_BLADE = 30663
        private val KAIN_FLYING_KNIFE = 30664
        private val KRISTINA = 30665
        private val DAURIN_HAMMERCRUSH = 30674

        // Monsters
        private val LETO_LIZARDMAN_LEADER = 27123
        private val LETO_LIZARDMAN_ASSASSIN = 27124
        private val LETO_LIZARDMAN_SNIPER = 27125
        private val LETO_LIZARDMAN_WIZARD = 27126
        private val LETO_LIZARDMAN_LORD = 27127
        private val TATOMA = 27134
    }
}