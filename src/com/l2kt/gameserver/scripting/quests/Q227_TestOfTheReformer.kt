package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q227_TestOfTheReformer : Quest(227, "Test Of The Reformer") {
    init {

        setItemsIds(
            BOOK_OF_REFORM,
            LETTER_OF_INTRODUCTION,
            SLA_LETTER,
            GREETINGS,
            OL_MAHUM_MONEY,
            KATARI_LETTER,
            NYAKURI_LETTER,
            UNDEAD_LIST,
            RAMUS_LETTER,
            RIPPED_DIARY,
            HUGE_NAIL,
            LETTER_OF_BETRAYER,
            BONE_FRAGMENT_4,
            BONE_FRAGMENT_5,
            BONE_FRAGMENT_6,
            BONE_FRAGMENT_7,
            BONE_FRAGMENT_8,
            BONE_FRAGMENT_9,
            KAKAN_LETTER
        )

        addStartNpc(PUPINA)
        addTalkId(PUPINA, SLA, RAMUS, KATARI, KAKAN, NYAKURI, OL_MAHUM_PILGRIM)

        addAttackId(NAMELESS_REVENANT, CRIMSON_WEREWOLF)
        addKillId(
            MISERY_SKELETON,
            SKELETON_ARCHER,
            SKELETON_MARKSMAN,
            SKELETON_LORD,
            SILENT_HORROR,
            NAMELESS_REVENANT,
            ARURAUNE,
            OL_MAHUM_INSPECTOR,
            OL_MAHUM_BETRAYER,
            CRIMSON_WEREWOLF,
            KRUDEL_LIZARDMAN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // PUPINA
        if (event.equals("30118-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(BOOK_OF_REFORM, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30118-04b.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30118-06.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BOOK_OF_REFORM, 1)
            st.takeItems(HUGE_NAIL, 1)
            st.giveItems(LETTER_OF_INTRODUCTION, 1)
        } else if (event.equals("30666-04.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_OF_INTRODUCTION, 1)
            st.giveItems(SLA_LETTER, 1)
        } else if (event.equals("30669-03.htm", ignoreCase = true)) {
            if (st.getInt("cond") != 12) {
                st["cond"] = "12"
                st.playSound(QuestState.SOUND_MIDDLE)
            }

            if (!_crimsonWerewolf) {
                addSpawn(CRIMSON_WEREWOLF, -9382, -89852, -2333, 0, false, 299000, true)
                _crimsonWerewolf = true

                // Resets Crimson Werewolf
                startQuestTimer("werewolf_cleanup", 300000, null, player, false)
            }
        } else if (event.equals("30670-03.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
            if (!_krudelLizardman) {
                addSpawn(KRUDEL_LIZARDMAN, 126019, -179983, -1781, 0, false, 299000, true)
                _krudelLizardman = true

                // Resets Krudel Lizardman
                startQuestTimer("lizardman_cleanup", 300000, null, player, false)
            }
        } else if (event.equals("werewolf_despawn", ignoreCase = true)) {
            npc!!.abortAttack()
            npc.broadcastNpcSay("Cowardly guy!")
            npc.decayMe()
            _crimsonWerewolf = false
            cancelQuestTimer("werewolf_cleanup", null, player)
            return null
        } else if (event.equals("ol_mahums_despawn", ignoreCase = true)) {
            _timer++

            if (st.getInt("cond") == 8 || _timer >= 60) {
                if (_olMahumPilgrim != null) {
                    _olMahumPilgrim!!.deleteMe()
                    _olMahumPilgrim = null
                }

                if (_olMahumInspector != null) {
                    _olMahumInspector!!.deleteMe()
                    _olMahumInspector = null
                }
                cancelQuestTimer("ol_mahums_despawn", null, player)
                _timer = 0
            }

            return null
        } else if (event.equals("betrayer_despawn", ignoreCase = true)) {
            if (_olMahumBetrayer != null) {
                _olMahumBetrayer!!.deleteMe()
                _olMahumBetrayer = null
            }

            return null
        } else if (event.equals("werewolf_cleanup", ignoreCase = true)) {
            _crimsonWerewolf = false
            return null
        } else if (event.equals("lizardman_cleanup", ignoreCase = true)) {
            _krudelLizardman = false
            return null
        }// Clean ups
        // Despawns
        // Despawns Crimson Werewolf
        // NYAKURI
        // KAKAN
        // SLA

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId == ClassId.CLERIC || player.classId == ClassId.SHILLIEN_ORACLE)
                htmltext = if (player.level < 39) "30118-01.htm" else "30118-03.htm"
            else
                htmltext = "30118-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    PUPINA -> if (cond < 3)
                        htmltext = "30118-04a.htm"
                    else if (cond == 3)
                        htmltext = "30118-05.htm"
                    else if (cond > 3)
                        htmltext = "30118-07.htm"

                    SLA -> if (cond == 4)
                        htmltext = "30666-01.htm"
                    else if (cond > 4 && cond < 10)
                        htmltext = "30666-05.htm"
                    else if (cond == 10) {
                        htmltext = "30666-06.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(OL_MAHUM_MONEY, 1)
                        st.giveItems(GREETINGS, 3)
                    } else if (cond > 10 && cond < 20)
                        htmltext = "30666-06.htm"
                    else if (cond == 20) {
                        htmltext = "30666-07.htm"
                        st.takeItems(KATARI_LETTER, 1)
                        st.takeItems(KAKAN_LETTER, 1)
                        st.takeItems(NYAKURI_LETTER, 1)
                        st.takeItems(RAMUS_LETTER, 1)
                        st.giveItems(MARK_OF_REFORMER, 1)
                        st.rewardExpAndSp(164032, 17500)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    KATARI -> if (cond == 5 || cond == 6) {
                        htmltext = "30668-01.htm"

                        if (cond == 5) {
                            st["cond"] = "6"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(SLA_LETTER, 1)
                        }

                        if (_olMahumPilgrim == null && _olMahumInspector == null) {
                            _olMahumPilgrim = addSpawn(OL_MAHUM_PILGRIM, -4015, 40141, -3664, 0, false, 0, true)
                            _olMahumInspector = addSpawn(OL_MAHUM_INSPECTOR, -4034, 40201, -3665, 0, false, 0, true)

                            // Resets Ol Mahums' instances
                            startQuestTimer("ol_mahums_despawn", 5000, null, player, true)

                            (_olMahumInspector as Attackable).addDamageHate(_olMahumPilgrim, 0, 99999)
                            _olMahumInspector!!.ai.setIntention(CtrlIntention.ATTACK, _olMahumPilgrim)

                            // TODO : make L2Npc be able to attack L2Attackable.
                            // ((L2Attackable) _olMahumPilgrim).addDamageHate(_olMahumInspector, 0, 99999);
                            // _olMahumPilgrim.getAi().setIntention(CtrlIntention.ATTACK, _olMahumInspector);
                        }
                    } else if (cond == 7) {
                        htmltext = "30668-01.htm"

                        if (_olMahumPilgrim == null) {
                            _olMahumPilgrim = addSpawn(OL_MAHUM_PILGRIM, -4015, 40141, -3664, 0, false, 0, true)

                            // Resets Ol Mahums' instances
                            startQuestTimer("ol_mahums_despawn", 5000, null, player, true)
                        }
                    } else if (cond == 8) {
                        htmltext = "30668-02.htm"

                        if (_olMahumBetrayer == null) {
                            _olMahumBetrayer = addSpawn(OL_MAHUM_BETRAYER, -4106, 40174, -3660, 0, false, 0, true)
                            _olMahumBetrayer!!.setRunning()
                            _olMahumBetrayer!!.ai.setIntention(CtrlIntention.MOVE_TO, Location(-7732, 36787, -3709))

                            // Resets Ol Mahum Betrayer's instance
                            startQuestTimer("betrayer_despawn", 40000, null, player, false)
                        }
                    } else if (cond == 9) {
                        htmltext = "30668-03.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LETTER_OF_BETRAYER, 1)
                        st.giveItems(KATARI_LETTER, 1)
                    } else if (cond > 9)
                        htmltext = "30668-04.htm"

                    OL_MAHUM_PILGRIM -> if (cond == 7) {
                        htmltext = "30732-01.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(OL_MAHUM_MONEY, 1)
                    }

                    KAKAN -> if (cond == 11 || cond == 12)
                        htmltext = "30669-01.htm"
                    else if (cond == 13) {
                        htmltext = "30669-04.htm"
                        st["cond"] = "14"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GREETINGS, 1)
                        st.giveItems(KAKAN_LETTER, 1)
                    } else if (cond > 13)
                        htmltext = "30669-04.htm"

                    NYAKURI -> if (cond == 14 || cond == 15)
                        htmltext = "30670-01.htm"
                    else if (cond == 16) {
                        htmltext = "30670-04.htm"
                        st["cond"] = "17"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GREETINGS, 1)
                        st.giveItems(NYAKURI_LETTER, 1)
                    } else if (cond > 16)
                        htmltext = "30670-04.htm"

                    RAMUS -> if (cond == 17) {
                        htmltext = "30667-01.htm"
                        st["cond"] = "18"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GREETINGS, 1)
                        st.giveItems(UNDEAD_LIST, 1)
                    } else if (cond == 18)
                        htmltext = "30667-02.htm"
                    else if (cond == 19) {
                        htmltext = "30667-03.htm"
                        st["cond"] = "20"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BONE_FRAGMENT_4, 1)
                        st.takeItems(BONE_FRAGMENT_5, 1)
                        st.takeItems(BONE_FRAGMENT_6, 1)
                        st.takeItems(BONE_FRAGMENT_7, 1)
                        st.takeItems(BONE_FRAGMENT_8, 1)
                        st.takeItems(UNDEAD_LIST, 1)
                        st.giveItems(RAMUS_LETTER, 1)
                    } else if (cond > 19)
                        htmltext = "30667-03.htm"
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
            NAMELESS_REVENANT -> if ((cond == 1 || cond == 2) && skill != null && skill.id == 1031)
                npc.scriptValue = 1

            CRIMSON_WEREWOLF -> if (cond == 12 && !npc.isScriptValue(1) && (skill == null || !ArraysUtil.contains(
                    ALLOWED_SKILLS,
                    skill.id
                ))
            ) {
                npc.scriptValue = 1
                startQuestTimer("werewolf_despawn", 1000, npc, player, false)
            }
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")
        when (npc.npcId) {
            NAMELESS_REVENANT -> if ((cond == 1 || cond == 2) && npc.isScriptValue(1) && st.dropItemsAlways(
                    RIPPED_DIARY,
                    1,
                    7
                )
            ) {
                st["cond"] = "2"
                st.takeItems(RIPPED_DIARY, -1)
                addSpawn(ARURAUNE, npc, false, 300000, true)
            }

            ARURAUNE -> if (cond == 2) {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(HUGE_NAIL, 1)
                npc.broadcastNpcSay("The concealed truth will always be revealed...!")
            }

            OL_MAHUM_INSPECTOR -> if (cond == 6) {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
            }

            OL_MAHUM_BETRAYER -> if (cond == 8) {
                st["cond"] = "9"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(LETTER_OF_BETRAYER, 1)
                cancelQuestTimer("betrayer_despawn", null, player)
                _olMahumBetrayer = null
            }

            CRIMSON_WEREWOLF -> if (cond == 12) {
                st["cond"] = "13"
                st.playSound(QuestState.SOUND_MIDDLE)
                cancelQuestTimer("werewolf_cleanup", null, player)
                _crimsonWerewolf = false
            }

            KRUDEL_LIZARDMAN -> if (cond == 15) {
                st["cond"] = "16"
                st.playSound(QuestState.SOUND_MIDDLE)
                cancelQuestTimer("lizardman_cleanup", null, player)
                _krudelLizardman = false
            }

            SILENT_HORROR -> if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_4)) {
                st.giveItems(BONE_FRAGMENT_4, 1)
                if (st.hasQuestItems(BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8)) {
                    st["cond"] = "19"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            SKELETON_LORD -> if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_5)) {
                st.giveItems(BONE_FRAGMENT_5, 1)
                if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_6, BONE_FRAGMENT_7, BONE_FRAGMENT_8)) {
                    st["cond"] = "19"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            SKELETON_MARKSMAN -> if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_6)) {
                st.giveItems(BONE_FRAGMENT_6, 1)
                if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_7, BONE_FRAGMENT_8)) {
                    st["cond"] = "19"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            MISERY_SKELETON -> if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_7)) {
                st.giveItems(BONE_FRAGMENT_7, 1)
                if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_8)) {
                    st["cond"] = "19"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            SKELETON_ARCHER -> if (cond == 18 && !st.hasQuestItems(BONE_FRAGMENT_8)) {
                st.giveItems(BONE_FRAGMENT_8, 1)
                if (st.hasQuestItems(BONE_FRAGMENT_4, BONE_FRAGMENT_5, BONE_FRAGMENT_6, BONE_FRAGMENT_7)) {
                    st["cond"] = "19"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q227_TestOfTheReformer"

        // Items
        private val BOOK_OF_REFORM = 2822
        private val LETTER_OF_INTRODUCTION = 2823
        private val SLA_LETTER = 2824
        private val GREETINGS = 2825
        private val OL_MAHUM_MONEY = 2826
        private val KATARI_LETTER = 2827
        private val NYAKURI_LETTER = 2828
        private val UNDEAD_LIST = 2829
        private val RAMUS_LETTER = 2830
        private val RIPPED_DIARY = 2831
        private val HUGE_NAIL = 2832
        private val LETTER_OF_BETRAYER = 2833
        private val BONE_FRAGMENT_4 = 2834
        private val BONE_FRAGMENT_5 = 2835
        private val BONE_FRAGMENT_6 = 2836
        private val BONE_FRAGMENT_7 = 2837
        private val BONE_FRAGMENT_8 = 2838
        private val BONE_FRAGMENT_9 = 2839
        private val KAKAN_LETTER = 3037

        // Rewards
        private val MARK_OF_REFORMER = 2821
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val PUPINA = 30118
        private val SLA = 30666
        private val RAMUS = 30667
        private val KATARI = 30668
        private val KAKAN = 30669
        private val NYAKURI = 30670
        private val OL_MAHUM_PILGRIM = 30732

        // Monsters
        private val MISERY_SKELETON = 20022
        private val SKELETON_ARCHER = 20100
        private val SKELETON_MARKSMAN = 20102
        private val SKELETON_LORD = 20104
        private val SILENT_HORROR = 20404
        private val NAMELESS_REVENANT = 27099
        private val ARURAUNE = 27128
        private val OL_MAHUM_INSPECTOR = 27129
        private val OL_MAHUM_BETRAYER = 27130
        private val CRIMSON_WEREWOLF = 27131
        private val KRUDEL_LIZARDMAN = 27132

        // Checks & Instances
        private var _timer: Long = 0

        private var _olMahumInspector: Npc? = null
        private var _olMahumPilgrim: Npc? = null
        private var _olMahumBetrayer: Npc? = null

        private var _crimsonWerewolf = false
        private var _krudelLizardman = false

        // Allowed skills when attacking Crimson Werewolf
        private val ALLOWED_SKILLS = intArrayOf(1031, 1069, 1164, 1168, 1147, 1177, 1184, 1201, 1206)
    }
}