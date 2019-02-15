package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q036_MakeASewingKit : Quest(36, "Make a Sewing Kit") {
    init {

        setItemsIds(REINFORCED_STEEL)

        addStartNpc(30847) // Ferris
        addTalkId(30847)

        addKillId(20566) // Iron Golem
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30847-1.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30847-3.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(REINFORCED_STEEL, 5)
        } else if (event.equals("30847-5.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(ORIHARUKON) >= 10 && st.getQuestItemsCount(ARTISANS_FRAME) >= 10) {
                st.takeItems(ARTISANS_FRAME, 10)
                st.takeItems(ORIHARUKON, 10)
                st.giveItems(SEWING_KIT, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            } else
                htmltext = "30847-4a.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.level >= 60) {
                val fwear = player.getQuestState("Q037_MakeFormalWear")
                if (fwear != null && fwear.getInt("cond") == 6)
                    htmltext = "30847-0.htm"
                else
                    htmltext = "30847-0a.htm"
            } else
                htmltext = "30847-0b.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30847-1a.htm"
                else if (cond == 2)
                    htmltext = "30847-2.htm"
                else if (cond == 3)
                    htmltext =
                            if (st.getQuestItemsCount(ORIHARUKON) < 10 || st.getQuestItemsCount(ARTISANS_FRAME) < 10) "30847-4a.htm" else "30847-4.htm"
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItems(REINFORCED_STEEL, 1, 5, 500000))
            st["cond"] = "2"

        return null
    }

    companion object {
        private const val qn = "Q036_MakeASewingKit"

        // Items
        private const val REINFORCED_STEEL = 7163
        private const val ARTISANS_FRAME = 1891
        private const val ORIHARUKON = 1893

        // Reward
        private const val SEWING_KIT = 7078
    }
}