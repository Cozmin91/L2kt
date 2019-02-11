package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.network.serverpackets.ExCursedWeaponList

class RequestCursedWeaponList : L2GameClientPacket() {
    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar ?: return
        player.sendPacket(ExCursedWeaponList(CursedWeaponManager.getInstance().cursedWeaponsIds))
    }
}