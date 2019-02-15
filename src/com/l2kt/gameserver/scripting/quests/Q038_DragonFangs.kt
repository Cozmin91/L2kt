package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q038_DragonFangs : Quest(38, "Dragon Fangs") {
    init {
        DROPLIST[21100] = intArrayOf(1, FEATHER_ORNAMENT, 100, 1000000)
        DROPLIST[20357] = intArrayOf(1, FEATHER_ORNAMENT, 100, 1000000)
        DROPLIST[21101] = intArrayOf(6, TOOTH_OF_DRAGON, 50, 500000)
        DROPLIST[20356] = intArrayOf(6, TOOTH_OF_DRAGON, 50, 500000)
    }

    init {

        setItemsIds(FEATHER_ORNAMENT, TOOTH_OF_TOTEM, TOOTH_OF_DRAGON, LETTER_OF_IRIS, LETTER_OF_ROHMER)

        addStartNpc(LUIS)
        addTalkId(LUIS, IRIS, ROHMER)

        for (mob in DROPLIST.keys)
            addKillId(mob)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30386-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30386-04.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(FEATHER_ORNAMENT, 100)
            st.giveItems(TOOTH_OF_TOTEM, 1)
        } else if (event.equals("30034-02a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(TOOTH_OF_TOTEM)) {
                htmltext = "30034-02.htm"
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(TOOTH_OF_TOTEM, 1)
                st.giveItems(LETTER_OF_IRIS, 1)
            }
        } else if (event.equals("30344-02a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(LETTER_OF_IRIS)) {
                htmltext = "30344-02.htm"
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(LETTER_OF_IRIS, 1)
                st.giveItems(LETTER_OF_ROHMER, 1)
            }
        } else if (event.equals("30034-04a.htm", ignoreCase = true)) {
            if (st.hasQuestItems(LETTER_OF_ROHMER)) {
                htmltext = "30034-04.htm"
                st["cond"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(LETTER_OF_ROHMER, 1)
            }
        } else if (event.equals("30034-06a.htm", ignoreCase = true)) {
            if (st.getQuestItemsCount(TOOTH_OF_DRAGON) >= 50) {
                val position = Rnd[REWARD.size]

                htmltext = "30034-06.htm"
                st.takeItems(TOOTH_OF_DRAGON, 50)
                st.giveItems(REWARD[position][0], 1)
                st.rewardItems(57, REWARD[position][1])
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 19) "30386-01a.htm" else "30386-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LUIS -> if (cond == 1)
                        htmltext = "30386-02a.htm"
                    else if (cond == 2)
                        htmltext = "30386-03.htm"
                    else if (cond > 2)
                        htmltext = "30386-03a.htm"

                    IRIS -> if (cond == 3)
                        htmltext = "30034-01.htm"
                    else if (cond == 4)
                        htmltext = "30034-02b.htm"
                    else if (cond == 5)
                        htmltext = "30034-03.htm"
                    else if (cond == 6)
                        htmltext = "30034-05a.htm"
                    else if (cond == 7)
                        htmltext = "30034-05.htm"

                    ROHMER -> if (cond == 4)
                        htmltext = "30344-01.htm"
                    else if (cond > 4)
                        htmltext = "30344-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val droplist = DROPLIST[npc.npcId] ?: return null

        if (st.getInt("cond") == droplist[0] && st.dropItems(droplist[1], 1, droplist[2], droplist[3]))
            st["cond"] = (droplist[0] + 1).toString()

        return null
    }

    companion object {
        private const val qn = "Q038_DragonFangs"

        // Items
        private const val FEATHER_ORNAMENT = 7173
        private const val TOOTH_OF_TOTEM = 7174
        private const val TOOTH_OF_DRAGON = 7175
        private const val LETTER_OF_IRIS = 7176
        private const val LETTER_OF_ROHMER = 7177

        // NPCs
        private const val LUIS = 30386
        private const val IRIS = 30034
        private const val ROHMER = 30344

        // Reward { item, adena }
        private val REWARD =
            arrayOf(intArrayOf(45, 5200), intArrayOf(627, 1500), intArrayOf(1123, 3200), intArrayOf(605, 3200))

        // Droplist
        private val DROPLIST = HashMap<Int, IntArray>()
    }
}