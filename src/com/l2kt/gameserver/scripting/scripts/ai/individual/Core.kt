package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

class Core : L2AttackableAIScript("ai/individual") {

    private val _minions = ArrayList<Attackable>()

    init {
        run{
            val info = GrandBossManager.getStatsSet(CORE) ?: return@run
            val status = GrandBossManager.getBossStatus(CORE)
            if (status == DEAD.toInt()) {
                // load the unlock date and time for Core from DB
                val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                if (temp > 0) {
                    // The time has not yet expired. Mark Core as currently locked (dead).
                    startQuestTimer("core_unlock", temp, null, null, false)
                } else {
                    // The time has expired while the server was offline. Spawn Core.
                    val core = addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false) as GrandBoss
                    GrandBossManager.setBossStatus(CORE, ALIVE.toInt())
                    spawnBoss(core)
                }
            } else {
                val loc_x = info.getInteger("loc_x")
                val loc_y = info.getInteger("loc_y")
                val loc_z = info.getInteger("loc_z")
                val heading = info.getInteger("heading")
                val hp = info.getInteger("currentHP")
                val mp = info.getInteger("currentMP")

                val core = addSpawn(CORE, loc_x, loc_y, loc_z, heading, false, 0, false) as GrandBoss
                core.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                spawnBoss(core)
            }
        }
    }

    override fun registerNpcs() {
        addAttackId(CORE)
        addKillId(CORE, DEATH_KNIGHT, DOOM_WRAITH, SUSCEPTOR)
    }

    fun spawnBoss(npc: GrandBoss) {
        GrandBossManager.addBoss(npc)
        npc.broadcastPacket(PlaySound(1, "BS01_A", npc))

        // Spawn minions
        var mob: Attackable
        for (i in 0..4) {
            val x = 16800 + i * 360
            mob = addSpawn(DEATH_KNIGHT, x, 110000, npc.z, 280 + Rnd[40], false, 0, false) as Attackable
            mob.setMinion(true)
            _minions.add(mob)
            mob = addSpawn(DEATH_KNIGHT, x, 109000, npc.z, 280 + Rnd[40], false, 0, false) as Attackable
            mob.setMinion(true)
            _minions.add(mob)
            val x2 = 16800 + i * 600
            mob = addSpawn(DOOM_WRAITH, x2, 109300, npc.z, 280 + Rnd[40], false, 0, false) as Attackable
            mob.setMinion(true)
            _minions.add(mob)
        }

        for (i in 0..3) {
            val x = 16800 + i * 450
            mob = addSpawn(SUSCEPTOR, x, 110300, npc.z, 280 + Rnd[40], false, 0, false) as Attackable
            mob.setMinion(true)
            _minions.add(mob)
        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("core_unlock", ignoreCase = true)) {
            val core = addSpawn(CORE, 17726, 108915, -6480, 0, false, 0, false) as GrandBoss
            GrandBossManager.setBossStatus(CORE, ALIVE.toInt())
            spawnBoss(core)
        } else if (event.equals("spawn_minion", ignoreCase = true)) {
            val mob = addSpawn(npc!!.npcId, npc.x, npc.y, npc.z, npc.heading, false, 0, false) as Attackable
            mob.setMinion(true)
            _minions.add(mob)
        } else if (event.equals("despawn_minions", ignoreCase = true)) {
            for (i in _minions.indices) {
                val mob = _minions[i]
                mob?.decayMe()
            }
            _minions.clear()
        }
        return super.onAdvEvent(event, npc, player)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (attacker is Playable) {
            if (npc.isScriptValue(1)) {
                if (Rnd[100] == 0)
                    npc.broadcastNpcSay("Removing intruders.")
            } else {
                npc.scriptValue = 1
                npc.broadcastNpcSay("A non-permitted target has been discovered.")
                npc.broadcastNpcSay("Starting intruder removal system.")
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        if (npc.npcId == CORE) {
            npc.broadcastPacket(PlaySound(1, "BS02_D", npc))
            npc.broadcastNpcSay("A fatal error has occurred.")
            npc.broadcastNpcSay("System is being shut down...")
            npc.broadcastNpcSay("......")

            addSpawn(31842, 16502, 110165, -6394, 0, false, 900000, false)
            addSpawn(31842, 18948, 110166, -6397, 0, false, 900000, false)
            GrandBossManager.setBossStatus(CORE, DEAD.toInt())

            var respawnTime =
                Config.SPAWN_INTERVAL_CORE.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_CORE, Config.RANDOM_SPAWN_TIME_CORE]
            respawnTime *= 3600000

            startQuestTimer("core_unlock", respawnTime, null, null, false)

            val info = GrandBossManager.getStatsSet(CORE) ?: return null
            info.set("respawn_time", System.currentTimeMillis() + respawnTime)
            GrandBossManager.setStatsSet(CORE, info)
            startQuestTimer("despawn_minions", 20000, null, null, false)
            cancelQuestTimers("spawn_minion")
        } else if (GrandBossManager.getBossStatus(CORE) == ALIVE.toInt() && _minions != null && _minions.contains(
                npc
            )
        ) {
            _minions.remove(npc)
            startQuestTimer("spawn_minion", 60000, npc, null, false)
        }
        return super.onKill(npc, killer)
    }

    companion object {
        private const val CORE = 29006
        private const val DEATH_KNIGHT = 29007
        private const val DOOM_WRAITH = 29008
        private const val SUSCEPTOR = 29011

        private const val ALIVE: Byte = 0 // Core is spawned.
        private const val DEAD: Byte = 1 // Core has been killed.
    }
}