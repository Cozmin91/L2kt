package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.location.Location

class RequestRestartPoint : L2GameClientPacket() {

    protected var _requestType: Int = 0

    override fun readImpl() {
        _requestType = readD()
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (player.isFakeDeath) {
            player.stopFakeDeath(true)
            return
        }

        if (!player.isDead())
            return

        // Schedule a respawn delay if player is part of a clan registered in an active siege.
        if (player.clan != null) {
            val siege = CastleManager.getActiveSiege(player)
            if (siege != null && siege.checkSide(player.clan, Siege.SiegeSide.ATTACKER)) {
                ThreadPool.schedule(Runnable{ portPlayer(player) }, Config.ATTACKERS_RESPAWN_DELAY.toLong())
                return
            }
        }

        portPlayer(player)
    }

    /**
     * Teleport the [Player] to the associated [Location], based on _requestType.
     * @param player : The player set as parameter.
     */
    private fun portPlayer(player: Player) {
        val clan = player.clan

        val loc: Location?

        // Enforce type.
        if (player.isInJail)
            _requestType = 27
        else if (player.isFestivalParticipant)
            _requestType = 4

        // To clanhall.
        if (_requestType == 1) {
            if (clan == null || !clan.hasHideout())
                return

            loc = MapRegionData.getLocationToTeleport(player, MapRegionData.TeleportType.CLAN_HALL)

            val ch = ClanHallManager.getClanHallByOwner(clan)
            if (ch != null) {
                val function = ch.getFunction(ClanHall.FUNC_RESTORE_EXP)
                if (function != null)
                    player.restoreExp(function.lvl.toDouble())
            }
        } else if (_requestType == 2) {
            val siege = CastleManager.getActiveSiege(player)
            loc = if (siege != null) {
                val side = siege.getSide(clan)
                if (side == Siege.SiegeSide.DEFENDER || side == Siege.SiegeSide.OWNER)
                    MapRegionData.getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE)
                else if (side == Siege.SiegeSide.ATTACKER)
                    MapRegionData.getLocationToTeleport(player, MapRegionData.TeleportType.TOWN)
                else
                    return
            } else {
                if (clan == null || !clan.hasCastle())
                    return

                MapRegionData.getLocationToTeleport(player, MapRegionData.TeleportType.CASTLE)
            }
        } else if (_requestType == 3)
            loc = MapRegionData.getLocationToTeleport(player, MapRegionData.TeleportType.SIEGE_FLAG)
        else if (_requestType == 4) {
            if (!player.isGM && !player.isFestivalParticipant)
                return

            loc = player.position
        } else if (_requestType == 27) {
            if (!player.isInJail)
                return

            loc = JAIL_LOCATION
        } else
            loc = MapRegionData.getLocationToTeleport(
                player,
                MapRegionData.TeleportType.TOWN
            )// Nothing has been found, use regular "To town" behavior.
        // To jail.
        // Fixed.
        // To siege flag.
        // To castle.

        player.isIn7sDungeon = false

        if (player.isDead())
            player.doRevive()

        player.teleToLocation(loc, 20)
    }

    companion object {
        protected val JAIL_LOCATION = Location(-114356, -249645, -2984)
    }
}