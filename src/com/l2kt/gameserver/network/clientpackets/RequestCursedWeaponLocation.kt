package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.network.serverpackets.ExCursedWeaponLocation
import com.l2kt.gameserver.network.serverpackets.ExCursedWeaponLocation.CursedWeaponInfo
import java.util.*

class RequestCursedWeaponLocation : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return

        val list = ArrayList<CursedWeaponInfo>()
        for (cw in CursedWeaponManager.cursedWeapons) {
            if (!cw.isActive)
                continue

            val loc = cw.worldPosition
            if (loc != null)
                list.add(CursedWeaponInfo(loc, cw.itemId, if (cw.isActivated) 1 else 0))
        }

        if (!list.isEmpty())
            player.sendPacket(ExCursedWeaponLocation(list))
    }
}