package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class PrivateStoreMsgSell(private val _activeChar: Player) : L2GameServerPacket() {
    private var _storeMsg: String? = _activeChar.sellList?.title

    override fun writeImpl() {
        writeC(0x9c)
        writeD(_activeChar.objectId)
        writeS(_storeMsg)
    }
}