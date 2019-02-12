package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player

class PrivateStoreMsgBuy(private val _activeChar: Player) : L2GameServerPacket() {
    private var _storeMsg: String? = _activeChar.buyList?.title ?: ""

    override fun writeImpl() {
        writeC(0xb9)
        writeD(_activeChar.objectId)
        writeS(_storeMsg)
    }
}