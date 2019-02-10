package com.l2kt.gameserver.extensions

import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldRegion
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket

/**
 * Send a packet to all known players of the Creature that have the Character targeted.
 * @param character : The character to make checks on.
 * @param packet : The packet to send.
 */
fun Creature.toPlayersTargetingMyself(packet: L2GameServerPacket) {
    for (player in getKnownType(Player::class.java)) {
        if (player.target !== this)
            continue

        player.sendPacket(packet)
    }
}

/**
 * Send a packet to all known players of the Creature.
 * @param character : The character to make checks on.
 * @param packet : The packet to send.
 */
fun Creature.toKnownPlayers(packet: L2GameServerPacket) {
    for (player in getKnownType(Player::class.java))
        player.sendPacket(packet)
}

/**
 * Send a packet to all known players, in a specified radius, of the Creature.
 * @param character : The character to make checks on.
 * @param packet : The packet to send.
 * @param radius : The given radius.
 */
fun Creature.toKnownPlayersInRadius(packet: L2GameServerPacket, radius: Int) {
    var radius = radius
    if (radius < 0)
        radius = 1500

    for (player in getKnownTypeInRadius(Player::class.java, radius))
        player.sendPacket(packet)
}

/**
 * Send a packet to all known players of the Creature and to the specified Creature.
 * @param character : The character to make checks on.
 * @param packet : The packet to send.
 */
fun Creature.toSelfAndKnownPlayers(packet: L2GameServerPacket) {
    if (this is Player)
        sendPacket(packet)

    toKnownPlayers(packet)
}

/**
 * Send a packet to all known players, in a specified radius, of the Creature and to the specified Creature.
 * @param character : The character to make checks on.
 * @param packet : The packet to send.
 * @param radius : The given radius.
 */
fun Creature.toSelfAndKnownPlayersInRadius(packet: L2GameServerPacket, radius: Int) {
    var radius = radius
    if (radius < 0)
        radius = 600

    if (this is Player)
        sendPacket(packet)

    for (player in getKnownTypeInRadius(Player::class.java, radius))
        player.sendPacket(packet)
}

/**
 * Send a packet to all players present in the world.
 * @param packet : The packet to send.
 */
fun L2GameServerPacket.toAllOnlinePlayers() {
    for (player in World.getInstance().players) {
        if (player.isOnline)
            player.sendPacket(this)
    }
}

/**
 * Send a packet to all players in a specific region.
 * @param region : The region to send packets.
 * @param packets : The packets to send.
 */
fun WorldRegion.toAllPlayersInRegion(vararg packets: L2GameServerPacket) {
    for (`object` in objects) {
        if (`object` is Player) {
            for (packet in packets)
                `object`.sendPacket(packet)
        }
    }
}

/**
 * Send a packet to all players in a specific zone type.
 * @param <T> L2ZoneType.
 * @param zoneType : The zone type to send packets.
 * @param packets : The packets to send.
</T> */
fun <T : ZoneType> toAllPlayersInZoneType(zoneType: Class<T>, vararg packets: L2GameServerPacket) {
    for (temp in ZoneManager.getInstance().getAllZones(zoneType)) {
        for (player in temp.getKnownTypeInside(Player::class.java)) {
            for (packet in packets)
                player.sendPacket(packet)
        }
    }
}

fun String.announceToOnlinePlayers() {
    CreatureSay(0, Say2.ANNOUNCEMENT, "", this).toAllOnlinePlayers()
}

fun String.announceToOnlinePlayers(critical: Boolean) {
    CreatureSay(0, if (critical) Say2.CRITICAL_ANNOUNCE else Say2.ANNOUNCEMENT, "", this).toAllOnlinePlayers()
}