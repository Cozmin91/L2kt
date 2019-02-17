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

class BoatGludinRune(private val _boat: Boat) : Runnable {
    private var _cycle = 0
    private var _shoutCount = 0

    private val ARRIVED_AT_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GLUDIN)
    private val ARRIVED_AT_GLUDIN_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_10_MINUTES)
    private val LEAVE_GLUDIN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_5_MINUTES)
    private val LEAVE_GLUDIN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_1_MINUTE)
    private val LEAVING_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_RUNE_NOW)
    private val ARRIVED_AT_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.ARRIVED_AT_RUNE)
    private val ARRIVED_AT_RUNE_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GLUDIN_AFTER_10_MINUTES)
    private val LEAVE_RUNE5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_5_MINUTES)
    private val LEAVE_RUNE1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_1_MINUTE)
    private val LEAVE_RUNE0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_SHORTLY)
    private val LEAVING_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.DEPARTURE_FOR_GLUDIN_NOW)
    private val BUSY_GLUDIN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_RUNE_GLUDIN_DELAYED)
    private val BUSY_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GLUDIN_RUNE_DELAYED)

    private val ARRIVAL_RUNE15: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_15_MINUTES)
    private val ARRIVAL_RUNE10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_10_MINUTES)
    private val ARRIVAL_RUNE5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_5_MINUTES)
    private val ARRIVAL_RUNE1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GLUDIN_AT_RUNE_1_MINUTE)
    private val ARRIVAL_GLUDIN15: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_15_MINUTES)
    private val ARRIVAL_GLUDIN10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_10_MINUTES)
    private val ARRIVAL_GLUDIN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_5_MINUTES)
    private val ARRIVAL_GLUDIN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_RUNE_AT_GLUDIN_1_MINUTE)

    private val GLUDIN_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)
    private val RUNE_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)

    private val GLUDIN_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val GLUDIN_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    private val RUNE_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val RUNE_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    override fun run() {
        when (_cycle) {
            0 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN5)
                _boat.broadcastPacket(GLUDIN_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            1 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVE_GLUDIN1)
                _boat.broadcastPacket(GLUDIN_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 60000)
            }
            2 -> {
                BoatManager.dockBoat(BoatManager.GLUDIN_HARBOR, false)
                BoatManager.broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], LEAVING_GLUDIN)
                _boat.broadcastPacket(GLUDIN_SOUND)
                _boat.payForRide(7905, 1, OUST_LOC_1)
                _boat.executePath(GLUDIN_TO_RUNE)
                ThreadPool.schedule(this, 250000)
            }
            3 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE15)
                ThreadPool.schedule(this, 300000)
            }
            4 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE10)
                ThreadPool.schedule(this, 300000)
            }
            5 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE5)
                ThreadPool.schedule(this, 240000)
            }
            6 -> BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVAL_RUNE1)
            7 -> {
                if (BoatManager.isBusyDock(BoatManager.RUNE_HARBOR)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], BUSY_RUNE)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.RUNE_HARBOR, true)
                _boat.executePath(RUNE_DOCK)
            }
            8 -> {
                BoatManager.broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], ARRIVED_AT_RUNE, ARRIVED_AT_RUNE_2)
                _boat.broadcastPacket(RUNE_SOUND)
                ThreadPool.schedule(this, 300000)
            }
            9 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE5)
                _boat.broadcastPacket(RUNE_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            10 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE1)
                _boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            11 -> {
                BoatManager.broadcastPacket(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVE_RUNE0)
                _boat.broadcastPacket(RUNE_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            12 -> {
                BoatManager.dockBoat(BoatManager.RUNE_HARBOR, false)
                BoatManager.broadcastPackets(RUNE_DOCK[0], GLUDIN_DOCK[0], LEAVING_RUNE)
                _boat.broadcastPacket(RUNE_SOUND)
                _boat.payForRide(7904, 1, OUST_LOC_2)
                _boat.executePath(RUNE_TO_GLUDIN)
                ThreadPool.schedule(this, 60000)
            }
            13 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN15)
                ThreadPool.schedule(this, 300000)
            }
            14 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN10)
                ThreadPool.schedule(this, 300000)
            }
            15 -> {
                BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN5)
                ThreadPool.schedule(this, 240000)
            }
            16 -> BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVAL_GLUDIN1)
            17 -> {
                if (BoatManager.isBusyDock(BoatManager.GLUDIN_HARBOR)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(GLUDIN_DOCK[0], RUNE_DOCK[0], BUSY_GLUDIN)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.GLUDIN_HARBOR, true)
                _boat.executePath(GLUDIN_DOCK)
            }
            18 -> {
                BoatManager.broadcastPackets(GLUDIN_DOCK[0], RUNE_DOCK[0], ARRIVED_AT_GLUDIN, ARRIVED_AT_GLUDIN_2)
                _boat.broadcastPacket(GLUDIN_SOUND)
                ThreadPool.schedule(this, 300000)
            }
        }
        _shoutCount = 0

        _cycle++
        if (_cycle > 18)
            _cycle = 0
    }

    companion object {
        private val OUST_LOC_1 = Location(-90015, 150422, -3610)
        private val OUST_LOC_2 = Location(34513, -38009, -3640)

        // Time: 1151s
        private val GLUDIN_TO_RUNE = arrayOf(
            BoatLocation(-95686, 155514, -3610, 150, 800),
            BoatLocation(-98112, 159040, -3610, 150, 800),
            BoatLocation(-104192, 160608, -3610, 200, 1800),
            BoatLocation(-109952, 159616, -3610, 250, 1800),
            BoatLocation(-112768, 154784, -3610, 290, 1800),
            BoatLocation(-114688, 139040, -3610, 290, 1800),
            BoatLocation(-115232, 134368, -3610, 290, 1800),
            BoatLocation(-113888, 121696, -3610, 290, 1800),
            BoatLocation(-107808, 104928, -3610, 290, 1800),
            BoatLocation(-97152, 75520, -3610, 290, 800),
            BoatLocation(-85536, 67264, -3610, 290, 1800),
            BoatLocation(-64640, 55840, -3610, 290, 1800),
            BoatLocation(-60096, 44672, -3610, 290, 1800),
            BoatLocation(-52672, 37440, -3610, 290, 1800),
            BoatLocation(-46144, 33184, -3610, 290, 1800),
            BoatLocation(-36096, 24928, -3610, 290, 1800),
            BoatLocation(-33792, 8448, -3610, 290, 1800),
            BoatLocation(-23776, 3424, -3610, 290, 1000),
            BoatLocation(-12000, -1760, -3610, 290, 1000),
            BoatLocation(672, 480, -3610, 290, 1800),
            BoatLocation(15488, 200, -3610, 290, 1000),
            BoatLocation(24736, 164, -3610, 290, 1000),
            BoatLocation(32192, -1156, -3610, 290, 1000),
            BoatLocation(39200, -8032, -3610, 270, 1000),
            BoatLocation(44320, -25152, -3610, 270, 1000),
            BoatLocation(40576, -31616, -3610, 250, 800),
            BoatLocation(36819, -35315, -3610, 220, 800)
        )

        private val RUNE_DOCK = arrayOf(BoatLocation(34381, -37680, -3610, 200, 800))

        // Time: 967s
        private val RUNE_TO_GLUDIN = arrayOf(
            BoatLocation(32750, -39300, -3610, 150, 800),
            BoatLocation(27440, -39328, -3610, 180, 1000),
            BoatLocation(21456, -34272, -3610, 200, 1000),
            BoatLocation(6608, -29520, -3610, 250, 800),
            BoatLocation(4160, -27828, -3610, 270, 800),
            BoatLocation(2432, -25472, -3610, 270, 1000),
            BoatLocation(-8000, -16272, -3610, 220, 1000),
            BoatLocation(-18976, -9760, -3610, 290, 800),
            BoatLocation(-23776, 3408, -3610, 290, 800),
            BoatLocation(-33792, 8432, -3610, 290, 800),
            BoatLocation(-36096, 24912, -3610, 290, 800),
            BoatLocation(-46144, 33184, -3610, 290, 800),
            BoatLocation(-52688, 37440, -3610, 290, 800),
            BoatLocation(-60096, 44672, -3610, 290, 800),
            BoatLocation(-64640, 55840, -3610, 290, 800),
            BoatLocation(-85552, 67248, -3610, 290, 800),
            BoatLocation(-97168, 85264, -3610, 290, 800),
            BoatLocation(-107824, 104912, -3610, 290, 800),
            BoatLocation(-102151, 135704, -3610, 290, 800),
            BoatLocation(-96686, 140595, -3610, 290, 800),
            BoatLocation(-95686, 147717, -3610, 250, 800),
            BoatLocation(-95686, 148218, -3610, 200, 800)
        )

        private val GLUDIN_DOCK = arrayOf(BoatLocation(-95686, 150514, -3610, 150, 800))

        fun load() {
            val boat = BoatManager.getNewBoat(3, -95686, 150514, -3610, 16723)
            if (boat != null) {
                boat.registerEngine(BoatGludinRune(boat))
                boat.runEngine(180000)
                BoatManager.dockBoat(BoatManager.GLUDIN_HARBOR, true)
            }
        }
    }
}
