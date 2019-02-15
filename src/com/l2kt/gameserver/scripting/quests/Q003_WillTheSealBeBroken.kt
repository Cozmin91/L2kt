package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q003_WillTheSealBeBroken : Quest(3, "Will the Seal be Broken?") {
    init {

        setItemsIds(ONYX_BEAST_EYE, TAINT_STONE, SUCCUBUS_BLOOD)

        addStartNpc(30141) // Talloth
        addTalkId(30141)

        addKillId(20031, 20041, 20046, 20048, 20052, 20057)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30141-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
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
                htmltext = "30141-00.htm"
            else if (player.level < 16)
                htmltext = "30141-01.htm"
            else
                htmltext = "30141-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "30141-04.htm"
                else if (cond == 2) {
                    htmltext = "30141-06.htm"
                    st.takeItems(ONYX_BEAST_EYE, 1)
                    st.takeItems(SUCCUBUS_BLOOD, 1)
                    st.takeItems(TAINT_STONE, 1)
                    st.giveItems(SCROLL_ENCHANT_ARMOR_D, 1)
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        when (npc.npcId) {
            20031 -> if (st.dropItemsAlways(ONYX_BEAST_EYE, 1, 1) && st.hasQuestItems(TAINT_STONE, SUCCUBUS_BLOOD))
                st["cond"] = "2"

            20041, 20046 -> if (st.dropItemsAlways(TAINT_STONE, 1, 1) && st.hasQuestItems(
                    ONYX_BEAST_EYE,
                    SUCCUBUS_BLOOD
                )
            )
                st["cond"] = "2"

            20048, 20052, 20057 -> if (st.dropItemsAlways(SUCCUBUS_BLOOD, 1, 1) && st.hasQuestItems(
                    ONYX_BEAST_EYE,
                    TAINT_STONE
                )
            )
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private const val qn = "Q003_WillTheSealBeBroken"

        // Items
        private const val ONYX_BEAST_EYE = 1081
        private const val TAINT_STONE = 1082
        private const val SUCCUBUS_BLOOD = 1083

        // Reward
        private const val SCROLL_ENCHANT_ARMOR_D = 956
    }
}