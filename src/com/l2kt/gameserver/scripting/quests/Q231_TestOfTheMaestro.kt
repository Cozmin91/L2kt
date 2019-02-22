package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q231_TestOfTheMaestro : Quest(231, "Test Of The Maestro") {
    init {

        setItemsIds(
            RECOMMENDATION_OF_BALANKI,
            RECOMMENDATION_OF_FILAUR,
            RECOMMENDATION_OF_ARIN,
            LETTER_OF_SOLDER_DETACHMENT,
            PAINT_OF_KAMURU,
            NECKLACE_OF_KAMURU,
            PAINT_OF_TELEPORT_DEVICE,
            TELEPORT_DEVICE,
            ARCHITECTURE_OF_KRUMA,
            REPORT_OF_KRUMA,
            INGREDIENTS_OF_ANTIDOTE,
            STINGER_WASP_NEEDLE,
            MARSH_SPIDER_WEB,
            BLOOD_OF_LEECH,
            BROKEN_TELEPORT_DEVICE
        )

        addStartNpc(LOCKIRIN)
        addTalkId(LOCKIRIN, SPIRON, BALANKI, KEEF, FILAUR, ARIN, TOMA, CROTO, DUBABAH, LORAIN)

        addKillId(GIANT_MIST_LEECH, STINGER_WASP, MARSH_SPIDER, EVIL_EYE_LORD)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // LOCKIRIN
        if (event.equals("30531-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30531-04a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30533-02.htm", ignoreCase = true))
            st["bCond"] = "1"
        else if (event.equals("30671-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(PAINT_OF_KAMURU, 1)
        } else if (event.equals("30556-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(PAINT_OF_TELEPORT_DEVICE, 1)
            st.giveItems(BROKEN_TELEPORT_DEVICE, 1)
            player.teleToLocation(140352, -194133, -3146, 0)
            startQuestTimer("spawn_bugbears", 5000, null, player, false)
        } else if (event.equals("30673-04.htm", ignoreCase = true)) {
            st["fCond"] = "2"
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(BLOOD_OF_LEECH, -1)
            st.takeItems(INGREDIENTS_OF_ANTIDOTE, 1)
            st.takeItems(MARSH_SPIDER_WEB, -1)
            st.takeItems(STINGER_WASP_NEEDLE, -1)
            st.giveItems(REPORT_OF_KRUMA, 1)
        } else if (event.equals("spawn_bugbears", ignoreCase = true)) {
            val bugbear1 = addSpawn(KING_BUGBEAR, 140333, -194153, -3138, 0, false, 200000, true) as Attackable?
            bugbear1!!.addDamageHate(player, 0, 999)
            bugbear1.ai.setIntention(CtrlIntention.ATTACK, player)

            val bugbear2 = addSpawn(KING_BUGBEAR, 140395, -194147, -3146, 0, false, 200000, true) as Attackable?
            bugbear2!!.addDamageHate(player, 0, 999)
            bugbear2.ai.setIntention(CtrlIntention.ATTACK, player)

            val bugbear3 = addSpawn(KING_BUGBEAR, 140304, -194082, -3157, 0, false, 200000, true) as Attackable?
            bugbear3!!.addDamageHate(player, 0, 999)
            bugbear3.ai.setIntention(CtrlIntention.ATTACK, player)

            return null
        }// Spawns 3 King Bugbears
        // LORAIN
        // TOMA
        // CROTO
        // BALANKI

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.ARTISAN)
                htmltext = "30531-01.htm"
            else if (player.level < 39)
                htmltext = "30531-02.htm"
            else
                htmltext = "30531-03.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                LOCKIRIN -> {
                    val cond = st.getInt("cond")
                    if (cond == 1)
                        htmltext = "30531-05.htm"
                    else if (cond == 2) {
                        htmltext = "30531-06.htm"
                        st.takeItems(RECOMMENDATION_OF_ARIN, 1)
                        st.takeItems(RECOMMENDATION_OF_BALANKI, 1)
                        st.takeItems(RECOMMENDATION_OF_FILAUR, 1)
                        st.giveItems(MARK_OF_MAESTRO, 1)
                        st.rewardExpAndSp(46000, 5900)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }
                }

                SPIRON -> htmltext = "30532-01.htm"

                KEEF -> htmltext = "30534-01.htm"

                // Part 1
                BALANKI -> {
                    var bCond = st.getInt("bCond")
                    if (bCond == 0)
                        htmltext = "30533-01.htm"
                    else if (bCond == 1)
                        htmltext = "30533-03.htm"
                    else if (bCond == 2) {
                        htmltext = "30533-04.htm"
                        st["bCond"] = "3"
                        st.takeItems(LETTER_OF_SOLDER_DETACHMENT, 1)
                        st.giveItems(RECOMMENDATION_OF_BALANKI, 1)

                        if (st.hasQuestItems(RECOMMENDATION_OF_ARIN, RECOMMENDATION_OF_FILAUR)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (bCond == 3)
                        htmltext = "30533-05.htm"
                }

                CROTO -> {
                    var bCond = st.getInt("bCond")
                    if (bCond == 1) {
                        if (!st.hasQuestItems(PAINT_OF_KAMURU))
                            htmltext = "30671-01.htm"
                        else if (!st.hasQuestItems(NECKLACE_OF_KAMURU))
                            htmltext = "30671-03.htm"
                        else {
                            htmltext = "30671-04.htm"
                            st["bCond"] = "2"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(NECKLACE_OF_KAMURU, 1)
                            st.takeItems(PAINT_OF_KAMURU, 1)
                            st.giveItems(LETTER_OF_SOLDER_DETACHMENT, 1)
                        }
                    } else if (bCond > 1)
                        htmltext = "30671-05.htm"
                }

                DUBABAH -> htmltext = "30672-01.htm"

                // Part 2
                ARIN -> {
                    var aCond = st.getInt("aCond")
                    if (aCond == 0) {
                        htmltext = "30536-01.htm"
                        st["aCond"] = "1"
                        st.giveItems(PAINT_OF_TELEPORT_DEVICE, 1)
                    } else if (aCond == 1)
                        htmltext = "30536-02.htm"
                    else if (aCond == 2) {
                        htmltext = "30536-03.htm"
                        st["aCond"] = "3"
                        st.takeItems(TELEPORT_DEVICE, -1)
                        st.giveItems(RECOMMENDATION_OF_ARIN, 1)

                        if (st.hasQuestItems(RECOMMENDATION_OF_BALANKI, RECOMMENDATION_OF_FILAUR)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (aCond == 3)
                        htmltext = "30536-04.htm"
                }

                TOMA -> {
                    var aCond = st.getInt("aCond")
                    if (aCond == 1) {
                        if (!st.hasQuestItems(BROKEN_TELEPORT_DEVICE))
                            htmltext = "30556-01.htm"
                        else if (!st.hasQuestItems(TELEPORT_DEVICE)) {
                            htmltext = "30556-06.htm"
                            st["aCond"] = "2"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(BROKEN_TELEPORT_DEVICE, 1)
                            st.giveItems(TELEPORT_DEVICE, 5)
                        }
                    } else if (aCond > 1)
                        htmltext = "30556-07.htm"
                }

                // Part 3
                FILAUR -> {
                    var fCond = st.getInt("fCond")
                    if (fCond == 0) {
                        htmltext = "30535-01.htm"
                        st["fCond"] = "1"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.giveItems(ARCHITECTURE_OF_KRUMA, 1)
                    } else if (fCond == 1)
                        htmltext = "30535-02.htm"
                    else if (fCond == 2) {
                        htmltext = "30535-03.htm"
                        st["fCond"] = "3"
                        st.takeItems(REPORT_OF_KRUMA, 1)
                        st.giveItems(RECOMMENDATION_OF_FILAUR, 1)

                        if (st.hasQuestItems(RECOMMENDATION_OF_BALANKI, RECOMMENDATION_OF_ARIN)) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            st.playSound(QuestState.SOUND_ITEMGET)
                    } else if (fCond == 3)
                        htmltext = "30535-04.htm"
                }

                LORAIN -> {
                    var fCond = st.getInt("fCond")
                    if (fCond == 1) {
                        if (!st.hasQuestItems(REPORT_OF_KRUMA)) {
                            if (!st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE)) {
                                htmltext = "30673-01.htm"
                                st.playSound(QuestState.SOUND_ITEMGET)
                                st.takeItems(ARCHITECTURE_OF_KRUMA, 1)
                                st.giveItems(INGREDIENTS_OF_ANTIDOTE, 1)
                            } else if (st.getQuestItemsCount(STINGER_WASP_NEEDLE) < 10 || st.getQuestItemsCount(
                                    MARSH_SPIDER_WEB
                                ) < 10 || st.getQuestItemsCount(BLOOD_OF_LEECH) < 10
                            )
                                htmltext = "30673-02.htm"
                            else
                                htmltext = "30673-03.htm"
                        }
                    } else if (fCond > 1)
                        htmltext = "30673-05.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            GIANT_MIST_LEECH -> if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
                st.dropItemsAlways(BLOOD_OF_LEECH, 1, 10)

            STINGER_WASP -> if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
                st.dropItemsAlways(STINGER_WASP_NEEDLE, 1, 10)

            MARSH_SPIDER -> if (st.hasQuestItems(INGREDIENTS_OF_ANTIDOTE))
                st.dropItemsAlways(MARSH_SPIDER_WEB, 1, 10)

            EVIL_EYE_LORD -> if (st.hasQuestItems(PAINT_OF_KAMURU))
                st.dropItemsAlways(NECKLACE_OF_KAMURU, 1, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q231_TestOfTheMaestro"

        private val RECOMMENDATION_OF_BALANKI = 2864
        private val RECOMMENDATION_OF_FILAUR = 2865
        private val RECOMMENDATION_OF_ARIN = 2866
        private val LETTER_OF_SOLDER_DETACHMENT = 2868
        private val PAINT_OF_KAMURU = 2869
        private val NECKLACE_OF_KAMURU = 2870
        private val PAINT_OF_TELEPORT_DEVICE = 2871
        private val TELEPORT_DEVICE = 2872
        private val ARCHITECTURE_OF_KRUMA = 2873
        private val REPORT_OF_KRUMA = 2874
        private val INGREDIENTS_OF_ANTIDOTE = 2875
        private val STINGER_WASP_NEEDLE = 2876
        private val MARSH_SPIDER_WEB = 2877
        private val BLOOD_OF_LEECH = 2878
        private val BROKEN_TELEPORT_DEVICE = 2916

        // Rewards
        private val MARK_OF_MAESTRO = 2867
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val LOCKIRIN = 30531
        private val SPIRON = 30532
        private val BALANKI = 30533
        private val KEEF = 30534
        private val FILAUR = 30535
        private val ARIN = 30536
        private val TOMA = 30556
        private val CROTO = 30671
        private val DUBABAH = 30672
        private val LORAIN = 30673

        // Monsters
        private val KING_BUGBEAR = 20150
        private val GIANT_MIST_LEECH = 20225
        private val STINGER_WASP = 20229
        private val MARSH_SPIDER = 20233
        private val EVIL_EYE_LORD = 27133
    }
}