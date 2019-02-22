package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q624_TheFinestIngredients_Part1 : Quest(624, "The Finest Ingredients - Part 1") {
    init {

        setItemsIds(TRUNK_OF_NEPENTHES, FOOT_OF_BANDERSNATCHLING, SECRET_SPICE)

        addStartNpc(31521) // Jeremy
        addTalkId(31521)

        addKillId(NEPENTHES, ATROX, ATROXSPAWN, BANDERSNATCH)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31521-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31521-05.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50 && st.getQuestItemsCount(
                    SECRET_SPICE
                ) >= 50
            ) {
                st.takeItems(TRUNK_OF_NEPENTHES, -1)
                st.takeItems(FOOT_OF_BANDERSNATCHLING, -1)
                st.takeItems(SECRET_SPICE, -1)
                st.giveItems(ICE_CRYSTAL, 1)
                st.giveItems(SOY_SAUCE_JAR, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(true)
            } else {
                st["cond"] = "1"
                htmltext = "31521-07.htm"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 73) "31521-03.htm" else "31521-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                if (cond == 1)
                    htmltext = "31521-06.htm"
                else if (cond == 2) {
                    if (st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(
                            FOOT_OF_BANDERSNATCHLING
                        ) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50
                    )
                        htmltext = "31521-04.htm"
                    else
                        htmltext = "31521-07.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = getRandomPartyMember(player!!, npc, "1") ?: return null

        when (npc.npcId) {
            NEPENTHES -> if (st.dropItemsAlways(TRUNK_OF_NEPENTHES, 1, 50) && st.getQuestItemsCount(
                    FOOT_OF_BANDERSNATCHLING
                ) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50
            )
                st["cond"] = "2"

            ATROX, ATROXSPAWN -> if (st.dropItemsAlways(
                    SECRET_SPICE,
                    1,
                    50
                ) && st.getQuestItemsCount(TRUNK_OF_NEPENTHES) >= 50 && st.getQuestItemsCount(FOOT_OF_BANDERSNATCHLING) >= 50
            )
                st["cond"] = "2"

            BANDERSNATCH -> if (st.dropItemsAlways(FOOT_OF_BANDERSNATCHLING, 1, 50) && st.getQuestItemsCount(
                    TRUNK_OF_NEPENTHES
                ) >= 50 && st.getQuestItemsCount(SECRET_SPICE) >= 50
            )
                st["cond"] = "2"
        }

        return null
    }

    companion object {
        private val qn = "Q624_TheFinestIngredients_Part1"

        // Mobs
        private val NEPENTHES = 21319
        private val ATROX = 21321
        private val ATROXSPAWN = 21317
        private val BANDERSNATCH = 21314

        // Items
        private val TRUNK_OF_NEPENTHES = 7202
        private val FOOT_OF_BANDERSNATCHLING = 7203
        private val SECRET_SPICE = 7204

        // Rewards
        private val ICE_CRYSTAL = 7080
        private val SOY_SAUCE_JAR = 7205
    }
}