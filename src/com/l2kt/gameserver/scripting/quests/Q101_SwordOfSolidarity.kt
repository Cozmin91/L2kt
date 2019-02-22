package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q101_SwordOfSolidarity : Quest(101, "Sword of Solidarity") {
    init {

        setItemsIds(BROKEN_SWORD_HANDLE, BROKEN_BLADE_BOTTOM, BROKEN_BLADE_TOP)

        addStartNpc(ROIEN)
        addTalkId(ROIEN, ALTRAN)

        addKillId(20361, 20362)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30008-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ROIENS_LETTER, 1)
        } else if (event.equals("30283-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ROIENS_LETTER, 1)
            st.giveItems(DIR_TO_RUINS, 1)
        } else if (event.equals("30283-06.htm", ignoreCase = true)) {
            st.takeItems(BROKEN_SWORD_HANDLE, 1)
            st.giveItems(SWORD_OF_SOLIDARITY, 1)
            st.giveItems(LESSER_HEALING_POT, 100)

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

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.HUMAN)
                htmltext = "30008-01a.htm"
            else if (player.level < 9)
                htmltext = "30008-01.htm"
            else
                htmltext = "30008-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ROIEN -> if (cond == 1)
                        htmltext = "30008-04.htm"
                    else if (cond == 2)
                        htmltext = "30008-03a.htm"
                    else if (cond == 3)
                        htmltext = "30008-06.htm"
                    else if (cond == 4) {
                        htmltext = "30008-05.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALTRANS_NOTE, 1)
                        st.giveItems(BROKEN_SWORD_HANDLE, 1)
                    } else if (cond == 5)
                        htmltext = "30008-05a.htm"

                    ALTRAN -> if (cond == 1)
                        htmltext = "30283-01.htm"
                    else if (cond == 2)
                        htmltext = "30283-03.htm"
                    else if (cond == 3) {
                        htmltext = "30283-04.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DIR_TO_RUINS, 1)
                        st.takeItems(BROKEN_BLADE_TOP, 1)
                        st.takeItems(BROKEN_BLADE_BOTTOM, 1)
                        st.giveItems(ALTRANS_NOTE, 1)
                    } else if (cond == 4)
                        htmltext = "30283-04a.htm"
                    else if (cond == 5)
                        htmltext = "30283-05.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "2") ?: return null

        if (!st.hasQuestItems(BROKEN_BLADE_TOP))
            st.dropItems(BROKEN_BLADE_TOP, 1, 1, 200000)
        else if (st.dropItems(BROKEN_BLADE_BOTTOM, 1, 1, 200000))
            st["cond"] = "3"

        return null
    }

    companion object {
        private const val qn = "Q101_SwordOfSolidarity"

        // NPCs
        private const val ROIEN = 30008
        private const val ALTRAN = 30283

        // Items
        private const val BROKEN_SWORD_HANDLE = 739
        private const val BROKEN_BLADE_BOTTOM = 740
        private const val BROKEN_BLADE_TOP = 741
        private const val ROIENS_LETTER = 796
        private const val DIR_TO_RUINS = 937
        private const val ALTRANS_NOTE = 742

        private const val SWORD_OF_SOLIDARITY = 738
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val LESSER_HEALING_POT = 1060
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416
    }
}