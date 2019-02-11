package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.StaticObject

/**
 * format dd
 */
class StaticObjectInfo(private val _staticObject: StaticObject) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x99)
        writeD(_staticObject.staticObjectId) // staticObjectId
        writeD(_staticObject.objectId) // objectId
    }
}