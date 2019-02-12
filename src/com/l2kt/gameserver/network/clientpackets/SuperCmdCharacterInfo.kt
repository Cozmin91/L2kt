package com.l2kt.gameserver.network.clientpackets

/**
 * Format chS c: (id) 0x39 h: (subid) 0x00 S: the character name (or maybe cmd string ?)
 * @author -Wooden-
 */
class SuperCmdCharacterInfo : L2GameClientPacket() {
    private var _characterName: String = ""

    override fun readImpl() {
        _characterName = readS()
    }

    override fun runImpl() {}
}