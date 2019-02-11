package com.l2kt.gameserver.network.serverpackets

class ExCursedWeaponList(private val _cursedWeaponIds: Set<Int>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x45)

        writeD(_cursedWeaponIds.size)
        for (id in _cursedWeaponIds)
            writeD(id)
    }
}