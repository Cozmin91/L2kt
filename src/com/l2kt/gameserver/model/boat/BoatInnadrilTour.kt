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

class BoatInnadrilTour(private val _boat: Boat) : Runnable {
    private var _cycle = 0

    private val ARRIVED_AT_INNADRIL: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ANCHOR_10_MINUTES)
    private val LEAVE_INNADRIL5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_5_MINUTES)
    private val LEAVE_INNADRIL1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_IN_1_MINUTE)
    private val LEAVE_INNADRIL0: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVE_SOON)
    private val LEAVING_INNADRIL: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_LEAVING)

    private val ARRIVAL20: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_20_MINUTES)
    private val ARRIVAL15: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_15_MINUTES)
    private val ARRIVAL10: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_10_MINUTES)
    private val ARRIVAL5: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_5_MINUTES)
    private val ARRIVAL1: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.INNADRIL_BOAT_ARRIVE_1_MINUTE)

    private val INNADRIL_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)

    private val INNADRIL_SOUND_LEAVE_5MIN: PlaySound = PlaySound(0, "itemsound.ship_5min", _boat)
    private val INNADRIL_SOUND_LEAVE_1MIN: PlaySound = PlaySound(0, "itemsound.ship_1min", _boat)

    override fun run() {
        when (_cycle) {
            0 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL5)
                _boat.broadcastPacket(INNADRIL_SOUND_LEAVE_5MIN)
                ThreadPool.schedule(this, 240000)
            }
            1 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL1)
                _boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 40000)
            }
            2 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, LEAVE_INNADRIL0)
                _boat.broadcastPacket(INNADRIL_SOUND_LEAVE_1MIN)
                ThreadPool.schedule(this, 20000)
            }
            3 -> {
                BoatManager.broadcastPackets(DOCK, DOCK, LEAVING_INNADRIL, INNADRIL_SOUND)
                _boat.payForRide(0, 1, OUST_LOC)
                _boat.executePath(TOUR)
                ThreadPool.schedule(this, 650000)
            }
            4 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, ARRIVAL20)
                ThreadPool.schedule(this, 300000)
            }
            5 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, ARRIVAL15)
                ThreadPool.schedule(this, 300000)
            }
            6 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, ARRIVAL10)
                ThreadPool.schedule(this, 300000)
            }
            7 -> {
                BoatManager.broadcastPacket(DOCK, DOCK, ARRIVAL5)
                ThreadPool.schedule(this, 240000)
            }
            8 -> BoatManager.broadcastPacket(DOCK, DOCK, ARRIVAL1)
            9 -> {
                BoatManager.broadcastPackets(DOCK, DOCK, ARRIVED_AT_INNADRIL, INNADRIL_SOUND)
                ThreadPool.schedule(this, 300000)
            }
        }
        _cycle++
        if (_cycle > 9)
            _cycle = 0
    }

    companion object {
        private val OUST_LOC = Location(107092, 219098, -3952)

        // Time: 1867s
        private val TOUR = arrayOf(
            BoatLocation(105129, 226240, -3610, 150, 800),
            BoatLocation(90604, 238797, -3610, 150, 800),
            BoatLocation(74853, 237943, -3610, 150, 800),
            BoatLocation(68207, 235399, -3610, 150, 800),
            BoatLocation(63226, 230487, -3610, 150, 800),
            BoatLocation(61843, 224797, -3610, 150, 800),
            BoatLocation(61822, 203066, -3610, 150, 800),
            BoatLocation(59051, 197685, -3610, 150, 800),
            BoatLocation(54048, 195298, -3610, 150, 800),
            BoatLocation(41609, 195687, -3610, 150, 800),
            BoatLocation(35821, 200284, -3610, 150, 800),
            BoatLocation(35567, 205265, -3610, 150, 800),
            BoatLocation(35617, 222471, -3610, 150, 800),
            BoatLocation(37932, 226588, -3610, 150, 800),
            BoatLocation(42932, 229394, -3610, 150, 800),
            BoatLocation(74324, 245231, -3610, 150, 800),
            BoatLocation(81872, 250314, -3610, 150, 800),
            BoatLocation(101692, 249882, -3610, 150, 800),
            BoatLocation(107907, 256073, -3610, 150, 800),
            BoatLocation(112317, 257133, -3610, 150, 800),
            BoatLocation(126273, 255313, -3610, 150, 800),
            BoatLocation(128067, 250961, -3610, 150, 800),
            BoatLocation(128520, 238249, -3610, 150, 800),
            BoatLocation(126428, 235072, -3610, 150, 800),
            BoatLocation(121843, 234656, -3610, 150, 800),
            BoatLocation(120096, 234268, -3610, 150, 800),
            BoatLocation(118572, 233046, -3610, 150, 800),
            BoatLocation(117671, 228951, -3610, 150, 800),
            BoatLocation(115936, 226540, -3610, 150, 800),
            BoatLocation(113628, 226240, -3610, 150, 800),
            BoatLocation(111300, 226240, -3610, 150, 800),
            BoatLocation(111264, 226240, -3610, 150, 800)
        )

        private val DOCK = TOUR[TOUR.size - 1]

        fun load() {
            val boat = BoatManager.getNewBoat(4, 111264, 226240, -3610, 32768)
            if (boat != null) {
                boat.registerEngine(BoatInnadrilTour(boat))
                boat.runEngine(180000)
            }
        }
    }
}