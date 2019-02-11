package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature

/**
 * This packet is used to move characters to a target.<br></br>
 * It is aswell used to rotate characters in front of the target.
 */
class MoveToPawn(cha: Creature, target: WorldObject, private val _distance: Int) : L2GameServerPacket() {
    private val _objectId: Int = cha.objectId
    private val _targetId: Int = target.objectId
    private val _x: Int = cha.x
    private val _y: Int = cha.y
    private val _z: Int = cha.z

    override fun writeImpl() {
        writeC(0x60)

        writeD(_objectId)
        writeD(_targetId)
        writeD(_distance)

        writeD(_x)
        writeD(_y)
        writeD(_z)
    }
}