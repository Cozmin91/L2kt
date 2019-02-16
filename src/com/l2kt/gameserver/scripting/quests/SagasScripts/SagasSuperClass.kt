package com.l2kt.gameserver.scripting.quests.SagasScripts

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

open class SagasSuperClass : Quest {
    var qnu: Int = 0

    var NPC = intArrayOf()
    var Items = intArrayOf()
    var Mob = intArrayOf()

    var classid: Int = 0
    var prevclass: Int = 0

    var X = intArrayOf()
    var Y = intArrayOf()
    var Z = intArrayOf()

    private val _SpawnList = HashMap<Npc, Int>()

    constructor() : super(-1, "Saga's SuperClass") {}// initialize superclass

    constructor(id: Int, descr: String) : super(id, descr) {

        qnu = id
    }

    fun registerNPCs() {
        addStartNpc(NPC[0])
        addAttackId(Mob[2], Mob[1])
        addSkillSeeId(Mob[1])
        addFirstTalkId(NPC[4])

        for (npc in NPC)
            addTalkId(npc)

        for (mobid in Mob)
            addKillId(mobid)

        val questItemIds = Items.clone()
        questItemIds[0] = 0
        questItemIds[2] = 0 // remove Ice Crystal and Divine Stone of Wisdom
        setItemsIds(*questItemIds)

        for (Archon_Minion in 21646..21651)
            addKillId(Archon_Minion)

        for (element in Archon_Hellisha_Norm)
            addKillId(element)

        for (Guardian_Angel in 27214..27216)
            addKillId(Guardian_Angel)
    }

    fun AddSpawn(st: QuestState, mob: Npc?) {
        if(mob == null)
            return

        _SpawnList[mob] = st.player.objectId
    }

    fun DeleteSpawn(st: QuestState, npc: Npc?) {
        if (_SpawnList.containsKey(npc)) {
            _SpawnList.remove(npc)
            npc!!.deleteMe()
        }
    }

    fun findRightState(npc: Npc): QuestState? {
        if (_SpawnList.containsKey(npc)) {
            val player = World.getPlayer(_SpawnList[npc]!!)
            if (player != null)
                return player.getQuestState(name)
        }
        return null
    }

    fun giveHallishaMark(st2: QuestState?) {
        if (st2!!.getInt("spawned") == 0) {
            if (st2.getQuestItemsCount(Items[3]) >= 700) {
                st2.takeItems(Items[3], 20)
                val Archon = addSpawn(Mob[1], st2.player, false, 0, true)
                AddSpawn(st2, Archon)
                st2["spawned"] = "1"
                startQuestTimer("Archon Hellisha has despawned", 600000, Archon, st2.player, false)

                // Attack player
                (Archon as Attackable).addDamageHate(st2.player, 0, 99999)
                Archon.ai.setIntention(CtrlIntention.ATTACK, st2.player, null)
            } else {
                st2.giveItems(Items[3], 1)
                st2.playSound(QuestState.SOUND_ITEMGET)
            }
        }
    }

    fun findQuest(player: Player): QuestState? {
        val st = player.getQuestState(name)
        return if (st != null && player.classId.id == QuestClass[qnu - 70]) st else null

    }

    fun getClassId(player: Player): Int {
        return classid
    }

