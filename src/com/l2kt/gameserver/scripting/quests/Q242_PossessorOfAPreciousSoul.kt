package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState

class Q242_PossessorOfAPreciousSoul : Quest(242, "Possessor of a Precious Soul - 2") {
    init {

        setItemsIds(GOLDEN_HAIR, SORCERY_INGREDIENT, ORB_OF_BINDING)

        addStartNpc(VIRGIL)
        addTalkId(
            VIRGIL,
            KASSANDRA,
            OGMAR,
            MYSTERIOUS_KNIGHT,
            ANGEL_CORPSE,
            KALIS,
            MATILD,
            CORNERSTONE,
            FALLEN_UNICORN,
            PURE_UNICORN
        )

        addKillId(RESTRAINER_OF_GLORY)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var npc = npc
        var htmltext: String? = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        // Kasandra
        if (event.equals("31743-05.htm", ignoreCase = true)) {
            st["cond"] = "2"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31744-02.htm", ignoreCase = true)) {
            st["cond"] = "3"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("31751-02.htm", ignoreCase = true)) {
            st["cond"] = "4"
            st["angel"] = "0"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30759-02.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30759-05.htm", ignoreCase = true)) {
            if (st.hasQuestItems(SORCERY_INGREDIENT)) {
                st["orb"] = "0"
                st["cornerstone"] = "0"
                st["cond"] = "9"
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(GOLDEN_HAIR, 1)
                st.takeItems(SORCERY_INGREDIENT, 1)
            } else {
                st["cond"] = "7"
                htmltext = "30759-02.htm"
            }
        } else if (event.equals("30738-02.htm", ignoreCase = true)) {
            st["cond"] = "8"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(SORCERY_INGREDIENT, 1)
        } else if (event.equals("31748-03.htm", ignoreCase = true)) {
            if (st.hasQuestItems(ORB_OF_BINDING)) {
                npc!!.deleteMe()
                st.takeItems(ORB_OF_BINDING, 1)

                var cornerstones = st.getInt("cornerstone")
                cornerstones++
                if (cornerstones == 4) {
                    st.unset("orb")
                    st.unset("cornerstone")
                    st["cond"] = "10"
                    st.playSound(QuestState.SOUND_MIDDLE)
                } else
                    st["cornerstone"] = Integer.toString(cornerstones)
            } else
                htmltext = null
        } else if (event.equals("spu", ignoreCase = true)) {
            addSpawn(PURE_UNICORN, 85884, -76588, -3470, 0, false, 0, true)
            return null
        } else if (event.equals("dspu", ignoreCase = true)) {
            npc!!.spawn.setRespawnState(false)
            npc.deleteMe()
            startQuestTimer("sfu", 2000, null, player, false)
            return null
        } else if (event.equals("sfu", ignoreCase = true)) {
            npc = addSpawn(FALLEN_UNICORN, 85884, -76588, -3470, 0, false, 0, true)
            npc!!.spawn.setRespawnState(true)
            return null
        }// Spawn Fallen Unicorn
        // Despawn Pure Unicorn
        // Spawn Pure Unicorn
        // Cornerstone
        // Matild
        // Kalis
        // Mysterious Knight
        // Ogmar

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(qn) ?: return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> if (st.hasQuestItems(VIRGIL_LETTER)) {
                if (!player.isSubClassActive || player.level < 60)
                    htmltext = "31742-02.htm"
                else {
                    htmltext = "31742-03.htm"
                    st.state = Quest.STATE_STARTED
                    st["cond"] = "1"
                    st.playSound(QuestState.SOUND_ACCEPT)
                    st.takeItems(VIRGIL_LETTER, 1)
                }
            }

            Quest.STATE_STARTED -> run{
                if (!player.isSubClassActive)
                    return@run

                val cond = st.getInt("cond")
                when (npc.npcId) {
                    VIRGIL -> if (cond == 1)
                        htmltext = "31742-04.htm"
                    else if (cond == 2)
                        htmltext = "31742-05.htm"

                    KASSANDRA -> if (cond == 1)
                        htmltext = "31743-01.htm"
                    else if (cond == 2)
                        htmltext = "31743-06.htm"
                    else if (cond == 11) {
                        htmltext = "31743-07.htm"
                        st.giveItems(CARADINE_LETTER, 1)
                        st.rewardExpAndSp(455764, 0)
                        player.broadcastPacket(SocialAction(player, 3))
                        st.playSound(QuestState.SOUND_FINISH)
                        st.exitQuest(false)
                    }

                    OGMAR -> if (cond == 2)
                        htmltext = "31744-01.htm"
                    else if (cond == 3)
                        htmltext = "31744-03.htm"

                    MYSTERIOUS_KNIGHT -> if (cond == 3)
                        htmltext = "31751-01.htm"
                    else if (cond == 4)
                        htmltext = "31751-03.htm"
                    else if (cond == 5) {
                        if (st.hasQuestItems(GOLDEN_HAIR)) {
                            htmltext = "31751-04.htm"
                            st["cond"] = "6"
                            st.playSound(QuestState.SOUND_MIDDLE)
                        } else {
                            htmltext = "31751-03.htm"
                            st["cond"] = "4"
                        }
                    } else if (cond == 6)
                        htmltext = "31751-05.htm"

                    ANGEL_CORPSE -> if (cond == 4) {
                        npc.deleteMe()
                        var hair = st.getInt("angel")
                        hair++

                        if (hair == 4) {
                            htmltext = "31752-02.htm"
                            st.unset("angel")
                            st["cond"] = "5"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.giveItems(GOLDEN_HAIR, 1)
                        } else {
                            st["angel"] = Integer.toString(hair)
                            htmltext = "31752-01.htm"
                        }
                    } else if (cond == 5)
                        htmltext = "31752-01.htm"

                    KALIS -> if (cond == 6)
                        htmltext = "30759-01.htm"
                    else if (cond == 7)
                        htmltext = "30759-03.htm"
                    else if (cond == 8) {
                        if (st.hasQuestItems(SORCERY_INGREDIENT))
                            htmltext = "30759-04.htm"
                        else {
                            htmltext = "30759-03.htm"
                            st["cond"] = "7"
                        }
                    } else if (cond == 9)
                        htmltext = "30759-06.htm"

                    MATILD -> if (cond == 7)
                        htmltext = "30738-01.htm"
                    else if (cond == 8)
                        htmltext = "30738-03.htm"

                    CORNERSTONE -> if (cond == 9) {
                        if (st.hasQuestItems(ORB_OF_BINDING))
                            htmltext = "31748-02.htm"
                        else
                            htmltext = "31748-01.htm"
                    }

                    FALLEN_UNICORN -> if (cond == 9)
                        htmltext = "31746-01.htm"
                    else if (cond == 10) {
                        if (!_unicorn)
                        // Global variable check to prevent multiple spawns
                        {
                            _unicorn = true
                            npc.spawn.setRespawnState(false) // Despawn fallen unicorn
                            npc.deleteMe()
                            startQuestTimer("spu", 3000, npc, player, false)
                        }
                        htmltext = "31746-02.htm"
                    }

                    PURE_UNICORN -> if (cond == 10) {
                        st["cond"] = "11"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        if (_unicorn)
                        // Global variable check to prevent multiple spawns
                        {
                            _unicorn = false
                            startQuestTimer("dspu", 3000, npc, player, false)
                        }
                        htmltext = "31747-01.htm"
                    } else if (cond == 11)
                        htmltext = "31747-02.htm"
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }
        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "9")
        if (st == null || !player!!.isSubClassActive)
            return null

        var orbs = st.getInt("orb") // check orbs internally, because player can use them before he gets them all
        if (orbs < 4) {
            orbs++
            st["orb"] = Integer.toString(orbs)
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(ORB_OF_BINDING, 1)
        }

        return null
    }

    companion object {
        private val qn = "Q242_PossessorOfAPreciousSoul"

        // NPCs
        private val VIRGIL = 31742
        private val KASSANDRA = 31743
        private val OGMAR = 31744
        private val MYSTERIOUS_KNIGHT = 31751
        private val ANGEL_CORPSE = 31752
        private val KALIS = 30759
        private val MATILD = 30738
        private val CORNERSTONE = 31748
        private val FALLEN_UNICORN = 31746
        private val PURE_UNICORN = 31747

        // Monsters
        private val RESTRAINER_OF_GLORY = 27317

        // Items
        private val VIRGIL_LETTER = 7677
        private val GOLDEN_HAIR = 7590
        private val SORCERY_INGREDIENT = 7596
        private val ORB_OF_BINDING = 7595
        private val CARADINE_LETTER = 7678

        private var _unicorn = false
    }
}