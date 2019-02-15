package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q265_ChainsOfSlavery : Quest(265, "Chains of Slavery") {
    init {

        setItemsIds(SHACKLE)

        addStartNpc(30357) // Kristin
        addTalkId(30357)

        addKillId(20004, 20005)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30357-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30357-06.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
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
                htmltext = "30357-00.htm"
            else if (player.level < 6)
                htmltext = "30357-01.htm"
            else
                htmltext = "30357-02.htm"

            Quest.STATE_STARTED -> {
                val shackles = st.getQuestItemsCount(SHACKLE)
                if (shackles == 0)
                    htmltext = "30357-04.htm"
                else {
                    var reward = 12 * shackles
                    if (shackles > 10)
                        reward += 500

                    htmltext = "30357-05.htm"
                    st.takeItems(SHACKLE, -1)
                    st.rewardItems(57, reward)

                    if (player.isNewbie && st.getInt("Reward") == 0) {
                        st.showQuestionMark(26)
                        st["Reward"] = "1"

                        if (player.isMageClass) {
                            st.playTutorialVoice("tutorial_voice_027")
                            st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000)
                        } else {
                            st.playTutorialVoice("tutorial_voice_026")
                            st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000)
                        }
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(SHACKLE, 1, 0, if (npc.npcId == 20004) 500000 else 600000)

        return null
    }

    companion object {
        private val qn = "Q265_ChainsOfSlavery"

        // Item
        private val SHACKLE = 1368

        // Newbie Items
        private val SPIRITSHOT_FOR_BEGINNERS = 5790
        private val SOULSHOT_FOR_BEGINNERS = 5789
    }
}