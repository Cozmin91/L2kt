package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q107_MercilessPunishment : Quest(107, "Merciless Punishment") {
    init {

        setItemsIds(HATOS_ORDER_1, HATOS_ORDER_2, HATOS_ORDER_3, LETTER_TO_HUMAN, LETTER_TO_DARKELF, LETTER_TO_ELF)

        addStartNpc(HATOS)
        addTalkId(HATOS, PARUGON)

        addKillId(27041) // Baranka's Messenger
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30568-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(HATOS_ORDER_1, 1)
        } else if (event.equals("30568-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("30568-07.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HATOS_ORDER_1, 1)
            st.giveItems(HATOS_ORDER_2, 1)
        } else if (event.equals("30568-09.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HATOS_ORDER_2, 1)
            st.giveItems(HATOS_ORDER_3, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30568-00.htm"
            else if (player.level < 12)
                htmltext = "30568-01.htm"
            else
                htmltext = "30568-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    HATOS -> if (cond == 1 || cond == 2)
                        htmltext = "30568-04.htm"
                    else if (cond == 3)
                        htmltext = "30568-05.htm"
                    else if (cond == 4 || cond == 6)
                        htmltext = "30568-09.htm"
                    else if (cond == 5)
                        htmltext = "30568-08.htm"
                    else if (cond == 7) {
                        htmltext = "30568-10.htm"
                        st.takeItems(HATOS_ORDER_3, -1)
                        st.takeItems(LETTER_TO_DARKELF, -1)
                        st.takeItems(LETTER_TO_HUMAN, -1)
                        st.takeItems(LETTER_TO_ELF, -1)

                        st.giveItems(BUTCHER_SWORD, 1)
                        st.giveItems(LESSER_HEALING_POTION, 100)

                        if (player.isNewbie) {
                            st.showQuestionMark(26)
                            if (player.isMageClass) {
                                st.playTutorialVoice("tutorial_voice_027")
                                st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000)
                            } else {
                                st.playTutorialVoice("tutorial_voice_026")
                                st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000)
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

                    PARUGON -> {
                        htmltext = "30580-01.htm"
                        if (cond == 1) {
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        if (cond == 2) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LETTER_TO_HUMAN, 1)
        } else if (cond == 4) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LETTER_TO_DARKELF, 1)
        } else if (cond == 6) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LETTER_TO_ELF, 1)
        }

        return null
    }

    companion object {
        private const val qn = "Q107_MercilessPunishment"

        // NPCs
        private const val HATOS = 30568
        private const val PARUGON = 30580

        // Items
        private const val HATOS_ORDER_1 = 1553
        private const val HATOS_ORDER_2 = 1554
        private const val HATOS_ORDER_3 = 1555
        private const val LETTER_TO_HUMAN = 1557
        private const val LETTER_TO_DARKELF = 1556
        private const val LETTER_TO_ELF = 1558

        // Rewards
        private const val BUTCHER_SWORD = 1510
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416
        private const val LESSER_HEALING_POTION = 1060
    }
}