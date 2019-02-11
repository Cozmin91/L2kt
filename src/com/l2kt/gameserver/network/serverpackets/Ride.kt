package com.l2kt.gameserver.network.serverpackets

class Ride(private val _id: Int, private val _bRide: Int, rideClassId: Int) : L2GameServerPacket() {
    var mountType: Int = 0
        private set
    private val _rideClassID: Int = rideClassId + 1000000

    init {
        // npcID

        when (rideClassId) {
            12526, 12527 // Striders
                , 12528 -> mountType = 1

            12621 // Wyvern
            -> mountType = 2
        }
    }// charobjectID
    // 1 for mount ; 2 for dismount

    override fun writeImpl() {
        writeC(0x86)
        writeD(_id)
        writeD(_bRide)
        writeD(mountType)
        writeD(_rideClassID)
    }

    companion object {
        const val ACTION_MOUNT = 1
        const val ACTION_DISMOUNT = 0
    }
}