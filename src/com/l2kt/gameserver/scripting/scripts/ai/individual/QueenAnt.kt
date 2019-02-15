package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.ai.type.AttackableAI
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class QueenAnt : L2AttackableAIScript("ai/individual") {

    private var _larva: Monster? = null

    init {

        // Queen Ant is dead, calculate the respawn time. If passed, we spawn it directly, otherwise we set a task to spawn it lately.
        if (GrandBossManager.getInstance().getBossStatus(QUEEN) == DEAD.toInt()) {
            val temp =
                GrandBossManager.getInstance().getStatsSet(QUEEN).getLong("respawn_time") - System.currentTimeMillis()
            if (temp > 0)
                startQuestTimer("queen_unlock", temp, null, null, false)
            else
                spawnBoss(true)
        } else
            spawnBoss(false)// Queen Ant is alive, spawn it using stored data.
    }

    override fun registerNpcs() {
        addAttackId(QUEEN, LARVA, NURSE, GUARD, ROYAL)
        addAggroRangeEnterId(LARVA, NURSE, GUARD, ROYAL)
        addFactionCallId(QUEEN, NURSE)
        addKillId(QUEEN, NURSE, ROYAL)
        addSkillSeeId(QUEEN, LARVA, NURSE, GUARD, ROYAL)
        addSpawnId(LARVA, NURSE)
        addExitZoneId(110017)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        when {
            event.equals("action", ignoreCase = true) -> {
                // Animation timer.
                if (Rnd[10] < 3)
                    npc?.broadcastPacket(SocialAction(npc, if (Rnd.nextBoolean()) 3 else 4))

                // Teleport Royal Guards back in zone if out.
                (npc as Monster).minionList.spawnedMinions.stream()
                    .filter { m -> m.npcId == ROYAL && !ZONE.isInsideZone(m) }.forEach { m -> m.teleToMaster() }
            }
            event.equals("chaos", ignoreCase = true) -> {
                // Randomize the target for Royal Guards.
                (npc as Monster).minionList.spawnedMinions.stream()
                    .filter { m -> m.npcId == ROYAL && m.isInCombat && Rnd[100] < 66 }
                    .forEach { m -> (m.ai as AttackableAI).aggroReconsider() }

                // Relaunch a new chaos task.
                startQuestTimer("chaos", (90000 + Rnd[240000]).toLong(), npc, null, false)
            }
            event.equals("clean", ignoreCase = true) -> {
                // Delete the larva and the reference.
                _larva!!.deleteMe()
                _larva = null
            }
            event.equals("queen_unlock", ignoreCase = true) -> {
                // Choose a teleport location, and teleport players out of Queen Ant zone.
                when {
                    Rnd[100] < 33 -> ZONE.movePlayersTo(PLAYER_TELE_OUT[0])
                    Rnd.nextBoolean() -> ZONE.movePlayersTo(PLAYER_TELE_OUT[1])
                    else -> ZONE.movePlayersTo(PLAYER_TELE_OUT[2])
                }

                // Spawn the boss.
                spawnBoss(true)
            }
        }
        return super.onAdvEvent(event, npc, player)
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        val realBypasser = if (isPet && player?.pet != null) player.pet else player
        return if (testCursesOnAggro(npc, realBypasser!!)) null else super.onAggro(
            npc,
            player,
            isPet
        )

    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (attacker is Playable) {
            // Curses
            if (testCursesOnAttack(npc, attacker, QUEEN))
                return null

            // Pick current attacker, and make actions based on it and the actual distance range seperating them.
            if (npc.npcId == QUEEN && !npc.isCastingNow) {
                val dist = Math.sqrt(npc.getPlanDistanceSq(attacker.getX(), attacker.getY()))
                if (dist > 500 && Rnd[100] < 10) {
                    npc.target = attacker
                    (npc as Monster).useMagic(SkillTable.FrequentSkill.QUEEN_ANT_STRIKE.skill)
                } else if (dist > 150 && Rnd[100] < 10) {
                    npc.target = attacker
                    (npc as Monster).useMagic(if (Rnd[10] < 8) SkillTable.FrequentSkill.QUEEN_ANT_STRIKE.skill else SkillTable.FrequentSkill.QUEEN_ANT_SPRINKLE.skill)
                } else if (dist < 250 && Rnd[100] < 5) {
                    npc.target = attacker
                    (npc as Monster).useMagic(SkillTable.FrequentSkill.QUEEN_ANT_BRANDISH.skill)
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onExitZone(character: Creature, zone: ZoneType): String? {
        if (character is GrandBoss) {
            if (character.npcId == QUEEN)
                character.teleToLocation(-21610, 181594, -5734, 0)
        }
        return super.onExitZone(character, zone)
    }

    override fun onFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean): String? {
        if(npc == null) return null

        if (npc.isCastingNow)
            return null

        when (npc.npcId) {
            QUEEN -> {
                // Pick current attacker, and make actions based on it and the actual distance range seperating them.
                val realAttacker = if (isPet && attacker?.pet != null) attacker.pet else attacker
                val dist = Math.sqrt(npc.getPlanDistanceSq(realAttacker!!.x, realAttacker.y))
                if (dist > 500 && Rnd[100] < 3) {
                    npc.target = realAttacker
                    (npc as Monster).useMagic(SkillTable.FrequentSkill.QUEEN_ANT_STRIKE.skill)
                } else if (dist > 150 && Rnd[100] < 3) {
                    npc.target = realAttacker
                    (npc as Monster).useMagic(if (Rnd[10] < 8) SkillTable.FrequentSkill.QUEEN_ANT_STRIKE.skill else SkillTable.FrequentSkill.QUEEN_ANT_SPRINKLE.skill)
                } else if (dist < 250 && Rnd[100] < 2) {
                    npc.target = realAttacker
                    (npc as Monster).useMagic(SkillTable.FrequentSkill.QUEEN_ANT_BRANDISH.skill)
                }
            }

            NURSE ->
                // If the faction caller is the larva, assist it directly, no matter what.
                if (caller?.npcId == LARVA) {
                    npc.target = caller
                    (npc as Monster).useMagic(if (Rnd.nextBoolean()) SkillTable.FrequentSkill.NURSE_HEAL_1.skill else SkillTable.FrequentSkill.NURSE_HEAL_2.skill)
                } else if (caller?.npcId == QUEEN) {
                    if (_larva != null && _larva!!.currentHp < _larva!!.maxHp) {
                        npc.target = _larva
                        (npc as Monster).useMagic(if (Rnd.nextBoolean()) SkillTable.FrequentSkill.NURSE_HEAL_1.skill else SkillTable.FrequentSkill.NURSE_HEAL_2.skill)
                    } else {
                        npc.target = caller
                        (npc as Attackable).useMagic(SkillTable.FrequentSkill.NURSE_HEAL_1.skill)
                    }
                }// If the faction caller is Queen Ant, then check first Larva.
        }
        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        if (npc.npcId == QUEEN) {
            // Broadcast death sound.
            npc.broadcastPacket(PlaySound(1, "BS02_D", npc))

            // Flag Queen Ant as dead.
            GrandBossManager.getInstance().setBossStatus(QUEEN, DEAD.toInt())

            // Calculate the next respawn time.
            val respawnTime =
                (Config.SPAWN_INTERVAL_AQ + Rnd[-Config.RANDOM_SPAWN_TIME_AQ, Config.RANDOM_SPAWN_TIME_AQ]).toLong() * 3600000

            // Cancel tasks.
            cancelQuestTimer("action", npc, null)
            cancelQuestTimer("chaos", npc, null)

            // Start respawn timer, and clean the monster references.
            startQuestTimer("queen_unlock", respawnTime, null, null, false)
            startQuestTimer("clean", 5000, null, null, false)

            // Save the respawn time so that the info is maintained past reboots
            val info = GrandBossManager.getInstance().getStatsSet(QUEEN)
            info.set("respawn_time", System.currentTimeMillis() + respawnTime)
            GrandBossManager.getInstance().setStatsSet(QUEEN, info)
        } else {
            // Set the respawn time of Royal Guards and Nurses. Pick the npc master.
            val minion = npc as Monster
            val master = minion.master

            if (master != null && master.hasMinions())
                master.minionList.onMinionDie(minion, if (npc.getNpcId() == NURSE) 10000 else 280000 + Rnd[40] * 1000)

            return null
        }
        return super.onKill(npc, killer)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        val realAttacker = if (isPet && caster?.pet != null) caster.pet else caster
        if (!Config.RAID_DISABLE_CURSE && realAttacker!!.level - npc.level > 8) {
            val curse = SkillTable.FrequentSkill.RAID_CURSE.skill

            npc.broadcastPacket(MagicSkillUse(npc, realAttacker, curse!!.id, curse.level, 300, 0))
            curse.getEffects(npc, realAttacker)

            (npc as Attackable).stopHating(realAttacker)
            return null
        }

        // If Queen Ant see an aggroable skill, try to launch Queen Ant Strike.
        if (npc.npcId == QUEEN && !npc.isCastingNow && skill != null && skill.aggroPoints > 0 && Rnd[100] < 15) {
            npc.target = realAttacker
            (npc as Monster).useMagic(SkillTable.FrequentSkill.QUEEN_ANT_STRIKE.skill)
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onSpawn(npc: Npc): String? {
        when (npc.npcId) {
            LARVA -> {
                npc.setIsMortal(false)
                npc.setIsImmobilized(true)
                npc.disableCoreAI(true)
            }
            NURSE -> npc.disableCoreAI(true)
        }
        return super.onSpawn(npc)
    }

    /**
     * Make additional actions on boss spawn : register the NPC as boss, activate tasks, spawn the larva.
     * @param freshStart : If true, it uses static data, otherwise it uses stored data.
     */
    private fun spawnBoss(freshStart: Boolean) {
        val queen: GrandBoss
        if (freshStart) {
            GrandBossManager.getInstance().setBossStatus(QUEEN, ALIVE.toInt())

            queen = addSpawn(QUEEN, -21610, 181594, -5734, 0, false, 0, false) as GrandBoss
        } else {
            val info = GrandBossManager.getInstance().getStatsSet(QUEEN)

            queen = addSpawn(
                QUEEN,
                info.getInteger("loc_x"),
                info.getInteger("loc_y"),
                info.getInteger("loc_z"),
                info.getInteger("heading"),
                false,
                0,
                false
            ) as GrandBoss
            queen.setCurrentHpMp(info.getInteger("currentHP").toDouble(), info.getInteger("currentMP").toDouble())
        }

        GrandBossManager.getInstance().addBoss(queen)

        startQuestTimer("action", 10000, queen, null, true)
        startQuestTimer("chaos", (90000 + Rnd[240000]).toLong(), queen, null, false)

        queen.broadcastPacket(PlaySound(1, "BS01_A", queen))

        _larva = addSpawn(LARVA, -21600, 179482, -5846, Rnd[360], false, 0, false) as Monster
    }

    companion object {
        private val ZONE = ZoneManager.getZoneById(110017, BossZone::class.java)

        private const val QUEEN = 29001
        private const val LARVA = 29002
        private const val NURSE = 29003
        private const val GUARD = 29004
        private const val ROYAL = 29005

        private val PLAYER_TELE_OUT =
            arrayOf(Location(-19480, 187344, -5600), Location(-17928, 180912, -5520), Location(-23808, 182368, -5600))

        private const val ALIVE: Byte = 0
        private const val DEAD: Byte = 1
    }
}