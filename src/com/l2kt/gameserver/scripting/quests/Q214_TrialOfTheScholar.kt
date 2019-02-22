package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q214_TrialOfTheScholar : Quest(214, "Trial Of The Scholar") {
    init {

        setItemsIds(
            MIRIEN_SIGIL_1,
            MIRIEN_SIGIL_2,
            MIRIEN_SIGIL_3,
            MIRIEN_INSTRUCTION,
            MARIA_LETTER_1,
            MARIA_LETTER_2,
            LUCAS_LETTER,
            LUCILLA_HANDBAG,
            CRETA_LETTER_1,
            CRETA_PAINTING_1,
            CRETA_PAINTING_2,
            CRETA_PAINTING_3,
            BROWN_SCROLL_SCRAP,
            CRYSTAL_OF_PURITY_1,
            HIGH_PRIEST_SIGIL,
            GRAND_MAGISTER_SIGIL,
            CRONOS_SIGIL,
            SYLVAIN_LETTER,
            SYMBOL_OF_SYLVAIN,
            JUREK_LIST,
            MONSTER_EYE_DESTROYER_SKIN,
            SHAMAN_NECKLACE,
            SHACKLE_SCALP,
            SYMBOL_OF_JUREK,
            CRONOS_LETTER,
            DIETER_KEY,
            CRETA_LETTER_2,
            DIETER_LETTER,
            DIETER_DIARY,
            RAUT_LETTER_ENVELOPE,
            TRIFF_RING,
            SCRIPTURE_CHAPTER_1,
            SCRIPTURE_CHAPTER_2,
            SCRIPTURE_CHAPTER_3,
            SCRIPTURE_CHAPTER_4,
            VALKON_REQUEST,
            POITAN_NOTES,
            STRONG_LIQUOR,
            CRYSTAL_OF_PURITY_2,
            CASIAN_LIST,
            GHOUL_SKIN,
            MEDUSA_BLOOD,
            FETTERED_SOUL_ICHOR,
            ENCHANTED_GARGOYLE_NAIL,
            SYMBOL_OF_CRONOS
        )

        addStartNpc(MIRIEN)
        addTalkId(
            MIRIEN,
            SYLVAIN,
            LUCAS,
            VALKON,
            DIETER,
            JUREK,
            EDROC,
            RAUT,
            POITAN,
            MARIA,
            CRETA,
            CRONOS,
            TRIFF,
            CASIAN
        )

        addKillId(
            MONSTER_EYE_DESTROYER,
            MEDUSA,
            GHOUL,
            SHACKLE_1,
            SHACKLE_2,
            BREKA_ORC_SHAMAN,
            FETTERED_SOUL,
            GRANDIS,
            ENCHANTED_GARGOYLE,
            LETO_LIZARDMAN_WARRIOR
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // MIRIEN
        if (event.equals("30461-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(MIRIEN_SIGIL_1, 1)

            if (!player.memos.getBool("secondClassChange35", false)) {
                htmltext = "30461-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                player.memos.set("secondClassChange35", true)
            }
        } else if (event.equals("30461-09.htm", ignoreCase = true)) {
            if (player.level < 36) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(MIRIEN_INSTRUCTION, 1)
            } else {
                htmltext = "30461-10.htm"
                st["cond"] = "19"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(MIRIEN_SIGIL_2, 1)
                st.takeItems(SYMBOL_OF_JUREK, 1)
                st.giveItems(MIRIEN_SIGIL_3, 1)
            }
        } else if (event.equals("30070-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(HIGH_PRIEST_SIGIL, 1)
            st.giveItems(SYLVAIN_LETTER, 1)
        } else if (event.equals("30608-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SYLVAIN_LETTER, 1)
            st.giveItems(MARIA_LETTER_1, 1)
        } else if (event.equals("30608-08.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRETA_LETTER_1, 1)
            st.giveItems(LUCILLA_HANDBAG, 1)
        } else if (event.equals("30608-14.htm", ignoreCase = true)) {
            st["cond"] = "13"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BROWN_SCROLL_SCRAP, -1)
            st.takeItems(CRETA_PAINTING_3, 1)
            st.giveItems(CRYSTAL_OF_PURITY_1, 1)
        } else if (event.equals("30115-03.htm", ignoreCase = true)) {
            st["cond"] = "16"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GRAND_MAGISTER_SIGIL, 1)
            st.giveItems(JUREK_LIST, 1)
        } else if (event.equals("30071-04.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRETA_PAINTING_2, 1)
            st.giveItems(CRETA_PAINTING_3, 1)
        } else if (event.equals("30609-05.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MARIA_LETTER_2, 1)
            st.giveItems(CRETA_LETTER_1, 1)
        } else if (event.equals("30609-09.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LUCILLA_HANDBAG, 1)
            st.giveItems(CRETA_PAINTING_1, 1)
        } else if (event.equals("30609-14.htm", ignoreCase = true)) {
            st["cond"] = "22"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(DIETER_KEY, 1)
            st.giveItems(CRETA_LETTER_2, 1)
        } else if (event.equals("30610-10.htm", ignoreCase = true)) {
            st["cond"] = "20"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CRONOS_LETTER, 1)
            st.giveItems(CRONOS_SIGIL, 1)
        } else if (event.equals("30610-14.htm", ignoreCase = true)) {
            st["cond"] = "31"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRONOS_SIGIL, 1)
            st.takeItems(DIETER_DIARY, 1)
            st.takeItems(SCRIPTURE_CHAPTER_1, 1)
            st.takeItems(SCRIPTURE_CHAPTER_2, 1)
            st.takeItems(SCRIPTURE_CHAPTER_3, 1)
            st.takeItems(SCRIPTURE_CHAPTER_4, 1)
            st.takeItems(TRIFF_RING, 1)
            st.giveItems(SYMBOL_OF_CRONOS, 1)
        } else if (event.equals("30111-05.htm", ignoreCase = true)) {
            st["cond"] = "21"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRONOS_LETTER, 1)
            st.giveItems(DIETER_KEY, 1)
        } else if (event.equals("30111-09.htm", ignoreCase = true)) {
            st["cond"] = "23"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CRETA_LETTER_2, 1)
            st.giveItems(DIETER_DIARY, 1)
            st.giveItems(DIETER_LETTER, 1)
        } else if (event.equals("30230-02.htm", ignoreCase = true)) {
            st["cond"] = "24"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(DIETER_LETTER, 1)
            st.giveItems(RAUT_LETTER_ENVELOPE, 1)
        } else if (event.equals("30316-02.htm", ignoreCase = true)) {
            st["cond"] = "25"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(RAUT_LETTER_ENVELOPE, 1)
            st.giveItems(SCRIPTURE_CHAPTER_1, 1)
            st.giveItems(STRONG_LIQUOR, 1)
        } else if (event.equals("30611-04.htm", ignoreCase = true)) {
            st["cond"] = "26"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(STRONG_LIQUOR, 1)
            st.giveItems(TRIFF_RING, 1)
        } else if (event.equals("30103-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(VALKON_REQUEST, 1)
        } else if (event.equals("30612-04.htm", ignoreCase = true)) {
            st["cond"] = "28"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CASIAN_LIST, 1)
        } else if (event.equals("30612-07.htm", ignoreCase = true)) {
            st["cond"] = "30"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CASIAN_LIST, 1)
            st.takeItems(ENCHANTED_GARGOYLE_NAIL, -1)
            st.takeItems(FETTERED_SOUL_ICHOR, -1)
            st.takeItems(GHOUL_SKIN, -1)
            st.takeItems(MEDUSA_BLOOD, -1)
            st.takeItems(POITAN_NOTES, 1)
            st.giveItems(SCRIPTURE_CHAPTER_4, 1)
        }// CASIAN
        // VALKON
        // TRIFF
        // RAUT
        // EDROC
        // DIETER
        // CRONOS
        // CRETA
        // LUCAS
        // JUREK
        // MARIA
        // SYLVAIN

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.HUMAN_WIZARD && player.classId != ClassId.ELVEN_WIZARD && player.classId != ClassId.DARK_WIZARD)
                htmltext = "30461-01.htm"
            else if (player.level < 35)
                htmltext = "30461-02.htm"
            else
                htmltext = "30461-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MIRIEN -> if (cond < 14)
                        htmltext = "30461-05.htm"
                    else if (cond == 14) {
                        htmltext = "30461-06.htm"
                        st["cond"] = "15"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MIRIEN_SIGIL_1, 1)
                        st.takeItems(SYMBOL_OF_SYLVAIN, 1)
                        st.giveItems(MIRIEN_SIGIL_2, 1)
                    } else if (cond > 14 && cond < 18)
                        htmltext = "30461-07.htm"
                    else if (cond == 18) {
                        if (!st.hasQuestItems(MIRIEN_INSTRUCTION))
                            htmltext = "30461-08.htm"
                        else {
                            if (player.level < 36)
                                htmltext = "30461-11.htm"
                            else {
                                htmltext = "30461-12.htm"
                                st["cond"] = "19"
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(MIRIEN_INSTRUCTION, 1)
                                st.takeItems(MIRIEN_SIGIL_2, 1)
                                st.takeItems(SYMBOL_OF_JUREK, 1)
                                st.giveItems(MIRIEN_SIGIL_3, 1)
                            }
                        }
                    } else if (cond > 18 && cond < 31)
                        htmltext = "30461-13.htm"
                    else if (cond == 31) {
                        htmltext = "30461-14.htm"
                        st.takeItems(MIRIEN_SIGIL_3, 1)
                        st.takeItems(SYMBOL_OF_CRONOS, 1)
                        st.giveItems(MARK_OF_SCHOLAR, 1)
                        st.rewardExpAndSp(80265, 30000)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    SYLVAIN -> if (cond == 1)
                        htmltext = "30070-01.htm"
                    else if (cond < 13)
                        htmltext = "30070-03.htm"
                    else if (cond == 13) {
                        htmltext = "30070-04.htm"
                        st["cond"] = "14"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CRYSTAL_OF_PURITY_1, 1)
                        st.takeItems(HIGH_PRIEST_SIGIL, 1)
                        st.giveItems(SYMBOL_OF_SYLVAIN, 1)
                    } else if (cond == 14)
                        htmltext = "30070-05.htm"
                    else if (cond > 14)
                        htmltext = "30070-06.htm"

                    MARIA -> if (cond == 2)
                        htmltext = "30608-01.htm"
                    else if (cond == 3)
                        htmltext = "30608-03.htm"
                    else if (cond == 4) {
                        htmltext = "30608-04.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LUCAS_LETTER, 1)
                        st.giveItems(MARIA_LETTER_2, 1)
                    } else if (cond == 5)
                        htmltext = "30608-05.htm"
                    else if (cond == 6)
                        htmltext = "30608-06.htm"
                    else if (cond == 7)
                        htmltext = "30608-09.htm"
                    else if (cond == 8) {
                        htmltext = "30608-10.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CRETA_PAINTING_1, 1)
                        st.giveItems(CRETA_PAINTING_2, 1)
                    } else if (cond == 9)
                        htmltext = "30608-11.htm"
                    else if (cond == 10) {
                        htmltext = "30608-12.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 11)
                        htmltext = "30608-12.htm"
                    else if (cond == 12)
                        htmltext = "30608-13.htm"
                    else if (cond == 13)
                        htmltext = "30608-15.htm"
                    else if (st.hasAtLeastOneQuestItem(SYMBOL_OF_SYLVAIN, MIRIEN_SIGIL_2))
                        htmltext = "30608-16.htm"
                    else if (cond > 18) {
                        if (!st.hasQuestItems(VALKON_REQUEST))
                            htmltext = "30608-17.htm"
                        else {
                            htmltext = "30608-18.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(VALKON_REQUEST, 1)
                            st.giveItems(CRYSTAL_OF_PURITY_2, 1)
                        }
                    }

                    JUREK -> if (cond == 15)
                        htmltext = "30115-01.htm"
                    else if (cond == 16)
                        htmltext = "30115-04.htm"
                    else if (cond == 17) {
                        htmltext = "30115-05.htm"
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GRAND_MAGISTER_SIGIL, 1)
                        st.takeItems(JUREK_LIST, 1)
                        st.takeItems(MONSTER_EYE_DESTROYER_SKIN, -1)
                        st.takeItems(SHACKLE_SCALP, -1)
                        st.takeItems(SHAMAN_NECKLACE, -1)
                        st.giveItems(SYMBOL_OF_JUREK, 1)
                    } else if (cond == 18)
                        htmltext = "30115-06.htm"
                    else if (cond > 18)
                        htmltext = "30115-07.htm"

                    LUCAS -> if (cond == 3) {
                        htmltext = "30071-01.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MARIA_LETTER_1, 1)
                        st.giveItems(LUCAS_LETTER, 1)
                    } else if (cond > 3 && cond < 9)
                        htmltext = "30071-02.htm"
                    else if (cond == 9)
                        htmltext = "30071-03.htm"
                    else if (cond == 10 || cond == 11)
                        htmltext = "30071-05.htm"
                    else if (cond == 12)
                        htmltext = "30071-06.htm"
                    else if (cond > 12)
                        htmltext = "30071-07.htm"

                    CRETA -> if (cond == 5)
                        htmltext = "30609-01.htm"
                    else if (cond == 6)
                        htmltext = "30609-06.htm"
                    else if (cond == 7)
                        htmltext = "30609-07.htm"
                    else if (cond > 7 && cond < 13)
                        htmltext = "30609-10.htm"
                    else if (cond >= 13 && cond < 19)
                        htmltext = "30609-11.htm"
                    else if (cond == 21)
                        htmltext = "30609-12.htm"
                    else if (cond > 21)
                        htmltext = "30609-15.htm"

                    CRONOS -> if (cond == 19)
                        htmltext = "30610-01.htm"
                    else if (cond > 19 && cond < 30)
                        htmltext = "30610-11.htm"
                    else if (cond == 30)
                        htmltext = "30610-12.htm"
                    else if (cond == 31)
                        htmltext = "30610-15.htm"

                    DIETER -> if (cond == 20)
                        htmltext = "30111-01.htm"
                    else if (cond == 21)
                        htmltext = "30111-06.htm"
                    else if (cond == 22)
                        htmltext = "30111-07.htm"
                    else if (cond == 23)
                        htmltext = "30111-10.htm"
                    else if (cond == 24)
                        htmltext = "30111-11.htm"
                    else if (cond > 24 && cond < 31)
                        htmltext = if (!st.hasQuestItems(
                                SCRIPTURE_CHAPTER_1,
                                SCRIPTURE_CHAPTER_2,
                                SCRIPTURE_CHAPTER_3,
                                SCRIPTURE_CHAPTER_4
                            )
                        ) "30111-12.htm" else "30111-13.htm"
                    else if (cond == 31)
                        htmltext = "30111-15.htm"

                    EDROC -> if (cond == 23)
                        htmltext = "30230-01.htm"
                    else if (cond == 24)
                        htmltext = "30230-03.htm"
                    else if (cond > 24)
                        htmltext = "30230-04.htm"

                    RAUT -> if (cond == 24)
                        htmltext = "30316-01.htm"
                    else if (cond == 25)
                        htmltext = "30316-04.htm"
                    else if (cond > 25)
                        htmltext = "30316-05.htm"

                    TRIFF -> if (cond == 25)
                        htmltext = "30611-01.htm"
                    else if (cond > 25)
                        htmltext = "30611-05.htm"

                    VALKON -> if (st.hasQuestItems(TRIFF_RING)) {
                        if (!st.hasQuestItems(SCRIPTURE_CHAPTER_2)) {
                            if (!st.hasQuestItems(VALKON_REQUEST)) {
                                if (!st.hasQuestItems(CRYSTAL_OF_PURITY_2))
                                    htmltext = "30103-01.htm"
                                else {
                                    htmltext = "30103-06.htm"
                                    st.playSound(QuestState.SOUND_ITEMGET)
                                    st.takeItems(CRYSTAL_OF_PURITY_2, 1)
                                    st.giveItems(SCRIPTURE_CHAPTER_2, 1)
                                }
                            } else
                                htmltext = "30103-05.htm"
                        } else
                            htmltext = "30103-07.htm"
                    }

                    POITAN -> if (cond == 26 || cond == 27) {
                        if (!st.hasQuestItems(POITAN_NOTES)) {
                            htmltext = "30458-01.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.giveItems(POITAN_NOTES, 1)
                        } else
                            htmltext = "30458-02.htm"
                    } else if (cond == 28 || cond == 29)
                        htmltext = "30458-03.htm"
                    else if (cond == 30)
                        htmltext = "30458-04.htm"

                    CASIAN -> if ((cond == 26 || cond == 27) && st.hasQuestItems(POITAN_NOTES)) {
                        if (st.hasQuestItems(SCRIPTURE_CHAPTER_1, SCRIPTURE_CHAPTER_2, SCRIPTURE_CHAPTER_3))
                            htmltext = "30612-02.htm"
                        else {
                            htmltext = "30612-01.htm"
                            if (cond == 26) {
                                st["cond"] = "27"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            }
                        }
                    } else if (cond == 28)
                        htmltext = "30612-05.htm"
                    else if (cond == 29)
                        htmltext = "30612-06.htm"
                    else if (cond == 30)
                        htmltext = "30612-08.htm"
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
            LETO_LIZARDMAN_WARRIOR -> if (st.getInt("cond") == 11 && st.dropItems(BROWN_SCROLL_SCRAP, 1, 5, 500000))
                st["cond"] = "12"

            SHACKLE_1, SHACKLE_2 -> if (st.getInt("cond") == 16 && st.dropItems(SHACKLE_SCALP, 1, 2, 500000))
                if (st.getQuestItemsCount(MONSTER_EYE_DESTROYER_SKIN) == 5 && st.getQuestItemsCount(SHAMAN_NECKLACE) == 5)
                    st["cond"] = "17"

            MONSTER_EYE_DESTROYER -> if (st.getInt("cond") == 16 && st.dropItems(
                    MONSTER_EYE_DESTROYER_SKIN,
                    1,
                    5,
                    500000
                )
            )
                if (st.getQuestItemsCount(SHACKLE_SCALP) == 2 && st.getQuestItemsCount(SHAMAN_NECKLACE) == 5)
                    st["cond"] = "17"

            BREKA_ORC_SHAMAN -> if (st.getInt("cond") == 16 && st.dropItems(SHAMAN_NECKLACE, 1, 5, 500000))
                if (st.getQuestItemsCount(SHACKLE_SCALP) == 2 && st.getQuestItemsCount(MONSTER_EYE_DESTROYER_SKIN) == 5)
                    st["cond"] = "17"

            GRANDIS -> if (st.hasQuestItems(TRIFF_RING))
                st.dropItems(SCRIPTURE_CHAPTER_3, 1, 1, 300000)

            MEDUSA -> if (st.getInt("cond") == 28 && st.dropItemsAlways(MEDUSA_BLOOD, 1, 12))
                if (st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(FETTERED_SOUL_ICHOR) == 5 && st.getQuestItemsCount(
                        ENCHANTED_GARGOYLE_NAIL
                    ) == 5
                )
                    st["cond"] = "29"

            GHOUL -> if (st.getInt("cond") == 28 && st.dropItemsAlways(GHOUL_SKIN, 1, 10))
                if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(FETTERED_SOUL_ICHOR) == 5 && st.getQuestItemsCount(
                        ENCHANTED_GARGOYLE_NAIL
                    ) == 5
                )
                    st["cond"] = "29"

            FETTERED_SOUL -> if (st.getInt("cond") == 28 && st.dropItemsAlways(FETTERED_SOUL_ICHOR, 1, 5))
                if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(
                        ENCHANTED_GARGOYLE_NAIL
                    ) == 5
                )
                    st["cond"] = "29"

            ENCHANTED_GARGOYLE -> if (st.getInt("cond") == 28 && st.dropItemsAlways(ENCHANTED_GARGOYLE_NAIL, 1, 5))
                if (st.getQuestItemsCount(MEDUSA_BLOOD) == 12 && st.getQuestItemsCount(GHOUL_SKIN) == 10 && st.getQuestItemsCount(
                        FETTERED_SOUL_ICHOR
                    ) == 5
                )
                    st["cond"] = "29"
        }

        return null
    }

    companion object {
        private val qn = "Q214_TrialOfTheScholar"

        // Items
        private val MIRIEN_SIGIL_1 = 2675
        private val MIRIEN_SIGIL_2 = 2676
        private val MIRIEN_SIGIL_3 = 2677
        private val MIRIEN_INSTRUCTION = 2678
        private val MARIA_LETTER_1 = 2679
        private val MARIA_LETTER_2 = 2680
        private val LUCAS_LETTER = 2681
        private val LUCILLA_HANDBAG = 2682
        private val CRETA_LETTER_1 = 2683
        private val CRETA_PAINTING_1 = 2684
        private val CRETA_PAINTING_2 = 2685
        private val CRETA_PAINTING_3 = 2686
        private val BROWN_SCROLL_SCRAP = 2687
        private val CRYSTAL_OF_PURITY_1 = 2688
        private val HIGH_PRIEST_SIGIL = 2689
        private val GRAND_MAGISTER_SIGIL = 2690
        private val CRONOS_SIGIL = 2691
        private val SYLVAIN_LETTER = 2692
        private val SYMBOL_OF_SYLVAIN = 2693
        private val JUREK_LIST = 2694
        private val MONSTER_EYE_DESTROYER_SKIN = 2695
        private val SHAMAN_NECKLACE = 2696
        private val SHACKLE_SCALP = 2697
        private val SYMBOL_OF_JUREK = 2698
        private val CRONOS_LETTER = 2699
        private val DIETER_KEY = 2700
        private val CRETA_LETTER_2 = 2701
        private val DIETER_LETTER = 2702
        private val DIETER_DIARY = 2703
        private val RAUT_LETTER_ENVELOPE = 2704
        private val TRIFF_RING = 2705
        private val SCRIPTURE_CHAPTER_1 = 2706
        private val SCRIPTURE_CHAPTER_2 = 2707
        private val SCRIPTURE_CHAPTER_3 = 2708
        private val SCRIPTURE_CHAPTER_4 = 2709
        private val VALKON_REQUEST = 2710
        private val POITAN_NOTES = 2711
        private val STRONG_LIQUOR = 2713
        private val CRYSTAL_OF_PURITY_2 = 2714
        private val CASIAN_LIST = 2715
        private val GHOUL_SKIN = 2716
        private val MEDUSA_BLOOD = 2717
        private val FETTERED_SOUL_ICHOR = 2718
        private val ENCHANTED_GARGOYLE_NAIL = 2719
        private val SYMBOL_OF_CRONOS = 2720

        // Rewards
        private val MARK_OF_SCHOLAR = 2674
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val SYLVAIN = 30070
        private val LUCAS = 30071
        private val VALKON = 30103
        private val DIETER = 30111
        private val JUREK = 30115
        private val EDROC = 30230
        private val RAUT = 30316
        private val POITAN = 30458
        private val MIRIEN = 30461
        private val MARIA = 30608
        private val CRETA = 30609
        private val CRONOS = 30610
        private val TRIFF = 30611
        private val CASIAN = 30612

        // Monsters
        private val MONSTER_EYE_DESTROYER = 20068
        private val MEDUSA = 20158
        private val GHOUL = 20201
        private val SHACKLE_1 = 20235
        private val SHACKLE_2 = 20279
        private val BREKA_ORC_SHAMAN = 20269
        private val FETTERED_SOUL = 20552
        private val GRANDIS = 20554
        private val ENCHANTED_GARGOYLE = 20567
        private val LETO_LIZARDMAN_WARRIOR = 20580
    }
}