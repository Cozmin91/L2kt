package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q276_TotemOfTheHestui : Quest(276, "Totem of the Hestui") {
    init {

        setItemsIds(KASHA_PARASITE, KASHA_CRYSTAL)

        addStartNpc(30571) // Tanapi
        addTalkId(30571)

        addKillId(20479, 27044)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(qn) ?: return event

        if (event.equals("30571-03.htm", ignoreCase = true)) {
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
            Quest.STATE_CREATED -> if (player.race != ClassRace.ORC)
                htmltext = "30571-00.htm"
            else if (player.level < 15)
                htmltext = "30571-01.htm"
            else
                htmltext = "30571-02.htm"

            Quest.STATE_STARTED -> if (st.getInt("cond") == 1)
                htmltext = "30571-04.htm"
            else {
                htmltext = "30571-05.htm"
                st.takeItems(KASHA_CRYSTAL, -1)
                st.takeItems(KASHA_PARASITE, -1)
                st.giveItems(HESTUI_TOTEM, 1)
                st.giveItems(LEATHER_PANTS, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "1") ?: return null

        if (!st.hasQuestItems(KASHA_CRYSTAL)) {
            when (npc.npcId) {
                20479 -> {
                    val count = st.getQuestItemsCount(KASHA_PARASITE)
                    val random = Rnd[100]

                    if (count >= 79 || count >= 69 && random <= 20 || count >= 59 && random <= 15 || count >= 49 && random <= 10 || count >= 39 && random < 2) {
                        addSpawn(27044, npc, true, 0, true)
                        st.takeItems(KASHA_PARASITE, count)
                    } else
                        st.dropItemsAlways(KASHA_PARASITE, 1, 0)
                }

                27044 -> {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(KASHA_CRYSTAL, 1)
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q276_TotemOfTheHestui"

        // Items
        private val KASHA_PARASITE = 1480
        private val KASHA_CRYSTAL = 1481

        // Rewards
        private val HESTUI_TOTEM = 1500
        private val LEATHER_PANTS = 29
    }
}