package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.location.Location

/**
 * Format: (ch) d[ddddd]
 * @author -Wooden-
 */
class ExCursedWeaponLocation(private val _cursedWeaponInfo: List<CursedWeaponInfo>) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x46)

        if (!_cursedWeaponInfo.isEmpty()) {
            writeD(_cursedWeaponInfo.size)
            for (w in _cursedWeaponInfo) {
                writeD(w.id)
                writeD(w.activated)

                writeD(w.pos.x)
                writeD(w.pos.y)
                writeD(w.pos.z)
            }
        } else {
            writeD(0)
            writeD(0)
        }
    }

    class CursedWeaponInfo(
        var pos: Location, var id: Int, var activated: Int // 0 - not activated ? 1 - activated
    )
}