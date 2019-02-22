package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q217_TestimonyOfTrust : Quest(217, "Testimony of Trust") {
    init {

        setItemsIds(
            LETTER_TO_ELF,
            LETTER_TO_DARK_ELF,
            LETTER_TO_DWARF,
            LETTER_TO_ORC,
            LETTER_TO_SERESIN,
            SCROLL_OF_DARK_ELF_TRUST,
            SCROLL_OF_ELF_TRUST,
            SCROLL_OF_DWARF_TRUST,
            SCROLL_OF_ORC_TRUST,
            RECOMMENDATION_OF_HOLLINT,
            ORDER_OF_ASTERIOS,
            BREATH_OF_WINDS,
            SEED_OF_VERDURE,
            LETTER_FROM_THIFIELL,
            BLOOD_GUARDIAN_BASILIK,
            GIANT_APHID,
            STAKATO_FLUIDS,
            BASILIK_PLASMA,
            HONEY_DEW,
            STAKATO_ICHOR,
            ORDER_OF_CLAYTON,
            PARASITE_OF_LOTA,
            LETTER_TO_MANAKIA,
            LETTER_OF_MANAKIA,
            LETTER_TO_NIKOLA,
            ORDER_OF_NIKOLA,
            HEARTSTONE_OF_PORTA
        )

        addStartNpc(HOLLINT)
        addTalkId(HOLLINT, ASTERIOS, THIFIELL, CLAYTON, SERESIN, KAKAI, MANAKIA, LOCKIRIN, NIKOLA, BIOTIN)

        addKillId(
            DRYAD,
            DRYAD_ELDER,
            LIREIN,
            LIREIN_ELDER,
            ACTEA_OF_VERDANT_WILDS,
            LUELL_OF_ZEPHYR_WINDS,
            GUARDIAN_BASILIK,
            ANT_RECRUIT,
            ANT_PATROL,
            ANT_GUARD,
            ANT_SOLDIER,
            ANT_WARRIOR_CAPTAIN,
            MARSH_STAKATO,
            MARSH_STAKATO_WORKER,
            MARSH_STAKATO_SOLDIER,
            MARSH_STAKATO_DRONE,
            WINDSUS,
            PORTA
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event

        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30191-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LETTER_TO_ELF, 1)
            st.giveItems(LETTER_TO_DARK_ELF, 1)

            if (!player.memos.getBool("secondClassChange37", false)) {
                htmltext = "30191-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37[player.race.ordinal] ?: 0)
                player.memos.set("secondClassChange37", true)
            }
        } else if (event.equals("30154-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_ELF, 1)
            st.giveItems(ORDER_OF_ASTERIOS, 1)
        } else if (event.equals("30358-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_DARK_ELF, 1)
            st.giveItems(LETTER_FROM_THIFIELL, 1)
        } else if (event.equals("30515-02.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_MANAKIA, 1)
        } else if (event.equals("30531-02.htm", ignoreCase = true)) {
            st["cond"] = "18"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_DWARF, 1)
            st.giveItems(LETTER_TO_NIKOLA, 1)
        } else if (event.equals("30565-02.htm", ignoreCase = true)) {
            st["cond"] = "13"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_ORC, 1)
            st.giveItems(LETTER_TO_MANAKIA, 1)
        } else if (event.equals("30621-02.htm", ignoreCase = true)) {
            st["cond"] = "19"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_TO_NIKOLA, 1)
            st.giveItems(ORDER_OF_NIKOLA, 1)
        } else if (event.equals("30657-03.htm", ignoreCase = true)) {
            if (player.level < 38) {
                htmltext = "30657-02.htm"
                if (st.getInt("cond") == 10) {
                    st["cond"] = "11"
                    st.playSound(QuestState.SOUND_MIDDLE)
                }
            } else {
                st["cond"] = "12"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(LETTER_TO_SERESIN, 1)
                st.giveItems(LETTER_TO_DWARF, 1)
                st.giveItems(LETTER_TO_ORC, 1)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId.level() != 1)
                htmltext = "30191-01a.htm"
            else if (player.race != ClassRace.HUMAN)
                htmltext = "30191-02.htm"
            else if (player.level < 37)
                htmltext = "30191-01.htm"
            else
                htmltext = "30191-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HOLLINT -> if (cond < 9)
                        htmltext = "30191-08.htm"
                    else if (cond == 9) {
                        htmltext = "30191-05.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SCROLL_OF_DARK_ELF_TRUST, 1)
                        st.takeItems(SCROLL_OF_ELF_TRUST, 1)
                        st.giveItems(LETTER_TO_SERESIN, 1)
                    } else if (cond > 9 && cond < 22)
                        htmltext = "30191-09.htm"
                    else if (cond == 22) {
                        htmltext = "30191-06.htm"
                        st["cond"] = "23"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SCROLL_OF_DWARF_TRUST, 1)
                        st.takeItems(SCROLL_OF_ORC_TRUST, 1)
                        st.giveItems(RECOMMENDATION_OF_HOLLINT, 1)
                    } else if (cond == 23)
                        htmltext = "30191-07.htm"

                    ASTERIOS -> if (cond == 1)
                        htmltext = "30154-01.htm"
                    else if (cond == 2)
                        htmltext = "30154-04.htm"
                    else if (cond == 3) {
                        htmltext = "30154-05.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BREATH_OF_WINDS, 1)
                        st.takeItems(SEED_OF_VERDURE, 1)
                        st.takeItems(ORDER_OF_ASTERIOS, 1)
                        st.giveItems(SCROLL_OF_ELF_TRUST, 1)
                    } else if (cond > 3)
                        htmltext = "30154-06.htm"

                    THIFIELL -> if (cond == 4)
                        htmltext = "30358-01.htm"
                    else if (cond > 4 && cond < 8)
                        htmltext = "30358-05.htm"
                    else if (cond == 8) {
                        htmltext = "30358-03.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BASILIK_PLASMA, 1)
                        st.takeItems(HONEY_DEW, 1)
                        st.takeItems(STAKATO_ICHOR, 1)
                        st.giveItems(SCROLL_OF_DARK_ELF_TRUST, 1)
                    } else if (cond > 8)
                        htmltext = "30358-04.htm"

                    CLAYTON -> if (cond == 5) {
                        htmltext = "30464-01.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LETTER_FROM_THIFIELL, 1)
                        st.giveItems(ORDER_OF_CLAYTON, 1)
                    } else if (cond == 6)
                        htmltext = "30464-02.htm"
                    else if (cond > 6) {
                        htmltext = "30464-03.htm"
                        if (cond == 7) {
                            st["cond"] = "8"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(ORDER_OF_CLAYTON, 1)
                        }
                    }

                    SERESIN -> if (cond == 10 || cond == 11)
                        htmltext = "30657-01.htm"
                    else if (cond > 11 && cond < 22)
                        htmltext = "30657-04.htm"
                    else if (cond == 22)
                        htmltext = "30657-05.htm"

                    KAKAI -> if (cond == 12)
                        htmltext = "30565-01.htm"
                    else if (cond > 12 && cond < 16)
                        htmltext = "30565-03.htm"
                    else if (cond == 16) {
                        htmltext = "30565-04.htm"
                        st["cond"] = "17"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LETTER_OF_MANAKIA, 1)
                        st.giveItems(SCROLL_OF_ORC_TRUST, 1)
                    } else if (cond > 16)
                        htmltext = "30565-05.htm"

                    MANAKIA -> if (cond == 13)
                        htmltext = "30515-01.htm"
                    else if (cond == 14)
                        htmltext = "30515-03.htm"
                    else if (cond == 15) {
                        htmltext = "30515-04.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(PARASITE_OF_LOTA, -1)
                        st.giveItems(LETTER_OF_MANAKIA, 1)
                    } else if (cond > 15)
                        htmltext = "30515-05.htm"

                    LOCKIRIN -> if (cond == 17)
                        htmltext = "30531-01.htm"
                    else if (cond > 17 && cond < 21)
                        htmltext = "30531-03.htm"
                    else if (cond == 21) {
                        htmltext = "30531-04.htm"
                        st["cond"] = "22"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(SCROLL_OF_DWARF_TRUST, 1)
                    } else if (cond == 22)
                        htmltext = "30531-05.htm"

                    NIKOLA -> if (cond == 18)
                        htmltext = "30621-01.htm"
                    else if (cond == 19)
                        htmltext = "30621-03.htm"
                    else if (cond == 20) {
                        htmltext = "30621-04.htm"
                        st["cond"] = "21"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HEARTSTONE_OF_PORTA, -1)
                        st.takeItems(ORDER_OF_NIKOLA, 1)
                    } else if (cond > 20)
                        htmltext = "30621-05.htm"

                    BIOTIN -> if (cond == 23) {
                        htmltext = "30031-01.htm"
                        st.takeItems(RECOMMENDATION_OF_HOLLINT, 1)
                        st.giveItems(MARK_OF_TRUST, 1)
                        st.rewardExpAndSp(39571, 2500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId
        when (npcId) {
            DRYAD, DRYAD_ELDER -> if (st.getInt("cond") == 2 && !st.hasQuestItems(SEED_OF_VERDURE) && Rnd[100] < 33) {
                addSpawn(ACTEA_OF_VERDANT_WILDS, npc, true, 200000, true)
                st.playSound(QuestState.SOUND_BEFORE_BATTLE)
            }

            LIREIN, LIREIN_ELDER -> if (st.getInt("cond") == 2 && !st.hasQuestItems(BREATH_OF_WINDS) && Rnd[100] < 33) {
                addSpawn(LUELL_OF_ZEPHYR_WINDS, npc, true, 200000, true)
                st.playSound(QuestState.SOUND_BEFORE_BATTLE)
            }

            ACTEA_OF_VERDANT_WILDS -> if (st.getInt("cond") == 2 && !st.hasQuestItems(SEED_OF_VERDURE)) {
                st.giveItems(SEED_OF_VERDURE, 1)
                if (st.hasQuestItems(BREATH_OF_WINDS)) {
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            LUELL_OF_ZEPHYR_WINDS -> if (st.getInt("cond") == 2 && !st.hasQuestItems(BREATH_OF_WINDS)) {
                st.giveItems(BREATH_OF_WINDS, 1)
                if (st.hasQuestItems(SEED_OF_VERDURE)) {
                    st["cond"] = "3"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            MARSH_STAKATO, MARSH_STAKATO_WORKER, MARSH_STAKATO_SOLDIER, MARSH_STAKATO_DRONE -> if (st.getInt("cond") == 6 && !st.hasQuestItems(
                    STAKATO_ICHOR
                ) && st.dropItemsAlways(STAKATO_FLUIDS, 1, 10)
            ) {
                st.takeItems(STAKATO_FLUIDS, -1)
                st.giveItems(STAKATO_ICHOR, 1)

                if (st.hasQuestItems(BASILIK_PLASMA, HONEY_DEW))
                    st["cond"] = "7"
            }

            ANT_RECRUIT, ANT_PATROL, ANT_GUARD, ANT_SOLDIER, ANT_WARRIOR_CAPTAIN -> if (st.getInt("cond") == 6 && !st.hasQuestItems(
                    HONEY_DEW
                ) && st.dropItemsAlways(GIANT_APHID, 1, 10)
            ) {
                st.takeItems(GIANT_APHID, -1)
                st.giveItems(HONEY_DEW, 1)

                if (st.hasQuestItems(BASILIK_PLASMA, STAKATO_ICHOR))
                    st["cond"] = "7"
            }

            GUARDIAN_BASILIK -> if (st.getInt("cond") == 6 && !st.hasQuestItems(BASILIK_PLASMA) && st.dropItemsAlways(
                    BLOOD_GUARDIAN_BASILIK,
                    1,
                    10
                )
            ) {
                st.takeItems(BLOOD_GUARDIAN_BASILIK, -1)
                st.giveItems(BASILIK_PLASMA, 1)

                if (st.hasQuestItems(HONEY_DEW, STAKATO_ICHOR))
                    st["cond"] = "7"
            }

            WINDSUS -> if (st.getInt("cond") == 14 && st.dropItems(PARASITE_OF_LOTA, 1, 10, 500000))
                st["cond"] = "15"

            PORTA -> if (st.getInt("cond") == 19 && st.dropItemsAlways(HEARTSTONE_OF_PORTA, 1, 10))
                st["cond"] = "20"
        }

        return null
    }

    companion object {
        private val qn = "Q217_TestimonyOfTrust"

        // Items
        private val LETTER_TO_ELF = 2735
        private val LETTER_TO_DARK_ELF = 2736
        private val LETTER_TO_DWARF = 2737
        private val LETTER_TO_ORC = 2738
        private val LETTER_TO_SERESIN = 2739
        private val SCROLL_OF_DARK_ELF_TRUST = 2740
        private val SCROLL_OF_ELF_TRUST = 2741
        private val SCROLL_OF_DWARF_TRUST = 2742
        private val SCROLL_OF_ORC_TRUST = 2743
        private val RECOMMENDATION_OF_HOLLINT = 2744
        private val ORDER_OF_ASTERIOS = 2745
        private val BREATH_OF_WINDS = 2746
        private val SEED_OF_VERDURE = 2747
        private val LETTER_FROM_THIFIELL = 2748
        private val BLOOD_GUARDIAN_BASILIK = 2749
        private val GIANT_APHID = 2750
        private val STAKATO_FLUIDS = 2751
        private val BASILIK_PLASMA = 2752
        private val HONEY_DEW = 2753
        private val STAKATO_ICHOR = 2754
        private val ORDER_OF_CLAYTON = 2755
        private val PARASITE_OF_LOTA = 2756
        private val LETTER_TO_MANAKIA = 2757
        private val LETTER_OF_MANAKIA = 2758
        private val LETTER_TO_NIKOLA = 2759
        private val ORDER_OF_NIKOLA = 2760
        private val HEARTSTONE_OF_PORTA = 2761

        // Rewards
        private val MARK_OF_TRUST = 2734
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val HOLLINT = 30191
        private val ASTERIOS = 30154
        private val THIFIELL = 30358
        private val CLAYTON = 30464
        private val SERESIN = 30657
        private val KAKAI = 30565
        private val MANAKIA = 30515
        private val LOCKIRIN = 30531
        private val NIKOLA = 30621
        private val BIOTIN = 30031

        // Monsters
        private val DRYAD = 20013
        private val DRYAD_ELDER = 20019
        private val LIREIN = 20036
        private val LIREIN_ELDER = 20044
        private val ACTEA_OF_VERDANT_WILDS = 27121
        private val LUELL_OF_ZEPHYR_WINDS = 27120
        private val GUARDIAN_BASILIK = 20550
        private val ANT_RECRUIT = 20082
        private val ANT_PATROL = 20084
        private val ANT_GUARD = 20086
        private val ANT_SOLDIER = 20087
        private val ANT_WARRIOR_CAPTAIN = 20088
        private val MARSH_STAKATO = 20157
        private val MARSH_STAKATO_WORKER = 20230
        private val MARSH_STAKATO_SOLDIER = 20232
        private val MARSH_STAKATO_DRONE = 20234
        private val WINDSUS = 20553
        private val PORTA = 20213
    }
}