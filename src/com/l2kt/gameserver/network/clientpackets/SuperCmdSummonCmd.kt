package com.l2kt.gameserver.network.clientpackets

/**
 * Format chS c: (id) 0x39 h: (subid) 0x01 S: the summon name (or maybe cmd string ?)
 * @author -Wooden-
 */
class SuperCmdSummonCmd : L2GameClientPacket() {
    private var _summonName: String = ""

    override fun readImpl() {
        _summonName = readS()
    }

    override fun runImpl() {}
}