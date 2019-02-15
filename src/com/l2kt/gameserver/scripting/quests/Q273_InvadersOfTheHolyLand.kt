package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q273_InvadersOfTheHolyLand : Quest(273, "Invaders of the Holy Land") {
    init {

        setItemsIds(BLACK_SOULSTONE, RED_SOULSTONE)

        addStartNpc(30566) // Varkees
        addTalkId(30566)

        addKillId(20311, 20312, 20313)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30566-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30566-07.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30566-00.htm"
            else if (player.level < 6)
                htmltext = "30566-01.htm"
            else
                htmltext = "30566-02.htm"

            Quest.STATE_STARTED -> {
                val red = st.getQuestItemsCount(RED_SOULSTONE)
                val black = st.getQuestItemsCount(BLACK_SOULSTONE)

                if (red + black == 0)
                    htmltext = "30566-04.htm"
                else {
                    if (red == 0)
                        htmltext = "30566-05.htm"
                    else
                        htmltext = "30566-06.htm"

                    val reward = black * 3 + red * 10 + if (black >= 10) if (red >= 1) 1800 else 1500 else 0

                    st.takeItems(BLACK_SOULSTONE, -1)
                    st.takeItems(RED_SOULSTONE, -1)
                    st.rewardItems(57, reward)

                    if (player.isNewbie && st.getInt("Reward") == 0) {
                        st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000)
                        st.playTutorialVoice("tutorial_voice_026")
                        st["Reward"] = "1"
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId

        var probability = 77
        if (npcId == 20311)
            probability = 90
        else if (npcId == 20312)
            probability = 87

        if (Rnd[100] <= probability)
            st.dropItemsAlways(BLACK_SOULSTONE, 1, 0)
        else
            st.dropItemsAlways(RED_SOULSTONE, 1, 0)

        return null
    }

    companion object {
        private val qn = "Q273_InvadersOfTheHolyLand"

        // Items
        private val BLACK_SOULSTONE = 1475
        private val RED_SOULSTONE = 1476

        // Reward
        private val SOULSHOT_FOR_BEGINNERS = 5789
    }
}