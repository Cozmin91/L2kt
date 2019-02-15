package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q615_MagicalPowerOfFire_Part1 : Quest(615, "Magical Power of Fire - Part 1") {
    init {

        setItemsIds(STOLEN_RED_TOTEM)

        addStartNpc(NARAN)
        addTalkId(NARAN, UDAN, ASEFA_BOX)

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

        if (event.equals("31378-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["spawned"] = "0"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31559-03.htm", ignoreCase = true)) {
            // You have been discovered ; quest is failed.
            if (st.getInt("spawned") == 1)
                htmltext = "31559-04.htm"
            else if (!st.hasQuestItems(THIEF_KEY))
                htmltext = "31559-02.htm"
            else {
                st["cond"] = "3"
                st.playSound(QuestState.SOUND_ITEMGET)
                st.takeItems(THIEF_KEY, 1)
                st.giveItems(STOLEN_RED_TOTEM, 1)
            }// No Thief's Key in inventory.
        } else if (event.equals("UdanEyeDespawn", ignoreCase = true)) {
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
                    if (player.level >= 74 && player.allianceWithVarkaKetra <= -2) "31378-01.htm" else "31378-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    NARAN -> htmltext = "31378-04.htm"

                    UDAN -> if (cond == 1) {
                        htmltext = "31379-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 2) {
                        if (st.getInt("spawned") == 0)
                            htmltext = "31379-02.htm"
                        else {
                            htmltext = "31379-03.htm"
                            st["spawned"] = "0"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        }
                    } else if (cond == 3 && st.hasQuestItems(STOLEN_RED_TOTEM)) {
                        htmltext = "31379-04.htm"

                        st.takeItems(STOLEN_RED_TOTEM, 1)
                        st.giveItems(RED_TOTEM, 1)
                        st.giveItems(DIVINE_STONE, 1)

                        st.unset("spawned")
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    ASEFA_BOX -> if (cond == 2)
                        htmltext = "31559-01.htm"
                    else if (cond == 3)
                        htmltext = "31559-05.htm"
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

            // Spawn Udan's eye.
            val udanEye = addSpawn(EYE, player, true, 10000, true)
            if (udanEye != null) {
                startQuestTimer("UdanEyeDespawn", 9000, udanEye, player, false)
                udanEye.broadcastNpcSay("You cannot escape Udan's Eye!")
                st.playSound(QuestState.SOUND_GIVEUP)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q615_MagicalPowerOfFire_Part1"

        // NPCs
        private val NARAN = 31378
        private val UDAN = 31379
        private val ASEFA_BOX = 31559
        private val EYE = 31684

        // Items
        private val THIEF_KEY = 1661
        private val STOLEN_RED_TOTEM = 7242
        private val RED_TOTEM = 7243
        private val DIVINE_STONE = 7081
    }
}