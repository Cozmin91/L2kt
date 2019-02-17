package com.l2kt.gameserver.model.boat

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.manager.BoatManager

import com.l2kt.gameserver.model.actor.Boat
import com.l2kt.gameserver.model.location.BoatLocation
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.PlaySound

class BoatTalkingGludin(private val _boat: Boat) : Runnable {
    private var _cycle = 0
    private var _shoutCount = 0

    private val ARRIVED_AT_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING)
    private val ARRIVED_AT_TALKING_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES)
    private val LEAVE_TALKING5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_5_MINUTES)
    private val LEAVE_TALKING1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_IN_1_MINUTE)
    private val LEAVE_TALKING1_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.MAKE_HASTE_GET_ON_BOAT)
    private val LEAVE_TALKING0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GLUDIN)
    private val LEAVING_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_GLUDIN)
    private val ARRIVED_AT_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN)
    private val ARRIVED_AT_GLUDIN_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES)
    private val LEAVE_GLUDIN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES)
    private val LEAVE_GLUDIN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE)
    private val LEAVE_GLUDIN0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING)
    private val LEAVING_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING)
    private val BUSY_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GLUDIN_TALKING_DELAYED)
    private val BUSY_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_TALKING_GLUDIN_DELAYED)

    private val ARRIVAL_GLUDIN10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_10_MINUTES)
    private val ARRIVAL_GLUDIN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_5_MINUTES)
    private val ARRIVAL_GLUDIN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GLUDIN_1_MINUTE)
    private val ARRIVAL_TALKING10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_10_MINUTES)
    private val ARRIVAL_TALKING5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_5_MINUTES)
    private val ARRIVAL_TALKING1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_ARRIVE_AT_TALKING_1_MINUTE)

    private val TALKING_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)
    private val GLUDIN_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)

    private val TALKING_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val TALKING_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    private val GLUDIN_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val GLUDIN_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    override fun run() {
        when (_cycle) {
            0 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING5)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            1 -> {
                BoatManager.broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING1, LEAVE_TALKING1_2)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            2 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_TALKING0)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            3 -> {
                BoatManager.dockBoat(BoatManager.TALKING_ISLAND, false)
                BoatManager.broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVING_TALKING)
                _boat.broadcastPacket(TALKING_SOUND)
                _boat.payForRide(1074, 1, OUST_LOC_1)
                _boat.executePath(TALKING_TO_GLUDIN)
                ThreadPool.schedule(this, 300000)
            }
            4 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN10)
                ThreadPool.schedule(this, 300000)
            }
            5 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN5)
                ThreadPool.schedule(this, 240000)
            }
            6 -> BoatManager.broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVAL_GLUDIN1)
            7 -> {
                if (BoatManager.isBusyDock(BoatManager.GLUDIN_HARBOR)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(GLUDIN_DOCK[0], TALKING_DOCK[0], BUSY_GLUDIN)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.GLUDIN_HARBOR, true)
                _boat.executePath(GLUDIN_DOCK)
            }
            8 -> {
                BoatManager.broadcastPackets(GLUDIN_DOCK[0], TALKING_DOCK[0], ARRIVED_AT_GLUDIN, ARRIVED_AT_GLUDIN_2)
                _boat.broadcastPacket(GLUDIN_SOUND)
                ThreadPool.schedule(this, 300000)
            }
            9 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN5)
                _boat.broadcastPacket(GLUDIN_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            10 -> {
                BoatManager.broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN1, LEAVE_TALKING1_2)
                _boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            11 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVE_GLUDIN0)
                _boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            12 -> {
                BoatManager.dockBoat(BoatManager.GLUDIN_HARBOR, false)
                BoatManager.broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], LEAVING_GLUDIN)
                _boat.broadcastPacket(GLUDIN_SOUND)
                _boat.payForRide(1075, 1, OUST_LOC_2)
                _boat.executePath(GLUDIN_TO_TALKING)
                ThreadPool.schedule(this, 150000)
            }
            13 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING10)
                ThreadPool.schedule(this, 300000)
            }
            14 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING5)
                ThreadPool.schedule(this, 240000)
            }
            15 -> BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_TALKING1)
            16 -> {
                if (BoatManager.isBusyDock(BoatManager.TALKING_ISLAND)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(TALKING_DOCK[0], GLUDIN_DOCK[0], BUSY_TALKING)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.TALKING_ISLAND, true)
                _boat.executePath(TALKING_DOCK)
            }
            17 -> {
                BoatManager.broadcastPackets(TALKING_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_TALKING, ARRIVED_AT_TALKING_2)
                _boat.broadcastPacket(TALKING_SOUND)
                ThreadPool.schedule(this, 300000)
            }
        }
        _shoutCount = 0

        _cycle++
        if (_cycle > 17)
            _cycle = 0
    }

    companion object {
        private val OUST_LOC_1 = Location(-96777, 258970, -3623)
        private val OUST_LOC_2 = Location(-90015, 150422, -3610)

        // Time: 919s
        private val TALKING_TO_GLUDIN = arrayOf(
            BoatLocation(-121385, 261660, -3610, 180, 800),
            BoatLocation(-127694, 253312, -3610, 200, 800),
            BoatLocation(-129274, 237060, -3610, 250, 800),
            BoatLocation(-114688, 139040, -3610, 200, 800),
            BoatLocation(-109663, 135704, -3610, 180, 800),
            BoatLocation(-102151, 135704, -3610, 180, 800),
            BoatLocation(-96686, 140595, -3610, 180, 800),
            BoatLocation(-95686, 147718, -3610, 180, 800),
            BoatLocation(-95686, 148718, -3610, 180, 800),
            BoatLocation(-95686, 149718, -3610, 150, 800)
        )

        private val GLUDIN_DOCK = arrayOf(BoatLocation(-95686, 150514, -3610, 150, 800))

        // Time: 780s
        private val GLUDIN_TO_TALKING = arrayOf(
            BoatLocation(-95686, 155514, -3610, 180, 800),
            BoatLocation(-95686, 185514, -3610, 250, 800),
            BoatLocation(-60136, 238816, -3610, 200, 800),
            BoatLocation(-60520, 259609, -3610, 180, 1800),
            BoatLocation(-65344, 261460, -3610, 180, 1800),
            BoatLocation(-83344, 261560, -3610, 180, 1800),
            BoatLocation(-88344, 261660, -3610, 180, 1800),
            BoatLocation(-92344, 261660, -3610, 150, 1800),
            BoatLocation(-94242, 261659, -3610, 150, 1800)
        )

        private val TALKING_DOCK = arrayOf(BoatLocation(-96622, 261660, -3610, 150, 1800))

        fun load() {
            val boat = BoatManager.getNewBoat(1, -96622, 261660, -3610, 32768)
            if (boat != null) {
                boat.registerEngine(BoatTalkingGludin(boat))
                boat.runEngine(180000)
                BoatManager.dockBoat(BoatManager.TALKING_ISLAND, true)
            }
        }
    }
}