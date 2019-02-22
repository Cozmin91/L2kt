package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q415_PathToAMonk : Quest(415, "Path to a Monk") {
    init {

        setItemsIds(
            POMEGRANATE,
            LEATHER_POUCH_1,
            LEATHER_POUCH_2,
            LEATHER_POUCH_3,
            LEATHER_POUCH_FULL_1,
            LEATHER_POUCH_FULL_2,
            LEATHER_POUCH_FULL_3,
            KASHA_BEAR_CLAW,
            KASHA_BLADE_SPIDER_TALON,
            SCARLET_SALAMANDER_SCALE,
            FIERY_SPIRIT_SCROLL,
            ROSHEEK_LETTER,
            GANTAKI_LETTER_OF_RECOMMENDATION,
            FIG,
            LEATHER_POUCH_4,
            LEATHER_POUCH_FULL_4,
            VUKU_ORC_TUSK,
            RATMAN_FANG,
            LANG_KLIZARDMAN_TOOTH,
            FELIM_LIZARDMAN_TOOTH,
            IRON_WILL_SCROLL,
            TORUKU_LETTER,
            KASHA_SPIDER_TOOTH,
            HORN_OF_BAAR_DRE_VANUL
        )

        addStartNpc(GANTAKI)
        addTalkId(GANTAKI, ROSHEEK, KASMAN, TORUKU, AREN, MOIRA)

        addKillId(20014, 20017, 20024, 20359, 20415, 20476, 20478, 20479, 21118)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30587-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ORC_FIGHTER)
                htmltext = if (player.classId == ClassId.MONK) "30587-02a.htm" else "30587-02.htm"
            else if (player.level < 19)
                htmltext = "30587-03.htm"
            else if (st.hasQuestItems(KHAVATARI_TOTEM))
                htmltext = "30587-04.htm"
        } else if (event.equals("30587-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(POMEGRANATE, 1)
        } else if (event.equals("30587-09a.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROSHEEK_LETTER, 1)
            st.giveItems(GANTAKI_LETTER_OF_RECOMMENDATION, 1)
        } else if (event.equals("30587-09b.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROSHEEK_LETTER, 1)
        } else if (event.equals("32056-03.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32056-08.htm", ignoreCase = true)) {
            st["cond"] = "20"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31979-03.htm", ignoreCase = true)) {
            st.takeItems(FIERY_SPIRIT_SCROLL, 1)
            st.giveItems(KHAVATARI_TOTEM, 1)
            st.rewardExpAndSp(3200, 4230)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30587-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GANTAKI -> if (cond == 1)
                        htmltext = "30587-07.htm"
                    else if (cond > 1 && cond < 8)
                        htmltext = "30587-08.htm"
                    else if (cond == 8)
                        htmltext = "30587-09.htm"
                    else if (cond == 9)
                        htmltext = "30587-10.htm"
                    else if (cond > 9)
                        htmltext = "30587-11.htm"

                    ROSHEEK -> if (cond == 1) {
                        htmltext = "30590-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(POMEGRANATE, 1)
                        st.giveItems(LEATHER_POUCH_1, 1)
                    } else if (cond == 2)
                        htmltext = "30590-02.htm"
                    else if (cond == 3) {
                        htmltext = "30590-03.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LEATHER_POUCH_FULL_1, 1)
                        st.giveItems(LEATHER_POUCH_2, 1)
                    } else if (cond == 4)
                        htmltext = "30590-04.htm"
                    else if (cond == 5) {
                        htmltext = "30590-05.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LEATHER_POUCH_FULL_2, 1)
                        st.giveItems(LEATHER_POUCH_3, 1)
                    } else if (cond == 6)
                        htmltext = "30590-06.htm"
                    else if (cond == 7) {
                        htmltext = "30590-07.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LEATHER_POUCH_FULL_3, 1)
                        st.giveItems(FIERY_SPIRIT_SCROLL, 1)
                        st.giveItems(ROSHEEK_LETTER, 1)
                    } else if (cond == 8)
                        htmltext = "30590-08.htm"
                    else if (cond > 8)
                        htmltext = "30590-09.htm"

                    KASMAN -> if (cond == 9) {
                        htmltext = "30501-01.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GANTAKI_LETTER_OF_RECOMMENDATION, 1)
                        st.giveItems(FIG, 1)
                    } else if (cond == 10)
                        htmltext = "30501-02.htm"
                    else if (cond == 11 || cond == 12)
                        htmltext = "30501-03.htm"
                    else if (cond == 13) {
                        htmltext = "30501-04.htm"
                        st.takeItems(FIERY_SPIRIT_SCROLL, 1)
                        st.takeItems(IRON_WILL_SCROLL, 1)
                        st.takeItems(TORUKU_LETTER, 1)
                        st.giveItems(KHAVATARI_TOTEM, 1)
                        st.rewardExpAndSp(3200, 1500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    TORUKU -> if (cond == 10) {
                        htmltext = "30591-01.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FIG, 1)
                        st.giveItems(LEATHER_POUCH_4, 1)
                    } else if (cond == 11)
                        htmltext = "30591-02.htm"
                    else if (cond == 12) {
                        htmltext = "30591-03.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LEATHER_POUCH_FULL_4, 1)
                        st.giveItems(IRON_WILL_SCROLL, 1)
                        st.giveItems(TORUKU_LETTER, 1)
                    } else if (cond == 13)
                        htmltext = "30591-04.htm"

                    AREN -> if (cond == 14)
                        htmltext = "32056-01.htm"
                    else if (cond == 15)
                        htmltext = "32056-04.htm"
                    else if (cond == 16) {
                        htmltext = "32056-05.htm"
                        st["cond"] = "17"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KASHA_SPIDER_TOOTH, -1)
                    } else if (cond == 17)
                        htmltext = "32056-06.htm"
                    else if (cond == 18) {
                        htmltext = "32056-07.htm"
                        st["cond"] = "19"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HORN_OF_BAAR_DRE_VANUL, -1)
                    } else if (cond == 20)
                        htmltext = "32056-09.htm"

                    MOIRA -> if (cond == 20)
                        htmltext = "31979-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val weapon = player!!.attackType
        if (weapon != WeaponType.DUALFIST && weapon != WeaponType.FIST) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
            return null
        }

        when (npc.npcId) {
            20479 -> if (st.getInt("cond") == 2 && st.dropItemsAlways(KASHA_BEAR_CLAW, 1, 5)) {
                st["cond"] = "3"
                st.takeItems(KASHA_BEAR_CLAW, -1)
                st.takeItems(LEATHER_POUCH_1, 1)
                st.giveItems(LEATHER_POUCH_FULL_1, 1)
            }

            20478 -> if (st.getInt("cond") == 4 && st.dropItemsAlways(KASHA_BLADE_SPIDER_TALON, 1, 5)) {
                st["cond"] = "5"
                st.takeItems(KASHA_BLADE_SPIDER_TALON, -1)
                st.takeItems(LEATHER_POUCH_2, 1)
                st.giveItems(LEATHER_POUCH_FULL_2, 1)
            } else if (st.getInt("cond") == 15 && st.dropItems(KASHA_SPIDER_TOOTH, 1, 6, 500000))
                st["cond"] = "16"

            20476 -> if (st.getInt("cond") == 15 && st.dropItems(KASHA_SPIDER_TOOTH, 1, 6, 500000))
                st["cond"] = "16"

            20415 -> if (st.getInt("cond") == 6 && st.dropItemsAlways(SCARLET_SALAMANDER_SCALE, 1, 5)) {
                st["cond"] = "7"
                st.takeItems(SCARLET_SALAMANDER_SCALE, -1)
                st.takeItems(LEATHER_POUCH_3, 1)
                st.giveItems(LEATHER_POUCH_FULL_3, 1)
            }

            20014 -> if (st.getInt("cond") == 11 && st.dropItemsAlways(FELIM_LIZARDMAN_TOOTH, 1, 3)) {
                if (st.getQuestItemsCount(RATMAN_FANG) == 3 && st.getQuestItemsCount(LANG_KLIZARDMAN_TOOTH) == 3 && st.getQuestItemsCount(
                        VUKU_ORC_TUSK
                    ) == 3
                ) {
                    st["cond"] = "12"
                    st.takeItems(VUKU_ORC_TUSK, -1)
                    st.takeItems(RATMAN_FANG, -1)
                    st.takeItems(LANG_KLIZARDMAN_TOOTH, -1)
                    st.takeItems(FELIM_LIZARDMAN_TOOTH, -1)
                    st.takeItems(LEATHER_POUCH_4, 1)
                    st.giveItems(LEATHER_POUCH_FULL_4, 1)
                }
            }

            20017 -> if (st.getInt("cond") == 11 && st.dropItemsAlways(VUKU_ORC_TUSK, 1, 3)) {
                if (st.getQuestItemsCount(RATMAN_FANG) == 3 && st.getQuestItemsCount(LANG_KLIZARDMAN_TOOTH) == 3 && st.getQuestItemsCount(
                        FELIM_LIZARDMAN_TOOTH
                    ) == 3
                ) {
                    st["cond"] = "12"
                    st.takeItems(VUKU_ORC_TUSK, -1)
                    st.takeItems(RATMAN_FANG, -1)
                    st.takeItems(LANG_KLIZARDMAN_TOOTH, -1)
                    st.takeItems(FELIM_LIZARDMAN_TOOTH, -1)
                    st.takeItems(LEATHER_POUCH_4, 1)
                    st.giveItems(LEATHER_POUCH_FULL_4, 1)
                }
            }

            20024 -> if (st.getInt("cond") == 11 && st.dropItemsAlways(LANG_KLIZARDMAN_TOOTH, 1, 3)) {
                if (st.getQuestItemsCount(RATMAN_FANG) == 3 && st.getQuestItemsCount(FELIM_LIZARDMAN_TOOTH) == 3 && st.getQuestItemsCount(
                        VUKU_ORC_TUSK
                    ) == 3
                ) {
                    st["cond"] = "12"
                    st.takeItems(VUKU_ORC_TUSK, -1)
                    st.takeItems(RATMAN_FANG, -1)
                    st.takeItems(LANG_KLIZARDMAN_TOOTH, -1)
                    st.takeItems(FELIM_LIZARDMAN_TOOTH, -1)
                    st.takeItems(LEATHER_POUCH_4, 1)
                    st.giveItems(LEATHER_POUCH_FULL_4, 1)
                }
            }

            20359 -> if (st.getInt("cond") == 11 && st.dropItemsAlways(RATMAN_FANG, 1, 3)) {
                if (st.getQuestItemsCount(LANG_KLIZARDMAN_TOOTH) == 3 && st.getQuestItemsCount(FELIM_LIZARDMAN_TOOTH) == 3 && st.getQuestItemsCount(
                        VUKU_ORC_TUSK
                    ) == 3
                ) {
                    st["cond"] = "12"
                    st.takeItems(VUKU_ORC_TUSK, -1)
                    st.takeItems(RATMAN_FANG, -1)
                    st.takeItems(LANG_KLIZARDMAN_TOOTH, -1)
                    st.takeItems(FELIM_LIZARDMAN_TOOTH, -1)
                    st.takeItems(LEATHER_POUCH_4, 1)
                    st.giveItems(LEATHER_POUCH_FULL_4, 1)
                }
            }

            21118 -> if (st.getInt("cond") == 17) {
                st["cond"] = "18"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(HORN_OF_BAAR_DRE_VANUL, 1)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q415_PathToAMonk"

        // Items
        private val POMEGRANATE = 1593
        private val LEATHER_POUCH_1 = 1594
        private val LEATHER_POUCH_2 = 1595
        private val LEATHER_POUCH_3 = 1596
        private val LEATHER_POUCH_FULL_1 = 1597
        private val LEATHER_POUCH_FULL_2 = 1598
        private val LEATHER_POUCH_FULL_3 = 1599
        private val KASHA_BEAR_CLAW = 1600
        private val KASHA_BLADE_SPIDER_TALON = 1601
        private val SCARLET_SALAMANDER_SCALE = 1602
        private val FIERY_SPIRIT_SCROLL = 1603
        private val ROSHEEK_LETTER = 1604
        private val GANTAKI_LETTER_OF_RECOMMENDATION = 1605
        private val FIG = 1606
        private val LEATHER_POUCH_4 = 1607
        private val LEATHER_POUCH_FULL_4 = 1608
        private val VUKU_ORC_TUSK = 1609
        private val RATMAN_FANG = 1610
        private val LANG_KLIZARDMAN_TOOTH = 1611
        private val FELIM_LIZARDMAN_TOOTH = 1612
        private val IRON_WILL_SCROLL = 1613
        private val TORUKU_LETTER = 1614
        private val KHAVATARI_TOTEM = 1615
        private val KASHA_SPIDER_TOOTH = 8545
        private val HORN_OF_BAAR_DRE_VANUL = 8546

        // NPCs
        private val GANTAKI = 30587
        private val ROSHEEK = 30590
        private val KASMAN = 30501
        private val TORUKU = 30591
        private val AREN = 32056
        private val MOIRA = 31979
    }
}