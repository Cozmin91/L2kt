package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q639_GuardiansOfTheHolyGrail : Quest(639, "Guardians of the Holy Grail") {
    init {
        CHANCES[22122] = 760000
        CHANCES[22123] = 750000
        CHANCES[22124] = 590000
        CHANCES[22125] = 580000
        CHANCES[22126] = 590000
        CHANCES[22127] = 580000
        CHANCES[22128] = 170000
        CHANCES[22129] = 590000
        CHANCES[22130] = 850000
        CHANCES[22131] = 920000
        CHANCES[22132] = 580000
        CHANCES[22133] = 930000
        CHANCES[22134] = 230000
        CHANCES[22135] = 580000
    }

    init {

        setItemsIds(SCRIPTURE, WATER_BOTTLE, HOLY_WATER_BOTTLE)

        addStartNpc(DOMINIC)
        addTalkId(DOMINIC, GREMORY, HOLY_GRAIL)

        for (id in CHANCES.keys)
            addKillId(id)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // DOMINIC
        if (event.equals("31350-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31350-08.htm", ignoreCase = true)) {
            val count = st.getQuestItemsCount(SCRIPTURE)

            st.takeItems(SCRIPTURE, -1)
            st.rewardItems(57, 1625 * count + if (count >= 10) 33940 else 0)
        } else if (event.equals("31350-09.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_GIVEUP)
            st.exitQuest(true)
        } else if (event.equals("32008-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(WATER_BOTTLE, 1)
        } else if (event.equals("32008-09.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HOLY_WATER_BOTTLE, 1)
        } else if (event.equals("32008-12.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SCRIPTURE) >= 4000) {
                htmltext = "32008-11.htm"
                st.takeItems(SCRIPTURE, 4000)
                st.rewardItems(959, 1)
            }
        } else if (event.equals("32008-14.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(SCRIPTURE) >= 400) {
                htmltext = "32008-13.htm"
                st.takeItems(SCRIPTURE, 400)
                st.rewardItems(960, 1)
            }
        } else if (event.equals("32028-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(WATER_BOTTLE, 1)
            st.giveItems(HOLY_WATER_BOTTLE, 1)
        }// HOLY GRAIL
        // GREMORY

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31350-02.htm" else "31350-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    DOMINIC -> htmltext = if (st.hasQuestItems(SCRIPTURE)) "31350-05.htm" else "31350-06.htm"

                    GREMORY -> if (cond == 1)
                        htmltext = "32008-01.htm"
                    else if (cond == 2)
                        htmltext = "32008-06.htm"
                    else if (cond == 3)
                        htmltext = "32008-08.htm"
                    else if (cond == 4)
                        htmltext = "32008-10.htm"

                    HOLY_GRAIL -> if (cond == 2)
                        htmltext = "32028-01.htm"
                    else if (cond > 2)
                        htmltext = "32028-03.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st.dropItems(SCRIPTURE, 1, 0, CHANCES[npc.npcId] ?: 0)

        return null
    }

    companion object {
        private val qn = "Q639_GuardiansOfTheHolyGrail"

        // NPCs
        private val DOMINIC = 31350
        private val GREMORY = 32008
        private val HOLY_GRAIL = 32028

        // Items
        private val SCRIPTURE = 8069
        private val WATER_BOTTLE = 8070
        private val HOLY_WATER_BOTTLE = 8071

        private val CHANCES = HashMap<Int, Int>()
    }
}