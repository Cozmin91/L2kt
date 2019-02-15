package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q233_TestOfTheWarSpirit : Quest(233, "Test of the War Spirit") {
    init {

        setItemsIds(
            VENDETTA_TOTEM,
            TAMLIN_ORC_HEAD,
            WARSPIRIT_TOTEM,
            ORIM_CONTRACT,
            PORTA_EYE,
            EXCURO_SCALE,
            MORDEO_TALON,
            BRAKI_REMAINS_1,
            PEKIRON_TOTEM,
            TONAR_SKULL,
            TONAR_RIBBONE,
            TONAR_SPINE,
            TONAR_ARMBONE,
            TONAR_THIGHBONE,
            TONAR_REMAINS_1,
            MANAKIA_TOTEM,
            HERMODT_SKULL,
            HERMODT_RIBBONE,
            HERMODT_SPINE,
            HERMODT_ARMBONE,
            HERMODT_THIGHBONE,
            HERMODT_REMAINS_1,
            RACOY_TOTEM,
            VIVYAN_LETTER,
            INSECT_DIAGRAM_BOOK,
            KIRUNA_SKULL,
            KIRUNA_RIBBONE,
            KIRUNA_SPINE,
            KIRUNA_ARMBONE,
            KIRUNA_THIGHBONE,
            KIRUNA_REMAINS_1,
            BRAKI_REMAINS_2,
            TONAR_REMAINS_2,
            HERMODT_REMAINS_2,
            KIRUNA_REMAINS_2
        )

        addStartNpc(SOMAK)
        addTalkId(SOMAK, VIVYAN, SARIEN, RACOY, MANAKIA, ORIM, ANCESTOR_MARTANKUS, PEKIRON)
        addKillId(
            NOBLE_ANT,
            NOBLE_ANT_LEADER,
            MEDUSA,
            PORTA,
            EXCURO,
            MORDEO,
            LETO_LIZARDMAN_SHAMAN,
            LETO_LIZARDMAN_OVERLORD,
            TAMLIN_ORC,
            TAMLIN_ORC_ARCHER,
            STENOA_GORGON_QUEEN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // SOMAK
        if (event.equals("30510-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30510-05e.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30630-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(ORIM_CONTRACT, 1)
        } else if (event.equals("30507-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(RACOY_TOTEM, 1)
        } else if (event.equals("30030-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(VIVYAN_LETTER, 1)
        } else if (event.equals("30682-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(PEKIRON_TOTEM, 1)
        } else if (event.equals("30515-02.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(MANAKIA_TOTEM, 1)
        } else if (event.equals("30649-03.htm", ignoreCase = true)) {
            st.takeItems(TAMLIN_ORC_HEAD, -1)
            st.takeItems(WARSPIRIT_TOTEM, -1)
            st.takeItems(BRAKI_REMAINS_2, -1)
            st.takeItems(HERMODT_REMAINS_2, -1)
            st.takeItems(KIRUNA_REMAINS_2, -1)
            st.takeItems(TONAR_REMAINS_2, -1)
            st.giveItems(MARK_OF_WARSPIRIT, 1)
            st.rewardExpAndSp(63483, 17500)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }// ANCESTOR MARTANKUS
        // MANAKIA
        // PEKIRON
        // VIVYAN
        // RACOY
        // ORIM

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId == ClassId.ORC_SHAMAN)
                htmltext = if (player.level < 39) "30510-03.htm" else "30510-04.htm"
            else
                htmltext = if (player.race == ClassRace.ORC) "30510-02.htm" else "30510-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    SOMAK -> if (cond == 1)
                        htmltext = "30510-06.htm"
                    else if (cond == 2) {
                        htmltext = "30510-07.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BRAKI_REMAINS_1, 1)
                        st.takeItems(HERMODT_REMAINS_1, 1)
                        st.takeItems(KIRUNA_REMAINS_1, 1)
                        st.takeItems(TONAR_REMAINS_1, 1)
                        st.giveItems(VENDETTA_TOTEM, 1)
                    } else if (cond == 3)
                        htmltext = "30510-08.htm"
                    else if (cond == 4) {
                        htmltext = "30510-09.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(VENDETTA_TOTEM, 1)
                        st.giveItems(BRAKI_REMAINS_2, 1)
                        st.giveItems(HERMODT_REMAINS_2, 1)
                        st.giveItems(KIRUNA_REMAINS_2, 1)
                        st.giveItems(TONAR_REMAINS_2, 1)
                        st.giveItems(WARSPIRIT_TOTEM, 1)
                    } else if (cond == 5)
                        htmltext = "30510-10.htm"

                    ORIM -> if (cond == 1 && !st.hasQuestItems(BRAKI_REMAINS_1)) {
                        if (!st.hasQuestItems(ORIM_CONTRACT))
                            htmltext = "30630-01.htm"
                        else if (st.getQuestItemsCount(PORTA_EYE) + st.getQuestItemsCount(EXCURO_SCALE) + st.getQuestItemsCount(
                                MORDEO_TALON
                            ) == 30
                        ) {
                            htmltext = "30630-06.htm"
                            st.takeItems(EXCURO_SCALE, 10)
                            st.takeItems(MORDEO_TALON, 10)
                            st.takeItems(PORTA_EYE, 10)
                            st.takeItems(ORIM_CONTRACT, 1)
                            st.giveItems(BRAKI_REMAINS_1, 1)

                            if (st.hasQuestItems(HERMODT_REMAINS_1, KIRUNA_REMAINS_1, TONAR_REMAINS_1)) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30630-05.htm"
                    } else
                        htmltext = "30630-07.htm"

                    RACOY -> if (cond == 1 && !st.hasQuestItems(KIRUNA_REMAINS_1)) {
                        if (!st.hasQuestItems(RACOY_TOTEM))
                            htmltext = "30507-01.htm"
                        else if (st.hasQuestItems(VIVYAN_LETTER))
                            htmltext = "30507-04.htm"
                        else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK)) {
                            if (st.hasQuestItems(
                                    KIRUNA_ARMBONE,
                                    KIRUNA_RIBBONE,
                                    KIRUNA_SKULL,
                                    KIRUNA_SPINE,
                                    KIRUNA_THIGHBONE
                                )
                            ) {
                                htmltext = "30507-06.htm"
                                st.takeItems(INSECT_DIAGRAM_BOOK, 1)
                                st.takeItems(RACOY_TOTEM, 1)
                                st.takeItems(KIRUNA_ARMBONE, 1)
                                st.takeItems(KIRUNA_RIBBONE, 1)
                                st.takeItems(KIRUNA_SKULL, 1)
                                st.takeItems(KIRUNA_SPINE, 1)
                                st.takeItems(KIRUNA_THIGHBONE, 1)
                                st.giveItems(KIRUNA_REMAINS_1, 1)

                                if (st.hasQuestItems(BRAKI_REMAINS_1, HERMODT_REMAINS_1, TONAR_REMAINS_1)) {
                                    st["cond"] = "2"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            } else
                                htmltext = "30507-05.htm"
                        } else
                            htmltext = "30507-03.htm"
                    } else
                        htmltext = "30507-07.htm"

                    VIVYAN -> if (cond == 1 && st.hasQuestItems(RACOY_TOTEM)) {
                        if (st.hasQuestItems(VIVYAN_LETTER))
                            htmltext = "30030-05.htm"
                        else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
                            htmltext = "30030-06.htm"
                        else
                            htmltext = "30030-01.htm"
                    } else
                        htmltext = "30030-07.htm"

                    SARIEN -> if (cond == 1 && st.hasQuestItems(RACOY_TOTEM)) {
                        if (st.hasQuestItems(VIVYAN_LETTER)) {
                            htmltext = "30436-01.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.takeItems(VIVYAN_LETTER, 1)
                            st.giveItems(INSECT_DIAGRAM_BOOK, 1)
                        } else if (st.hasQuestItems(INSECT_DIAGRAM_BOOK))
                            htmltext = "30436-02.htm"
                    } else
                        htmltext = "30436-03.htm"

                    PEKIRON -> if (cond == 1 && !st.hasQuestItems(TONAR_REMAINS_1)) {
                        if (!st.hasQuestItems(PEKIRON_TOTEM))
                            htmltext = "30682-01.htm"
                        else if (st.hasQuestItems(
                                TONAR_ARMBONE,
                                TONAR_RIBBONE,
                                TONAR_SKULL,
                                TONAR_SPINE,
                                TONAR_THIGHBONE
                            )
                        ) {
                            htmltext = "30682-04.htm"
                            st.takeItems(PEKIRON_TOTEM, 1)
                            st.takeItems(TONAR_ARMBONE, 1)
                            st.takeItems(TONAR_RIBBONE, 1)
                            st.takeItems(TONAR_SKULL, 1)
                            st.takeItems(TONAR_SPINE, 1)
                            st.takeItems(TONAR_THIGHBONE, 1)
                            st.giveItems(TONAR_REMAINS_1, 1)

                            if (st.hasQuestItems(BRAKI_REMAINS_1, HERMODT_REMAINS_1, KIRUNA_REMAINS_1)) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30682-03.htm"
                    } else
                        htmltext = "30682-05.htm"

                    MANAKIA -> if (cond == 1 && !st.hasQuestItems(HERMODT_REMAINS_1)) {
                        if (!st.hasQuestItems(MANAKIA_TOTEM))
                            htmltext = "30515-01.htm"
                        else if (st.hasQuestItems(
                                HERMODT_ARMBONE,
                                HERMODT_RIBBONE,
                                HERMODT_SKULL,
                                HERMODT_SPINE,
                                HERMODT_THIGHBONE
                            )
                        ) {
                            htmltext = "30515-04.htm"
                            st.takeItems(MANAKIA_TOTEM, 1)
                            st.takeItems(HERMODT_ARMBONE, 1)
                            st.takeItems(HERMODT_RIBBONE, 1)
                            st.takeItems(HERMODT_SKULL, 1)
                            st.takeItems(HERMODT_SPINE, 1)
                            st.takeItems(HERMODT_THIGHBONE, 1)
                            st.giveItems(HERMODT_REMAINS_1, 1)

                            if (st.hasQuestItems(BRAKI_REMAINS_1, KIRUNA_REMAINS_1, TONAR_REMAINS_1)) {
                                st["cond"] = "2"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "30515-03.htm"
                    } else
                        htmltext = "30515-05.htm"

                    ANCESTOR_MARTANKUS -> if (cond == 5)
                        htmltext = "30649-01.htm"
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
            PORTA -> if (st.hasQuestItems(ORIM_CONTRACT))
                st.dropItemsAlways(PORTA_EYE, 1, 10)

            EXCURO -> if (st.hasQuestItems(ORIM_CONTRACT))
                st.dropItemsAlways(EXCURO_SCALE, 1, 10)

            MORDEO -> if (st.hasQuestItems(ORIM_CONTRACT))
                st.dropItemsAlways(MORDEO_TALON, 1, 10)

            NOBLE_ANT, NOBLE_ANT_LEADER -> if (st.hasQuestItems(INSECT_DIAGRAM_BOOK)) {
                val rndAnt = Rnd[100]
                if (rndAnt > 70) {
                    if (st.hasQuestItems(KIRUNA_THIGHBONE))
                        st.dropItemsAlways(KIRUNA_ARMBONE, 1, 1)
                    else
                        st.dropItemsAlways(KIRUNA_THIGHBONE, 1, 1)
                } else if (rndAnt > 40) {
                    if (st.hasQuestItems(KIRUNA_SPINE))
                        st.dropItemsAlways(KIRUNA_RIBBONE, 1, 1)
                    else
                        st.dropItemsAlways(KIRUNA_SPINE, 1, 1)
                } else if (rndAnt > 10)
                    st.dropItemsAlways(KIRUNA_SKULL, 1, 1)
            }

            LETO_LIZARDMAN_SHAMAN, LETO_LIZARDMAN_OVERLORD -> if (st.hasQuestItems(PEKIRON_TOTEM) && Rnd.nextBoolean()) {
                if (!st.hasQuestItems(TONAR_SKULL))
                    st.dropItemsAlways(TONAR_SKULL, 1, 1)
                else if (!st.hasQuestItems(TONAR_RIBBONE))
                    st.dropItemsAlways(TONAR_RIBBONE, 1, 1)
                else if (!st.hasQuestItems(TONAR_SPINE))
                    st.dropItemsAlways(TONAR_SPINE, 1, 1)
                else if (!st.hasQuestItems(TONAR_ARMBONE))
                    st.dropItemsAlways(TONAR_ARMBONE, 1, 1)
                else
                    st.dropItemsAlways(TONAR_THIGHBONE, 1, 1)
            }

            MEDUSA -> if (st.hasQuestItems(MANAKIA_TOTEM) && Rnd.nextBoolean()) {
                if (!st.hasQuestItems(HERMODT_RIBBONE))
                    st.dropItemsAlways(HERMODT_RIBBONE, 1, 1)
                else if (!st.hasQuestItems(HERMODT_SPINE))
                    st.dropItemsAlways(HERMODT_SPINE, 1, 1)
                else if (!st.hasQuestItems(HERMODT_ARMBONE))
                    st.dropItemsAlways(HERMODT_ARMBONE, 1, 1)
                else
                    st.dropItemsAlways(HERMODT_THIGHBONE, 1, 1)
            }

            STENOA_GORGON_QUEEN -> if (st.hasQuestItems(MANAKIA_TOTEM))
                st.dropItemsAlways(HERMODT_SKULL, 1, 1)

            TAMLIN_ORC, TAMLIN_ORC_ARCHER -> if (st.hasQuestItems(VENDETTA_TOTEM) && st.dropItems(
                    TAMLIN_ORC_HEAD,
                    1,
                    13,
                    500000
                )
            )
                st["cond"] = "4"
        }

        return null
    }

    companion object {
        private val qn = "Q233_TestOfTheWarSpirit"

        // Items
        private val VENDETTA_TOTEM = 2880
        private val TAMLIN_ORC_HEAD = 2881
        private val WARSPIRIT_TOTEM = 2882
        private val ORIM_CONTRACT = 2883
        private val PORTA_EYE = 2884
        private val EXCURO_SCALE = 2885
        private val MORDEO_TALON = 2886
        private val BRAKI_REMAINS_1 = 2887
        private val PEKIRON_TOTEM = 2888
        private val TONAR_SKULL = 2889
        private val TONAR_RIBBONE = 2890
        private val TONAR_SPINE = 2891
        private val TONAR_ARMBONE = 2892
        private val TONAR_THIGHBONE = 2893
        private val TONAR_REMAINS_1 = 2894
        private val MANAKIA_TOTEM = 2895
        private val HERMODT_SKULL = 2896
        private val HERMODT_RIBBONE = 2897
        private val HERMODT_SPINE = 2898
        private val HERMODT_ARMBONE = 2899
        private val HERMODT_THIGHBONE = 2900
        private val HERMODT_REMAINS_1 = 2901
        private val RACOY_TOTEM = 2902
        private val VIVYAN_LETTER = 2903
        private val INSECT_DIAGRAM_BOOK = 2904
        private val KIRUNA_SKULL = 2905
        private val KIRUNA_RIBBONE = 2906
        private val KIRUNA_SPINE = 2907
        private val KIRUNA_ARMBONE = 2908
        private val KIRUNA_THIGHBONE = 2909
        private val KIRUNA_REMAINS_1 = 2910
        private val BRAKI_REMAINS_2 = 2911
        private val TONAR_REMAINS_2 = 2912
        private val HERMODT_REMAINS_2 = 2913
        private val KIRUNA_REMAINS_2 = 2914

        // Rewards
        private val MARK_OF_WARSPIRIT = 2879
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val VIVYAN = 30030
        private val SARIEN = 30436
        private val RACOY = 30507
        private val SOMAK = 30510
        private val MANAKIA = 30515
        private val ORIM = 30630
        private val ANCESTOR_MARTANKUS = 30649
        private val PEKIRON = 30682

        // Monsters
        private val NOBLE_ANT = 20089
        private val NOBLE_ANT_LEADER = 20090
        private val MEDUSA = 20158
        private val PORTA = 20213
        private val EXCURO = 20214
        private val MORDEO = 20215
        private val LETO_LIZARDMAN_SHAMAN = 20581
        private val LETO_LIZARDMAN_OVERLORD = 20582
        private val TAMLIN_ORC = 20601
        private val TAMLIN_ORC_ARCHER = 20602
        private val STENOA_GORGON_QUEEN = 27108
    }
}