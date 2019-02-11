package com.l2kt.gameserver.network.serverpackets

/**
 * @author Kerberos
 */
class NpcSay(private val _objectId: Int, private val _textType: Int, npcId: Int, private val _text: String) :
    L2GameServerPacket() {
    private val _npcId: Int = 1000000 + npcId

    override fun writeImpl() {
        writeC(0x02)
        writeD(_objectId)
        writeD(_textType)
        writeD(_npcId)
        writeS(_text)
    }
}