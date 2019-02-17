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

class BoatRunePrimeval(private val _boat: Boat) : Runnable {
    private var _cycle = 0
    private var _shoutCount = 0

    private val ARRIVED_AT_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.ARRIVED_AT_RUNE)
    private val ARRIVED_AT_RUNE_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_PRIMEVAL_3_MINUTES)
    private val LEAVING_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_RUNE_FOR_PRIMEVAL_NOW)
    private val ARRIVED_AT_PRIMEVAL: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_ARRIVED_AT_PRIMEVAL)
    private val ARRIVED_AT_PRIMEVAL_2: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_FOR_RUNE_3_MINUTES)
    private val LEAVING_PRIMEVAL: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_LEAVING_PRIMEVAL_FOR_RUNE_NOW)
    private val BUSY_RUNE: CreatureSay = CreatureSay(0, Say2.BOAT, 801, SystemMessageId.FERRY_FROM_PRIMEVAL_TO_RUNE_DELAYED)

    private val RUNE_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)
    private val PRIMEVAL_SOUND: PlaySound = PlaySound(0, "itemsound.ship_arrival_departure", _boat)

    override fun run() {
        when (_cycle) {
            0 -> {
                BoatManager.dockBoat(BoatManager.RUNE_HARBOR, false)
                BoatManager.broadcastPackets(RUNE_DOCK[0], PRIMEVAL_DOCK, LEAVING_RUNE, RUNE_SOUND)
                _boat.payForRide(8925, 1, OUST_LOC_1)
                _boat.executePath(RUNE_TO_PRIMEVAL)
            }
            1 -> {
                BoatManager.broadcastPackets(
                    PRIMEVAL_DOCK,
                    RUNE_DOCK[0],
                    ARRIVED_AT_PRIMEVAL,
                    ARRIVED_AT_PRIMEVAL_2,
                    PRIMEVAL_SOUND
                )
                ThreadPool.schedule(this, 180000)
            }
            2 -> {
                BoatManager.broadcastPackets(PRIMEVAL_DOCK, RUNE_DOCK[0], LEAVING_PRIMEVAL, PRIMEVAL_SOUND)
                _boat.payForRide(8924, 1, OUST_LOC_2)
                _boat.executePath(PRIMEVAL_TO_RUNE)
            }
            3 -> {
                if (BoatManager.isBusyDock(BoatManager.RUNE_HARBOR)) {
                    if (_shoutCount == 0)
                        BoatManager.broadcastPacket(RUNE_DOCK[0], PRIMEVAL_DOCK, BUSY_RUNE)

                    _shoutCount++
                    if (_shoutCount > 35)
                        _shoutCount = 0

                    ThreadPool.schedule(this, 5000)
                    return
                }
                BoatManager.dockBoat(BoatManager.RUNE_HARBOR, true)
                _boat.executePath(RUNE_DOCK)
            }
            4 -> {
                BoatManager.broadcastPackets(
                    RUNE_DOCK[0],
                    PRIMEVAL_DOCK,
                    ARRIVED_AT_RUNE,
                    ARRIVED_AT_RUNE_2,
                    RUNE_SOUND
                )
                ThreadPool.schedule(this, 180000)
            }
        }
        _shoutCount = 0

        _cycle++
        if (_cycle > 4)
            _cycle = 0
    }

    companion object {
        private val OUST_LOC_1 = Location(34513, -38009, -3640)
        private val OUST_LOC_2 = Location(10447, -24982, -3664)

        // Time: 239s
        private val RUNE_TO_PRIMEVAL = arrayOf(
            BoatLocation(32750, -39300, -3610, 180, 800),
            BoatLocation(27440, -39328, -3610, 250, 1000),
            BoatLocation(19616, -39360, -3610, 270, 1000),
            BoatLocation(3840, -38528, -3610, 270, 1000),
            BoatLocation(1664, -37120, -3610, 270, 1000),
            BoatLocation(896, -34560, -3610, 180, 1800),
            BoatLocation(832, -31104, -3610, 180, 180),
            BoatLocation(2240, -29132, -3610, 150, 1800),
            BoatLocation(4160, -27828, -3610, 150, 1800),
            BoatLocation(5888, -27279, -3610, 150, 1800),
            BoatLocation(7000, -27279, -3610, 150, 1800),
            BoatLocation(10342, -27279, -3610, 150, 1800)
        )

        // Time: 221s
        private val PRIMEVAL_TO_RUNE = arrayOf(
            BoatLocation(15528, -27279, -3610, 180, 800),
            BoatLocation(22304, -29664, -3610, 290, 800),
            BoatLocation(33824, -26880, -3610, 290, 800),
            BoatLocation(38848, -21792, -3610, 240, 1200),
            BoatLocation(43424, -22080, -3610, 180, 1800),
            BoatLocation(44320, -25152, -3610, 180, 1800),
            BoatLocation(40576, -31616, -3610, 250, 800),
            BoatLocation(36819, -35315, -3610, 220, 800)
        )

        private val RUNE_DOCK = arrayOf(BoatLocation(34381, -37680, -3610, 220, 800))

        private val PRIMEVAL_DOCK = RUNE_TO_PRIMEVAL[RUNE_TO_PRIMEVAL.size - 1]

        fun load() {
            val boat = BoatManager.getNewBoat(5, 34381, -37680, -3610, 40785)
            if (boat != null) {
                boat.registerEngine(BoatRunePrimeval(boat))
                boat.runEngine(180000)
                BoatManager.dockBoat(BoatManager.RUNE_HARBOR, true)
            }
        }
    }
}