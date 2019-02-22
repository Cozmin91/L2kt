package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SpecialCamera
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.concurrent.CopyOnWriteArrayList

class Sailren : L2AttackableAIScript("ai/individual") {

    private val _mobs = CopyOnWriteArrayList<Npc>()

    init {
        run{
            val info = GrandBossManager.getStatsSet(SAILREN) ?: return@run

            when (GrandBossManager.getBossStatus(SAILREN).toByte()) {
                DEAD // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
                -> {
                    val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                    if (temp > 0)
                        startQuestTimer("unlock", temp, null, null, false)
                    else
                        GrandBossManager.setBossStatus(SAILREN, DORMANT.toInt())
                }

                FIGHTING -> {
                    val loc_x = info.getInteger("loc_x")
                    val loc_y = info.getInteger("loc_y")
                    val loc_z = info.getInteger("loc_z")
                    val heading = info.getInteger("heading")
                    val hp = info.getInteger("currentHP")
                    val mp = info.getInteger("currentMP")

                    val sailren = addSpawn(SAILREN, loc_x, loc_y, loc_z, heading, false, 0, false)
                    GrandBossManager.addBoss(sailren as GrandBoss)
                    _mobs.add(sailren)

                    sailren.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                    sailren.setRunning()

                    // Don't need to edit _timeTracker, as it's initialized to 0.
                    startQuestTimer("inactivity", INTERVAL_CHECK, null, null, true)
                }
            }
        }
    }

