package com.l2kt.gameserver.network.serverpackets

/**
 * Send Private (Friend) Message Format: c dSSS d: Unknown S: Sending Player S: Receiving Player S: Message
 * @author Tempy
 */
class L2FriendSay(private val _sender: String, private val _receiver: String, private val _message: String) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfd)
        writeD(0) // ??
        writeS(_receiver)
        writeS(_sender)
        writeS(_message)
    }
}