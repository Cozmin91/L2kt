package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.HennaData
import com.l2kt.gameserver.network.serverpackets.HennaEquipList

class RequestHennaList : L2GameClientPacket() {
    private var _unknown: Int = 0

    override fun readImpl() {
        _unknown = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        activeChar.sendPacket(HennaEquipList(activeChar, HennaData.getInstance().getAvailableHennasFor(activeChar)))
    }
}