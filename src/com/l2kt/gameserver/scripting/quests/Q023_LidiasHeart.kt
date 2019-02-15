package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q023_LidiasHeart : Quest(23, "Lidia's Heart") {

    // NPC instance
    private var _ghost: Npc? = null

    init {

        setItemsIds(FOREST_OF_DEADMAN_MAP, SILVER_KEY, LIDIA_DIARY, SILVER_SPEAR)

        addStartNpc(INNOCENTIN)
        addTalkId(INNOCENTIN, BROKEN_BOOKSHELF, GHOST_OF_VON_HELLMANN, VIOLET, BOX, TOMBSTONE)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31328-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(FOREST_OF_DEADMAN_MAP, 1)
            st.giveItems(SILVER_KEY, 1)
        } else if (event.equals("31328-06.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31526-05.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(LIDIA_HAIRPIN)) {
                st.giveItems(LIDIA_HAIRPIN, 1)
                if (st.hasQuestItems(LIDIA_DIARY)) {
                    st["cond"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("31526-11.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(LIDIA_DIARY)) {
                st.giveItems(LIDIA_DIARY, 1)
                if (st.hasQuestItems(LIDIA_HAIRPIN)) {
                    st["cond"] = "4"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }
        } else if (event.equals("31328-11.htm", ignoreCase = true)) {
            if (st.getInt("cond") < 5) {
                st["cond"] = "5"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        } else if (event.equals("31328-19.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31524-04.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LIDIA_DIARY, 1)
        } else if (event.equals("31523-02.htm", ignoreCase = true)) {
            if (_ghost == null) {
                _ghost = addSpawn(31524, 51432, -54570, -3136, 0, false, 60000, true)
                _ghost!!.broadcastNpcSay("Who awoke me?")
                startQuestTimer("ghost_cleanup", 58000, null, player, false)
            }
        } else if (event.equals("31523-05.htm", ignoreCase = true)) {
            // Don't launch twice the same task...
            if (getQuestTimer("tomb_digger", null, player) == null)
                startQuestTimer("tomb_digger", 10000, null, player, false)
        } else if (event.equals("tomb_digger", ignoreCase = true)) {
            htmltext = "31523-06.htm"
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SILVER_KEY, 1)
        } else if (event.equals("31530-02.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(SILVER_KEY, 1)
            st.giveItems(SILVER_SPEAR, 1)
        } else if (event.equals("ghost_cleanup", ignoreCase = true)) {
            _ghost = null
            return null
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> {
                val st2 = player.getQuestState("Q022_TragedyInVonHellmannForest")
                if (st2 != null && st2.isCompleted) {
                    if (player.level >= 64)
                        htmltext = "31328-01.htm"
                    else
                        htmltext = "31328-00a.htm"
                } else
                    htmltext = "31328-00.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    INNOCENTIN -> if (cond == 1)
                        htmltext = "31328-03.htm"
                    else if (cond == 2)
                        htmltext = "31328-07.htm"
                    else if (cond == 4)
                        htmltext = "31328-08.htm"
                    else if (cond > 5)
                        htmltext = "31328-21.htm"

                    BROKEN_BOOKSHELF -> if (cond == 2) {
                        htmltext = "31526-00.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 3) {
                        if (!st.hasQuestItems(LIDIA_DIARY))
                            htmltext = if (!st.hasQuestItems(LIDIA_HAIRPIN)) "31526-02.htm" else "31526-06.htm"
                        else if (!st.hasQuestItems(LIDIA_HAIRPIN))
                            htmltext = "31526-12.htm"
                    } else if (cond > 3)
                        htmltext = "31526-13.htm"

                    GHOST_OF_VON_HELLMANN -> if (cond == 6)
                        htmltext = "31524-01.htm"
                    else if (cond > 6)
                        htmltext = "31524-05.htm"

                    TOMBSTONE -> if (cond == 6)
                        htmltext = if (_ghost == null) "31523-01.htm" else "31523-03.htm"
                    else if (cond == 7)
                        htmltext = "31523-04.htm"
                    else if (cond > 7)
                        htmltext = "31523-06.htm"

                    VIOLET -> if (cond == 8) {
                        htmltext = "31386-01.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 9)
                        htmltext = "31386-02.htm"
                    else if (cond == 10) {
                        if (st.hasQuestItems(SILVER_SPEAR)) {
                            htmltext = "31386-03.htm"
                            st.takeItems(SILVER_SPEAR, 1)
                            st.rewardItems(57, 100000)
                            st.playSound(QuestState.SOUND_FINISH)
                            st.exitQuest(false)
                        } else {
                            htmltext = "31386-02.htm"
                            st["cond"] = "9"
                        }
                    }

                    BOX -> if (cond == 9)
                        htmltext = "31530-01.htm"
                    else if (cond == 10)
                        htmltext = "31530-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> if (npc.npcId == VIOLET)
                htmltext = "31386-04.htm"
            else
                htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    companion object {
        private const val qn = "Q023_LidiasHeart"

        // NPCs
        private const val INNOCENTIN = 31328
        private const val BROKEN_BOOKSHELF = 31526
        private const val GHOST_OF_VON_HELLMANN = 31524
        private const val TOMBSTONE = 31523
        private const val VIOLET = 31386
        private const val BOX = 31530

        // Items
        private const val FOREST_OF_DEADMAN_MAP = 7063
        private const val SILVER_KEY = 7149
        private const val LIDIA_HAIRPIN = 7148
        private const val LIDIA_DIARY = 7064
        private const val SILVER_SPEAR = 7150
    }
}