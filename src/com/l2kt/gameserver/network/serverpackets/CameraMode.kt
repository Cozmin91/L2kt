package com.l2kt.gameserver.network.serverpackets

class CameraMode
/**
 * Forces client camera mode change
 * @param mode 0 - third person cam 1 - first person cam
 */
    (private val _mode: Int) : L2GameServerPacket() {

    public override fun writeImpl() {
        writeC(0xf1)
        writeD(_mode)
    }
}