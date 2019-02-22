package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q103_SpiritOfCraftsman : Quest(103, "Spirit of Craftsman") {
    init {

        setItemsIds(
            KARROD_LETTER,
            CECKTINON_VOUCHER_1,
            CECKTINON_VOUCHER_2,
            BONE_FRAGMENT,
            SOUL_CATCHER,
            PRESERVING_OIL,
            ZOMBIE_HEAD,
            STEELBENDER_HEAD
        )

        addStartNpc(KARROD)
        addTalkId(KARROD, CECKTINON, HARNE)

        addKillId(20015, 20020, 20455, 20517, 20518)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30307-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(KARROD_LETTER, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30307-00.htm"
            else if (player.level < 11)
                htmltext = "30307-02.htm"
            else
                htmltext = "30307-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    KARROD -> if (cond < 8)
                        htmltext = "30307-06.htm"
                    else if (cond == 8) {
                        htmltext = "30307-07.htm"
                        st.takeItems(STEELBENDER_HEAD, 1)
                        st.giveItems(BLOODSABER, 1)
                        st.rewardItems(LESSER_HEALING_POT, 100)

                        if (player.isMageClass)
                            st.giveItems(SPIRITSHOT_NO_GRADE, 500)
                        else
                            st.giveItems(SOULSHOT_NO_GRADE, 1000)

                        if (player.isNewbie) {
                            st.showQuestionMark(26)
                            if (player.isMageClass) {
                                st.playTutorialVoice("tutorial_voice_027")
                                st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000)
                            } else {
                                st.playTutorialVoice("tutorial_voice_026")
                                st.giveItems(SOULSHOT_FOR_BEGINNERS, 7000)
                            }
                        }

                        st.giveItems(ECHO_BATTLE, 10)
                        st.giveItems(ECHO_LOVE, 10)
                        st.giveItems(ECHO_SOLITUDE, 10)
                        st.giveItems(ECHO_FEAST, 10)
                        st.giveItems(ECHO_CELEBRATION, 10)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    CECKTINON -> if (cond == 1) {
                        htmltext = "30132-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(KARROD_LETTER, 1)
                        st.giveItems(CECKTINON_VOUCHER_1, 1)
                    } else if (cond > 1 && cond < 5)
                        htmltext = "30132-02.htm"
                    else if (cond == 5) {
                        htmltext = "30132-03.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(SOUL_CATCHER, 1)
                        st.giveItems(PRESERVING_OIL, 1)
                    } else if (cond == 6)
                        htmltext = "30132-04.htm"
                    else if (cond == 7) {
                        htmltext = "30132-05.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ZOMBIE_HEAD, 1)
                        st.giveItems(STEELBENDER_HEAD, 1)
                    } else if (cond == 8)
                        htmltext = "30132-06.htm"

                    HARNE -> if (cond == 2) {
                        htmltext = "30144-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CECKTINON_VOUCHER_1, 1)
                        st.giveItems(CECKTINON_VOUCHER_2, 1)
                    } else if (cond == 3)
                        htmltext = "30144-02.htm"
                    else if (cond == 4) {
                        htmltext = "30144-03.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CECKTINON_VOUCHER_2, 1)
                        st.takeItems(BONE_FRAGMENT, 10)
                        st.giveItems(SOUL_CATCHER, 1)
                    } else if (cond == 5)
                        htmltext = "30144-04.htm"
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
            20517, 20518, 20455 -> if (st.getInt("cond") == 3 && st.dropItems(BONE_FRAGMENT, 1, 10, 300000))
                st["cond"] = "4"

            20015, 20020 -> if (st.getInt("cond") == 6 && st.dropItems(ZOMBIE_HEAD, 1, 1, 300000)) {
                st["cond"] = "7"
                st.takeItems(PRESERVING_OIL, 1)
            }
        }

        return null
    }

    companion object {
        private const val qn = "Q103_SpiritOfCraftsman"

        // Items
        private const val KARROD_LETTER = 968
        private const val CECKTINON_VOUCHER_1 = 969
        private const val CECKTINON_VOUCHER_2 = 970
        private const val SOUL_CATCHER = 971
        private const val PRESERVING_OIL = 972
        private const val ZOMBIE_HEAD = 973
        private const val STEELBENDER_HEAD = 974
        private const val BONE_FRAGMENT = 1107

        // Rewards
        private const val SPIRITSHOT_NO_GRADE = 2509
        private const val SOULSHOT_NO_GRADE = 1835
        private const val BLOODSABER = 975
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val LESSER_HEALING_POT = 1060
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416

        // NPCs
        private const val KARROD = 30307
        private const val CECKTINON = 30132
        private const val HARNE = 30144
    }
}