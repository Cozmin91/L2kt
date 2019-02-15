package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.instancemanager.GrandBossManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.GrandBoss
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

class Orfen : L2AttackableAIScript("ai/individual") {
    init {

        _isTeleported = false

        val info = GrandBossManager.getInstance().getStatsSet(ORFEN)
        val status = GrandBossManager.getInstance().getBossStatus(ORFEN)

        if (status == DEAD.toInt()) {
            // load the unlock date and time for Orfen from DB
            val temp = info.getLong("respawn_time") - System.currentTimeMillis()
            if (temp > 0) {
                // The time has not yet expired. Mark Orfen as currently locked (dead).
                startQuestTimer("orfen_unlock", temp, null, null, false)
            } else {
                // The time has already expired while the server was offline. Spawn Orfen in a random place.
                _currentIndex = Rnd[1, 3]

                val orfen = addSpawn(ORFEN, ORFEN_LOCATION[_currentIndex], false, 0, false) as GrandBoss
                GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE.toInt())
                spawnBoss(orfen)
            }
        } else {
            val loc_x = info.getInteger("loc_x")
            val loc_y = info.getInteger("loc_y")
            val loc_z = info.getInteger("loc_z")
            val heading = info.getInteger("heading")
            val hp = info.getInteger("currentHP")
            val mp = info.getInteger("currentMP")

            val orfen = addSpawn(ORFEN, loc_x, loc_y, loc_z, heading, false, 0, false) as GrandBoss
            orfen.setCurrentHpMp(hp.toDouble(), mp.toDouble())
            spawnBoss(orfen)
        }
    }

    override fun registerNpcs() {
        addAttackId(ORFEN, RIBA_IREN)
        addFactionCallId(RAIKEL_LEOS, RIBA_IREN)
        addKillId(ORFEN)
        addSkillSeeId(ORFEN)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        if (event.equals("orfen_unlock", ignoreCase = true)) {
            _currentIndex = Rnd[1, 3]

            val orfen = addSpawn(ORFEN, ORFEN_LOCATION[_currentIndex], false, 0, false) as GrandBoss
            GrandBossManager.getInstance().setBossStatus(ORFEN, ALIVE.toInt())
            spawnBoss(orfen)
        } else if (event.equals("check_orfen_pos", ignoreCase = true)) {
            // 30 minutes are gone without any hit ; Orfen will move to another location.
            if (_lastAttackTime + 1800000 < System.currentTimeMillis()) {
                // Generates a number until it is different of _currentIndex (avoid to spawn in same place 2 times).
                var index = _currentIndex
                while (index == _currentIndex)
                    index = Rnd[1, 3]

                // Set the new index as _currentIndex.
                _currentIndex = index

                // Set the teleport flag to false
                _isTeleported = false

                // Reinitialize the timer.
                _lastAttackTime = System.currentTimeMillis()

                goTo(npc, ORFEN_LOCATION[_currentIndex])
            } else if (_isTeleported && npc != null && !npc.isInsideZone(ZoneId.SWAMP))
                goTo(
                    npc,
                    ORFEN_LOCATION[0]
                )// Orfen already ported once and is lured out of her lair ; teleport her back.
        }
        return super.onAdvEvent(event, npc, player)
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        val originalCaster = if (isPet) caster?.pet else caster
        if (skill != null && skill.aggroPoints > 0 && Rnd[5] == 0 && npc.isInsideRadius(originalCaster!!, 1000, false, false)) {
            npc.broadcastNpcSay(ORFEN_CHAT[Rnd[4]].replace("\$s1", caster?.name ?: ""))
            originalCaster.teleToLocation(npc.x, npc.y, npc.z, 0)
            npc.target = originalCaster
            npc.doCast(SkillTable.getInfo(4064, 1))
        }
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    override fun onFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean): String? {
        if (caller == null || npc == null || npc.isCastingNow)
            return super.onFactionCall(npc, caller, attacker, isPet)

        val npcId = npc.npcId
        val callerId = caller.npcId
        if (npcId == RAIKEL_LEOS && Rnd[20] == 0) {
            npc.target = attacker
            npc.doCast(SkillTable.getInfo(4067, 4))
        } else if (npcId == RIBA_IREN) {
            var chance = 1
            if (callerId == ORFEN)
                chance = 9

            if (callerId != RIBA_IREN && caller.currentHp / caller.maxHp < 0.5 && Rnd[10] < chance) {
                npc.ai.setIntention(CtrlIntention.IDLE, null, null)
                npc.target = caller
                npc.doCast(SkillTable.getInfo(4516, 1))
            }
        }
        return super.onFactionCall(npc, caller, attacker, isPet)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer
        if (player != null) {
            if (npc.npcId == ORFEN) {
                // update a variable with the last action against Orfen.
                _lastAttackTime = System.currentTimeMillis()

                if (!_isTeleported && npc.currentHp - damage < npc.maxHp / 2) {
                    _isTeleported = true
                    goTo(npc, ORFEN_LOCATION[0])
                } else if (npc.isInsideRadius(player, 1000, false, false) && !npc.isInsideRadius(
                        player,
                        300,
                        false,
                        false
                    ) && Rnd[10] == 0
                ) {
                    npc.broadcastNpcSay(ORFEN_CHAT[Rnd[3]].replace("\$s1", player.name))
                    player.teleToLocation(npc.x, npc.y, npc.z, 0)
                    npc.target = player
                    npc.doCast(SkillTable.getInfo(4064, 1))
                }
            } else {
                if (!npc.isCastingNow && npc.currentHp - damage < npc.maxHp / 2.0) {
                    npc.target = player
                    npc.doCast(SkillTable.getInfo(4516, 1))
                }
            }// RIBA_IREN case, as it's the only other registered.
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        npc.broadcastPacket(PlaySound(1, "BS02_D", npc))
        GrandBossManager.getInstance().setBossStatus(ORFEN, DEAD.toInt())

        var respawnTime =
            Config.SPAWN_INTERVAL_ORFEN.toLong() + Rnd[-Config.RANDOM_SPAWN_TIME_ORFEN, Config.RANDOM_SPAWN_TIME_ORFEN]
        respawnTime *= 3600000

        startQuestTimer("orfen_unlock", respawnTime, null, null, false)

        // also save the respawn time so that the info is maintained past reboots
        val info = GrandBossManager.getInstance().getStatsSet(ORFEN)
        info.set("respawn_time", System.currentTimeMillis() + respawnTime)
        GrandBossManager.getInstance().setStatsSet(ORFEN, info)

        cancelQuestTimer("check_orfen_pos", npc, null)
        return super.onKill(npc, killer)
    }

    private fun spawnBoss(npc: GrandBoss) {
        GrandBossManager.getInstance().addBoss(npc)
        npc.broadcastPacket(PlaySound(1, "BS01_A", npc))
        startQuestTimer("check_orfen_pos", 60000, npc, null, true)

        // start monitoring Orfen's inactivity
        _lastAttackTime = System.currentTimeMillis()
    }

    companion object {
        private val ORFEN_LOCATION = arrayOf(
            SpawnLocation(43728, 17220, -4342, 0),
            SpawnLocation(55024, 17368, -5412, 0),
            SpawnLocation(53504, 21248, -5486, 0),
            SpawnLocation(53248, 24576, -5262, 0)
        )

        private val ORFEN_CHAT = arrayOf(
            "\$s1. Stop kidding yourself about your own powerlessness!",
            "\$s1. I'll make you feel what true fear is!",
            "You're really stupid to have challenged me. \$s1! Get ready!",
            "\$s1. Do you think that's going to work?!"
        )

        private const val ORFEN = 29014
        private const val RAIKEL_LEOS = 29016
        private const val RIBA_IREN = 29018

        private const val ALIVE: Byte = 0
        private const val DEAD: Byte = 1

        private var _lastAttackTime: Long = 0
        private var _isTeleported: Boolean = false
        private var _currentIndex: Int = 0

        /**
         * This method is used by Orfen in order to move from one location to another.<br></br>
         * Index 0 means a direct teleport to her lair (case where her HPs <= 50%).
         * @param npc : Orfen in any case.
         * @param index : 0 for her lair (teleport) or 1-3 (walking through desert).
         */
        private fun goTo(npc: Npc?, index: SpawnLocation) {
            (npc as Attackable).aggroList.clear()
            npc.getAI().setIntention(CtrlIntention.IDLE, null, null)

            // Edit the spawn location in case server crashes.
            val spawn = npc.getSpawn()
            spawn.loc = index

            if (index.x == 43728)
            // Hack !
                npc.teleToLocation(index.x, index.y, index.z, 0)
            else
                npc.getAI().setIntention(CtrlIntention.MOVE_TO, Location(index.x, index.y, index.z))
        }
    }
}