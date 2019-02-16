package com.l2kt.gameserver.data.manager

import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.model.actor.template.CreatureTemplate
import com.l2kt.gameserver.model.location.BoatLocation
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.templates.StatsSet
import java.util.*

object BoatManager {

    private val _boats = HashMap<Int, Boat>()
    private val _docksBusy = BooleanArray(3)
    const val TALKING_ISLAND = 0
    const val GLUDIN_HARBOR = 1
    const val RUNE_HARBOR = 2

    const val BOAT_BROADCAST_RADIUS = 20000

    /**
     * Generate a new [Boat], using a fresh [CreatureTemplate].
     * @param boatId : The boat id to use.
     * @param x : The X position to use.
     * @param y : The Y position to use.
     * @param z : The Z position to use.
     * @param heading : The heading to use.
     * @return the new boat instance.
     */
    fun getNewBoat(boatId: Int, x: Int, y: Int, z: Int, heading: Int): Boat {
        val set = StatsSet()
        set.set("id", boatId)
        set.set("level", 0)

        set.set("str", 0)
        set.set("con", 0)
        set.set("dex", 0)
        set.set("int", 0)
        set.set("wit", 0)
        set.set("men", 0)

        set.set("hp", 50000)
        set.set("mp", 0)

        set.set("hpRegen", 3e-3)
        set.set("mpRegen", 3e-3)

        set.set("radius", 0)
        set.set("height", 0)
        set.set("type", "")

        set.set("exp", 0)
        set.set("sp", 0)

        set.set("pAtk", 0)
        set.set("mAtk", 0)
        set.set("pDef", 100)
        set.set("mDef", 100)

        set.set("rHand", 0)
        set.set("lHand", 0)

        set.set("walkSpd", 0)
        set.set("runSpd", 0)

        val template = CreatureTemplate(set)
        val boat = Boat(IdFactory.getInstance().nextId, template)

        _boats[boat.objectId] = boat

        boat.heading = heading
        boat.spawnMe(x, y, z)

        return boat
    }

    fun getBoat(boatId: Int): Boat? {
        return _boats[boatId]
    }

    /**
     * Lock/unlock dock so only one boat can be docked.
     * @param dockId : The dock id.
     * @param value : True if the dock is locked.
     */
    fun dockBoat(dockId: Int, value: Boolean) {
        _docksBusy[dockId] = value
    }

    /**
     * Check if the dock is busy.
     * @param dockId : The dock id.
     * @return true if the dock is locked, false otherwise.
     */
    fun isBusyDock(dockId: Int): Boolean {
        return _docksBusy[dockId]
    }

    /**
     * Broadcast one packet in both path points.
     * @param point1 : The first location to broadcast the packet.
     * @param point2 : The second location to broadcast the packet.
     * @param packet : The packet to broadcast.
     */
    fun broadcastPacket(point1: BoatLocation, point2: BoatLocation, packet: L2GameServerPacket) {
        for (player in World.players) {
            var dx = player.x.toDouble() - point1.x
            var dy = player.y.toDouble() - point1.y

            if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
                player.sendPacket(packet)
            else {
                dx = player.x.toDouble() - point2.x
                dy = player.y.toDouble() - point2.y

                if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
                    player.sendPacket(packet)
            }
        }
    }

    /**
     * Broadcast several packets in both path points.
     * @param point1 : The first location to broadcast the packet.
     * @param point2 : The second location to broadcast the packet.
     * @param packets : The packets to broadcast.
     */
    fun broadcastPackets(point1: BoatLocation, point2: BoatLocation, vararg packets: L2GameServerPacket) {
        for (player in World.players) {
            var dx = player.x.toDouble() - point1.x
            var dy = player.y.toDouble() - point1.y

            if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS) {
                for (p in packets)
                    player.sendPacket(p)
            } else {
                dx = player.x.toDouble() - point2.x
                dy = player.y.toDouble() - point2.y

                if (Math.sqrt(dx * dx + dy * dy) < BOAT_BROADCAST_RADIUS)
                    for (p in packets)
                        player.sendPacket(p)
            }
        }
    }
}