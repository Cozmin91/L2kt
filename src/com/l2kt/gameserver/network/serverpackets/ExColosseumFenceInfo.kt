package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Fence

/**
 * Format: (ch)ddddddd d: object id d: type (00 - no fence, 01 - only 4 columns, 02 - columns with fences) d: x coord d: y coord d: z coord d: width d: height
 */
class ExColosseumFenceInfo(private val _objectId: Int, private val _fence: Fence) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x09)

        writeD(_objectId)
        writeD(_fence.type)
        writeD(_fence.x)
        writeD(_fence.y)
        writeD(_fence.z)
        writeD(_fence.sizeX)
        writeD(_fence.sizeY)
    }
}