package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.data.xml.HennaData
import com.l2kt.gameserver.network.serverpackets.HennaItemInfo

class RequestHennaItemInfo : L2GameClientPacket() {
    private var _symbolId: Int = 0

    override fun readImpl() {
        _symbolId = readD()
    }

    override fun runImpl() {
        val activeChar = client.activeChar ?: return

        val template = HennaData.getInstance().getHenna(_symbolId) ?: return

        activeChar.sendPacket(HennaItemInfo(template, activeChar))
    }
}