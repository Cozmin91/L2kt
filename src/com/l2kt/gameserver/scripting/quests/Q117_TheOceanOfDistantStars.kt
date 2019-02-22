package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q117_TheOceanOfDistantStars : Quest(117, "The Ocean of Distant Stars") {
    init {

        setItemsIds(GREY_STAR, ENGRAVED_HAMMER)

        addStartNpc(ABEY)
        addTalkId(ABEY, ANCIENT_GHOST, GHOST, OBI, BOX)
        addKillId(BANDIT_WARRIOR, BANDIT_INSPECTOR)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("32053-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("32055-02.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32052-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32053-04.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32076-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(ENGRAVED_HAMMER, 1)
        } else if (event.equals("32053-06.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32052-04.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32052-06.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GREY_STAR, 1)
        } else if (event.equals("32055-04.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(ENGRAVED_HAMMER, 1)
        } else if (event.equals("32054-03.htm", ignoreCase = true)) {
            st.rewardExpAndSp(63591, 0)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(false)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 39) "32053-00.htm" else "32053-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    ANCIENT_GHOST -> if (cond == 1)
                        htmltext = "32055-01.htm"
                    else if (cond > 1 && cond < 9)
                        htmltext = "32055-02.htm"
                    else if (cond == 9)
                        htmltext = "32055-03.htm"
                    else if (cond > 9)
                        htmltext = "32055-05.htm"

                    OBI -> if (cond == 2)
                        htmltext = "32052-01.htm"
                    else if (cond > 2 && cond < 6)
                        htmltext = "32052-02.htm"
                    else if (cond == 6)
                        htmltext = "32052-03.htm"
                    else if (cond == 7)
                        htmltext = "32052-04.htm"
                    else if (cond == 8)
                        htmltext = "32052-05.htm"
                    else if (cond > 8)
                        htmltext = "32052-06.htm"

                    ABEY -> if (cond == 1 || cond == 2)
                        htmltext = "32053-02.htm"
                    else if (cond == 3)
                        htmltext = "32053-03.htm"
                    else if (cond == 4)
                        htmltext = "32053-04.htm"
                    else if (cond == 5)
                        htmltext = "32053-05.htm"
                    else if (cond > 5)
                        htmltext = "32053-06.htm"

                    BOX -> if (cond == 4)
                        htmltext = "32076-01.htm"
                    else if (cond > 4)
                        htmltext = "32076-03.htm"

                    GHOST -> if (cond == 10)
                        htmltext = "32054-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "7") ?: return null

        if (st.dropItems(GREY_STAR, 1, 1, 200000))
            st["cond"] = "8"

        return null
    }

    companion object {
        private const val qn = "Q117_TheOceanOfDistantStars"

        // NPCs
        private const val ABEY = 32053
        private const val GHOST = 32054
        private const val ANCIENT_GHOST = 32055
        private const val OBI = 32052
        private const val BOX = 32076

        // Items
        private const val GREY_STAR = 8495
        private const val ENGRAVED_HAMMER = 8488

        // Monsters
        private const val BANDIT_WARRIOR = 22023
        private const val BANDIT_INSPECTOR = 22024
    }
}