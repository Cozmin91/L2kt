package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject

class DeleteObject : L2GameServerPacket {
    private val _objectId: Int
    private val _isSeated: Boolean

    constructor(obj: WorldObject) {
        _objectId = obj.objectId
        _isSeated = false
    }

    constructor(obj: WorldObject, sit: Boolean) {
        _objectId = obj.objectId
        _isSeated = sit
    }

    override fun writeImpl() {
        writeC(0x12)
        writeD(_objectId)
        writeD(if (_isSeated) 0x00 else 0x01) // 0 - stand up and delete, 1 - delete
    }
}