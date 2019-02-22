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
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.serverpackets.Earthquake
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

/**
 * Following animations are handled in that time tempo :
 *
 *  * wake(2), 0-13 secs
 *  * neck(3), 14-24 secs.
 *  * roar(1), 25-37 secs.
 *
 * Waker's sacrifice is handled between neck and roar animation.
 */
class Baium : L2AttackableAIScript("ai/individual") {

    private var _actualVictim: Creature? = null
    private var _timeTracker: Long = 0
    private val _minions = ArrayList<Npc>(5)

    init {

        run {
            // Quest NPC starter initialization
            addStartNpc(STONE_BAIUM)
            addTalkId(STONE_BAIUM)

            val info = GrandBossManager.getStatsSet(LIVE_BAIUM) ?: return@run
            val status = GrandBossManager.getBossStatus(LIVE_BAIUM)

            if (status == DEAD.toInt()) {
                // load the unlock date and time for baium from DB
                val temp = info.getLong("respawn_time") - System.currentTimeMillis()
                if (temp > 0) {
                    // The time has not yet expired. Mark Baium as currently locked (dead).
                    startQuestTimer("baium_unlock", temp, null, null, false)
                } else {
                    // The time has expired while the server was offline. Spawn the stone-baium as ASLEEP.
                    addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false)
                    GrandBossManager.setBossStatus(LIVE_BAIUM, ASLEEP.toInt())
                }
            } else if (status == AWAKE.toInt()) {
                val loc_x = info.getInteger("loc_x")
                val loc_y = info.getInteger("loc_y")
                val loc_z = info.getInteger("loc_z")
                val heading = info.getInteger("heading")
                val hp = info.getInteger("currentHP")
                val mp = info.getInteger("currentMP")

                val baium = addSpawn(LIVE_BAIUM, loc_x, loc_y, loc_z, heading, false, 0, false)
                GrandBossManager.addBoss(baium as GrandBoss)

                baium.setCurrentHpMp(hp.toDouble(), mp.toDouble())
                baium.setRunning()

                // start monitoring baium's inactivity
                _timeTracker = System.currentTimeMillis()
                startQuestTimer("baium_despawn", 60000, baium, null, true)
                startQuestTimer("skill_range", 2000, baium, null, true)

                // Spawns angels
                for (loc in ANGEL_LOCATION) {
                    val angel = addSpawn(ARCHANGEL, loc.x, loc.y, loc.z, loc.heading, false, 0, true)
                    (angel as Attackable).isMinion = true
                    angel.setRunning()
                    _minions.add(angel)
                }

                // Angels AI
                startQuestTimer("angels_aggro_reconsider", 5000, null, null, true)
            } else
                addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false)
        }
    }

    override fun registerNpcs() {
        addEventIds(LIVE_BAIUM, EventType.ON_ATTACK, EventType.ON_KILL, EventType.ON_SPAWN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (npc != null && npc.npcId == LIVE_BAIUM) {
            if (event.equals("skill_range", ignoreCase = true)) {
                callSkillAI(npc)
            } else if (event.equals("baium_neck", ignoreCase = true)) {
                npc.broadcastPacket(SocialAction(npc, 3))
            } else if (event.equals("sacrifice_waker", ignoreCase = true)) {
                if (player != null) {
                    // If player is far of Baium, teleport him back.
                    if (!MathUtil.checkIfInShortRadius(300, player, npc, true)) {
                        BAIUM_LAIR.allowPlayerEntry(player, 10)
                        player.teleToLocation(115929, 17349, 10077, 0)
                    }

                    // 60% to die.
                    if (Rnd[100] < 60)
                        player.doDie(npc)
                }
            } else if (event.equals("baium_roar", ignoreCase = true)) {
                // Roar animation
                npc.broadcastPacket(SocialAction(npc, 1))

                // Spawn angels
                for (loc in ANGEL_LOCATION) {
                    val angel = addSpawn(ARCHANGEL, loc.x, loc.y, loc.z, loc.heading, false, 0, true)
                    (angel as Attackable).isMinion = true
                    angel.setRunning()
                    _minions.add(angel)
                }

                // Angels AI
                startQuestTimer("angels_aggro_reconsider", 5000, null, null, true)
            } else if (event.equals("baium_move", ignoreCase = true)) {
                npc.setIsInvul(false)
                npc.setRunning()

                // Start monitoring baium's inactivity and activate the AI
                _timeTracker = System.currentTimeMillis()

                startQuestTimer("baium_despawn", 60000, npc, null, true)
                startQuestTimer("skill_range", 2000, npc, null, true)
            } else if (event.equals("baium_despawn", ignoreCase = true)) {
                if (_timeTracker + 1800000 < System.currentTimeMillis()) {
                    // despawn the live-baium
                    npc.deleteMe()

                    // Unspawn angels
                    for (minion in _minions) {
                        minion.spawn?.setRespawnState(false)
                        minion.deleteMe()
                    }
                    _minions.clear()

                    addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false) // spawn stone-baium
                    GrandBossManager
                        .setBossStatus(LIVE_BAIUM, ASLEEP.toInt()) // Baium isn't awaken anymore
                    BAIUM_LAIR.oustAllPlayers()
                    cancelQuestTimer("baium_despawn", npc, null)
                } else if (_timeTracker + 300000 < System.currentTimeMillis() && npc.currentHp / npc.maxHp < 0.75) {
                    npc.target = npc
                    npc.doCast(SkillTable.getInfo(4135, 1))
                } else if (!BAIUM_LAIR.isInsideZone(npc))
                    npc.teleToLocation(116033, 17447, 10104, 0)
            }// despawn the live baium after 30 minutes of inactivity
            // also check if the players are cheating, having pulled Baium outside his zone...
        } else if (event.equals("baium_unlock", ignoreCase = true)) {
            GrandBossManager.setBossStatus(LIVE_BAIUM, ASLEEP.toInt())
            addSpawn(STONE_BAIUM, 116033, 17447, 10104, 40188, false, 0, false)
        } else if (event.equals("angels_aggro_reconsider", ignoreCase = true)) {
            var updateTarget = false // Update or no the target

            for (minion in _minions) {
                val angel = minion as Attackable
                val victim = angel.mostHated

                if (Rnd[100] < 10)
                // Chaos time
                    updateTarget = true
                else {
                    if (victim != null)
                    // Target is a unarmed player ; clean aggro.
                    {
                        if (victim is Player && victim.activeWeaponInstance == null) {
                            angel.stopHating(victim) // Clean the aggro number of previous victim.
                            updateTarget = true
                        }
                    } else
                    // No target currently.
                        updateTarget = true
                }

                if (updateTarget) {
                    val newVictim = getRandomTarget(minion)
                    if (newVictim != null && victim !== newVictim) {
                        angel.addDamageHate(newVictim, 0, 10000)
                        angel.ai.setIntention(CtrlIntention.ATTACK, newVictim)
                    }
                }
            }
        }
        return super.onAdvEvent(event, npc, player)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val htmltext = ""

        if (GrandBossManager.getBossStatus(LIVE_BAIUM) == ASLEEP.toInt()) {
            GrandBossManager.setBossStatus(LIVE_BAIUM, AWAKE.toInt())

            val baium = addSpawn(LIVE_BAIUM, npc, false, 0, false)
            baium?.setIsInvul(true)

            GrandBossManager.addBoss(baium as GrandBoss)

            // First animation
            baium.broadcastPacket(SocialAction(baium, 2))
            baium.broadcastPacket(Earthquake(baium.x, baium.y, baium.z, 40, 10))

            // Second animation, waker sacrifice, followed by angels spawn, third animation and finally movement.
            startQuestTimer("baium_neck", 13000, baium, null, false)
            startQuestTimer("sacrifice_waker", 24000, baium, player, false)
            startQuestTimer("baium_roar", 28000, baium, null, false)
            startQuestTimer("baium_move", 35000, baium, null, false)

            // Delete the statue.
            npc.deleteMe()
        }
        return htmltext
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

    override fun onKill(npc: Npc, killer: Creature?): String? {
        cancelQuestTimer("baium_despawn", npc, null)
        npc.broadcastPacket(PlaySound(1, "BS01_D", npc))

        // spawn the "Teleportation Cubic" for 15 minutes (to allow players to exit the lair)
        addSpawn(29055, 115203, 16620, 10078, 0, false, 900000, false)

        var respawnTime =
            Config.SPAWN_INTERVAL_BAIUM.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_BAIUM, Config.RANDOM_SPAWN_TIME_BAIUM]
        respawnTime *= 3600000

        GrandBossManager.setBossStatus(LIVE_BAIUM, DEAD.toInt())
        startQuestTimer("baium_unlock", respawnTime, null, null, false)

        val info = GrandBossManager.getStatsSet(LIVE_BAIUM) ?: return null
        info.set("respawn_time", System.currentTimeMillis() + respawnTime)
        GrandBossManager.setStatsSet(LIVE_BAIUM, info)

        // Unspawn angels.
        for (minion in _minions) {
            minion.spawn?.setRespawnState(false)
            minion.deleteMe()
        }
        _minions.clear()

        // Clean Baium AI
        cancelQuestTimer("skill_range", npc, null)

        // Clean angels AI
        cancelQuestTimer("angels_aggro_reconsider", null, null)

        return super.onKill(npc, killer)
    }

    /**
     * This method allows to select a random target, and is used both for Baium and angels.
     * @param npc to check.
     * @return the random target.
     */
    private fun getRandomTarget(npc: Npc): Creature? {
        val npcId = npc.npcId
        val result = ArrayList<Creature>()

        for (obj in npc.getKnownType(Creature::class.java)) {
            if (obj is Player) {
                if (obj.isDead() || !GeoEngine.canSeeTarget(npc, obj))
                    continue

                if (obj.isGM && obj.appearance.invisible)
                    continue

                if (npcId == ARCHANGEL && obj.activeWeaponInstance == null)
                    continue

                result.add(obj)
            } else if (obj is GrandBoss && npcId == ARCHANGEL)
                result.add(obj)// Case of Archangels, they can hit Baium.
        }

        // If there's no players available, Baium and Angels are hitting each other.
        if (result.isEmpty()) {
            if (npcId == LIVE_BAIUM)
            // Case of Baium. Angels should never be without target.
            {
                for (minion in _minions)
                    result.add(minion)
            }
        }

        return if (result.isEmpty()) null else Rnd[result]
    }

    /**
     * The personal casting AI for Baium.
     * @param npc baium, basically...
     */
    private fun callSkillAI(npc: Npc) {
        if (npc.isInvul || npc.isCastingNow)
            return

        // Pickup a target if no or dead victim. If Baium was hitting an angel, 50% luck he reconsiders his target. 10% luck he decides to reconsiders his target.
        if (_actualVictim == null || _actualVictim!!.isDead || !npc.getKnownType(Player::class.java).contains(
                _actualVictim
            ) || _actualVictim is Monster && Rnd[10] < 5 || Rnd[10] == 0
        )
            _actualVictim = getRandomTarget(npc)

        // If result is null, return directly.
        if (_actualVictim == null)
            return

        val skill = SkillTable.getInfo(getRandomSkill(npc), 1)

        // Adapt the skill range, because Baium is fat.
        if (MathUtil.checkIfInRange((skill!!.castRange + npc.collisionRadius).toInt(), npc, _actualVictim, true)) {
            npc.ai.setIntention(CtrlIntention.IDLE)
            npc.target = if (skill.id == 4135) npc else _actualVictim
            npc.doCast(skill)
        } else
            npc.ai.setIntention(CtrlIntention.FOLLOW, _actualVictim, null)
    }

    /**
     * Pick a random skill through that list.<br></br>
     * If Baium feels surrounded, he will use AoE skills. Same behavior if he is near 2+ angels.<br></br>
     * @param npc baium
     * @return a usable skillId
     */
    private fun getRandomSkill(npc: Npc): Int {
        // Baium's selfheal. It happens exceptionaly.
        if (npc.currentHp / npc.maxHp < 0.1) {
            if (Rnd[10000] == 777)
            // His lucky day.
                return 4135
        }

        var skill = 4127 // Default attack if nothing is possible.
        val chance = Rnd[100] // Remember, it's 0 to 99, not 1 to 100.

        // If Baium feels surrounded or see 2+ angels, he unleashes his wrath upon heads :).
        if (getPlayersCountInRadius(600, npc, false) >= 20 || npc.getKnownTypeInRadius(Monster::class.java, 600).size >= 2
        ) {
            when {
                chance < 25 -> skill = 4130
                chance in 25..49 -> skill = 4131
                chance in 50..74 -> skill = 4128
                chance in 75..99 -> skill = 4129
            }
        } else {
            if (npc.currentHp / npc.maxHp > 0.75) {
                if (chance < 10)
                    skill = 4128
                else if (chance in 10..19)
                    skill = 4129
            } else if (npc.currentHp / npc.maxHp > 0.5) {
                when {
                    chance < 10 -> skill = 4131
                    chance in 10..19 -> skill = 4128
                    chance in 20..29 -> skill = 4129
                }
            } else if (npc.currentHp / npc.maxHp > 0.25) {
                when {
                    chance < 10 -> skill = 4130
                    chance in 10..19 -> skill = 4131
                    chance in 20..29 -> skill = 4128
                    chance in 30..39 -> skill = 4129
                }
            } else {
                when {
                    chance < 10 -> skill = 4130
                    chance in 10..19 -> skill = 4131
                    chance in 20..29 -> skill = 4128
                    chance in 30..39 -> skill = 4129
                }
            }
        }
        return skill
    }

    companion object {
        private val BAIUM_LAIR = ZoneManager.getZoneById(110002, BossZone::class.java)

        private const val STONE_BAIUM = 29025
        private const val LIVE_BAIUM = 29020
        private const val ARCHANGEL = 29021

        // Baium status tracking
        const val ASLEEP: Byte = 0 // baium is in the stone version, waiting to be woken up. Entry is unlocked.
        const val AWAKE: Byte = 1 // baium is awake and fighting. Entry is locked.
        const val DEAD: Byte = 2 // baium has been killed and has not yet spawned. Entry is locked.

        // Archangels spawns
        private val ANGEL_LOCATION = arrayOf(
            SpawnLocation(114239, 17168, 10080, 63544),
            SpawnLocation(115780, 15564, 10080, 13620),
            SpawnLocation(114880, 16236, 10080, 5400),
            SpawnLocation(115168, 17200, 10080, 0),
            SpawnLocation(115792, 16608, 10080, 0)
        )
    }
}