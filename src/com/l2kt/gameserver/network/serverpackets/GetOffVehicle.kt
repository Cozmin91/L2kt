package com.l2kt.gameserver.network.serverpackets

/**
 * @author Maktakien
 */
class GetOffVehicle(
    private val _charObjId: Int,
    private val _boatObjId: Int,
    private val _x: Int,
    private val _y: Int,
    private val _z: Int
) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x5D)
        writeD(_charObjId)
        writeD(_boatObjId)
        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}