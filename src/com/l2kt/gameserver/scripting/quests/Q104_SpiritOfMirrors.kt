package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q104_SpiritOfMirrors : Quest(104, "Spirit of Mirrors") {
    init {

        setItemsIds(GALLINS_OAK_WAND, WAND_SPIRITBOUND_1, WAND_SPIRITBOUND_2, WAND_SPIRITBOUND_3)

        addStartNpc(GALLINT)
        addTalkId(GALLINT, ARNOLD, JOHNSTONE, KENYOS)

        addKillId(27003, 27004, 27005)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30017-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(GALLINS_OAK_WAND, 1)
            st.giveItems(GALLINS_OAK_WAND, 1)
            st.giveItems(GALLINS_OAK_WAND, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.HUMAN)
                htmltext = "30017-00.htm"
            else if (player.level < 10)
                htmltext = "30017-01.htm"
            else
                htmltext = "30017-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GALLINT -> if (cond == 1 || cond == 2)
                        htmltext = "30017-04.htm"
                    else if (cond == 3) {
                        htmltext = "30017-05.htm"

                        st.takeItems(WAND_SPIRITBOUND_1, -1)
                        st.takeItems(WAND_SPIRITBOUND_2, -1)
                        st.takeItems(WAND_SPIRITBOUND_3, -1)

                        st.giveItems(WAND_OF_ADEPT, 1)
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

                    KENYOS, JOHNSTONE, ARNOLD -> {
                        htmltext = npc.npcId.toString() + "-01.htm"
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

        if (st.getItemEquipped(Inventory.PAPERDOLL_RHAND) == GALLINS_OAK_WAND) {
            when (npc.npcId) {
                27003 -> if (!st.hasQuestItems(WAND_SPIRITBOUND_1)) {
                    st.takeItems(GALLINS_OAK_WAND, 1)
                    st.giveItems(WAND_SPIRITBOUND_1, 1)

                    if (st.hasQuestItems(WAND_SPIRITBOUND_2, WAND_SPIRITBOUND_3)) {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }

                27004 -> if (!st.hasQuestItems(WAND_SPIRITBOUND_2)) {
                    st.takeItems(GALLINS_OAK_WAND, 1)
                    st.giveItems(WAND_SPIRITBOUND_2, 1)

                    if (st.hasQuestItems(WAND_SPIRITBOUND_1, WAND_SPIRITBOUND_3)) {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }

                27005 -> if (!st.hasQuestItems(WAND_SPIRITBOUND_3)) {
                    st.takeItems(GALLINS_OAK_WAND, 1)
                    st.giveItems(WAND_SPIRITBOUND_3, 1)

                    if (st.hasQuestItems(WAND_SPIRITBOUND_1, WAND_SPIRITBOUND_2)) {
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        st.playSound(QuestState.SOUND_ITEMGET)
                }
            }
        }

        return null
    }

    companion object {
        private const val qn = "Q104_SpiritOfMirrors"

        // Items
        private const val GALLINS_OAK_WAND = 748
        private const val WAND_SPIRITBOUND_1 = 1135
        private const val WAND_SPIRITBOUND_2 = 1136
        private const val WAND_SPIRITBOUND_3 = 1137

        // Rewards
        private const val SPIRITSHOT_NO_GRADE = 2509
        private const val SOULSHOT_NO_GRADE = 1835
        private const val WAND_OF_ADEPT = 747
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val LESSER_HEALING_POT = 1060
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416

        // NPCs
        private const val GALLINT = 30017
        private const val ARNOLD = 30041
        private const val JOHNSTONE = 30043
        private const val KENYOS = 30045
    }
}