package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q257_TheGuardIsBusy : Quest(257, "The Guard Is Busy") {
    init {

        setItemsIds(ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG, GLUDIO_LORD_MARK)

        addStartNpc(30039) // Gilbert
        addTalkId(30039)

        addKillId(20006, 20093, 20096, 20098, 20130, 20131, 20132, 20342, 20343)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30039-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(GLUDIO_LORD_MARK, 1)
        } else if (event.equals("30039-05.htm", ignoreCase = true)) {
            st.takeItems(GLUDIO_LORD_MARK, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 6) "30039-01.htm" else "30039-02.htm"

            Quest.STATE_STARTED -> {
                val amulets = st.getQuestItemsCount(ORC_AMULET)
                val necklaces = st.getQuestItemsCount(ORC_NECKLACE)
                val fangs = st.getQuestItemsCount(WEREWOLF_FANG)

                if (amulets + necklaces + fangs == 0)
                    htmltext = "30039-04.htm"
                else {
                    htmltext = "30039-07.htm"

                    st.takeItems(ORC_AMULET, -1)
                    st.takeItems(ORC_NECKLACE, -1)
                    st.takeItems(WEREWOLF_FANG, -1)

                    var reward = 10 * amulets + 20 * (necklaces + fangs)
                    if (amulets + necklaces + fangs >= 10)
                        reward += 1000

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

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            20006, 20130, 20131 -> st.dropItems(ORC_AMULET, 1, 0, 500000)

            20093, 20096, 20098 -> st.dropItems(ORC_NECKLACE, 1, 0, 500000)

            20342 -> st.dropItems(WEREWOLF_FANG, 1, 0, 200000)

            20343 -> st.dropItems(WEREWOLF_FANG, 1, 0, 400000)

            20132 -> st.dropItems(WEREWOLF_FANG, 1, 0, 500000)
        }

        return null
    }

    companion object {
        private val qn = "Q257_TheGuardIsBusy"

        // Items
        private val GLUDIO_LORD_MARK = 1084
        private val ORC_AMULET = 752
        private val ORC_NECKLACE = 1085
        private val WEREWOLF_FANG = 1086

        // Newbie Items
        private val SPIRITSHOT_FOR_BEGINNERS = 5790
        private val SOULSHOT_FOR_BEGINNERS = 5789
    }
}