package com.l2kt.loginserver.network.serverpackets

import com.l2kt.loginserver.network.LoginClient

/**
 * Format: dd b dddd s d: session id d: protocol revision b: 0x90 bytes : 0x80 bytes for the scrambled RSA public key 0x10 bytes at 0x00 d: unknow d: unknow d: unknow d: unknow s: blowfish key
 */
class Init(private val _publicKey: ByteArray, private val _blowfishKey: ByteArray, private val _sessionId: Int) :
    L2LoginServerPacket() {

    constructor(client: LoginClient) : this(client.scrambledModulus, client.blowfishKey, client.sessionId) {}

    override fun write() {
        writeC(0x00) // init packet id

        writeD(_sessionId) // session id
        writeD(0x0000c621) // protocol revision

        writeB(_publicKey) // RSA Public Key
        writeD(0x29DD954E)
        writeD(0x77C39CFC)
        writeD(-0x685249e0)
        writeD(0x07BDE0F7)

        writeB(_blowfishKey) // BlowFish key
        writeC(0x00) // null termination ;)
    }
}
