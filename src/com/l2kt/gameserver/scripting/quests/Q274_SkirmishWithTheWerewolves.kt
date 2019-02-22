package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q274_SkirmishWithTheWerewolves : Quest(274, "Skirmish with the Werewolves") {
    init {

        setItemsIds(MARAKU_WEREWOLF_HEAD, MARAKU_WOLFMEN_TOTEM)

        addStartNpc(30569)
        addTalkId(30569)

        addKillId(20363, 20364)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30569-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30569-00.htm"
            else if (player.level < 9)
                htmltext = "30569-01.htm"
            else if (st.hasAtLeastOneQuestItem(NECKLACE_OF_COURAGE, NECKLACE_OF_VALOR))
                htmltext = "30569-02.htm"
            else
                htmltext = "30569-07.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30569-04.htm"
            else {
                htmltext = "30569-05.htm"

                val amount = 3500 + st.getQuestItemsCount(MARAKU_WOLFMEN_TOTEM) * 600

                st.takeItems(MARAKU_WEREWOLF_HEAD, -1)
                st.takeItems(MARAKU_WOLFMEN_TOTEM, -1)
                st.rewardItems(57, amount)

                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (st.dropItemsAlways(MARAKU_WEREWOLF_HEAD, 1, 40))
            st["cond"] = "2"

        if (Rnd[100] < 6)
            st.giveItems(MARAKU_WOLFMEN_TOTEM, 1)

        return null
    }

    companion object {
        private val qn = "Q274_SkirmishWithTheWerewolves"

        // Needed items
        private val NECKLACE_OF_VALOR = 1507
        private val NECKLACE_OF_COURAGE = 1506

        // Items
        private val MARAKU_WEREWOLF_HEAD = 1477
        private val MARAKU_WOLFMEN_TOTEM = 1501
    }
}