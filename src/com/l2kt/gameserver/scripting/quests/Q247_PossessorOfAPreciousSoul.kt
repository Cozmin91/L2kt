package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q247_PossessorOfAPreciousSoul : Quest(247, "Possessor of a Precious Soul - 4") {
    init {

        addStartNpc(CARADINE)
        addTalkId(CARADINE, LADY_OF_THE_LAKE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        // Caradine
        if (event.equals("31740-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.takeItems(CARADINE_LETTER, 1)
        } else if (event.equals("31740-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            player.teleToLocation(143209, 43968, -3038, 0)
        } else if (event.equals("31745-05.htm", ignoreCase = true)) {
            player.setNoble(true, true)
            st.giveItems(NOBLESS_TIARA, 1)
            st.rewardExpAndSp(93836, 0)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }// Lady of the lake

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (st.hasQuestItems(CARADINE_LETTER))
                htmltext = if (!player.isSubClassActive || player.level < 75) "31740-02.htm" else "31740-01.htm"

            Quest.STATE_STARTED -> run{
                if (!player.isSubClassActive)
                    return@run

                val cond = st.getInt("cond")
                when (npc.npcId) {
                    CARADINE -> if (cond == 1)
                        htmltext = "31740-04.htm"
                    else if (cond == 2)
                        htmltext = "31740-06.htm"

                    LADY_OF_THE_LAKE -> if (cond == 2)
                        htmltext = if (player.level < 75) "31745-06.htm" else "31745-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private val qn = "Q247_PossessorOfAPreciousSoul"

        // NPCs
        private val CARADINE = 31740
        private val LADY_OF_THE_LAKE = 31745

        // Items
        private val CARADINE_LETTER = 7679
        private val NOBLESS_TIARA = 7694
    }
}