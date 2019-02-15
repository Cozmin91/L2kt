package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q416_PathToAnOrcShaman : Quest(416, "Path To An Orc Shaman") {
    init {

        setItemsIds(
            FIRE_CHARM,
            KASHA_BEAR_PELT,
            KASHA_BLADE_SPIDER_HUSK,
            FIERY_EGG_1,
            HESTUI_MASK,
            FIERY_EGG_2,
            TOTEM_SPIRIT_CLAW,
            TATARU_LETTER,
            FLAME_CHARM,
            GRIZZLY_BLOOD,
            BLOOD_CAULDRON,
            SPIRIT_NET,
            BOUND_DURKA_SPIRIT,
            DURKA_PARASITE,
            TOTEM_SPIRIT_BLOOD
        )

        addStartNpc(TATARU_ZU_HESTUI)
        addTalkId(
            TATARU_ZU_HESTUI,
            UMOS,
            HESTUI_TOTEM_SPIRIT,
            DUDA_MARA_TOTEM_SPIRIT,
            MOIRA,
            TOTEM_SPIRIT_OF_GANDI,
            DEAD_LEOPARD_CARCASS
        )

        addKillId(
            VENOMOUS_SPIDER,
            ARACHNID_TRACKER,
            GRIZZLY_BEAR,
            SCARLET_SALAMANDER,
            KASHA_BLADE_SPIDER,
            KASHA_BEAR,
            DURKA_SPIRIT,
            BLACK_LEOPARD
        )
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // TATARU ZU HESTUI
        if (event.equals("30585-05.htm", ignoreCase = true)) {
            if (player.classId != ClassId.ORC_MYSTIC)
                htmltext = if (player.classId == ClassId.ORC_SHAMAN) "30585-02a.htm" else "30585-02.htm"
            else if (player.level < 19)
                htmltext = "30585-03.htm"
            else if (st.hasQuestItems(MASK_OF_MEDIUM))
                htmltext = "30585-04.htm"
        } else if (event.equals("30585-06.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
            st.giveItems(FIRE_CHARM, 1)
        } else if (event.equals("30585-11b.htm", ignoreCase = true)) {
            st["cond"] = "5"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TOTEM_SPIRIT_CLAW, 1)
            st.giveItems(TATARU_LETTER, 1)
        } else if (event.equals("30585-11c.htm", ignoreCase = true)) {
            st["cond"] = "12"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(TOTEM_SPIRIT_CLAW, 1)
        } else if (event.equals("30592-03.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(HESTUI_MASK, 1)
            st.takeItems(FIERY_EGG_2, 1)
            st.giveItems(TOTEM_SPIRIT_CLAW, 1)
        } else if (event.equals("30593-03.htm", ignoreCase = true)) {
            st["cond"] = "9"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.takeItems(BLOOD_CAULDRON, 1)
            st.giveItems(SPIRIT_NET, 1)
        } else if (event.equals("32057-02.htm", ignoreCase = true)) {
            st["cond"] = "14"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32057-05.htm", ignoreCase = true)) {
            st["cond"] = "21"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("32090-04.htm", ignoreCase = true)) {
            st["cond"] = "18"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30502-07.htm", ignoreCase = true)) {
            st.takeItems(TOTEM_SPIRIT_BLOOD, -1)
            st.giveItems(MASK_OF_MEDIUM, 1)
            st.rewardExpAndSp(3200, 2600)
            player.broadcastPacket(SocialAction(player, 3))
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }// UMOS
        // DEAD LEOPARD CARCASS
        // TOTEM SPIRIT OF GANDI
        // DUDA MARA TOTEM SPIRIT
        // HESTUI TOTEM SPIRIT

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "30585-01.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    TATARU_ZU_HESTUI -> if (cond == 1)
                        htmltext = "30585-07.htm"
                    else if (cond == 2) {
                        htmltext = "30585-08.htm"
                        st["cond"] = "3"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FIERY_EGG_1, 1)
                        st.takeItems(FIRE_CHARM, 1)
                        st.takeItems(KASHA_BEAR_PELT, 1)
                        st.takeItems(KASHA_BLADE_SPIDER_HUSK, 1)
                        st.giveItems(FIERY_EGG_2, 1)
                        st.giveItems(HESTUI_MASK, 1)
                    } else if (cond == 3)
                        htmltext = "30585-09.htm"
                    else if (cond == 4)
                        htmltext = "30585-10.htm"
                    else if (cond == 5)
                        htmltext = "30585-12.htm"
                    else if (cond > 5 && cond < 12)
                        htmltext = "30585-13.htm"
                    else if (cond == 12)
                        htmltext = "30585-11c.htm"

                    HESTUI_TOTEM_SPIRIT -> if (cond == 3)
                        htmltext = "30592-01.htm"
                    else if (cond == 4)
                        htmltext = "30592-04.htm"
                    else if (cond > 4 && cond < 12)
                        htmltext = "30592-05.htm"

                    UMOS -> if (cond == 5) {
                        htmltext = "30502-01.htm"
                        st["cond"] = "6"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(TATARU_LETTER, 1)
                        st.giveItems(FLAME_CHARM, 1)
                    } else if (cond == 6)
                        htmltext = "30502-02.htm"
                    else if (cond == 7) {
                        htmltext = "30502-03.htm"
                        st["cond"] = "8"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(FLAME_CHARM, 1)
                        st.takeItems(GRIZZLY_BLOOD, 3)
                        st.giveItems(BLOOD_CAULDRON, 1)
                    } else if (cond == 8)
                        htmltext = "30502-04.htm"
                    else if (cond == 9 || cond == 10)
                        htmltext = "30502-05.htm"
                    else if (cond == 11)
                        htmltext = "30502-06.htm"

                    MOIRA -> if (cond == 12) {
                        htmltext = "31979-01.htm"
                        st["cond"] = "13"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond > 12 && cond < 21)
                        htmltext = "31979-02.htm"
                    else if (cond == 21) {
                        htmltext = "31979-03.htm"
                        st.giveItems(MASK_OF_MEDIUM, 1)
                        st.rewardExpAndSp(3200, 3250)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(true)
                    }

                    TOTEM_SPIRIT_OF_GANDI -> if (cond == 13)
                        htmltext = "32057-01.htm"
                    else if (cond > 13 && cond < 20)
                        htmltext = "32057-03.htm"
                    else if (cond == 20)
                        htmltext = "32057-04.htm"

                    DUDA_MARA_TOTEM_SPIRIT -> if (cond == 8)
                        htmltext = "30593-01.htm"
                    else if (cond == 9)
                        htmltext = "30593-04.htm"
                    else if (cond == 10) {
                        htmltext = "30593-05.htm"
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(BOUND_DURKA_SPIRIT, 1)
                        st.giveItems(TOTEM_SPIRIT_BLOOD, 1)
                    } else if (cond == 11)
                        htmltext = "30593-06.htm"

                    DEAD_LEOPARD_CARCASS -> if (cond == 14)
                        htmltext = "32090-01a.htm"
                    else if (cond == 15) {
                        htmltext = "32090-01.htm"
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else if (cond == 16)
                        htmltext = "32090-01b.htm"
                    else if (cond == 17)
                        htmltext = "32090-02.htm"
                    else if (cond == 18)
                        htmltext = "32090-05.htm"
                    else if (cond == 19) {
                        htmltext = "32090-06.htm"
                        st["cond"] = "20"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    }
                }
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        val cond = st.getInt("cond")

        when (npc.npcId) {
            KASHA_BEAR -> if (cond == 1 && !st.hasQuestItems(KASHA_BEAR_PELT)) {
                st.giveItems(KASHA_BEAR_PELT, 1)
                if (st.hasQuestItems(FIERY_EGG_1, KASHA_BLADE_SPIDER_HUSK)) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            KASHA_BLADE_SPIDER -> if (cond == 1 && !st.hasQuestItems(KASHA_BLADE_SPIDER_HUSK)) {
                st.giveItems(KASHA_BLADE_SPIDER_HUSK, 1)
                if (st.hasQuestItems(KASHA_BEAR_PELT, FIERY_EGG_1)) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            SCARLET_SALAMANDER -> if (cond == 1 && !st.hasQuestItems(FIERY_EGG_1)) {
                st.giveItems(FIERY_EGG_1, 1)
                if (st.hasQuestItems(KASHA_BEAR_PELT, KASHA_BLADE_SPIDER_HUSK)) {
                    st["cond"] = "2"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st.playSound(QuestState.SOUND_ITEMGET)
            }

            GRIZZLY_BEAR -> if (cond == 6 && st.dropItemsAlways(GRIZZLY_BLOOD, 1, 3))
                st["cond"] = "7"

            VENOMOUS_SPIDER, ARACHNID_TRACKER -> if (cond == 9) {
                val count = st.getQuestItemsCount(DURKA_PARASITE)
                val rnd = Rnd[10]
                if (count == 5 && rnd < 1 || (count == 6 || count == 7) && rnd < 2 || count >= 8) {
                    st.playSound(QuestState.SOUND_BEFORE_BATTLE)
                    st.takeItems(DURKA_PARASITE, -1)
                    addSpawn(DURKA_SPIRIT, npc, false, 120000, true)
                } else
                    st.dropItemsAlways(DURKA_PARASITE, 1, 0)
            }

            DURKA_SPIRIT -> if (cond == 9) {
                st["cond"] = "10"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(DURKA_PARASITE, -1)
                st.takeItems(SPIRIT_NET, 1)
                st.giveItems(BOUND_DURKA_SPIRIT, 1)
            }

            BLACK_LEOPARD -> if (cond == 14) {
                if (st.getInt("leopard") > 0) {
                    st["cond"] = "15"
                    st.playSound(QuestState.SOUND_MIDDLE)

                    if (Rnd[3] < 2)
                        npc.broadcastNpcSay("My dear friend of " + player!!.name + ", who has gone on ahead of me!")
                } else
                    st["leopard"] = "1"
            } else if (cond == 16) {
                st["cond"] = "17"
                st.playSound(QuestState.SOUND_MIDDLE)

                if (Rnd[3] < 2)
                    npc.broadcastNpcSay("Listen to Tejakar Gandi, young Oroka! The spirit of the slain leopard is calling you, " + player!!.name + "!")
            } else if (cond == 18) {
                st["cond"] = "19"
                st.playSound(QuestState.SOUND_MIDDLE)
            }
        }

        return null
    }

    companion object {
        private val qn = "Q416_PathToAnOrcShaman"

        // Items
        private val FIRE_CHARM = 1616
        private val KASHA_BEAR_PELT = 1617
        private val KASHA_BLADE_SPIDER_HUSK = 1618
        private val FIERY_EGG_1 = 1619
        private val HESTUI_MASK = 1620
        private val FIERY_EGG_2 = 1621
        private val TOTEM_SPIRIT_CLAW = 1622
        private val TATARU_LETTER = 1623
        private val FLAME_CHARM = 1624
        private val GRIZZLY_BLOOD = 1625
        private val BLOOD_CAULDRON = 1626
        private val SPIRIT_NET = 1627
        private val BOUND_DURKA_SPIRIT = 1628
        private val DURKA_PARASITE = 1629
        private val TOTEM_SPIRIT_BLOOD = 1630
        private val MASK_OF_MEDIUM = 1631

        // NPCs
        private val TATARU_ZU_HESTUI = 30585
        private val UMOS = 30502
        private val HESTUI_TOTEM_SPIRIT = 30592
        private val DUDA_MARA_TOTEM_SPIRIT = 30593
        private val MOIRA = 31979
        private val TOTEM_SPIRIT_OF_GANDI = 32057
        private val DEAD_LEOPARD_CARCASS = 32090

        // Monsters
        private val VENOMOUS_SPIDER = 20038
        private val ARACHNID_TRACKER = 20043
        private val GRIZZLY_BEAR = 20335
        private val SCARLET_SALAMANDER = 20415
        private val KASHA_BLADE_SPIDER = 20478
        private val KASHA_BEAR = 20479
        private val DURKA_SPIRIT = 27056
        private val BLACK_LEOPARD = 27319
    }
}