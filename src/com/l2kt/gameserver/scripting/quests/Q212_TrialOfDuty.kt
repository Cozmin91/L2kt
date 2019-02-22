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

class Q212_TrialOfDuty : Quest(212, "Trial of Duty") {
    init {

        setItemsIds(
            LETTER_OF_DUSTIN,
            KNIGHTS_TEAR,
            MIRROR_OF_ORPIC,
            TEAR_OF_CONFESSION,
            REPORT_PIECE_1,
            REPORT_PIECE_2,
            TEAR_OF_LOYALTY,
            MILITAS_ARTICLE,
            SAINTS_ASHES_URN,
            ATHEBALDT_SKULL,
            ATHEBALDT_RIBS,
            ATHEBALDT_SHIN,
            LETTER_OF_WINDAWOOD,
            OLD_KNIGHT_SWORD
        )

        addStartNpc(HANNAVALT)
        addTalkId(HANNAVALT, DUSTIN, SIR_COLLIN, SIR_ARON, SIR_KIEL, SILVERSHADOW, SPIRIT_TALIANUS)

        addKillId(20144, 20190, 20191, 20200, 20201, 20270, 27119, 20577, 20578, 20579, 20580, 20581, 20582)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30109-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            if (!player.memos.getBool("secondClassChange35", false)) {
                htmltext = "30109-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                player.memos.set("secondClassChange35", true)
            }
        } else if (event.equals("30116-05.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TEAR_OF_LOYALTY, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.KNIGHT && player.classId != ClassId.ELVEN_KNIGHT && player.classId != ClassId.PALUS_KNIGHT)
                htmltext = "30109-02.htm"
            else if (player.level < 35)
                htmltext = "30109-01.htm"
            else
                htmltext = "30109-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HANNAVALT -> if (cond == 18) {
                        htmltext = "30109-05.htm"
                        st.takeItems(LETTER_OF_DUSTIN, 1)
                        st.giveItems(MARK_OF_DUTY, 1)
                        st.rewardExpAndSp(79832, 3750)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    } else
                        htmltext = "30109-04a.htm"

                    SIR_ARON -> if (cond == 1) {
                        htmltext = "30653-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(OLD_KNIGHT_SWORD, 1)
                    } else if (cond == 2)
                        htmltext = "30653-02.htm"
                    else if (cond == 3) {
                        htmltext = "30653-03.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KNIGHTS_TEAR, 1)
                        st.takeItems(OLD_KNIGHT_SWORD, 1)
                    } else if (cond > 3)
                        htmltext = "30653-04.htm"

                    SIR_KIEL -> if (cond == 4) {
                        htmltext = "30654-01.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 5)
                        htmltext = "30654-02.htm"
                    else if (cond == 6) {
                        htmltext = "30654-03.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(MIRROR_OF_ORPIC, 1)
                    } else if (cond == 7)
                        htmltext = "30654-04.htm"
                    else if (cond == 9) {
                        htmltext = "30654-05.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TEAR_OF_CONFESSION, 1)
                    } else if (cond > 9)
                        htmltext = "30654-06.htm"

                    SPIRIT_TALIANUS -> if (cond == 8) {
                        htmltext = "30656-01.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MIRROR_OF_ORPIC, 1)
                        st.takeItems(REPORT_PIECE_2, 1)
                        st.giveItems(TEAR_OF_CONFESSION, 1)

                        // Despawn the spirit.
                        npc.deleteMe()
                    }

                    SILVERSHADOW -> if (cond == 10) {
                        if (player.level < 35)
                            htmltext = "30655-01.htm"
                        else {
                            htmltext = "30655-02.htm"
                            st["cond"] = "11"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 11)
                        htmltext = "30655-03.htm"
                    else if (cond == 12) {
                        htmltext = "30655-04.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MILITAS_ARTICLE, -1)
                        st.giveItems(TEAR_OF_LOYALTY, 1)
                    } else if (cond == 13)
                        htmltext = "30655-05.htm"

                    DUSTIN -> if (cond == 13)
                        htmltext = "30116-01.htm"
                    else if (cond == 14)
                        htmltext = "30116-06.htm"
                    else if (cond == 15) {
                        htmltext = "30116-07.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ATHEBALDT_SKULL, 1)
                        st.takeItems(ATHEBALDT_RIBS, 1)
                        st.takeItems(ATHEBALDT_SHIN, 1)
                        st.giveItems(SAINTS_ASHES_URN, 1)
                    } else if (cond == 16)
                        htmltext = "30116-09.htm"
                    else if (cond == 17) {
                        htmltext = "30116-08.htm"
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LETTER_OF_WINDAWOOD, 1)
                        st.giveItems(LETTER_OF_DUSTIN, 1)
                    } else if (cond == 18)
                        htmltext = "30116-10.htm"

                    SIR_COLLIN -> if (cond == 16) {
                        htmltext = "30311-01.htm"
                        st["cond"] = "17"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SAINTS_ASHES_URN, 1)
                        st.giveItems(LETTER_OF_WINDAWOOD, 1)
                    } else if (cond > 16)
                        htmltext = "30311-02.htm"
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
            20190, 20191 -> if (cond == 2 && Rnd[10] < 1) {
                st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                addSpawn(27119, npc, false, 120000, true)
            }

            27119 -> if (cond == 2 && st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == OLD_KNIGHT_SWORD) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(KNIGHTS_TEAR, 1)
            }

