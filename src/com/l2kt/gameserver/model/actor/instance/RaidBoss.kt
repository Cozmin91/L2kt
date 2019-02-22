package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.RaidPointManager
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager.StatusEnum
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.type.AttackableAI
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.concurrent.ScheduledFuture

/**
 * This class manages all classic raid bosses.<br></br>
 * <br></br>
 * Raid Bosses (RB) are mobs which are supposed to be defeated by a party of several players. It extends most of [Monster] aspects.<br></br>
 * They automatically teleport if out of their initial spawn area, and can randomly attack a Player from their Hate List once attacked.
 */
class RaidBoss(objectId: Int, template: NpcTemplate) : Monster(objectId, template) {
    var raidStatus: StatusEnum? = null
    private var _maintenanceTask: ScheduledFuture<*>? = null

    init {
        setRaid(true)
    }

    override fun onSpawn() {
        // No random walk allowed.
        setIsNoRndWalk(true)

        // Basic behavior.
        super.onSpawn()

        // "AI task" for regular bosses.
        _maintenanceTask = ThreadPool.scheduleAtFixedRate(Runnable{
            // Don't bother with dead bosses.
            if (!isDead) {
                // The boss isn't in combat, check the teleport possibility.
                if (!isInCombat) {
                    // Gordon is excluded too.
                    if (npcId != 29095 && Rnd.nextBoolean()) {
                        // Spawn must exist.
                        val spawn = spawn ?: return@Runnable

                        // If the boss is above drift range (or 200 minimum), teleport him on his spawn.
                        if (!isInsideRadius(
                                spawn.locX,
                                spawn.locY,
                                spawn.locZ,
                                Math.max(Config.MAX_DRIFT_RANGE, 200),
                                true,
                                false
                            )
                        )
                            teleToLocation(spawn.loc, 0)
                    }
                } else if (Rnd[5] == 0)
                    (ai as AttackableAI).aggroReconsider()// Randomized attack if the boss is already attacking.
            }

            // For each minion (if any), randomize the attack.
            if (hasMinions()) {
                for (minion in minionList.spawnedMinions) {
                    // Don't bother with dead minions.
                    if (minion.isDead || !minion.isInCombat)
                        return@Runnable

                    // Randomized attack if the boss is already attacking.
                    if (Rnd[3] == 0)
                        (minion.ai as AttackableAI).aggroReconsider()
                }
            }
        }, 1000, 60000)
    }

    override fun doDie(killer: Creature?): Boolean {
        if (!super.doDie(killer))
            return false

        if (_maintenanceTask != null) {
            _maintenanceTask!!.cancel(false)
            _maintenanceTask = null
        }

        if (killer != null) {
            val player = killer?.actingPlayer
            if (player != null) {
                broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL))
                broadcastPacket(PlaySound("systemmsg_e.1209"))

                val party = player.party
                if (party != null) {
                    for (member in party.members) {
                        RaidPointManager.addPoints(member, npcId, level / 2 + Rnd[-5, 5])
                        if (member.isNoble)
                            Hero.setRBkilled(member.objectId, npcId)
                    }
                } else {
                    RaidPointManager.addPoints(player, npcId, level / 2 + Rnd[-5, 5])
                    if (player.isNoble)
                        Hero.setRBkilled(player.objectId, npcId)
                }
            }
        }

        RaidBossSpawnManager.updateStatus(this, true)
        return true
    }

    override fun deleteMe() {
        if (_maintenanceTask != null) {
            _maintenanceTask!!.cancel(false)
            _maintenanceTask = null
        }

        super.deleteMe()
    }
}