package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q022_TragedyInVonHellmannForest : Quest(22, "Tragedy in von Hellmann Forest") {

    private var _ghostOfPriestInstance: Npc? = null
    private var _soulOfWellInstance: Npc? = null

    init {

        setItemsIds(
            LOST_SKULL_OF_ELF,
            REPORT_BOX,
            SEALED_REPORT_BOX,
            LETTER_OF_INNOCENTIN,
            RED_JEWEL_OF_ADVENTURER,
            GREEN_JEWEL_OF_ADVENTURER
        )

        addStartNpc(TIFAREN, INNOCENTIN)
        addTalkId(INNOCENTIN, TIFAREN, GHOST_OF_PRIEST, GHOST_OF_ADVENTURER, WELL)

        addAttackId(SOUL_OF_WELL)
        addKillId(SOUL_OF_WELL, 21553, 21554, 21555, 21556, 21561)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("31334-03.htm", ignoreCase = true)) {
            val st2 = player.getQuestState("Q021_HiddenTruth")
            if (st2 != null && st2.isCompleted && player.level >= 63)
                htmltext = "31334-02.htm"
        } else if (event.equals("31334-04.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("31334-07.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(CROSS_OF_EINHASAD))
                st["cond"] = "2"
            else
                htmltext = "31334-06.htm"
        } else if (event.equals("31334-08.htm", ignoreCase = true)) {
            if (st.hasQuestItems(CROSS_OF_EINHASAD)) {
                st["cond"] = "4"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(CROSS_OF_EINHASAD, 1)
            } else {
                st["cond"] = "2"
                htmltext = "31334-07.htm"
            }
        } else if (event.equals("31334-13.htm", ignoreCase = true)) {
            if (_ghostOfPriestInstance != null) {
                st["cond"] = "6"
                htmltext = "31334-14.htm"
            } else {
                st["cond"] = "7"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(LOST_SKULL_OF_ELF, 1)

                _ghostOfPriestInstance = addSpawn(GHOST_OF_PRIEST, 38418, -49894, -1104, 0, false, 120000, true)
                _ghostOfPriestInstance!!.broadcastNpcSay("Did you call me, " + player.name + "?")
                startQuestTimer("ghost_cleanup", 118000, null, player, false)
            }
        } else if (event.equals("31528-08.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)

            cancelQuestTimer("ghost_cleanup", null, player)

            if (_ghostOfPriestInstance != null) {
                _ghostOfPriestInstance!!.deleteMe()
                _ghostOfPriestInstance = null
            }
        } else if (event.equals("31328-10.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(LETTER_OF_INNOCENTIN, 1)
        } else if (event.equals("31529-12.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LETTER_OF_INNOCENTIN, 1)
            st.giveItems(GREEN_JEWEL_OF_ADVENTURER, 1)
        } else if (event.equals("31527-02.htm", ignoreCase = true)) {
            if (_soulOfWellInstance == null) {
                _soulOfWellInstance = addSpawn(SOUL_OF_WELL, 34860, -54542, -2048, 0, false, 0, true)

                // Attack player.
                (_soulOfWellInstance as Attackable).addDamageHate(player, 0, 99999)
                _soulOfWellInstance!!.ai.setIntention(CtrlIntention.ATTACK, player, true)
            }
        } else if (event.equals("attack_timer", ignoreCase = true)) {
            st["cond"] = "11"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(GREEN_JEWEL_OF_ADVENTURER, 1)
            st.giveItems(RED_JEWEL_OF_ADVENTURER, 1)
        } else if (event.equals("31328-13.htm", ignoreCase = true)) {
            st["cond"] = "15"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(REPORT_BOX, 1)
        } else if (event.equals("31328-21.htm", ignoreCase = true)) {
            st["cond"] = "16"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("ghost_cleanup", ignoreCase = true)) {
            _ghostOfPriestInstance!!.broadcastNpcSay("I'm confused! Maybe it's time to go back.")
            _ghostOfPriestInstance = null
            return null
        }
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> when (npc.npcId) {
                INNOCENTIN -> {
                    val st2 = player.getQuestState("Q021_HiddenTruth")
                    if (st2 != null && st2.isCompleted) {
                        if (!st.hasQuestItems(CROSS_OF_EINHASAD)) {
                            htmltext = "31328-01.htm"
                            st.giveItems(CROSS_OF_EINHASAD, 1)
                            st.playSound(QuestState.SOUND_ITEMGET)
                        } else
                            htmltext = "31328-01b.htm"
                    }
                }

                TIFAREN -> htmltext = "31334-01.htm"
            }

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    TIFAREN -> if (cond == 1 || cond == 2 || cond == 3)
                        htmltext = "31334-05.htm"
                    else if (cond == 4)
                        htmltext = "31334-09.htm"
                    else if (cond == 5 || cond == 6) {
                        if (st.hasQuestItems(LOST_SKULL_OF_ELF))
                            htmltext = if (_ghostOfPriestInstance == null) "31334-10.htm" else "31334-11.htm"
                        else {
                            htmltext = "31334-09.htm"
                            st["cond"] = "4"
                        }
                    } else if (cond == 7)
                        htmltext = if (_ghostOfPriestInstance != null) "31334-15.htm" else "31334-17.htm"
                    else if (cond > 7)
                        htmltext = "31334-18.htm"

                    INNOCENTIN -> if (cond < 3) {
                        if (!st.hasQuestItems(CROSS_OF_EINHASAD)) {
                            htmltext = "31328-01.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.giveItems(CROSS_OF_EINHASAD, 1)
                        } else
                            htmltext = "31328-01b.htm"
                    } else if (cond == 3)
                        htmltext = "31328-02.htm"
                    else if (cond == 8)
                        htmltext = "31328-03.htm"
                    else if (cond == 9)
                        htmltext = "31328-11.htm"
                    else if (cond == 14) {
                        if (st.hasQuestItems(REPORT_BOX))
                            htmltext = "31328-12.htm"
                        else
                            st["cond"] = "13"
                    } else if (cond == 15)
                        htmltext = "31328-14.htm"
                    else if (cond == 16) {
                        htmltext = if (player.level < 64) "31328-23.htm" else "31328-22.htm"
                        st.exitQuest(false)
                        st.playSound(QuestState.SOUND_FINISH)
                    }

                    GHOST_OF_PRIEST -> if (cond == 7)
                        htmltext = "31528-01.htm"
                    else if (cond == 8)
                        htmltext = "31528-08.htm"

                    GHOST_OF_ADVENTURER -> if (cond == 9) {
                        if (st.hasQuestItems(LETTER_OF_INNOCENTIN))
                            htmltext = "31529-01.htm"
                        else {
                            htmltext = "31529-10.htm"
                            st["cond"] = "8"
                        }
                    } else if (cond == 10)
                        htmltext = "31529-16.htm"
                    else if (cond == 11) {
                        if (st.hasQuestItems(RED_JEWEL_OF_ADVENTURER)) {
                            htmltext = "31529-17.htm"
                            st["cond"] = "12"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(RED_JEWEL_OF_ADVENTURER, 1)
                        } else {
                            htmltext = "31529-09.htm"
                            st["cond"] = "10"
                        }
                    } else if (cond == 12)
                        htmltext = "31529-17.htm"
                    else if (cond == 13) {
                        if (st.hasQuestItems(SEALED_REPORT_BOX)) {
                            htmltext = "31529-18.htm"
                            st["cond"] = "14"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(SEALED_REPORT_BOX, 1)
                            st.giveItems(REPORT_BOX, 1)
                        } else {
                            htmltext = "31529-10.htm"
                            st["cond"] = "12"
                        }
                    } else if (cond > 13)
                        htmltext = "31529-19.htm"

                    WELL -> if (cond == 10)
                        htmltext = "31527-01.htm"
                    else if (cond == 11)
                        htmltext = "31527-03.htm"
                    else if (cond == 12) {
                        htmltext = "31527-04.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.giveItems(SEALED_REPORT_BOX, 1)
                    } else if (cond > 12)
                        htmltext = "31527-05.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = player!!.getQuestState(qn)
        if (st == null || !st.isStarted)
            return null

        if (attacker is Summon)
            return null

        if (getQuestTimer("attack_timer", null, player) != null)
            return null

        if (st.getInt("cond") == 10)
            startQuestTimer("attack_timer", 20000, null, player, false)

        return null
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (npc.npcId != SOUL_OF_WELL) {
            if (st.getInt("cond") == 4 && st.dropItems(LOST_SKULL_OF_ELF, 1, 1, 100000))
                st["cond"] = "5"
        } else {
            cancelQuestTimer("attack_timer", null, player)

            _soulOfWellInstance = null
        }

        return null
    }

    companion object {
        private const val qn = "Q022_TragedyInVonHellmannForest"

        // NPCs
        private const val WELL = 31527
        private const val TIFAREN = 31334
        private const val INNOCENTIN = 31328
        private const val GHOST_OF_PRIEST = 31528
        private const val GHOST_OF_ADVENTURER = 31529

        // Items
        private const val CROSS_OF_EINHASAD = 7141
        private const val LOST_SKULL_OF_ELF = 7142
        private const val LETTER_OF_INNOCENTIN = 7143
        private const val GREEN_JEWEL_OF_ADVENTURER = 7144
        private const val RED_JEWEL_OF_ADVENTURER = 7145
        private const val SEALED_REPORT_BOX = 7146
        private const val REPORT_BOX = 7147

        // Monsters
        private const val SOUL_OF_WELL = 27217
    }
}