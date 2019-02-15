package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q215_TrialOfThePilgrim : Quest(215, "Trial of the Pilgrim") {
    init {

        setItemsIds(
            BOOK_OF_SAGE,
            VOUCHER_OF_TRIAL,
            SPIRIT_OF_FLAME,
            ESSENCE_OF_FLAME,
            BOOK_OF_GERALD,
            GRAY_BADGE,
            PICTURE_OF_NAHIR,
            HAIR_OF_NAHIR,
            STATUE_OF_EINHASAD,
            BOOK_OF_DARKNESS,
            DEBRIS_OF_WILLOW,
            TAG_OF_RUMOR
        )

        addStartNpc(SANTIAGO)
        addTalkId(
            SANTIAGO,
            TANAPI,
            ANCESTOR_MARTANKUS,
            GAURI_TWINKLEROCK,
            DORF,
            GERALD,
            PRIMOS,
            PETRON,
            ANDELLIA,
            URUHA,
            CASIAN
        )

        addKillId(LAVA_SALAMANDER, NAHIR, BLACK_WILLOW)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event

        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30648-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(VOUCHER_OF_TRIAL, 1)

            if (!player.memos.getBool("secondClassChange35", false)) {
                htmltext = "30648-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                player.memos.set("secondClassChange35", true)
            }
        } else if (event.equals("30649-04.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ESSENCE_OF_FLAME, 1)
            st.giveItems(SPIRIT_OF_FLAME, 1)
        } else if (event.equals("30650-02.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(57) >= 100000) {
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(57, 100000)
                st.giveItems(BOOK_OF_GERALD, 1)
            } else
                htmltext = "30650-03.htm"
        } else if (event.equals("30652-02.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(DEBRIS_OF_WILLOW, 1)
            st.giveItems(BOOK_OF_DARKNESS, 1)
        } else if (event.equals("30362-04.htm", ignoreCase = true)) {
            st["cond"] = "16"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30362-05.htm", ignoreCase = true)) {
            st["cond"] = "16"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BOOK_OF_DARKNESS, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.CLERIC && player.classId != ClassId.ELVEN_ORACLE && player.classId != ClassId.SHILLIEN_ORACLE && player.classId != ClassId.ORC_SHAMAN)
                htmltext = "30648-02.htm"
            else if (player.level < 35)
                htmltext = "30648-01.htm"
            else
                htmltext = "30648-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SANTIAGO -> if (cond < 17)
                        htmltext = "30648-09.htm"
                    else if (cond == 17) {
                        htmltext = "30648-10.htm"
                        st.takeItems(BOOK_OF_SAGE, 1)
                        st.giveItems(MARK_OF_PILGRIM, 1)
                        st.rewardExpAndSp(77382, 16000)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    TANAPI -> if (cond == 1) {
                        htmltext = "30571-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(VOUCHER_OF_TRIAL, 1)
                    } else if (cond < 5)
                        htmltext = "30571-02.htm"
                    else if (cond >= 5) {
                        htmltext = "30571-03.htm"

                        if (cond == 5) {
                            st["cond"] = "6"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    }

                    ANCESTOR_MARTANKUS -> if (cond == 2) {
                        htmltext = "30649-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 3)
                        htmltext = "30649-02.htm"
                    else if (cond == 4)
                        htmltext = "30649-03.htm"

                    GAURI_TWINKLEROCK -> if (cond == 6) {
                        htmltext = "30550-01.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(TAG_OF_RUMOR, 1)
                    } else if (cond > 6)
                        htmltext = "30550-02.htm"

                    DORF -> if (cond == 7) {
                        htmltext = if (!st.hasQuestItems(BOOK_OF_GERALD)) "30651-01.htm" else "30651-02.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TAG_OF_RUMOR, 1)
                        st.giveItems(GRAY_BADGE, 1)
                    } else if (cond > 7)
                        htmltext = "30651-03.htm"

                    GERALD -> if (cond == 7 && !st.hasQuestItems(BOOK_OF_GERALD))
                        htmltext = "30650-01.htm"
                    else if (cond == 8 && st.hasQuestItems(BOOK_OF_GERALD)) {
                        htmltext = "30650-04.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.takeItems(BOOK_OF_GERALD, 1)
                        st.giveItems(57, 100000)
                    }

                    PRIMOS -> if (cond == 8) {
                        htmltext = "30117-01.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 8)
                        htmltext = "30117-02.htm"

                    PETRON -> if (cond == 9) {
                        htmltext = "30036-01.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(PICTURE_OF_NAHIR, 1)
                    } else if (cond == 10)
                        htmltext = "30036-02.htm"
                    else if (cond == 11) {
                        htmltext = "30036-03.htm"
                        st["cond"] = "12"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(HAIR_OF_NAHIR, 1)
                        st.takeItems(PICTURE_OF_NAHIR, 1)
                        st.giveItems(STATUE_OF_EINHASAD, 1)
                    } else if (cond > 11)
                        htmltext = "30036-04.htm"

                    ANDELLIA -> if (cond == 12) {
                        if (player.level < 36)
                            htmltext = "30362-01a.htm"
                        else {
                            htmltext = "30362-01.htm"
                            st["cond"] = "13"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 13)
                        htmltext = if (Rnd.nextBoolean()) "30362-02.htm" else "30362-02a.htm"
                    else if (cond == 14)
                        htmltext = "30362-07.htm"
                    else if (cond == 15)
                        htmltext = "30362-03.htm"
                    else if (cond == 16)
                        htmltext = "30362-06.htm"

                    URUHA -> if (cond == 14)
                        htmltext = "30652-01.htm"
                    else if (cond == 15)
                        htmltext = "30652-03.htm"

                    CASIAN -> if (cond == 16) {
                        htmltext = "30612-01.htm"
                        st["cond"] = "17"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BOOK_OF_DARKNESS, 1)
                        st.takeItems(GRAY_BADGE, 1)
                        st.takeItems(SPIRIT_OF_FLAME, 1)
                        st.takeItems(STATUE_OF_EINHASAD, 1)
                        st.giveItems(BOOK_OF_SAGE, 1)
                    } else if (cond == 17)
                        htmltext = "30612-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            LAVA_SALAMANDER -> if (st.getInt("cond") == 3 && st.dropItems(ESSENCE_OF_FLAME, 1, 1, 200000))
                st["cond"] = "4"

            NAHIR -> if (st.getInt("cond") == 10 && st.dropItems(HAIR_OF_NAHIR, 1, 1, 200000))
                st["cond"] = "11"

            BLACK_WILLOW -> if (st.getInt("cond") == 13 && st.dropItems(DEBRIS_OF_WILLOW, 1, 1, 200000))
                st["cond"] = "14"
        }

        return null
    }

    companion object {
        private val qn = "Q215_TrialOfThePilgrim"

        // Items
        private val BOOK_OF_SAGE = 2722
        private val VOUCHER_OF_TRIAL = 2723
        private val SPIRIT_OF_FLAME = 2724
        private val ESSENCE_OF_FLAME = 2725
        private val BOOK_OF_GERALD = 2726
        private val GRAY_BADGE = 2727
        private val PICTURE_OF_NAHIR = 2728
        private val HAIR_OF_NAHIR = 2729
        private val STATUE_OF_EINHASAD = 2730
        private val BOOK_OF_DARKNESS = 2731
        private val DEBRIS_OF_WILLOW = 2732
        private val TAG_OF_RUMOR = 2733

        // Rewards
        private val MARK_OF_PILGRIM = 2721
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val SANTIAGO = 30648
        private val TANAPI = 30571
        private val ANCESTOR_MARTANKUS = 30649
        private val GAURI_TWINKLEROCK = 30550
        private val DORF = 30651
        private val GERALD = 30650
        private val PRIMOS = 30117
        private val PETRON = 30036
        private val ANDELLIA = 30362
        private val URUHA = 30652
        private val CASIAN = 30612

        // Monsters
        private val LAVA_SALAMANDER = 27116
        private val NAHIR = 27117
        private val BLACK_WILLOW = 27118
    }
}