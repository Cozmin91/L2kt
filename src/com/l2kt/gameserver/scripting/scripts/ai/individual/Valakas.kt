package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SpecialCamera
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class Valakas : L2AttackableAIScript("ai/individual") {

    private var _timeTracker: Long = 0 // Time tracker for last attack on Valakas.
    private var _actualVictim: Player? = null // Actual target of Valakas.

    init {

        val info = GrandBossManager.getInstance().getStatsSet(VALAKAS)

        when (GrandBossManager.getInstance().getBossStatus(VALAKAS).toByte()) {
            DEAD // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
            -> {
                val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                if (temp > 0)
                    startQuestTimer("valakas_unlock", temp, null, null, false)
                else
                    GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT.toInt())
            }

            WAITING -> startQuestTimer("beginning", Config.WAIT_TIME_VALAKAS.toLong(), null, null, false)

            FIGHTING -> {
                val loc_x = info.getInteger("loc_x")
                val loc_y = info.getInteger("loc_y")
                val loc_z = info.getInteger("loc_z")
                val heading = info.getInteger("heading")
                val hp = info.getInteger("currentHP")
                val mp = info.getInteger("currentMP")

                val valakas = addSpawn(VALAKAS, loc_x, loc_y, loc_z, heading, false, 0, false)
                GrandBossManager.getInstance().addBoss(valakas as GrandBoss)

                valakas.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                valakas.setRunning()

                // stores current time for inactivity task.
                _timeTracker = System.currentTimeMillis()

                // Start timers.
                startQuestTimer("regen_task", 60000, valakas, null, true)
                startQuestTimer("skill_task", 2000, valakas, null, true)
            }
        }
    }

    override fun registerNpcs() {
        addEventIds(VALAKAS, EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN, EventType.ON_AGGRO)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var npc = npc
        if (event.equals("beginning", ignoreCase = true)) {
            // Stores current time
            _timeTracker = System.currentTimeMillis()

            // Spawn Valakas and set him invul.
            npc = addSpawn(VALAKAS, 212852, -114842, -1632, 0, false, 0, false)
            GrandBossManager.getInstance().addBoss(npc as GrandBoss?)
            npc!!.setIsInvul(true)

            // Sound + socialAction.
            for (plyr in VALAKAS_LAIR.getKnownTypeInside(Player::class.java)) {
                plyr.sendPacket(PlaySound(1, "B03_A", npc))
                plyr.sendPacket(SocialAction(npc, 3))
            }

            // Launch the cinematic, and tasks (regen + skill).
            startQuestTimer("spawn_1", 2000, npc, null, false) // 2000
            startQuestTimer("spawn_2", 3500, npc, null, false) // 1500
            startQuestTimer("spawn_3", 6800, npc, null, false) // 3300
            startQuestTimer("spawn_4", 9700, npc, null, false) // 2900
            startQuestTimer("spawn_5", 12400, npc, null, false) // 2700
            startQuestTimer("spawn_6", 12401, npc, null, false) // 1
            startQuestTimer("spawn_7", 15601, npc, null, false) // 3200
            startQuestTimer("spawn_8", 17001, npc, null, false) // 1400
            startQuestTimer("spawn_9", 23701, npc, null, false) // 6700 - end of cinematic
            startQuestTimer("spawn_10", 29401, npc, null, false) // 5700 - AI + unlock
        } else if (event.equals("regen_task", ignoreCase = true)) {
            // Inactivity task - 15min
            if (GrandBossManager.getInstance().getBossStatus(VALAKAS) == FIGHTING.toInt()) {
                if (_timeTracker + 900000 < System.currentTimeMillis()) {
                    // Set it dormant.
                    GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT.toInt())

                    // Drop all players from the zone.
                    VALAKAS_LAIR.oustAllPlayers()

                    // Cancel skill_task and regen_task.
                    cancelQuestTimer("regen_task", npc, null)
                    cancelQuestTimer("skill_task", npc, null)

                    // Delete current instance of Valakas.
                    npc!!.deleteMe()

                    return null
                }
            }

            // Regeneration buff.
            if (Rnd[30] == 0) {
                val skillRegen: L2Skill?
                val hpRatio = npc!!.currentHp / npc.maxHp

                // Current HPs are inferior to 25% ; apply lvl 4 of regen skill.
                if (hpRatio < 0.25)
                    skillRegen = SkillTable.getInfo(4691, 4)
                else if (hpRatio < 0.5)
                    skillRegen = SkillTable.getInfo(4691, 3)
                else if (hpRatio < 0.75)
                    skillRegen = SkillTable.getInfo(4691, 2)
                else
                    skillRegen =
                            SkillTable.getInfo(4691, 1)// Current HPs are inferior to 75% ; apply lvl 2 of regen skill.
                // Current HPs are inferior to 50% ; apply lvl 3 of regen skill.

                skillRegen!!.getEffects(npc, npc)
            }
        } else if (event.equals("spawn_1", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1800, 180, -1, 1500, 10000, 0, 0, 1, 0))
        else if (event.equals("spawn_2", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1300, 180, -5, 3000, 10000, 0, -5, 1, 0))
        else if (event.equals("spawn_3", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 500, 180, -8, 600, 10000, 0, 60, 1, 0))
        else if (event.equals("spawn_4", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 800, 180, -8, 2700, 10000, 0, 30, 1, 0))
        else if (event.equals("spawn_5", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 200, 250, 70, 0, 10000, 30, 80, 1, 0))
        else if (event.equals("spawn_6", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1100, 250, 70, 2500, 10000, 30, 80, 1, 0))
        else if (event.equals("spawn_7", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 700, 150, 30, 0, 10000, -10, 60, 1, 0))
        else if (event.equals("spawn_8", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1200, 150, 20, 2900, 10000, -10, 30, 1, 0))
        else if (event.equals("spawn_9", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 750, 170, -10, 3400, 4000, 10, -15, 1, 0))
        else if (event.equals("spawn_10", ignoreCase = true)) {
            GrandBossManager.getInstance().setBossStatus(VALAKAS, FIGHTING.toInt())
            npc!!.setIsInvul(false)

            startQuestTimer("regen_task", 60000, npc, null, true)
            startQuestTimer("skill_task", 2000, npc, null, true)
        } else if (event.equals("die_1", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 2000, 130, -1, 0, 10000, 0, 0, 1, 1))
        else if (event.equals("die_2", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1100, 210, -5, 3000, 10000, -13, 0, 1, 1))
        else if (event.equals("die_3", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1300, 200, -8, 3000, 10000, 0, 15, 1, 1))
        else if (event.equals("die_4", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1000, 190, 0, 500, 10000, 0, 10, 1, 1))
        else if (event.equals("die_5", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1700, 120, 0, 2500, 10000, 12, 40, 1, 1))
        else if (event.equals("die_6", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1700, 20, 0, 700, 10000, 10, 10, 1, 1))
        else if (event.equals("die_7", ignoreCase = true))
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1700, 10, 0, 1000, 10000, 20, 70, 1, 1))
        else if (event.equals("die_8", ignoreCase = true)) {
            VALAKAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1700, 10, 0, 300, 250, 20, -20, 1, 1))

            for (loc in CUBE_LOC)
                addSpawn(31759, loc, false, 900000, false)

            startQuestTimer("remove_players", 900000, null, null, false)
        } else if (event.equals("skill_task", ignoreCase = true))
            callSkillAI(npc!!)
        else if (event.equals("valakas_unlock", ignoreCase = true))
            GrandBossManager.getInstance().setBossStatus(VALAKAS, DORMANT.toInt())
        else if (event.equals("remove_players", ignoreCase = true))
            VALAKAS_LAIR.oustAllPlayers()// Death cinematic, spawn of Teleport Cubes.
        // Spawn cinematic, regen_task and choose of skill.
        // Regeneration && inactivity task

        return super.onAdvEvent(event, npc, player)
    }

    override fun onSpawn(npc: Npc): String? {
        npc.disableCoreAI(true)
        return super.onSpawn(npc)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.isInvul)
            return null

        if (attacker is Playable) {
            // Curses
            if (testCursesOnAttack(npc, attacker))
                return null

            // Refresh timer on every hit.
            _timeTracker = System.currentTimeMillis()
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        // Cancel skill_task and regen_task.
        cancelQuestTimer("regen_task", npc, null)
        cancelQuestTimer("skill_task", npc, null)

        // Launch death animation.
        VALAKAS_LAIR.broadcastPacket(PlaySound(1, "B03_D", npc))

        startQuestTimer("die_1", 300, npc, null, false) // 300
        startQuestTimer("die_2", 600, npc, null, false) // 300
        startQuestTimer("die_3", 3800, npc, null, false) // 3200
        startQuestTimer("die_4", 8200, npc, null, false) // 4400
        startQuestTimer("die_5", 8700, npc, null, false) // 500
        startQuestTimer("die_6", 13300, npc, null, false) // 4600
        startQuestTimer("die_7", 14000, npc, null, false) // 700
        startQuestTimer("die_8", 16500, npc, null, false) // 2500

        GrandBossManager.getInstance().setBossStatus(VALAKAS, DEAD.toInt())

        var respawnTime =
            Config.SPAWN_INTERVAL_VALAKAS.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_VALAKAS, Config.RANDOM_SPAWN_TIME_VALAKAS]
        respawnTime *= 3600000

        startQuestTimer("valakas_unlock", respawnTime, null, null, false)

        // also save the respawn time so that the info is maintained past reboots
        val info = GrandBossManager.getInstance().getStatsSet(VALAKAS)
        info.set("respawn_time", System.currentTimeMillis() + respawnTime)
        GrandBossManager.getInstance().setStatsSet(VALAKAS, info)

        return super.onKill(npc, killer)
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        return null
    }

    private fun callSkillAI(npc: Npc) {
        if (npc.isInvul || npc.isCastingNow)
            return

        // Pickup a target if no or dead victim. 10% luck he decides to reconsiders his target.
        if (_actualVictim == null || _actualVictim!!.isDead || !npc.getKnownType(Player::class.java).contains(_actualVictim!!) || Rnd[10] == 0)
            _actualVictim = getRandomPlayer(npc)

        // If result is still null, Valakas will roam. Don't go deeper in skill AI.
        if (_actualVictim == null) {
            if (Rnd[10] == 0) {
                val x = npc.x
                val y = npc.y
                val z = npc.z

                val posX = x + Rnd[-1400, 1400]
                val posY = y + Rnd[-1400, 1400]

                if (GeoEngine.canMoveToTarget(x, y, z, posX, posY, z))
                    npc.ai.setIntention(CtrlIntention.MOVE_TO, Location(posX, posY, z))
            }
            return
        }

        val skill = SkillTable.getInfo(getRandomSkill(npc), 1)

        // Cast the skill or follow the target.
        if (MathUtil.checkIfInRange(if (skill!!.castRange < 600) 600 else skill.castRange, npc, _actualVictim, true)) {
            npc.ai.setIntention(CtrlIntention.IDLE)
            npc.target = _actualVictim
            npc.doCast(skill)
        } else
            npc.ai.setIntention(CtrlIntention.FOLLOW, _actualVictim, null)
    }

    /**
     * Pick a random skill.<br></br>
     * Valakas will mostly use utility skills. If Valakas feels surrounded, he will use AoE skills.<br></br>
     * Lower than 50% HPs, he will begin to use Meteor skill.
     * @param npc valakas
     * @return a usable skillId
     */
    private fun getRandomSkill(npc: Npc): Int {
        val hpRatio = npc.currentHp / npc.maxHp

        // Valakas Lava Skin is prioritary.
        if (hpRatio < 0.25 && Rnd[1500] == 0 && npc.getFirstEffect(4680) == null)
            return LAVA_SKIN

        if (hpRatio < 0.5 && Rnd[60] == 0)
            return METEOR_SWARM

        // Find enemies surrounding Valakas.
        val playersAround = getPlayersCountInPositions(1200, npc, false)

        // Behind position got more ppl than front position, use behind aura skill.
        return if (playersAround[1] > playersAround[0]) BEHIND_SKILLS[Rnd[BEHIND_SKILLS.size]] else FRONT_SKILLS[Rnd[FRONT_SKILLS.size]]

        // Use front aura skill.
    }

    companion object {
        private val VALAKAS_LAIR = ZoneManager.getZoneById(110010, BossZone::class.java)

        const val DORMANT: Byte = 0 // Valakas is spawned and no one has entered yet. Entry is unlocked.
        const val WAITING: Byte =
            1 // Valakas is spawned and someone has entered, triggering a 30 minute window for additional people to enter. Entry is unlocked.
        const val FIGHTING: Byte = 2 // Valakas is engaged in battle, annihilating his foes. Entry is locked.
        const val DEAD: Byte = 3 // Valakas has been killed. Entry is locked.

        private val FRONT_SKILLS = intArrayOf(4681, 4682, 4683, 4684, 4689)

        private val BEHIND_SKILLS = intArrayOf(4685, 4686, 4688)

        private const val LAVA_SKIN = 4680
        private const val METEOR_SWARM = 4690

        private val CUBE_LOC = arrayOf(
            SpawnLocation(214880, -116144, -1644, 0),
            SpawnLocation(213696, -116592, -1644, 0),
            SpawnLocation(212112, -116688, -1644, 0),
            SpawnLocation(211184, -115472, -1664, 0),
            SpawnLocation(210336, -114592, -1644, 0),
            SpawnLocation(211360, -113904, -1644, 0),
            SpawnLocation(213152, -112352, -1644, 0),
            SpawnLocation(214032, -113232, -1644, 0),
            SpawnLocation(214752, -114592, -1644, 0),
            SpawnLocation(209824, -115568, -1421, 0),
            SpawnLocation(210528, -112192, -1403, 0),
            SpawnLocation(213120, -111136, -1408, 0),
            SpawnLocation(215184, -111504, -1392, 0),
            SpawnLocation(215456, -117328, -1392, 0),
            SpawnLocation(213200, -118160, -1424, 0)
        )

        const val VALAKAS = 29028
    }
}