package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
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
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.network.serverpackets.SpecialCamera
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.concurrent.CopyOnWriteArrayList

/**
 * That AI is heavily based on Valakas/Baium scripts.<br></br>
 * It uses the 29019 dummy id in order to register it (addBoss and statsSet), but 3 different templates according the situation.
 */
class Antharas : L2AttackableAIScript("ai/individual") {

    private var _timeTracker: Long = 0 // Time tracker for last attack on Antharas.
    private var _actualVictim: Player? = null // Actual target of Antharas.
    private val _monsters = CopyOnWriteArrayList<Npc>() // amount of Antharas minions.

    private var _antharasId: Int = 0 // The current Antharas, used when server shutdowns.
    private var _skillRegen: L2Skill? = null // The regen skill used by Antharas.
    private var _minionTimer: Int = 0 // The timer used by minions in order to spawn.

    init {

        run{
            val info = GrandBossManager.getStatsSet(ANTHARAS) ?: return@run

            when (GrandBossManager.getBossStatus(ANTHARAS).toByte()) {
                DEAD // Launch the timer to set DORMANT, or set DORMANT directly if timer expired while offline.
                -> {
                    val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                    if (temp > 0)
                        startQuestTimer("antharas_unlock", temp, null, null, false)
                    else
                        GrandBossManager.setBossStatus(ANTHARAS, DORMANT.toInt())
                }

                WAITING // Launch beginning timer.
                -> startQuestTimer("beginning", Config.WAIT_TIME_ANTHARAS.toLong(), null, null, false)

                FIGHTING -> {
                    val loc_x = info.getInteger("loc_x")
                    val loc_y = info.getInteger("loc_y")
                    val loc_z = info.getInteger("loc_z")
                    val heading = info.getInteger("heading")
                    val hp = info.getInteger("currentHP")
                    val mp = info.getInteger("currentMP")

                    // Update Antharas informations.
                    updateAntharas()

                    val antharas = addSpawn(_antharasId, loc_x, loc_y, loc_z, heading, false, 0, false)
                    GrandBossManager.addBoss(ANTHARAS, antharas as GrandBoss)

                    antharas.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                    antharas.setRunning()

                    // stores current time for inactivity task.
                    _timeTracker = System.currentTimeMillis()

                    startQuestTimer("regen_task", 60000, antharas, null, true)
                    startQuestTimer("skill_task", 2000, antharas, null, true)
                    startQuestTimer("minions_spawn", _minionTimer.toLong(), antharas, null, true)
                }
            }
        }
    }

