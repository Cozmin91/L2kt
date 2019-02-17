package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import java.util.concurrent.ConcurrentHashMap

class Zaken : L2AttackableAIScript("ai/individual") {

    private val _zakenLocation = Location(0, 0, 0)

    private var _teleportCheck: Int = 0
    private var _minionStatus: Int = 0
    private var _hate: Int = 0

    private var _hasTeleported: Boolean = false

    private var _mostHated: Creature? = null

    init {
        run{
            val info = GrandBossManager.getStatsSet(ZAKEN) ?: return@run

            // Zaken is dead, calculate the respawn time. If passed, we spawn it directly, otherwise we set a task to spawn it lately.
            if (GrandBossManager.getBossStatus(ZAKEN) == DEAD.toInt()) {
                val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                if (temp > 0)
                    startQuestTimer("zaken_unlock", temp, null, null, false)
                else
                    spawnBoss(true)
            } else
                spawnBoss(false)// Zaken is alive, spawn it using stored data.
        }
    }

    override fun registerNpcs() {
        addAggroRangeEnterId(ZAKEN, DOLL_BLADER, VALE_MASTER, PIRATE_CAPTAIN, PIRATE_ZOMBIE)
        addAttackId(ZAKEN)
        addFactionCallId(DOLL_BLADER, VALE_MASTER, PIRATE_CAPTAIN, PIRATE_ZOMBIE)
        addKillId(ZAKEN, DOLL_BLADER, VALE_MASTER, PIRATE_CAPTAIN, PIRATE_ZOMBIE)
        addSkillSeeId(ZAKEN)
        addSpellFinishedId(ZAKEN)

        addGameTimeNotify()
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (GrandBossManager.getBossStatus(ZAKEN) == DEAD.toInt() && !event.equals(
                "zaken_unlock",
                ignoreCase = true
            )
        )
            return super.onAdvEvent(event, npc, player)

        if (event.equals("1001", ignoreCase = true)) {
            if (GameTimeTaskManager.isNight) {
                var skill = SkillTable.FrequentSkill.ZAKEN_DAY_TO_NIGHT.skill
                if (npc!!.getFirstEffect(skill) == null) {
                    // Add effect "Day to Night" if not found.
                    skill!!.getEffects(npc, npc)

                    // Refresh stored Zaken location.
                    _zakenLocation.set(npc!!.position)
                }

                // Add Night regen if not found.
                skill = SkillTable.FrequentSkill.ZAKEN_REGEN_NIGHT.skill
                if (npc.getFirstEffect(skill) == null)
                    skill!!.getEffects(npc, npc)

                val mostHated = (npc as Attackable).mostHated

                // Under attack stance, but didn't yet teleported. Check most hated and current victims distance.
                if (npc.getAI().desire.intention == CtrlIntention.ATTACK && !_hasTeleported) {
                    var willTeleport = true

                    // Check most hated distance. If distance is low, Zaken doesn't teleport.
                    if (mostHated != null && mostHated.isInsideRadius(_zakenLocation, 1500, true, false))
                        willTeleport = false

                    // We're still under willTeleport possibility. Now we check each victim distance. If at least one is near Zaken, we cancel the teleport possibility.
                    if (willTeleport) {
                        for (ply in VICTIMS) {
                            if (ply.isInsideRadius(_zakenLocation, 1500, true, false)) {
                                willTeleport = false
                                continue
                            }
                        }
                    }

                    // All targets are far, clear victims list and Zaken teleport.
                    if (willTeleport) {
                        VICTIMS.clear()
                        npc.doCast(SkillTable.FrequentSkill.ZAKEN_SELF_TELE.skill)
                    }
                }

                // Potentially refresh the stored location.
                if (Rnd[20] < 1 && !_hasTeleported)
                    _zakenLocation.set(npc.position)

                // Process to cleanup hate from most hated upon 5 straight AI loops.
                if (npc.getAI().desire.intention == CtrlIntention.ATTACK && mostHated != null) {
                    if (_hate == 0) {
                        _mostHated = mostHated
                        _hate = 1
                    } else {
                        if (_mostHated === mostHated)
                            _hate++
                        else {
                            _hate = 1
                            _mostHated = mostHated
                        }
                    }
                }

                // Cleanup build hate towards Intention IDLE.
                if (npc.getAI().desire.intention == CtrlIntention.IDLE)
                    _hate = 0

                // We built enough hate ; release the current most hated target, reset the hate counter.
                if (_hate > 5) {
                    npc.stopHating(_mostHated)

                    _hate = 0
                }
            } else {
                var skill = SkillTable.FrequentSkill.ZAKEN_NIGHT_TO_DAY.skill
                if (npc!!.getFirstEffect(skill) == null) {
                    // Add effect "Night to Day" if not found.
                    skill!!.getEffects(npc, npc)

                    _teleportCheck = 3
                }

                // Add Day regen if not found.
                skill = SkillTable.FrequentSkill.ZAKEN_REGEN_DAY.skill
                if (npc!!.getFirstEffect(skill) == null)
                    skill!!.getEffects(npc, npc)
            }

            if (Rnd[40] < 1)
                npc!!.doCast(SkillTable.FrequentSkill.ZAKEN_SELF_TELE.skill)

            startQuestTimer("1001", 30000, npc, null, false)
        } else if (event.equals("1002", ignoreCase = true)) {
            // Clear victims list.
            VICTIMS.clear()

            // Teleport Zaken.
            npc!!.doCast(SkillTable.FrequentSkill.ZAKEN_SELF_TELE.skill)

            // Flag the teleport as false.
            _hasTeleported = false
        } else if (event.equals("1003", ignoreCase = true)) {
            when (_minionStatus) {
                1 -> {
                    spawnMinionOnEveryLocation(PIRATE_CAPTAIN, 1)

                    // Pass to the next spawn cycle.
                    _minionStatus = 2
                }
                2 -> {
                    spawnMinionOnEveryLocation(DOLL_BLADER, 1)

                    // Pass to the next spawn cycle.
                    _minionStatus = 3
                }
                3 -> {
                    spawnMinionOnEveryLocation(VALE_MASTER, 2)

                    // Pass to the next spawn cycle.
                    _minionStatus = 4
                }
                4 -> {
                    spawnMinionOnEveryLocation(PIRATE_ZOMBIE, 5)

                    // Pass to the next spawn cycle.
                    _minionStatus = 5
                }
                5 -> {
                    addSpawn(DOLL_BLADER, 52675, 219371, -3290, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 52687, 219596, -3368, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 52672, 219740, -3418, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 52857, 219992, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 52959, 219997, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 53381, 220151, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54236, 220948, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54885, 220144, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55264, 219860, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55399, 220263, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55679, 220129, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 56276, 220783, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 57173, 220234, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 56267, 218826, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56294, 219482, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 56094, 219113, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56364, 218967, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 57113, 218079, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56186, 217153, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55440, 218081, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55202, 217940, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55225, 218236, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54973, 218075, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 53412, 218077, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54226, 218797, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54394, 219067, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54139, 219253, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 54262, 219480, -3488, Rnd[65536], false, 0, true)

                    // Pass to the next spawn cycle.
                    _minionStatus = 6
                }
                6 -> {
                    addSpawn(PIRATE_ZOMBIE, 53412, 218077, -3488, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54413, 217132, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 54841, 217132, -3488, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 55372, 217128, -3343, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 55893, 217122, -3488, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 56282, 217237, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 56963, 218080, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 56267, 218826, -3216, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56294, 219482, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 56094, 219113, -3216, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56364, 218967, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 56276, 220783, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 57173, 220234, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54885, 220144, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55264, 219860, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55399, 220263, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55679, 220129, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54236, 220948, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54464, 219095, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54226, 218797, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54394, 219067, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54139, 219253, -3216, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 54262, 219480, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 53412, 218077, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55440, 218081, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55202, 217940, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55225, 218236, -3216, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54973, 218075, -3216, Rnd[65536], false, 0, true)

                    // Pass to the next spawn cycle.
                    _minionStatus = 7
                }
                7 -> {
                    addSpawn(PIRATE_ZOMBIE, 54228, 217504, -3216, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54181, 217168, -3216, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 54714, 217123, -3168, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 55298, 217127, -3073, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 55787, 217130, -2993, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 56284, 217216, -2944, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 56963, 218080, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 56267, 218826, -2944, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56294, 219482, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 56094, 219113, -2944, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 56364, 218967, -2944, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 56276, 220783, -2944, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 57173, 220234, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54885, 220144, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55264, 219860, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55399, 220263, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55679, 220129, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54236, 220948, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54464, 219095, -2944, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54226, 218797, -2944, Rnd[65536], false, 0, true)
                    addSpawn(VALE_MASTER, 54394, 219067, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54139, 219253, -2944, Rnd[65536], false, 0, true)
                    addSpawn(DOLL_BLADER, 54262, 219480, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 53412, 218077, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 54280, 217200, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55440, 218081, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_CAPTAIN, 55202, 217940, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 55225, 218236, -2944, Rnd[65536], false, 0, true)
                    addSpawn(PIRATE_ZOMBIE, 54973, 218075, -2944, Rnd[65536], false, 0, true)

                    cancelQuestTimer("1003", null, null)
                }
            }
        } else if (event.equals("zaken_unlock", ignoreCase = true)) {
            // Spawn the boss.
            spawnBoss(true)
        } else if (event.equals("CreateOnePrivateEx", ignoreCase = true))
            addSpawn(npc!!.npcId, npc.x, npc.y, npc.z, Rnd[65535], false, 0, true)

        return super.onAdvEvent(event, npc, player)
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        val realBypasser = if (isPet && player?.pet != null) player.pet else player

        if (ZONE.isInsideZone(npc))
            (npc as Attackable).addDamageHate(realBypasser, 1, 200)

        if (npc.npcId == ZAKEN) {
            // Feed victims list, but only if not already full.
            if (Rnd[3] < 1 && VICTIMS.size < 5)
                VICTIMS.add(player)

            // Cast a skill.
            if (Rnd[15] < 1)
                callSkills(npc, realBypasser!!)
        } else if (testCursesOnAggro(npc, realBypasser!!))
            return null

        return super.onAggro(npc, player, isPet)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        // Curses
        if (attacker is Playable && testCursesOnAttack(npc, attacker))
            return null

        if (Rnd[10] < 1)
            callSkills(npc, attacker)

        if (!GameTimeTaskManager.isNight && npc.currentHp < npc.maxHp * _teleportCheck / 4) {
            _teleportCheck -= 1
            npc.doCast(SkillTable.FrequentSkill.ZAKEN_SELF_TELE.skill)
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean): String? {
        if (caller?.npcId == ZAKEN && GameTimeTaskManager.isNight) {
            if (npc?.ai?.desire?.intention == CtrlIntention.IDLE && !_hasTeleported && caller.currentHp < 0.9 * caller.maxHp && Rnd[450] < 1) {
                // Set the teleport flag as true.
                _hasTeleported = true

                // Edit Zaken stored location.
                _zakenLocation.set(npc.position)

                // Run the 1002 timer.
                startQuestTimer("1002", 300, caller, null, false)
            }
        }
        return super.onFactionCall(npc, caller, attacker, isPet)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        if (npc.npcId == ZAKEN) {
            // Broadcast death sound.
            npc.broadcastPacket(PlaySound(1, "BS02_D", npc))

            // Flag Zaken as dead.
            GrandBossManager.setBossStatus(ZAKEN, DEAD.toInt())

            // Calculate the next respawn time.
            val respawnTime =
                (Config.SPAWN_INTERVAL_ZAKEN + Rnd[-Config.RANDOM_SPAWN_TIME_ZAKEN, Config.RANDOM_SPAWN_TIME_ZAKEN]).toLong() * 3600000

            // Cancel tasks.
            cancelQuestTimer("1001", npc, null)
            cancelQuestTimer("1003", null, null)

            // Start respawn timer.
            startQuestTimer("zaken_unlock", respawnTime, null, null, false)

            // Save the respawn time so that the info is maintained past reboots
            val info = GrandBossManager.getStatsSet(ZAKEN) ?: return null
            info.set("respawn_time", System.currentTimeMillis() + respawnTime)
            GrandBossManager.setStatsSet(ZAKEN, info)
        } else if (GrandBossManager.getBossStatus(ZAKEN) == ALIVE.toInt())
            startQuestTimer("CreateOnePrivateEx", ((30 + Rnd[60]) * 1000).toLong(), npc, null, false)

        return super.onKill(npc, killer)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (Rnd[12] < 1)
            callSkills(npc, caster!!)

        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        when (skill?.id) {
            4222 // Instant Move; a self teleport skill Zaken uses to move from one point to another. Location is computed on the fly, depending conditions/checks.
            -> {
                (npc as Attackable).cleanAllHate()
                npc.teleToLocation(_zakenLocation, 0)
            }

            4216 // Scatter Enemy ; a target teleport skill, which teleports the targeted Player to a defined, random Location.
            -> {
                (npc as Attackable).stopHating(player)
                player?.teleToLocation(LOCS[Rnd[15]], 0)
            }

            4217 // Mass Teleport ; teleport victims and targeted Player, each on a defined, random Location.
            -> {
                for (ply in VICTIMS) {
                    if (ply.isInsideRadius(player, 250, true, false)) {
                        (npc as Attackable).stopHating(ply)
                        ply.teleToLocation(LOCS[Rnd[15]], 0)
                    }
                }
                (npc as Attackable).stopHating(player)
                player?.teleToLocation(LOCS[Rnd[15]], 0)
            }
        }
        return super.onSpellFinished(npc, player, skill)
    }

    override fun onGameTime() {
        if (GameTimeTaskManager.gameTime == 0) {
            val door = DoorData.getDoor(21240006)
            door?.openMe()
        }
    }

    /**
     * Make additional actions on boss spawn : register the NPC as boss, activate tasks.
     * @param freshStart : If true, it uses static data, otherwise it uses stored data.
     */
    private fun spawnBoss(freshStart: Boolean) {
        val zaken: GrandBoss
        if (freshStart) {
            GrandBossManager.setBossStatus(ZAKEN, ALIVE.toInt())

            val loc = LOCS[Rnd[15]]
            zaken = addSpawn(ZAKEN, loc.x, loc.y, loc.z, 0, false, 0, false) as GrandBoss
        } else {
            val info = GrandBossManager.getStatsSet(ZAKEN) ?: return

            zaken = addSpawn(
                ZAKEN,
                info.getInteger("loc_x"),
                info.getInteger("loc_y"),
                info.getInteger("loc_z"),
                info.getInteger("heading"),
                false,
                0,
                false
            ) as GrandBoss
            zaken.setCurrentHpMp(info.getInteger("currentHP").toDouble(), info.getInteger("currentMP").toDouble())
        }

        GrandBossManager.addBoss(zaken)

        // Reset variables.
        _teleportCheck = 3
        _hate = 0
        _hasTeleported = false
        _mostHated = null

        // Store current Zaken position.
        _zakenLocation.set(zaken.position)

        // Clear victims list.
        VICTIMS.clear()

        // If Zaken is on its lair, begin the minions spawn cycle.
        if (ZONE.isInsideZone(zaken)) {
            _minionStatus = 1
            startQuestTimer("1003", 1700, null, null, true)
        }

        // Generic task is running from now.
        startQuestTimer("1001", 1000, zaken, null, false)

        zaken.broadcastPacket(PlaySound(1, "BS01_A", zaken))
    }

    /**
     * Spawn one [Npc] on every [Location] from the LOCS array. Process it for the roundsNumber amount.
     * @param npcId : The npcId to spawn.
     * @param roundsNumber : The rounds number to process.
     */
    private fun spawnMinionOnEveryLocation(npcId: Int, roundsNumber: Int) {
        for (loc in LOCS) {
            for (i in 0 until roundsNumber) {
                val x = loc.x + Rnd[650]
                val y = loc.y + Rnd[650]

                addSpawn(npcId, x, y, loc.z, Rnd[65536], false, 0, true)
            }
        }
    }

    companion object {
        private val ZONE = ZoneManager.getZoneById(110000, BossZone::class.java)
        private val VICTIMS = ConcurrentHashMap.newKeySet<Player>()

        private val LOCS = arrayOf(
            Location(53950, 219860, -3488),
            Location(55980, 219820, -3488),
            Location(54950, 218790, -3488),
            Location(55970, 217770, -3488),
            Location(53930, 217760, -3488),

            Location(55970, 217770, -3216),
            Location(55980, 219920, -3216),
            Location(54960, 218790, -3216),
            Location(53950, 219860, -3216),
            Location(53930, 217760, -3216),

            Location(55970, 217770, -2944),
            Location(55980, 219920, -2944),
            Location(54960, 218790, -2944),
            Location(53950, 219860, -2944),
            Location(53930, 217760, -2944)
        )

        private const val ZAKEN = 29022
        private const val DOLL_BLADER = 29023
        private const val VALE_MASTER = 29024
        private const val PIRATE_CAPTAIN = 29026
        private const val PIRATE_ZOMBIE = 29027

        private const val ALIVE: Byte = 0
        private const val DEAD: Byte = 1

        /**
         * Call skills depending of luck and specific events.
         * @param npc : The npc who casts the spell (Zaken).
         * @param target : The target Zaken currently aims at.
         */
        private fun callSkills(npc: Npc, target: WorldObject) {
            if (npc.isCastingNow)
                return

            npc.target = target

            val chance = Rnd[225]
            if (chance < 1)
                npc.doCast(SkillTable.FrequentSkill.ZAKEN_TELE.skill)
            else if (chance < 2)
                npc.doCast(SkillTable.FrequentSkill.ZAKEN_MASS_TELE.skill)
            else if (chance < 4)
                npc.doCast(SkillTable.FrequentSkill.ZAKEN_HOLD.skill)
            else if (chance < 8)
                npc.doCast(SkillTable.FrequentSkill.ZAKEN_DRAIN.skill)
            else if (chance < 15) {
                if (target !== (npc as Attackable).mostHated && npc.isInsideRadius(target, 100, false, false))
                    npc.doCast(SkillTable.FrequentSkill.ZAKEN_MASS_DUAL_ATTACK.skill)
            }

            if (Rnd.nextBoolean() && target === (npc as Attackable).mostHated)
                npc.doCast(SkillTable.FrequentSkill.ZAKEN_DUAL_ATTACK.skill)
        }
    }
}