            20201, 20200 -> if (cond == 5 && st.dropItemsAlways(REPORT_PIECE_1, 1, 10)) {
                st["cond"] = "6"
                st.takeItems(REPORT_PIECE_1, -1)
                st.giveItems(REPORT_PIECE_2, 1)
            }

            20144 -> if ((cond == 7 || cond == 8) && Rnd[100] < 33) {
                if (cond == 7) {
                    st["cond"] = "8"
                    st.playSound(QuestState.SOUND_MIDDLE)
                }
                addSpawn(30656, npc, false, 300000, true)
            }

            20577, 20578, 20579, 20580, 20581, 20582 -> if (cond == 11 && st.dropItemsAlways(MILITAS_ARTICLE, 1, 20))
                st["cond"] = "12"

            20270 -> if (cond == 14 && Rnd.nextBoolean()) {
                if (!st.hasQuestItems(ATHEBALDT_SKULL)) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(ATHEBALDT_SKULL, 1)
                } else if (!st.hasQuestItems(ATHEBALDT_RIBS)) {
                    st.playSound(QuestState.SOUND_ITEMGET)
                    st.giveItems(ATHEBALDT_RIBS, 1)
                } else if (!st.hasQuestItems(ATHEBALDT_SHIN)) {
                    st["cond"] = "15"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(ATHEBALDT_SHIN, 1)
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q212_TrialOfDuty"

        // Items
        private val LETTER_OF_DUSTIN = 2634
        private val KNIGHTS_TEAR = 2635
        private val MIRROR_OF_ORPIC = 2636
        private val TEAR_OF_CONFESSION = 2637
        private val REPORT_PIECE_1 = 2638
        private val REPORT_PIECE_2 = 2639
        private val TEAR_OF_LOYALTY = 2640
        private val MILITAS_ARTICLE = 2641
        private val SAINTS_ASHES_URN = 2642
        private val ATHEBALDT_SKULL = 2643
        private val ATHEBALDT_RIBS = 2644
        private val ATHEBALDT_SHIN = 2645
        private val LETTER_OF_WINDAWOOD = 2646
        private val OLD_KNIGHT_SWORD = 3027

        // Rewards
        private val MARK_OF_DUTY = 2633
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val HANNAVALT = 30109
        private val DUSTIN = 30116
        private val SIR_COLLIN = 30311
        private val SIR_ARON = 30653
        private val SIR_KIEL = 30654
        private val SILVERSHADOW = 30655
        private val SPIRIT_TALIANUS = 30656
    }
}