    override fun registerNpcs() {
        addEventIds(ANTHARAS_IDS, EventType.ON_ATTACK, EventType.ON_SPAWN)
        addKillId(29066, 29067, 29068, 29069, 29070, 29071, 29072, 29073, 29074, 29075, 29076)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        // Regeneration && inactivity task
        if (event.equals("regen_task", ignoreCase = true)) {
            // Inactivity task - 30min
            if (_timeTracker + 1800000 < System.currentTimeMillis()) {
                // Set it dormant.
                GrandBossManager.setBossStatus(ANTHARAS, DORMANT.toInt())

                // Drop all players from the zone.
                ANTHARAS_LAIR.oustAllPlayers()

                // Drop tasks.
                if(npc != null)
                    dropTimers(npc)

                // Delete current instance of Antharas.
                npc?.deleteMe()
                return null
            }
            _skillRegen!!.getEffects(npc, npc)
        } else if (event.equals("spawn_1", ignoreCase = true))
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 700, 13, -19, 0, 20000, 0, 0, 1, 0))
        else if (event.equals("spawn_2", ignoreCase = true)) {
            npc!!.broadcastPacket(SocialAction(npc, 1))
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc.objectId, 700, 13, 0, 6000, 20000, 0, 0, 1, 0))
        } else if (event.equals("spawn_3", ignoreCase = true))
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 3700, 0, -3, 0, 10000, 0, 0, 1, 0))
        else if (event.equals("spawn_4", ignoreCase = true)) {
            npc!!.broadcastPacket(SocialAction(npc, 2))
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc.objectId, 1100, 0, -3, 22000, 30000, 0, 0, 1, 0))
        } else if (event.equals("spawn_5", ignoreCase = true))
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc!!.objectId, 1100, 0, -3, 300, 7000, 0, 0, 1, 0))
        else if (event.equals("spawn_6", ignoreCase = true)) {
            // stores current time for inactivity task.
            _timeTracker = System.currentTimeMillis()

            GrandBossManager.setBossStatus(ANTHARAS, FIGHTING.toInt())
            npc?.setIsInvul(false)
            npc?.setRunning()

            startQuestTimer("regen_task", 60000, npc, null, true)
            startQuestTimer("skill_task", 2000, npc, null, true)
            startQuestTimer("minions_spawn", _minionTimer.toLong(), npc, null, true)
        } else if (event.equals("skill_task", ignoreCase = true)){
            if(npc != null)
                callSkillAI(npc)
        }
        else if (event.equals("minions_spawn", ignoreCase = true)) {
            val isBehemoth = Rnd[100] < 60
            val mobNumber = if (isBehemoth) 2 else 3

            // Set spawn.
            for (i in 0 until mobNumber) {
                if (_monsters.size > 9)
                    break

                val npcId = if (isBehemoth) 29069 else Rnd[29070, 29076]
                val dragon = addSpawn(npcId, npc!!.x + Rnd[-200, 200], npc.y + Rnd[-200, 200], npc.z, 0, false, 0, true)
                (dragon as Attackable).isMinion = true

                _monsters.add(dragon)

                val victim = getRandomPlayer(dragon)
                if (victim != null)
                    attack(dragon, victim)

                if (!isBehemoth)
                    startQuestTimer("self_destruct", (_minionTimer / 3).toLong(), dragon, null, false)
            }
        } else if (event.equals("self_destruct", ignoreCase = true)) {
            val skill: L2Skill?
            when (npc!!.npcId) {
                29070, 29071, 29072, 29073, 29074, 29075 -> skill = SkillTable.getInfo(5097, 1)
                else -> skill = SkillTable.getInfo(5094, 1)
            }
            npc.doCast(skill)
        } else if (event.equals("beginning", ignoreCase = true)) {
            updateAntharas()

            val antharas = addSpawn(_antharasId, 181323, 114850, -7623, 32542, false, 0, false)
            GrandBossManager.addBoss(ANTHARAS, antharas as GrandBoss)
            antharas.setIsInvul(true)

            // Launch the cinematic, and tasks (regen + skill).
            startQuestTimer("spawn_1", 16, antharas, null, false)
            startQuestTimer("spawn_2", 3016, antharas, null, false)
            startQuestTimer("spawn_3", 13016, antharas, null, false)
            startQuestTimer("spawn_4", 13216, antharas, null, false)
            startQuestTimer("spawn_5", 24016, antharas, null, false)
            startQuestTimer("spawn_6", 25916, antharas, null, false)
        } else if (event.equals("die_1", ignoreCase = true)) {
            addSpawn(31859, 177615, 114941, -7709, 0, false, 900000, false)
            startQuestTimer("remove_players", 900000, null, null, false)
        } else if (event.equals("antharas_unlock", ignoreCase = true))
            GrandBossManager.setBossStatus(ANTHARAS, DORMANT.toInt())
        else if (event.equals("remove_players", ignoreCase = true))
            ANTHARAS_LAIR.oustAllPlayers()// spawn of Teleport Cube.
        // Cinematic
        // Spawn cinematic, regen_task and choose of skill.

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
        if (npc.npcId == _antharasId) {
            // Drop tasks.
            dropTimers(npc)

            // Launch death animation.
            ANTHARAS_LAIR.broadcastPacket(SpecialCamera(npc.objectId, 1200, 20, -10, 10000, 13000, 0, 0, 0, 0))
            ANTHARAS_LAIR.broadcastPacket(PlaySound(1, "BS01_D", npc))
            startQuestTimer("die_1", 8000, null, null, false)

            GrandBossManager.setBossStatus(ANTHARAS, DEAD.toInt())

            var respawnTime =
                Config.SPAWN_INTERVAL_ANTHARAS.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_ANTHARAS, Config.RANDOM_SPAWN_TIME_ANTHARAS]
            respawnTime *= 3600000

            startQuestTimer("antharas_unlock", respawnTime, null, null, false)

            val info = GrandBossManager.getStatsSet(ANTHARAS) ?: return null
            info.set("respawn_time", System.currentTimeMillis() + respawnTime)
            GrandBossManager.setStatsSet(ANTHARAS, info)
        } else {
            cancelQuestTimer("self_destruct", npc, null)
            _monsters.remove(npc)
        }

        return super.onKill(npc, killer)
    }

    private fun callSkillAI(npc: Npc) {
        if (npc.isInvul || npc.isCastingNow)
            return

        // Pickup a target if no or dead victim. 10% luck he decides to reconsiders his target.
        if (_actualVictim == null || _actualVictim!!.isDead || !npc.getKnownType(Player::class.java).contains(_actualVictim!!) || Rnd[10] == 0)
            _actualVictim = getRandomPlayer(npc)

        // If result is still null, Antharas will roam. Don't go deeper in skill AI.
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

        val skill = getRandomSkill(npc)

        // Cast the skill or follow the target.
        if (MathUtil.checkIfInRange(if (skill!!.castRange < 600) 600 else skill.castRange, npc, _actualVictim, true)) {
            npc.ai.setIntention(CtrlIntention.IDLE)
            npc.target = _actualVictim
            npc.doCast(skill)
        } else
            npc.ai.setIntention(CtrlIntention.FOLLOW, _actualVictim, null)
    }

    /**
     * Update Antharas informations depending about how much players joined the fight.<br></br>
     * Used when server restarted and Antharas is fighting, or used while the cinematic occurs (after the 30min timer).
     */
    private fun updateAntharas() {
        val playersNumber = ANTHARAS_LAIR.allowedPlayers.size
        when {
            playersNumber < 45 -> {
                _antharasId = ANTHARAS_IDS[0]
                _skillRegen = SkillTable.getInfo(4239, 1)
                _minionTimer = 180000
            }
            playersNumber < 63 -> {
                _antharasId = ANTHARAS_IDS[1]
                _skillRegen = SkillTable.getInfo(4240, 1)
                _minionTimer = 150000
            }
            else -> {
                _antharasId = ANTHARAS_IDS[2]
                _skillRegen = SkillTable.getInfo(4241, 1)
                _minionTimer = 120000
            }
        }
    }

    /**
     * Pick a random skill.<br></br>
     * The use is based on current HPs ratio.
     * @param npc Antharas
     * @return a usable skillId
     */
    private fun getRandomSkill(npc: Npc): L2Skill? {
        val hpRatio = npc.currentHp / npc.maxHp

        // Find enemies surrounding Antharas.
        val playersAround = getPlayersCountInPositions(1100, npc, false)

        if (hpRatio < 0.25) {
            if (Rnd[100] < 30)
                return SkillTable.FrequentSkill.ANTHARAS_MOUTH.skill

            if (playersAround[1] >= 10 && Rnd[100] < 80)
                return SkillTable.FrequentSkill.ANTHARAS_TAIL.skill

            if (playersAround[0] >= 10) {
                if (Rnd[100] < 40)
                    return SkillTable.FrequentSkill.ANTHARAS_DEBUFF.skill

                if (Rnd[100] < 10)
                    return SkillTable.FrequentSkill.ANTHARAS_JUMP.skill
            }

            if (Rnd[100] < 10)
                return SkillTable.FrequentSkill.ANTHARAS_METEOR.skill
        } else if (hpRatio < 0.5) {
            if (playersAround[1] >= 10 && Rnd[100] < 80)
                return SkillTable.FrequentSkill.ANTHARAS_TAIL.skill

            if (playersAround[0] >= 10) {
                if (Rnd[100] < 40)
                    return SkillTable.FrequentSkill.ANTHARAS_DEBUFF.skill

                if (Rnd[100] < 10)
                    return SkillTable.FrequentSkill.ANTHARAS_JUMP.skill
            }

            if (Rnd[100] < 7)
                return SkillTable.FrequentSkill.ANTHARAS_METEOR.skill
        } else if (hpRatio < 0.75) {
            if (playersAround[1] >= 10 && Rnd[100] < 80)
                return SkillTable.FrequentSkill.ANTHARAS_TAIL.skill

            if (playersAround[0] >= 10 && Rnd[100] < 10)
                return SkillTable.FrequentSkill.ANTHARAS_JUMP.skill

            if (Rnd[100] < 5)
                return SkillTable.FrequentSkill.ANTHARAS_METEOR.skill
        } else {
            if (playersAround[1] >= 10 && Rnd[100] < 80)
                return SkillTable.FrequentSkill.ANTHARAS_TAIL.skill

            if (Rnd[100] < 3)
                return SkillTable.FrequentSkill.ANTHARAS_METEOR.skill
        }

        if (Rnd[100] < 6)
            return SkillTable.FrequentSkill.ANTHARAS_BREATH.skill

        if (Rnd[100] < 50)
            return SkillTable.FrequentSkill.ANTHARAS_NORMAL_ATTACK.skill

        return if (Rnd[100] < 5) {
            if (Rnd[100] < 50) SkillTable.FrequentSkill.ANTHARAS_FEAR.skill else SkillTable.FrequentSkill.ANTHARAS_SHORT_FEAR.skill

        } else SkillTable.FrequentSkill.ANTHARAS_NORMAL_ATTACK_EX.skill

    }

    /**
     * Drop timers, meaning Antharas is dead or inactivity task occured.
     * @param npc : The NPC to affect.
     */
    private fun dropTimers(npc: Npc) {
        cancelQuestTimer("regen_task", npc, null)
        cancelQuestTimer("skill_task", npc, null)
        cancelQuestTimer("minions_spawn", npc, null)

        for (mob in _monsters) {
            cancelQuestTimer("self_destruct", mob, null)
            mob.deleteMe()
        }
        _monsters.clear()
    }

    companion object {
        private val ANTHARAS_LAIR = ZoneManager.getZoneById(110001, BossZone::class.java)

        private val ANTHARAS_IDS = intArrayOf(29066, 29067, 29068)

        const val ANTHARAS = 29019 // Dummy Antharas id used for status updates only.

        const val DORMANT: Byte = 0 // No one has entered yet. Entry is unlocked.
        const val WAITING: Byte =
            1 // Someone has entered, triggering a 30 minute window for additional people to enter. Entry is unlocked.
        const val FIGHTING: Byte = 2 // Antharas is engaged in battle, annihilating his foes. Entry is locked.
        const val DEAD: Byte = 3 // Antharas has been killed. Entry is locked.


    }
}