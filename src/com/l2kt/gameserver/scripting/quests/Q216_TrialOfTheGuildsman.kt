package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q216_TrialOfTheGuildsman : Quest(216, "Trial of the Guildsman") {
    init {

        setItemsIds(
            RECIPE_JOURNEYMAN_RING,
            RECIPE_AMBER_BEAD,
            VALKON_RECOMMENDATION,
            MANDRAGORA_BERRY,
            ALTRAN_INSTRUCTIONS,
            ALTRAN_RECOMMENDATION_1,
            ALTRAN_RECOMMENDATION_2,
            NORMAN_INSTRUCTIONS,
            NORMAN_RECEIPT,
            DUNING_INSTRUCTIONS,
            DUNING_KEY,
            NORMAN_LIST,
            GRAY_BONE_POWDER,
            GRANITE_WHETSTONE,
            RED_PIGMENT,
            BRAIDED_YARN,
            JOURNEYMAN_GEM,
            PINTER_INSTRUCTIONS,
            AMBER_BEAD,
            AMBER_LUMP,
            JOURNEYMAN_DECO_BEADS,
            JOURNEYMAN_RING
        )

        addStartNpc(VALKON)
        addTalkId(VALKON, NORMAN, ALTRAN, PINTER, DUNING)

        addKillId(
            ANT,
            ANT_CAPTAIN,
            GRANITE_GOLEM,
            MANDRAGORA_SPROUT,
            MANDRAGORA_SAPLING,
            MANDRAGORA_BLOSSOM,
            SILENOS,
            STRAIN,
            GHOUL,
            DEAD_SEEKER,
            BREKA_ORC_SHAMAN,
            BREKA_ORC_OVERLORD,
            BREKA_ORC_WARRIOR
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event

        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("30103-06.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(57) >= 2000) {
                st.state = Quest.STATE_STARTED
                st["cond"] = "1"
                st.playSound(QuestState.SOUND_ACCEPT)
                st.takeItems(57, 2000)
                st.giveItems(VALKON_RECOMMENDATION, 1)

                if (!player.memos.getBool("secondClassChange35", false)) {
                    htmltext = "30103-06d.htm"
                    st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_35[player.classId.id] ?: 0)
                    player.memos.set("secondClassChange35", true)
                }
            } else
                htmltext = "30103-05a.htm"
        } else if (event.equals("30103-06c.htm", ignoreCase = true) || event.equals(
                "30103-07c.htm",
                ignoreCase = true
            )
        ) {
            if (st.getInt("cond") < 3) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("30103-09a.htm", ignoreCase = true) || event.equals(
                "30103-09b.htm",
                ignoreCase = true
            )
        ) {
            st.takeItems(ALTRAN_INSTRUCTIONS, 1)
            st.takeItems(JOURNEYMAN_RING, -1)
            st.giveItems(MARK_OF_GUILDSMAN, 1)
            st.rewardExpAndSp(80993, 12250)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        } else if (event.equals("30210-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(ALTRAN_RECOMMENDATION_1, 1)
            st.giveItems(NORMAN_INSTRUCTIONS, 1)
            st.giveItems(NORMAN_RECEIPT, 1)
        } else if (event.equals("30210-10.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(NORMAN_LIST, 1)
        } else if (event.equals("30283-03.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(MANDRAGORA_BERRY, 1)
            st.takeItems(VALKON_RECOMMENDATION, 1)
            st.giveItems(ALTRAN_INSTRUCTIONS, 1)
            st.giveItems(ALTRAN_RECOMMENDATION_1, 1)
            st.giveItems(ALTRAN_RECOMMENDATION_2, 1)
            st.giveItems(RECIPE_JOURNEYMAN_RING, 1)
        } else if (event.equals("30298-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(ALTRAN_RECOMMENDATION_2, 1)
            st.giveItems(PINTER_INSTRUCTIONS, 1)

            // Artisan receives a recipe to craft Amber Beads, while spoiler case is handled in onKill section.
            if (player.classId == ClassId.ARTISAN) {
                htmltext = "30298-05.htm"
                st.giveItems(RECIPE_AMBER_BEAD, 1)
            }
        } else if (event.equals("30688-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(NORMAN_RECEIPT, 1)
            st.giveItems(DUNING_INSTRUCTIONS, 1)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.SCAVENGER && player.classId != ClassId.ARTISAN)
                htmltext = "30103-01.htm"
            else if (player.level < 35)
                htmltext = "30103-02.htm"
            else
                htmltext = "30103-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VALKON -> if (cond == 1)
                        htmltext = "30103-06c.htm"
                    else if (cond < 5)
                        htmltext = "30103-07.htm"
                    else if (cond == 5)
                        htmltext = "30103-08.htm"
                    else if (cond == 6)
                        htmltext = if (st.getQuestItemsCount(JOURNEYMAN_RING) == 7) "30103-09.htm" else "30103-08.htm"

                    ALTRAN -> if (cond < 4) {
                        htmltext = "30283-01.htm"
                        if (cond == 1) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 4)
                        htmltext = "30283-02.htm"
                    else if (cond > 4)
                        htmltext = "30283-04.htm"

                    NORMAN -> if (cond == 5) {
                        if (st.hasQuestItems(ALTRAN_RECOMMENDATION_1))
                            htmltext = "30210-01.htm"
                        else if (st.hasQuestItems(NORMAN_RECEIPT))
                            htmltext = "30210-05.htm"
                        else if (st.hasQuestItems(DUNING_INSTRUCTIONS))
                            htmltext = "30210-06.htm"
                        else if (st.getQuestItemsCount(DUNING_KEY) == 30) {
                            htmltext = "30210-07.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(DUNING_KEY, -1)
                        } else if (st.hasQuestItems(NORMAN_LIST)) {
                            if (st.getQuestItemsCount(GRAY_BONE_POWDER) == 70 && st.getQuestItemsCount(GRANITE_WHETSTONE) == 70 && st.getQuestItemsCount(
                                    RED_PIGMENT
                                ) == 70 && st.getQuestItemsCount(BRAIDED_YARN) == 70
                            ) {
                                htmltext = "30210-12.htm"
                                st.takeItems(NORMAN_INSTRUCTIONS, 1)
                                st.takeItems(NORMAN_LIST, 1)
                                st.takeItems(BRAIDED_YARN, -1)
                                st.takeItems(GRANITE_WHETSTONE, -1)
                                st.takeItems(GRAY_BONE_POWDER, -1)
                                st.takeItems(RED_PIGMENT, -1)
                                st.giveItems(JOURNEYMAN_GEM, 7)

                                if (st.getQuestItemsCount(JOURNEYMAN_DECO_BEADS) == 7) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            } else
                                htmltext = "30210-11.htm"
                        }
                    }

                    DUNING -> if (cond == 5) {
                        if (st.hasQuestItems(NORMAN_RECEIPT))
                            htmltext = "30688-01.htm"
                        else if (st.hasQuestItems(DUNING_INSTRUCTIONS)) {
                            if (st.getQuestItemsCount(DUNING_KEY) < 30)
                                htmltext = "30688-03.htm"
                            else {
                                htmltext = "30688-04.htm"
                                st.playSound(QuestState.SOUND_ITEMGET)
                                st.takeItems(DUNING_INSTRUCTIONS, 1)
                            }
                        } else
                            htmltext = "30688-05.htm"
                    }

                    PINTER -> if (cond == 5) {
                        if (st.hasQuestItems(ALTRAN_RECOMMENDATION_2))
                            htmltext = if (player.level < 36) "30298-01.htm" else "30298-02.htm"
                        else if (st.hasQuestItems(PINTER_INSTRUCTIONS)) {
                            if (st.getQuestItemsCount(AMBER_BEAD) < 70)
                                htmltext = "30298-06.htm"
                            else {
                                htmltext = "30298-07.htm"
                                st.takeItems(AMBER_BEAD, -1)
                                st.takeItems(PINTER_INSTRUCTIONS, 1)
                                st.giveItems(JOURNEYMAN_DECO_BEADS, 7)

                                if (st.getQuestItemsCount(JOURNEYMAN_GEM) == 7) {
                                    st["cond"] = "6"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            }
                        }
                    } else if (st.hasQuestItems(JOURNEYMAN_DECO_BEADS))
                        htmltext = "30298-08.htm"
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
            MANDRAGORA_SPROUT, MANDRAGORA_SAPLING, MANDRAGORA_BLOSSOM -> if (st.getInt("cond") == 3 && st.dropItemsAlways(
                    MANDRAGORA_BERRY,
                    1,
                    1
                )
            )
                st["cond"] = "4"

            BREKA_ORC_WARRIOR, BREKA_ORC_OVERLORD, BREKA_ORC_SHAMAN -> if (st.hasQuestItems(DUNING_INSTRUCTIONS))
                st.dropItemsAlways(DUNING_KEY, 1, 30)

            GHOUL, STRAIN -> if (st.hasQuestItems(NORMAN_LIST))
                st.dropItemsAlways(GRAY_BONE_POWDER, 5, 70)

            GRANITE_GOLEM -> if (st.hasQuestItems(NORMAN_LIST))
                st.dropItemsAlways(GRANITE_WHETSTONE, 7, 70)

            DEAD_SEEKER -> if (st.hasQuestItems(NORMAN_LIST))
                st.dropItemsAlways(RED_PIGMENT, 7, 70)

            SILENOS -> if (st.hasQuestItems(NORMAN_LIST))
                st.dropItemsAlways(BRAIDED_YARN, 10, 70)

            ANT, ANT_CAPTAIN -> if (st.hasQuestItems(PINTER_INSTRUCTIONS)) {
                // Different cases if player is a wannabe BH or WS.
                if (st.dropItemsAlways(
                        AMBER_BEAD,
                        if (player!!.classId == ClassId.SCAVENGER && npc.spoilerId == player.objectId) 10 else 5,
                        70
                    )
                )
                    if (player.classId == ClassId.ARTISAN && Rnd.nextBoolean())
                        st.giveItems(AMBER_LUMP, 1)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q216_TrialOfTheGuildsman"

        // Items
        private val RECIPE_JOURNEYMAN_RING = 3024
        private val RECIPE_AMBER_BEAD = 3025
        private val VALKON_RECOMMENDATION = 3120
        private val MANDRAGORA_BERRY = 3121
        private val ALTRAN_INSTRUCTIONS = 3122
        private val ALTRAN_RECOMMENDATION_1 = 3123
        private val ALTRAN_RECOMMENDATION_2 = 3124
        private val NORMAN_INSTRUCTIONS = 3125
        private val NORMAN_RECEIPT = 3126
        private val DUNING_INSTRUCTIONS = 3127
        private val DUNING_KEY = 3128
        private val NORMAN_LIST = 3129
        private val GRAY_BONE_POWDER = 3130
        private val GRANITE_WHETSTONE = 3131
        private val RED_PIGMENT = 3132
        private val BRAIDED_YARN = 3133
        private val JOURNEYMAN_GEM = 3134
        private val PINTER_INSTRUCTIONS = 3135
        private val AMBER_BEAD = 3136
        private val AMBER_LUMP = 3137
        private val JOURNEYMAN_DECO_BEADS = 3138
        private val JOURNEYMAN_RING = 3139

        // Rewards
        private val MARK_OF_GUILDSMAN = 3119
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val VALKON = 30103
        private val NORMAN = 30210
        private val ALTRAN = 30283
        private val PINTER = 30298
        private val DUNING = 30688

        // Monsters
        private val ANT = 20079
        private val ANT_CAPTAIN = 20080
        private val GRANITE_GOLEM = 20083
        private val MANDRAGORA_SPROUT = 20154
        private val MANDRAGORA_SAPLING = 20155
        private val MANDRAGORA_BLOSSOM = 20156
        private val SILENOS = 20168
        private val STRAIN = 20200
        private val GHOUL = 20201
        private val DEAD_SEEKER = 20202
        private val BREKA_ORC_SHAMAN = 20269
        private val BREKA_ORC_OVERLORD = 20270
        private val BREKA_ORC_WARRIOR = 20271
    }
}