package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q021_HiddenTruth : Quest(21, "Hidden Truth") {

    private var _duke: Npc? = null
    private var _page: Npc? = null

    init {

        setItemsIds(CROSS_OF_EINHASAD)

        addStartNpc(MYSTERIOUS_WIZARD)
        addTalkId(
            MYSTERIOUS_WIZARD,
            TOMBSTONE,
            VON_HELLMAN_DUKE,
            VON_HELLMAN_PAGE,
            BROKEN_BOOKSHELF,
            AGRIPEL,
            DOMINIC,
            BENEDICT,
            INNOCENTIN
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("31522-02.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31523-03.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
            spawnTheDuke(player)
        } else if (event.equals("31524-06.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            spawnThePage(player)
        } else if (event.equals("31526-08.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31526-14.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(CROSS_OF_EINHASAD, 1)
        } else if (event.equals("1", ignoreCase = true)) {
            _page!!.ai.setIntention(CtrlIntention.MOVE_TO, PAGE_LOCS[0])
            _page!!.broadcastNpcSay("Follow me...")

            startQuestTimer("2", 5000, _page, player, false)
            return null
        } else if (event.equals("2", ignoreCase = true)) {
            _page!!.ai.setIntention(CtrlIntention.MOVE_TO, PAGE_LOCS[1])

            startQuestTimer("3", 12000, _page, player, false)
            return null
        } else if (event.equals("3", ignoreCase = true)) {
            _page!!.ai.setIntention(CtrlIntention.MOVE_TO, PAGE_LOCS[2])

            startQuestTimer("4", 18000, _page, player, false)
            return null
        } else if (event.equals("4", ignoreCase = true)) {
            st["end_walk"] = "1"

            _page!!.broadcastNpcSay("Please check this bookcase, " + player.name + ".")

            startQuestTimer("5", 47000, _page, player, false)
            return null
        } else if (event.equals("5", ignoreCase = true)) {
            _page!!.broadcastNpcSay("I'm confused! Maybe it's time to go back.")
            return null
        } else if (event.equals("31328-05.htm", ignoreCase = true)) {
            if (st.hasQuestItems(CROSS_OF_EINHASAD)) {
                st.takeItems(CROSS_OF_EINHASAD, 1)
                st.giveItems(CROSS_OF_EINHASAD_NEXT_QUEST, 1)
                st.playSound(QuestState.SOUND_FINISH)
                st.exitQuest(false)
            }
        } else if (event.equals("dukeDespawn", ignoreCase = true)) {
            _duke!!.deleteMe()
            _duke = null

            return null
        } else if (event.equals("pageDespawn", ignoreCase = true)) {
            _page!!.deleteMe()
            _page = null

            return null
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 63) "31522-03.htm" else "31522-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    MYSTERIOUS_WIZARD -> htmltext = "31522-05.htm"

                    TOMBSTONE -> if (cond == 1)
                        htmltext = "31523-01.htm"
                    else if (cond == 2 || cond == 3) {
                        htmltext = "31523-04.htm"
                        spawnTheDuke(player)
                    } else if (cond > 3)
                        htmltext = "31523-04.htm"

                    VON_HELLMAN_DUKE -> if (cond == 2)
                        htmltext = "31524-01.htm"
                    else if (cond == 3) {
                        htmltext = "31524-07.htm"
                        spawnThePage(player)
                    } else if (cond > 3)
                        htmltext = "31524-07a.htm"

                    VON_HELLMAN_PAGE -> if (cond == 3) {
                        if (st.getInt("end_walk") == 1) {
                            htmltext = "31525-02.htm"
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else
                            htmltext = "31525-01.htm"
                    } else if (cond == 4)
                        htmltext = "31525-02.htm"

                    BROKEN_BOOKSHELF -> if (cond == 3 && st.getInt("end_walk") == 1 || cond == 4) {
                        htmltext = "31526-01.htm"

                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)

                        if (_page != null) {
                            cancelQuestTimer("5", _page, player)
                            cancelQuestTimer("pageDespawn", _page, player)

                            _page!!.deleteMe()
                            _page = null
                        }

                        if (_duke != null) {
                            cancelQuestTimer("dukeDespawn", _duke, player)

                            _duke!!.deleteMe()
                            _duke = null
                        }
                    } else if (cond == 5)
                        htmltext = "31526-10.htm"
                    else if (cond > 5)
                        htmltext = "31526-15.htm"

                    AGRIPEL, BENEDICT, DOMINIC -> if ((cond == 6 || cond == 7) && st.hasQuestItems(CROSS_OF_EINHASAD)) {
                        val npcId = npc.npcId

                        // For cond 6, make checks until cond 7 is activated.
                        if (cond == 6) {
                            var npcId1 = 0
                            var npcId2 = 0
                            if (npcId == AGRIPEL) {
                                npcId1 = BENEDICT
                                npcId2 = DOMINIC
                            } else if (npcId == BENEDICT) {
                                npcId1 = AGRIPEL
                                npcId2 = DOMINIC
                            } else if (npcId == DOMINIC) {
                                npcId1 = AGRIPEL
                                npcId2 = BENEDICT
                            }

                            if (st.getInt(npcId1.toString()) == 1 && st.getInt(npcId2.toString()) == 1) {
                                st["cond"] = "7"
                                st.playSound(QuestState.SOUND_MIDDLE)
                            } else
                                st[npcId.toString()] = "1"
                        }

                        htmltext = npcId.toString() + "-01.htm"
                    }

                    INNOCENTIN -> if (cond == 7 && st.hasQuestItems(CROSS_OF_EINHASAD))
                        htmltext = "31328-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> if (npc.npcId == INNOCENTIN)
                htmltext = "31328-06.htm"
            else
                htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    private fun spawnTheDuke(player: Player) {
        if (_duke == null) {
            _duke = addSpawn(VON_HELLMAN_DUKE, 51432, -54570, -3136, 0, false, 0, true)
            _duke!!.broadcastNpcSay("Who awoke me?")

            startQuestTimer("dukeDespawn", 300000, _duke, player, false)
        }
    }

    private fun spawnThePage(player: Player) {
        if (_page == null) {
            _page = addSpawn(VON_HELLMAN_PAGE, 51608, -54520, -3168, 0, false, 0, true)
            _page!!.broadcastNpcSay("My master has instructed me to be your guide, " + player.name + ".")

            startQuestTimer("1", 4000, _page, player, false)
            startQuestTimer("pageDespawn", 90000, _page, player, false)
        }
    }

    companion object {
        private const val qn = "Q021_HiddenTruth"

        // NPCs
        private const val MYSTERIOUS_WIZARD = 31522
        private const val TOMBSTONE = 31523
        private const val VON_HELLMAN_DUKE = 31524
        private const val VON_HELLMAN_PAGE = 31525
        private const val BROKEN_BOOKSHELF = 31526
        private const val AGRIPEL = 31348
        private const val DOMINIC = 31350
        private const val BENEDICT = 31349
        private const val INNOCENTIN = 31328

        // Items
        private const val CROSS_OF_EINHASAD = 7140
        private const val CROSS_OF_EINHASAD_NEXT_QUEST = 7141

        private val PAGE_LOCS =
            arrayOf(Location(51992, -54424, -3160), Location(52328, -53400, -3160), Location(51928, -51656, -3096))
    }
}