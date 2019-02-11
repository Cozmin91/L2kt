package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature

/**
 * @author KenM
 */
class FlyToLocation(
    cha: Creature,
    private val _destX: Int,
    private val _destY: Int,
    private val _destZ: Int,
    private val _type: FlyType
) : L2GameServerPacket() {
    private val _chaObjId: Int = cha.objectId
    private val _chaX: Int = cha.x
    private val _chaY: Int = cha.y
    private val _chaZ: Int = cha.z

    enum class FlyType {
        THROW_UP,
        THROW_HORIZONTAL,
        DUMMY
    }

    constructor(cha: Creature, dest: WorldObject, type: FlyType) : this(cha, dest.x, dest.y, dest.z, type) {}

    override fun writeImpl() {
        writeC(0xC5)
        writeD(_chaObjId)
        writeD(_destX)
        writeD(_destY)
        writeD(_destZ)
        writeD(_chaX)
        writeD(_chaY)
        writeD(_chaZ)
        writeD(_type.ordinal)
    }
}