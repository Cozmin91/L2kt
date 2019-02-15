package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q609_MagicalPowerOfWater_Part1 : Quest(609, "Magical Power of Water - Part 1") {
    init {

        setItemsIds(STOLEN_GREEN_TOTEM)

        addStartNpc(WAHKAN)
        addTalkId(WAHKAN, ASEFA, UDAN_BOX)

        // IDs aggro ranges to avoid, else quest is automatically failed.
        addAggroRangeEnterId(
            21350,
            21351,
            21353,
            21354,
            21355,
            21357,
            21358,
            21360,
            21361,
            21362,
            21369,
            21370,
            21364,
            21365,
            21366,
            21368,
            21371,
            21372,
            21373,
            21374,
            21375
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31371-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["spawned"] = "0"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31561-03.htm", ignoreCase = true)) {
            // You have been discovered ; quest is failed.
            if (st.getInt("spawned") == 1)
                htmltext = "31561-04.htm"
            else if (!st.hasQuestItems(THIEF_KEY))
                htmltext = "31561-02.htm"
            else {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(THIEF_KEY, 1)
                st.giveItems(STOLEN_GREEN_TOTEM, 1)
            }// No Thief's Key in inventory.
        } else if (event.equals("AsefaEyeDespawn", ignoreCase = true)) {
            npc!!.broadcastNpcSay("I'll be waiting for your return.")
            return null
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext =
                    if (player.level >= 74 && player.allianceWithVarkaKetra >= 2) "31371-01.htm" else "31371-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    WAHKAN -> htmltext = "31371-04.htm"

                    ASEFA -> if (cond == 1) {
                        htmltext = "31372-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 2) {
                        if (st.getInt("spawned") == 0)
                            htmltext = "31372-02.htm"
                        else {
                            htmltext = "31372-03.htm"
                            st["spawned"] = "0"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 3 && st.hasQuestItems(STOLEN_GREEN_TOTEM)) {
                        htmltext = "31372-04.htm"

                        st.takeItems(STOLEN_GREEN_TOTEM, 1)
                        st.giveItems(GREEN_TOTEM, 1)
                        st.giveItems(DIVINE_STONE, 1)

                        st.unset("spawned")
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    UDAN_BOX -> if (cond == 2)
                        htmltext = "31561-01.htm"
                    else if (cond == 3)
                        htmltext = "31561-05.htm"
                }
            }
        }

        return htmltext
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        val st = player?.getQuestState(qn) ?: return null

        if (st.getInt("spawned") == 0 && st.getInt("cond") == 2) {
            // Put "spawned" flag to 1 to avoid to spawn another.
            st["spawned"] = "1"

            // Spawn Asefa's eye.
            val asefaEye = addSpawn(EYE, player, true, 10000, true)
            if (asefaEye != null) {
                startQuestTimer("AsefaEyeDespawn", 9000, asefaEye, player, false)
                asefaEye.broadcastNpcSay("You cannot escape Asefa's Eye!")
                st.playSound(QuestState.SOUND_GIVEUP)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q609_MagicalPowerOfWater_Part1"

        // NPCs
        private val WAHKAN = 31371
        private val ASEFA = 31372
        private val UDAN_BOX = 31561
        private val EYE = 31685

        // Items
        private val THIEF_KEY = 1661
        private val STOLEN_GREEN_TOTEM = 7237
        private val GREEN_TOTEM = 7238
        private val DIVINE_STONE = 7081
    }
}