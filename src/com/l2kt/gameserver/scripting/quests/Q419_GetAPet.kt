package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q419_GetAPet : Quest(419, "Get a Pet") {
    init {
        DROPLIST[20103] = intArrayOf(BLOODY_FANG, 600000)
        DROPLIST[20106] = intArrayOf(BLOODY_FANG, 750000)
        DROPLIST[20108] = intArrayOf(BLOODY_FANG, 1000000)
        DROPLIST[20460] = intArrayOf(BLOODY_CLAW, 600000)
        DROPLIST[20308] = intArrayOf(BLOODY_CLAW, 750000)
        DROPLIST[20466] = intArrayOf(BLOODY_CLAW, 1000000)
        DROPLIST[20025] = intArrayOf(BLOODY_NAIL, 600000)
        DROPLIST[20105] = intArrayOf(BLOODY_NAIL, 750000)
        DROPLIST[20034] = intArrayOf(BLOODY_NAIL, 1000000)
        DROPLIST[20474] = intArrayOf(BLOODY_KASHA_FANG, 600000)
        DROPLIST[20476] = intArrayOf(BLOODY_KASHA_FANG, 750000)
        DROPLIST[20478] = intArrayOf(BLOODY_KASHA_FANG, 1000000)
        DROPLIST[20403] = intArrayOf(BLOODY_TARANTULA_NAIL, 750000)
        DROPLIST[20508] = intArrayOf(BLOODY_TARANTULA_NAIL, 1000000)
    }

    init {

        setItemsIds(
            ANIMAL_LOVER_LIST,
            ANIMAL_SLAYER_LIST_1,
            ANIMAL_SLAYER_LIST_2,
            ANIMAL_SLAYER_LIST_3,
            ANIMAL_SLAYER_LIST_4,
            ANIMAL_SLAYER_LIST_5,
            BLOODY_FANG,
            BLOODY_CLAW,
            BLOODY_NAIL,
            BLOODY_KASHA_FANG,
            BLOODY_TARANTULA_NAIL
        )

        addStartNpc(MARTIN)
        addTalkId(MARTIN, BELLA, ELLIE, METTY)

        for (npcId in DROPLIST.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("task", ignoreCase = true)) {
            val race = player.race.ordinal

            htmltext = "30731-0" + (race + 4) + ".htm"
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(ANIMAL_SLAYER_LIST_1 + race, 1)
        } else if (event.equals("30731-12.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ANIMAL_SLAYER_LIST_1, 1)
            st.takeItems(ANIMAL_SLAYER_LIST_2, 1)
            st.takeItems(ANIMAL_SLAYER_LIST_3, 1)
            st.takeItems(ANIMAL_SLAYER_LIST_4, 1)
            st.takeItems(ANIMAL_SLAYER_LIST_5, 1)
            st.takeItems(BLOODY_FANG, -1)
            st.takeItems(BLOODY_CLAW, -1)
            st.takeItems(BLOODY_NAIL, -1)
            st.takeItems(BLOODY_KASHA_FANG, -1)
            st.takeItems(BLOODY_TARANTULA_NAIL, -1)
            st.giveItems(ANIMAL_LOVER_LIST, 1)
        } else if (event.equals("30256-03.htm", ignoreCase = true)) {
            st["progress"] = (st.getInt("progress") or 1).toString()
            if (st.getInt("progress") == 7)
                st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30072-02.htm", ignoreCase = true)) {
            st["progress"] = (st.getInt("progress") or 2).toString()
            if (st.getInt("progress") == 7)
                st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30091-02.htm", ignoreCase = true)) {
            st["progress"] = (st.getInt("progress") or 4).toString()
            if (st.getInt("progress") == 7)
                st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("test", ignoreCase = true)) {
            st["answers"] = "0"
            st["quiz"] = "20 21 22 23 24 25 26 27 28 29 30 31 32 33"
            return checkQuestions(st)
        } else if (event.equals("wrong", ignoreCase = true)) {
            st["wrong"] = (st.getInt("wrong") + 1).toString()
            return checkQuestions(st)
        } else if (event.equals("right", ignoreCase = true)) {
            st["correct"] = (st.getInt("correct") + 1).toString()
            return checkQuestions(st)
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30731-01.htm" else "30731-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                MARTIN -> if (st.hasAtLeastOneQuestItem(
                        ANIMAL_SLAYER_LIST_1,
                        ANIMAL_SLAYER_LIST_2,
                        ANIMAL_SLAYER_LIST_3,
                        ANIMAL_SLAYER_LIST_4,
                        ANIMAL_SLAYER_LIST_5
                    )
                ) {
                    val proofs =
                        st.getQuestItemsCount(BLOODY_FANG) + st.getQuestItemsCount(BLOODY_CLAW) + st.getQuestItemsCount(
                            BLOODY_NAIL
                        ) + st.getQuestItemsCount(BLOODY_KASHA_FANG) + st.getQuestItemsCount(BLOODY_TARANTULA_NAIL)
                    if (proofs == 0)
                        htmltext = "30731-09.htm"
                    else if (proofs < 50)
                        htmltext = "30731-10.htm"
                    else
                        htmltext = "30731-11.htm"
                } else if (st.getInt("progress") == 7)
                    htmltext = "30731-13.htm"
                else
                    htmltext = "30731-16.htm"

                BELLA -> htmltext = "30256-01.htm"

                METTY -> htmltext = "30072-01.htm"

                ELLIE -> htmltext = "30091-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val drop = DROPLIST[npc.npcId] ?: return null

        if (st.hasQuestItems(drop[0] - 5))
            st.dropItems(drop[0], 1, 50, drop[1])

        return null
    }

    companion object {
        private val qn = "Q419_GetAPet"

        // Items
        private val ANIMAL_LOVER_LIST = 3417
        private val ANIMAL_SLAYER_LIST_1 = 3418
        private val ANIMAL_SLAYER_LIST_2 = 3419
        private val ANIMAL_SLAYER_LIST_3 = 3420
        private val ANIMAL_SLAYER_LIST_4 = 3421
        private val ANIMAL_SLAYER_LIST_5 = 3422
        private val BLOODY_FANG = 3423
        private val BLOODY_CLAW = 3424
        private val BLOODY_NAIL = 3425
        private val BLOODY_KASHA_FANG = 3426
        private val BLOODY_TARANTULA_NAIL = 3427

        // Reward
        private val WOLF_COLLAR = 2375

        // NPCs
        private val MARTIN = 30731
        private val BELLA = 30256
        private val METTY = 30072
        private val ELLIE = 30091

        // Droplist
        private val DROPLIST = HashMap<Int, IntArray>()

        private fun checkQuestions(st: QuestState): String {
            val answers = st.getInt("correct") + st.getInt("wrong")
            if (answers < 10) {
                val questions = st["quiz"]!!.split(" ").dropLastWhile { it.isEmpty() }.toTypedArray()
                val index = Rnd[questions.size - 1]
                val question = questions[index]

                if (questions.size > 10 - answers) {
                    questions[index] = questions[questions.size - 1]

                    st["quiz"] = Arrays.copyOf(questions, questions.size - 1).joinToString(" ")
                }
                return "30731-$question.htm"
            }

            if (st.getInt("wrong") > 0) {
                st.unset("progress")
                st.unset("answers")
                st.unset("quiz")
                st.unset("wrong")
                st.unset("correct")
                return "30731-14.htm"
            }

            st.takeItems(ANIMAL_LOVER_LIST, 1)
            st.giveItems(WOLF_COLLAR, 1)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)

            return "30731-15.htm"
        }
    }
}