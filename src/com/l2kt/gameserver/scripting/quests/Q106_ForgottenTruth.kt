package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q106_ForgottenTruth : Quest(106, "Forgotten Truth") {
    init {

        setItemsIds(ONYX_TALISMAN_1, ONYX_TALISMAN_2, ANCIENT_SCROLL, ANCIENT_CLAY_TABLET, KARTIA_TRANSLATION)

        addStartNpc(THIFIELL)
        addTalkId(THIFIELL, KARTIA)

        addKillId(27070) // Tumran Orc Brigand
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30358-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ONYX_TALISMAN_1, 1)
        }
        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DARK_ELF)
                htmltext = "30358-00.htm"
            else if (player.level < 10)
                htmltext = "30358-02.htm"
            else
                htmltext = "30358-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    THIFIELL -> if (cond == 1)
                        htmltext = "30358-06.htm"
                    else if (cond == 2)
                        htmltext = "30358-06.htm"
                    else if (cond == 3)
                        htmltext = "30358-06.htm"
                    else if (cond == 4) {
                        htmltext = "30358-07.htm"
                        st.takeItems(KARTIA_TRANSLATION, 1)
                        st.giveItems(ELDRITCH_DAGGER, 1)
                        st.giveItems(LESSER_HEALING_POTION, 100)

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

                    KARTIA -> if (cond == 1) {
                        htmltext = "30133-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ONYX_TALISMAN_1, 1)
                        st.giveItems(ONYX_TALISMAN_2, 1)
                    } else if (cond == 2)
                        htmltext = "30133-02.htm"
                    else if (cond == 3) {
                        htmltext = "30133-03.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ONYX_TALISMAN_2, 1)
                        st.takeItems(ANCIENT_SCROLL, 1)
                        st.takeItems(ANCIENT_CLAY_TABLET, 1)
                        st.giveItems(KARTIA_TRANSLATION, 1)
                    } else if (cond == 4)
                        htmltext = "30133-04.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (!st.hasQuestItems(ANCIENT_SCROLL))
            st.dropItems(ANCIENT_SCROLL, 1, 1, 200000)
        else if (st.dropItems(ANCIENT_CLAY_TABLET, 1, 1, 200000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q106_ForgottenTruth"

        // NPCs
        private const val THIFIELL = 30358
        private const val KARTIA = 30133

        // Items
        private const val ONYX_TALISMAN_1 = 984
        private const val ONYX_TALISMAN_2 = 985
        private const val ANCIENT_SCROLL = 986
        private const val ANCIENT_CLAY_TABLET = 987
        private const val KARTIA_TRANSLATION = 988

        // Rewards
        private const val SPIRITSHOT_NO_GRADE = 2509
        private const val SOULSHOT_NO_GRADE = 1835
        private const val ELDRITCH_DAGGER = 989
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