    fun getPrevClass(player: Player): Int {
        return prevclass
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player!!.getQuestState(name)
        var htmltext = ""
        if (st != null) {
            if (event.equals("0-011.htm", ignoreCase = true) || event.equals(
                    "0-012.htm",
                    ignoreCase = true
                ) || event.equals("0-013.htm", ignoreCase = true) || event.equals(
                    "0-014.htm",
                    ignoreCase = true
                ) || event.equals("0-015.htm", ignoreCase = true)
            )
                htmltext = event
            else if (event.equals("accept", ignoreCase = true)) {
                st["cond"] = "1"
                st.state = Quest.STATE_STARTED
                st.playSound(QuestState.SOUND_ACCEPT)
                st.giveItems(Items[10], 1)
                htmltext = "0-03.htm"
            } else if (event.equals("0-1", ignoreCase = true)) {
                if (player.level < 76) {
                    htmltext = "0-02.htm"
                    if (st.isCreated)
                        st.exitQuest(true)
                } else
                    htmltext = "0-05.htm"
            } else if (event.equals("0-2", ignoreCase = true)) {
                if (player.level >= 76) {
                    st.exitQuest(false)
                    st["cond"] = "0"
                    htmltext = "0-07.htm"
                    st.takeItems(Items[10], -1)
                    st.rewardExpAndSp(2299404, 0)
                    st.giveItems(57, 5000000)
                    st.giveItems(6622, 1)

                    val Class = getClassId(player)
                    player.setClassId(Class)
                    if (!player.isSubClassActive && player.baseClass == getPrevClass(player))
                        player.baseClass = Class

                    player.broadcastUserInfo()
                    cast(npc, player, 4339, 1)
                } else {
                    st.takeItems(Items[10], -1)
                    st.playSound(QuestState.SOUND_MIDDLE)
                    st["cond"] = "20"
                    htmltext = "0-08.htm"
                }
            } else if (event.equals("1-3", ignoreCase = true)) {
                st["cond"] = "3"
                htmltext = "1-05.htm"
            } else if (event.equals("1-4", ignoreCase = true)) {
                st["cond"] = "4"
                st.takeItems(Items[0], 1)
                if (Items[11] != 0)
                    st.takeItems(Items[11], 1)
                st.giveItems(Items[1], 1)
                htmltext = "1-06.htm"
            } else if (event.equals("2-1", ignoreCase = true)) {
                st["cond"] = "2"
                htmltext = "2-05.htm"
            } else if (event.equals("2-2", ignoreCase = true)) {
                st["cond"] = "5"
                st.takeItems(Items[1], 1)
                st.giveItems(Items[4], 1)
                htmltext = "2-06.htm"
            } else if (event.equals("3-5", ignoreCase = true)) {
                htmltext = "3-07.htm"
            } else if (event.equals("3-6", ignoreCase = true)) {
                st["cond"] = "11"
                htmltext = "3-02.htm"
            } else if (event.equals("3-7", ignoreCase = true)) {
                st["cond"] = "12"
                htmltext = "3-03.htm"
            } else if (event.equals("3-8", ignoreCase = true)) {
                st["cond"] = "13"
                st.takeItems(Items[2], 1)
                st.giveItems(Items[7], 1)
                htmltext = "3-08.htm"
            } else if (event.equals("4-1", ignoreCase = true)) {
                htmltext = "4-010.htm"
            } else if (event.equals("4-2", ignoreCase = true)) {
                st.giveItems(Items[9], 1)
                st["cond"] = "18"
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "4-011.htm"
            } else if (event.equals("4-3", ignoreCase = true)) {
                st.giveItems(Items[9], 1)
                st["cond"] = "18"
                npc!!.broadcastNpcSay(Text[13])
                st["Quest0"] = "0"
                cancelQuestTimer("Mob_2 has despawned", npc, player)
                st.playSound(QuestState.SOUND_MIDDLE)
                DeleteSpawn(st, npc)
                return null
            } else if (event.equals("5-1", ignoreCase = true)) {
                st["cond"] = "6"
                st.takeItems(Items[4], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "5-02.htm"
            } else if (event.equals("6-1", ignoreCase = true)) {
                st["cond"] = "8"
                st.takeItems(Items[5], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "6-03.htm"
            } else if (event.equals("7-1", ignoreCase = true)) {
                if (st.getInt("spawned") == 1)
                    htmltext = "7-03.htm"
                else if (st.getInt("spawned") == 0) {
                    val Mob_1 = addSpawn(Mob[0], X[0], Y[0], Z[0], 0, false, 0, true)
                    st["spawned"] = "1"
                    startQuestTimer("Mob_1 Timer 1", 500, Mob_1, player, false)
                    startQuestTimer("Mob_1 has despawned", 300000, Mob_1, player, false)
                    AddSpawn(st, Mob_1)
                    htmltext = "7-02.htm"
                } else
                    htmltext = "7-04.htm"
            } else if (event.equals("7-2", ignoreCase = true)) {
                st["cond"] = "10"
                st.takeItems(Items[6], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "7-06.htm"
            } else if (event.equals("8-1", ignoreCase = true)) {
                st["cond"] = "14"
                st.takeItems(Items[7], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "8-02.htm"
            } else if (event.equals("9-1", ignoreCase = true)) {
                st["cond"] = "17"
                st.takeItems(Items[8], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "9-03.htm"
            } else if (event.equals("10-1", ignoreCase = true)) {
                if (st.getInt("Quest0") == 0) {
                    // Spawn NPC and mob fighting each other, and register them in _Spawnlist.
                    val Mob_3 = addSpawn(Mob[2], X[1], Y[1], Z[1], 0, false, 0, true)
                    val Mob_2 = addSpawn(NPC[4], X[2], Y[2], Z[2], 0, false, 0, true)
                    AddSpawn(st, Mob_3)
                    AddSpawn(st, Mob_2)

                    st["Mob_2"] = Mob_2!!.objectId.toString()

                    st["Quest0"] = "1"
                    st["Quest1"] = "45"

                    startQuestTimer("Mob_3 Timer 1", 500, Mob_3, player, false)
                    startQuestTimer("Mob_2 Timer 1", 500, Mob_2, player, false)

                    startQuestTimer("Mob_3 has despawned", 59000, Mob_3, player, false)
                    startQuestTimer("Mob_2 has despawned", 60000, Mob_2, player, false)

                    htmltext = "10-02.htm"
                } else if (st.getInt("Quest1") == 45)
                    htmltext = "10-03.htm"
                else
                    htmltext = "10-04.htm"
            } else if (event.equals("10-2", ignoreCase = true)) {
                st["cond"] = "19"
                st.takeItems(Items[9], 1)
                cast(npc, player, 4546, 1)
                st.playSound(QuestState.SOUND_MIDDLE)
                htmltext = "10-06.htm"
            } else if (event.equals("11-9", ignoreCase = true)) {
                st["cond"] = "15"
                htmltext = "11-03.htm"
            } else if (event.equals("Mob_1 Timer 1", ignoreCase = true)) {
                // Attack player
                (npc as Attackable).addDamageHate(st.player, 0, 99999)
                npc.ai.setIntention(CtrlIntention.ATTACK, st.player, null)

                npc.broadcastNpcSay(Text[0].replace("PLAYERNAME", player.name))
                return null
            } else if (event.equals("Mob_1 has despawned", ignoreCase = true)) {
                npc!!.broadcastNpcSay(Text[1])
                st["spawned"] = "0"
                DeleteSpawn(st, npc)
                return null
            } else if (event.equals("Archon Hellisha has despawned", ignoreCase = true)) {
                st["spawned"] = "0"
                DeleteSpawn(st, npc)
                return null
            } else if (event.equals("Mob_3 Timer 1", ignoreCase = true)) {
                // Search the NPC.
                val Mob_2 = World.getObject(st.getInt("Mob_2")) as Npc ?: return null

                if (_SpawnList.containsKey(Mob_2) && _SpawnList[Mob_2] == player.objectId) {
                    (npc as Attackable).addDamageHate(Mob_2, 0, 99999)
                    npc.ai.setIntention(CtrlIntention.ATTACK, Mob_2, null)

                    npc.broadcastNpcSay(Text[14])
                }
                return null
            } else if (event.equals("Mob_3 has despawned", ignoreCase = true)) {
                npc!!.broadcastNpcSay(Text[15])
                st["Quest0"] = "2"
                DeleteSpawn(st, npc)
                return null
            } else if (event.equals("Mob_2 Timer 1", ignoreCase = true)) {
                npc!!.broadcastNpcSay(Text[7])
                startQuestTimer("Mob_2 Timer 2", 1500, npc, player, false)
                if (st.getInt("Quest1") == 45)
                    st["Quest1"] = "0"
                return null
            } else if (event.equals("Mob_2 Timer 2", ignoreCase = true)) {
                npc!!.broadcastNpcSay(Text[8].replace("PLAYERNAME", player.name))
                startQuestTimer("Mob_2 Timer 3", 10000, npc, player, false)
                return null
            } else if (event.equals("Mob_2 Timer 3", ignoreCase = true)) {
                if (st.getInt("Quest0") == 0) {
                    startQuestTimer("Mob_2 Timer 3", 13000, npc, player, false)
                    if (Rnd[2] == 0)
                        npc!!.broadcastNpcSay(Text[9].replace("PLAYERNAME", player.name))
                    else
                        npc!!.broadcastNpcSay(Text[10].replace("PLAYERNAME", player.name))
                }
                return null
            } else if (event.equals("Mob_2 has despawned", ignoreCase = true)) {
                st["Quest1"] = (st.getInt("Quest1") + 1).toString()
                if (st.getInt("Quest0") == 1 || st.getInt("Quest0") == 2 || st.getInt("Quest1") > 3) {
                    st["Quest0"] = "0"
                    if (st.getInt("Quest0") == 1)
                        npc!!.broadcastNpcSay(Text[11])
                    else
                        npc!!.broadcastNpcSay(Text[12])
                    DeleteSpawn(st, npc)
                } else
                    startQuestTimer("Mob_2 has despawned", 1000, npc, player, false)
                return null
            }
        } else
            return null
        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var htmltext = Quest.noQuestMsg
        val st = player.getQuestState(name)
        if (st != null) {
            val npcId = npc.npcId
            val cond = st.getInt("cond")
            if (st.isCompleted && npcId == NPC[0])
                htmltext = Quest.alreadyCompletedMsg
            else if (player.classId.id == getPrevClass(player)) {
                if (cond == 0) {
                    if (npcId == NPC[0])
                        htmltext = "0-01.htm"
                } else if (cond == 1) {
                    if (npcId == NPC[0])
                        htmltext = "0-04.htm"
                    else if (npcId == NPC[2])
                        htmltext = "2-01.htm"
                } else if (cond == 2) {
                    if (npcId == NPC[2])
                        htmltext = "2-02.htm"
                    else if (npcId == NPC[1])
                        htmltext = "1-01.htm"
                } else if (cond == 3) {
                    if (npcId == NPC[1] && st.hasQuestItems(Items[0])) {
                        htmltext = "1-02.htm"
                        if (Items[11] == 0 || st.hasQuestItems(Items[11]))
                            htmltext = "1-03.htm"
                    }
                } else if (cond == 4) {
                    if (npcId == NPC[1])
                        htmltext = "1-04.htm"
                    else if (npcId == NPC[2])
                        htmltext = "2-03.htm"
                } else if (cond == 5) {
                    if (npcId == NPC[2])
                        htmltext = "2-04.htm"
                    else if (npcId == NPC[5])
                        htmltext = "5-01.htm"
                } else if (cond == 6) {
                    if (npcId == NPC[5])
                        htmltext = "5-03.htm"
                    else if (npcId == NPC[6])
                        htmltext = "6-01.htm"
                } else if (cond == 7) {
                    if (npcId == NPC[6])
                        htmltext = "6-02.htm"
                } else if (cond == 8) {
                    if (npcId == NPC[6])
                        htmltext = "6-04.htm"
                    else if (npcId == NPC[7])
                        htmltext = "7-01.htm"
                } else if (cond == 9) {
                    if (npcId == NPC[7])
                        htmltext = "7-05.htm"
                } else if (cond == 10) {
                    if (npcId == NPC[7])
                        htmltext = "7-07.htm"
                    else if (npcId == NPC[3])
                        htmltext = "3-01.htm"
                } else if (cond == 11 || cond == 12) {
                    if (npcId == NPC[3]) {
                        if (st.hasQuestItems(Items[2]))
                            htmltext = "3-05.htm"
                        else
                            htmltext = "3-04.htm"
                    }
                } else if (cond == 13) {
                    if (npcId == NPC[3])
                        htmltext = "3-06.htm"
                    else if (npcId == NPC[8])
                        htmltext = "8-01.htm"
                } else if (cond == 14) {
                    if (npcId == NPC[8])
                        htmltext = "8-03.htm"
                    else if (npcId == NPC[11])
                        htmltext = "11-01.htm"
                } else if (cond == 15) {
                    if (npcId == NPC[11])
                        htmltext = "11-02.htm"
                    else if (npcId == NPC[9])
                        htmltext = "9-01.htm"
                } else if (cond == 16) {
                    if (npcId == NPC[9])
                        htmltext = "9-02.htm"
                } else if (cond == 17) {
                    if (npcId == NPC[9])
                        htmltext = "9-04.htm"
                    else if (npcId == NPC[10])
                        htmltext = "10-01.htm"
                } else if (cond == 18) {
                    if (npcId == NPC[10])
                        htmltext = "10-05.htm"
                } else if (cond == 19) {
                    if (npcId == NPC[10])
                        htmltext = "10-07.htm"
                    else if (npcId == NPC[0])
                        htmltext = "0-06.htm"
                } else if (cond == 20) {
                    if (npcId == NPC[0]) {
                        if (player.level >= 76) {
                            htmltext = "0-09.htm"
                            st.exitQuest(false)
                            st["cond"] = "0"
                            st.rewardExpAndSp(2299404, 0)
                            st.giveItems(57, 5000000)
                            st.giveItems(6622, 1)
                            val Class = getClassId(player)
                            val prevClass = getPrevClass(player)
                            player.setClassId(Class)
                            if (!player.isSubClassActive && player.baseClass == prevClass)
                                player.baseClass = Class
                            player.broadcastUserInfo()
                            cast(npc, player, 4339, 1)
                        } else
                            htmltext = "0-010.htm"
                    }
                }
            }
        }
        return htmltext
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var htmltext = ""
        val st = player.getQuestState(name)
        val npcId = npc.npcId
        if (st != null) {
            val cond = st.getInt("cond")
            if (npcId == NPC[4]) {
                if (cond == 17) {
                    val st2 = findRightState(npc)
                    if (st2 != null) {
                        player.lastQuestNpcObject = npc.objectId
                        if (st == st2) {
                            if (st.getInt("Tab") == 1) {
                                if (st.getInt("Quest0") == 0)
                                    htmltext = "4-04.htm"
                                else if (st.getInt("Quest0") == 1)
                                    htmltext = "4-06.htm"
                            } else {
                                if (st.getInt("Quest0") == 0)
                                    htmltext = "4-01.htm"
                                else if (st.getInt("Quest0") == 1)
                                    htmltext = "4-03.htm"
                            }
                        } else {
                            if (st.getInt("Tab") == 1) {
                                if (st.getInt("Quest0") == 0)
                                    htmltext = "4-05.htm"
                                else if (st.getInt("Quest0") == 1)
                                    htmltext = "4-07.htm"
                            } else {
                                if (st.getInt("Quest0") == 0)
                                    htmltext = "4-02.htm"
                            }
                        }
                    }
                } else if (cond == 18)
                    htmltext = "4-08.htm"
            }
        }
        if (htmltext === "")
            npc.showChatWindow(player)
        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer
        if (player != null) {
            val st2 = findRightState(npc) ?: return super.onAttack(npc, attacker, damage, skill)

            val cond = st2.getInt("cond")
            val st = player.getQuestState(name)
            val npcId = npc.npcId
            if (npcId == Mob[2] && st == st2 && cond == 17) {
                st["Quest0"] = (st.getInt("Quest0") + 1).toString()
                if (st.getInt("Quest0") == 1)
                    npc.broadcastNpcSay(Text[16].replace("PLAYERNAME", player.name))
                if (st.getInt("Quest0") > 15) {
                    st["Quest0"] = "1"
                    npc.broadcastNpcSay(Text[17])
                    cancelQuestTimer("Mob_3 has despawned", npc, st2.player)
                    st["Tab"] = "1"
                    DeleteSpawn(st, npc)
                }
            } else if (npcId == Mob[1] && cond == 15) {
                if (st != st2 || st == st2 && player.isInParty) {
                    npc.broadcastNpcSay(Text[5])
                    cancelQuestTimer("Archon Hellisha has despawned", npc, st2.player)
                    st2["spawned"] = "0"
                    DeleteSpawn(st2, npc)
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onSkillSee(
        npc: Npc,
        player: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (_SpawnList.containsKey(npc) && _SpawnList[npc] != player!!.objectId) {
            val quest_player = World.getPlayer(_SpawnList[npc]!!) ?: return null

            for (obj in targets) {
                if (obj === quest_player || obj === npc) {
                    val st2 = findRightState(npc) ?: return null

                    npc.broadcastNpcSay(Text[5])
                    cancelQuestTimer("Archon Hellisha has despawned", npc, st2.player)
                    st2["spawned"] = "0"
                    DeleteSpawn(st2, npc)
                }
            }
        }
        return super.onSkillSee(npc, player, skill, targets, isPet)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer
        if (player == null)
            return super.onKill(npc, player!!)

        val npcId = npc.npcId

        for (Archon_Minion in 21646..21651) {
            if (npcId == Archon_Minion) {
                val party = player.party
                if (party != null) {
                    val PartyQuestMembers = ArrayList<QuestState>()
                    for (player1 in party.members) {
                        val st1 = findQuest(player1)
                        if (st1 != null && player1.isInsideRadius(player, Config.PARTY_RANGE, false, false)) {
                            if (st1.getInt("cond") == 15)
                                PartyQuestMembers.add(st1)
                        }
                    }
                    if (PartyQuestMembers.size > 0) {
                        val st2 = Rnd[PartyQuestMembers]
                        giveHallishaMark(st2)
                    }
                } else {
                    val st1 = findQuest(player)
                    if (st1 != null) {
                        if (st1.getInt("cond") == 15)
                            giveHallishaMark(st1)
                    }
                }
                return super.onKill(npc, player)
            }
        }

        for (element in Archon_Hellisha_Norm) {
            if (npcId == element) {
                val st1 = findQuest(player)
                if (st1 != null) {
                    if (st1.getInt("cond") == 15) {
                        npc.broadcastNpcSay(Text[4])
                        st1.giveItems(Items[8], 1)
                        st1.takeItems(Items[3], -1)
                        st1["cond"] = "16"
                        st1.playSound(QuestState.SOUND_MIDDLE)
                    }
                }
                return super.onKill(npc, player)
            }
        }

        for (Guardian_Angel in 27214..27216) {
            if (npcId == Guardian_Angel) {
                val st1 = findQuest(player)
                if (st1 != null) {
                    if (st1.getInt("cond") == 6) {
                        if (st1.getInt("kills") < 9)
                            st1["kills"] = (st1.getInt("kills") + 1).toString()
                        else {
                            st1.playSound(QuestState.SOUND_MIDDLE)
                            st1.giveItems(Items[5], 1)
                            st1["cond"] = "7"
                        }
                    }
                }
                return super.onKill(npc, player)
            }
        }

        var st = player.getQuestState(name)
        if (st != null && npcId != Mob[2]) {
            val st2 = findRightState(npc) ?: return super.onKill(npc, player)

            val cond = st.getInt("cond")
            if (npcId == Mob[0] && cond == 8) {
                if (!player.isInParty) {
                    if (st == st2) {
                        npc.broadcastNpcSay(Text[12])
                        st.giveItems(Items[6], 1)
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    }
                }
                cancelQuestTimer("Mob_1 has despawned", npc, st2.player)
                st2["spawned"] = "0"
                DeleteSpawn(st2, npc)
            } else if (npcId == Mob[1] && cond == 15) {
                if (!player.isInParty) {
                    if (st == st2) {
                        npc.broadcastNpcSay(Text[4])
                        st.giveItems(Items[8], 1)
                        st.takeItems(Items[3], -1)
                        st["cond"] = "16"
                        st.playSound(QuestState.SOUND_MIDDLE)
                    } else
                        npc.broadcastNpcSay(Text[5])
                }
                cancelQuestTimer("Archon Hellisha has despawned", npc, st2.player)
                st2["spawned"] = "0"
                DeleteSpawn(st2, npc)
            }
        } else {
            if (npcId == Mob[0]) {
                st = findRightState(npc)
                if (st != null) {
                    cancelQuestTimer("Mob_1 has despawned", npc, st.player)
                    st["spawned"] = "0"
                    DeleteSpawn(st, npc)
                }
            } else if (npcId == Mob[1]) {
                st = findRightState(npc)
                if (st != null) {
                    cancelQuestTimer("Archon Hellisha has despawned", npc, st.player)
                    st["spawned"] = "0"
                    DeleteSpawn(st, npc)
                }
            }
        }
        return super.onKill(npc, player)
    }

    companion object {

        private val Text = arrayOf(
            "PLAYERNAME! Pursued to here! However, I jumped out of the Banshouren boundaries! You look at the giant as the sign of power!",
            "... Oh ... good! So it was ... let's begin!",
            "I do not have the patience ..! I have been a giant force ...! Cough chatter ah ah ah!",
            "Paying homage to those who disrupt the orderly will be PLAYERNAME's death!",
            "Now, my soul freed from the shackles of the millennium, Halixia, to the back side I come ...",
            "Why do you interfere others' battles?",
            "This is a waste of time.. Say goodbye...!",
            "...That is the enemy",
            "...Goodness! PLAYERNAME you are still looking?",
            "PLAYERNAME ... Not just to whom the victory. Only personnel involved in the fighting are eligible to share in the victory.",
            "Your sword is not an ornament. Don't you think, PLAYERNAME?",
            "Goodness! I no longer sense a battle there now.",
            "let...",
            "Only engaged in the battle to bar their choice. Perhaps you should regret.",
            "The human nation was foolish to try and fight a giant's strength.",
            "Must...Retreat... Too...Strong.",
            "PLAYERNAME. Defeat...by...retaining...and...Mo...Hacker",
            "....! Fight...Defeat...It...Fight...Defeat...It..."
        )

        private val Archon_Hellisha_Norm = intArrayOf(18212, 18214, 18215, 18216, 18218)

        private val QuestClass = intArrayOf(
            0x05,
            0x14,
            0x15,
            0x02,
            0x03,
            0x2e,
            0x30,
            0x33,
            0x34,
            0x08,
            0x17,
            0x24,
            0x09,
            0x18,
            0x25,
            0x10,
            0x11,
            0x1e,
            0x0c,
            0x1b,
            0x28,
            0x0e,
            0x1c,
            0x29,
            0x0d,
            0x06,
            0x22,
            0x21,
            0x2b,
            0x37,
            0x39
        )

        private fun cast(npc: Npc?, target: Creature, skillId: Int, level: Int) {
            target.broadcastPacket(MagicSkillUse(target, target, skillId, level, 6000, 1))
            target.broadcastPacket(MagicSkillUse(npc!!, npc, skillId, level, 6000, 1))
        }
    }
}