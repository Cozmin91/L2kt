package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q219_TestimonyOfFate : Quest(219, "Testimony of Fate") {
    init {
        CHANCES[DEAD_SEEKER] = 500000
        CHANCES[TYRANT] = 500000
        CHANCES[TYRANT_KINGPIN] = 600000
        CHANCES[MEDUSA] = 500000
        CHANCES[MARSH_STAKATO] = 400000
        CHANCES[MARSH_STAKATO_WORKER] = 300000
        CHANCES[MARSH_STAKATO_SOLDIER] = 500000
        CHANCES[MARSH_STAKATO_DRONE] = 600000
        CHANCES[MARSH_SPIDER] = 500000
    }

    init {

        setItemsIds(
            KAIRA_LETTER,
            METHEUS_FUNERAL_JAR,
            KASANDRA_REMAINS,
            HERBALISM_TEXTBOOK,
            IXIA_LIST,
            MEDUSA_ICHOR,
            MARSH_SPIDER_FLUIDS,
            DEAD_SEEKER_DUNG,
            TYRANT_BLOOD,
            NIGHTSHADE_ROOT,
            BELLADONNA,
            ALDER_SKULL_1,
            ALDER_SKULL_2,
            ALDER_RECEIPT,
            REVELATIONS_MANUSCRIPT,
            KAIRA_RECOMMENDATION,
            KAIRA_INSTRUCTIONS,
            PALUS_CHARM,
            THIFIELL_LETTER,
            ARKENIA_NOTE,
            PIXY_GARNET,
            GRANDIS_SKULL,
            KARUL_BUGBEAR_SKULL,
            BREKA_OVERLORD_SKULL,
            LETO_OVERLORD_SKULL,
            RED_FAIRY_DUST,
            BLIGHT_TREANT_SEED,
            BLACK_WILLOW_LEAF,
            BLIGHT_TREANT_SAP,
            ARKENIA_LETTER
        )

        addStartNpc(KAIRA)
        addTalkId(KAIRA, METHEUS, IXIA, ALDER_SPIRIT, ROA, NORMAN, THIFIELL, ARKENIA, BLOODY_PIXY, BLIGHT_TREANT)

        addKillId(
            HANGMAN_TREE,
            MARSH_STAKATO,
            MEDUSA,
            TYRANT,
            TYRANT_KINGPIN,
            DEAD_SEEKER,
            MARSH_STAKATO_WORKER,
            MARSH_STAKATO_SOLDIER,
            MARSH_SPIDER,
            MARSH_STAKATO_DRONE,
            BREKA_ORC_OVERLORD,
            GRANDIS,
            LETO_LIZARDMAN_OVERLORD,
            KARUL_BUGBEAR,
            BLACK_WILLOW_LURKER
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30476-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(KAIRA_LETTER, 1)

            if (!player.memos.getBool("secondClassChange37", false)) {
                htmltext = "30476-05a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37[player.race.ordinal] ?: 0)
                player.memos.set("secondClassChange37", true)
            }
        } else if (event.equals("30114-04.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ALDER_SKULL_2, 1)
            st.giveItems(ALDER_RECEIPT, 1)
        } else if (event.equals("30476-12.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)

            if (player.level < 38) {
                htmltext = "30476-13.htm"
                st["cond"] = "14"
                st.giveItems(KAIRA_INSTRUCTIONS, 1)
            } else {
                st["cond"] = "15"
                st.takeItems(REVELATIONS_MANUSCRIPT, 1)
                st.giveItems(KAIRA_RECOMMENDATION, 1)
            }
        } else if (event.equals("30419-02.htm", ignoreCase = true)) {
            st["cond"] = "17"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(THIFIELL_LETTER, 1)
            st.giveItems(ARKENIA_NOTE, 1)
        } else if (event.equals("31845-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(PIXY_GARNET, 1)
        } else if (event.equals("31850-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(BLIGHT_TREANT_SEED, 1)
        } else if (event.equals("30419-05.htm", ignoreCase = true)) {
            st["cond"] = "18"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARKENIA_NOTE, 1)
            st.takeItems(BLIGHT_TREANT_SAP, 1)
            st.takeItems(RED_FAIRY_DUST, 1)
            st.giveItems(ARKENIA_LETTER, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30476-02.htm"
            else if (player.level < 37 || player.classId.level() != 1)
                htmltext = "30476-01.htm"
            else
                htmltext = "30476-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    KAIRA -> if (cond == 1)
                        htmltext = "30476-06.htm"
                    else if (cond == 2 || cond == 3)
                        htmltext = "30476-07.htm"
                    else if (cond > 3 && cond < 9)
                        htmltext = "30476-08.htm"
                    else if (cond == 9) {
                        htmltext = "30476-09.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALDER_SKULL_1, 1)
                        addSpawn(ALDER_SPIRIT, player, false, 0, false)
                    } else if (cond > 9 && cond < 13)
                        htmltext = "30476-10.htm"
                    else if (cond == 13)
                        htmltext = "30476-11.htm"
                    else if (cond == 14) {
                        if (player.level < 38)
                            htmltext = "30476-14.htm"
                        else {
                            htmltext = "30476-12.htm"
                            st["cond"] = "15"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(KAIRA_INSTRUCTIONS, 1)
                            st.takeItems(REVELATIONS_MANUSCRIPT, 1)
                            st.giveItems(KAIRA_RECOMMENDATION, 1)
                        }
                    } else if (cond == 15)
                        htmltext = "30476-16.htm"
                    else if (cond > 15)
                        htmltext = "30476-17.htm"

                    METHEUS -> if (cond == 1) {
                        htmltext = "30614-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KAIRA_LETTER, 1)
                        st.giveItems(METHEUS_FUNERAL_JAR, 1)
                    } else if (cond == 2)
                        htmltext = "30614-02.htm"
                    else if (cond == 3) {
                        htmltext = "30614-03.htm"
                        st["cond"] = "4"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KASANDRA_REMAINS, 1)
                        st.giveItems(HERBALISM_TEXTBOOK, 1)
                    } else if (cond > 3 && cond < 8)
                        htmltext = "30614-04.htm"
                    else if (cond == 8) {
                        htmltext = "30614-05.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BELLADONNA, 1)
                        st.giveItems(ALDER_SKULL_1, 1)
                    } else if (cond > 8)
                        htmltext = "30614-06.htm"

                    IXIA -> if (cond == 5) {
                        htmltext = "30463-01.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HERBALISM_TEXTBOOK, 1)
                        st.giveItems(IXIA_LIST, 1)
                    } else if (cond == 6)
                        htmltext = "30463-02.htm"
                    else if (cond == 7) {
                        htmltext = "30463-03.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(IXIA_LIST, 1)
                        st.takeItems(DEAD_SEEKER_DUNG, -1)
                        st.takeItems(MARSH_SPIDER_FLUIDS, -1)
                        st.takeItems(MEDUSA_ICHOR, -1)
                        st.takeItems(NIGHTSHADE_ROOT, -1)
                        st.takeItems(TYRANT_BLOOD, -1)
                        st.giveItems(BELLADONNA, 1)
                    } else if (cond == 8)
                        htmltext = "30463-04.htm"
                    else if (cond > 8)
                        htmltext = "30463-05.htm"

                    ALDER_SPIRIT -> if (cond == 10) {
                        htmltext = "30613-01.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(ALDER_SKULL_2, 1)
                        npc.deleteMe()
                    }

                    ROA -> if (cond == 11)
                        htmltext = "30114-01.htm"
                    else if (cond == 12)
                        htmltext = "30114-05.htm"
                    else if (cond > 12)
                        htmltext = "30114-06.htm"

                    NORMAN -> if (cond == 12) {
                        htmltext = "30210-01.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALDER_RECEIPT, 1)
                        st.giveItems(REVELATIONS_MANUSCRIPT, 1)
                    } else if (cond > 12)
                        htmltext = "30210-02.htm"

                    THIFIELL -> if (cond == 15) {
                        htmltext = "30358-01.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KAIRA_RECOMMENDATION, 1)
                        st.giveItems(PALUS_CHARM, 1)
                        st.giveItems(THIFIELL_LETTER, 1)
                    } else if (cond == 16)
                        htmltext = "30358-02.htm"
                    else if (cond == 17)
                        htmltext = "30358-03.htm"
                    else if (cond == 18) {
                        htmltext = "30358-04.htm"
                        st.takeItems(PALUS_CHARM, 1)
                        st.takeItems(ARKENIA_LETTER, 1)
                        st.giveItems(MARK_OF_FATE, 1)
                        st.rewardExpAndSp(68183, 1750)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ARKENIA -> if (cond == 16)
                        htmltext = "30419-01.htm"
                    else if (cond == 17)
                        htmltext =
                                if (st.hasQuestItems(BLIGHT_TREANT_SAP) && st.hasQuestItems(RED_FAIRY_DUST)) "30419-04.htm" else "30419-03.htm"
                    else if (cond == 18)
                        htmltext = "30419-06.htm"

                    BLOODY_PIXY -> if (cond == 17) {
                        if (st.hasQuestItems(PIXY_GARNET)) {
                            if (st.getQuestItemsCount(GRANDIS_SKULL) >= 10 && st.getQuestItemsCount(KARUL_BUGBEAR_SKULL) >= 10 && st.getQuestItemsCount(
                                    BREKA_OVERLORD_SKULL
                                ) >= 10 && st.getQuestItemsCount(LETO_OVERLORD_SKULL) >= 10
                            ) {
                                htmltext = "31845-04.htm"
                                st.playSound(QuestState.SOUND_ITEMGET)
                                st.takeItems(BREKA_OVERLORD_SKULL, -1)
                                st.takeItems(GRANDIS_SKULL, -1)
                                st.takeItems(KARUL_BUGBEAR_SKULL, -1)
                                st.takeItems(LETO_OVERLORD_SKULL, -1)
                                st.takeItems(PIXY_GARNET, 1)
                                st.giveItems(RED_FAIRY_DUST, 1)
                            } else
                                htmltext = "31845-03.htm"
                        } else if (st.hasQuestItems(RED_FAIRY_DUST))
                            htmltext = "31845-05.htm"
                        else
                            htmltext = "31845-01.htm"
                    } else if (cond == 18)
                        htmltext = "31845-05.htm"

                    BLIGHT_TREANT -> if (cond == 17) {
                        if (st.hasQuestItems(BLIGHT_TREANT_SEED)) {
                            if (st.hasQuestItems(BLACK_WILLOW_LEAF)) {
                                htmltext = "31850-04.htm"
                                st.playSound(QuestState.SOUND_ITEMGET)
                                st.takeItems(BLACK_WILLOW_LEAF, 1)
                                st.takeItems(BLIGHT_TREANT_SEED, 1)
                                st.giveItems(BLIGHT_TREANT_SAP, 1)
                            } else
                                htmltext = "31850-03.htm"
                        } else if (st.hasQuestItems(BLIGHT_TREANT_SAP))
                            htmltext = "31850-05.htm"
                        else
                            htmltext = "31850-01.htm"
                    } else if (cond == 18)
                        htmltext = "31850-05.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        when (npcId) {
            HANGMAN_TREE -> if (st.getInt("cond") == 2) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(METHEUS_FUNERAL_JAR, 1)
                st.giveItems(KASANDRA_REMAINS, 1)
            }

            DEAD_SEEKER -> if (st.getInt("cond") == 6 && st.dropItems(DEAD_SEEKER_DUNG, 1, 10, CHANCES[npcId]!!))
                if (st.getQuestItemsCount(TYRANT_BLOOD) >= 10 && st.getQuestItemsCount(MEDUSA_ICHOR) >= 10 && st.getQuestItemsCount(
                        NIGHTSHADE_ROOT
                    ) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10
                )
                    st["cond"] = "7"

            TYRANT, TYRANT_KINGPIN -> if (st.getInt("cond") == 6 && st.dropItems(TYRANT_BLOOD, 1, 10, CHANCES[npcId]!!))
                if (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10 && st.getQuestItemsCount(MEDUSA_ICHOR) >= 10 && st.getQuestItemsCount(
                        NIGHTSHADE_ROOT
                    ) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10
                )
                    st["cond"] = "7"

            MEDUSA -> if (st.getInt("cond") == 6 && st.dropItems(MEDUSA_ICHOR, 1, 10, CHANCES[npcId]!!))
                if (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10 && st.getQuestItemsCount(TYRANT_BLOOD) >= 10 && st.getQuestItemsCount(
                        NIGHTSHADE_ROOT
                    ) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10
                )
                    st["cond"] = "7"

            MARSH_STAKATO, MARSH_STAKATO_WORKER, MARSH_STAKATO_SOLDIER, MARSH_STAKATO_DRONE -> if (st.getInt("cond") == 6 && st.dropItems(
                    NIGHTSHADE_ROOT,
                    1,
                    10,
                    CHANCES[npcId] ?: 0
                )
            )
                if (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10 && st.getQuestItemsCount(TYRANT_BLOOD) >= 10 && st.getQuestItemsCount(
                        MEDUSA_ICHOR
                    ) >= 10 && st.getQuestItemsCount(MARSH_SPIDER_FLUIDS) >= 10
                )
                    st["cond"] = "7"

            MARSH_SPIDER -> if (st.getInt("cond") == 6 && st.dropItems(MARSH_SPIDER_FLUIDS, 1, 10, CHANCES[npcId]!!))
                if (st.getQuestItemsCount(DEAD_SEEKER_DUNG) >= 10 && st.getQuestItemsCount(TYRANT_BLOOD) >= 10 && st.getQuestItemsCount(
                        MEDUSA_ICHOR
                    ) >= 10 && st.getQuestItemsCount(NIGHTSHADE_ROOT) >= 10
                )
                    st["cond"] = "7"

            GRANDIS -> if (st.hasQuestItems(PIXY_GARNET))
                st.dropItemsAlways(GRANDIS_SKULL, 1, 10)

            LETO_LIZARDMAN_OVERLORD -> if (st.hasQuestItems(PIXY_GARNET))
                st.dropItemsAlways(LETO_OVERLORD_SKULL, 1, 10)

            BREKA_ORC_OVERLORD -> if (st.hasQuestItems(PIXY_GARNET))
                st.dropItemsAlways(BREKA_OVERLORD_SKULL, 1, 10)

            KARUL_BUGBEAR -> if (st.hasQuestItems(PIXY_GARNET))
                st.dropItemsAlways(KARUL_BUGBEAR_SKULL, 1, 10)

            BLACK_WILLOW_LURKER -> if (st.hasQuestItems(BLIGHT_TREANT_SEED))
                st.dropItemsAlways(BLACK_WILLOW_LEAF, 1, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q219_TestimonyOfFate"

        // NPCs
        private val KAIRA = 30476
        private val METHEUS = 30614
        private val IXIA = 30463
        private val ALDER_SPIRIT = 30613
        private val ROA = 30114
        private val NORMAN = 30210
        private val THIFIELL = 30358
        private val ARKENIA = 30419
        private val BLOODY_PIXY = 31845
        private val BLIGHT_TREANT = 31850

        // Items
        private val KAIRA_LETTER = 3173
        private val METHEUS_FUNERAL_JAR = 3174
        private val KASANDRA_REMAINS = 3175
        private val HERBALISM_TEXTBOOK = 3176
        private val IXIA_LIST = 3177
        private val MEDUSA_ICHOR = 3178
        private val MARSH_SPIDER_FLUIDS = 3179
        private val DEAD_SEEKER_DUNG = 3180
        private val TYRANT_BLOOD = 3181
        private val NIGHTSHADE_ROOT = 3182
        private val BELLADONNA = 3183
        private val ALDER_SKULL_1 = 3184
        private val ALDER_SKULL_2 = 3185
        private val ALDER_RECEIPT = 3186
        private val REVELATIONS_MANUSCRIPT = 3187
        private val KAIRA_RECOMMENDATION = 3189
        private val KAIRA_INSTRUCTIONS = 3188
        private val PALUS_CHARM = 3190
        private val THIFIELL_LETTER = 3191
        private val ARKENIA_NOTE = 3192
        private val PIXY_GARNET = 3193
        private val GRANDIS_SKULL = 3194
        private val KARUL_BUGBEAR_SKULL = 3195
        private val BREKA_OVERLORD_SKULL = 3196
        private val LETO_OVERLORD_SKULL = 3197
        private val RED_FAIRY_DUST = 3198
        private val BLIGHT_TREANT_SEED = 3199
        private val BLACK_WILLOW_LEAF = 3200
        private val BLIGHT_TREANT_SAP = 3201
        private val ARKENIA_LETTER = 3202

        // Rewards
        private val MARK_OF_FATE = 3172
        private val DIMENSIONAL_DIAMOND = 7562

        // Monsters
        private val HANGMAN_TREE = 20144
        private val MARSH_STAKATO = 20157
        private val MEDUSA = 20158
        private val TYRANT = 20192
        private val TYRANT_KINGPIN = 20193
        private val DEAD_SEEKER = 20202
        private val MARSH_STAKATO_WORKER = 20230
        private val MARSH_STAKATO_SOLDIER = 20232
        private val MARSH_SPIDER = 20233
        private val MARSH_STAKATO_DRONE = 20234
        private val BREKA_ORC_OVERLORD = 20270
        private val GRANDIS = 20554
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val KARUL_BUGBEAR = 20600
        private val BLACK_WILLOW_LURKER = 27079

        // Cond 6 drop chances
        private val CHANCES = HashMap<Int, Int>()
    }
}