package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q374_WhisperOfDreams_Part1 : Quest(374, "Whisper of Dreams, Part 1") {
    init {

        setItemsIds(DEATH_WAVE_LIGHT, CAVE_BEAST_TOOTH, SEALED_MYSTERIOUS_STONE, MYSTERIOUS_STONE)

        addStartNpc(MANAKIA)
        addTalkId(MANAKIA, TORAI)

        addKillId(CAVE_BEAST, DEATH_WAVE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        // Manakia
        if (event.equals("30515-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st["condStone"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.startsWith("30515-06-")) {
            if (st.getQuestItemsCount(CAVE_BEAST_TOOTH) >= 65 && st.getQuestItemsCount(DEATH_WAVE_LIGHT) >= 65) {
                htmltext = "30515-06.htm"
                st.playSound(QuestState.SOUND_MIDDLE)

                val reward = REWARDS[Integer.parseInt(event.substring(9, 10))]

                st.takeItems(CAVE_BEAST_TOOTH, -1)
                st.takeItems(DEATH_WAVE_LIGHT, -1)

                st.rewardItems(57, reward[2])
                st.giveItems(reward[0], reward[1])
            } else
                htmltext = "30515-07.htm"
        } else if (event.equals("30515-08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30557-02.htm", ignoreCase = true)) {
            if (st.getInt("cond") == 2 && st.hasQuestItems(SEALED_MYSTERIOUS_STONE)) {
                st["cond"] = "3"
                st.takeItems(SEALED_MYSTERIOUS_STONE, -1)
                st.giveItems(MYSTERIOUS_STONE, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
            } else
                htmltext = "30557-03.htm"
        }// Torai
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 56) "30515-01.htm" else "30515-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MANAKIA -> if (!st.hasQuestItems(SEALED_MYSTERIOUS_STONE)) {
                        if (st.getQuestItemsCount(CAVE_BEAST_TOOTH) >= 65 && st.getQuestItemsCount(DEATH_WAVE_LIGHT) >= 65)
                            htmltext = "30515-05.htm"
                        else
                            htmltext = "30515-04.htm"
                    } else {
                        if (cond == 1) {
                            htmltext = "30515-09.htm"
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "30515-10.htm"
                    }

                    TORAI -> if (cond == 2 && st.hasQuestItems(SEALED_MYSTERIOUS_STONE))
                        htmltext = "30557-01.htm"
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        // Drop tooth or light to anyone.
        var st: QuestState? = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        st!!.dropItems(if (npc.npcId == CAVE_BEAST) CAVE_BEAST_TOOTH else DEATH_WAVE_LIGHT, 1, 65, 500000)

        // Drop sealed mysterious stone to party member who still need it.
        st = getRandomPartyMember(player, npc, "condStone", "1")
        if (st == null)
            return null

        if (st.dropItems(SEALED_MYSTERIOUS_STONE, 1, 1, 1000))
            st.unset("condStone")

        return null
    }

    companion object {
        private val qn = "Q374_WhisperOfDreams_Part1"

        // NPCs
        private val MANAKIA = 30515
        private val TORAI = 30557

        // Monsters
        private val CAVE_BEAST = 20620
        private val DEATH_WAVE = 20621

        // Items
        private val CAVE_BEAST_TOOTH = 5884
        private val DEATH_WAVE_LIGHT = 5885
        private val SEALED_MYSTERIOUS_STONE = 5886
        private val MYSTERIOUS_STONE = 5887

        // Rewards
        private val REWARDS = arrayOf(
            intArrayOf(5486, 3, 2950), // Dark Crystal, 3x, 2950 adena
            intArrayOf(5487, 2, 18050), // Nightmare, 2x, 18050 adena
            intArrayOf(5488, 2, 18050), // Majestic, 2x, 18050 adena
            intArrayOf(5485, 4, 10450), // Tallum Tunic, 4, 10450 adena
            intArrayOf(5489, 6, 15550)
        )// Tallum Stockings, 6, 15550 adena
    }
}