package com.l2kt.gameserver.scripting.scripts.ai.individual

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.NpcSay
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import java.util.*

/**
 * Benom is a specific Raid Boss, appearing in Rune Castle. He is aggressive towards anyone.<br></br>
 * <br></br>
 * The castle owning clan can defeat Benom. It can teleport to Benom's den using a specific gatekeeper, 24 hours before siege start. If the clan doesn't kill Benom before the siege start, Benom will appear during the siege if at least 2 life controls crystals have been broken.
 */
class Benom : L2AttackableAIScript("ai/individual") {

    private val _siege: Siege = addSiegeNotify(8)

    private var _benom: Npc? = null

    private var _isPrisonOpened: Boolean = false

    private val _targets = ArrayList<Player>()

    init {

        addStartNpc(DUNGEON_KEEPER, TELEPORT_CUBE)
        addTalkId(DUNGEON_KEEPER, TELEPORT_CUBE)
    }

    override fun registerNpcs() {
        addEventIds(BENOM, EventType.ON_AGGRO, EventType.ON_SPELL_FINISHED, EventType.ON_ATTACK, EventType.ON_KILL)
    }

    override fun onTalk(npc: Npc, talker: Player): String? {
        when (npc.npcId) {
            TELEPORT_CUBE -> talker.teleToLocation(MapRegionData.TeleportType.TOWN)

            DUNGEON_KEEPER -> if (_isPrisonOpened)
                talker.teleToLocation(12589, -49044, -3008, 0)
            else
                return HtmCache.getHtm("data/html/doormen/35506-2.htm")
        }
        return super.onTalk(npc, talker)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        when (event) {
            "benom_spawn" -> {
                _isPrisonOpened = true

                _benom = addSpawn(BENOM, PRISON_LOC, false, 0, false)
                _benom!!.broadcastNpcSay("Who dares to covet the throne of our castle! Leave immediately or you will pay the price of your audacity with your very own blood!")
            }

            "tower_check" -> if (_siege.controlTowerCount < 2) {
                npc!!.teleToLocation(THRONE_LOC, 0)
                _siege.castle.siegeZone!!.broadcastPacket(
                    NpcSay(
                        0,
                        Say2.ALL,
                        DUNGEON_KEEPER,
                        "Oh no! The defenses have failed. It is too dangerous to remain inside the castle. Flee! Every man for himself!"
                    )
                )

                cancelQuestTimer("tower_check", npc, null)
                startQuestTimer("raid_check", 10000, npc, null, true)
            }

            "raid_check" -> if (!npc!!.isInsideZone(ZoneId.SIEGE) && !npc.isTeleporting)
                npc.teleToLocation(THRONE_LOC, 0)
        }
        return event
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        if (isPet)
            return super.onAggro(npc, player, isPet)

        if (_targets.size < 10 && Rnd[3] < 1 && player != null)
            _targets.add(player)

        return super.onAggro(npc, player, isPet)
    }

    override fun onSiegeEvent() {
        // Don't go further if the castle isn't owned.
        if (_siege.castle.ownerId <= 0)
            return

        when (_siege.status) {
            Siege.SiegeStatus.IN_PROGRESS -> {
                _isPrisonOpened = false
                if (_benom != null && !_benom!!.isDead())
                    startQuestTimer("tower_check", 30000, _benom, null, true)
            }

            Siege.SiegeStatus.REGISTRATION_OPENED -> {
                _isPrisonOpened = false

                if (_benom != null) {
                    cancelQuestTimer("tower_check", _benom, null)
                    cancelQuestTimer("raid_check", _benom, null)

                    _benom!!.deleteMe()
                }

                startQuestTimer(
                    "benom_spawn",
                    _siege.siegeDate!!.timeInMillis - 8640000 - System.currentTimeMillis(),
                    null,
                    null,
                    false
                )
            }

            Siege.SiegeStatus.REGISTRATION_OVER -> startQuestTimer("benom_spawn", 0, null, null, false)
        }
    }

    override fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        if(player == null)
            return null

        when (skill?.id) {
            4995 -> {
                teleportTarget(player)
                (npc as Attackable).stopHating(player)
            }

            4996 -> {
                teleportTarget(player)
                (npc as Attackable).stopHating(player)
                if (!_targets.isEmpty()) {
                    for (target in _targets) {
                        val x = (player.x - target.x).toLong()
                        val y = (player.y - target.y).toLong()
                        val z = (player.z - target.z).toLong()
                        val range: Long = 250
                        if (x * x + y * y + z * z <= range * range) {
                            teleportTarget(target)
                            npc.stopHating(target)
                        }
                    }
                    _targets.clear()
                }
            }
        }

        return null
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (attacker is Playable) {
            if (Rnd[100] <= 25) {
                npc.target = attacker
                npc.doCast(SkillTable.getInfo(4995, 1))
            } else if (!npc.isCastingNow) {
                if (npc.currentHp < npc.maxHp / 3 && Rnd[500] < 1) {
                    npc.target = attacker
                    npc.doCast(SkillTable.getInfo(4996, 1))
                } else if (!npc.isInsideRadius(attacker, 300, true, false) && Rnd[100] < 1) {
                    npc.target = attacker
                    npc.doCast(SkillTable.getInfo(4993, 1))
                } else if (Rnd[100] < 1) {
                    npc.target = attacker
                    npc.doCast(SkillTable.getInfo(4994, 1))
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        npc.broadcastNpcSay("It's not over yet... It won't be... over... like this... Never...")
        cancelQuestTimer("raid_check", npc, null)

        addSpawn(TELEPORT_CUBE, 12589, -49044, -3008, 0, false, 120000, false)

        return null
    }

    companion object {
        private const val BENOM = 29054
        private const val TELEPORT_CUBE = 29055
        private const val DUNGEON_KEEPER = 35506

        // Important : the heading is used as offset.
        private val TARGET_TELEPORTS = arrayOf(
            SpawnLocation(12860, -49158, -976, 650),
            SpawnLocation(14878, -51339, 1024, 100),
            SpawnLocation(15674, -49970, 864, 100),
            SpawnLocation(15696, -48326, 864, 100),
            SpawnLocation(14873, -46956, 1024, 100),
            SpawnLocation(12157, -49135, -1088, 650),
            SpawnLocation(12875, -46392, -288, 200),
            SpawnLocation(14087, -46706, -288, 200),
            SpawnLocation(14086, -51593, -288, 200),
            SpawnLocation(12864, -51898, -288, 200),
            SpawnLocation(15538, -49153, -1056, 200),
            SpawnLocation(17001, -49149, -1064, 650)
        )

        private val THRONE_LOC = SpawnLocation(11025, -49152, -537, 0)
        private val PRISON_LOC = SpawnLocation(11882, -49216, -3008, 0)

        /**
         * Move a player by Skill. Venom has two skill related.
         * @param player the player targeted
         */
        private fun teleportTarget(player: Player?) {
            if (player != null) {
                val loc = Rnd[TARGET_TELEPORTS]
                player.teleToLocation(loc, loc!!.heading)
            }
        }
    }
}