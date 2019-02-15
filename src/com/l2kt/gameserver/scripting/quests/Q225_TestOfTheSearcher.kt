package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q225_TestOfTheSearcher : Quest(225, "Test of the Searcher") {
    init {

        setItemsIds(
            LUTHER_LETTER,
            ALEX_WARRANT,
            LEIRYNN_ORDER_1,
            DELU_TOTEM,
            LEIRYNN_ORDER_2,
            CHIEF_KALKI_FANG,
            LEIRYNN_REPORT,
            STRANGE_MAP,
            LAMBERT_MAP,
            ALEX_LETTER,
            ALEX_ORDER,
            WINE_CATALOG,
            TYRA_CONTRACT,
            RED_SPORE_DUST,
            MALRUKIAN_WINE,
            OLD_ORDER,
            JAX_DIARY,
            TORN_MAP_PIECE_1,
            TORN_MAP_PIECE_2,
            SOLT_MAP,
            MAKEL_MAP,
            COMBINED_MAP,
            RUSTED_KEY,
            GOLD_BAR,
            ALEX_RECOMMEND
        )

        addStartNpc(LUTHER)
        addTalkId(ALEX, TYRA, TREE, STRONG_WOODEN_CHEST, LUTHER, LEIRYNN, BORYS, JAX)

        addAttackId(DELU_LIZARDMAN_SHAMAN)
        addKillId(HANGMAN_TREE, ROAD_SCAVENGER, GIANT_FUNGUS, DELU_LIZARDMAN_SHAMAN, DELU_CHIEF_KALKIS, NEER_BODYGUARD)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // LUTHER
        if (event.equals("30690-05.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(LUTHER_LETTER, 1)

            if (!player.memos.getBool("secondClassChange39", false)) {
                htmltext = "30690-05a.htm"
                st.giveItems(DIMENSIONAL_DIAMOND, DF_REWARD_39[player.classId.id] ?: 0)
                player.memos.set("secondClassChange39", true)
            }
        } else if (event.equals("30291-07.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(LEIRYNN_REPORT, 1)
            st.takeItems(STRANGE_MAP, 1)
            st.giveItems(ALEX_LETTER, 1)
            st.giveItems(ALEX_ORDER, 1)
            st.giveItems(LAMBERT_MAP, 1)
        } else if (event.equals("30420-01a.htm", ignoreCase = true)) {
            st["cond"] = "10"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(WINE_CATALOG, 1)
            st.giveItems(TYRA_CONTRACT, 1)
        } else if (event.equals("30730-01d.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(OLD_ORDER, 1)
            st.giveItems(JAX_DIARY, 1)
        } else if (event.equals("30627-01a.htm", ignoreCase = true)) {
            if (_strongWoodenChest == null) {
                if (st.getInt("cond") == 16) {
                    st["cond"] = "17"
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st.giveItems(RUSTED_KEY, 1)
                }

                _strongWoodenChest = addSpawn(STRONG_WOODEN_CHEST, 10098, 157287, -2406, 0, false, 0, true)
                startQuestTimer("chest_despawn", 300000, null, player, false)
            }
        } else if (event.equals("30628-01a.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(RUSTED_KEY))
                htmltext = "30628-02.htm"
            else {
                st["cond"] = "18"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(RUSTED_KEY, -1)
                st.giveItems(GOLD_BAR, 20)

                _strongWoodenChest!!.deleteMe()
                _strongWoodenChest = null
                cancelQuestTimer("chest_despawn", null, player)
            }
        } else if (event.equals("chest_despawn", ignoreCase = true)) {
            _strongWoodenChest!!.deleteMe()
            _strongWoodenChest = null
            return null
        }// STRONG WOODEN CHEST DESPAWN
        // STRONG WOODEN CHEST
        // TREE
        // JAX
        // TYRA
        // ALEX

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (player.classId != ClassId.ROGUE && player.classId != ClassId.ELVEN_SCOUT && player.classId != ClassId.ASSASSIN && player.classId != ClassId.SCAVENGER)
                htmltext = "30690-01.htm"
            else if (player.level < 39)
                htmltext = "30690-02.htm"
            else
                htmltext = if (player.classId == ClassId.SCAVENGER) "30690-04.htm" else "30690-03.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    LUTHER -> if (cond == 1)
                        htmltext = "30690-06.htm"
                    else if (cond > 1 && cond < 19)
                        htmltext = "30690-07.htm"
                    else if (cond == 19) {
                        htmltext = "30690-08.htm"
                        st.takeItems(ALEX_RECOMMEND, 1)
                        st.giveItems(MARK_OF_SEARCHER, 1)
                        st.rewardExpAndSp(37831, 18750)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    ALEX -> if (cond == 1) {
                        htmltext = "30291-01.htm"
                        st["cond"] = "2"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LUTHER_LETTER, 1)
                        st.giveItems(ALEX_WARRANT, 1)
                    } else if (cond == 2)
                        htmltext = "30291-02.htm"
                    else if (cond > 2 && cond < 7)
                        htmltext = "30291-03.htm"
                    else if (cond == 7)
                        htmltext = "30291-04.htm"
                    else if (cond > 7 && cond < 13)
                        htmltext = "30291-08.htm"
                    else if (cond > 12 && cond < 16)
                        htmltext = "30291-09.htm"
                    else if (cond > 15 && cond < 18)
                        htmltext = "30291-10.htm"
                    else if (cond == 18) {
                        htmltext = "30291-11.htm"
                        st["cond"] = "19"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALEX_ORDER, 1)
                        st.takeItems(COMBINED_MAP, 1)
                        st.takeItems(GOLD_BAR, -1)
                        st.giveItems(ALEX_RECOMMEND, 1)
                    } else if (cond == 19)
                        htmltext = "30291-12.htm"

                    LEIRYNN -> if (cond == 2) {
                        htmltext = "30728-01.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALEX_WARRANT, 1)
                        st.giveItems(LEIRYNN_ORDER_1, 1)
                    } else if (cond == 3)
                        htmltext = "30728-02.htm"
                    else if (cond == 4) {
                        htmltext = "30728-03.htm"
                        st["cond"] = "5"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(DELU_TOTEM, -1)
                        st.takeItems(LEIRYNN_ORDER_1, 1)
                        st.giveItems(LEIRYNN_ORDER_2, 1)
                    } else if (cond == 5)
                        htmltext = "30728-04.htm"
                    else if (cond == 6) {
                        htmltext = "30728-05.htm"
                        st["cond"] = "7"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(CHIEF_KALKI_FANG, 1)
                        st.takeItems(LEIRYNN_ORDER_2, 1)
                        st.giveItems(LEIRYNN_REPORT, 1)
                    } else if (cond == 7)
                        htmltext = "30728-06.htm"
                    else if (cond > 7)
                        htmltext = "30728-07.htm"

                    BORYS -> if (cond == 8) {
                        htmltext = "30729-01.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(ALEX_LETTER, 1)
                        st.giveItems(WINE_CATALOG, 1)
                    } else if (cond > 8 && cond < 12)
                        htmltext = "30729-02.htm"
                    else if (cond == 12) {
                        htmltext = "30729-03.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(MALRUKIAN_WINE, 1)
                        st.takeItems(WINE_CATALOG, 1)
                        st.giveItems(OLD_ORDER, 1)
                    } else if (cond == 13)
                        htmltext = "30729-04.htm"
                    else if (cond > 13)
                        htmltext = "30729-05.htm"

                    TYRA -> if (cond == 9)
                        htmltext = "30420-01.htm"
                    else if (cond == 10)
                        htmltext = "30420-02.htm"
                    else if (cond == 11) {
                        htmltext = "30420-03.htm"
                        st["cond"] = "12"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(RED_SPORE_DUST, -1)
                        st.takeItems(TYRA_CONTRACT, 1)
                        st.giveItems(MALRUKIAN_WINE, 1)
                    } else if (cond > 11)
                        htmltext = "30420-04.htm"

                    JAX -> if (cond == 13)
                        htmltext = "30730-01.htm"
                    else if (cond == 14)
                        htmltext = "30730-02.htm"
                    else if (cond == 15) {
                        htmltext = "30730-03.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(LAMBERT_MAP, 1)
                        st.takeItems(MAKEL_MAP, 1)
                        st.takeItems(JAX_DIARY, 1)
                        st.takeItems(SOLT_MAP, 1)
                        st.giveItems(COMBINED_MAP, 1)
                    } else if (cond > 15)
                        htmltext = "30730-04.htm"

                    TREE -> if (cond == 16 || cond == 17)
                        htmltext = "30627-01.htm"

                    STRONG_WOODEN_CHEST -> if (cond == 17)
                        htmltext = "30628-01.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (st.hasQuestItems(LEIRYNN_ORDER_1) && !npc.isScriptValue(1)) {
            npc.scriptValue = 1
            addSpawn(NEER_BODYGUARD, npc, false, 200000, true)
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st: QuestState?

        when (npc.npcId) {
            DELU_LIZARDMAN_SHAMAN -> {
                st = checkPlayerCondition(player, npc, "cond", "3")
                if (st == null)
                    return null

                if (st.dropItemsAlways(DELU_TOTEM, 1, 10))
                    st["cond"] = "4"
            }

            DELU_CHIEF_KALKIS -> {
                st = checkPlayerCondition(player, npc, "cond", "5")
                if (st == null)
                    return null

                st["cond"] = "6"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.giveItems(CHIEF_KALKI_FANG, 1)
                st.giveItems(STRANGE_MAP, 1)
            }

            GIANT_FUNGUS -> {
                st = checkPlayerCondition(player, npc, "cond", "10")
                if (st == null)
                    return null

                if (st.dropItemsAlways(RED_SPORE_DUST, 1, 10))
                    st["cond"] = "11"
            }

            ROAD_SCAVENGER -> {
                st = checkPlayerCondition(player, npc, "cond", "14")
                if (st == null)
                    return null

                if (!st.hasQuestItems(SOLT_MAP) && st.dropItems(TORN_MAP_PIECE_1, 1, 4, 500000)) {
                    st.takeItems(TORN_MAP_PIECE_1, -1)
                    st.giveItems(SOLT_MAP, 1)

                    if (st.hasQuestItems(MAKEL_MAP))
                        st["cond"] = "15"
                }
            }

            HANGMAN_TREE -> {
                st = checkPlayerCondition(player, npc, "cond", "14")
                if (st == null)
                    return null

                if (!st.hasQuestItems(MAKEL_MAP) && st.dropItems(TORN_MAP_PIECE_2, 1, 4, 500000)) {
                    st.takeItems(TORN_MAP_PIECE_2, -1)
                    st.giveItems(MAKEL_MAP, 1)

                    if (st.hasQuestItems(SOLT_MAP))
                        st["cond"] = "15"
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q225_TestOfTheSearcher"

        // Items
        private val LUTHER_LETTER = 2784
        private val ALEX_WARRANT = 2785
        private val LEIRYNN_ORDER_1 = 2786
        private val DELU_TOTEM = 2787
        private val LEIRYNN_ORDER_2 = 2788
        private val CHIEF_KALKI_FANG = 2789
        private val LEIRYNN_REPORT = 2790
        private val STRANGE_MAP = 2791
        private val LAMBERT_MAP = 2792
        private val ALEX_LETTER = 2793
        private val ALEX_ORDER = 2794
        private val WINE_CATALOG = 2795
        private val TYRA_CONTRACT = 2796
        private val RED_SPORE_DUST = 2797
        private val MALRUKIAN_WINE = 2798
        private val OLD_ORDER = 2799
        private val JAX_DIARY = 2800
        private val TORN_MAP_PIECE_1 = 2801
        private val TORN_MAP_PIECE_2 = 2802
        private val SOLT_MAP = 2803
        private val MAKEL_MAP = 2804
        private val COMBINED_MAP = 2805
        private val RUSTED_KEY = 2806
        private val GOLD_BAR = 2807
        private val ALEX_RECOMMEND = 2808

        // Rewards
        private val MARK_OF_SEARCHER = 2809
        private val DIMENSIONAL_DIAMOND = 7562

        // NPCs
        private val ALEX = 30291
        private val TYRA = 30420
        private val TREE = 30627
        private val STRONG_WOODEN_CHEST = 30628
        private val LUTHER = 30690
        private val LEIRYNN = 30728
        private val BORYS = 30729
        private val JAX = 30730

        // Monsters
        private val HANGMAN_TREE = 20144
        private val ROAD_SCAVENGER = 20551
        private val GIANT_FUNGUS = 20555
        private val DELU_LIZARDMAN_SHAMAN = 20781
        private val DELU_CHIEF_KALKIS = 27093
        private val NEER_BODYGUARD = 27092

        private var _strongWoodenChest: Npc? = null // Used to avoid to spawn multiple instances.
    }
}