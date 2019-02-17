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

class BoatGiranTalking(private val _boat: Boat) : Runnable {
    private var _cycle = 0
    private var _shoutCount = 0

    private val ARRIVED_AT_GIRAN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_GIRAN)
    private val ARRIVED_AT_GIRAN_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_AFTER_10_MINUTES)
    private val LEAVE_GIRAN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_5_MINUTES)
    private val LEAVE_GIRAN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_TALKING_IN_1_MINUTE)
    private val LEAVE_GIRAN0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_TALKING)
    private val LEAVING_GIRAN: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_TALKING)
    private val ARRIVED_AT_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_TALKING)
    private val ARRIVED_AT_TALKING_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_AFTER_10_MINUTES)
    private val LEAVE_TALKING5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_5_MINUTES)
    private val LEAVE_TALKING1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_FOR_GIRAN_IN_1_MINUTE)
    private val LEAVE_TALKING0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVE_SOON_FOR_GIRAN)
    private val LEAVING_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_GIRAN)
    private val BUSY_TALKING: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_GIRAN_TALKING_DELAYED)

    private val ARRIVAL_TALKING15: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_15_MINUTES)
    private val ARRIVAL_TALKING10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_10_MINUTES)
    private val ARRIVAL_TALKING5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_5_MINUTES)
    private val ARRIVAL_TALKING1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_GIRAN_ARRIVE_AT_TALKING_1_MINUTE)
    private val ARRIVAL_GIRAN20: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_20_MINUTES)
    private val ARRIVAL_GIRAN15: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_15_MINUTES)
    private val ARRIVAL_GIRAN10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_10_MINUTES)
    private val ARRIVAL_GIRAN5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_5_MINUTES)
    private val ARRIVAL_GIRAN1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_TALKING_ARRIVE_AT_GIRAN_1_MINUTE)

    private val GIRAN_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)
    private val TALKING_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)

    private val GIRAN_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val GIRAN_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    private val TALKING_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val TALKING_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    override fun run() {
        when (_cycle) {
            0 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN5)
                _boat.broadcastPacket(GIRAN_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            1 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN1)
                _boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            2 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], LEAVE_GIRAN0)
                _boat.broadcastPacket(GIRAN_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            3 -> {
                BoatManager.broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], LEAVING_GIRAN, ARRIVAL_TALKING15)
                _boat.broadcastPacket(GIRAN_SOUND)
                _boat.payForRide(3946, 1, OUST_LOC_1)
                _boat.executePath(GIRAN_TO_TALKING)
                ThreadPool.schedule(this, 250000)
            }
            4 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING10)
                ThreadPool.schedule(this, 300000)
            }
            5 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING5)
                ThreadPool.schedule(this, 240000)
            }
            6 -> BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, ARRIVAL_TALKING1)
            7 -> {
                if (BoatManager.isBusyDock(BoatManager.TALKING_ISLAND)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, BUSY_TALKING)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.TALKING_ISLAND, true)
                _boat.executePath(TALKING_DOCK)
            }
            8 -> {
                BoatManager.broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, ARRIVED_AT_TALKING, ARRIVED_AT_TALKING_2)
                _boat.broadcastPacket(TALKING_SOUND)
                ThreadPool.schedule(this, 300000)
            }
            9 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING5)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            10 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING1)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            11 -> {
                BoatManager.broadcastPacket(TALKING_DOCK[0], GIRAN_DOCK, LEAVE_TALKING0)
                _boat.broadcastPacket(TALKING_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            12 -> {
                BoatManager.dockBoat(BoatManager.TALKING_ISLAND, false)
                BoatManager.broadcastPackets(TALKING_DOCK[0], GIRAN_DOCK, LEAVING_TALKING)
                _boat.broadcastPacket(TALKING_SOUND)
                _boat.payForRide(3945, 1, OUST_LOC_2)
                _boat.executePath(TALKING_TO_GIRAN)
                ThreadPool.schedule(this, 200000)
            }
            13 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN20)
                ThreadPool.schedule(this, 300000)
            }
            14 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN15)
                ThreadPool.schedule(this, 300000)
            }
            15 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN10)
                ThreadPool.schedule(this, 300000)
            }
            16 -> {
                BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN5)
                ThreadPool.schedule(this, 240000)
            }
            17 -> BoatManager.broadcastPacket(GIRAN_DOCK, TALKING_DOCK[0], ARRIVAL_GIRAN1)
            18 -> {
                BoatManager.broadcastPackets(GIRAN_DOCK, TALKING_DOCK[0], ARRIVED_AT_GIRAN, ARRIVED_AT_GIRAN_2)
                _boat.broadcastPacket(GIRAN_SOUND)
                ThreadPool.schedule(this, 300000)
            }
        }
        _shoutCount = 0

        _cycle++
        if (_cycle > 18)
            _cycle = 0
    }

    companion object {
        private val OUST_LOC_1 = Location(46763, 187041, -3451)
        private val OUST_LOC_2 = Location(-96777, 258970, -3623)

        // Time: 868s
        private val GIRAN_TO_TALKING = arrayOf(
            BoatLocation(51914, 189023, -3610, 150, 800),
            BoatLocation(60567, 189789, -3610, 150, 800),
            BoatLocation(63732, 197457, -3610, 200, 800),
            BoatLocation(63732, 219946, -3610, 250, 800),
            BoatLocation(62008, 222240, -3610, 250, 1200),
            BoatLocation(56115, 226791, -3610, 250, 1200),
            BoatLocation(40384, 226432, -3610, 300, 800),
            BoatLocation(37760, 226432, -3610, 300, 800),
            BoatLocation(27153, 226791, -3610, 300, 800),
            BoatLocation(12672, 227535, -3610, 300, 800),
            BoatLocation(-1808, 228280, -3610, 300, 800),
            BoatLocation(-22165, 230542, -3610, 300, 800),
            BoatLocation(-42523, 235205, -3610, 300, 800),
            BoatLocation(-68451, 259560, -3610, 250, 800),
            BoatLocation(-70848, 261696, -3610, 200, 800),
            BoatLocation(-83344, 261610, -3610, 200, 800),
            BoatLocation(-88344, 261660, -3610, 180, 800),
            BoatLocation(-92344, 261660, -3610, 180, 800),
            BoatLocation(-94242, 261659, -3610, 150, 800)
        )

        private val TALKING_DOCK = arrayOf(BoatLocation(-96622, 261660, -3610, 150, 800))

        // Time: 1398s
        private val TALKING_TO_GIRAN = arrayOf(
            BoatLocation(-113925, 261660, -3610, 150, 800),
            BoatLocation(-126107, 249116, -3610, 180, 800),
            BoatLocation(-126107, 234499, -3610, 180, 800),
            BoatLocation(-126107, 219882, -3610, 180, 800),
            BoatLocation(-109414, 204914, -3610, 180, 800),
            BoatLocation(-92807, 204914, -3610, 180, 800),
            BoatLocation(-80425, 216450, -3610, 250, 800),
            BoatLocation(-68043, 227987, -3610, 250, 800),
            BoatLocation(-63744, 231168, -3610, 250, 800),
            BoatLocation(-60844, 231369, -3610, 250, 1800),
            BoatLocation(-44915, 231369, -3610, 200, 800),
            BoatLocation(-28986, 231369, -3610, 200, 800),
            BoatLocation(8233, 207624, -3610, 200, 800),
            BoatLocation(21470, 201503, -3610, 180, 800),
            BoatLocation(40058, 195383, -3610, 180, 800),
            BoatLocation(43022, 193793, -3610, 150, 800),
            BoatLocation(45986, 192203, -3610, 150, 800),
            BoatLocation(48950, 190613, -3610, 150, 800)
        )

        private val GIRAN_DOCK = TALKING_TO_GIRAN[TALKING_TO_GIRAN.size - 1]

        fun load() {
            val boat = BoatManager.getNewBoat(2, 48950, 190613, -3610, 60800)
            if (boat != null) {
                boat.registerEngine(BoatGiranTalking(boat))
                boat.runEngine(180000)
            }
        }
    }
}
