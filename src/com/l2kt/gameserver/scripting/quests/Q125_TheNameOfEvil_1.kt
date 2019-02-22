package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q125_TheNameOfEvil_1 : Quest(125, "The Name of Evil - 1") {
    init {

        setItemsIds(ORNITHOMIMUS_CLAW, DEINONYCHUS_BONE, EPITAPH_OF_WISDOM, GAZKH_FRAGMENT)

        addStartNpc(MUSHIKA)
        addTalkId(MUSHIKA, KARAKAWEI, ULU_KAIMU, BALU_KAIMU, CHUTA_KAIMU)

        for (i in ORNITHOMIMUS)
            addKillId(i)

        for (i in DEINONYCHUS)
            addKillId(i)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("32114-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32114-09.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(GAZKH_FRAGMENT, 1)
        } else if (event.equals("32117-08.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32117-14.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32119-14.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32120-15.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32121-16.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GAZKH_FRAGMENT, -1)
            st.giveItems(EPITAPH_OF_WISDOM, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val first = player.getQuestState(Q124_MeetingTheElroki.qn)
                if (first != null && first.isCompleted && player.level >= 76)
                    htmltext = "32114-01.htm"
                else
                    htmltext = "32114-00.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MUSHIKA -> if (cond == 1)
                        htmltext = "32114-07.htm"
                    else if (cond == 2)
                        htmltext = "32114-10.htm"
                    else if (cond > 2 && cond < 8)
                        htmltext = "32114-11.htm"
                    else if (cond == 8) {
                        htmltext = "32114-12.htm"
                        st.takeItems(EPITAPH_OF_WISDOM, -1)
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    KARAKAWEI -> if (cond == 2)
                        htmltext = "32117-01.htm"
                    else if (cond == 3)
                        htmltext = "32117-09.htm"
                    else if (cond == 4) {
                        if (st.getQuestItemsCount(ORNITHOMIMUS_CLAW) >= 2 && st.getQuestItemsCount(DEINONYCHUS_BONE) >= 2) {
                            htmltext = "32117-10.htm"
                            st.takeItems(ORNITHOMIMUS_CLAW, -1)
                            st.takeItems(DEINONYCHUS_BONE, -1)
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else {
                            htmltext = "32117-09.htm"
                            st["cond"] = "3"
                        }
                    } else if (cond == 5)
                        htmltext = "32117-15.htm"
                    else if (cond == 6 || cond == 7)
                        htmltext = "32117-16.htm"
                    else if (cond == 8)
                        htmltext = "32117-17.htm"

                    ULU_KAIMU -> if (cond == 5) {
                        npc.doCast(SkillTable.getInfo(5089, 1))
                        htmltext = "32119-01.htm"
                    } else if (cond == 6)
                        htmltext = "32119-14.htm"

                    BALU_KAIMU -> if (cond == 6) {
                        npc.doCast(SkillTable.getInfo(5089, 1))
                        htmltext = "32120-01.htm"
                    } else if (cond == 7)
                        htmltext = "32120-16.htm"

                    CHUTA_KAIMU -> if (cond == 7) {
                        npc.doCast(SkillTable.getInfo(5089, 1))
                        htmltext = "32121-01.htm"
                    } else if (cond == 8)
                        htmltext = "32121-17.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "3") ?: return null

        val npcId = npc.npcId
        if (ArraysUtil.contains(ORNITHOMIMUS, npcId)) {
            if (st.dropItems(ORNITHOMIMUS_CLAW, 1, 2, 50000))
                if (st.getQuestItemsCount(DEINONYCHUS_BONE) == 2)
                    st["cond"] = "4"
        } else if (ArraysUtil.contains(DEINONYCHUS, npcId)) {
            if (st.dropItems(DEINONYCHUS_BONE, 1, 2, 50000))
                if (st.getQuestItemsCount(ORNITHOMIMUS_CLAW) == 2)
                    st["cond"] = "4"
        }
        return null
    }

    companion object {
        const val qn = "Q125_TheNameOfEvil_1"

        private const val MUSHIKA = 32114
        private const val KARAKAWEI = 32117
        private const val ULU_KAIMU = 32119
        private const val BALU_KAIMU = 32120
        private const val CHUTA_KAIMU = 32121

        private const val ORNITHOMIMUS_CLAW = 8779
        private const val DEINONYCHUS_BONE = 8780
        private const val EPITAPH_OF_WISDOM = 8781
        private const val GAZKH_FRAGMENT = 8782

        private val ORNITHOMIMUS = intArrayOf(22200, 22201, 22202, 22219, 22224, 22742, 22744)

        private val DEINONYCHUS = intArrayOf(16067, 22203, 22204, 22205, 22220, 22225, 22743, 22745)
    }
}