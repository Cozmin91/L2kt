package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q218_TestimonyOfLife : Quest(218, "Testimony of Life") {
    init {

        setItemsIds(
            TALINS_SPEAR,
            CARDIEN_LETTER,
            CAMOMILE_CHARM,
            HIERARCH_LETTER,
            MOONFLOWER_CHARM,
            GRAIL_DIAGRAM,
            THALIA_LETTER_1,
            THALIA_LETTER_2,
            THALIA_INSTRUCTIONS,
            PUSHKIN_LIST,
            PURE_MITHRIL_CUP,
            ARKENIA_CONTRACT,
            ARKENIA_INSTRUCTIONS,
            ADONIUS_LIST,
            ANDARIEL_SCRIPTURE_COPY,
            STARDUST,
            ISAEL_INSTRUCTIONS,
            ISAEL_LETTER,
            GRAIL_OF_PURITY,
            TEARS_OF_UNICORN,
            WATER_OF_LIFE,
            PURE_MITHRIL_ORE,
            ANT_SOLDIER_ACID,
            WYRM_TALON,
            SPIDER_ICHOR,
            HARPY_DOWN,
            3166,
            3167,
            3168,
            3169,
            3170,
            3171
        )

        addStartNpc(CARDIEN)
        addTalkId(ASTERIOS, PUSHKIN, THALIA, ADONIUS, ARKENIA, CARDIEN, ISAEL)

        addKillId(20145, 20176, 20233, 27077, 20550, 20581, 20582, 20082, 20084, 20086, 20087, 20088)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30460-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(CARDIEN_LETTER, 1)

            if (!player.memos.getBool("secondClassChange37", false)) {
                htmltext = "30460-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37[player.race.ordinal] ?: 0)
                player.memos.set("secondClassChange37", true)
            }
        } else if (event.equals("30154-07.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(CARDIEN_LETTER, 1)
            st.giveItems(HIERARCH_LETTER, 1)
            st.giveItems(MOONFLOWER_CHARM, 1)
        } else if (event.equals("30371-03.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HIERARCH_LETTER, 1)
            st.giveItems(GRAIL_DIAGRAM, 1)
        } else if (event.equals("30371-11.htm", ignoreCase = true)) {
            st.takeItems(STARDUST, 1)
            st.playSound(QuestState.SOUND_MIDDLE)

            if (player.level < 38) {
                htmltext = "30371-10.htm"
                st["cond"] = "13"
                st.giveItems(THALIA_INSTRUCTIONS, 1)
            } else {
                st["cond"] = "14"
                st.giveItems(THALIA_LETTER_2, 1)
            }
        } else if (event.equals("30300-06.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GRAIL_DIAGRAM, 1)
            st.giveItems(PUSHKIN_LIST, 1)
        } else if (event.equals("30300-10.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(PUSHKIN_LIST, 1)
            st.takeItems(ANT_SOLDIER_ACID, -1)
            st.takeItems(PURE_MITHRIL_ORE, -1)
            st.takeItems(WYRM_TALON, -1)
            st.giveItems(PURE_MITHRIL_CUP, 1)
        } else if (event.equals("30419-04.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(THALIA_LETTER_1, 1)
            st.giveItems(ARKENIA_CONTRACT, 1)
            st.giveItems(ARKENIA_INSTRUCTIONS, 1)
        } else if (event.equals("30375-02.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ARKENIA_INSTRUCTIONS, 1)
            st.giveItems(ADONIUS_LIST, 1)
        } else if (event.equals("30655-02.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(THALIA_LETTER_2, 1)
            st.giveItems(ISAEL_INSTRUCTIONS, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ELF)
                htmltext = "30460-01.htm"
            else if (player.level < 37 || player.classId.level() != 1)
                htmltext = "30460-02.htm"
            else
                htmltext = "30460-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ASTERIOS -> if (cond == 1)
                        htmltext = "30154-01.htm"
                    else if (cond == 2)
                        htmltext = "30154-08.htm"
                    else if (cond == 20) {
                        htmltext = "30154-09.htm"
                        st["cond"] = "21"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MOONFLOWER_CHARM, 1)
                        st.takeItems(WATER_OF_LIFE, 1)
                        st.giveItems(CAMOMILE_CHARM, 1)
                    } else if (cond == 21)
                        htmltext = "30154-10.htm"

                    PUSHKIN -> if (cond == 3)
                        htmltext = "30300-01.htm"
                    else if (cond == 4)
                        htmltext = "30300-07.htm"
                    else if (cond == 5)
                        htmltext = "30300-08.htm"
                    else if (cond == 6)
                        htmltext = "30300-11.htm"
                    else if (cond > 6)
                        htmltext = "30300-12.htm"

                    THALIA -> if (cond == 2)
                        htmltext = "30371-01.htm"
                    else if (cond == 3)
                        htmltext = "30371-04.htm"
                    else if (cond > 3 && cond < 6)
                        htmltext = "30371-05.htm"
                    else if (cond == 6) {
                        htmltext = "30371-06.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(PURE_MITHRIL_CUP, 1)
                        st.giveItems(THALIA_LETTER_1, 1)
                    } else if (cond == 7)
                        htmltext = "30371-07.htm"
                    else if (cond > 7 && cond < 12)
                        htmltext = "30371-08.htm"
                    else if (cond == 12)
                        htmltext = "30371-09.htm"
                    else if (cond == 13) {
                        if (player.level < 38)
                            htmltext = "30371-12.htm"
                        else {
                            htmltext = "30371-13.htm"
                            st["cond"] = "14"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(THALIA_INSTRUCTIONS, 1)
                            st.giveItems(THALIA_LETTER_2, 1)
                        }
                    } else if (cond == 14)
                        htmltext = "30371-14.htm"
                    else if (cond > 14 && cond < 17)
                        htmltext = "30371-15.htm"
                    else if (cond == 17) {
                        htmltext = "30371-16.htm"
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ISAEL_LETTER, 1)
                        st.giveItems(GRAIL_OF_PURITY, 1)
                    } else if (cond == 18)
                        htmltext = "30371-17.htm"
                    else if (cond == 19) {
                        htmltext = "30371-18.htm"
                        st["cond"] = "20"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TEARS_OF_UNICORN, 1)
                        st.giveItems(WATER_OF_LIFE, 1)
                    } else if (cond > 19)
                        htmltext = "30371-19.htm"

                    ADONIUS -> if (cond == 8)
                        htmltext = "30375-01.htm"
                    else if (cond == 9)
                        htmltext = "30375-03.htm"
                    else if (cond == 10) {
                        htmltext = "30375-04.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ADONIUS_LIST, 1)
                        st.takeItems(HARPY_DOWN, -1)
                        st.takeItems(SPIDER_ICHOR, -1)
                        st.giveItems(ANDARIEL_SCRIPTURE_COPY, 1)
                    } else if (cond == 11)
                        htmltext = "30375-05.htm"
                    else if (cond > 11)
                        htmltext = "30375-06.htm"

                    ARKENIA -> if (cond == 7)
                        htmltext = "30419-01.htm"
                    else if (cond > 7 && cond < 11)
                        htmltext = "30419-05.htm"
                    else if (cond == 11) {
                        htmltext = "30419-06.htm"
                        st["cond"] = "12"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ANDARIEL_SCRIPTURE_COPY, 1)
                        st.takeItems(ARKENIA_CONTRACT, 1)
                        st.giveItems(STARDUST, 1)
                    } else if (cond == 12)
                        htmltext = "30419-07.htm"
                    else if (cond > 12)
                        htmltext = "30419-08.htm"

                    CARDIEN -> if (cond == 1)
                        htmltext = "30460-05.htm"
                    else if (cond > 1 && cond < 21)
                        htmltext = "30460-06.htm"
                    else if (cond == 21) {
                        htmltext = "30460-07.htm"
                        st.takeItems(CAMOMILE_CHARM, 1)
                        st.giveItems(MARK_OF_LIFE, 1)
                        st.rewardExpAndSp(104591, 11250)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ISAEL -> if (cond == 14)
                        htmltext = "30655-01.htm"
                    else if (cond == 15)
                        htmltext = "30655-03.htm"
                    else if (cond == 16) {
                        if (st.hasQuestItems(*TALINS_PIECES)) {
                            htmltext = "30655-04.htm"
                            st["cond"] = "17"
                            st.playSound(QuestState.SOUND_MIDDLE)

                            for (itemId in TALINS_PIECES)
                                st.takeItems(itemId, 1)

                            st.takeItems(ISAEL_INSTRUCTIONS, 1)
                            st.giveItems(ISAEL_LETTER, 1)
                            st.giveItems(TALINS_SPEAR, 1)
                        } else
                            htmltext = "30655-03.htm"
                    } else if (cond == 17)
                        htmltext = "30655-05.htm"
                    else if (cond > 17)
                        htmltext = "30655-06.htm"
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
            20550 -> if (st.getInt("cond") == 4 && st.dropItems(PURE_MITHRIL_ORE, 1, 10, 500000))
                if (st.getQuestItemsCount(WYRM_TALON) >= 20 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
                    st["cond"] = "5"

            20176 -> if (st.getInt("cond") == 4 && st.dropItems(WYRM_TALON, 1, 20, 500000))
                if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(ANT_SOLDIER_ACID) >= 20)
                    st["cond"] = "5"

            20082, 20084, 20086, 20087, 20088 -> if (st.getInt("cond") == 4 && st.dropItems(
                    ANT_SOLDIER_ACID,
                    1,
                    20,
                    800000
                )
            )
                if (st.getQuestItemsCount(PURE_MITHRIL_ORE) >= 10 && st.getQuestItemsCount(WYRM_TALON) >= 20)
                    st["cond"] = "5"

            20233 -> if (st.getInt("cond") == 9 && st.dropItems(SPIDER_ICHOR, 1, 20, 500000) && st.getQuestItemsCount(
                    HARPY_DOWN
                ) >= 20
            )
                st["cond"] = "10"

            20145 -> if (st.getInt("cond") == 9 && st.dropItems(HARPY_DOWN, 1, 20, 500000) && st.getQuestItemsCount(
                    SPIDER_ICHOR
                ) >= 20
            )
                st["cond"] = "10"

            27077 -> if (st.getInt("cond") == 18 && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == TALINS_SPEAR) {
                st["cond"] = "19"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(GRAIL_OF_PURITY, 1)
                st.takeItems(TALINS_SPEAR, 1)
                st.giveItems(TEARS_OF_UNICORN, 1)
            }

            20581, 20582 -> if (st.getInt("cond") == 15 && Rnd.nextBoolean()) {
                for (itemId in TALINS_PIECES) {
                    if (!st.hasQuestItems(itemId)) {
                        st.giveItems(itemId, 1)

                        if (st.hasQuestItems(*TALINS_PIECES)) {
                            st["cond"] = "16"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)

                        return null
                    }
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q218_TestimonyOfLife"

        private val ASTERIOS = 30154
        private val PUSHKIN = 30300
        private val THALIA = 30371
        private val ADONIUS = 30375
        private val ARKENIA = 30419
        private val CARDIEN = 30460
        private val ISAEL = 30655

        // Items
        private val TALINS_SPEAR = 3026
        private val CARDIEN_LETTER = 3141
        private val CAMOMILE_CHARM = 3142
        private val HIERARCH_LETTER = 3143
        private val MOONFLOWER_CHARM = 3144
        private val GRAIL_DIAGRAM = 3145
        private val THALIA_LETTER_1 = 3146
        private val THALIA_LETTER_2 = 3147
        private val THALIA_INSTRUCTIONS = 3148
        private val PUSHKIN_LIST = 3149
        private val PURE_MITHRIL_CUP = 3150
        private val ARKENIA_CONTRACT = 3151
        private val ARKENIA_INSTRUCTIONS = 3152
        private val ADONIUS_LIST = 3153
        private val ANDARIEL_SCRIPTURE_COPY = 3154
        private val STARDUST = 3155
        private val ISAEL_INSTRUCTIONS = 3156
        private val ISAEL_LETTER = 3157
        private val GRAIL_OF_PURITY = 3158
        private val TEARS_OF_UNICORN = 3159
        private val WATER_OF_LIFE = 3160
        private val PURE_MITHRIL_ORE = 3161
        private val ANT_SOLDIER_ACID = 3162
        private val WYRM_TALON = 3163
        private val SPIDER_ICHOR = 3164
        private val HARPY_DOWN = 3165

        private val TALINS_PIECES = intArrayOf(3166, 3167, 3168, 3169, 3170, 3171)

        // Rewards
        private val MARK_OF_LIFE = 3140
        private val DIMENSIONAL_DIAMOND = 7562
    }
}