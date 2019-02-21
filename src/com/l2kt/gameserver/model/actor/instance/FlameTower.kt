package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.model.zone.CastleZoneType
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class FlameTower(objectId: Int, template: NpcTemplate) : Npc(objectId, template) {
    private var _upgradeLevel: Int = 0
    private var _zoneList: List<Int>? = null

    override// Attackable during siege by attacker only
    val isAttackable: Boolean
        get() = castle != null && castle.siege.isInProgress

    override fun isAutoAttackable(attacker: Creature): Boolean {
        // Attackable during siege by attacker only
        return attacker is Player && castle != null && castle.siege.isInProgress && castle.siege.checkSide(
            attacker.clan,
            SiegeSide.ATTACKER
        )
    }

    override fun onForcedAttack(player: Player) {
        onAction(player)
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player) && Math.abs(player.z - z) < 100 && GeoEngine.canSeeTarget(player, this)) {
                // Notify the Player AI with INTERACT
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            } else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)
            }
        }
    }

    override fun doDie(killer: Creature): Boolean {
        enableZones(false)

        if (castle != null) {
            // Message occurs only if the trap was triggered first.
            if (_zoneList != null && _upgradeLevel != 0)
                castle.siege.announceToPlayers(
                    SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_STOPPED),
                    false
                )

            // Spawn a little version of it. This version is a simple NPC, cleaned on siege end.
            try {
                val spawn = L2Spawn(NpcData.getTemplate(13005))
                spawn.loc = position

                val tower = spawn.doSpawn(false)
                tower!!.castle = castle

                castle.siege.destroyedTowers.add(tower)
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't spawn the flame tower.", e)
            }

        }

        return super.doDie(killer)
    }

    override fun deleteMe() {
        enableZones(false)
        super.deleteMe()
    }

    fun enableZones(state: Boolean) {
        if (_zoneList != null && _upgradeLevel != 0) {
            val maxIndex = _upgradeLevel * 2
            for (i in 0 until maxIndex) {
                val zone = ZoneManager.getZoneById(_zoneList!![i])
                if (zone != null && zone is CastleZoneType)
                    zone.isEnabled = state
            }
        }
    }

    fun setUpgradeLevel(level: Int) {
        _upgradeLevel = level
    }

    fun setZoneList(list: List<Int>) {
        _zoneList = list
        enableZones(true)
    }
}