    override fun registerNpcs() {
        addAttackId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN)
        addKillId(VELOCIRAPTOR, PTEROSAUR, TREX, SAILREN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("beginning", ignoreCase = true)) {
            _timeTracker = 0

            for (i in 0..2) {
                val temp = addSpawn(VELOCIRAPTOR, SAILREN_LOC, true, 0, false)  ?: continue
                temp.ai.setIntention(CtrlIntention.ACTIVE)
                temp.setRunning()
                _mobs.add(temp)
            }
            startQuestTimer("inactivity", INTERVAL_CHECK, null, null, true)
        } else if (event.equals("spawn", ignoreCase = true)) {
            // Dummy spawn used to cast the skill. Despawned after 26sec.
            val temp = addSpawn(DUMMY, SAILREN_LOC, false, 26000, false)

            // Cast skill every 2,5sec.
            SAILREN_LAIR.broadcastPacket(MagicSkillUse(npc!!, npc, 5090, 1, 2500, 0))
            startQuestTimer("skill", 2500, temp, null, true)

            // Cinematic, meanwhile.
            SAILREN_LAIR.broadcastPacket(SpecialCamera(temp!!.objectId, 60, 110, 30, 4000, 4000, 0, 65, 1, 0)) // 4sec

            startQuestTimer("camera_0", 3900, temp, null, false) // 3sec
            startQuestTimer("camera_1", 6800, temp, null, false) // 3sec
            startQuestTimer("camera_2", 9700, temp, null, false) // 3sec
            startQuestTimer("camera_3", 12600, temp, null, false) // 3sec
            startQuestTimer("camera_4", 15500, temp, null, false) // 3sec
            startQuestTimer("camera_5", 18400, temp, null, false) // 7sec
        } else if (event.equals("skill", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(MagicSkillUse(npc!!, npc, 5090, 1, 2500, 0))
        else if (event.equals("camera_0", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 100, 180, 30, 3000, 3000, 0, 50, 1, 0))
        else if (event.equals("camera_1", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 150, 270, 25, 3000, 3000, 0, 30, 1, 0))
        else if (event.equals("camera_2", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 160, 360, 20, 3000, 3000, 10, 15, 1, 0))
        else if (event.equals("camera_3", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 160, 450, 10, 3000, 3000, 0, 10, 1, 0))
        else if (event.equals("camera_4", ignoreCase = true)) {
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 160, 560, 0, 3000, 3000, 0, 10, 1, 0))

            val temp = addSpawn(SAILREN, SAILREN_LOC, false, 0, false)
            GrandBossManager.addBoss(temp as GrandBoss)
            _mobs.add(temp)

            // Stop skill task.
            cancelQuestTimers("skill")
            SAILREN_LAIR.broadcastPacket(MagicSkillUse(npc, npc, 5091, 1, 2500, 0))

            temp.broadcastPacket(SocialAction(temp, 2))
        } else if (event.equals("camera_5", ignoreCase = true))
            SAILREN_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 70, 560, 0, 500, 7000, -15, 10, 1, 0))
        else if (event.equals("unlock", ignoreCase = true))
            GrandBossManager.setBossStatus(SAILREN, DORMANT.toInt())
        else if (event.equals("inactivity", ignoreCase = true)) {
            // 10 minutes without any attack activity leads to a reset.
            if (System.currentTimeMillis() - _timeTracker >= INTERVAL_CHECK) {
                // Set it dormant.
                GrandBossManager.setBossStatus(SAILREN, DORMANT.toInt())

                // Delete all monsters and clean the list.
                if (!_mobs.isEmpty()) {
                    for (mob in _mobs)
                        mob.deleteMe()

                    _mobs.clear()
                }

                // Oust all players from area.
                SAILREN_LAIR.oustAllPlayers()

                // Cancel inactivity task.
                cancelQuestTimers("inactivity")
            }
        } else if (event.equals("oust", ignoreCase = true)) {
            // Oust all players from area.
            SAILREN_LAIR.oustAllPlayers()
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        if (killer is Playable) {
            val player = killer?.actingPlayer
            if (player == null || !_mobs.contains(npc) || !SAILREN_LAIR.allowedPlayers.contains(player.objectId))
                return null
        }

        when (npc.npcId) {
            VELOCIRAPTOR ->
                // Once the 3 Velociraptors are dead, spawn a Pterosaur.
                if (_mobs.remove(npc) && _mobs.isEmpty()) {
                    val temp = addSpawn(PTEROSAUR, SAILREN_LOC, false, 0, false) ?: return null
                    temp.setRunning()
                    temp.ai.setIntention(CtrlIntention.ATTACK, killer)
                    _mobs.add(temp)
                }

            PTEROSAUR ->
                // Pterosaur is dead, spawn a Trex.
                if (_mobs.remove(npc)) {
                    val temp = addSpawn(TREX, SAILREN_LOC, false, 0, false) ?: return null
                    temp.setRunning()
                    temp.ai.setIntention(CtrlIntention.ATTACK, killer)
                    temp.broadcastNpcSay("?")
                    _mobs.add(temp)
                }

            TREX ->
                // Trex is dead, wait 5min and spawn Sailren.
                if (_mobs.remove(npc))
                    startQuestTimer("spawn", Config.WAIT_TIME_SAILREN.toLong(), npc, null, false)

            SAILREN -> if (_mobs.remove(npc)) {
                // Set Sailren as dead.
                GrandBossManager.setBossStatus(SAILREN, DEAD.toInt())

                // Spawn the Teleport Cube for 10min.
                addSpawn(CUBE, npc, false, INTERVAL_CHECK, false)

                // Cancel inactivity task.
                cancelQuestTimers("inactivity")

                var respawnTime =
                    Config.SPAWN_INTERVAL_SAILREN.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_SAILREN, Config.RANDOM_SPAWN_TIME_SAILREN]
                respawnTime *= 3600000

                startQuestTimer("oust", INTERVAL_CHECK, null, null, false)
                startQuestTimer("unlock", respawnTime, null, null, false)

                // Save the respawn time so that the info is maintained past reboots.
                val info = GrandBossManager.getStatsSet(SAILREN) ?: return null
                info.set("respawn_time", System.currentTimeMillis() + respawnTime)
                GrandBossManager.setStatsSet(SAILREN, info)
            }
        }

        return super.onKill(npc, killer)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (attacker is Playable) {
            val player = attacker.actingPlayer
            if (player == null || !_mobs.contains(npc) || !SAILREN_LAIR.allowedPlayers.contains(player.objectId))
                return null

            // Curses
            if (testCursesOnAttack(npc, attacker, SAILREN))
                return null

            // Refresh timer on every hit.
            _timeTracker = System.currentTimeMillis()
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    companion object {
        private val SAILREN_LAIR = ZoneManager.getZoneById(110011, BossZone::class.java)

        const val SAILREN = 29065

        const val DORMANT: Byte = 0 // No one has entered yet. Entry is unlocked.
        const val FIGHTING: Byte = 1 // A group entered in the nest. Entry is locked.
        const val DEAD: Byte = 2 // Sailren has been killed. Entry is locked.

        private const val VELOCIRAPTOR = 22223
        private const val PTEROSAUR = 22199
        private const val TREX = 22217

        private const val DUMMY = 32110
        private const val CUBE = 32107

        private const val INTERVAL_CHECK = 600000L // 10 minutes

        private val SAILREN_LOC = SpawnLocation(27549, -6638, -2008, 0)
        private var _timeTracker: Long = 0
    }
}