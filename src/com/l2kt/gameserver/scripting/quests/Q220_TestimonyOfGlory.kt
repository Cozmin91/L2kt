package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q220_TestimonyOfGlory : Quest(220, "Testimony Of Glory") {
    init {

        setItemsIds(
            VOKIAN_ORDER_1,
            MANASHEN_SHARD,
            TYRANT_TALON,
            GUARDIAN_BASILISK_FANG,
            VOKIAN_ORDER_2,
            NECKLACE_OF_AUTHORITY,
            CHIANTA_ORDER_1,
            SCEPTER_OF_BREKA,
            SCEPTER_OF_ENKU,
            SCEPTER_OF_VUKU,
            SCEPTER_OF_TUREK,
            SCEPTER_OF_TUNATH,
            CHIANTA_ORDER_2,
            CHIANTA_ORDER_3,
            TAMLIN_ORC_SKULL,
            TIMAK_ORC_HEAD,
            SCEPTER_BOX,
            PASHIKA_HEAD,
            VULTUS_HEAD,
            GLOVE_OF_VOLTAR,
            ENKU_OVERLORD_HEAD,
            GLOVE_OF_KEPRA,
            MAKUM_BUGBEAR_HEAD,
            GLOVE_OF_BURAI,
            MANAKIA_LETTER_1,
            MANAKIA_LETTER_2,
            KASMAN_LETTER_1,
            KASMAN_LETTER_2,
            KASMAN_LETTER_3,
            DRIKO_CONTRACT,
            STAKATO_DRONE_HUSK,
            TANAPI_ORDER,
            SCEPTER_OF_TANTOS,
            RITUAL_BOX
        )

        addStartNpc(VOKIAN)
        addTalkId(KASMAN, VOKIAN, MANAKIA, KAKAI, TANAPI, VOLTAR, KEPRA, BURAI, HARAK, DRIKO, CHIANTA)

        addAttackId(RAGNA_ORC_OVERLORD, RAGNA_ORC_SEER, REVENANT_OF_TANTOS_CHIEF)
        addKillId(
            TYRANT,
            MARSH_STAKATO_DRONE,
            GUARDIAN_BASILISK,
            MANASHEN_GARGOYLE,
            TIMAK_ORC,
            TIMAK_ORC_ARCHER,
            TIMAK_ORC_SOLDIER,
            TIMAK_ORC_WARRIOR,
            TIMAK_ORC_SHAMAN,
            TIMAK_ORC_OVERLORD,
            TAMLIN_ORC,
            TAMLIN_ORC_ARCHER,
            RAGNA_ORC_OVERLORD,
            RAGNA_ORC_SEER,
            PASHIKA_SON_OF_VOLTAR,
            VULTUS_SON_OF_VOLTAR,
            ENKU_ORC_OVERLORD,
            MAKUM_BUGBEAR_THUG,
            REVENANT_OF_TANTOS_CHIEF
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // VOKIAN
        if (event.equals("30514-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(VOKIAN_ORDER_1, 1)

            if (!player.memos.getBool("secondClassChange37", false)) {
                htmltext = "30514-05a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_37[player.race.ordinal] ?: 0)
                player.memos.set("secondClassChange37", true)
            }
        } else if (event.equals("30642-03.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(VOKIAN_ORDER_2, 1)
            st.giveItems(CHIANTA_ORDER_1, 1)
        } else if (event.equals("30642-07.htm", ignoreCase = true)) {
            st.takeItems(CHIANTA_ORDER_1, 1)
            st.takeItems(KASMAN_LETTER_1, 1)
            st.takeItems(MANAKIA_LETTER_1, 1)
            st.takeItems(MANAKIA_LETTER_2, 1)
            st.takeItems(SCEPTER_OF_BREKA, 1)
            st.takeItems(SCEPTER_OF_ENKU, 1)
            st.takeItems(SCEPTER_OF_TUNATH, 1)
            st.takeItems(SCEPTER_OF_TUREK, 1)
            st.takeItems(SCEPTER_OF_VUKU, 1)

            if (player.level >= 37) {
                st["cond"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(CHIANTA_ORDER_3, 1)
            } else {
                htmltext = "30642-06.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(CHIANTA_ORDER_2, 1)
            }
        } else if (event.equals("30501-02.htm", ignoreCase = true) && !st.hasQuestItems(SCEPTER_OF_VUKU)) {
            if (st.hasQuestItems(KASMAN_LETTER_1))
                htmltext = "30501-04.htm"
            else {
                htmltext = "30501-03.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(KASMAN_LETTER_1, 1)
            }
            st.addRadar(-2150, 124443, -3724)
        } else if (event.equals("30501-05.htm", ignoreCase = true) && !st.hasQuestItems(SCEPTER_OF_TUREK)) {
            if (st.hasQuestItems(KASMAN_LETTER_2))
                htmltext = "30501-07.htm"
            else {
                htmltext = "30501-06.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(KASMAN_LETTER_2, 1)
            }
            st.addRadar(-94294, 110818, -3563)
        } else if (event.equals("30501-08.htm", ignoreCase = true) && !st.hasQuestItems(SCEPTER_OF_TUNATH)) {
            if (st.hasQuestItems(KASMAN_LETTER_3))
                htmltext = "30501-10.htm"
            else {
                htmltext = "30501-09.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(KASMAN_LETTER_3, 1)
            }
            st.addRadar(-55217, 200628, -3724)
        } else if (event.equals("30515-02.htm", ignoreCase = true) && !st.hasQuestItems(SCEPTER_OF_BREKA)) {
            if (st.hasQuestItems(MANAKIA_LETTER_1))
                htmltext = "30515-04.htm"
            else {
                htmltext = "30515-03.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(MANAKIA_LETTER_1, 1)
            }
            st.addRadar(80100, 119991, -2264)
        } else if (event.equals("30515-05.htm", ignoreCase = true) && !st.hasQuestItems(SCEPTER_OF_ENKU)) {
            if (st.hasQuestItems(MANAKIA_LETTER_2))
                htmltext = "30515-07.htm"
            else {
                htmltext = "30515-06.htm"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.giveItems(MANAKIA_LETTER_2, 1)
            }
            st.addRadar(19815, 189703, -3032)
        } else if (event.equals("30615-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(MANAKIA_LETTER_1, 1)
            st.giveItems(GLOVE_OF_VOLTAR, 1)

            if (!_sonsOfVoltar) {
                addSpawn(PASHIKA_SON_OF_VOLTAR, 80117, 120039, -2259, 0, false, 200000, true)
                addSpawn(VULTUS_SON_OF_VOLTAR, 80058, 120038, -2259, 0, false, 200000, true)
                _sonsOfVoltar = true

                // Resets Sons Of Voltar
                startQuestTimer("voltar_sons_cleanup", 201000, null, player, false)
            }
        } else if (event.equals("30616-05.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(MANAKIA_LETTER_2, 1)
            st.giveItems(GLOVE_OF_KEPRA, 1)

            if (!_enkuOrcOverlords) {
                addSpawn(ENKU_ORC_OVERLORD, 19894, 189743, -3074, 0, false, 200000, true)
                addSpawn(ENKU_ORC_OVERLORD, 19869, 189800, -3059, 0, false, 200000, true)
                addSpawn(ENKU_ORC_OVERLORD, 19818, 189818, -3047, 0, false, 200000, true)
                addSpawn(ENKU_ORC_OVERLORD, 19753, 189837, -3027, 0, false, 200000, true)
                _enkuOrcOverlords = true

                // Resets Enku Orc Overlords
                startQuestTimer("enku_orcs_cleanup", 201000, null, player, false)
            }
        } else if (event.equals("30617-04.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(KASMAN_LETTER_2, 1)
            st.giveItems(GLOVE_OF_BURAI, 1)

            if (!_makumBugbearThugs) {
                addSpawn(MAKUM_BUGBEAR_THUG, -94292, 110781, -3701, 0, false, 200000, true)
                addSpawn(MAKUM_BUGBEAR_THUG, -94293, 110861, -3701, 0, false, 200000, true)
                _makumBugbearThugs = true

                // Resets Makum Bugbear Thugs
                startQuestTimer("makum_bugbears_cleanup", 201000, null, player, false)
            }
        } else if (event.equals("30618-03.htm", ignoreCase = true)) {
            st.takeItems(KASMAN_LETTER_3, 1)
            st.giveItems(SCEPTER_OF_TUNATH, 1)

            if (st.hasQuestItems(SCEPTER_OF_BREKA, SCEPTER_OF_ENKU, SCEPTER_OF_VUKU, SCEPTER_OF_TUREK)) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
            } else
                st.playSound(QuestState.SOUND_ITEMGET)
        } else if (event.equals("30619-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(KASMAN_LETTER_1, 1)
            st.giveItems(DRIKO_CONTRACT, 1)
        } else if (event.equals("30571-03.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SCEPTER_BOX, 1)
            st.giveItems(TANAPI_ORDER, 1)
        } else if (event.equals("voltar_sons_cleanup", ignoreCase = true)) {
            _sonsOfVoltar = false
            return null
        } else if (event.equals("enku_orcs_cleanup", ignoreCase = true)) {
            _enkuOrcOverlords = false
            return null
        } else if (event.equals("makum_bugbears_cleanup", ignoreCase = true)) {
            _makumBugbearThugs = false
            return null
        }// Clean ups
        // TANAPI
        // DRIKO
        // HARAK
        // BURAI
        // KEPRA
        // VOLTAR
        // MANAKIA
        // KASMAN
        // CHIANTA

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30514-01.htm"
            else if (player.level < 37)
                htmltext = "30514-02.htm"
            else if (player.classId.level() != 1)
                htmltext = "30514-01a.htm"
            else
                htmltext = "30514-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VOKIAN -> if (cond == 1)
                        htmltext = "30514-06.htm"
                    else if (cond == 2) {
                        htmltext = "30514-08.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GUARDIAN_BASILISK_FANG, 10)
                        st.takeItems(MANASHEN_SHARD, 10)
                        st.takeItems(TYRANT_TALON, 10)
                        st.takeItems(VOKIAN_ORDER_1, 1)
                        st.giveItems(NECKLACE_OF_AUTHORITY, 1)
                        st.giveItems(VOKIAN_ORDER_2, 1)
                    } else if (cond == 3)
                        htmltext = "30514-09.htm"
                    else if (cond == 8)
                        htmltext = "30514-10.htm"

                    CHIANTA -> if (cond == 3)
                        htmltext = "30642-01.htm"
                    else if (cond == 4)
                        htmltext = "30642-04.htm"
                    else if (cond == 5) {
                        if (st.hasQuestItems(CHIANTA_ORDER_2)) {
                            if (player.level >= 37) {
                                htmltext = "30642-09.htm"
                                st["cond"] = "6"
                                st.playSound(QuestState.SOUND_MIDDLE)
                                st.takeItems(CHIANTA_ORDER_2, 1)
                                st.giveItems(CHIANTA_ORDER_3, 1)
                            } else
                                htmltext = "30642-08.htm"
                        } else
                            htmltext = "30642-05.htm"
                    } else if (cond == 6)
                        htmltext = "30642-10.htm"
                    else if (cond == 7) {
                        htmltext = "30642-11.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CHIANTA_ORDER_3, 1)
                        st.takeItems(NECKLACE_OF_AUTHORITY, 1)
                        st.takeItems(TAMLIN_ORC_SKULL, 20)
                        st.takeItems(TIMAK_ORC_HEAD, 20)
                        st.giveItems(SCEPTER_BOX, 1)
                    } else if (cond == 8)
                        htmltext = "30642-12.htm"
                    else if (cond > 8)
                        htmltext = "30642-13.htm"

                    KASMAN -> if (st.hasQuestItems(CHIANTA_ORDER_1))
                        htmltext = "30501-01.htm"
                    else if (cond > 4)
                        htmltext = "30501-11.htm"

                    MANAKIA -> if (st.hasQuestItems(CHIANTA_ORDER_1))
                        htmltext = "30515-01.htm"
                    else if (cond > 4)
                        htmltext = "30515-08.htm"

                    VOLTAR -> if (cond > 3) {
                        if (st.hasQuestItems(MANAKIA_LETTER_1)) {
                            htmltext = "30615-02.htm"
                            st.removeRadar(80100, 119991, -2264)
                        } else if (st.hasQuestItems(GLOVE_OF_VOLTAR)) {
                            htmltext = "30615-05.htm"
                            if (!_sonsOfVoltar) {
                                addSpawn(PASHIKA_SON_OF_VOLTAR, 80117, 120039, -2259, 0, false, 200000, true)
                                addSpawn(VULTUS_SON_OF_VOLTAR, 80058, 120038, -2259, 0, false, 200000, true)
                                _sonsOfVoltar = true

                                // Resets Sons Of Voltar
                                startQuestTimer("voltar_sons_cleanup", 201000, null, player, false)
                            }
                        } else if (st.hasQuestItems(PASHIKA_HEAD, VULTUS_HEAD)) {
                            htmltext = "30615-06.htm"
                            st.takeItems(PASHIKA_HEAD, 1)
                            st.takeItems(VULTUS_HEAD, 1)
                            st.giveItems(SCEPTER_OF_BREKA, 1)

                            if (st.hasQuestItems(
                                    SCEPTER_OF_ENKU,
                                    SCEPTER_OF_VUKU,
                                    SCEPTER_OF_TUREK,
                                    SCEPTER_OF_TUNATH
                                )
                            ) {
                                st["cond"] = "5"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else if (st.hasQuestItems(SCEPTER_OF_BREKA))
                            htmltext = "30615-07.htm"
                        else if (st.hasQuestItems(CHIANTA_ORDER_1))
                            htmltext = "30615-01.htm"
                        else if (cond < 9)
                            htmltext = "30615-08.htm"
                    }

                    KEPRA -> if (cond > 3) {
                        if (st.hasQuestItems(MANAKIA_LETTER_2)) {
                            htmltext = "30616-02.htm"
                            st.removeRadar(19815, 189703, -3032)
                        } else if (st.hasQuestItems(GLOVE_OF_KEPRA)) {
                            htmltext = "30616-05.htm"

                            if (!_enkuOrcOverlords) {
                                addSpawn(ENKU_ORC_OVERLORD, 19894, 189743, -3074, 0, false, 200000, true)
                                addSpawn(ENKU_ORC_OVERLORD, 19869, 189800, -3059, 0, false, 200000, true)
                                addSpawn(ENKU_ORC_OVERLORD, 19818, 189818, -3047, 0, false, 200000, true)
                                addSpawn(ENKU_ORC_OVERLORD, 19753, 189837, -3027, 0, false, 200000, true)
                                _enkuOrcOverlords = true

                                // Resets Enku Orc Overlords
                                startQuestTimer("enku_orcs_cleanup", 201000, null, player, false)
                            }
                        } else if (st.getQuestItemsCount(ENKU_OVERLORD_HEAD) == 4) {
                            htmltext = "30616-06.htm"
                            st.takeItems(ENKU_OVERLORD_HEAD, 4)
                            st.giveItems(SCEPTER_OF_ENKU, 1)

                            if (st.hasQuestItems(
                                    SCEPTER_OF_BREKA,
                                    SCEPTER_OF_VUKU,
                                    SCEPTER_OF_TUREK,
                                    SCEPTER_OF_TUNATH
                                )
                            ) {
                                st["cond"] = "5"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else if (st.hasQuestItems(SCEPTER_OF_ENKU))
                            htmltext = "30616-07.htm"
                        else if (st.hasQuestItems(CHIANTA_ORDER_1))
                            htmltext = "30616-01.htm"
                        else if (cond < 9)
                            htmltext = "30616-08.htm"
                    }

                    BURAI -> if (cond > 3) {
                        if (st.hasQuestItems(KASMAN_LETTER_2)) {
                            htmltext = "30617-02.htm"
                            st.removeRadar(-94294, 110818, -3563)
                        } else if (st.hasQuestItems(GLOVE_OF_BURAI)) {
                            htmltext = "30617-04.htm"

                            if (!_makumBugbearThugs) {
                                addSpawn(MAKUM_BUGBEAR_THUG, -94292, 110781, -3701, 0, false, 200000, true)
                                addSpawn(MAKUM_BUGBEAR_THUG, -94293, 110861, -3701, 0, false, 200000, true)
                                _makumBugbearThugs = true

                                // Resets Makum Bugbear Thugs
                                startQuestTimer("makum_bugbears_cleanup", 201000, null, player, false)
                            }
                        } else if (st.getQuestItemsCount(MAKUM_BUGBEAR_HEAD) == 2) {
                            htmltext = "30617-05.htm"
                            st.takeItems(MAKUM_BUGBEAR_HEAD, 2)
                            st.giveItems(SCEPTER_OF_TUREK, 1)

                            if (st.hasQuestItems(
                                    SCEPTER_OF_BREKA,
                                    SCEPTER_OF_VUKU,
                                    SCEPTER_OF_ENKU,
                                    SCEPTER_OF_TUNATH
                                )
                            ) {
                                st["cond"] = "5"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st.playSound(QuestState.SOUND_ITEMGET)
                        } else if (st.hasQuestItems(SCEPTER_OF_TUREK))
                            htmltext = "30617-06.htm"
                        else if (st.hasQuestItems(CHIANTA_ORDER_1))
                            htmltext = "30617-01.htm"
                        else if (cond < 8)
                            htmltext = "30617-07.htm"
                    }

                    HARAK -> if (cond > 3) {
                        if (st.hasQuestItems(KASMAN_LETTER_3)) {
                            htmltext = "30618-02.htm"
                            st.removeRadar(-55217, 200628, -3724)
                        } else if (st.hasQuestItems(SCEPTER_OF_TUNATH))
                            htmltext = "30618-04.htm"
                        else if (st.hasQuestItems(CHIANTA_ORDER_1))
                            htmltext = "30618-01.htm"
                        else if (cond < 9)
                            htmltext = "30618-05.htm"
                    }

                    DRIKO -> if (cond > 3) {
                        if (st.hasQuestItems(KASMAN_LETTER_1)) {
                            htmltext = "30619-02.htm"
                            st.removeRadar(-2150, 124443, -3724)
                        } else if (st.hasQuestItems(DRIKO_CONTRACT)) {
                            if (st.getQuestItemsCount(STAKATO_DRONE_HUSK) == 30) {
                                htmltext = "30619-05.htm"
                                st.takeItems(DRIKO_CONTRACT, 1)
                                st.takeItems(STAKATO_DRONE_HUSK, 30)
                                st.giveItems(SCEPTER_OF_VUKU, 1)

                                if (st.hasQuestItems(
                                        SCEPTER_OF_BREKA,
                                        SCEPTER_OF_TUREK,
                                        SCEPTER_OF_ENKU,
                                        SCEPTER_OF_TUNATH
                                    )
                                ) {
                                    st["cond"] = "5"
                                    st.playSound(QuestState.SOUND_MIDDLE)
                                } else
                                    st.playSound(QuestState.SOUND_ITEMGET)
                            } else
                                htmltext = "30619-04.htm"
                        } else if (st.hasQuestItems(SCEPTER_OF_VUKU))
                            htmltext = "30619-06.htm"
                        else if (st.hasQuestItems(CHIANTA_ORDER_1))
                            htmltext = "30619-01.htm"
                        else if (cond < 8)
                            htmltext = "30619-07.htm"
                    }

                    TANAPI -> if (cond == 8)
                        htmltext = "30571-01.htm"
                    else if (cond == 9)
                        htmltext = "30571-04.htm"
                    else if (cond == 10) {
                        htmltext = "30571-05.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SCEPTER_OF_TANTOS, 1)
                        st.takeItems(TANAPI_ORDER, 1)
                        st.giveItems(RITUAL_BOX, 1)
                    } else if (cond == 11)
                        htmltext = "30571-06.htm"

                    KAKAI -> if (cond > 7 && cond < 11)
                        htmltext = "30565-01.htm"
                    else if (cond == 11) {
                        htmltext = "30565-02.htm"
                        st.takeItems(RITUAL_BOX, 1)
                        st.giveItems(MARK_OF_GLORY, 1)
                        st.rewardExpAndSp(91457, 2500)
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

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            RAGNA_ORC_OVERLORD, RAGNA_ORC_SEER -> if (cond == 9 && npc.isScriptValue(0)) {
                npc.broadcastNpcSay("Is it a lackey of Kakai?!")
                npc.scriptValue = 1
            }

            REVENANT_OF_TANTOS_CHIEF -> if (cond == 9) {
                if (npc.isScriptValue(0)) {
                    npc.broadcastNpcSay("How regretful! Unjust dishonor!")
                    npc.scriptValue = 1
                } else if (npc.isScriptValue(1) && npc.currentHp / npc.maxHp < 0.33) {
                    npc.broadcastNpcSay("Indignant and unfair death!")
                    npc.scriptValue = 2
                }
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            TYRANT -> if (cond == 1 && st.dropItems(TYRANT_TALON, 1, 10, 500000) && st.getQuestItemsCount(
                    GUARDIAN_BASILISK_FANG
                ) + st.getQuestItemsCount(MANASHEN_SHARD) == 20
            )
                st["cond"] = "2"

            GUARDIAN_BASILISK -> if (cond == 1 && st.dropItems(
                    GUARDIAN_BASILISK_FANG,
                    1,
                    10,
                    500000
                ) && st.getQuestItemsCount(TYRANT_TALON) + st.getQuestItemsCount(MANASHEN_SHARD) == 20
            )
                st["cond"] = "2"

            MANASHEN_GARGOYLE -> if (cond == 1 && st.dropItems(MANASHEN_SHARD, 1, 10, 750000) && st.getQuestItemsCount(
                    TYRANT_TALON
                ) + st.getQuestItemsCount(GUARDIAN_BASILISK_FANG) == 20
            )
                st["cond"] = "2"

            MARSH_STAKATO_DRONE -> if (st.hasQuestItems(DRIKO_CONTRACT))
                st.dropItems(STAKATO_DRONE_HUSK, 1, 30, 750000)

            PASHIKA_SON_OF_VOLTAR -> if (st.hasQuestItems(GLOVE_OF_VOLTAR) && !st.hasQuestItems(PASHIKA_HEAD)) {
                st.giveItems(PASHIKA_HEAD, 1)
                if (st.hasQuestItems(VULTUS_HEAD)) {
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(GLOVE_OF_VOLTAR, 1)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            VULTUS_SON_OF_VOLTAR -> if (st.hasQuestItems(GLOVE_OF_VOLTAR) && !st.hasQuestItems(VULTUS_HEAD)) {
                st.giveItems(VULTUS_HEAD, 1)
                if (st.hasQuestItems(PASHIKA_HEAD)) {
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.takeItems(GLOVE_OF_VOLTAR, 1)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            ENKU_ORC_OVERLORD -> if (st.hasQuestItems(GLOVE_OF_KEPRA) && st.dropItemsAlways(ENKU_OVERLORD_HEAD, 1, 4))
                st.takeItems(GLOVE_OF_KEPRA, 1)

            MAKUM_BUGBEAR_THUG -> if (st.hasQuestItems(GLOVE_OF_BURAI) && st.dropItemsAlways(MAKUM_BUGBEAR_HEAD, 1, 2))
                st.takeItems(GLOVE_OF_BURAI, 1)

            TIMAK_ORC, TIMAK_ORC_ARCHER, TIMAK_ORC_SOLDIER, TIMAK_ORC_WARRIOR, TIMAK_ORC_SHAMAN, TIMAK_ORC_OVERLORD -> if (cond == 6 && st.dropItems(
                    TIMAK_ORC_HEAD,
                    1,
                    20,
                    500000 + (npc.npcId - 20583) * 100000
                ) && st.getQuestItemsCount(TAMLIN_ORC_SKULL) == 20
            )
                st["cond"] = "7"

            TAMLIN_ORC -> if (cond == 6 && st.dropItems(TAMLIN_ORC_SKULL, 1, 20, 500000) && st.getQuestItemsCount(
                    TIMAK_ORC_HEAD
                ) == 20
            )
                st["cond"] = "7"

            TAMLIN_ORC_ARCHER -> if (cond == 6 && st.dropItems(
                    TAMLIN_ORC_SKULL,
                    1,
                    20,
                    600000
                ) && st.getQuestItemsCount(TIMAK_ORC_HEAD) == 20
            )
                st["cond"] = "7"

            RAGNA_ORC_OVERLORD, RAGNA_ORC_SEER -> if (cond == 9) {
                npc.broadcastNpcSay("Too late!")
                addSpawn(REVENANT_OF_TANTOS_CHIEF, npc, true, 200000, true)
            }

            REVENANT_OF_TANTOS_CHIEF -> if (cond == 9 && st.dropItemsAlways(SCEPTER_OF_TANTOS, 1, 1)) {
                st["cond"] = "10"
                npc.broadcastNpcSay("I'll get revenge someday!!")
            }
        }

        return null
    }

    companion object {
        private val qn = "Q220_TestimonyOfGlory"

        // Items
        private val VOKIAN_ORDER_1 = 3204
        private val MANASHEN_SHARD = 3205
        private val TYRANT_TALON = 3206
        private val GUARDIAN_BASILISK_FANG = 3207
        private val VOKIAN_ORDER_2 = 3208
        private val NECKLACE_OF_AUTHORITY = 3209
        private val CHIANTA_ORDER_1 = 3210
        private val SCEPTER_OF_BREKA = 3211
        private val SCEPTER_OF_ENKU = 3212
        private val SCEPTER_OF_VUKU = 3213
        private val SCEPTER_OF_TUREK = 3214
        private val SCEPTER_OF_TUNATH = 3215
        private val CHIANTA_ORDER_2 = 3216
        private val CHIANTA_ORDER_3 = 3217
        private val TAMLIN_ORC_SKULL = 3218
        private val TIMAK_ORC_HEAD = 3219
        private val SCEPTER_BOX = 3220
        private val PASHIKA_HEAD = 3221
        private val VULTUS_HEAD = 3222
        private val GLOVE_OF_VOLTAR = 3223
        private val ENKU_OVERLORD_HEAD = 3224
        private val GLOVE_OF_KEPRA = 3225
        private val MAKUM_BUGBEAR_HEAD = 3226
        private val GLOVE_OF_BURAI = 3227
        private val MANAKIA_LETTER_1 = 3228
        private val MANAKIA_LETTER_2 = 3229
        private val KASMAN_LETTER_1 = 3230
        private val KASMAN_LETTER_2 = 3231
        private val KASMAN_LETTER_3 = 3232
        private val DRIKO_CONTRACT = 3233
        private val STAKATO_DRONE_HUSK = 3234
        private val TANAPI_ORDER = 3235
        private val SCEPTER_OF_TANTOS = 3236
        private val RITUAL_BOX = 3237

        // Rewards
        private val MARK_OF_GLORY = 3203
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val KASMAN = 30501
        private val VOKIAN = 30514
        private val MANAKIA = 30515
        private val KAKAI = 30565
        private val TANAPI = 30571
        private val VOLTAR = 30615
        private val KEPRA = 30616
        private val BURAI = 30617
        private val HARAK = 30618
        private val DRIKO = 30619
        private val CHIANTA = 30642

        // Monsters
        private val TYRANT = 20192
        private val MARSH_STAKATO_DRONE = 20234
        private val GUARDIAN_BASILISK = 20550
        private val MANASHEN_GARGOYLE = 20563
        private val TIMAK_ORC = 20583
        private val TIMAK_ORC_ARCHER = 20584
        private val TIMAK_ORC_SOLDIER = 20585
        private val TIMAK_ORC_WARRIOR = 20586
        private val TIMAK_ORC_SHAMAN = 20587
        private val TIMAK_ORC_OVERLORD = 20588
        private val TAMLIN_ORC = 20601
        private val TAMLIN_ORC_ARCHER = 20602
        private val RAGNA_ORC_OVERLORD = 20778
        private val RAGNA_ORC_SEER = 20779
        private val PASHIKA_SON_OF_VOLTAR = 27080
        private val VULTUS_SON_OF_VOLTAR = 27081
        private val ENKU_ORC_OVERLORD = 27082
        private val MAKUM_BUGBEAR_THUG = 27083
        private val REVENANT_OF_TANTOS_CHIEF = 27086

        // Checks & Instances
        private var _sonsOfVoltar = false
        private var _enkuOrcOverlords = false
        private var _makumBugbearThugs = false
    }
}