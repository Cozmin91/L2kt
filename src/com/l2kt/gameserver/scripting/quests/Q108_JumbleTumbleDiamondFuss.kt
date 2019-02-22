package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q108_JumbleTumbleDiamondFuss : Quest(108, "Jumble, Tumble, Diamond Fuss") {
    init {

        setItemsIds(
            GOUPH_CONTRACT,
            REEP_CONTRACT,
            ELVEN_WINE,
            BRUNON_DICE,
            BRUNON_CONTRACT,
            AQUAMARINE,
            CHRYSOBERYL,
            GEM_BOX,
            COAL_PIECE,
            BRUNON_LETTER,
            BERRY_TART,
            BAT_DIAGRAM,
            STAR_DIAMOND
        )

        addStartNpc(GOUPH)
        addTalkId(GOUPH, REEP, MURDOC, AIRY, BRUNON, MARON, TOROCCO)

        addKillId(GOBLIN_BRIGAND_LEADER, GOBLIN_BRIGAND_LIEUTENANT, BLADE_BAT)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("30523-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(GOUPH_CONTRACT, 1)
        } else if (event.equals("30555-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(REEP_CONTRACT, 1)
            st.giveItems(ELVEN_WINE, 1)
        } else if (event.equals("30526-02.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BRUNON_DICE, 1)
            st.giveItems(BRUNON_CONTRACT, 1)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.race != ClassRace.DWARF)
                htmltext = "30523-00.htm"
            else if (player.level < 10)
                htmltext = "30523-01.htm"
            else
                htmltext = "30523-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    GOUPH -> if (cond == 1)
                        htmltext = "30523-04.htm"
                    else if (cond > 1 && cond < 7)
                        htmltext = "30523-05.htm"
                    else if (cond == 7) {
                        htmltext = "30523-06.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GEM_BOX, 1)
                        st.giveItems(COAL_PIECE, 1)
                    } else if (cond > 7 && cond < 12)
                        htmltext = "30523-07.htm"
                    else if (cond == 12) {
                        htmltext = "30523-08.htm"
                        st.takeItems(STAR_DIAMOND, -1)
                        st.giveItems(SILVERSMITH_HAMMER, 1)
                        st.giveItems(LESSER_HEALING_POTION, 100)

                        if (player.isNewbie) {
                            st.showQuestionMark(26)
                            if (player.isMageClass) {
                                st.playTutorialVoice("tutorial_voice_027")
                                st.giveItems(SPIRITSHOT_FOR_BEGINNERS, 3000)
                            } else {
                                st.playTutorialVoice("tutorial_voice_026")
                                st.giveItems(SOULSHOT_FOR_BEGINNERS, 6000)
                            }
                        }

                        st.giveItems(ECHO_BATTLE, 10)
                        st.giveItems(ECHO_LOVE, 10)
                        st.giveItems(ECHO_SOLITUDE, 10)
                        st.giveItems(ECHO_FEAST, 10)
                        st.giveItems(ECHO_CELEBRATION, 10)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    REEP -> if (cond == 1) {
                        htmltext = "30516-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(GOUPH_CONTRACT, 1)
                        st.giveItems(REEP_CONTRACT, 1)
                    } else if (cond > 1)
                        htmltext = "30516-02.htm"

                    TOROCCO -> if (cond == 2)
                        htmltext = "30555-01.htm"
                    else if (cond == 3)
                        htmltext = "30555-03.htm"
                    else if (cond == 7)
                        htmltext = "30555-04.htm"
                    else if (cond > 7)
                        htmltext = "30555-05.htm"

                    MARON -> if (cond == 3) {
                        htmltext = "30529-01.htm"
                        st["cond"] = "4"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ELVEN_WINE, 1)
                        st.giveItems(BRUNON_DICE, 1)
                    } else if (cond == 4)
                        htmltext = "30529-02.htm"
                    else if (cond > 4)
                        htmltext = "30529-03.htm"

                    BRUNON -> if (cond == 4)
                        htmltext = "30526-01.htm"
                    else if (cond == 5)
                        htmltext = "30526-03.htm"
                    else if (cond == 6) {
                        htmltext = "30526-04.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BRUNON_CONTRACT, 1)
                        st.takeItems(AQUAMARINE, -1)
                        st.takeItems(CHRYSOBERYL, -1)
                        st.giveItems(GEM_BOX, 1)
                    } else if (cond == 7)
                        htmltext = "30526-05.htm"
                    else if (cond == 8) {
                        htmltext = "30526-06.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(COAL_PIECE, 1)
                        st.giveItems(BRUNON_LETTER, 1)
                    } else if (cond == 9)
                        htmltext = "30526-07.htm"
                    else if (cond > 9)
                        htmltext = "30526-08.htm"

                    MURDOC -> if (cond == 9) {
                        htmltext = "30521-01.htm"
                        st["cond"] = "10"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BRUNON_LETTER, 1)
                        st.giveItems(BERRY_TART, 1)
                    } else if (cond == 10)
                        htmltext = "30521-02.htm"
                    else if (cond > 10)
                        htmltext = "30521-03.htm"

                    AIRY -> if (cond == 10) {
                        htmltext = "30522-01.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BERRY_TART, 1)
                        st.giveItems(BAT_DIAGRAM, 1)
                    } else if (cond == 11)
                        htmltext = if (Rnd.nextBoolean()) "30522-02.htm" else "30522-04.htm"
                    else if (cond == 12)
                        htmltext = "30522-03.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        when (npc.npcId) {
            GOBLIN_BRIGAND_LEADER -> if (st.getInt("cond") == 5 && st.dropMultipleItems(LEADER_DROPLIST))
                st["cond"] = "6"

            GOBLIN_BRIGAND_LIEUTENANT -> if (st.getInt("cond") == 5 && st.dropMultipleItems(LIEUTENANT_DROPLIST))
                st["cond"] = "6"

            BLADE_BAT -> if (st.getInt("cond") == 11 && st.dropItems(STAR_DIAMOND, 1, 1, 200000)) {
                st.takeItems(BAT_DIAGRAM, 1)
                st["cond"] = "12"
            }
        }
        return null
    }

    companion object {
        private const val qn = "Q108_JumbleTumbleDiamondFuss"

        // NPCs
        private const val GOUPH = 30523
        private const val REEP = 30516
        private const val MURDOC = 30521
        private const val AIRY = 30522
        private const val BRUNON = 30526
        private const val MARON = 30529
        private const val TOROCCO = 30555

        // Items
        private const val GOUPH_CONTRACT = 1559
        private const val REEP_CONTRACT = 1560
        private const val ELVEN_WINE = 1561
        private const val BRUNON_DICE = 1562
        private const val BRUNON_CONTRACT = 1563
        private const val AQUAMARINE = 1564
        private const val CHRYSOBERYL = 1565
        private const val GEM_BOX = 1566
        private const val COAL_PIECE = 1567
        private const val BRUNON_LETTER = 1568
        private const val BERRY_TART = 1569
        private const val BAT_DIAGRAM = 1570
        private const val STAR_DIAMOND = 1571

        // Monsters
        private const val GOBLIN_BRIGAND_LEADER = 20323
        private const val GOBLIN_BRIGAND_LIEUTENANT = 20324
        private const val BLADE_BAT = 20480

        // Rewards
        private const val SILVERSMITH_HAMMER = 1511
        private const val SPIRITSHOT_FOR_BEGINNERS = 5790
        private const val SOULSHOT_FOR_BEGINNERS = 5789
        private const val ECHO_BATTLE = 4412
        private const val ECHO_LOVE = 4413
        private const val ECHO_SOLITUDE = 4414
        private const val ECHO_FEAST = 4415
        private const val ECHO_CELEBRATION = 4416
        private const val LESSER_HEALING_POTION = 1060

        private val LEADER_DROPLIST =
            arrayOf(intArrayOf(AQUAMARINE, 1, 10, 800000), intArrayOf(CHRYSOBERYL, 1, 10, 800000))

        private val LIEUTENANT_DROPLIST =
            arrayOf(intArrayOf(AQUAMARINE, 1, 10, 600000), intArrayOf(CHRYSOBERYL, 1, 10, 600000))
    }
}