package com.l2kt.gameserver.network.gameserverpackets

class ServerStatus : GameServerBasePacket() {
    private val _attributes = mutableListOf<Attribute>()

    override val content: ByteArray
        get() {
            writeC(0x06)
            writeD(_attributes.size)
            for (temp in _attributes) {
                writeD(temp.id)
                writeD(temp.value)
            }

            return bytes
        }

    internal inner class Attribute(var id: Int, var value: Int)

    fun addAttribute(id: Int, value: Int) {
        _attributes.add(Attribute(id, value))
    }

    fun addAttribute(id: Int, onOrOff: Boolean) {
        _attributes.add(Attribute(id, if (onOrOff) ServerStatus.ON else ServerStatus.OFF))
    }

    companion object {

        val STATUS_STRING = arrayOf("Auto", "Good", "Normal", "Full", "Down", "Gm Only")

        const val STATUS = 0x01
        const val CLOCK = 0x02
        const val BRACKETS = 0x03
        const val AGE_LIMIT = 0x04
        const val TEST_SERVER = 0x05
        const val PVP_SERVER = 0x06
        const val MAX_PLAYERS = 0x07

        const val STATUS_AUTO = 0x00
        const val STATUS_GOOD = 0x01
        const val STATUS_NORMAL = 0x02
        const val STATUS_FULL = 0x03
        const val STATUS_DOWN = 0x04
        const val STATUS_GM_ONLY = 0x05

        const val ON = 0x01
        const val OFF = 0x00
    }
}