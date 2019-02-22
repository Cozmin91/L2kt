package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q224_TestOfSagittarius : Quest(224, "Test Of Sagittarius") {
    init {

        setItemsIds(
            BERNARD_INTRODUCTION,
            HAMIL_LETTER_1,
            HAMIL_LETTER_2,
            HAMIL_LETTER_3,
            HUNTER_RUNE_1,
            HUNTER_RUNE_2,
            TALISMAN_OF_KADESH,
            TALISMAN_OF_SNAKE,
            MITHRIL_CLIP,
            STAKATO_CHITIN,
            REINFORCED_BOWSTRING,
            MANASHEN_HORN,
            BLOOD_OF_LIZARDMAN,
            CRESCENT_MOON_BOW
        )

        addStartNpc(BERNARD)
        addTalkId(BERNARD, HAMIL, SIR_ARON_TANFORD, VOKIAN, GAUEN)

        addKillId(
            ANT,
            ANT_CAPTAIN,
            ANT_OVERSEER,
            ANT_RECRUIT,
            ANT_PATROL,
            ANT_GUARD,
            NOBLE_ANT,
            NOBLE_ANT_LEADER,
            BREKA_ORC_SHAMAN,
            BREKA_ORC_OVERLORD,
            MARSH_STAKATO_WORKER,
            MARSH_STAKATO_SOLDIER,
            MARSH_STAKATO_DRONE,
            MARSH_SPIDER,
            ROAD_SCAVENGER,
            MANASHEN_GARGOYLE,
            LETO_LIZARDMAN,
            LETO_LIZARDMAN_ARCHER,
            LETO_LIZARDMAN_SOLDIER,
            LETO_LIZARDMAN_WARRIOR,
            LETO_LIZARDMAN_SHAMAN,
            LETO_LIZARDMAN_OVERLORD,
            SERPENT_DEMON_KADESH
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // BERNARD
        if (event.equals("30702-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BERNARD_INTRODUCTION, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30702-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30626-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BERNARD_INTRODUCTION, 1)
            st.giveItems(HAMIL_LETTER_1, 1)
        } else if (event.equals("30626-07.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HUNTER_RUNE_1, 10)
            st.giveItems(HAMIL_LETTER_2, 1)
        } else if (event.equals("30653-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HAMIL_LETTER_1, 1)
        } else if (event.equals("30514-02.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HAMIL_LETTER_2, 1)
        }// VOKIAN
        // SIR_ARON_TANFORD
        // HAMIL

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.ROGUE && player.classId != ClassId.ELVEN_SCOUT && player.classId != ClassId.ASSASSIN)
                htmltext = "30702-02.htm"
            else if (player.level < 39)
                htmltext = "30702-01.htm"
            else
                htmltext = "30702-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    BERNARD -> htmltext = "30702-05.htm"

                    HAMIL -> if (cond == 1)
                        htmltext = "30626-01.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30626-04.htm"
                    else if (cond == 4)
                        htmltext = "30626-05.htm"
                    else if (cond > 4 && cond < 8)
                        htmltext = "30626-08.htm"
                    else if (cond == 8) {
                        htmltext = "30626-09.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HUNTER_RUNE_2, 10)
                        st.giveItems(HAMIL_LETTER_3, 1)
                    } else if (cond > 8 && cond < 12)
                        htmltext = "30626-10.htm"
                    else if (cond == 12) {
                        htmltext = "30626-11.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 13)
                        htmltext = "30626-12.htm"
                    else if (cond == 14) {
                        htmltext = "30626-13.htm"
                        st.takeItems(BLOOD_OF_LIZARDMAN, -1)
                        st.takeItems(CRESCENT_MOON_BOW, 1)
                        st.takeItems(TALISMAN_OF_KADESH, 1)
                        st.giveItems(MARK_OF_SAGITTARIUS, 1)
                        st.rewardExpAndSp(54726, 20250)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    SIR_ARON_TANFORD -> if (cond == 2)
                        htmltext = "30653-01.htm"
                    else if (cond > 2)
                        htmltext = "30653-03.htm"

                    VOKIAN -> if (cond == 5)
                        htmltext = "30514-01.htm"
                    else if (cond == 6)
                        htmltext = "30514-03.htm"
                    else if (cond == 7) {
                        htmltext = "30514-04.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TALISMAN_OF_SNAKE, 1)
                    } else if (cond > 7)
                        htmltext = "30514-05.htm"

                    GAUEN -> if (cond == 9) {
                        htmltext = "30717-01.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HAMIL_LETTER_3, 1)
                    } else if (cond == 10)
                        htmltext = "30717-03.htm"
                    else if (cond == 11) {
                        htmltext = "30717-02.htm"
                        st["cond"] = "12"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MANASHEN_HORN, 1)
                        st.takeItems(MITHRIL_CLIP, 1)
                        st.takeItems(REINFORCED_BOWSTRING, 1)
                        st.takeItems(STAKATO_CHITIN, 1)
                        st.giveItems(CRESCENT_MOON_BOW, 1)
                        st.giveItems(WOODEN_ARROW, 10)
                    } else if (cond > 11)
                        htmltext = "30717-04.htm"
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
            ANT, ANT_CAPTAIN, ANT_OVERSEER, ANT_RECRUIT, ANT_PATROL, ANT_GUARD, NOBLE_ANT, NOBLE_ANT_LEADER -> if (st.getInt(
                    "cond"
                ) == 3 && st.dropItems(HUNTER_RUNE_1, 1, 10, 500000)
            )
                st["cond"] = "4"

            BREKA_ORC_SHAMAN, BREKA_ORC_OVERLORD -> if (st.getInt("cond") == 6 && st.dropItems(
                    HUNTER_RUNE_2,
                    1,
                    10,
                    500000
                )
            ) {
                st["cond"] = "7"
                st.giveItems(TALISMAN_OF_SNAKE, 1)
            }

            MARSH_STAKATO_WORKER, MARSH_STAKATO_SOLDIER, MARSH_STAKATO_DRONE -> if (st.getInt("cond") == 10 && st.dropItems(
                    STAKATO_CHITIN,
                    1,
                    1,
                    100000
                ) && st.hasQuestItems(MANASHEN_HORN, MITHRIL_CLIP, REINFORCED_BOWSTRING)
            )
                st["cond"] = "11"

            MARSH_SPIDER -> if (st.getInt("cond") == 10 && st.dropItems(
                    REINFORCED_BOWSTRING,
                    1,
                    1,
                    100000
                ) && st.hasQuestItems(MANASHEN_HORN, MITHRIL_CLIP, STAKATO_CHITIN)
            )
                st["cond"] = "11"

            ROAD_SCAVENGER -> if (st.getInt("cond") == 10 && st.dropItems(
                    MITHRIL_CLIP,
                    1,
                    1,
                    100000
                ) && st.hasQuestItems(MANASHEN_HORN, REINFORCED_BOWSTRING, STAKATO_CHITIN)
            )
                st["cond"] = "11"

            MANASHEN_GARGOYLE -> if (st.getInt("cond") == 10 && st.dropItems(
                    MANASHEN_HORN,
                    1,
                    1,
                    100000
                ) && st.hasQuestItems(REINFORCED_BOWSTRING, MITHRIL_CLIP, STAKATO_CHITIN)
            )
                st["cond"] = "11"

            LETO_LIZARDMAN, LETO_LIZARDMAN_ARCHER, LETO_LIZARDMAN_SOLDIER, LETO_LIZARDMAN_WARRIOR, LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD -> if (st.getInt(
                    "cond"
                ) == 13
            ) {
                if ((st.getQuestItemsCount(BLOOD_OF_LIZARDMAN) - 120) * 5 > Rnd[100]) {
                    st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                    st.takeItems(BLOOD_OF_LIZARDMAN, -1)
                    addSpawn(SERPENT_DEMON_KADESH, player!!, false, 300000, true)
                } else
                    st.dropItemsAlways(BLOOD_OF_LIZARDMAN, 1, 0)
            }

            SERPENT_DEMON_KADESH -> if (st.getInt("cond") == 13) {
                if (st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == CRESCENT_MOON_BOW) {
                    st["cond"] = "14"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(TALISMAN_OF_KADESH, 1)
                } else
                    addSpawn(SERPENT_DEMON_KADESH, player!!, false, 300000, true)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q224_TestOfSagittarius"

        // Items
        private val BERNARD_INTRODUCTION = 3294
        private val HAMIL_LETTER_1 = 3295
        private val HAMIL_LETTER_2 = 3296
        private val HAMIL_LETTER_3 = 3297
        private val HUNTER_RUNE_1 = 3298
        private val HUNTER_RUNE_2 = 3299
        private val TALISMAN_OF_KADESH = 3300
        private val TALISMAN_OF_SNAKE = 3301
        private val MITHRIL_CLIP = 3302
        private val STAKATO_CHITIN = 3303
        private val REINFORCED_BOWSTRING = 3304
        private val MANASHEN_HORN = 3305
        private val BLOOD_OF_LIZARDMAN = 3306

        private val CRESCENT_MOON_BOW = 3028
        private val WOODEN_ARROW = 17

        // Rewards
        private val MARK_OF_SAGITTARIUS = 3293
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val BERNARD = 30702
        private val HAMIL = 30626
        private val SIR_ARON_TANFORD = 30653
        private val VOKIAN = 30514
        private val GAUEN = 30717

        // Monsters
        private val ANT = 20079
        private val ANT_CAPTAIN = 20080
        private val ANT_OVERSEER = 20081
        private val ANT_RECRUIT = 20082
        private val ANT_PATROL = 20084
        private val ANT_GUARD = 20086
        private val NOBLE_ANT = 20089
        private val NOBLE_ANT_LEADER = 20090
        private val BREKA_ORC_SHAMAN = 20269
        private val BREKA_ORC_OVERLORD = 20270
        private val MARSH_STAKATO_WORKER = 20230
        private val MARSH_STAKATO_SOLDIER = 20232
        private val MARSH_STAKATO_DRONE = 20234
        private val MARSH_SPIDER = 20233
        private val ROAD_SCAVENGER = 20551
        private val MANASHEN_GARGOYLE = 20563
        private val LETO_LIZARDMAN = 20577
        private val LETO_LIZARDMAN_ARCHER = 20578
        private val LETO_LIZARDMAN_SOLDIER = 20579
        private val LETO_LIZARDMAN_WARRIOR = 20580
        private val LETO_LIZARDMAN_SHAMAN = 20581
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val SERPENT_DEMON_KADESH = 27090
    }
}